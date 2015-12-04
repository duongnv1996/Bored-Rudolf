package org.faudroids.loooooading.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;

import org.faudroids.loooooading.R;

/**
 * A snowflake!
 */
public class Player {

	// used for getting the final orientation
	private final Matrix matrix = new Matrix();
	private final Bitmap defaultBitmap, lookingUpBitmap;

	private final PointF location;

	private Player(Bitmap defaultBitmap, Bitmap lookingUpBitmap, PointF location) {
		this.defaultBitmap = defaultBitmap;
		this.lookingUpBitmap = lookingUpBitmap;
		this.location = location;
	}

	public void setxPos(float xPos) {
		this.location.x = xPos;
	}

	public void setyPos(float yPos) {
		this.location.y = yPos;
	}

	public Matrix getMatrix() {
		matrix.reset();
		matrix.postTranslate(location.x , location.y);
		return matrix;
	}


	// for testing only!!!
	private int drawCount = 0;
	private boolean showingRegular = true;

	public Bitmap getDefaultBitmap() {
		++drawCount;
		if (drawCount > 100) {
			showingRegular = !showingRegular;
			drawCount = 0;
		}
		if (showingRegular) return defaultBitmap;
		else return lookingUpBitmap;
	}


	public Bitmap getLookingUpBitmap() {
		return lookingUpBitmap;
	}


	public static class Builder {

		private final Bitmap defaultBitmap, lookingUpBitmap;

		private float xPos, yPos; // in pixel


		public Builder(Context context) {
			defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player_default);
			lookingUpBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player_looking_up);
		}

		public Builder xPos(float xPos) {
			this.xPos = xPos;
			return this;
		}

		public Player build() {
			return new Player(defaultBitmap, lookingUpBitmap, new PointF(xPos, yPos));
		}

	}
}
