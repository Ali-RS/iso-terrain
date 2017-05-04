package com.jayfella.terrain.iso.fractal;

import com.jayfella.terrain.chunk.Chunk;
import com.jayfella.terrain.chunk.GridPosition;
import com.jayfella.terrain.iso.DensityVolume;
import com.jayfella.terrain.iso.volume.ArrayDensityVolume;
import com.jayfella.terrain.world.World;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import java.util.Random;

/**
 * Represents a density volume that can be modified.
 */
public class ModifiableDensityVolume implements DensityVolume {

    private final World world;

    private final PerlinNoise noise;
    private final PerlinNoise noise2;
    private final PerlinNoise noise3;

    private final Quaternion octaveMat0 = new Quaternion();
    private final Quaternion octaveMat1 = new Quaternion().fromAngles(0.1f, 0.01f, -0.1f);
    private final Quaternion octaveMat2 = new Quaternion().fromAngles(0.2f, 0.02f, -0.2f);
    private final Quaternion octaveMat3 = new Quaternion().fromAngles(0.3f, 0.03f, -0.3f);
    private final Quaternion octaveMat4 = new Quaternion().fromAngles(0.4f, 0.04f, -0.4f);
    private final Quaternion octaveMat5 = new Quaternion().fromAngles(0.5f, 0.05f, -0.5f);
    private final Quaternion octaveMat6 = new Quaternion().fromAngles(0.6f, 0.06f, -0.6f);
    private final Quaternion octaveMat7 = new Quaternion().fromAngles(0.7f, 0.07f, -0.7f);

    private final float hard_floor_y = 12f;

    public ModifiableDensityVolume(World world) {

        this.world = world;

        Random random = new Random(world.getSeed());

        noise = new PerlinNoise(random.nextInt());
        noise2 = new PerlinNoise(random.nextInt());
        noise3 = new PerlinNoise(random.nextInt());
    }

    private double getNoise(Vector3f loc, double scale) {
        scale *= 16;
        return noise.getNoise(loc.x * scale, loc.y * scale, loc.z * scale);
    }

    private void warp(Vector3f loc, double frequency, double scale) {
        double x = noise.getNoise(loc.x * frequency, loc.y * frequency, loc.z * frequency);
        double y = noise2.getNoise(loc.x * frequency, loc.y * frequency, loc.z * frequency);
        double z = noise3.getNoise(loc.x * frequency, loc.y * frequency, loc.z * frequency);

        loc.x = (float)(loc.x + x * scale);
        loc.y = (float)(loc.y + y * scale);
        loc.z = (float)(loc.z + z * scale);
    }

    private float density(Vector3f ws) {

        // check the chunk first to see if it has been loaded before.
        Chunk chunk = world.getChunk(GridPosition.fromWorldLocation(ws));

        // if the chunk is null, it's not loaded, so it shouldn't be modified.
        // This could occur if the brush is very large, or some kind of chunk error occured.



        // Even though each chunk has it's own density volume, we are pulling and pushing density values to individual
        // chunks and creating a kind of "world-wide" density volume here. This will come to help us with collision.
        // The aim of this "world-wide" volume is to create and modify individual chunk volumes in a global manner.
        // The application can modify a "world location" and it will modify the required chunk automatically.

        // get the local translation by removing the chunk localTranslation from the worldTranslation.

        ArrayDensityVolume dv = null;
        Vector3f localCoords = null;

        if (chunk != null) {

            localCoords = ws.subtract(chunk.getChunkNode().getLocalTranslation());

            dv = chunk.getDensityVolume();

            if (dv != null) {
                float cDensity = dv.getDensity((int)localCoords.x, (int)localCoords.y, (int)localCoords.z);

                // if the density has a "good" value (we use Float.MIN_VALUE as an identifier)
                if (cDensity != Float.MIN_VALUE) {
                    return cDensity;
                }
            }
        }

        Vector3f ws_orig = ws.clone();

        if (ws.y <= 1f) {
            return 1f;
        }

        double density = -ws.y;

        Vector3f mountainNoise = ws.mult(.002f);
        density += (256d * noise3.getNoise(mountainNoise.x, mountainNoise.y, mountainNoise.z)); 

        // Warp the location
        warp(ws, 0.04, 1);

        Vector3f c0 = octaveMat0.mult(ws);
        Vector3f c1 = octaveMat1.mult(ws);
        Vector3f c2 = octaveMat2.mult(ws);
        Vector3f c3 = octaveMat3.mult(ws);
        Vector3f c4 = octaveMat4.mult(ws);
        Vector3f c5 = octaveMat5.mult(ws);
        Vector3f c6 = octaveMat6.mult(ws);
        Vector3f c7 = octaveMat7.mult(ws);

        density += getNoise(c0, 0.1600*1.021) * 0.32*1.16;
        density += getNoise(c1, 0.0800*0.985) * 0.64*1.12;
        density += getNoise(c2, 0.0400*1.051) * 1.28*1.08;
        density += getNoise(c3, 0.0200*1.020) * 2.56*1.04;
        density += getNoise(c4, 0.0100*0.968) * 5;
        density += getNoise(c5, 0.0050*0.994) * 10;
        density += getNoise(c6, 0.0025*1.045) * 20*0.9;
        density += getNoise(c7, 0.0012*0.972) * 40*0.8;

        // hard sea-bed floor
        density += FastMath.saturate((hard_floor_y - ws.y)) * 16d;

        float result = (float)density;

        if (dv != null) {
            dv.setDensity((int)localCoords.x, (int)localCoords.y, (int)localCoords.z, result);
        }

        return result;
    }

    @Override
    public float getDensity(int x, int y, int z) {
        return density(new Vector3f(x, y, z));
    }

    @Override
    public float getDensity(float x, float y, float z) {
        return density(new Vector3f(x, y, z));
    }

    public void setDensity(Vector3f loc, float density) {

        Chunk chunk = world.getChunk(GridPosition.fromWorldLocation(loc));
        Vector3f localCoords = loc.subtract(chunk.getChunkNode().getLocalTranslation());
        chunk.getDensityVolume().setDensity((int)localCoords.x, (int)localCoords.y, (int)localCoords.z, density);

    }

    @Override
    public Vector3f getFieldDirection(float x, float y, float z, Vector3f target) {

        float d = 1f;

        double nx = getDensity(x + d, y, z)
                - getDensity(x - d, y, z);
        double ny = getDensity(x, y + d, z)
                - getDensity(x, y - d, z);
        double nz = getDensity(x, y, z + d)
                - getDensity(x, y, z - d);

        if( target == null ) {
            target = new Vector3f((float)-nx, (float)-ny, (float)-nz).normalizeLocal();
        } else {
            target.set((float)-nx, (float)-ny, (float)-nz);
            target.normalizeLocal();
        }

        return target;

    }
}
