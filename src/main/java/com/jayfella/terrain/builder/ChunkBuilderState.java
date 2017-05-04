package com.jayfella.terrain.builder;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jayfella.terrain.chunk.Chunk;
import com.jayfella.terrain.world.World;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Builds and disposes of chunks in a multi-threaded environment.
 */
public class ChunkBuilderState extends BaseAppState {

    private int loadedChunks = 0;

    private final Logger log = LoggerFactory.getLogger(ChunkBuilderState.class);

    private final World world;

    private final ThreadPoolExecutor threadpool;
    private int threadCount;

    private final List<Future<Chunk>> awaitingBuild = new ArrayList<>();
    private final List<Chunk> awaitingDisposal = new ArrayList<>();

    public ChunkBuilderState(World world, int nThreads) {
        this.world = world;
        this.threadCount = nThreads;

        // for building chunks - lower priority
        ThreadFactoryBuilder factoryBuilder = new ThreadFactoryBuilder();
        factoryBuilder.setNameFormat("Chunk-Build-Thread-%d");
        // factoryBuilder.setPriority(10);

        // this.threadpool = Executors.newCachedThreadPool(factoryBuilder.build());

        this.threadpool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue<>(100, new PriorityComparator())) {

            protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
                RunnableFuture<T> newTaskFor = super.newTaskFor(callable);
                return new PriorityFuture<>(newTaskFor, ((Chunk) callable).getPriority());
            }
        };

        this.threadpool.setThreadFactory(factoryBuilder.build());

    }

    @Override protected void initialize(Application app) { }
    @Override protected void onEnable() { }
    @Override protected void onDisable() { }

    @Override
    protected void cleanup(Application app) {
        this.threadpool.shutdown();
    }

    public int getLoadedChunkCount() {
        return this.loadedChunks;
    }

    public int getThreadCount() {
        return this.threadCount;
    }

    public void setThreadCount(int nThreads) {
        this.threadCount = nThreads;
        this.threadpool.setCorePoolSize(this.threadCount);
    }

    public void buildChunk(Callable<Chunk> ref) {
        Future<Chunk> future = this.threadpool.submit(ref);
        this.awaitingBuild.add(future);
    }

    public void disposeChunk(Chunk chunk) {
        this.awaitingDisposal.add(chunk);
    }

    public int getAwaitingBuildCount() {
        return this.awaitingBuild.size();
    }

    public int getAwaitingDisposalCount() {
        return this.awaitingDisposal.size();
    }

    @Override
    public void update(float tpf) {

        // for now we'll add and remove as many chunks as possible per frame.
        awaitingDisposal.removeIf(chunk -> {

            if (chunk.disposeSafely()) {
                this.loadedChunks--;
                return true;
            }

            return false;
        });

        awaitingBuild.removeIf(future -> {

            if (future.isDone()) {

                try {
                    Chunk newChunk = future.get();
                    newChunk.applyToScene();

                    if (newChunk.isMarkedForRebuild()) {
                        newChunk.unmarkForRebuild();
                    }
                    else {
                        this.loadedChunks++;
                    }
                }
                catch (InterruptedException | ExecutionException ex) {
                    log.warn("Error building chunk", ex);
                }

                return true;
            }
            else {
                return false;
            }

        });


    }

}
