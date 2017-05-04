package com.jayfella.terrain.interaction.brush.shape;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by James on 01/05/2017.
 */
public class CubeBrushShape implements BrushShape {

    @Override
    public Map<Vector3f, Float> apply(Vector3f position, float density, float smooth, Vector3f size) {

        Vector3f min = position.subtract(size);
        Vector3f max = position.add(size);

        Vector3f boxCenter = max.add(min).divideLocal(2f);
        Vector3f boxExtents = boxCenter.subtract(min);

        float xDiff, yDiff, zDiff;
        float maxDiff;

        Vector3f innerExtents = boxExtents.subtract(smooth, smooth, smooth);

        Map<Vector3f, Float> affectedAreas = new HashMap<>();

        for (int z = (int)min.z; z <= max.z; z++)
        {
            for (int y = (int) min.y; y <= max.y; y++) {
                for (int x = (int) min.x; x <= max.x; x++) {

                    if (y < 1) {
                        continue;
                    }

                    Vector3f v3dCurrentPos = new Vector3f(x, y, z);

                    // Our new density value
                    xDiff = 1f - FastMath.clamp((FastMath.abs(v3dCurrentPos.x - boxCenter.x) - innerExtents.x) / smooth, 0f, 1f);
                    yDiff = 1f - FastMath.clamp((FastMath.abs(v3dCurrentPos.y - boxCenter.y) - innerExtents.y) / smooth, 0f, 1f);
                    zDiff = 1f - FastMath.clamp((FastMath.abs(v3dCurrentPos.z - boxCenter.z) - innerExtents.z) / smooth, 0f, 1f);

                    maxDiff = Math.max(xDiff, Math.max(yDiff, zDiff));

                    float uDensity = density * maxDiff;

                    affectedAreas.put(v3dCurrentPos, uDensity);
                }
            }
        }

        return affectedAreas;
    }

    @Override
    public String toString() {
        return "Cube";
    }

}
