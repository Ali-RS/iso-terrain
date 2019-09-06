package com.jayfella.terrain.core;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.*;
import com.jme3.texture.TextureArray;

import java.util.ArrayList;
import java.util.List;

/**
 * A global material manager for the game
 */
public class MaterialManager {

    private final AssetManager assetManager;

    private final Material earthMaterial;

    public MaterialManager(AssetManager assetManager) {
        this.assetManager = assetManager;

        // Texture texture = assetManager.loadTexture("Textures/Earth/grass_diffuse.jpg");
        // this.earthMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        // this.earthMaterial.setTexture("DiffuseMap", texture);

        // this.earthMaterial = this.createEarthMaterial(assetManager);
        this.earthMaterial = this.createMaterial(assetManager);
        // this.earthMaterial = createBasicMaterial(assetManager);
        // this.earthMaterial = createUnlitMaterial(assetManager);
    }

    public Material getEarthMaterial() {
        return this.earthMaterial;
    }

    private Material createEarthMaterial(AssetManager assetManager) {

        Material material = new Material(assetManager, "MatDefs/TrilinearLighting.j3md");
        material.setFloat("Shininess", 0);
        material.setColor("Diffuse", ColorRGBA.White);
        material.setColor("Ambient", ColorRGBA.White);
        material.setBoolean("UseMaterialColors", true);

        // Setup the trilinear textures for the different axis,
        // X, Y, and Z.  We use the regular diffuse map for the top
        // texture.
        Texture texture;

        texture = assetManager.loadTexture("Textures/Earth/grass.jpg");
        texture.setWrap(WrapMode.Repeat);
        material.setTexture("DiffuseMap", texture);

        texture = assetManager.loadTexture("Textures/Earth/grass-flat.jpg");
        texture.setWrap(Texture.WrapMode.Repeat);
        material.setTexture("DiffuseMapLow", texture);

        texture = assetManager.loadTexture("Textures/Earth/brown-dirt-norm.jpg");
        texture.setWrap(Texture.WrapMode.Repeat);
        material.setTexture("NormalMap", texture);



        texture = assetManager.loadTexture("Textures/Earth/brown-dirt2.jpg");
        texture.setWrap(WrapMode.Repeat);
        material.setTexture("DiffuseMapX", texture);

        //texture = assets.loadTexture("Textures/test-norm.png");
        texture = assetManager.loadTexture("Textures/Earth/brown-dirt-norm.jpg");
        texture.setWrap(WrapMode.Repeat);
        material.setTexture("NormalMapX", texture);

        texture = assetManager.loadTexture("Textures/Earth/brown-dirt2.jpg");
        texture.setWrap(WrapMode.Repeat);
        material.setTexture("DiffuseMapZ", texture);

        //texture = assets.loadTexture("Textures/test-norm.png");
        texture = assetManager.loadTexture("Textures/Earth/brown-dirt-norm.jpg");
        texture.setWrap(WrapMode.Repeat);
        material.setTexture("NormalMapZ", texture);

        // Now the default down texture... we use a separate one
        // and DiffuseMap will be used for the top
        texture = assetManager.loadTexture("Textures/Earth/canvas128.jpg");
        texture.setWrap(WrapMode.Repeat);
        material.setTexture("DiffuseMapY", texture);

        //texture = assets.loadTexture("Textures/test-norm.png");
        texture = assetManager.loadTexture("Textures/Earth/brown-dirt-norm.jpg");
        texture.setWrap(WrapMode.Repeat);
        material.setTexture("NormalMapY", texture);

        // We will need a noise texture soon, might as well set it
        // now
        texture = assetManager.loadTexture("Textures/Earth/noise-x3-512.png");
        texture.setWrap(WrapMode.Repeat);
        material.setTexture("Noise", texture);

        return material;
    }

    private Material createMaterial(AssetManager assetManager) {

        Material material = new Material(assetManager, "MatDefs/TrilinearTerrain.j3md");
        //material.setFloat("Shininess", 0);
        //material.setColor("Diffuse", ColorRGBA.White);
        //material.setColor("Ambient", ColorRGBA.White);
        //material.setBoolean("UseMaterialColors", true);

        // material.setVector3("WorldOffset", world.getWorldOffset());

        List<Image> diffuseTextures = new ArrayList<>();
        diffuseTextures.add(assetManager.loadTexture("Textures/Earth/dirt_diffuse.jpg").getImage());
        diffuseTextures.add(assetManager.loadTexture("Textures/Earth/grass_diffuse.jpg").getImage());
        diffuseTextures.add(assetManager.loadTexture("Textures/Earth/sand_diffuse.jpg").getImage());

        TextureArray diffuseArray = new TextureArray(diffuseTextures);
        diffuseArray.setWrap(WrapMode.Repeat);
        diffuseArray.setMinFilter(MinFilter.Trilinear);
        diffuseArray.setMagFilter(MagFilter.Bilinear);

        material.setTexture("DiffuseArray", diffuseArray);


        List<Image> normalTextures = new ArrayList<>();
        normalTextures.add(assetManager.loadTexture("Textures/Earth/dirt_normal.jpg").getImage());
        normalTextures.add(assetManager.loadTexture("Textures/Earth/grass_normal.jpg").getImage());
        normalTextures.add(assetManager.loadTexture("Textures/Earth/sand_normal.jpg").getImage());

        TextureArray normalArray = new TextureArray(normalTextures);
        normalArray.setWrap(WrapMode.Repeat);
        normalArray.setMinFilter(MinFilter.Trilinear);
        normalArray.setMagFilter(MagFilter.Bilinear);

        material.setTexture("NormalArray", normalArray);

        return material;
    }

    private Material createBasicMaterial(AssetManager assetManager) {
        Texture texture = assetManager.loadTexture("Textures/Earth/grass_diffuse.jpg");
        Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material.setTexture("DiffuseMap", texture);

        return material;
    }

    private Material createUnlitMaterial(AssetManager assetManager) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);

        return mat;
    }

}
