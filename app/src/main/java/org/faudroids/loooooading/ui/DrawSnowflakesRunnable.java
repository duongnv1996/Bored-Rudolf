package org.faudroids.loooooading.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.view.SurfaceHolder;

import org.faudroids.loooooading.game.GameManager;
import org.faudroids.loooooading.game.Player;
import org.faudroids.loooooading.game.Snowflake;

import timber.log.Timber;


class DrawSnowflakesRunnable implements Runnable {

	private static final boolean DEBUG = false;

	private static final int MS_PER_FRAME = 1000 / 50; // = 40 FPS

	private static final int CHEWING_ANIM_FRAME_LENGTH_IN_MS = 150;

	private final Paint
			PAINT = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG),
			DEBUG_PAINT = new Paint(),
			DEBUG_PAINT_STROKE = new Paint();

	private final GameManager gameManager;
	private final SurfaceHolder surfaceHolder;

	private volatile boolean isRunning = true;


	public DrawSnowflakesRunnable(Context context, GameManager gameManager, SurfaceHolder surfaceHolder) {
		this.gameManager = gameManager;
		this.surfaceHolder = surfaceHolder;

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

		while (isRunning) {
			// update game
			long timeDiff = gameManager.loop();

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
			}

			// draw snowflakes
			for (Snowflake snowflake : gameManager.getSnowflakes()) {
				canvas.drawBitmap(snowflake.getBitmap(), snowflake.getMatrix(), PAINT);
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
				long sleepTime = Math.max(0, MS_PER_FRAME + (MS_PER_FRAME - timeDiff));
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				Timber.e(e, "interrupted while sleeping in draw");
			}
		}
	}

	public void stop() {
		isRunning = false;
	}

}
