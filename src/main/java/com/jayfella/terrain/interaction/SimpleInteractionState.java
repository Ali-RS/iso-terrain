package com.jayfella.terrain.interaction;

import com.jayfella.terrain.core.ApplicationContext;
import com.jayfella.terrain.input.MappedInput;
import com.jayfella.terrain.interaction.brush.DensityBrush;
import com.jayfella.terrain.interaction.brush.shape.CubeBrushShape;
import com.jayfella.terrain.material.WorldMaterial;
import com.jayfella.terrain.world.World;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.simsilica.lemur.input.*;

/**
 * Created by James on 28/04/2017.
 */
public class SimpleInteractionState extends BaseAppState {

    private final ApplicationContext appContext;
    private final World world;
    private final Camera cam;

    private final InputMapper inputMapper;

    private final Ray ray;
    private final CollisionResults collisionResults;
    private CollisionResult collisionResult;

    private Geometry geometry;
    private DensityBrush brush;

    private final InteractionListener interactionListener;

    public SimpleInteractionState(ApplicationContext appContext, World world) {
        this.appContext = appContext;
        this.world = world;
        this.cam = appContext.getCamera();

        this.inputMapper = appContext.getInputMapper();

        this.ray = new Ray();
        this.ray.setLimit(500f);

        this.collisionResults = new CollisionResults();

        this.brush = new DensityBrush(world);
        this.brush.setSize(new Vector3f(5,5,5));
        this.brush.setBrushShape(new CubeBrushShape());
        this.brush.setWorldMaterial(WorldMaterial.DIRT);

        this.interactionListener = new InteractionListener();
    }


    @Override protected void initialize(Application app) { }

    @Override
    protected void onEnable() {
        interactionListener.addMappings();
    }

    @Override
    protected void onDisable() {
        interactionListener.removeMappings();
    }

    @Override
    protected void cleanup(Application app) { }

    private Vector3f lastLocation;

    public void update(float tpf) {

        ray.setOrigin(cam.getLocation());
        ray.setDirection(cam.getDirection());

        world.getWorldNode().collideWith(ray, collisionResults);

        if (collisionResults.size() > 0) {
            collisionResult = collisionResults.getClosestCollision();
            this.lastLocation = collisionResult.getContactPoint();
            collisionResults.clear();
        }
    }

    public DensityBrush getBrush() {
        return this.brush;
    }

    public void setBrush(DensityBrush brush) {
        this.brush = brush;
    }

    private class InteractionListener implements StateFunctionListener, MappedInput {

        private final String SIMPLE_INTERACTION = "Simple Interaction";
        private final FunctionId F_ADD_DENSITY = new FunctionId(SIMPLE_INTERACTION, "Add Density");
        private final FunctionId F_REMOVE_DENSITY = new FunctionId(SIMPLE_INTERACTION, "Remove Density");

        @Override
        public void addMappings() {
            inputMapper.map(F_ADD_DENSITY, Button.MOUSE_BUTTON1);
            inputMapper.map(F_REMOVE_DENSITY, Button.MOUSE_BUTTON2);

            inputMapper.addStateListener(this,
                    F_ADD_DENSITY,
                    F_REMOVE_DENSITY);

            inputMapper.activateGroup(SIMPLE_INTERACTION);
        }

        @Override
        public void removeMappings() {
            inputMapper.removeMapping(F_ADD_DENSITY, Button.MOUSE_BUTTON1);
            inputMapper.removeMapping(F_REMOVE_DENSITY, Button.MOUSE_BUTTON2);

            inputMapper.removeStateListener(this,
                    F_ADD_DENSITY,
                    F_REMOVE_DENSITY);

            inputMapper.deactivateGroup(SIMPLE_INTERACTION);
        }

        @Override
        public void valueChanged(FunctionId func, InputState value, double tpf) {

            if (value == InputState.Off) {
                return;
            }

            float fracX = lastLocation.x - FastMath.floor(lastLocation.x);
            float fracY = lastLocation.y - FastMath.floor(lastLocation.y);
            float fracZ = lastLocation.z = FastMath.floor(lastLocation.z);

            Vector3f pos = new Vector3f(
                    (fracX < 0.5f) ? FastMath.floor(lastLocation.x) : FastMath.ceil(lastLocation.x),
                    (fracY < 0.5f) ? FastMath.floor(lastLocation.y) : FastMath.ceil(lastLocation.y),
                    (fracZ < 0.5f) ? FastMath.floor(lastLocation.z) : FastMath.ceil(lastLocation.z));

            if (func == F_ADD_DENSITY) {

                if (brush != null) {
                    brush.apply(pos, 1f, 1f);
                }

            }
            else if (func == F_REMOVE_DENSITY) {
                if (brush != null) {
                    brush.apply(pos, -1f, 1f);
                }

            }

        }

    }

}
