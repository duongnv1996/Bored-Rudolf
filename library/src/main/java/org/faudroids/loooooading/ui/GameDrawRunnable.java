package org.faudroids.loooooading.ui;

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
import org.faudroids.loooooading.game.FallingObject;
import org.faudroids.loooooading.game.GameManager;
import org.faudroids.loooooading.game.GameState;
import org.faudroids.loooooading.game.Player;

import java.util.List;


class GameDrawRunnable implements Runnable {

	private static final String TAG = GameDrawRunnable.class.getName();

	private static final int MS_PER_FRAME = 1000 / 50;

	private final Paint
			PAINT = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);

	private final GameManager gameManager;
	private final SurfaceHolder surfaceHolder;
	private final TextView scoreView;
	private final Animation scoreAnimation;

	private Runnable postAction = null; // action to be run after this thread stops


	public GameDrawRunnable(Context context, GameManager gameManager, SurfaceHolder surfaceHolder, TextView scoreView) {
		this.gameManager = gameManager;
		this.surfaceHolder = surfaceHolder;
		this.scoreView = scoreView;
		this.scoreAnimation = AnimationUtils.loadAnimation(context, R.anim.score_zoom);
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
			if (canvas == null) return;
			canvas.drawColor(0, PorterDuff.Mode.CLEAR);

			// draw player
			Player player = gameManager.getPlayer();
			canvas.drawBitmap(player.getBitmap(), player.getMatrix(), PAINT);

			// draw snowflakes + presents
			drawObjects(gameManager.getSnowflakes(), canvas);
			drawObjects(gameManager.getPresents(), canvas);

			// draw superman clouds
			if (!gameManager.getState().equals(GameState.RUNNING)) {
				canvas.drawBitmap(gameManager.getSupermanClouds().getBitmap(), gameManager.getSupermanClouds().getMatrix(), PAINT);
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

	private void drawObjects(List<FallingObject> objects, Canvas canvas) {
		for (FallingObject object : objects) {
			PAINT.setAlpha((int) (object.getAlpha() * 255));
			canvas.drawBitmap(object.getBitmap(), object.getMatrix(), PAINT);
		}
		PAINT.setAlpha(255);
	}

}
