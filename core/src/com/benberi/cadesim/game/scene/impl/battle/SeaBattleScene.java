package com.benberi.cadesim.game.scene.impl.battle;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.benberi.cadesim.GameContext;
import com.benberi.cadesim.game.entity.projectile.CannonBall;
import com.benberi.cadesim.game.entity.vessel.*;
import com.benberi.cadesim.game.entity.vessel.move.MoveAnimationTurn;
import com.benberi.cadesim.game.entity.vessel.move.MovePhase;
import com.benberi.cadesim.game.entity.vessel.move.MoveType;
import com.benberi.cadesim.game.scene.GameScene;
import com.benberi.cadesim.game.scene.impl.battle.map.BlockadeMap;
import com.benberi.cadesim.game.scene.impl.battle.map.GameObject;
import com.benberi.cadesim.game.scene.impl.battle.map.tile.GameTile;
import com.benberi.cadesim.game.scene.impl.battle.map.tile.impl.Cell;
import com.benberi.cadesim.game.scene.impl.battle.map.tile.impl.Whirlpool;
import com.benberi.cadesim.game.scene.impl.battle.map.tile.impl.Wind;

import java.util.Iterator;

public class SeaBattleScene implements GameScene {

    /**
     * The main game context
     */
    private GameContext context;

    /**
     * The sprite batch renderer
     */
    private SpriteBatch batch;

    /**
     * The battle map
     */
    private SeaMap map;

    /**
     * The camera view of the scene
     */
    private OrthographicCamera camera;
    
    /**
     * Whether the camera follows the vessel
     * Initially true on respawn
     * 
     * As per PP, drag moves the camera anywhere
     *     if you right clicked, camera stays when you release
     *     if you left  clicked, camera locks back onto ship
     */
    private boolean cameraFollowsVessel = true;

    /**
     * The sea texture
     */
    private Texture sea;

    /**
     * The shape renderer
     */
    private ShapeRenderer renderer;

    /**
     * If the user can drag the map
     */
    private boolean canDragMap;

    /**
     * The game information panel
     */
    private GameInformation information;

    /**
     * The sea battle font for ship names
     */
    private BitmapFont font;

    /**
     * The current execution slot move
     */
    private int currentSlot = -1;

    /**
     * The current executing phase
     */
    private MovePhase currentPhase;

    private BlockadeMap blockadeMap;

    private int vesselsCountWithCurrentPhase = 0;
    private int vesselsCountNonSinking = 0;
    private boolean turnFinished;
    private Vector3 mousePosititon;

    public SeaBattleScene(GameContext context) {
        this.context = context;
        information = new GameInformation(context, this);
    }

    public void createMap(int[][] tiles) {
        this.blockadeMap = new BlockadeMap(context, tiles);
    }

    private void recountVessels() {
        vesselsCountWithCurrentPhase = context.getEntities().countVsselsByPhase(currentPhase);
        vesselsCountNonSinking = context.getEntities().countNonSinking();
    }

