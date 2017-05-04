package com.jayfella.terrain.interaction.brush.shape;

import com.jme3.math.Vector3f;

import java.util.Map;

/**
 * Created by James on 01/05/2017.
 */
public interface BrushShape {

    Map<Vector3f, Float> apply(Vector3f position, float density, float smooth, Vector3f size);
}
