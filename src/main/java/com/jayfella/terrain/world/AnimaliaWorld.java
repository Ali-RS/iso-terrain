package com.jayfella.terrain.world;

import com.jayfella.terrain.biome.BiomeGenerator;
import com.jayfella.terrain.chunk.Chunk;
import com.jayfella.terrain.chunk.ChunkLoader;
import com.jayfella.terrain.chunk.GridPosition;
import com.jayfella.terrain.config.StoragePaths;
import com.jayfella.terrain.core.ApplicationContext;
import com.jayfella.terrain.pager.ChunkPager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;

import java.io.File;
import java.nio.file.Paths;

/**
 * Created by James on 24/04/2017.
 */
public class AnimaliaWorld implements World {

    private final ApplicationContext appContext;
    private final WorldType worldType;
    private final long seed;
    private final String name;

    private final Node worldNode;
    private final ChunkLoader chunkLoader;
    private final ChunkPager chunkPager;

    private final BiomeGenerator biomeGenerator;

    public AnimaliaWorld(ApplicationContext appContext, WorldType worldType, long seed, String name) {

        this.appContext = appContext;
        this.worldType = worldType;
        this.seed = seed;
        this.name = name;

        this.worldNode = new Node(String.format("Node - %s", this.toString()));
        this.worldNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        this.chunkLoader = new ChunkLoader(this);
        this.chunkPager = new ChunkPager(this);

        this.biomeGenerator = new BiomeGenerator(seed);

        this.chunkPager.setWorldLocation(new Vector3f(0,0,32), true);

        File worldPath = Paths.get(StoragePaths.SAVEGAME_DIR.toString(), this.getWorldName()).toFile();

        if (!worldPath.exists()) {
            worldPath.mkdirs();
        }


    }

    @Override
    public Node getWorldNode() {
        return this.worldNode;
    }

    @Override
    public long getSeed() {
        return this.seed;
    }

    @Override
    public ApplicationContext getAppContext() {
        return this.appContext;
    }

    @Override
    public ChunkLoader getChunkLoader() {
        return this.chunkLoader;
    }

    @Override
    public ChunkPager getChunkPager() {
        return this.chunkPager;
    }

    @Override
    public BiomeGenerator getBiomeGenerator() {
        return this.biomeGenerator;
    }

    @Override
    public WorldType getWorldType() {
        return this.worldType;
    }

    @Override
    public String getWorldName() {
        return this.name;
    }

    /**
     * Gets the chunk at the specified grid position, or null if it is not loaded.
     * @param gridPosition the grid position of the chunk.
     * @return the chunk, or null if it is not loaded.
     */
    @Override
    public Chunk getChunk(GridPosition gridPosition) {
        return chunkPager.getChunk(gridPosition);
    }

    @Override
    public String toString() {
        return String.format("World [%s] %s", worldType, name);
    }

}
