package org.faudroids.loooooading.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.faudroids.loooooading.R;
import org.faudroids.loooooading.utils.RandomUtils;

class BombsCollection extends FallingObjectsCollection {

	private final Bitmap bombBitmap;
	private final int fieldWidth;

	public BombsCollection(Context context, int fieldWidth) {
		super(2, 1500, 2500);
		this.bombBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bomb);
		this.fieldWidth = fieldWidth;
	}

	@Override
	protected FallingObject createObject() {
		return new FallingObject.Builder(bombBitmap)
				.xPos(RandomUtils.randomInt(bombBitmap.getWidth(), fieldWidth - bombBitmap.getWidth() * 2))
				.yPos(-bombBitmap.getHeight())
				.fallSpeed(RandomUtils.randomInt(250, 300))
				.scale(1)
				.rotation(RandomUtils.randomInt(-45, 45))
				.maxVerticalVelocity(0)
				.verticalVelocityAccelerationDiff(0)
				.accelerateToRight(RandomUtils.randomBoolean())
				.build();
	}
}
