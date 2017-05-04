package com.jayfella.terrain.input.movement;

import com.jayfella.terrain.core.ApplicationContext;
import com.jayfella.terrain.input.MappedInput;
import com.jayfella.terrain.world.World;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.KeyInput;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.input.*;

/**
 * Simple Camera Movement with a cross-hair. Not much more.
 */
public class SimpleCameraMovement extends BaseAppState {

    private final ApplicationContext appContext;
    private final World world;
    private final Label crosshair;

    private CameraMovementListener cameraMovementListener;

    public SimpleCameraMovement(ApplicationContext appContext, World world) {
        this.appContext = appContext;
        this.world = world;

        this.cameraMovementListener = new CameraMovementListener(appContext.getInputMapper(), appContext.getApplication().getCamera());
        this.crosshair = createCrosshair();
    }

    private Label createCrosshair() {
        Label crosshair = new Label("+");
        crosshair.setFontSize(24f);
        crosshair.setColor(ColorRGBA.White);

        Vector3f pref = crosshair.getPreferredSize();
        crosshair.setLocalTranslation((appContext.getCamera().getWidth() / 2) - (pref.x / 2f), (appContext.getCamera().getHeight() / 2) + (pref.y / 2f), 0);

        return crosshair;
    }

    @Override
    protected void initialize(Application app) {

    }

    @Override
    protected void onEnable() {
        this.cameraMovementListener.addMappings();
        this.appContext.getInputManager().setCursorVisible(false);
        this.appContext.getGuiRootNode().attachChild(crosshair);
    }

