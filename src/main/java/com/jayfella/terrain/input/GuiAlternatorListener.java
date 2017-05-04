package com.jayfella.terrain.input;

import com.jayfella.terrain.core.ApplicationContext;
import com.jayfella.terrain.input.movement.SimpleCameraMovement;
import com.jayfella.terrain.interaction.SimpleInteractionState;
import com.jme3.input.KeyInput;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;
import com.simsilica.lemur.input.StateFunctionListener;

/**
 * Allows alternating between game input and menu input.
 */
public class GuiAlternatorListener implements StateFunctionListener, MappedInput {

    private final String GUI_ALTERNATOR = "Gui Alternator";
    private final FunctionId F_ALTERNATE = new FunctionId(GUI_ALTERNATOR, "Alternate");

    private final ApplicationContext appContext;
    private final InputMapper inputMapper;

    private boolean guiEnabled = false;

    public GuiAlternatorListener(ApplicationContext appContext) {
        this.appContext = appContext;
        this.inputMapper = appContext.getInputMapper();
    }

    @Override
    public void addMappings() {
        inputMapper.map(F_ALTERNATE, InputState.Negative, KeyInput.KEY_TAB);

        inputMapper.addStateListener(this,
                F_ALTERNATE);

        inputMapper.activateGroup(GUI_ALTERNATOR);
    }

    @Override
    public void removeMappings() {
        inputMapper.removeMapping(F_ALTERNATE, InputState.Negative, KeyInput.KEY_TAB);

        inputMapper.removeStateListener(this,
                F_ALTERNATE);

        inputMapper.deactivateGroup(GUI_ALTERNATOR);
    }

    @Override
    public void valueChanged(FunctionId func, InputState value, double tpf) {

        if (value == InputState.Off) {
            return;
        }

        if (func == F_ALTERNATE) {
            // @TODO: provide a context for the current camera controller, so we can disable it.

            this.guiEnabled = !guiEnabled;
            appContext.getAppStateManager().getState(SimpleCameraMovement.class).setEnabled(!this.guiEnabled);
            appContext.getAppStateManager().getState(SimpleInteractionState.class).setEnabled(!this.guiEnabled);

            if (this.guiEnabled) {
                appContext.getInputManager().setCursorVisible(true);
            }
        }

    }
}
