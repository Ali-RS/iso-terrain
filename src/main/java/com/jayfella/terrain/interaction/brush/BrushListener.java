package com.jayfella.terrain.interaction.brush;

import com.jayfella.terrain.interaction.brush.shape.BrushShape;
import com.jayfella.terrain.material.WorldMaterial;
import com.jme3.math.Vector3f;

/**
 * Provides methods for listening to brush changes.
 */
public interface BrushListener {

    void brushChanged(BrushShape brush);
    void SizeChanged(Vector3f size);
    void worldMaterialChanged(WorldMaterial worldMaterial);

}
