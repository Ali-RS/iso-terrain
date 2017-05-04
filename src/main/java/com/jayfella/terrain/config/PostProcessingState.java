package com.jayfella.terrain.config;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.renderer.ViewPort;
import com.jme3.shadow.DirectionalLightShadowFilter;

/**
 * Created by James on 01/05/2017.
 */
public class PostProcessingState extends BaseAppState {

    private final AppConfig appConfig;
    private final DirectionalLight sun;

    private FilterPostProcessor fpp;

    private DirectionalLightShadowFilter directionalLightShadowFilter;
    private SSAOFilter ssao;

    public PostProcessingState(AppConfig appConfig, DirectionalLight sun) {
        this.appConfig = appConfig;
        this.sun = sun;
    }

    @Override
    protected void initialize(Application app) {

        AssetManager assetManager = app.getAssetManager();
        ViewPort viewport = app.getViewPort();

        this.fpp = new FilterPostProcessor(assetManager);
        viewport.addProcessor(fpp);

        directionalLightShadowFilter = new DirectionalLightShadowFilter(assetManager, 4096, 4);
        directionalLightShadowFilter.setShadowIntensity(0.3f);
        directionalLightShadowFilter.setLight(sun);
        directionalLightShadowFilter.setEnabled(true);
        fpp.addFilter(directionalLightShadowFilter);


        ssao = new SSAOFilter();
        ssao.setEnabled(true);
        fpp.addFilter(ssao);
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
