package com.jayfella.terrain.core;

import com.jayfella.terrain.config.AppConfig;
import com.jayfella.terrain.gui.GuiPositionHelper;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.InputMapper;

/**
 * Represents the context of the application and methods for application-related actions.
 */
public class ApplicationContext {

    private final SimpleApplication app;
    private final InputMapper inputMapper;
    private final AppConfig appConfig;

    private final MaterialManager materialManager;

    private final GuiPositionHelper guiPositionHelper;

    public ApplicationContext(SimpleApplication app) {

        this.app = app;
        this.inputMapper = GuiGlobals.getInstance().getInputMapper();
        this.appConfig = new AppConfig();

        this.materialManager = new MaterialManager(app.getAssetManager());

        this.guiPositionHelper = new GuiPositionHelper(app.getCamera());
    }

    public SimpleApplication getApplication() {
        return this.app;
    }

    public InputManager getInputManager() {
        return this.app.getInputManager();
    }

    public InputMapper getInputMapper() {
        return this.inputMapper;
    }

    public AssetManager getAssetManager() {
        return this.app.getAssetManager();
    }

    public AppStateManager getAppStateManager() {
        return this.app.getStateManager();
    }

    public Node getSceneRootNode() {
        return this.app.getRootNode();
    }

    public Node getGuiRootNode() {
        return this.app.getGuiNode();
    }

    public AppConfig getAppConfig() {
        return this.appConfig;
    }

    public MaterialManager getMaterialManager() {
        return this.materialManager;
    }

    public Camera getCamera() {
        return this.app.getCamera();
    }

    public GuiPositionHelper getGuiPositionHelper() {
        return this.guiPositionHelper;
    }

}
