package org.faudroids.loooooading.game;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;

/**
 * A snowflake!
 */
public class Snowflake {

	// used for getting the final orientation
	private final Matrix matrix = new Matrix();
	private final Bitmap bitmap;

	private final PointF location;
	private final float fallSpeed; // pixel / seconds
	private final float scale;
	private final float rotation; // degrees

	private Snowflake(Bitmap bitmap, PointF location, float fallSpeed, float scale, float rotation) {
		this.bitmap = bitmap;
		this.location = location;
		this.scale = scale;
		this.fallSpeed = fallSpeed;
		this.rotation = rotation;
	}


	public void onTimePassed(long timeInMs) {
		location.y += fallSpeed / 1000f * timeInMs;
	}


	public float getyPos() {
		return location.y;
	}


	public Bitmap getBitmap() {
		return bitmap;
	}


	public Matrix getMatrix() {
		matrix.reset();
		matrix.postScale(scale, scale, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
		matrix.postRotate(rotation);
		matrix.postTranslate(location.x, location.y);
		return matrix;
	}


	public static class Builder {

		private final Bitmap bitmap;

		private float xPos, yPos; // in pixel
		private float fallSpeed; // pixel / seconds
		private float scale;
		private float rotation;


		public Builder(Bitmap bitmap) {
			this.bitmap = bitmap;
		}

		public Builder xPos(float xPos) {
			this.xPos = xPos;
			return this;
		}

		public Builder yPos(float yPos) {
			this.yPos = yPos;
			return this;
		}

		public Builder fallSpeed(float fallSpeed) {
			this.fallSpeed = fallSpeed;
			return this;
		}

		public Builder scale(float scale) {
			this.scale = scale;
			return this;
		}

		public Builder rotation(float rotation) {
			this.rotation = rotation;
			return this;
		}

		public Snowflake build() {
			return new Snowflake(bitmap, new PointF(xPos, yPos), fallSpeed, scale, rotation);
		}

	}
}
