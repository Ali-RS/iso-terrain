package com.jayfella.terrain.gui.debug;

import com.jayfella.terrain.gui.GuiPositionHelper;
import com.jayfella.terrain.world.World;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.simsilica.lemur.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Displays debug information for various objects within the game.
 */
public class DebugHudState extends BaseAppState {

    private final World world;
    private final RollupPanel rollupPanel;

    private final List<DebugState> stateList = new ArrayList<>();

    public DebugHudState(World world) {

        this.world = world;
        this.rollupPanel = new RollupPanel("Debug", "glass");

        TabbedPanel tabbedPanel = new TabbedPanel();

        stateList.add(new WorldDebugState(this, world));
        stateList.add(new PlayerDebugState(this, world));

        stateList.forEach(state -> tabbedPanel.addTab(state.getName(), state.getContainer()));

        rollupPanel.setContents(tabbedPanel);
        world.getAppContext().getGuiPositionHelper().setLocation(rollupPanel, GuiPositionHelper.PlaneX.LEFT, GuiPositionHelper.PlaneY.TOP);
    }

    @Override protected void initialize(Application app) { }

    @Override protected void onEnable() {
        world.getAppContext().getGuiRootNode().attachChild(this.rollupPanel);
        stateList.forEach(state -> getStateManager().attach(state));
    }

    @Override protected void onDisable() {
        this.rollupPanel.removeFromParent();
        stateList.forEach(state -> getStateManager().detach(state));
    }

    @Override protected void cleanup(Application app) {

    }

}
