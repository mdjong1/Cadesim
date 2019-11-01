package com.benberi.cadesim.game.entity.projectile.impl;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.benberi.cadesim.GameContext;
import com.benberi.cadesim.game.entity.projectile.CannonBall;
import com.benberi.cadesim.game.entity.vessel.Vessel;

public class MediumCannonball extends CannonBall {

    public MediumCannonball(GameContext context, Vessel source, Vector2 target, Texture splash, Texture hit) {
        super(source, target, context.getTextures().getMisc("cannonball_medium"), splash, hit);
    }
}
