package com.benberi.cadesim.server.service;

import com.benberi.cadesim.server.ServerContext;
import com.benberi.cadesim.server.config.Constants;
import com.benberi.cadesim.server.config.ServerConfiguration;
import com.benberi.cadesim.server.model.player.PlayerManager;

/**
 * This is the "heartbeat" main loop of the game server
 */
public class GameService implements Runnable {
    /**
     * The server context
     */
    private ServerContext context;
    private PlayerManager playerManager; // this called so often, should cache
    
    /**
     * Keep track of how many games we've played
     * And when we last rotated the map
     */
    private int gamesCompleted = 0;
    private int lastMapRotation = 0;

    public GameService(ServerContext context) {
        this.context = context;
        this.playerManager = context.getPlayerManager();
    }

    /**
     * helper method to randomly rotate the map
     */
    private void randomRotateMap() {
        // rotate the current one
    	ServerConfiguration.setMapName(
            ServerConfiguration.getNextMapName()
    	);

        // generate the next one
        ServerConfiguration.pregenerateNextMapName();
        ServerContext.log("pre-generated the next map name in rotation: " + ServerConfiguration.getNextMapName());

        // renew it
    	context.renewMap();
    }

    @Override
    public void run() {
        try {
            context.getPackets().queuePackets();
            playerManager.tick();
            playerManager.queueOutgoing();
            context.getTimeMachine().tick();

            if(playerManager.isGameEnded()) {
            	// print out the scores for the game
            	playerManager.serverBroadcastMessage(
            			"Round ended, final scores were:\n" +
            			"    Defender:" + playerManager.getPointsDefender() + "\n" + 
            			"    Attacker:" + playerManager.getPointsAttacker()
            	);

            	ServerContext.log("Ending game #" + Integer.toString(gamesCompleted) + ".");
            	gamesCompleted++;

                // handle switching maps.
                String oldMap = ServerConfiguration.getMapName();
                if (playerManager.shouldSwitchMap())
                {
                	randomRotateMap();
                    lastMapRotation = gamesCompleted;
                	ServerContext.log(
                		"Players voted to switch map; rotated map to: " +
                		ServerConfiguration.getMapName()
                	);
                }
                else if (playerManager.shouldRestartMap())
                {
                    ServerContext.log(
                        "Players voted to restart map; keeping map: " +
                        ServerConfiguration.getMapName()
                    );
                }
                else if (!ServerConfiguration.getRunContinuousMode())
                {
                    // it would be cruel to exit early if players voted for a restart/nextmap
                    ServerContext.log("Not in run-continuous mode, so quitting early.");
                    System.exit(Constants.EXIT_SUCCESS);
                }
                else if (playerManager.isUpdateScheduledAfterGame()) {
                    // check for updates and restart the server if we need to
                    java.io.File f = new java.io.File(Constants.AUTO_UPDATING_LOCK_DIRECTORY_NAME);
                    int sleep_ms = 2000;
                    int sleepTotal = 0;
                    while (!f.mkdir()) {
                        ServerContext.log("UPDATER: Waiting to update... (" + sleepTotal + ")");
                        Thread.sleep(sleep_ms); // TODO need exit condition
                        sleepTotal += sleep_ms;
                    }

                    ServerContext.log("UPDATER: Created lock directory (" + f.getName() + ")");

                    // TODO

                    System.exit(Constants.EXIT_SUCCESS_SCHEDULED_UPDATE);
                }
                else if (
                        (ServerConfiguration.getMapRotationPeriod() > 0) && // -1 == don't rotate, 0 invalid
                        ((gamesCompleted - lastMapRotation) >= ServerConfiguration.getMapRotationPeriod())
                ) {
                    lastMapRotation = gamesCompleted;

                    randomRotateMap();

                	ServerContext.log(
                		"Rotated map after " +
                		Integer.toString(ServerConfiguration.getMapRotationPeriod()) +
                		" games, automatically chose random map: " +
                		ServerConfiguration.getMapName()
                	);
                }
                
                // message if map changed
                String newMap = ServerConfiguration.getMapName();
                if (!newMap.contentEquals(oldMap))
                {
                	playerManager.serverBroadcastMessage("Changed map to " + newMap);
                }

                // complete the game refresh
                playerManager.renewGame();
                context.getTimeMachine().renewRound(); // bugfix - order matters

                playerManager.serverBroadcastMessage("Started new round: #" + (gamesCompleted + 1));
            }

        } catch (Exception e) {
            e.printStackTrace();
            ServerContext.log(e.getMessage());
        }
    }
}
