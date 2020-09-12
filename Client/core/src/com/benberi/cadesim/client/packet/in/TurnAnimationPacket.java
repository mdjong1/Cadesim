package com.benberi.cadesim.client.packet.in;

import com.benberi.cadesim.GameContext;
import com.benberi.cadesim.client.codec.util.Packet;
import com.benberi.cadesim.client.packet.ClientPacketExecutor;
import com.benberi.cadesim.game.entity.vessel.Vessel;
import com.benberi.cadesim.game.entity.vessel.VesselMovementAnimation;
import com.benberi.cadesim.game.entity.vessel.move.MoveAnimationTurn;
import com.benberi.cadesim.game.entity.vessel.move.MovePhase;
import com.benberi.cadesim.game.entity.vessel.move.MoveType;

public class TurnAnimationPacket extends ClientPacketExecutor {

	private GameContext context;
	
    public TurnAnimationPacket(GameContext ctx) {
        super(ctx);
        context = ctx;
    }

    @Override
    public void execute(Packet p) {
    	int numberOfShips = p.readByte();

        for (int i = 0; i < numberOfShips; i++) {
            String name = p.readByteString();

            Vessel vessel = getContext().getEntities().getVesselByName(name);
            if (vessel != null) {
                for (int slot = 0; slot < 4; slot++) {
                    MoveAnimationTurn turn = vessel.getStructure().getTurn(slot);
                    turn.setTokenUsed(MoveType.forId(p.readByte()));
                    turn.setAnimation(VesselMovementAnimation.forId(p.readByte()));
                    turn.setSubAnimation(VesselMovementAnimation.forId(p.readByte()));
                    turn.setLeftShoots(p.readByte());
                    turn.setRightShoots(p.readByte());

                    byte sunk = p.readByte();
                    if (sunk == 1) {
                        turn.setSunk(true);
                    }
                }
            }
            else {
                p.getBuffer().readerIndex(p.getBuffer().readerIndex() + 4);
            }

        }

        getContext().getBattleScene().setTurnExecute();
    }

    @Override
    public int getSize() {
        return -1;
    }
}
