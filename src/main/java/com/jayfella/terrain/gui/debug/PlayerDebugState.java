package com.jayfella.terrain.gui.debug;

import com.jayfella.terrain.core.gameloop.UpdateMonitor;
import com.jayfella.terrain.core.gameloop.UpdateSpeed;
import com.jayfella.terrain.world.World;
import com.jme3.app.Application;
import com.simsilica.lemur.Label;

/**
 * Displays information about the player.
 */
public class PlayerDebugState extends DebugState {

    private final World world;

    private final Label locationLabel;
    private final String locationFormat = "Location: %.2f, %.2f, %.2f";

    private final Label chunkLabel;
    private final String chunkFormat = "Chunk: %d, %d, %d";

    private final UpdateMonitor updateMonitor;

    public PlayerDebugState(DebugHudState parent, World world) {
        super(parent, "Player");
        this.world = world;

        this.locationLabel = getContainer().addChild(new Label(String.format(locationFormat, 0f, 0f, 0f)));
        this.chunkLabel = getContainer().addChild(new Label(String.format(chunkFormat, 0, 0, 0)));

        this.updateMonitor = new UpdateMonitor(UpdateSpeed.FPS_30);
    }

    @Override protected void initialize(Application app) { }
    @Override protected void cleanup(Application app) { }
    @Override protected void onEnable() { }
    @Override protected void onDisable() { }

    @Override
    public void update(float tpf) {

        if (!this.updateMonitor.update(tpf)) {
            return;
        }

        locationLabel.setText(String.format(locationFormat,
                world.getAppContext().getApplication().getCamera().getLocation().x,
                world.getAppContext().getApplication().getCamera().getLocation().y,
                world.getAppContext().getApplication().getCamera().getLocation().z));

        chunkLabel.setText(String.format(chunkFormat,
                (int)world.getAppContext().getCamera().getLocation().x >> 4,
                (int)world.getAppContext().getCamera().getLocation().y >> 4,
                (int)world.getAppContext().getCamera().getLocation().z >> 4));
    }

}
