package com.jayfella.terrain.config;

/**
 * Created by James on 24/04/2017.
 */
public class VideoConfig {

    private int renderDistance;
    private boolean vsync;
    private int anistropicFilteringLevel;

    public VideoConfig() {

        // default settings for now...
        this.renderDistance = 6;
        this.vsync = false;
        this.anistropicFilteringLevel = 16;
    }

    public int getRenderDistance() { return this.renderDistance; }
    public void setRenderDistance(int renderDistance) { this.renderDistance = renderDistance; }

    public boolean isVsync() { return this.vsync; }
    public void setVsync(boolean vsync) { this.vsync = vsync; }

    public int getAnistropicFilteringLevel() { return this.anistropicFilteringLevel; }
    public void setAnistropicFilteringLevel(int anistropicFilteringLevel) { this.anistropicFilteringLevel = anistropicFilteringLevel; }

}
