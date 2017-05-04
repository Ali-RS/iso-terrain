package com.jayfella.terrain.gui.debug;

import com.jayfella.terrain.core.gameloop.UpdateMonitor;
import com.jayfella.terrain.core.gameloop.UpdateSpeed;
import com.jayfella.terrain.world.World;
import com.jme3.app.Application;
import com.simsilica.lemur.Label;

/**
 * Displays information related to the world the player resides.
 *
 */
public class WorldDebugState extends DebugState {

    private World world;

    private final Label worldNameLabel;
    private final String worldNameFormat = "World: [%s] %s";

    private final Label worldSeedLabel;
    private final String worldSeedFormat = "Seed: %s";

    private final Label chunksLoadedLabel;
    private final String chunksLoadedFormat = "Chunks In Scene: %d / %d";

    private final Label chunksLoadingLabel;
    private final String chunksLoadingFormat = "Chunks Loading: %d";

    private final Label chunksDisposingLabel;
    private final String chunksDisposingFormat = "Chunks Disposing: %d";

    private final UpdateMonitor updateMonitor;

    public WorldDebugState(DebugHudState parent, World world) {
        super(parent, "World");
        this.world = world;

        this.worldNameLabel = getContainer().addChild(new Label(String.format(worldNameFormat, world.getWorldType(), world.getWorldName())));
        this.worldSeedLabel = getContainer().addChild(new Label(String.format(worldSeedFormat, world.getSeed())));
        this.chunksLoadedLabel = getContainer().addChild(new Label(String.format(chunksLoadedFormat, 0, 0)));
        this.chunksLoadingLabel = getContainer().addChild(new Label(String.format(chunksLoadingFormat, 0)));
        this.chunksDisposingLabel = getContainer().addChild(new Label(String.format(chunksDisposingFormat, 0)));

        this.updateMonitor = new UpdateMonitor(UpdateSpeed.FPS_20);
    }

    @Override
    protected void initialize(Application app) {

    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    public void update(float tpf) {

        if (!this.updateMonitor.update(tpf)) {
            return;
        }

        this.chunksLoadedLabel.setText(String.format(chunksLoadedFormat, world.getChunkLoader().getChunkBuilder().getLoadedChunkCount(), world.getChunkPager().getChunksRequired()));
        this.chunksLoadingLabel.setText(String.format(chunksLoadingFormat, world.getChunkLoader().getChunkBuilder().getAwaitingBuildCount()));
        this.chunksDisposingLabel.setText(String.format(chunksDisposingFormat, world.getChunkLoader().getChunkBuilder().getAwaitingDisposalCount()));
    }
}
