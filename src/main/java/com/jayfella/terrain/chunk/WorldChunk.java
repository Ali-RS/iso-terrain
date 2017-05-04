package com.jayfella.terrain.chunk;

import com.jayfella.terrain.Main;
import com.jayfella.terrain.config.StoragePaths;
import com.jayfella.terrain.iso.MeshGenerator;
import com.jayfella.terrain.iso.mc.MarchingCubesMeshGenerator;
import com.jayfella.terrain.iso.volume.ArrayDensityVolume;
import com.jayfella.terrain.material.WorldMaterial;
import com.jayfella.terrain.util.GeometryUtils;
import com.jayfella.terrain.world.World;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.FloatBuffer;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A pre-configured world.
 */
public class WorldChunk implements Chunk, Callable<Chunk> {

    private final Logger log = LoggerFactory.getLogger(WorldChunk.class);

    private File file;

    private final World world;
    private final GridPosition gridPosition;

    private final AtomicBoolean isBuilt = new AtomicBoolean(false);
    private final AtomicBoolean markedForRebuild = new AtomicBoolean(false);

    private Node chunkNode;
    private Geometry chunkGeometry;
    private ArrayDensityVolume densityVolume;
    // private VoxelVolume voxelVolume;
    private ArrayDensityVolume voxelVolume;

    private int priority = 1;

    public WorldChunk(World world, GridPosition gridPosition) {
        this.world = world;
        this.gridPosition = gridPosition;

        file = Paths.get(StoragePaths.SAVEGAME_DIR.toString(), world.getWorldName(), toString() + ".chunk").toFile();

        this.chunkNode = new Node(String.format("Chunk-Node [%s]", gridPosition.toString()));
        this.chunkNode.setLocalTranslation(this.gridPosition.toWorldTranslation());
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    @Override
    public GridPosition getGridPosition() {
        return this.gridPosition;
    }

    @Override
    public Node getChunkNode() {
        return this.chunkNode;
    }

    @Override
    public ArrayDensityVolume getDensityVolume() {
        return this.densityVolume;
    }

    @Override
    public ArrayDensityVolume getVoxelVolume() {
        return this.voxelVolume;
    }

    @Override
    public void setVoxel(int x, int y, int z, WorldMaterial worldMaterial, float density) {

        getDensityVolume().setDensity(x, y, z, density);

        if (density > 0.0f) {
            // getVoxelVolume().setVoxelId(x, y, z, worldMaterial);
            getVoxelVolume().setDensity(x, y, z, worldMaterial.getId());
        }

    }

    @Override
    public void applyToScene() {

        this.world.getWorldNode().attachChild(this.chunkNode);
        log.debug(String.format("Added %s to scene", toString()));
    }

    private void load(Vector3f chunkDensityVolumeSize) {

        long timeStart = System.currentTimeMillis();

        if (file.exists() && Main.SAVE_CHUNKS) {
            try (FileInputStream fin = new FileInputStream(file); ObjectInputStream ois = new ObjectInputStream(fin)) {
                this.densityVolume = new ArrayDensityVolume((float[]) ois.readObject(), chunkDensityVolumeSize);
            }
            catch (IOException | ClassNotFoundException ex) {
                log.error("Error loading chunk", ex);
            }
        }
        else {
            this.densityVolume = new ArrayDensityVolume(chunkDensityVolumeSize);

            this.densityVolume = ArrayDensityVolume
                    .extractVolume(world.getChunkLoader().getDensityVolume(),
                            gridPosition.getWorldTranslationX(),
                            gridPosition.getWorldTranslationY(),
                            gridPosition.getWorldTranslationZ(),
                            (int)chunkDensityVolumeSize.x,
                            (int)chunkDensityVolumeSize.y,
                            (int)chunkDensityVolumeSize.z);
        }

        long timeEnd = System.currentTimeMillis();

        log.debug(String.format("Loading %s took %d ms", toString(), (timeEnd - timeStart)));
    }

    @Override
    public Chunk call() throws Exception {

        long timeStart = System.currentTimeMillis();

        if (markedForRebuild.get()) {
            save();
        }

        MeshGenerator meshGenerator = new MarchingCubesMeshGenerator(Chunk.SIZE_XYZ, Chunk.SIZE_XYZ, Chunk.SIZE_XYZ, 1);
        // MeshGenerator meshGenerator = new SurfaceNetsMeshGenerator(Chunk.SIZE_XYZ, Chunk.SIZE_XYZ, Chunk.SIZE_XYZ, 1);
        Vector3f chunkDensityVolumeSize = meshGenerator.getRequiredVolumeSize();

        if (this.voxelVolume == null) {
            // this.voxelVolume = new VoxelVolume(chunkDensityVolumeSize, this.getWorld().getBiomeGenerator(), this.chunkNode.getLocalTranslation());
            this.voxelVolume = new ArrayDensityVolume(chunkDensityVolumeSize);

            for (int x = 0; x < 19; x++) {
                for (int y = 0; y < 19; y++) {
                    for (int z = 0; z < 19; z++) {

                        Vector3f loc = this.chunkNode.getLocalTranslation().add(x, y, z);
                        int voxelId = this.getWorld().getBiomeGenerator().getVoxelId(loc);
                        this.voxelVolume.setDensity(x, y, z, voxelId);
                    }
                }
            }
        }


        // we only need to load the density volume if it's being built for the first time.
        // when the density field is modified (and thus re-built) - we already have the data. we don't need to load it again.
        if (!markedForRebuild.get()) {
            load(chunkDensityVolumeSize);
        }

        Mesh mesh = meshGenerator.buildMesh(this.densityVolume);

        if (mesh != null) {

            Geometry newGeom = new Geometry(String.format("Terrain-Geometry [%s]", gridPosition.toString()), mesh);
            newGeom.setMaterial(world.getAppContext().getMaterialManager().getEarthMaterial());

            // rather than meddle with the mesh generator and add the voxel data there, we'll keep that class
            // clean and just add the data here.

            Vector3f[] vertices = BufferUtils.getVector3Array(mesh.getFloatBuffer(VertexBuffer.Type.Position));
            Vector2f[] voxelData = new Vector2f[vertices.length];

            for (int i = 0; i < vertices.length; i++) {
                // voxelData[i] = new Vector2f(this.voxelVolume.getVoxelId(vertices[i]), 0);
                voxelData[i] = new Vector2f(voxelVolume.getDensity((int)vertices[i].x, (int)vertices[i].y, (int)vertices[i].z), 0);
            }

            FloatBuffer tb = BufferUtils.createFloatBuffer(voxelData);
            mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, tb);

            if (markedForRebuild.get()) {
                world.getAppContext().getApplication().enqueue(() -> {
                    GeometryUtils.destroyGeometry(this.chunkGeometry);
                    this.chunkGeometry = newGeom;
                    this.chunkNode.attachChild(newGeom);
                });
            }
            else {
                this.chunkGeometry = newGeom;
                this.chunkNode.attachChild(newGeom);
            }
        }
        else { // this geometry may now be "null" or empty after modification / update.
            if (markedForRebuild.get()) {
                if (this.chunkGeometry != null) {
                    world.getAppContext().getApplication().enqueue(() -> GeometryUtils.destroyGeometry(this.chunkGeometry));
                }

            }
        }

        this.isBuilt.set(true);

        if (!markedForRebuild.get()) {
            save();
        }

        long timeEnd = System.currentTimeMillis();

        log.info(String.format("%s %s in %d ms", markedForRebuild.get() ? "Rebuilt" : "Built", toString(), (timeEnd - timeStart)));

        return this;
    }

