package com.jayfella.terrain.gui.debug;

import com.jme3.app.state.BaseAppState;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Insets3f;

/**
 * Created by James on 25/04/2017.
 */
public abstract class DebugState extends BaseAppState {

    private final DebugHudState parent;
    private final String name;
    private final Container container;

    public DebugState(DebugHudState parent, String name) {
        this.parent = parent;
        this.name = name;
        this.container = new Container();
        this.container.setInsets(new Insets3f(3,3,3,3));
    }

    protected final DebugHudState getParent() {
        return this.parent;
    }

    public final String getName() {
        return this.name;
    }

    public final Container getContainer() {
        return this.container;
    }

}
