package com.jayfella.terrain.pager;

import com.jayfella.terrain.chunk.Chunk;
import com.jayfella.terrain.chunk.GridPosition;
import com.jayfella.terrain.world.World;
import com.jme3.math.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads and unloads chunks around a specified location.
 */
public class ChunkPager {

    private final Logger log = LoggerFactory.getLogger(ChunkPager.class);

    private final World world;

    // chunks the pager has loaded. The pager loaded them, so the pager is responsible for them.
    private final Map<GridPosition, Chunk> loadedChunks;
    private int layers = 16;

    public ChunkPager(World world) {
        this.world = world;

        this.loadedChunks = new HashMap<>();
    }

    private GridPosition lastGridPosition = new GridPosition(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    public void setWorldLocation(Vector3f location) {
        setWorldLocation(location, false);
    }

    public void setWorldLocation(Vector3f location, boolean forceReload) {

        long timeStart = System.currentTimeMillis();

        GridPosition newPosition = GridPosition.fromWorldLocation(location);

        if (!forceReload) {
            if (newPosition.equals(lastGridPosition) || (newPosition.getX() == lastGridPosition.getX() && newPosition.getZ() == lastGridPosition.getZ())) {
                return;
            }
        }

        int renderDistance = world.getAppContext().getAppConfig().getVideoConfig().getRenderDistance();

        int tlX = newPosition.getX() - renderDistance;
        int tlZ = newPosition.getZ() - renderDistance;

        int brX = newPosition.getX() + renderDistance;
        int brZ = newPosition.getZ() + renderDistance;

        Map<GridPosition, Chunk> newChunks = new HashMap<>();

        // get all new chunks. Either get them from the pre-loaded map, or generate them if necessary.
        for (int x = tlX; x <= brX; x++) {
            for (int z = tlZ; z <= brZ; z++) {
                for (int y = 0; y < layers; y++) {

                    GridPosition gp = new GridPosition(x, y, z);
                    Chunk chunk = this.loadedChunks.get(gp);

                    if (chunk == null) {
                        chunk = world.getChunkLoader().loadChunk(gp);
                    }

                    newChunks.put(gp, chunk);
                }
            }
        }

        this.loadedChunks
                .entrySet()
                .removeIf(entry -> {
                    if (!newChunks.containsKey(entry.getKey())) {
                        world.getChunkLoader().disposeChunk(entry.getValue());
                        return true;
                    }

                    return false;
                });


        // add all the new chunks into the loaded list.
        this.loadedChunks.putAll(newChunks);

        this.lastGridPosition = newPosition;

        long timeEnd = System.currentTimeMillis();

        log.debug(String.format("Paged Chunks in %d ms", (timeEnd - timeStart)));
    }

    public int getChunksLoadedCount() {
        return this.loadedChunks.size();
    }

    public int getChunksRequired() {
        int renderDistance = world.getAppContext().getAppConfig().getVideoConfig().getRenderDistance();

        int dimensionSize = (renderDistance * 2) + 1;

        return (dimensionSize * dimensionSize) * layers;
    }

    public Chunk getChunk(GridPosition gridPosition) {
        return this.loadedChunks.get(gridPosition);
    }

}
