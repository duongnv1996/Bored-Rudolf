package org.faudroids.loooooading.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;

import org.faudroids.loooooading.R;

/**
 * The player!
 */
public class Player {

	// used for getting the final orientation
	private final Matrix matrix = new Matrix();
	private final Bitmap defaultBitmap, lookingUpBitmap;

	private final PointF location;

	private final RectF mouthRect = new RectF();
	private final float mouthWidth, mouthHeight;
	private final float mouthOffsetFromBottom;

	private Player(Bitmap defaultBitmap, Bitmap lookingUpBitmap, PointF location, float mouthWidth, float mouthHeight, float mouthOffsetFromBottom) {

		this.defaultBitmap = defaultBitmap;
		this.lookingUpBitmap = lookingUpBitmap;
		this.location = location;
		this.mouthWidth = mouthWidth;
		this.mouthHeight = mouthHeight;
		this.mouthOffsetFromBottom = mouthOffsetFromBottom;
		updateMouthRect();
	}

	public void setxPos(float xPos) {
		this.location.x = xPos;
		updateMouthRect();
	}

	public void setyPos(float yPos) {
		this.location.y = yPos;
		updateMouthRect();
	}

	public float getxPos() {
		return location.x;
	}

	public float getyPos() {
		return location.y;
	}

	public float getWidth() {
		return defaultBitmap.getWidth();
	}

	public float getHeight() {
		return defaultBitmap.getHeight();
	}

	public RectF getMouthRect() {
		return mouthRect;
	}

	private void updateMouthRect() {
		float left = getxPos() + (getWidth() - mouthWidth) / 2;
		float top = getyPos() + (getHeight() - mouthHeight - mouthOffsetFromBottom);
		mouthRect.set(
				left,
				top,
				(left + mouthWidth),
				(top + mouthHeight)
		);
	}

	public float getMouthHeight() {
		return mouthHeight;
	}

	public float getMouthOffsetFromBottom() {
		return mouthOffsetFromBottom;
	}

	public Matrix getMatrix() {
		matrix.reset();
		matrix.postTranslate(location.x , location.y);
		return matrix;
	}

	public Bitmap getDefaultBitmap() {
		return defaultBitmap;
	}

	public Bitmap getLookingUpBitmap() {
		return lookingUpBitmap;
	}


	public static class Builder {

		private final Bitmap defaultBitmap, lookingUpBitmap;
		private final float mouthWidth, mouthHeight;
		private final float mouthOffsetFromBottom;

		private float xPos, yPos; // in pixel


		public Builder(Context context) {
			this.defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player_default);
			this.lookingUpBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player_looking_up);
			this.mouthWidth = context.getResources().getDimension(R.dimen.player_mouth_width);
			this.mouthHeight = context.getResources().getDimension(R.dimen.player_mouth_height);
			this.mouthOffsetFromBottom = context.getResources().getDimension(R.dimen.player_mouth_offset_from_bottom);
		}

		public Builder xPos(float xPos) {
			this.xPos = xPos;
			return this;
		}

		public Player build() {
			return new Player(defaultBitmap, lookingUpBitmap, new PointF(xPos, yPos), mouthWidth, mouthHeight, mouthOffsetFromBottom);
		}

	}
}
