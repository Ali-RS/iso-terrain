package com.jayfella.terrain.chunk;

import com.jme3.math.Vector3f;

/**
 * Created by James on 24/04/2017.
 */
public class GridPosition {

    private final int x, y, z;

    public GridPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static GridPosition fromWorldLocation(Vector3f worldLocation) {
        return new GridPosition(
            (int)worldLocation.x >> 4,
            (int)worldLocation.y >> 4,
            (int)worldLocation.z >> 4);
    }

    public int getX() { return this.x; }
    public int getY() { return this.y; }
    public int getZ() { return this.z; }

    /**
     * converts the world grid position to a world co-ordinate.
     * @return
     */
    public Vector3f toWorldTranslation() {
        return new Vector3f(x << 4, y << 4, z << 4);
    }

    public int getWorldTranslationX() {
        return x << 4;
    }

    public int getWorldTranslationY() {
        return y << 4;
    }

    public int getWorldTranslationZ() {
        return z << 4;
    }

    public GridPosition subtract(int x, int y, int z) {
        return new GridPosition(this.x - x, this.y - y, this.z - z);
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof GridPosition)) {
            return false;
        }

        GridPosition other = (GridPosition)obj;

        return (this.x == other.x && this.y == other.y && this.z == other.z);

    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash += 7 * hash + this.x;
        hash += 7 * hash + this.y;
        hash += 7 * hash + this.z;
        return hash;
    }

    @Override
    public String toString() {
        return String.format("%d,%d,%d", x, y, z);
    }

}
