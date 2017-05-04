package com.jayfella.terrain.util;

import com.jme3.scene.Geometry;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

/**
 * Created by James on 24/04/2017.
 */
public class GeometryUtils {

    public static void destroyGeometry(Geometry geometry) {

        if (geometry != null) {

            if (geometry.getParent() != null) {
                geometry.removeFromParent();
            }

            for( VertexBuffer vb : geometry.getMesh().getBufferList() ) {
                BufferUtils.destroyDirectBuffer( vb.getData() );
            }

            geometry = null;
        }
    }

}
