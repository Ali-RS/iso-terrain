package com.jayfella.terrain.biome;

import com.jayfella.terrain.iso.fractal.PerlinNoise;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

import java.util.Random;

/**
 * Created by James on 30/04/2017.
 */
public class BiomeGenerator {

    private PerlinNoise noise0;
    private PerlinNoise noise1;

    public BiomeGenerator(long seed) {

        Random random = new Random(seed);

        noise0 = new PerlinNoise(random.nextInt());
        noise1 = new PerlinNoise(random.nextInt());
    }

    public int getVoxelId(Vector3f location) {
        return getVoxelId(location.x, location.y, location.z);
    }

    // private float waterScalar = 4f;
    // private float tempScalar = 6f;
    private float stretch = 332f;
    private float scale = 332f;

    public int getVoxelId(float x, float y, float z) {

        double n0 = noise0.getNoise(x / stretch, 16f, z / scale); // rainfall
        double n1 = noise1.getNoise(x / scale, 16f, z / stretch); // temperature

        double result = (n0 + n1);// add them together

        // add 2 (to get only positive numbers) and divide by 4 (to get a 0.0 - 1.0 variance)
        double id = (result + 2d) / 4d;

        // multiply by the amount of voxel types ( +2 to avoid "middling out")
        double vox = id * 5d;

        // vox = vox;// - 1d;
        vox = FastMath.clamp((float)vox, 1, 3);
        vox -= 1d;

        int voxelId = (int) vox;

        return voxelId;
        // return 0;
    }


}
