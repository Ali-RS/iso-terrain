package com.jayfella.terrain.core.gameloop;

public class UpdateMonitor {

    private UpdateSpeed speed;
    private float currentDelay;

    public UpdateMonitor(UpdateSpeed speed) {
        this.speed = speed;
    }

    public boolean update(float time) {
        this.currentDelay += time;

        if (this.currentDelay >= speed.getDelay()) {
            this.currentDelay = 0f;
            return true;
        }

        return false;
    }

    public UpdateSpeed getSpeed() {
        return this.speed;
    }

    public void setSpeed(UpdateSpeed speed) {
        this.speed = speed;
    }

}
