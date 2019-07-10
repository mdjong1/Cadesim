package com.benberi.cadesim.game.entity.vessel.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.benberi.cadesim.GameContext;
import com.benberi.cadesim.game.entity.projectile.CannonBall;
import com.benberi.cadesim.game.entity.projectile.impl.LargeCannonball;
import com.benberi.cadesim.game.entity.vessel.Vessel;
import com.benberi.cadesim.game.entity.vessel.VesselMoveType;
import com.benberi.cadesim.util.PackedObjectOrientation;

public class WarFrigate extends Vessel {

    public WarFrigate(GameContext context, String name, int x, int y) {
        super(context, name, x, y);
    }

    @Override
    public void create() {
        try {
            setDefaultTexture();
            this.shootSmoke = new TextureRegion(getContext().getTextures().getMisc("explode_big"));
            shootSmoke.setRegion(0,0,40, 30);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public int getMaxCannons() {
        return 0;
    }

    @Override
    public float getInfluenceRadius() {
        return 5.5f;
    }

    @Override
    public CannonBall createCannon(GameContext ctx, Vessel source, Vector2 target) {
        return new LargeCannonball(ctx, source, target, getContext().getTextures().getMisc("large_splash"),
                getContext().getTextures().getMisc("hit"));
    }

    @Override
    public VesselMoveType getMoveType() {
        return VesselMoveType.THREE_MOVES;
    }

    @Override
    public void setDefaultTexture() {
        this.setTexture(getVesselTexture("warfrigate"));
        this.setOrientationPack(getContext().getTools().getGson().fromJson(
                Gdx.files.internal("assets/vessel/wf/properties.json").readString(),
                PackedObjectOrientation.class));
    }

    @Override
    public void setSinkingTexture() {
        this.setTexture(getVesselTexture("warfrigate_sinking"));
        this.setOrientationPack(getContext().getTools().getGson().fromJson(
                Gdx.files.internal("assets/vessel/wf/sinking.json").readString(),
                PackedObjectOrientation.class));
    }
}
