package com.jayfella.terrain.gui;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.simsilica.lemur.Panel;

/**
 * Utility methods for placing GUI elements on the GUI node.
 */
public class GuiPositionHelper {

    public enum PlaneX { LEFT, RIGHT, CENTER }
    public enum PlaneY { TOP, BOTTOM, CENTER }

    private final Camera camera;
    private final float BORDER = 10f;

    public GuiPositionHelper(Camera camera) {
        this.camera = camera;
    }

    public Vector3f get(Panel panel, PlaneX planeX, PlaneY planeY) {

        Vector3f prefSize = panel.getPreferredSize();

        float x = 0f;
        float y = 0f;
        float z = prefSize.z;

        switch (planeX) {
            case LEFT: {
                x = BORDER;
                break;
            }
            case RIGHT: {
                x = camera.getWidth() - prefSize.x - BORDER;
                break;
            }
            case CENTER: {
                x = (camera.getWidth() / 2f) - (prefSize.x / 2f);
                break;
            }
        }

        switch (planeY) {
            case TOP: {
                y = camera.getHeight() - BORDER;
                break;
            }
            case BOTTOM: {
                y = prefSize.y + BORDER;
                break;
            }
            case CENTER: {
                y = (camera.getHeight() / 2f) + (prefSize.y / 2f);
                break;
            }
        }

        return new Vector3f(x, y, z);
    }

    public void setLocation(Panel panel, PlaneX planeX, PlaneY planeY) {
        panel.setLocalTranslation(get(panel, planeX, planeY));
    }

}