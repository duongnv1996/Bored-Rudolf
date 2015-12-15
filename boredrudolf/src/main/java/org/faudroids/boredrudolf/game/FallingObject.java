package org.faudroids.boredrudolf.game;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;

/**
 * Something that's falling from the sky ...
 */
public class FallingObject {

	// used for getting the final orientation
	private final Matrix matrix = new Matrix();
	private final Bitmap bitmap;

	private final PointF location, center;
	private final float size;
	private final float fallSpeed; // pixel / seconds
	private final float scale;
	private final float rotation; // degrees
	private final RectF boundingBox;

	private final float maxVerticalVelocity; // pixel / seconds
	private final float verticalVelocityAccelerationDiff; // pixel / seconds
	private float verticalVelocity; // pixel / seconds
	private boolean accelerateToRight;

	private float alpha;

	private FallingObject(Bitmap bitmap, PointF location, float size, float fallSpeed, float scale, float rotation,
						  float maxVerticalVelocity, float verticalVelocityAccelerationDiff, boolean accelerateToRight) {

		this.bitmap = bitmap;
		this.location = location;
		this.size = size;
		this.scale = scale;
		this.fallSpeed = fallSpeed;
		this.rotation = rotation;
		this.center = new PointF();
		updateCenter();
		this.boundingBox = new RectF();
		this.maxVerticalVelocity = maxVerticalVelocity;
		this.verticalVelocityAccelerationDiff = verticalVelocityAccelerationDiff;
		this.verticalVelocity = 0;
		this.accelerateToRight = accelerateToRight;
		this.alpha = 1;
	}

	public void onTimePassed(long timeInMs) {
		location.y += fallSpeed / 1000f * timeInMs;
		location.x += verticalVelocity / 1000 * timeInMs;

		if (accelerateToRight) {
			verticalVelocity += verticalVelocityAccelerationDiff / 1000 * timeInMs;
		} else {
			verticalVelocity -= verticalVelocityAccelerationDiff / 1000 * timeInMs;
		}
		if (verticalVelocity >= maxVerticalVelocity) {
			accelerateToRight = false;
		}
		if (verticalVelocity <= -maxVerticalVelocity) {
			accelerateToRight = true;
		}

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

	public float getAlpha() {
		return alpha;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
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

		private float maxVerticalVelocity = 100; // pixel / seconds
		private float verticalVelocityAccelerationDiff = 100; // pixel / seconds
		private boolean accelerateToRight = false;


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

		public Builder maxVerticalVelocity(float maxVerticalVelocity) {
			this.maxVerticalVelocity = maxVerticalVelocity;
			return this;
		}

		public Builder verticalVelocityAccelerationDiff(float verticalVelocityAccelerationDiff) {
			this.verticalVelocityAccelerationDiff = verticalVelocityAccelerationDiff;
			return this;
		}

		public Builder accelerateToRight(boolean accelerateToRight) {
			this.accelerateToRight = accelerateToRight;
			return this;
		}

		public FallingObject build() {
			return new FallingObject(bitmap, new PointF(xPos, yPos), size, fallSpeed, scale, rotation,
					maxVerticalVelocity, verticalVelocityAccelerationDiff, accelerateToRight);
		}

	}
}
