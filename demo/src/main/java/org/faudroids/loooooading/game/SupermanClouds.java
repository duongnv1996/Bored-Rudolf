package org.faudroids.loooooading.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;

import org.faudroids.loooooading.R;

/**
 * Clouds from superman!
 */
public class SupermanClouds {

	private final Matrix matrix = new Matrix();
	private final Bitmap bitmap;

	private final PointF location;

	private SupermanClouds(Bitmap bitmap, PointF location) {
		this.bitmap = bitmap;
		this.location = location;
	}

	public void setyPos(float yPos) {
		this.location.y = yPos;
		matrix.reset();
		matrix.postTranslate(location.x, location.y);
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public Matrix getMatrix() {
		return matrix;
	}

	public int getHeight() {
		return bitmap.getHeight();
	}

	public static class Builder {

		private final Context context;

		public Builder(Context context) {
			this.context = context;
		}

		public SupermanClouds build() {
			return new SupermanClouds(
					BitmapFactory.decodeResource(context.getResources(), R.drawable.superman_clouds),
					new PointF());
		}

	}
}
