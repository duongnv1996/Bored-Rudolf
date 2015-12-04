package org.faudroids.loooooading.game;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;

/**
 * A snowflake!
 */
public class Snowflake {

	// used for getting the final orientation
	private final Matrix matrix = new Matrix();
	private final Bitmap bitmap;

	private final PointF location, center;
	private final float size;
	private final float fallSpeed; // pixel / seconds
	private final float scale;
	private final float rotation; // degrees
	private final RectF boundingBox;

	private Snowflake(Bitmap bitmap, PointF location, float size, float fallSpeed, float scale, float rotation) {
		this.bitmap = bitmap;
		this.location = location;
		this.size = size;
		this.scale = scale;
		this.fallSpeed = fallSpeed;
		this.rotation = rotation;
		this.center = new PointF();
		updateCenter();
		this.boundingBox = new RectF();
	}

	public void onTimePassed(long timeInMs) {
		location.y += fallSpeed / 1000f * timeInMs;
		updateCenter();
		updateBoundingBox();
	}

	public float getSize() {
		return size;
	}

	public float getxPos() {
		return location.x;
	}

	public float getyPos() {
		return location.y;
	}

	public PointF getCenter() {
		return center;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public Matrix getMatrix() {
		matrix.reset();
		matrix.postScale(scale, scale);
		matrix.postRotate(rotation, getSize() / 2, getSize() / 2);
		matrix.postTranslate(location.x, location.y);
		return matrix;
	}

	public RectF getBoundingBox() {
		return boundingBox;
	}

	private void updateCenter() {
		center.x = getxPos() + getSize() / 2;
		center.y = getyPos() + getSize() / 2;
	}

	private void updateBoundingBox() {
		boundingBox.set(
				getxPos(),
				getyPos(),
				getxPos() + size,
				getyPos() + size
		);
	}

	public static class Builder {

		private final Bitmap bitmap;
		private float size;

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
			this.size = bitmap.getHeight() * scale;
			return this;
		}

		public Builder rotation(float rotation) {
			this.rotation = rotation;
			return this;
		}

		public Snowflake build() {
			return new Snowflake(bitmap, new PointF(xPos, yPos), size, fallSpeed, scale, rotation);
		}

	}
}
