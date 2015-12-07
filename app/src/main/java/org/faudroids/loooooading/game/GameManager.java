package org.faudroids.loooooading.game;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.RectF;

import org.faudroids.loooooading.R;
import org.faudroids.loooooading.utils.RandomUtils;
import org.roboguice.shaded.goole.common.base.Optional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

public class GameManager {

	private static final int PLAYER_CHEWING_DURATION_IN_MS = 750;

	private final Context context;

	private final Bitmap snowflakeBitmap;

	private final Player player;
	private final List<Snowflake> snowflakes = new ArrayList<>();
	private final Score score = new Score();

	private long lastRunTimestamp;
	private int fieldWidth, fieldHeight;
	private int nextSnowflakeCountdown;
	private Optional<PointF> newPlayerLocation = Optional.absent();


	@Inject
	public GameManager(Context context) {
		this.context = context;
		this.snowflakeBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.snowflake);
		this.player = new Player.Builder(context).xPos(100).build();
	}


	public void start(int fieldWidth, int fieldHeight) {
		this.nextSnowflakeCountdown = 0;
		this.snowflakes.clear();
		this.fieldWidth = fieldWidth;
		this.fieldHeight = fieldHeight;
		this.lastRunTimestamp = System.currentTimeMillis();
		this.player.setState(PlayerState.DEFAULT);
		onPlayerTouch(fieldWidth / 2); // start with centered player
		this.score.reset();
	}


	/**
	 * Run the game loop. Call this every couple of ms.
	 *
	 * @return the time in ms since the last loop
	 */
	public long loop() {
		final long currentTimestamp = System.currentTimeMillis();
		final long timeDiff = currentTimestamp - lastRunTimestamp;

		// create new snowflakes
		if (nextSnowflakeCountdown <= 0 && snowflakes.size() < 10) {
			Snowflake snowflake = new Snowflake.Builder(snowflakeBitmap)
					.xPos(RandomUtils.randomInt(snowflakeBitmap.getWidth(), fieldWidth - snowflakeBitmap.getWidth() * 2))
					.yPos(-snowflakeBitmap.getHeight())
					.fallSpeed(RandomUtils.randomInt(100, 150))
					.scale(RandomUtils.randomInt(750, 1000) / 1000f)
					.rotation(RandomUtils.randomInt(0, 90))
					.build();

			snowflakes.add(snowflake);
			nextSnowflakeCountdown = RandomUtils.randomInt(20, 50);

		} else {
			--nextSnowflakeCountdown;
		}

		// stop chewing if necessary
		if (player.getChewingDuration() >= PLAYER_CHEWING_DURATION_IN_MS) {
			player.setState(PlayerState.DEFAULT);
		}

		// update player pos
		if (newPlayerLocation.isPresent()) {
			player.setxPos(newPlayerLocation.get().x);
			player.setyPos(newPlayerLocation.get().y);
			newPlayerLocation = Optional.absent();
		}

		boolean playerBelowSnowflake = false;
		Iterator<Snowflake> iterator = snowflakes.iterator();
		while (iterator.hasNext()) {

			// update snowflakes
			Snowflake snowflake = iterator.next();
			snowflake.onTimePassed(timeDiff);
			if (snowflake.getyPos() > fieldHeight) {
				iterator.remove();
				continue;
			}

			// check for collisions
			RectF mouthRect = player.getMouthRect();
			if (player.canEatSnowflake() && mouthRect.contains(snowflake.getCenter().x, snowflake.getCenter().y)) {
				iterator.remove();
				player.setState(PlayerState.CHEWING);
				player.startChewingTimer();
				score.onSnowflakeConsumed();
				continue;
			}

			// make player look up
			if (mouthRect.left <= snowflake.getCenter().x
					&& mouthRect.right > snowflake.getCenter().x
					&& mouthRect.bottom >= snowflake.getCenter().y) {
				playerBelowSnowflake = true;
			}
		}

		// let him finish eating that snow!
		if (!player.getState().equals(PlayerState.CHEWING)) {
			if (playerBelowSnowflake) {
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
	 * Call if the user wants to change the player pos.
	 * @param xPos x-position of touch event
	 */
	public void onPlayerTouch(float xPos) {
		// if eating don't move the player
		if (player.getState().equals(PlayerState.CHEWING)) return;
		xPos = xPos - player.getDefaultBitmap().getWidth() / 2;
		float yPos = fieldHeight - player.getDefaultBitmap().getHeight() - context.getResources().getDimension(R.dimen.player_vertical_offset);
		this.newPlayerLocation = Optional.of(new PointF(xPos, yPos));
	}


	public Player getPlayer() {
		return player;
	}


	public List<Snowflake> getSnowflakes() {
		return snowflakes;
	}


	public Score getScore() {
		return score;
	}

}