    @Override
    protected void onDisable() {
        this.cameraMovementListener.removeMappings();
        this.crosshair.removeFromParent();
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    public void update(float tpf) {
        world.getChunkPager().setWorldLocation(appContext.getCamera().getLocation());
    }

    private class CameraMovementListener implements AnalogFunctionListener, StateFunctionListener, MappedInput {

        private final InputMapper inputMapper;
        private final Camera cam;

        private float moveSpeed = 16f;
        private float runSpeed = 64f;
        private float speed = moveSpeed;
        private float rotationSpeed = 1f;

        private Vector3f initialUpVec;
        private boolean invertY = false;

        CameraMovementListener(InputMapper inputMapper, Camera cam) {
            this.inputMapper = inputMapper;
            this.cam = cam;

            this.initialUpVec = cam.getUp().clone();
        }

        private final String SIMPLE_CAM_MOVEMENT = "Simple Camera Movement";
        private final FunctionId F_FORWARD = new FunctionId(SIMPLE_CAM_MOVEMENT, "Forward");
        private final FunctionId F_BACKWARD = new FunctionId(SIMPLE_CAM_MOVEMENT, "Backward");
        private final FunctionId F_STRAFE = new FunctionId(SIMPLE_CAM_MOVEMENT, "Strafe");
        private final FunctionId F_ELEVATE = new FunctionId(SIMPLE_CAM_MOVEMENT, "Elevate");
        private final FunctionId F_ROTATE_HORIZONTAL = new FunctionId(SIMPLE_CAM_MOVEMENT, "RotateH");
        private final FunctionId F_ROTATE_VERTICAL = new FunctionId(SIMPLE_CAM_MOVEMENT, "RotateV");
        private final FunctionId F_SPEED = new FunctionId(SIMPLE_CAM_MOVEMENT, "Speed");

        @Override
        public void addMappings() {
            inputMapper.map(F_FORWARD, InputState.Negative, KeyInput.KEY_W);
            inputMapper.map(F_BACKWARD, KeyInput.KEY_S);

            inputMapper.map(F_STRAFE, InputState.Negative, KeyInput.KEY_A);
            inputMapper.map(F_STRAFE, KeyInput.KEY_D);

            inputMapper.map(F_ELEVATE, InputState.Negative, KeyInput.KEY_Q);
            inputMapper.map(F_ELEVATE, KeyInput.KEY_Z);

            inputMapper.map(F_ROTATE_HORIZONTAL, Axis.MOUSE_X);
            inputMapper.map(F_ROTATE_VERTICAL, Axis.MOUSE_Y);

            inputMapper.map(F_SPEED, KeyInput.KEY_LSHIFT);

            this.inputMapper.addAnalogListener(this,
                    F_FORWARD,
                    F_BACKWARD,
                    F_STRAFE,
                    F_ELEVATE,
                    F_ROTATE_HORIZONTAL,
                    F_ROTATE_VERTICAL
            );

            this.inputMapper.addStateListener(this,
                    F_SPEED);

            this.inputMapper.activateGroup(SIMPLE_CAM_MOVEMENT);
        }

        @Override
        public void removeMappings() {
            inputMapper.removeMapping(F_FORWARD, InputState.Negative, KeyInput.KEY_W);

            inputMapper.removeMapping(F_FORWARD, InputState.Negative, KeyInput.KEY_W);
            inputMapper.removeMapping(F_BACKWARD, KeyInput.KEY_S);

            inputMapper.removeMapping(F_STRAFE, InputState.Negative, KeyInput.KEY_A);
            inputMapper.removeMapping(F_STRAFE, KeyInput.KEY_D);

            inputMapper.removeMapping(F_ELEVATE, InputState.Negative, KeyInput.KEY_Q);
            inputMapper.removeMapping(F_ELEVATE, KeyInput.KEY_Z);

            inputMapper.removeMapping(F_ROTATE_HORIZONTAL, Axis.MOUSE_X);
            inputMapper.removeMapping(F_ROTATE_VERTICAL, Axis.MOUSE_Y);

            inputMapper.removeMapping(F_SPEED, KeyInput.KEY_LSHIFT);

            this.inputMapper.removeAnalogListener(this,
                    F_FORWARD,
                    F_BACKWARD,
                    F_STRAFE,
                    F_ELEVATE,
                    F_ROTATE_HORIZONTAL,
                    F_ROTATE_VERTICAL
            );

            this.inputMapper.removeStateListener(this,
                    F_SPEED);

            this.inputMapper.deactivateGroup(SIMPLE_CAM_MOVEMENT);
        }

        private void moveCamera(float value, boolean sideways) {

            Vector3f vel = new Vector3f();
            Vector3f pos = cam.getLocation().clone();

            if (sideways) {
                cam.getLeft(vel);
            }
            else {
                cam.getDirection(vel);
            }

            vel.multLocal(value * speed);
            pos.addLocal(vel);
            cam.setLocation(pos);
        }

        private void riseCamera(float value) {

            Vector3f vel = new Vector3f(0, value * moveSpeed, 0);
            Vector3f pos = cam.getLocation().clone();
            pos.addLocal(vel);
            cam.setLocation(pos);
        }

        private void rotateCamera(float value, Vector3f axis) {

            Matrix3f mat = new Matrix3f();
            mat.fromAngleNormalAxis(rotationSpeed * value, axis);

            Vector3f up = cam.getUp();
            Vector3f left = cam.getLeft();
            Vector3f dir = cam.getDirection();

            mat.mult(up, up);
            mat.mult(left, left);
            mat.mult(dir, dir);

            Quaternion q = new Quaternion();
            q.fromAxes(left, up, dir);
            q.normalizeLocal();

            cam.setAxes(q);
        }



        @Override
        public void valueActive(FunctionId func, double value, double tpf) {

            float fVal = -(float)(value * tpf);

            if (func == F_STRAFE) {
                moveCamera(fVal, true);
            }
            else if (func == F_FORWARD) {
                moveCamera(fVal, false);
            }
            else if (func == F_BACKWARD) {
                moveCamera(fVal, false);
            }
            else if (func == F_ELEVATE) {
                riseCamera(fVal);
            }
            else if (func == F_ROTATE_HORIZONTAL) {
                rotateCamera(fVal, initialUpVec);
            }
            else if (func == F_ROTATE_VERTICAL) {
                rotateCamera(-fVal * (invertY ? 1 : -1), cam.getLeft());
            }

        }

        @Override
        public void valueChanged(FunctionId func, InputState value, double tpf) {

            boolean isPressed = value == InputState.Positive;

            if (func == F_SPEED) {

                this.speed = isPressed ? this.runSpeed : this.moveSpeed;
            }

        }


    }

}
