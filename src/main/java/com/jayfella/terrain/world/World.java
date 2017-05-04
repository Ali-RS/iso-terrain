package com.jayfella.terrain.world;

import com.jayfella.terrain.biome.BiomeGenerator;
import com.jayfella.terrain.chunk.Chunk;
import com.jayfella.terrain.chunk.ChunkLoader;
import com.jayfella.terrain.chunk.GridPosition;
import com.jayfella.terrain.core.ApplicationContext;
import com.jayfella.terrain.pager.ChunkPager;
import com.jme3.scene.Node;

/**
 * Represents a World
 */
public interface World {

    Node getWorldNode();
    long getSeed();
    ApplicationContext getAppContext();
    ChunkLoader getChunkLoader();
    ChunkPager getChunkPager();
    BiomeGenerator getBiomeGenerator();

    WorldType getWorldType();
    String getWorldName();

    Chunk getChunk(GridPosition gridPosition);

}
