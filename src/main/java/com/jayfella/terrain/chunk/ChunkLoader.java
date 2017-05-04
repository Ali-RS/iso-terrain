package com.jayfella.terrain.chunk;

import com.jayfella.terrain.builder.ChunkBuilderState;
import com.jayfella.terrain.iso.DensityVolume;
import com.jayfella.terrain.iso.fractal.ModifiableDensityVolume;
import com.jayfella.terrain.world.World;
import com.jme3.math.Vector3f;

/**
 * Loads and unloads chunks for a specified world.
 */
public class ChunkLoader {

    private final World world;
    private final ModifiableDensityVolume densityVolume;
    private final ChunkBuilderState chunkBuilder;

    public ChunkLoader(World world) {
        this.world = world;
        // this.densityVolume = new GemsFractalDensityVolume(world.getSeed());
        this.densityVolume = new ModifiableDensityVolume(world);

        int availableProcessors = Runtime.getRuntime().availableProcessors() - 1;
        this.chunkBuilder = new ChunkBuilderState(this.world, availableProcessors);
        world.getAppContext().getAppStateManager().attach(this.chunkBuilder);
    }

    public Chunk loadChunk(GridPosition gridPosition) {

        WorldChunk chunk = new WorldChunk(this.world, gridPosition);

        // set the priority based on how close the camera is to the chunk.

        GridPosition camPos = GridPosition.fromWorldLocation(world.getAppContext().getCamera().getLocation());

        int distX = Math.abs(gridPosition.getX() - camPos.getX());
        // int distY = Math.abs(gridPosition.getY() - camPos.getY());
        int distZ = Math.abs(gridPosition.getZ() - camPos.getZ());

        // int min = Math.min(distX, (distY > distZ) ? distZ : distY);
        int min = Math.min(distX, distZ);



        // int viewDistance = world.getAppContext().getAppConfig().getVideoConfig().getRenderDistance();

        // int priority = viewDistance - min;

        chunk.setPriority(min);

        chunkBuilder.buildChunk(chunk);
        return chunk;
    }

    public void disposeChunk(Chunk chunk) {
        this.chunkBuilder.disposeChunk(chunk);
    }

    public DensityVolume getDensityVolume() {
        return this.densityVolume;
    }

    public ChunkBuilderState getChunkBuilder() {
        return this.chunkBuilder;
    }

    public void modifyDensity(Vector3f loc, float density) {
        this.densityVolume.setDensity(loc, density);

        // and rebuild the chunk.
    }

}
