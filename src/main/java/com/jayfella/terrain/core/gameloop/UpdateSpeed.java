package com.jayfella.terrain.core.gameloop;

/**
 * Created by James on 28/04/2017.
 */
public enum  UpdateSpeed {

    FPS_120(1f / 120f),
    FPS_60(1f / 60f),
    FPS_30(1f / 30f),
    FPS_20(1f / 20f),
    FPS_10(1f / 10f),
    FPS_5(1f / 5f);

    private final float delay;

    UpdateSpeed(float delay) {
        this.delay = delay;
    }

    public float getDelay() {
        return this.delay;
    }
}
