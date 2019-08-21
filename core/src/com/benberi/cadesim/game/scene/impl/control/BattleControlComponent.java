package com.benberi.cadesim.game.scene.impl.control;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.benberi.cadesim.GameContext;
import com.benberi.cadesim.game.entity.vessel.move.MoveType;
import com.benberi.cadesim.game.scene.SceneComponent;
import com.benberi.cadesim.game.scene.impl.control.hand.HandMove;
import com.benberi.cadesim.game.scene.impl.control.hand.impl.BigShipHandMove;
import com.benberi.cadesim.game.scene.impl.control.hand.impl.SmallShipHandMove;

public class BattleControlComponent extends SceneComponent<ControlAreaScene> {

    /**
     * Left moves
     */
    private int leftMoves = 2;

    /**
     * Right moves
     */
    private int rightMoves = 4;

    /**
     * Forward moves
     */
    private int forwardMoves;

    /**
     * The available shoots
     */
    private int cannons = 0;

    /**
     * The selected moves
     */
    private HandMove[] movesHolder;

    /**
     * Batch renderer for sprites and textures
     */
    private SpriteBatch batch;

    /**
     * Shape renderer for shapes, used for damage/bilge and such
     */
    private ShapeRenderer shape;

    /**
     * Font for texts
     */
    private BitmapFont font;

    /**
     * The target move
     */
    private MoveType targetMove = MoveType.RIGHT;

    /**
     * The damage of the vessel
     */
    private float damageAmount;

    /**
     * The bilge of the vessel
     */
    private float bilgeAmount;

    /**
     * If the move selection is automatic or not
     */
    private boolean auto = true;

    /**
     * The turn time
     */
    private int time = 0;
    
    /**
     * modifier to calculate button placement
     */
    int heightmod = Gdx.graphics.getHeight() - 700;
    int absheight = Gdx.graphics.getHeight(); // absolute height
    
    
    /**
     * Textures
     */
    private Texture shiphand;
    private Texture moves;
    private Texture emptyMoves;
    private TextureRegion leftMoveTexture;
    private TextureRegion rightMoveTexture;
    private TextureRegion forwardMoveTexture;
    private TextureRegion manuaverTexture; // for ships that only are 3 moves
    private TextureRegion emptyLeftMoveTexture;
    private TextureRegion emptyRightMoveTexture;
    private TextureRegion emptyForwardMoveTexture;
    private Texture sandTopTexture;
    private Texture sandBottomTexture;
    private TextureRegion sandBottom;
    private TextureRegion sandTop;

    private TextureRegion emptyCannon;
    private TextureRegion cannon;

    private Texture sandTrickleTexture;
    private TextureRegion sandTrickle;

    private Texture hourGlass;
    private Texture cannonSlots;
    private TextureRegion cannonLeft;
    private TextureRegion cannonRight;
    private TextureRegion emptyCannonLeft;
    private TextureRegion emptyCannonRight;
    private Texture controlBackground;
    private Texture goOceansideBackground;
    private Texture shipStatus;
    private Texture shipStatusBg;
    private TextureRegion damage;
    private TextureRegion bilge;
    private Texture moveGetTargetTexture;
    private TextureRegion moveTargetSelAuto;
    private TextureRegion moveTargetSelForce;
    private Texture title;
    private Texture radioOn;
    private Texture radioOff;
    private Texture autoOn;
    private Texture autoOff;

    private Texture cannonSelection;
    private Texture cannonSelectionEmpty;

    private Texture goOceansideUp;
    private Texture goOceansideDown;
    private int goOceansideBackgroundOriginX = 5+336+5;
	private int goOceansideBackgroundOriginY = 8;
	private int goOceansideOriginX = goOceansideBackgroundOriginX + 19;
	private int goOceansideOriginY = goOceansideBackgroundOriginY + 24;

	/**
	 * state of goOceanside button. true if pushed, false if not.
	 */
	private boolean goOceansideButtonIsDown = false; // initial


    private int manuaverSlot = 3;

    private boolean isBigShip;

    private boolean  isDragging;     //       are we dragging
    private MoveType startDragMove;  // what  are we dragging
    private int      startDragSlot;  // where are we dragging from
    private Vector2 draggingPosition;
    private boolean executionMoves;

