package com.jayfella.terrain.config;

import com.jme3.asset.AssetEventListener;
import com.jme3.asset.AssetKey;
import com.jme3.asset.TextureKey;

/**
 * Created by James on 23/04/2017.
 */
public class AnistropicFilteringAssetListener implements AssetEventListener {

    private int level = 0;

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public AnistropicFilteringAssetListener() {
        this(0);
    }

    public AnistropicFilteringAssetListener(int level) {
        this.level = level;
    }

    @Override
    public void assetLoaded(AssetKey key) {

    }

    @Override
    public void assetRequested(AssetKey key) {

        if (key.getExtension().equals("png") || key.getExtension().equals("jpg") || key.getExtension().equals("dds")) {
            TextureKey tkey = (TextureKey) key;
            tkey.setAnisotropy(level);
        }
    }

    @Override
    public void assetDependencyNotFound(AssetKey parentKey, AssetKey dependentAssetKey) {

    }

}
