package org.faudroids.loooooading.game;


import android.content.Context;
import android.graphics.PointF;
import android.os.Vibrator;

import org.faudroids.loooooading.R;

import java.util.Iterator;
import java.util.List;

public class GameManager {

	private static final int
			PLAYER_CHEWING_DURATION_IN_MS = 750,
			PLAYER_BLASTED_DURATION_IN_MS = 1500;

	private final int GAME_SHUTDOWN_DELAY;
	private static final int FLYING_SUPERMAN_DELAY = 500;

	private final Context context;
	private final Vibrator vibrator;

	private final Player player;
	private SnowflakesCollection snowflakesCollection;
	private BombsCollection bombsCollection;
	private final SupermanClouds supermanClouds;
	private final Score score;

	private long lastRunTimestamp;
	private int fieldHeight;
	private PointF newPlayerLocation = null;

	private GameState gameState = GameState.STOPPED;
	private long gameShutdownRequestTimestamp = 0; // when the game was stopped, in ms


	public GameManager(Context context) {
		this.GAME_SHUTDOWN_DELAY = context.getResources().getInteger(R.integer.game_shutdown_delay);
		this.context = context;
		this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		this.player = new Player.Builder(context).xPos(100).build();
		this.supermanClouds = new SupermanClouds.Builder(context).build();
		this.score = new Score(context);
	}


	public void start(int fieldWidth, int fieldHeight) {
		this.snowflakesCollection = new SnowflakesCollection(context, fieldWidth);
		this.bombsCollection = new BombsCollection(context, fieldWidth);
		this.fieldHeight = fieldHeight;
		this.lastRunTimestamp = System.currentTimeMillis();
		this.player.setState(PlayerState.DEFAULT);
		onPlayerTouch(fieldWidth / 2); // start with centered player
		this.score.reset();
		this.gameState = GameState.RUNNING;
		this.supermanClouds.setyPos(supermanClouds.getHeight());
	}


	/**
	 * Run the game loop. Call this every couple of ms.
	 *
	 * @return the time in ms since the last loop
	 */
	public long loop() {
		final long currentTimestamp = System.currentTimeMillis();
		final long timeDiff = currentTimestamp - lastRunTimestamp; // in ms
		final int shutdownDiff = (int) (System.currentTimeMillis() - gameShutdownRequestTimestamp);
		final float shutdownProgress = shutdownDiff >= FLYING_SUPERMAN_DELAY && gameState.equals(GameState.SHUTDOWN_REQUESTED)
				? (shutdownDiff - FLYING_SUPERMAN_DELAY) / (float) (GAME_SHUTDOWN_DELAY - FLYING_SUPERMAN_DELAY)
				: 0;

		// create new falling objects
		snowflakesCollection.onTimePassed(timeDiff);
		bombsCollection.onTimePassed(timeDiff);

		if (gameState.equals(GameState.SHUTDOWN_REQUESTED)) {
			// let player fly away
			player.setyPos(getDefaultPlayerHeight() * (1 - shutdownProgress) - (player.getHeight() * shutdownProgress));
			supermanClouds.setyPos(supermanClouds.getHeight() * (1 - shutdownProgress) - ((supermanClouds.getHeight() - fieldHeight) * shutdownProgress));

			// update state
			if (shutdownProgress >= 1) {
				gameState = GameState.STOPPED;
			}

		} else {
			// stop chewing if necessary
			if (player.isInState(PlayerState.CHEWING) && player.getChewingDuration() >= PLAYER_CHEWING_DURATION_IN_MS) {
				player.setState(PlayerState.DEFAULT);
			}

			// stop blasted if necessary
			if (player.isInState(PlayerState.BLASTED)  && player.getBlastedDuration() >= PLAYER_BLASTED_DURATION_IN_MS) {
				player.setState(PlayerState.DEFAULT);
			}

			// update player pos
			if (newPlayerLocation != null) {
				player.setxPos(newPlayerLocation.x);
				player.setyPos(newPlayerLocation.y);
				newPlayerLocation = null;
			}
		}

		boolean playerBelowObject =
				updateFallingObjects(timeDiff, shutdownProgress, snowflakesCollection, true, false)
				| updateFallingObjects(timeDiff, shutdownProgress, bombsCollection, false, true);

		// make player look up!
		if (player.isInState(PlayerState.DEFAULT) || player.isInState(PlayerState.LOOKING_UP)) {
			if (playerBelowObject) {
				player.setState(PlayerState.LOOKING_UP);
			} else {
				player.setState(PlayerState.DEFAULT);
			}
		}

		// update timestamp
		lastRunTimestamp = currentTimestamp;

		return timeDiff;
	}


	/**
	 * @return true if the player is below an object
	 */
	private boolean updateFallingObjects(
			long timeDiff,
			float shutdownProgress,
			FallingObjectsCollection fallingObjects,
			boolean isSnowflake,
			boolean isBomb) {

		if (isSnowflake && isBomb) throw new IllegalArgumentException("cannot be both snowflake and bomb");

		boolean playerBelowObject = false;
		Iterator<FallingObject> iterator = fallingObjects.iterator();
		while (iterator.hasNext()) {
			FallingObject object = iterator.next();

			// update objects
			object.onTimePassed(timeDiff);
			object.setAlpha(Math.max(0, (1 - shutdownProgress)));
			if (object.getyPos() > fieldHeight) {
				iterator.remove();
				continue;
			}

			if (!gameState.equals(GameState.RUNNING)) continue;

			// check for snowflake collisions
			if (isSnowflake) {
				if (player.canEatSnowflake() && player.doesMouthContainPoint(object.getCenter())) {
					iterator.remove();
					player.setState(PlayerState.CHEWING);
					player.startChewingTimer();
					score.onSnowflakeConsumed();
					continue;
				}
			}

			// check for bomb collisions
			if (isBomb) {
				if (player.doesPlayerContainPoint(object.getCenter())) {
					iterator.remove();
					player.setState(PlayerState.BLASTED);
					player.startBlastedTimer();
					score.onHitByBomb();
					vibrator.vibrate(200);
					continue;
				}
			}

			// make player look up
			if (player.isPlayerBelowPoint(object.getCenter())) {
				playerBelowObject = true;
			}
		}
		return playerBelowObject;
	}


	/**
	 * Call if the user wants to change the player pos.
	 * @param xPos x-position of touch event
	 */
	public void onPlayerTouch(float xPos) {
		// if eating / blasted don't move the player
		if (player.isInState(PlayerState.CHEWING) || player.isInState(PlayerState.BLASTED))  return;
		xPos = xPos - player.getDefaultBitmap().getWidth() / 2;
		float yPos = getDefaultPlayerHeight();
		this.newPlayerLocation = new PointF(xPos, yPos);
	}


	public Player getPlayer() {
		return player;
	}


	public SupermanClouds getSupermanClouds() {
		return supermanClouds;
	}


	public List<FallingObject> getSnowflakes() {
		return snowflakesCollection.getObjects();
	}


	public List<FallingObject> getBombs() {
		return bombsCollection.getObjects();
	}


	public Score getScore() {
		return score;
	}


	public GameState getState() {
		return gameState;
	}


	public void requestShutdown() {
		gameState = GameState.SHUTDOWN_REQUESTED;
		gameShutdownRequestTimestamp = System.currentTimeMillis();
		player.setState(PlayerState.SUPERMAN);
	}


	private float getDefaultPlayerHeight() {
		return fieldHeight - player.getDefaultBitmap().getHeight() - context.getResources().getDimension(R.dimen.player_vertical_offset);
	}

}
