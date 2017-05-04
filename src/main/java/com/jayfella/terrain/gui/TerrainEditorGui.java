package com.jayfella.terrain.gui;

import com.jayfella.terrain.core.gameloop.UpdateMonitor;
import com.jayfella.terrain.core.gameloop.UpdateSpeed;
import com.jayfella.terrain.interaction.brush.BrushListener;
import com.jayfella.terrain.interaction.SimpleInteractionState;
import com.jayfella.terrain.interaction.brush.shape.BrushShape;
import com.jayfella.terrain.interaction.brush.shape.CubeBrushShape;
import com.jayfella.terrain.interaction.brush.shape.SphereBrushShape;
import com.jayfella.terrain.material.WorldMaterial;
import com.jayfella.terrain.world.World;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.*;
import com.simsilica.lemur.core.VersionedList;
import com.simsilica.lemur.props.PropertyPanel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A GUI to allow changing density brushes and various properties.
 */
public class TerrainEditorGui extends BaseAppState {

    private final World world;
    private final SimpleInteractionState interactionState;
    private final Container container;

    private final Label currentToolLabel;
    private final String currentToolFormat = "Tool: %s";

    private final Label toolSizeLabel;
    private final String toolSizeFormat = "Size: %s";

    private final Label materialLabel;
    private final String materialFormat = "Material: %s";

    private BrushListener brushListener;

    private List<BrushShape> brushShapes;

    private ListBox brushListBox;
    private int selectedBrushIndex;

    private int selectedMaterialIndex;
    private ListBox materialsListBox;

    private final UpdateMonitor updateMonitor;
    private boolean updateGuiPos = true;

    public TerrainEditorGui(World world, SimpleInteractionState interactionState) {
        this.world = world;
        this.interactionState = interactionState;

        this.container = new Container();
        this.currentToolLabel = container.addChild(new Label(String.format(currentToolFormat, interactionState.getBrush().getBrushShape())));
        this.toolSizeLabel = container.addChild(new Label(String.format(toolSizeFormat, interactionState.getBrush().getSize())));
        this.materialLabel = container.addChild(new Label(String.format(materialFormat, interactionState.getBrush().getWorldMaterial())));

        this.brushListener = new BrushListener() {
            @Override
            public void brushChanged(BrushShape brush) {
                currentToolLabel.setText(String.format(currentToolFormat, brush));
            }

            @Override
            public void SizeChanged(Vector3f size) {
                toolSizeLabel.setText(String.format(toolSizeFormat, size));
            }

            @Override
            public void worldMaterialChanged(WorldMaterial worldMaterial) {
                materialLabel.setText(String.format(materialFormat, worldMaterial));
            }
        };

        interactionState.getBrush().addBrushListener(this.brushListener);

        PropertyPanel propertyPanel = container.addChild(new PropertyPanel("glass"));
        // propertyPanel.addIntProperty("Size", interactionState, "brushSize", 1, 16, 1);
        propertyPanel.addFloatProperty("Size", this, "totalBrushSize", 1.0f, 16.0f, 1.0f);
        propertyPanel.addFloatProperty("Size-X", this, "brushSizeX", 1.0f, 16.0f, 1.0f);
        propertyPanel.addFloatProperty("Size-Y", this, "brushSizeY", 1.0f, 16.0f, 1.0f);
        propertyPanel.addFloatProperty("Size-Z", this, "brushSizeZ", 1.0f, 16.0f, 1.0f);

        propertyPanel.setInsets(new Insets3f(3,3,3,3));


        this.brushShapes = new ArrayList<>();
        this.brushShapes.add(new CubeBrushShape());
        this.brushShapes.add(new SphereBrushShape());

        VersionedList<BrushShape> brushesVersionedList = new VersionedList<>(this.brushShapes);
        this.brushListBox = container.addChild(new ListBox<>(brushesVersionedList));
        this.brushListBox.getSelectionModel().setSelection(0);
        this.brushListBox.setInsets(new Insets3f(3,3,3,3));

        VersionedList<WorldMaterial> materialVersionedList = new VersionedList<>(Arrays.asList(WorldMaterial.values()));
        this.materialsListBox = container.addChild(new ListBox<>(materialVersionedList));
        this.materialsListBox.getSelectionModel().setSelection(0);
        this.materialsListBox.setInsets(new Insets3f(3,3,3,3));

        this.updateMonitor = new UpdateMonitor(UpdateSpeed.FPS_20);
    }

    @Override protected void initialize(Application app) { }
    @Override protected void cleanup(Application app) { }
    @Override protected void onEnable() {
        world.getAppContext().getGuiRootNode().attachChild(this.container);
    }
    @Override protected void onDisable() {
        this.container.removeFromParent();
    }

    private float totalSize = 1;

    public float getTotalBrushSize() {
        return totalSize;
    }

    public void setTotalBrushSize(float size) {
        this.totalSize = size;
        this.interactionState.getBrush().setSize(new Vector3f(size, size, size));
    }

    public float getBrushSizeX() {
        return this.interactionState.getBrush().getSize().x;
    }
    public void setBrushSizeX(float x) {
        // this.interactionState.getBrush().getSize().setX(x);
        this.interactionState.getBrush().setSize(this.interactionState.getBrush().getSize().setX(x));
    }

    public float getBrushSizeY() {
        return this.interactionState.getBrush().getSize().y;
    }
    public void setBrushSizeY(float y) {
        this.interactionState.getBrush().setSize(this.interactionState.getBrush().getSize().setY(y));
    }

    public float getBrushSizeZ() {
        return this.interactionState.getBrush().getSize().z;
    }
    public void setBrushSizeZ(float z) {
        this.interactionState.getBrush().setSize(this.interactionState.getBrush().getSize().setZ(z));
    }



    @Override
    public void update(float tpf) {

        if (!this.updateMonitor.update(tpf)) {
            return;
        }

        if (brushListBox.getSelectionModel().getSelection() != null && brushListBox.getSelectionModel().getSelection() != this.selectedBrushIndex) {
            this.selectedBrushIndex = brushListBox.getSelectionModel().getSelection();
            interactionState.getBrush().setBrushShape(brushShapes.get(this.selectedBrushIndex));

            updateGuiPos = true;
        }

        if (materialsListBox.getSelectionModel().getSelection() != null && materialsListBox.getSelectionModel().getSelection() != this.selectedMaterialIndex) {
            this.selectedMaterialIndex = materialsListBox.getSelectionModel().getSelection();
            interactionState.getBrush().setWorldMaterial(WorldMaterial.values()[this.selectedMaterialIndex]);

            updateGuiPos = true;
        }

        if (updateGuiPos) {

            if (container.getPreferredSize().x < 200) {
                container.setPreferredSize(container.getPreferredSize().setX(200));
            }
            world.getAppContext().getGuiPositionHelper().setLocation(container, GuiPositionHelper.PlaneX.RIGHT, GuiPositionHelper.PlaneY.BOTTOM);
            updateGuiPos = false;
        }

    }

}
