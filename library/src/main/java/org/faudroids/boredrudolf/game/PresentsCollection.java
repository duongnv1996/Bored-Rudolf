package org.faudroids.boredrudolf.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.faudroids.boredrudolf.R;
import org.faudroids.boredrudolf.utils.RandomUtils;

class PresentsCollection extends FallingObjectsCollection {

	private final Bitmap[] presentBitmaps;
	private final int fieldWidth;

	public PresentsCollection(Context context, int fieldWidth) {
		super(2, 1500, 2500);
		this.presentBitmaps = new Bitmap[3];
		presentBitmaps[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.present_blue);
		presentBitmaps[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.present_pink);
		presentBitmaps[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.present_yellow);
		this.fieldWidth = fieldWidth;
	}

	@Override
	protected FallingObject createObject() {
		int bitmapSize = presentBitmaps[0].getWidth();
		return new FallingObject.Builder(presentBitmaps[RandomUtils.randomInt(0, 2)])
				.xPos(RandomUtils.randomInt(bitmapSize, fieldWidth - bitmapSize * 2))
				.yPos(-bitmapSize)
				.fallSpeed(RandomUtils.randomInt(250, 300))
				.scale(1)
				.rotation(RandomUtils.randomInt(-45, 45))
				.maxVerticalVelocity(0)
				.verticalVelocityAccelerationDiff(0)
				.accelerateToRight(RandomUtils.randomBoolean())
				.build();
	}
}