    @Override
    public boolean disposeSafely() {

        if (this.isBuilt.get()) {
            GeometryUtils.destroyGeometry(chunkGeometry);
        }

        log.debug(String.format("Disposed %s", toString()));
        return this.isBuilt.get();
    }

    @Override
    public void rebuild() {
        if (markedForRebuild.get()) {
            return;
        }

        priority = -99;
        markedForRebuild.set(true);
        world.getChunkLoader().getChunkBuilder().buildChunk(this);
    }

    @Override
    public boolean isMarkedForRebuild() {
        return markedForRebuild.get();
    }

    @Override
    public void unmarkForRebuild() {
        markedForRebuild.set(false);
        priority = 1;
    }


    @Override
    public void save() {

        if (!Main.SAVE_CHUNKS) {
            return;
        }

        long timeStart = System.currentTimeMillis();

        try (FileOutputStream fout = new FileOutputStream(file); ObjectOutputStream oos = new ObjectOutputStream(fout)) {
            oos.writeObject(this.densityVolume.getDenistyArray());
        }
        catch (IOException ex) {
            log.error("Error saving chunk", ex);
        }

        long timeEnd = System.currentTimeMillis();

        log.debug(String.format("Saving %s took %d ms", toString(), (timeEnd - timeStart)));
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return String.format("Chunk [%s]", gridPosition.toString());
    }

}
