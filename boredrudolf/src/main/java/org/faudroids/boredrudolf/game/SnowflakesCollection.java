package org.faudroids.boredrudolf.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.faudroids.boredrudolf.R;
import org.faudroids.boredrudolf.utils.RandomUtils;

class SnowflakesCollection extends FallingObjectsCollection {

	private final Bitmap snowflakeBitmap;
	private final int fieldWidth;

	public SnowflakesCollection(Context context, int fieldWidth) {
		super(10, 1000, 1500);
		this.snowflakeBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.snowflake);
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
