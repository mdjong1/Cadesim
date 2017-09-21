package com.benberi.cadesim.game.scene.impl.battle.map;

import com.benberi.cadesim.GameContext;
import com.benberi.cadesim.game.scene.impl.battle.map.tile.GameTile;

public class GameObject extends GameTile {

    /**
     * X position
     */
    private int x;

    /**
     * Y position
     */
    private int y;

    public GameObject(GameContext context) {
        super(context);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
