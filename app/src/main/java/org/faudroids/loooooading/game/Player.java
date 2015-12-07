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
	private final Bitmap defaultBitmap, lookingUpBitmap, chewing0Bitmap, chewing1Bitmap, supermanBitmap;

	private final PointF location;

	private final RectF mouthRect = new RectF();
	private final float mouthWidth, mouthHeight;
	private final float mouthOffsetFromBottom;

	private long chewingStartTimestamp; // if player is eating, this will indicate when he / she started eating

	private PlayerState state;

	private Player(Bitmap defaultBitmap, Bitmap lookingUpBitmap, Bitmap chewing0Bitmap, Bitmap chewing1Bitmap, Bitmap supermanBitmap,
				   PointF location, float mouthWidth, float mouthHeight, float mouthOffsetFromBottom) {

		this.defaultBitmap = defaultBitmap;
		this.lookingUpBitmap = lookingUpBitmap;
		this.chewing0Bitmap = chewing0Bitmap;
		this.chewing1Bitmap = chewing1Bitmap;
		this.supermanBitmap = supermanBitmap;
		this.location = location;
		this.mouthWidth = mouthWidth;
		this.mouthHeight = mouthHeight;
		this.mouthOffsetFromBottom = mouthOffsetFromBottom;
		updateMouthRect();
		this.state = PlayerState.DEFAULT;
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

	public Bitmap getChewing0Bitmap() {
		return chewing0Bitmap;
	}

	public Bitmap getChewing1Bitmap() {
		return chewing1Bitmap;
	}

	public Bitmap getSupermanBitmap() {
		return supermanBitmap;
	}

	/**
	 * @return how long the player has been eating in ms
	 */
	public long getChewingDuration() {
		return System.currentTimeMillis() - chewingStartTimestamp;
	}

	public void startChewingTimer() {
		this.chewingStartTimestamp = System.currentTimeMillis();
	}

	public boolean canEatSnowflake() {
		return !state.equals(PlayerState.CHEWING);
	}

	public PlayerState getState() {
		return state;
	}

	public void setState(PlayerState state) {
		this.state = state;
	}

	public static class Builder {

		private final Context context;

		private float xPos; // in pixel


		public Builder(Context context) {
			this.context = context;
		}

		public Builder xPos(float xPos) {
			this.xPos = xPos;
			return this;
		}

		public Player build() {
			return new Player(
					BitmapFactory.decodeResource(context.getResources(), R.drawable.player_default),
					BitmapFactory.decodeResource(context.getResources(), R.drawable.player_looking_up),
					BitmapFactory.decodeResource(context.getResources(), R.drawable.player_chewing_0),
					BitmapFactory.decodeResource(context.getResources(), R.drawable.player_chewing_1),
					BitmapFactory.decodeResource(context.getResources(), R.drawable.player_superman),
					new PointF(xPos, 0),
					context.getResources().getDimension(R.dimen.player_mouth_width),
					context.getResources().getDimension(R.dimen.player_mouth_height),
					context.getResources().getDimension(R.dimen.player_mouth_offset_from_bottom));
		}

	}
}