    @Override
    public void create() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("assets/font/Pixel-Miners.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 8;
        parameter.spaceX = 0;
        parameter.shadowColor = new Color(0, 0, 0, 0.5f);
        parameter.borderColor = Color.BLACK;
        parameter.borderWidth = 1;
        parameter.borderStraight = true;
        parameter.shadowOffsetY = 1;
        parameter.shadowOffsetX = 1;
        font = generator.generateFont(parameter);

        renderer = new ShapeRenderer();
        this.batch = new SpriteBatch();
        information.create();
        sea = new Texture("assets/sea/sea1.png");
        sea.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() - 200);
    }

    @Override
    public void update()
    {
        //System.out.println(Gdx.graphics.getDeltaTime());
    	
        // update the camera
        camera.update();

        if (currentSlot > -1) {
           // if (context.getEntities().countVsselsByPhase(currentPhase) == context.getEntities().countNonSinking()) {
            if (vesselsCountWithCurrentPhase == vesselsCountNonSinking) {
                MovePhase phase = MovePhase.getNext(currentPhase);
                if (phase == null) {
                    for (Vessel vessel : context.getEntities().listVesselEntities()) {
                        MoveAnimationTurn turn = vessel.getStructure().getTurn(currentSlot);
                        if (turn.isSunk()) {
                            vessel.setSinking(true);
                        }
                    }
                    currentPhase = MovePhase.MOVE_TOKEN;
                    currentSlot++;

                    for (Vessel v : context.getEntities().listVesselEntities()) {
                        v.setMovePhase(null);
                    }

                    if (currentSlot > 3) {
                        currentSlot = -1;
                        turnFinished = true;
                    }

                    recountVessels();
                }
                else {
                    currentPhase = phase;
                    recountVessels();
                }
            }
        }

        for (Vessel vessel : context.getEntities().listVesselEntities()) {
            if (vessel.isSinking()) {
                if (!vessel.isSinkingTexture()) {
                    vessel.tickNonSinkingTexture();
                }
                else {
                    vessel.tickSinkingTexture();
                }
                continue;
            }
            if (vessel.getMoveDelay() != -1) {
                vessel.tickMoveDelay();
            }

            if (!vessel.isMoving()) {
                if (currentSlot != -1) {
                    MoveAnimationTurn turn = vessel.getStructure().getTurn(currentSlot);
                    if (currentPhase == MovePhase.MOVE_TOKEN && vessel.getMovePhase() == null) {
                        if (turn.getAnimation() != VesselMovementAnimation.NO_ANIMATION && vessel.getMoveDelay() == -1) {
                            if (!VesselMovementAnimation.isBump(turn.getAnimation())) {
                                vessel.performMove(turn.getAnimation());

                            }
                            else {
                                vessel.performBump(turn.getTokenUsed(), turn.getAnimation());
                            }

                            turn.setAnimation(VesselMovementAnimation.NO_ANIMATION);
                        }
                        else {
                            vessel.setMovePhase(MovePhase.getNext(vessel.getMovePhase()));
                            recountVessels();

                        }
                    }
                    else if (currentPhase == MovePhase.ACTION_MOVE && vessel.getMovePhase() == MovePhase.MOVE_TOKEN && vessel.getMoveDelay() == -1 && !context.getEntities().hasDelayedVessels()) {
                        if (turn.getSubAnimation() != VesselMovementAnimation.NO_ANIMATION) {
                            if (!VesselMovementAnimation.isBump(turn.getSubAnimation())) {
                                vessel.performMove(turn.getSubAnimation());

                            }
                            else {
                                vessel.performBump(MoveType.NONE, turn.getSubAnimation());
                            }
                            turn.setSubAnimation(VesselMovementAnimation.NO_ANIMATION);
                        }
                        else {
                            vessel.setMovePhase(MovePhase.getNext(vessel.getMovePhase()));
                            recountVessels();
                        }
                    }
                    else if (currentPhase == MovePhase.SHOOT && vessel.getMovePhase() == MovePhase.ACTION_MOVE  && vessel.getMoveDelay() == -1 && vessel.getCannonballs().size() == 0 && !context.getEntities().hasDelayedVessels()) {
                        if (turn.getLeftShoots() == 0 && turn.getRightShoots() == 0) {
                            vessel.setMovePhase(MovePhase.getNext(vessel.getMovePhase()));
                            recountVessels();
                        }
                        else {
                            if (turn.getLeftShoots() > 0) {
                                vessel.performLeftShoot(turn.getLeftShoots());
                                turn.setLeftShoots(0);
                            }
                            if (turn.getRightShoots() > 0) {
                                vessel.performRightShoot(turn.getRightShoots());
                                turn.setRightShoots(0);
                            }
                        }
                    }
                }
            }
            else {
                if (vessel.isBumping()) {
                    VesselBumpVector vector = vessel.getBumpVector();
                    if (!vessel.isBumpReached()) {
                        float speed = vessel.getCurrentPerformingMove() == VesselMovementAnimation.BUMP_PHASE_1 ? 1f : 1.75f;

                        vessel.setX(vessel.getX() + (vector.getDirectionX() * speed * Gdx.graphics.getDeltaTime()));
                        vessel.setY(vessel.getY() + (vector.getDirectionY() * speed * Gdx.graphics.getDeltaTime()));

                        float distance = vector.getStart().dst(new Vector2(vessel.getX(), vessel.getY()));
                        if(distance >= vector.getDistance())
                        {
                            vessel.setPosition(vector.getEnd().x, vector.getEnd().y);
                            vessel.setBumpReached(true);
                            if (vessel.getCurrentPerformingMove() == VesselMovementAnimation.BUMP_PHASE_1) {
                                vessel.tickBumpRotation(2);
                            }
                            else {
                                vessel.tickBumpRotation(1);
                            }

                        }
                        else if (vessel.getCurrentPerformingMove() == VesselMovementAnimation.BUMP_PHASE_2 && distance >= vector.getDistance() / 2 && !vector.isPlayedMiddleAnimation()) {
                            vessel.tickBumpRotation(1);
                            vector.setPlayedMiddleAnimation(true);
                        }
                    }
                    else {
                        if (vessel.getCurrentPerformingMove() == VesselMovementAnimation.BUMP_PHASE_1 || vessel.getCurrentPerformingMove().getId() >= 12) {
                            vessel.setX(vessel.getX() + (vector.getDirectionX() * 2f * Gdx.graphics.getDeltaTime()));
                            vessel.setY(vessel.getY() + (vector.getDirectionY() * 2f * Gdx.graphics.getDeltaTime()));
                            if (vector.getStart().dst(new Vector2(vessel.getX(), vessel.getY())) >= vector.getDistance()) {
                                vessel.setPosition(vector.getEnd().x, vector.getEnd().y);
                                vessel.tickBumpRotation(1);
                                vessel.disposeBump();
                            }
                        }
                        else {
                            vessel.tickBumpRotation(1);
                            vessel.disposeBump();
                        }
                    }
                }
                else {
                    VesselMovementAnimation move = vessel.getCurrentPerformingMove();

                    Vector2 start = vessel.getAnimation().getStartPoint();
                    Vector2 inbetween = vessel.getAnimation().getInbetweenPoint();
                    Vector2 end = vessel.getAnimation().getEndPoint();
                    Vector2 current = vessel.getAnimation().getCurrentAnimationLocation();

                    // calculate step based on progress towards target (0 -> 1)
                    // float step = 1 - (ship.getEndPoint().dst(ship.getLinearVector()) / ship.getDistanceToEndPoint());


                   // float velocityTurns = (0.011f * Gdx.graphics.getDeltaTime()) * 100; //Gdx.graphics.getDeltaTime();
                    float velocityTurns = (1.25f * Gdx.graphics.getDeltaTime()); //Gdx.graphics.getDeltaTime();
                    float velocityForward = (1.8f * Gdx.graphics.getDeltaTime());

                    if (!move.isOneDimensionMove()) {
                        vessel.getAnimation().addStep(velocityTurns);
                        // step on curve (0 -> 1), first bezier point, second bezier point, third bezier point, temporary vector for calculations
                        Bezier.quadratic(current, (float) vessel.getAnimation().getCurrentStep(), start.cpy(),
                                inbetween.cpy(), end.cpy(), new Vector2());
                    }
                    else {
                        // When ship moving forward, we may not want to use the curve
                        int add = move.getIncrementXForRotation(vessel.getRotationIndex());
                        if (add == -1 || add == 1) {
                            current.x += (velocityForward * add);
                            //current.x += (velocityForward * (float) add);
                        }
                        else {
                            add = move.getIncrementYForRotation(vessel.getRotationIndex());
                            // current.y += (velocityForward * (float) add);
                            current.y += (velocityForward * add);
                        }
                        /// vessel.getAnimation().addStep(velocityForward);
                        vessel.getAnimation().addStep(velocityForward);
                    }

                    int result = (int) (vessel.getAnimation().getCurrentStep() * 100);
                    vessel.getAnimation().tickAnimationTicks(velocityTurns * 100);
                    //System.out.println(result);

                    // check if the step is reached to the end, and dispose the movement
                    if (result >= 100) {
                        vessel.setX(end.x);
                        vessel.setY(end.y);
                        vessel.setMoving(false);

                        if (!move.isOneDimensionMove())
                            vessel.setRotationIndex(vessel.getRotationTargetIndex());

                        vessel.setMovePhase(MovePhase.getNext(vessel.getMovePhase()));
                        recountVessels();

                        vessel.setMoveDelay();
                    }
                    else {
                        // process move
                        vessel.setX(current.x);
                        vessel.setY(current.y);
                    }

                    if (result >= 25 && result <= 50 && vessel.getAnimation().getTickIndex() == 0 ||
                            result >= 50 && result <= 75 && vessel.getAnimation().getTickIndex() == 1 ||
                            result >= 75 && result <= 100 && vessel.getAnimation().getTickIndex() == 2 ||
                            result >= 99 && vessel.getAnimation().getTickIndex() == 3 ) {
                        vessel.tickRotation();
                        vessel.getAnimation().setTickIndex(vessel.getAnimation().getTickIndex() + 1);
                    }

                }
            }
            
            // let camera move with vessel if it's supposed to
            if (cameraFollowsVessel) {
    			Vessel myVessel = context.getEntities().getVesselByName(context.myVessel);
    			camera.translate(
    					getIsometricX(myVessel.getX(), myVessel.getY(), myVessel) - camera.position.x,
    					getIsometricY(myVessel.getX(), myVessel.getY(), myVessel) - camera.position.y
    			);
        	}

            if (vessel.isSmoking()) {
                vessel.tickSmoke();
            }
            Iterator<CannonBall> itr = vessel.getCannonballs().iterator();
            while (itr.hasNext()) {
                CannonBall c = itr.next();
                if (c.isReleased()) {
                    if (c.hasSubCannon()) {
                        if (c.canReleaseSubCannon()) {
                            c.getSubcannon().setReleased(true);
                        }
                    }
                    if (!c.reached()) {
                        c.move();
                    } else {
                        if (c.finnishedEndingAnimation()) {
                            itr.remove();
                        }
                        else {
                            c.tickEndingAnimation();
                        }
                    }
                }
            }
        }

        if (turnFinished) {
            boolean found = false;
            for (Vessel v : context.getEntities().listVesselEntities()) {
                if (!v.isSinkingAnimationFinished()) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                context.notifyFinishTurn();
                turnFinished = false;
            }
        }
        information.update();
    }

    private boolean drawn;

    @Override
    public void render() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        Gdx.gl.glViewport(0,200, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() - 200);

        drawSea();

        // Render the map
        renderSeaBattle();

        // Render ships
        renderEntities();

        batch.end();

        information.render();
    }

    public GameInformation getInformation() {
        return information;
    }

    /**
     * Draws the sea background
     */
    private void drawSea() {
        batch.draw(sea, -2000, -1000, 0, 0, 5000, 5000);
    }

    /**
     * Renders all entities
     */
    private void renderEntities() {
        renderer.setProjectionMatrix(camera.combined);

        for (int x = BlockadeMap.MAP_WIDTH - 1; x > -1; x--) {
            for (int y = BlockadeMap.MAP_HEIGHT - 1; y > -1; y--) {
                GameObject object = blockadeMap.getObject(x, y);
                if (object != null) {
                    TextureRegion region = object.getRegion();

                    int xx = (object.getX() * GameTile.TILE_WIDTH / 2) - (object.getY() * GameTile.TILE_WIDTH / 2) - region.getRegionWidth() / 2;
                    int yy = (object.getX() * GameTile.TILE_HEIGHT / 2) + (object.getY() * GameTile.TILE_HEIGHT / 2) - region.getRegionHeight() / 2;

                    if (!object.isOriented() || canDraw(xx + object.getOrientationLocation().getOffsetx(), yy + object.getOrientationLocation().getOffsety(), region.getRegionWidth(), region.getRegionHeight())) {
                        int offsetX = 0;
                        int offsetY = 0;
                        if (object.isOriented()) {
                            offsetX = object.getOrientationLocation().getOffsetx();
                            offsetY = object.getOrientationLocation().getOffsety();
                        }
                        else {
                            offsetX = object.getCustomOffsetX();
                            offsetY = object.getCustomOffsetY();
                        }
                        batch.draw(region, xx + offsetX, yy + offsetY);
                    }
                }

                Vessel vessel = context.getEntities().getVesselByPosition(x, y);
                if (vessel != null) {
                    // X position of the vessel
                    float xx = getIsometricX(vessel.getX(), vessel.getY(), vessel);

                    // Y position of the vessel
                    float yy = getIsometricY(vessel.getX(), vessel.getY(), vessel);

                    if (canDraw(xx + vessel.getOrientationLocation().getOffsetx(), yy + vessel.getOrientationLocation().getOffsety(), vessel.getRegionWidth(), vessel.getRegionHeight())) {
                        // draw vessel
                        batch.draw(vessel, xx + vessel.getOrientationLocation().getOffsetx(), yy + vessel.getOrientationLocation().getOffsety());
                    }


                    Vector3 v = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                    camera.unproject(v, 0, 200, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() - 200);

                    float xxx = xx + vessel.getOrientationLocation().getOffsetx();
                    float yyy = yy + vessel.getOrientationLocation().getOffsety();

                    if (v.x>= xxx && v.x <= xxx + vessel.getRegionWidth() && v.y >= yyy && v.y <= yyy + vessel.getRegionHeight()) {

                        batch.end();
                        // The vessel radius (diameter / 2)
                        float radious = vessel.getInfluenceRadius();
                        renderer.begin(ShapeRenderer.ShapeType.Line);
                        //renderer.circle(vessel.getX(), vessel.getY(), radious * GameTile.TILE_HEIGHT);

                        Gdx.gl.glEnable(GL20.GL_BLEND);
                        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                        Color color = (context.myVessel.equals(vessel.getName()) || context.myTeam.getID() == vessel.getTeam().getID()) ? Vessel.DEFAULT_BORDER_COLOR.cpy() : vessel.getTeam().getColor().cpy();
                        color.a = 0.35f;

                        renderer.setColor(color);
                        for (int i = 0; i < 5; i++) {
                            int width = (int) (radious * GameTile.TILE_WIDTH) + i;
                            int height = (int) (radious * GameTile.TILE_HEIGHT) + i;
                            renderer.ellipse(xx - width / 2 + vessel.getRegionWidth() / 2, yy - height / 2 + vessel.getRegionHeight() / 2, width, height);
                        }
                        renderer.end();

                        Gdx.gl.glDisable(GL20.GL_BLEND);
                        batch.begin();
                    }
                }
            }
        }

//        public float getIsometricX(float x, float y, TextureRegion region) {
//            return (x * GameTile.TILE_WIDTH / 2) - (y * GameTile.TILE_WIDTH / 2) - (region.getRegionWidth() / 2);
//        }
//
//        public float getIsometricY(float x, float y, TextureRegion region) {
//            return (x * GameTile.TILE_HEIGHT / 2) + (y * GameTile.TILE_HEIGHT / 2) - (region.getRegionHeight() / 2);
//        }

        for (Vessel vessel : context.getEntities().listVesselEntities()) {
            // render cannon balls
        	for (CannonBall c : vessel.getCannonballs()) {
                float cx = getIsometricX(c.getX(), c.getY(), c);
                float cy = getIsometricY(c.getX(), c.getY(), c);
                if (!canDraw(cx, cy, c.getRegionWidth(), c.getRegionHeight())) {
                    continue;
                }

                if (!c.reached()) {
                    batch.draw(c, cx, cy);
                }
                else {
                    cx = getIsometricX(c.getX(), c.getY(), c.getEndingAnimationRegion());
                    cy = getIsometricY(c.getX(), c.getY(), c.getEndingAnimationRegion());
                    batch.draw(c.getEndingAnimationRegion(), cx, cy);
                }
            }

        	// render smoke
            if (vessel.isSmoking()) {
                TextureRegion r = vessel.getShootSmoke();
                float cx = getIsometricX(vessel.getX(), vessel.getY(), r);
                float cy = getIsometricY(vessel.getX(), vessel.getY(), r);
                if (canDraw(cx, cy, r.getRegionWidth(), r.getRegionHeight())) {
                    batch.draw(r, cx, cy);
                }
            }

            batch.end();
            
            // render move bar
            renderer.begin(ShapeRenderer.ShapeType.Line);
            float x = getIsometricX(vessel.getX(), vessel.getY(), vessel);
            float y = getIsometricY(vessel.getX(), vessel.getY(), vessel);

            int width = vessel.getMoveType().getBarWidth();
            renderer.setColor(Color.BLACK);
            renderer.rect(x + (vessel.getRegionWidth() / 2) - (width / 2), Math.round(y + vessel.getRegionHeight() * 1.35f), width, 7);
            renderer.end();
            renderer.begin(ShapeRenderer.ShapeType.Filled);
            renderer.setColor(Color.WHITE);

            int w = (width - 1) / 3;
            int fill = vessel.getNumberOfMoves() > 3 ? 3 : vessel.getNumberOfMoves();

            renderer.rect(x + (vessel.getRegionWidth() / 2) - (width / 2), Math.round(y + vessel.getRegionHeight() * 1.35f) + 1, fill * w, 6);
            renderer.setColor(Color.RED);
            if (vessel.getMoveType() == VesselMoveType.THREE_MOVES && vessel.getNumberOfMoves() > 3) {
                renderer.rect(x + (vessel.getRegionWidth() / 2) - (width / 2) + (width - 1) - 2, Math.round(y + vessel.getRegionHeight() * 1.35f) + 1, 10, 6);
            }
            renderer.end();

            // draw flags above vessel
            batch.begin();
            GlyphLayout layout = new GlyphLayout(font, vessel.getName());

            if (vessel.getName().equalsIgnoreCase(context.myVessel) || vessel.getTeam().getID() == context.myTeam.getID()) {
                font.setColor(Vessel.DEFAULT_BORDER_COLOR);
            }
            else {
                font.setColor(vessel.getTeam().getColor());
            }

            font.draw(batch, vessel.getName(), x + (vessel.getRegionWidth() / 2) - (layout.width / 2), y + vessel.getRegionHeight() * 1.7f);

            float startX = x;
            float flagsY = y + vessel.getRegionHeight() * 1.8f;

            int points = 0;
            for (FlagSymbol symbol : vessel.getFlags()) {
                if (!symbol.isWar()) {
                    points += symbol.getSize();
                }
                batch.draw(symbol, startX, flagsY);
                startX += symbol.getRegionWidth() + 3;
            }

            if (vessel.hasScoreDisplay()) {
                if (points > 0) {
                    Color color = (context.myVessel.equals(vessel.getName()) || context.myTeam.getID() == vessel.getTeam().getID()) ? Vessel.DEFAULT_BORDER_COLOR.cpy() : vessel.getTeam().getColor().cpy();
                    color.a = vessel.getScoreDisplayMovement() / 100f;
                    if (color.a < 0) {
                        color.a = 0;
                    }
                    font.setColor(color);
                    font.draw(batch, "+" + points + " points", x + vessel.getRegionWidth() + 20, y + vessel.getRegionHeight() * 1.7f - (100 - vessel.getScoreDisplayMovement()));
                }
                vessel.tickScoreMovement();
            }
        }
    }


    public float getIsometricX(float x, float y, TextureRegion region) {
        return (x * GameTile.TILE_WIDTH / 2) - (y * GameTile.TILE_WIDTH / 2) - (region.getRegionWidth() / 2);
    }

    public float getIsometricY(float x, float y, TextureRegion region) {
        return (x * GameTile.TILE_HEIGHT / 2) + (y * GameTile.TILE_HEIGHT / 2) - (region.getRegionHeight() / 2);
    }

    @Override
    public void dispose() {
        currentPhase = MovePhase.MOVE_TOKEN;
        currentSlot = -1;
        information.dispose();
        recountVessels();
    }

    @Override
    public boolean handleDrag(float sx, float sy, float x, float y) {
        if (sy > camera.viewportHeight) {
            return false;
        }

        if (this.canDragMap) {
            camera.translate(-x, y);
        }

        return true;
    }

    @Override
    public boolean handleClick(float x, float y, int button) {    	
        if (y < camera.viewportHeight) {
        	// handle camera not following vessel
        	cameraFollowsVessel = false;

            this.canDragMap = true;
            return true;
        }
        this.canDragMap = false;
        return false;
    }

    @Override
    public boolean handleMouseMove(float x, float y) {
        this.mousePosititon = new Vector3(x, y, 0);
        return false;
    }

    @Override
    public boolean handleClickRelease(float x, float y, int button) {
    	if (y < camera.viewportHeight) {
    		// handle camera following/not following vessel
        	if (button == Input.Buttons.RIGHT) {
        		cameraFollowsVessel = false;
        	} else {
        		this.cameraFollowsVessel = true;
        		Vessel vessel = context.getEntities().getVesselByName(context.myVessel);
        		camera.translate(
        				getIsometricX(vessel.getX(), vessel.getY(), vessel) - camera.position.x,
        				getIsometricY(vessel.getX(), vessel.getY(), vessel) - camera.position.y
        		);
        	}
    		
            return true;
        }
        
        this.canDragMap = false;
        return false;
    }



    private void renderSeaBattle() {

        int count = 0;

        // The map tiles
       // GameTile[][] tiles = map.getTiles();

        Cell[][] sea = blockadeMap.getSea();
        Wind[][] winds = blockadeMap.getWinds();
        Whirlpool[][] whirls = blockadeMap.getWhirls();

        for (int i = 0; i < sea.length; i++) {
            for(int j = 0; j < sea[i].length; j++) {
                TextureRegion region = sea[i][j].getRegion();
                int x = (i * GameTile.TILE_WIDTH / 2) - (j * GameTile.TILE_WIDTH / 2) - region.getRegionWidth() / 2;
                int y = (i * GameTile.TILE_HEIGHT / 2) + (j * GameTile.TILE_HEIGHT / 2) - region.getRegionHeight() / 2;

                if (canDraw(x, y, GameTile.TILE_WIDTH, GameTile.TILE_HEIGHT)) {
                    count++;
                    batch.draw(region, x, y);
                    if (winds[i][j] != null) {
                        region = winds[i][j].getRegion();

                        batch.draw(region, x, y);
                    }
                    else if (whirls[i][j] != null) {
                        region = whirls[i][j].getRegion();
                        batch.draw(region, x, y);
                    }
                }
            }
        }
    }

    private boolean canDraw(float x, float y, int width, int height) {
        return x + width >= camera.position.x - camera.viewportWidth / 2 && x <= camera.position.x + camera.viewportWidth / 2 &&
                y + height >= camera.position.y - camera.viewportHeight / 2 && y <= camera.position.y + camera.viewportHeight / 2;
    }

    public void setTurnExecute() {
        this.currentSlot = 0;
        this.currentPhase = MovePhase.MOVE_TOKEN;
        for (Vessel vessel : context.getEntities().listVesselEntities()) {
            vessel.setMovePhase(null);
        }
        recountVessels();
    }

    public BlockadeMap getMap() {
        return blockadeMap;
    }

    public void initializePlayerCamera(Vessel vessel) {
        cameraFollowsVessel = true; // force reset
        camera.translate(
				getIsometricX(vessel.getX(), vessel.getY(), vessel) - camera.position.x,
				getIsometricY(vessel.getX(), vessel.getY(), vessel) - camera.position.y
		);
    }
}