    protected BattleControlComponent(GameContext context, ControlAreaScene owner, boolean big) {
        super(context, owner);
        if (big) {
            movesHolder = new BigShipHandMove[4];
            isBigShip = true;
        }
        else {
            movesHolder = new SmallShipHandMove[4];
        }

        for (int i = 0; i < movesHolder.length; i++) {
            movesHolder[i] = createMove();
        }
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        shape = new ShapeRenderer();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("assets/font/Roboto-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 14;
        font = generator.generateFont(parameter);

        title = new Texture("assets/ui/title.png");
        radioOn = new Texture("assets/ui/radio-on.png");
        radioOff = new Texture("assets/ui/radio-off.png");
        autoOn = new Texture("assets/ui/auto-on.png");
        autoOff = new Texture("assets/ui/auto-off.png");
        
        goOceansideUp = new Texture("assets/ui/go_oceanside.png");
        goOceansideDown = new Texture("assets/ui/go_oceansidePressed.png");

        sandTopTexture = new Texture("assets/ui/sand_top.png");
        sandBottomTexture = new Texture("assets/ui/sand_bot.png");

        sandTrickleTexture = new Texture("assets/ui/sand_trickle.png");
        sandTrickle = new TextureRegion(sandTrickleTexture, 0, 0, 1, sandTopTexture.getHeight());

        sandTop = new TextureRegion(sandTopTexture, sandTopTexture.getWidth(), sandTopTexture.getHeight());
        sandBottom= new TextureRegion(sandBottomTexture, sandBottomTexture.getWidth(), sandBottomTexture.getHeight());

        cannonSlots = new Texture("assets/ui/cannonslots.png");
        moves = new Texture("assets/ui/move.png");
        emptyMoves = new Texture("assets/ui/move_empty.png");
        shiphand = new Texture("assets/ui/shiphand.png");
        hourGlass = new Texture("assets/ui/hourglass.png");
        controlBackground = new Texture("assets/ui/moves-background.png");
        goOceansideBackground = new Texture("assets/ui/go_oceanside_background.png");
        shipStatus = new Texture("assets/ui/status.png");
        shipStatusBg = new Texture("assets/ui/status-bg.png");
        moveGetTargetTexture = new Texture("assets/ui/sel_border_square.png");
        cannonSelectionEmpty = new Texture("assets/ui/grapplecannon_empty.png");
        cannonSelection = new Texture("assets/ui/grapplecannon.png");
        damage = new TextureRegion(new Texture("assets/ui/damage.png"));
        bilge = new TextureRegion(new Texture("assets/ui/bilge.png"));
        damage.flip(false, true);
        bilge.flip(false, true);

        emptyCannon = new TextureRegion(cannonSelectionEmpty, 25, 0, 25, 25);
        cannon = new TextureRegion(cannonSelection, 25, 0, 25, 25);

        damage.setRegionWidth(damage.getTexture().getWidth());
        bilge.setRegionWidth(bilge.getTexture().getWidth());

        leftMoveTexture = new TextureRegion(moves, 0, 0, 28, 28);
        forwardMoveTexture = new TextureRegion(moves, 28, 0, 28, 28);
        rightMoveTexture = new TextureRegion(moves, 56, 0, 28, 28);
        manuaverTexture = new TextureRegion(moves, 84, 0, 28, 28);

        emptyLeftMoveTexture = new TextureRegion(emptyMoves, 0, 0, 28, 28);
        emptyForwardMoveTexture = new TextureRegion(emptyMoves, 28, 0, 28, 28);
        emptyRightMoveTexture = new TextureRegion(emptyMoves, 56, 0, 28, 28);

        emptyCannonLeft = new TextureRegion(cannonSlots, 0, 0, 16, 18);
        emptyCannonRight = new TextureRegion(cannonSlots, 16, 0, 16, 18);

        cannonLeft = new TextureRegion(cannonSlots, 32, 0, 16, 18);
        cannonRight = new TextureRegion(cannonSlots, 48, 0, 16, 18);

        moveTargetSelForce = new TextureRegion(moveGetTargetTexture, 0, 0, 36, 36);
        moveTargetSelAuto = new TextureRegion(moveGetTargetTexture, 36, 0, 36, 36);
        
        setDamagePercentage(70);
        setBilgePercentage(30);

    }

    public void setExecutingMoves(boolean flag) {
        this.executionMoves = flag;
    }
    
    public HandMove createMove() {
        if (isBigShip) {
            return new BigShipHandMove();
        }
        return new SmallShipHandMove();
    }

    @Override
    public void update() {
    	int turnDuration = getContext().getTurnDuration();

        double ratio = (double) sandTopTexture.getHeight() / (double) turnDuration;

        sandTop.setRegionY(sandTopTexture.getHeight() - (int) Math.round(time * ratio));
        sandTop.setRegionHeight((int) Math.round(time * ratio));

        ratio =  (double) sandBottomTexture.getHeight() / (double) turnDuration;

        sandBottom.setRegionY(sandBottomTexture.getHeight() - (int) Math.round((turnDuration - time) * ratio));
        sandBottom.setRegionHeight((int) Math.round((turnDuration - time) * ratio));
    }

    @Override
    public void render() {
        renderMoveControl();
        renderGoOceanside();
    }

    @Override
    public void dispose() {
        resetMoves();
        targetMove = MoveType.FORWARD;
        auto = true;
        manuaverSlot = 3;
    }


    @Override
    public boolean handleClick(float x, float y, int button) {
    	// only activate the disengage click if it's not active already
    	if (!goOceansideButtonIsDown) {
    		if (isClickingDisengage(x, y)) {
    			goOceansideButtonIsDown = true;
    		}
    	}
        return false;
    }

    private void handleLeftCannonPlace(float x, float y) {

    }

    private boolean isTogglingAuto(float x, float y) {
        return x >= 52 && x <= 68 && y >= heightmod + 579 && y <= heightmod + 591;
    }

    private boolean isPlacingLeftCannons(float x, float y) {
        return x >= 181 && x <= 206;
    }

    private boolean isPlacingRightCannons(float x, float y) {
        return x >= 241 && x <= 271;
    }
    
    private boolean isClickingDisengage(float x, float y) {
    	return
    		(x >= goOceansideOriginX) &&
    		(x <= (goOceansideOriginX + goOceansideUp.getWidth() + 1)) &&
    		(y >= (absheight - goOceansideOriginY - goOceansideUp.getHeight())) &&
    		(y <= (absheight - goOceansideOriginY - 1));
    }

    private int getSlotForPosition(float x, float y) {
        // battle slots
        if (isPlacingMoves(x, y)) {
            if (y >= heightmod + 538 && y <= heightmod + 569) {
                return 0;
            }
            else if (y >= heightmod + 573 && y <= heightmod + 603) {
               return 1;
            }
            else if (y >= heightmod + 606 && y <= heightmod + 637) {
                return 2;
            }
            else if(y >= heightmod + 642 && y <= heightmod + 670) {
               return 3;
            } else {
                return -1;
            }
        } else if (isPickingMoves(x, y)) { // TODO add cannons to this enum, cant drag those yet
            if (x >= 80 && x <= 108) {
                return 4;
            } else if (x >= 110 && x <= 138) {
                return 5;
            } else if (x >= 140 && x <= 168) {
                return 6;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    @Override
    public boolean handleDrag(float x, float y, float ix, float iy) {
        if (!isDragging) {
            startDragSlot = getSlotForPosition(x, y);
            if (startDragSlot != -1) { // cant start dragging from an invalid region
                isDragging = true;
                switch (startDragSlot) {
                case 4:
                case 5:
                case 6:
                    startDragMove = MoveType.forId(startDragSlot - 3);
                    break;
                default:
                    startDragMove  = movesHolder[startDragSlot].getMove();
                    break;
                }
            }
        }

        if (isDragging) {
            draggingPosition = new Vector2(x, y);
        }
        
        // if we drag off disengage, 
        // deactivate it with no penalty to the user.
        if (goOceansideButtonIsDown) {
        	if (!isClickingDisengage(x, y)) {
        		goOceansideButtonIsDown = false;
        	}
        }
        return false;
    }

    @Override
    public boolean handleRelease(float x, float y, int button) {
        if (isDragging) {
            isDragging = false;
            int endDragSlot = getSlotForPosition(x, y);
            if (endDragSlot == -1) { // dragged from nothing

                if (startDragSlot <= 3) {
                    getContext().sendSelectMoveSlot(startDragSlot, MoveType.NONE);
                }
            } else { // dragged from something
                if ((manuaverSlot == startDragSlot) && endDragSlot <= 3) { // cant drag manauver off to new piece
                    manuaverSlot = endDragSlot;
                    getContext().sendManuaverSlotChanged(manuaverSlot);
                } else if ((manuaverSlot == endDragSlot) && startDragSlot <= 3) { // cant drag new piece onto manuaver
                    manuaverSlot = startDragSlot;
                    getContext().sendManuaverSlotChanged(manuaverSlot);
                }

                if (startDragSlot <= 3 && endDragSlot <= 3) { // drag from place to place; swap
                    // swap
                    int tmpSlot = startDragSlot;
                    startDragSlot = endDragSlot;
                    endDragSlot = tmpSlot;

                    // update
                    getContext().sendSelectMoveSlot(startDragSlot, movesHolder[endDragSlot].getMove());
                    getContext().sendSelectMoveSlot(endDragSlot, movesHolder[startDragSlot].getMove());
                } else if (startDragSlot > 3 && endDragSlot <= 3) { // moving from available to placed; replace
                    // if there's anything there already, replace it, apart from a manuaver slot
                    if (manuaverSlot != endDragSlot) {
                        getContext().sendSelectMoveSlot(endDragSlot, MoveType.forId(startDragSlot-3));
                    }
                }
            }

            draggingPosition = null;
        } else {
            if (executionMoves) {
                return false;
            }
			if (isPlacingMoves(x, y)) {
			    if (y >= heightmod + 538 && y <= heightmod + 569) {
			        handleMovePlace(0, button);
			    }
			    else if (y >= heightmod + 573 && y <= heightmod + 603) {
			        handleMovePlace(1, button);
			    }
			    else if (y >= heightmod + 606 && y <= heightmod + 637) {
			        handleMovePlace(2, button);
			    }
			    else if(y >= heightmod + 642 && y <= heightmod + 670) {
			        handleMovePlace(3, button);
			    }
			}
			else if (isPlacingLeftCannons(x, y)) {
			    if (y >= heightmod + 548 && y <= heightmod + 562) {
			        getContext().sendAddCannon(0, 0);
			    }
			    else if (y >= heightmod + 582 && y <= heightmod + 597) {
			        getContext().sendAddCannon(0, 1);
			    }
			    else if (y >= heightmod + 618 && y <= heightmod + 630) {
			        getContext().sendAddCannon(0, 2);
			    }
			    else if (y >= heightmod + 650 && y <= heightmod + 665) {
			        getContext().sendAddCannon(0, 3);
			    }
			}
			else if (isPlacingRightCannons(x, y)) {
			    if (y >= heightmod + 548 && y <= heightmod + 562) {
			        getContext().sendAddCannon(1, 0);
			    }
			    else if (y >= heightmod + 582 && y <= heightmod + 597) {
			        getContext().sendAddCannon(1, 1);
			    }
			    else if (y >= heightmod + 618 && y <= heightmod + 630) {
			        getContext().sendAddCannon(1, 2);
			    }
			    else if (y >= heightmod + 650 && y <= heightmod + 665) {
			        getContext().sendAddCannon(1, 3);
			    }
			}
			else if (isTogglingAuto(x, y)) {
			    if (auto) {
			        auto = false;
			    }
			    else {
			        auto = true;
			    }
			    getContext().sendToggleAuto(auto);
			}
			else if (isClickingDisengage(x, y)) {
				getContext().sendOceansideRequestPacket();
				goOceansideButtonIsDown = false;
			}
			else if (!auto){
			    if (isChosedLeft(x, y)) {
			        this.targetMove = MoveType.LEFT;
			        getContext().sendGenerationTarget(targetMove);
			    }
			    else if (isChosedForward(x, y)) {
			        this.targetMove = MoveType.FORWARD;
			        getContext().sendGenerationTarget(targetMove);
			    }
			     else if (isChosedRight(x, y)) {
			         this.targetMove = MoveType.RIGHT;
			         getContext().sendGenerationTarget(targetMove);
			    }
			}
	    }
        return false;
    }

    private void handleMovePlace(int position, int button) {
        if (position == manuaverSlot) {
            return;
        }
        if (isDragging) {
            return;
        }
        HandMove move = movesHolder[position];
        if (move.getMove() == MoveType.NONE) {
            if (button == Input.Buttons.LEFT) {
                if (leftMoves > 0) {
                    placeMove(position, MoveType.LEFT, true);
                    getContext().sendSelectMoveSlot(position, MoveType.LEFT);
                }
                else if (forwardMoves > 0) {
                    placeMove(position, MoveType.FORWARD, true);
                    getContext().sendSelectMoveSlot(position, MoveType.FORWARD);
                }
                else if (rightMoves > 0) {
                    placeMove(position, MoveType.RIGHT, true);
                    getContext().sendSelectMoveSlot(position, MoveType.RIGHT);
                }
            }
            else if (button == Input.Buttons.MIDDLE) {
                if (forwardMoves > 0) {
                    placeMove(position, MoveType.FORWARD, true);
                    getContext().sendSelectMoveSlot(position, MoveType.FORWARD);
                }
                else if (rightMoves > 0) {
                    placeMove(position, MoveType.RIGHT, true);
                    getContext().sendSelectMoveSlot(position, MoveType.RIGHT);
                }
                else if (leftMoves > 0) {
                    placeMove(position, MoveType.LEFT, true);
                    getContext().sendSelectMoveSlot(position, MoveType.LEFT);
                }
            }
            else if (button == Input.Buttons.RIGHT) {
                if (rightMoves > 0) {
                    placeMove(position, MoveType.RIGHT, true);
                    getContext().sendSelectMoveSlot(position, MoveType.RIGHT);
                }
                else if (forwardMoves > 0) {
                    placeMove(position, MoveType.FORWARD, true);
                    getContext().sendSelectMoveSlot(position, MoveType.FORWARD);
                }
                else if (leftMoves > 0) {
                    placeMove(position, MoveType.LEFT, true);
                    getContext().sendSelectMoveSlot(position, MoveType.LEFT);
                }
            }
        }
        else {
            if (button == Input.Buttons.LEFT) {
                MoveType next = move.getMove().getNext();
                if (hasMove(next)) {
                    placeMove(position, next, true);
                    getContext().sendSelectMoveSlot(position, next);
                }
                else if (hasMove(next.getNext())) {
                    placeMove(position, next.getNext(), true);
                    getContext().sendSelectMoveSlot(position, next.getNext());
                }
                else if (hasMove(next.getNext().getNext())) {
                    placeMove(position, next.getNext().getNext(), true);
                    getContext().sendSelectMoveSlot(position, next.getNext().getNext());
                }
            }
            else if (button == Input.Buttons.RIGHT) {
                MoveType prev = move.getMove().getPrevious();
                if (hasMove(prev)) {
                    placeMove(position, prev, true);
                    getContext().sendSelectMoveSlot(position, prev);
                }
                else if (hasMove(prev.getPrevious())) {
                    placeMove(position, prev.getPrevious(), true);
                    getContext().sendSelectMoveSlot(position, prev.getPrevious());
                }
                else if (hasMove(prev.getPrevious().getPrevious())) {
                    placeMove(position, prev.getPrevious().getPrevious(), true);
                    getContext().sendSelectMoveSlot(position, prev.getPrevious().getPrevious());
                }
            }
        }

    }

    private boolean hasMove(MoveType move) {
        switch (move) {
            case LEFT:
                return leftMoves > 0;
            case RIGHT:
                return rightMoves > 0;
            case FORWARD:
                return forwardMoves > 0;
            default:
                return true;
        }
    }

    private boolean isPlacingMoves(float x, float y) {
        return x >= 208 && x <= 239 && y >= (heightmod + 538) && y <= (heightmod + 670);
    }

    private boolean isPickingMoves(float x, float y) {
        return (x >= 80) && (x <= 166) && (y >= heightmod + 598) && (y <= heightmod + 624);
    }


    /**
     * Sets the turn time
     * @param time  The turn time in seconds
     */
    public void setTime(int time) {
        this.time = time;
        int sandX = sandTrickle.getRegionX();
        sandX++;
        if (sandX > sandTrickleTexture.getWidth()) {
            sandX = 0;
        }

        sandTrickle.setRegionX(sandX);
        sandTrickle.setRegionWidth(1);
    }

    /**
     * Sets the damage percentage
     * @param d The percentage to set out of 100
     */
    public void setDamagePercentage(int d) {
        if (d > 100) {
            d = 100;
        }
        this.damageAmount = d;
    }

    /**
     * Sets the bilge percentage
     * @param b The percentage to set out of 100
     */
    public void setBilgePercentage(int b) {
        if (b > 100) {
            b = 100;
        }
        this.bilgeAmount = b;
    }

    public void setMoveSelAutomatic(boolean auto) {
        this.auto = auto;
    }

    /**
     * Sets the available moves to use
     * @param left      The amount of left movements
     * @param forward   The amount of forward movements
     * @param right     The amount of right movements
     */
    public void setMoves(int left, int forward, int right) {
        this.leftMoves = left;
        this.forwardMoves = forward;
        this.rightMoves = right;
    }

    /**
     * Sets the available cannonballs to use
     * @param cannonballs The number of available cannonballs for use
     */
    public void setLoadedCannonballs(int cannonballs) {
        this.cannons = cannonballs;
    }

    private void renderMoveControl() {
        batch.begin();

        // The yellow BG for tokens and moves and hourglass
        batch.draw(controlBackground, 5, 8, controlBackground.getWidth(), controlBackground.getHeight() + 5);
        
        drawMoveHolder();
        drawShipStatus();
        drawTimer();
        drawMovesSelect();
        TextureRegion t = manuaverTexture; // initial, prevent crashes
        batch.draw(title, 65, 140);
        if (isDragging && startDragSlot != -1) {
            if (startDragSlot == manuaverSlot) {
                t = manuaverTexture;
            } else {
                t = getTextureForMove(startDragMove);
            }

            if ((startDragMove != MoveType.NONE) || (startDragSlot == manuaverSlot)) {
                batch.draw(t, draggingPosition.x - t.getRegionWidth() / 2, Gdx.graphics.getHeight() - draggingPosition.y - t.getRegionHeight() / 2);
            }
        }
        batch.end();
    }
    
    /**
     * background for disengage button / pirates aboard
     */
    private void renderGoOceanside() {
    	batch.begin();
        batch.draw(goOceansideBackground, goOceansideBackgroundOriginX, goOceansideBackgroundOriginY, goOceansideBackground.getWidth(), goOceansideBackground.getHeight() + 5); 
        if (goOceansideButtonIsDown == false) {
        	batch.draw(goOceansideUp, goOceansideOriginX, goOceansideOriginY, goOceansideUp.getWidth(), goOceansideUp.getHeight());
        }
        else {
        	batch.draw(goOceansideDown, goOceansideOriginX, goOceansideOriginY, goOceansideDown.getWidth(), goOceansideDown.getHeight());
        }
        batch.end();
    }

    private void drawMoveHolder() {

        // The hand bg
        batch.draw(shiphand, controlBackground.getWidth() - shiphand.getWidth() - 80, 19);


        int height = controlBackground.getHeight() - 40;
        for (int i = 0; i < movesHolder.length; i++) {
            HandMove move = movesHolder[i];

            boolean[] left = move.getLeft();
            boolean[] right = move.getRight();

            batch.draw(emptyCannonLeft, controlBackground.getWidth() - shiphand.getWidth() - 81, height); // left
            if (left[0]) {
                batch.draw(cannonLeft, controlBackground.getWidth() - shiphand.getWidth() - 81, height); // left
            }
            batch.draw(emptyCannonRight, controlBackground.getWidth() - shiphand.getWidth() - 35, height); // right
            if (right[0]) {
                batch.draw(cannonRight, controlBackground.getWidth() - shiphand.getWidth() - 35, height); // left
            }

            if (movesHolder instanceof BigShipHandMove[]) {
                batch.draw(emptyCannonLeft, controlBackground.getWidth() - shiphand.getWidth() - 96, height); // left
                if (left[0] && left[1]) {
                    batch.draw(cannonLeft, controlBackground.getWidth() - shiphand.getWidth() - 96, height); // left
                }
                batch.draw(emptyCannonRight, controlBackground.getWidth() - shiphand.getWidth() - 20, height); // right
                if (right[0] && right[1]) {
                    batch.draw(cannonRight, controlBackground.getWidth() - shiphand.getWidth() - 20, height); // right
                }
            }

            if (i == manuaverSlot) {
                batch.draw(manuaverTexture, controlBackground.getWidth() - shiphand.getWidth() - 64, height - 4);
            }
            else {
                if (move.getMove() != MoveType.NONE) {
                    Color color = batch.getColor();
                    if (move.isMoveTemp()) {
                        batch.setColor(0.5F, 0.5F, 0.5F, 1F);
                    }
                    else {
                        batch.setColor(color.r, color.g, color.b, 1f);
                    }

                    batch.draw(getTextureForMove(move.getMove()), controlBackground.getWidth() - shiphand.getWidth() - 64, height - 4);
                    batch.setColor(color.r, color.g, color.b, 1f);
                }
            }

            height -= 34;
        }
    }

    private TextureRegion getTextureForMove(MoveType type) {
        switch (type) {
            case LEFT:
                return leftMoveTexture;
            case RIGHT:
                return rightMoveTexture;
            default:
            case FORWARD:
                return forwardMoveTexture;
        }
    }

    /**
     * Draws the movement selection section
     */
    private void drawMovesSelect() {

        font.draw(batch, "Auto", 18, controlBackground.getHeight() - 54);
        if (auto) {
            batch.draw(autoOn, 53, controlBackground.getHeight() - 70);
        }
        else {
            batch.draw(autoOff, 53, controlBackground.getHeight() - 70);
        }

        if (cannons > 0) {
            batch.draw(cannon, 49, controlBackground.getHeight() - 103);
        }
        else {
            batch.draw(emptyCannon, 49, controlBackground.getHeight() - 103);
        }

        font.draw(batch, "x" + Integer.toString(cannons), 56, controlBackground.getHeight() - 109);

        int x = 80;
        int y = controlBackground.getHeight() - 100;

        if (leftMoves == 0) {
            batch.draw(emptyLeftMoveTexture, x, y);
        }
        else {
            batch.draw(leftMoveTexture, x, y);
        }

        if (forwardMoves == 0) {
            batch.draw(emptyForwardMoveTexture, x + emptyLeftMoveTexture.getRegionWidth() + 2, y);
        }
        else {
            batch.draw(forwardMoveTexture, x + emptyLeftMoveTexture.getRegionWidth() + 2, y);
        }

        if (rightMoves == 0) {
            batch.draw(emptyRightMoveTexture, x + (emptyLeftMoveTexture.getRegionWidth() * 2) + 4, y);
        }
        else {
            batch.draw(rightMoveTexture, x + (emptyLeftMoveTexture.getRegionWidth() * 2) + 4, y);
        }


        TextureRegion sel = auto ? moveTargetSelAuto : moveTargetSelForce;

        if (targetMove != null) {
            switch(targetMove) {
                case LEFT:
                    batch.draw(sel, x - 4, y - 4);
                    break;
                case FORWARD:
                    batch.draw(sel, x + emptyLeftMoveTexture.getRegionWidth() + 2 - 4, y - 4);
                    break;
                case RIGHT:
                    batch.draw(sel, x + (emptyLeftMoveTexture.getRegionWidth() * 2) + 4 - 4, y - 4);
                    break;


            }
        }



        font.setColor(Color.BLACK);
        font.draw(batch, "x" + Integer.toString(leftMoves), x + 5, y - 5);
        font.draw(batch, "x" + Integer.toString(forwardMoves), x + emptyLeftMoveTexture.getRegionWidth() + 2 + 5, y - 5);
        font.draw(batch, "x" + Integer.toString(rightMoves), x + (emptyLeftMoveTexture.getRegionWidth() * 2) + 4 + 5, y - 5);
    }

    /**
     * Draws the sand clock
     */
    private void drawTimer() {
        batch.draw(hourGlass, controlBackground.getWidth() - hourGlass.getWidth() - 20, 25);
        batch.draw(sandTrickle,controlBackground.getWidth() - hourGlass.getWidth() - 7, 30 );
        batch.draw(sandTop, controlBackground.getWidth() - hourGlass.getWidth() - 16, 72);
        batch.draw(sandBottom, controlBackground.getWidth() - hourGlass.getWidth() - 16, 28);
    }

    /**
     * Draws ship status
     *
     * Ship damage, Ship bilge, etc
     */
    private void drawShipStatus() {
        int x = controlBackground.getWidth() - shipStatus.getWidth() - 12;
        int y = controlBackground.getHeight() - 50;
        batch.draw(shipStatusBg, x, y);

        batch.end();

        shape.begin(ShapeRenderer.ShapeType.Filled);

        // The values for damage and water are hard-coded here, they
        // should come from your code

        float redstuff = damageAmount / 100f;
        float redStart = 90.0f + 180.0f * (1.0f - redstuff);
        float redLength = 180.0f * redstuff;

        float bluestuff = bilgeAmount / 100;

        float blueStart = 270.0f;
        float blueLength = 180.0f * bluestuff;

        shape.setColor(new Color(131 / 255f, 6 / 255f, 0f, .7f));
        shape.arc(301, 146, 16.5f, redStart, redLength);
        shape.setColor(new Color(0f, 207 / 255f, 249f, .7f));
        shape.arc(304, 146, 16.5f, blueStart, blueLength);
        shape.end();

        batch.begin();

        batch.draw(shipStatus, x, y);
    }


    public boolean isChosedLeft(float x, float y) {
        return x >= 80 && x <= 107 && y >= heightmod + 598 && y <= heightmod + 624;
    }

    public boolean isChosedForward(float x, float y) {
        return x >= 110 && x <= 135 && y >= heightmod + 598 && y <= heightmod + 624;
    }

    public boolean isChosedRight(float x, float y) {
        return x >= 140 && x <= 166 && y >= heightmod + 598 && y <= heightmod + 624;
    }

    public void placeMove(int slot, MoveType move, boolean temp) {
        HandMove hm = movesHolder[slot];
        hm.setMove(move);
        hm.setMoveTemporary(temp);
    }

    public void resetMoves() {
        for (int i = 0; i < movesHolder.length; i++) {
            movesHolder[i].setMove(MoveType.NONE);
            movesHolder[i].resetLeft();
            movesHolder[i].resetRight();

            // fix stuck moves that might appear after a turn completes
            getContext().sendSelectMoveSlot(i, MoveType.NONE);
        }
        manuaverSlot = 3;
        getContext().sendManuaverSlotChanged(3);

        // fix stuck disengage button if it was clicked across a turn
        // with no penalty to the user
        if (goOceansideButtonIsDown) {
        	goOceansideButtonIsDown = false;
        }
    }

    public void setCannons(int side, int slot, int amount) {
        if (side == 0) {
            movesHolder[slot].resetLeft();
            for (int i = 0; i < amount; i++)
                movesHolder[slot].addLeft();
        }
        else if (side == 1) {
            movesHolder[slot].resetRight();
            for (int i = 0; i < amount; i++)
                movesHolder[slot].addRight();
        }
    }

    public void setMoveSealTarget(MoveType moveSealTarget) {
        this.targetMove = moveSealTarget;
    }

    public void setMovePlaces(byte[] moves, byte[] left, byte[] right) {
        for (int slot = 0; slot < 4; slot++) {
            HandMove move = movesHolder[slot];
            move.setMoveTemporary(false);
            move.resetRight();
            move.resetLeft();

            move.setMove(MoveType.forId(moves[slot]));
            for (int i = 0; i < left[slot]; i++) {
                move.addLeft();
            }
            for (int i = 0; i < right[slot]; i++) {
                move.addRight();
            }
        }
    }
}
