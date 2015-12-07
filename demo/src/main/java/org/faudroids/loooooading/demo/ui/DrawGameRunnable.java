package org.faudroids.loooooading.demo.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import org.faudroids.loooooading.R;
import org.faudroids.loooooading.game.GameManager;
import org.faudroids.loooooading.game.GameState;
import org.faudroids.loooooading.game.Player;
import org.faudroids.loooooading.game.Snowflake;


class DrawGameRunnable implements Runnable {

	private static final String TAG = DrawGameRunnable.class.getName();

	private static final boolean DEBUG = false;

	private static final int MS_PER_FRAME = 1000 / 50;

	private static final int CHEWING_ANIM_FRAME_LENGTH_IN_MS = 150;

	private final Paint
			PAINT = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG),
			DEBUG_PAINT = new Paint(),
			DEBUG_PAINT_STROKE = new Paint();

	private final GameManager gameManager;
	private final SurfaceHolder surfaceHolder;
	private final TextView scoreView;
	private final Animation scoreAnimation;

	private Runnable postAction = null; // action to be run after this thread stops


	public DrawGameRunnable(Context context, GameManager gameManager, SurfaceHolder surfaceHolder, TextView scoreView) {
		this.gameManager = gameManager;
		this.surfaceHolder = surfaceHolder;
		this.scoreView = scoreView;
		this.scoreAnimation = AnimationUtils.loadAnimation(context, R.anim.score_zoom);

		DEBUG_PAINT.setColor(context.getResources().getColor(android.R.color.holo_red_light));
		DEBUG_PAINT_STROKE.setColor(context.getResources().getColor(android.R.color.holo_red_light));
		DEBUG_PAINT_STROKE.setStrokeWidth(3);
		DEBUG_PAINT_STROKE.setStyle(Paint.Style.STROKE);
	}

	@Override
	public void run() {
		Canvas tmpCanvas = surfaceHolder.lockCanvas();
		gameManager.start(tmpCanvas.getWidth(), tmpCanvas.getHeight());
		surfaceHolder.unlockCanvasAndPost(tmpCanvas);
		int lastScore = gameManager.getScore().getNumericScore();

		while (!gameManager.getState().equals(GameState.STOPPED)) {
			// update game
			final long timeDiff = gameManager.loop();

			// update score
			final int score = gameManager.getScore().getNumericScore();
			if (lastScore != score) {
				lastScore = score;
				scoreView.post(new Runnable() {
					@Override
					public void run() {
						scoreView.setText(String.valueOf(score));
						scoreAnimation.reset();
						scoreView.clearAnimation();
						scoreView.startAnimation(scoreAnimation);
					}
				});
			}

			// start drawing + clear background
			final Canvas canvas = surfaceHolder.lockCanvas();
			canvas.drawColor(0, PorterDuff.Mode.CLEAR);

			// draw player
			Player player = gameManager.getPlayer();
			switch (player.getState()) {
				case DEFAULT:
					canvas.drawBitmap(player.getDefaultBitmap(), player.getMatrix(), PAINT);
					break;

				case LOOKING_UP:
					canvas.drawBitmap(player.getLookingUpBitmap(), player.getMatrix(), PAINT);
					break;

				case CHEWING:
					if ((player.getChewingDuration() / CHEWING_ANIM_FRAME_LENGTH_IN_MS) % 2 == 0) {
						canvas.drawBitmap(player.getChewing0Bitmap(), player.getMatrix(), PAINT);
					} else {
						canvas.drawBitmap(player.getChewing1Bitmap(), player.getMatrix(), PAINT);
					}
					break;

				case SUPERMAN:
					canvas.drawBitmap(player.getSupermanBitmap(), player.getMatrix(), PAINT);
					break;
			}

			// draw snowflakes
			for (Snowflake snowflake : gameManager.getSnowflakes()) {
				PAINT.setAlpha((int) (snowflake.getAlpha() * 255));
				canvas.drawBitmap(snowflake.getBitmap(), snowflake.getMatrix(), PAINT);
			}
			PAINT.setAlpha(255);

			// draw superman clouds
			if (!gameManager.getState().equals(GameState.RUNNING)) {
				canvas.drawBitmap(gameManager.getSupermanClouds().getBitmap(), gameManager.getSupermanClouds().getMatrix(), PAINT);
			}

			// draw debug
			if (DEBUG) {
				canvas.drawRect(player.getMouthRect(), DEBUG_PAINT_STROKE);
				canvas.drawCircle(player.getxPos(), player.getyPos(), 3, DEBUG_PAINT);
				for (Snowflake snowflake : gameManager.getSnowflakes()) {
					canvas.drawRect(snowflake.getBoundingBox(), DEBUG_PAINT_STROKE);
					canvas.drawCircle(snowflake.getCenter().x, snowflake.getCenter().y, 3, DEBUG_PAINT);
				}
			}

			surfaceHolder.unlockCanvasAndPost(canvas);

			try {
				long sleepTime = Math.max(0, MS_PER_FRAME - timeDiff);
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				Log.e(TAG, "interrupted while sleeping in draw", e);
			}
		}

		if (postAction != null) {
			postAction.run();
		}
	}

	public void stop(Runnable postAction) {
		this.postAction = postAction;
		gameManager.requestShutdown();
	}

}
