package com.jayfella.terrain.voxel;

import com.jayfella.terrain.biome.BiomeGenerator;
import com.jayfella.terrain.material.WorldMaterial;
import com.jme3.math.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by James on 30/04/2017.
 */
public class VoxelVolume {

    private final Logger log = LoggerFactory.getLogger(VoxelVolume.class);

    private final int[][][] voxeldata;
    // private final BiomeGenerator biomeGenerator;

    public VoxelVolume(Vector3f size, BiomeGenerator biomeGenerator, Vector3f offset) {
        this.voxeldata = new int[(int)size.x][(int)size.y][(int)size.z];
        // this.biomeGenerator = biomeGenerator;

        for (int x = 0; x < voxeldata.length; x++) {
            for (int y = 0; y < voxeldata[x].length; y++) {
                for (int z = 0; z < voxeldata[y].length; z++) {
                    voxeldata[x][y][z] = biomeGenerator.getVoxelId(new Vector3f(x, y, z).addLocal(offset));
                }
            }
        }
    }

    public int getVoxelId(Vector3f location) {
        return getVoxelId((int)location.x, (int)location.y, (int)location.z);
    }

    public int getVoxelId(int x, int y, int z) {
        return voxeldata[x][y][z];
    }

    public void setVoxelId(Vector3f location, WorldMaterial worldMaterial) {
        setVoxelId(
                (int)location.x,
                (int)location.y,
                (int)location.z,
                worldMaterial);
    }

    public void setVoxelId(int x, int y, int z, WorldMaterial worldMaterial) {
        this.voxeldata[x][y][z] = worldMaterial.getId();
        // log.info(String.format("Set Voxel [%d,%d,%d] to %s", x, y, z, worldMaterial));
    }
}
