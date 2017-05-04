package com.jayfella.terrain;

import com.jayfella.terrain.config.AnistropicFilteringAssetListener;
import com.jayfella.terrain.config.StoragePaths;
import com.jayfella.terrain.core.ApplicationContext;
import com.jayfella.terrain.gui.TerrainEditorGui;
import com.jayfella.terrain.gui.debug.DebugHudState;
import com.jayfella.terrain.input.GuiAlternatorListener;
import com.jayfella.terrain.input.movement.SimpleCameraMovement;
import com.jayfella.terrain.interaction.SimpleInteractionState;
import com.jayfella.terrain.world.AnimaliaWorld;
import com.jayfella.terrain.world.World;
import com.jayfella.terrain.world.WorldType;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.event.MouseAppState;
import com.simsilica.lemur.style.BaseStyles;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Main extends SimpleApplication {

    public static boolean SAVE_CHUNKS = false;

    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) {

        ConsoleAppender console = new ConsoleAppender();
        String PATTERN = "%d{dd MMM yyyy HH:mm:ss} [ %p | %c{1} ] %m%n";
        console.setLayout(new PatternLayout(PATTERN));
        console.setThreshold(Level.INFO);
        console.activateOptions();
        org.apache.log4j.Logger.getRootLogger().addAppender(console);

        Main main = new Main();

        AppSettings appSettings = new AppSettings(true);
        appSettings.setTitle("Animalia - jMonkeyEngine");
        appSettings.setSettingsDialogImage("/Interface/splash.png");
        appSettings.setResolution(1280, 720);
        // appSettings.setResolution(1680, 1050);
        // appSettings.setFullscreen(true);
        // appSettings.setUseJoysticks(true);
        appSettings.setVSync(true);

        try {
            BufferedImage[] icons = new BufferedImage[] {
                    ImageIO.read( Main.class.getResource( "/Interface/icons/animalia-128.png" ) ),
                    ImageIO.read( Main.class.getResource( "/Interface/icons/animalia-64.png" ) ),
                    ImageIO.read( Main.class.getResource( "/Interface/icons/animalia-32.png" ) ),
                    ImageIO.read( Main.class.getResource( "/Interface/icons/animalia-16.png" ) )
            };
            appSettings.setIcons(icons);
        }
        catch( IOException e ) {
            log.warn("Error loading icons", e);
        }

        main.setSettings(appSettings);
        // main.setPauseOnLostFocus(false);
        main.setShowSettings(false);
        main.setDisplayStatView(true);
        main.setDisplayFps(true);
        main.start();

    }

    private Main() {
        super(new AppState[0]);

        StoragePaths.create();
    }

    private void initLemur() {
        // initialize lemur
        GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        // only add the gui viewport for collision data to save on calculations.
        stateManager.getState(MouseAppState.class).setIncludeDefaultCollisionRoots(false);
        stateManager.getState(MouseAppState.class).addCollisionRoot(guiViewPort);
    }

    private World earth;

    @Override
    public void simpleInitApp() {

        inputManager.setCursorVisible(false);
        // flyCam.setDragToRotate(true);
        // flyCam.setMoveSpeed(100);

        initLemur();

        // the context for application-specific data
        ApplicationContext appContext = new ApplicationContext(this);

        // light for now...
        AmbientLight ambientLight = new AmbientLight(ColorRGBA.White);
        rootNode.addLight(ambientLight);

        DirectionalLight sun = new DirectionalLight(new Vector3f(-1, -1, -1).normalizeLocal(), ColorRGBA.White);
        rootNode.addLight(sun);

        // Anistropic Filtering
        AnistropicFilteringAssetListener anistropicFilteringAssetListener = new AnistropicFilteringAssetListener(appContext.getAppConfig().getVideoConfig().getAnistropicFilteringLevel());
        assetManager.addAssetEventListener(anistropicFilteringAssetListener);

        earth = new AnimaliaWorld(appContext, WorldType.EARTH, 1337, "My World");
        rootNode.attachChild(earth.getWorldNode());

        // PostProcessingState postProcessingState = new PostProcessingState(appContext.getAppConfig(), sun);
        // stateManager.attach(postProcessingState);

        // debug
        DebugHudState debugHudState = new DebugHudState(earth);
        stateManager.attach(debugHudState);

        // simple camera movement
        SimpleCameraMovement cameraMovement = new SimpleCameraMovement(appContext, earth);
        stateManager.attach(cameraMovement);

        // for tabbing in and out of the game / GUI
        GuiAlternatorListener guiAlternatorListener = new GuiAlternatorListener(appContext);
        guiAlternatorListener.addMappings();

        SimpleInteractionState interactionState = new SimpleInteractionState(appContext, earth);
        stateManager.attach(interactionState);

        TerrainEditorGui terrainEditorGui = new TerrainEditorGui(earth, interactionState);
        stateManager.attach(terrainEditorGui);

    }


}
