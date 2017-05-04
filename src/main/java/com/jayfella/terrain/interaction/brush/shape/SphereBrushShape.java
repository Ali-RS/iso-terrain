package com.jayfella.terrain.interaction.brush.shape;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by James on 01/05/2017.
 */
public class SphereBrushShape implements BrushShape {


    @Override
    public Map<Vector3f, Float> apply(Vector3f position, float density, float smooth, Vector3f size) {

        float brushSize = FastMath.sqr(size.x);

        int tx = (int) position.x;
        int ty = (int) position.y;
        int tz = (int) position.z;

        int minx = (int) FastMath.floor(position.x - size.x);
        int maxx = (int) FastMath.floor(position.x + size.x);

        int miny = (int) Math.max(FastMath.floor(position.y) - size.y, 0f);
        int maxy = (int) Math.min(FastMath.floor(position.y + size.y) + 1, 255);

        int minz = (int) FastMath.floor(position.z - size.z);
        int maxz = (int) FastMath.floor(position.z + size.z) + 1;

        Map<Vector3f, Float> affectedAreas = new HashMap<>();

        for (int x = maxx; x >= minx; x--) {
            float xs = FastMath.sqr(tx - x);

            for (int y = maxy; y >= miny; y--) {
                float ys = FastMath.sqr(ty - y);

                for (int z = maxz; z >= minz; z--) {
                    float zs = FastMath.sqr(tz - z);

                    float val = xs + ys + zs;

                    if (xs + ys + zs < brushSize) {
                        float uDensity = (brushSize / val) * density;

                        affectedAreas.put(new Vector3f(x, y, z), uDensity);
                    }
                }
            }
        }

        return affectedAreas;
    }

    @Override
    public String toString() {
        return "Sphere";
    }

}
