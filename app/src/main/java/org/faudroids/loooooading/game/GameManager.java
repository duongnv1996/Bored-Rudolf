package org.faudroids.loooooading.game;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.faudroids.loooooading.R;
import org.faudroids.loooooading.utils.RandomUtils;
import org.roboguice.shaded.goole.common.base.Optional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

public class GameManager {

	private final Context context;

	private final Bitmap snowflakeBitmap;

	private final Player player;
	private final List<Snowflake> snowflakes = new ArrayList<>();

	private long lastRunTimestamp;
	private int fieldWidth, fieldHeight;
	private int nextSnowflakeCountdown;
	private Optional<Location> newPlayerLocation = Optional.absent();


	@Inject
	GameManager(Context context) {
		this.context = context;
		this.snowflakeBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.snowflake);
		Bitmap playerBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player);
		this.player = new Player.Builder(playerBitmap).xPos(100).yPos(playerBitmap.getHeight()).build();
	}


	public void start(int fieldWidth, int fieldHeight) {
		this.nextSnowflakeCountdown = 0;
		this.fieldWidth = fieldWidth;
		this.fieldHeight = fieldHeight;
		this.lastRunTimestamp = System.currentTimeMillis();
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
		if (nextSnowflakeCountdown <= 0 && snowflakes.size() < 30) {
			Snowflake snowflake = new Snowflake.Builder(snowflakeBitmap)
					.xPos(RandomUtils.randomInt(-snowflakeBitmap.getWidth(), fieldWidth))
					.yPos(-snowflakeBitmap.getHeight())
					.fallSpeed(RandomUtils.randomInt(50, 100))
					.scale(RandomUtils.randomInt(400, 1000) / 1000f)
					.rotation(RandomUtils.randomInt(0, 90))
					.build();

			snowflakes.add(snowflake);
			nextSnowflakeCountdown = RandomUtils.randomInt(20, 50);

		} else {
			--nextSnowflakeCountdown;
		}

		// update snowflakes
		Iterator<Snowflake> iterator = snowflakes.iterator();
		while (iterator.hasNext()) {
			Snowflake snowflake = iterator.next();
			snowflake.onTimePassed(timeDiff);
			if (snowflake.getyPos() > fieldHeight) {
				iterator.remove();
			}
		}

		// update player pos
		if (newPlayerLocation.isPresent()) {
			player.setxPos(newPlayerLocation.get().xPos);
			player.setyPos(newPlayerLocation.get().yPos);
			newPlayerLocation = Optional.absent();
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
		xPos = xPos - player.getBitmap().getWidth() / 2;
		float yPos = fieldHeight - player.getBitmap().getHeight() - context.getResources().getDimension(R.dimen.player_vertical_offset);
		this.newPlayerLocation = Optional.of(new Location(xPos, yPos));
	}


	public Player getPlayer() {
		return player;
	}

	public List<Snowflake> getSnowflakes() {
		return snowflakes;
	}

}
