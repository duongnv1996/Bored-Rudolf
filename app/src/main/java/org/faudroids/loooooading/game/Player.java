package org.faudroids.loooooading.game;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * A snowflake!
 */
public class Player {

	// used for getting the final orientation
	private final Matrix matrix = new Matrix();
	private final Bitmap bitmap;

	private final Location location;

	private Player(Bitmap bitmap, Location location) {
		this.bitmap = bitmap;
		this.location = location;
	}

	public void setxPos(float xPos) {
		this.location.xPos = xPos;
	}

	public void setyPos(float yPos) {
		this.location.yPos = yPos;
	}

	public Matrix getMatrix() {
		matrix.reset();
		matrix.postTranslate(location.xPos, location.yPos);
		return matrix;
	}


	public Bitmap getBitmap() {
		return bitmap;
	}


	public static class Builder {

		private final Bitmap bitmap;

		private float xPos, yPos; // in pixel


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

		public Player build() {
			return new Player(bitmap, new Location(xPos, yPos));
		}

	}
}
