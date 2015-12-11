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

	private static final int CHEWING_ANIM_FRAME_LENGTH_IN_MS = 150;

	// used for getting the final orientation
	private final Matrix matrix = new Matrix();
	private final Bitmap defaultBitmap, lookingUpBitmap, blastedBitmap, supermanBitmap;
	private final Bitmap[] chewingBitmaps;

	private final PointF location;

	private final RectF playerRect = new RectF(), mouthRect = new RectF();
	private final float mouthWidth, mouthHeight;
	private final float mouthOffsetFromBottom;

	private long chewingStartTimestamp; // if player is eating this will indicates the eating start timestamp
	private long blastedStartTimestamp; // if player has been blasted this indicates the start timestamp

	private PlayerState state;

	private Player(Bitmap defaultBitmap, Bitmap lookingUpBitmap, Bitmap[] chewingBitmaps, Bitmap blastedBitmap, Bitmap supermanBitmap,
				   PointF location, float mouthWidth, float mouthHeight, float mouthOffsetFromBottom) {

		this.defaultBitmap = defaultBitmap;
		this.lookingUpBitmap = lookingUpBitmap;
		this.chewingBitmaps = chewingBitmaps;
		this.blastedBitmap = blastedBitmap;
		this.supermanBitmap = supermanBitmap;
		this.location = location;
		this.mouthWidth = mouthWidth;
		this.mouthHeight = mouthHeight;
		this.mouthOffsetFromBottom = mouthOffsetFromBottom;
		updateRects();
		this.state = PlayerState.DEFAULT;
	}

	public void setxPos(float xPos) {
		this.location.x = xPos;
		updateRects();
	}

	public void setyPos(float yPos) {
		this.location.y = yPos;
		updateRects();
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

	public boolean doesPlayerContainPoint(PointF point) {
		return playerRect.contains(point.x, point.y);
	}

	public boolean doesMouthContainPoint(PointF point) {
		return mouthRect.contains(point.x, point.y);
	}

	public boolean isPlayerBelowPoint(PointF point) {
		return mouthRect.left <= point.x
				&& mouthRect.right > point.x
				&& mouthRect.bottom >= point.y;
	}

	private void updateRects() {
		// set player rect
		playerRect.set(
				getxPos(),
				getyPos() + getHeight() * 0.1f,
				getxPos() + getWidth(),
				getyPos() + getHeight()
		);

		// set mouth rect
		float left = getxPos() + (getWidth() - mouthWidth) / 2;
		float top = getyPos() + (getHeight() - mouthHeight - mouthOffsetFromBottom);
		mouthRect.set(
				left,
				top,
				(left + mouthWidth),
				(top + mouthHeight)
		);
	}

	public Matrix getMatrix() {
		matrix.reset();
		matrix.postTranslate(location.x, location.y);
		return matrix;
	}

	public Bitmap getBitmap() {
		switch (state) {
			case DEFAULT:
				return defaultBitmap;

			case LOOKING_UP:
				return lookingUpBitmap;

			case CHEWING:
				return chewingBitmaps[(int) ((getChewingDuration() / CHEWING_ANIM_FRAME_LENGTH_IN_MS) % 2)];

			case BLASTED:
				return blastedBitmap;

			case SUPERMAN:
				return supermanBitmap;
		}
		throw new IllegalStateException("unkown state " + state.name());
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

	public long getBlastedDuration() {
		return System.currentTimeMillis() - blastedStartTimestamp;
	}

	public void startBlastedTimer() {
		this.blastedStartTimestamp = System.currentTimeMillis();
	}

	public boolean canEatSnowflake() {
		return state.equals(PlayerState.DEFAULT) || state.equals(PlayerState.LOOKING_UP);
	}

	public PlayerState getState() {
		return state;
	}

	public boolean isInState(PlayerState state) {
		return this.state.equals(state);
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
					new Bitmap[] {
							BitmapFactory.decodeResource(context.getResources(), R.drawable.player_chewing_0),
							BitmapFactory.decodeResource(context.getResources(), R.drawable.player_chewing_1)
					},
					BitmapFactory.decodeResource(context.getResources(), R.drawable.player_blasted),
					BitmapFactory.decodeResource(context.getResources(), R.drawable.player_superman),
					new PointF(xPos, 0),
					context.getResources().getDimension(R.dimen.player_mouth_width),
					context.getResources().getDimension(R.dimen.player_mouth_height),
					context.getResources().getDimension(R.dimen.player_mouth_offset_from_bottom));
		}

	}
}
