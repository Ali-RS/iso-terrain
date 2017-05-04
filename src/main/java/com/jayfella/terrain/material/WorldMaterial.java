package com.jayfella.terrain.material;

/**
 * Created by James on 30/04/2017.
 */
public enum WorldMaterial {

    DIRT(0),
    GRASS(1),
    SAND(2);
    // AIR(999);

    private final int id;

    WorldMaterial(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

}
