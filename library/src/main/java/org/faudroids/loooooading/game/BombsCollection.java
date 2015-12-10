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
		super(2, 2000, 3000);
		this.bombBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bomb);
		this.fieldWidth = fieldWidth;
	}

	@Override
	protected FallingObject createObject() {
		return new FallingObject.Builder(bombBitmap)
				.xPos(RandomUtils.randomInt(bombBitmap.getWidth(), fieldWidth - bombBitmap.getWidth() * 2))
				.yPos(-bombBitmap.getHeight())
				.fallSpeed(RandomUtils.randomInt(200, 250))
				.scale(1)
				.rotation(RandomUtils.randomInt(-30, 30))
				.maxVerticalVelocity(0)
				.verticalVelocityAccelerationDiff(0)
				.accelerateToRight(RandomUtils.randomBoolean())
				.build();
	}
}
