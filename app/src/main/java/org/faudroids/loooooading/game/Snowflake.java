package org.faudroids.loooooading.game;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * A snowflake!
 */
public class Snowflake {

	// used for getting the final orientation
	private final Matrix matrix = new Matrix();
	private final Bitmap bitmap;

	private float xPos, yPos; // in pixel
	private final float fallSpeed; // pixel / seconds
	private final float scale;
	private final float rotation; // degrees

	private Snowflake(Bitmap bitmap, float xPos, float yPos, float fallSpeed, float scale, float rotation) {
		this.bitmap = bitmap;
		this.xPos = xPos;
		this.yPos = yPos;
		this.scale = scale;
		this.fallSpeed = fallSpeed;
		this.rotation = rotation;
	}


	public void onTimePassed(long timeInMs) {
		yPos += fallSpeed / 1000f * timeInMs;
	}


	public float getyPos() {
		return yPos;
	}


	public Matrix getMatrix() {
		matrix.reset();
		matrix.postScale(scale, scale, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
		matrix.postRotate(rotation);
		matrix.postTranslate(xPos, yPos);
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
			return new Snowflake(bitmap, xPos, yPos, fallSpeed, scale, rotation);
		}

	}
}
