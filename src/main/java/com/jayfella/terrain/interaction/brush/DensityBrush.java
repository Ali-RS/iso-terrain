package com.jayfella.terrain.interaction.brush;

import com.jayfella.terrain.chunk.Chunk;
import com.jayfella.terrain.chunk.GridPosition;
import com.jayfella.terrain.interaction.brush.shape.BrushShape;
import com.jayfella.terrain.material.WorldMaterial;
import com.jayfella.terrain.world.World;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A brush of specified shape to modify a voxel/densityfield isosurface.
 */
public class DensityBrush {

    private final Logger log = LoggerFactory.getLogger(DensityBrush.class);

    private World world;

    private Vector3f size;
    private WorldMaterial worldMaterial;
    private BrushShape brushShape;

    private final List<BrushListener> brushListeners = new ArrayList<>();

    public DensityBrush(World world) {
        this.world = world;
    }

    public Vector3f getSize() {
        return this.size;
    }

    public void setSize(Vector3f size) {
        this.size = size;
        brushListeners.forEach(l -> l.SizeChanged(size));
    }

    public WorldMaterial getWorldMaterial() {
        return this.worldMaterial;
    }

    public void setWorldMaterial(WorldMaterial material) {
        this.worldMaterial = material;
        brushListeners.forEach(l -> l.worldMaterialChanged(material));
    }

    public BrushShape getBrushShape() {
        return this.brushShape;
    }

    public void setBrushShape(BrushShape brushShape) {
        this.brushShape = brushShape;
        this.brushListeners.forEach(l -> l.brushChanged(brushShape));
    }

    public void apply(Vector3f position, float density, float smooth) {
        Map<Vector3f, Float> result = this.brushShape.apply(position, density, smooth, size);
        rebuildAffectedChunks(result);
    }

    public void addBrushListener(BrushListener brushListener) {
        this.brushListeners.add(brushListener);
    }

    public void removeBrushListener(BrushListener brushListener) {
        this.brushListeners.remove(brushListener);
    }

    private void rebuildAffectedChunks(Map<Vector3f, Float> affectedAreas) {

        int skirt = 3;

        // make a unique list of the affected Chunks so we don't re-build the same affected chunks multiple times.
        Set<Chunk> affectedChunks = new HashSet<>();

        affectedAreas
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().y > 2.0f)
                .forEach(entry -> {

                    Vector3f worldLoc = entry.getKey();
                    float density = entry.getValue();

                    // get the chunk
                    GridPosition gp = GridPosition.fromWorldLocation(worldLoc);
                    Chunk chunk = world.getChunk(gp);

                    if (chunk == null) {
                        return; // we're in a lambda - it will just stop the current iteration, not the entire loop.
                    }

                    // get the local co-ordinates by subtracting the chunk location from the world location.
                    Vector3f localCoords = worldLoc.subtract(chunk.getChunkNode().getLocalTranslation());

                    // convert them to integers and ensure they are not negative
                    // because these co-ordinates are essentially an array index.
                    int localX = (int) FastMath.abs(localCoords.x);
                    int localY = (int) FastMath.abs(localCoords.y);
                    int localZ = (int) FastMath.abs(localCoords.z);

                    // modify this chunk
                    chunk.setVoxel(localX, localY, localZ, getWorldMaterial(), density);
                    affectedChunks.add(chunk);

                    // check if this affects any nearby skirted areas of local chunks

                    if (localX < skirt) {
                        Chunk skirtChunk = world.getChunk(gp.subtract(1, 0, 0));

                        if (skirtChunk != null) {
                            skirtChunk.setVoxel(16 + localX, localY, localZ, getWorldMaterial(), density);
                            affectedChunks.add(skirtChunk);
                        }
                    }

                    if (localY < skirt && gp.getY() > 0) {
                        Chunk skirtChunk = world.getChunk(gp.subtract(0, 1, 0));

                        if (skirtChunk != null) {
                            skirtChunk.setVoxel(localX, 16 + localY, localZ, getWorldMaterial(), density);
                            affectedChunks.add(skirtChunk);
                        }
                    }

                    if (localZ < skirt) {
                        Chunk skirtChunk = world.getChunk(gp.subtract(0, 0, 1));

                        if (skirtChunk != null) {
                            skirtChunk.setVoxel(localX, localY, 16 + localZ, getWorldMaterial(), density);
                            affectedChunks.add(skirtChunk);
                        }
                    }

                    // corner skirts. where two planes are in the skirt zone, which

                    if (localX < skirt && localZ < skirt) {
                        Chunk skirtChunk = world.getChunk(gp.subtract(1, 0, 1));

                        if (skirtChunk != null) {
                            skirtChunk.setVoxel(16 + localX, localY, 16 + localZ, getWorldMaterial(), density);
                            affectedChunks.add(skirtChunk);
                        }
                    }

                    if (localX < skirt && localY < skirt) {
                        Chunk skirtChunk = world.getChunk(gp.subtract(1, 1, 0));

                        if (skirtChunk != null) {
                            skirtChunk.setVoxel(16 + localX, 16 + localY, localZ, getWorldMaterial(), density);
                            affectedChunks.add(skirtChunk);
                        }
                    }

                    if (localZ < skirt && localY < skirt) {
                        Chunk skirtChunk = world.getChunk(gp.subtract(0, 1, 1));

                        if (skirtChunk != null) {
                            skirtChunk.setVoxel(localX, 16 + localY, 16 + localZ, getWorldMaterial(), density);
                            affectedChunks.add(skirtChunk);
                        }
                    }

                    if (localX < skirt && localY < skirt && localZ < skirt) {
                        Chunk skirtChunk = world.getChunk(gp.subtract(1, 1, 1));

                        if (skirtChunk != null) {
                            skirtChunk.setVoxel(16 + localX, 16 + localY, 16 + localZ, getWorldMaterial(), density);
                            affectedChunks.add(skirtChunk);
                        }
                    }
        });

        affectedChunks.forEach(Chunk::rebuild);

        log.info(String.format("%d points affected %d chunks", affectedAreas.size(), affectedChunks.size()));
    }

}
