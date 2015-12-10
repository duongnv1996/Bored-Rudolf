package org.faudroids.loooooading.game;

import android.graphics.Bitmap;

import org.faudroids.loooooading.utils.RandomUtils;

class SnowflakesCollection extends FallingObjectsCollection {

	private final Bitmap snowflakeBitmap;
	private final int fieldWidth;

	public SnowflakesCollection(Bitmap snowflakeBitmap, int fieldWidth) {
		super(10, 1000, 1500);
		this.snowflakeBitmap = snowflakeBitmap;
		this.fieldWidth = fieldWidth;
	}

	@Override
	protected FallingObject createObject() {
		return new FallingObject.Builder(snowflakeBitmap)
				.xPos(RandomUtils.randomInt(snowflakeBitmap.getWidth(), fieldWidth - snowflakeBitmap.getWidth() * 2))
				.yPos(-snowflakeBitmap.getHeight())
				.fallSpeed(RandomUtils.randomInt(100, 150))
				.scale(RandomUtils.randomInt(750, 1000) / 1000f)
				.rotation(RandomUtils.randomInt(0, 90))
				.maxVerticalVelocity(RandomUtils.randomInt(90, 150))
				.verticalVelocityAccelerationDiff(RandomUtils.randomInt(80, 120))
				.accelerateToRight(RandomUtils.randomBoolean())
				.build();
	}
}
