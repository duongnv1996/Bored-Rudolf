package org.faudroids.loooooading.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import org.faudroids.loooooading.R;
import org.faudroids.loooooading.game.GameManager;
import org.faudroids.loooooading.game.Player;
import org.faudroids.loooooading.game.Snowflake;
import org.roboguice.shaded.goole.common.base.Optional;

import javax.inject.Inject;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import timber.log.Timber;

@ContentView(R.layout.activity_game_test)
public class GameTestActivity extends RoboActionBarActivity implements SurfaceHolder.Callback {

	private static final boolean DEBUG = true;


	@InjectView(R.id.surface_view) private SurfaceView surfaceView;
    @InjectView(R.id.xCoord) private TextView xCoordTV;
    @InjectView(R.id.yCoord) private TextView yCoordTV;

	@Inject private GameManager gameManager;

	private Optional<DrawSnowflakesRunnable> drawRunnable = Optional.absent();
	private Optional<SurfaceHolder> surfaceHolder = Optional.absent();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
		surfaceView.setBackgroundColor(Color.TRANSPARENT);
		surfaceView.setZOrderOnTop(true);

	}


	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		surfaceHolder = Optional.of(holder);
		startSnowflakes();

        surfaceView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				//return super.onTouchEvent(event);
				int action = event.getAction();

				switch (action) {
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_MOVE:
					case MotionEvent.ACTION_DOWN:

						xCoordTV.setText("x: " + event.getX());
						yCoordTV.setText("y: " + event.getY());
						gameManager.onPlayerTouch(event.getX());
						break;

					default:
						break;
				}

				return true;

			}
		});
	}


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// nothing to do for now
	}


	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceHolder = Optional.absent();
		stopSnowflakes();
	}


	private void startSnowflakes() {
		if (surfaceHolder.isPresent()) {
			drawRunnable = Optional.of(new DrawSnowflakesRunnable(surfaceHolder.get()));
			new Thread(drawRunnable.get()).start();
		}
	}


	private void stopSnowflakes() {
		if (drawRunnable.isPresent()) {
			drawRunnable.get().stop();
			drawRunnable = Optional.absent();
		}

	}

	private class DrawSnowflakesRunnable implements Runnable {

		private final int msPerFrame = 1000 / 50; // = 40 FPS
		private final Paint PAINT = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);

		private final Paint DEBUG_PAINT = new Paint();
		private final Paint DEBUG_PAINT_STORKE = new Paint();
		{
			DEBUG_PAINT.setColor(getResources().getColor(android.R.color.holo_red_light));
			DEBUG_PAINT_STORKE.setColor(getResources().getColor(android.R.color.holo_red_light));
			DEBUG_PAINT_STORKE.setStrokeWidth(3);
			DEBUG_PAINT_STORKE.setStyle(Paint.Style.STROKE);
		}

		private final SurfaceHolder surfaceHolder;
		private volatile boolean isRunning = true;


		public DrawSnowflakesRunnable(SurfaceHolder surfaceHolder) {
			this.surfaceHolder = surfaceHolder;
		}

		@Override
		public void run() {
			Canvas tmpCanvas = surfaceHolder.lockCanvas();
			gameManager.start(tmpCanvas.getWidth(), tmpCanvas.getHeight());
			surfaceHolder.unlockCanvasAndPost(tmpCanvas);

			while (isRunning) {
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
				}


				// draw snowflakes
				for (Snowflake snowflake : gameManager.getSnowflakes()) {
					canvas.drawBitmap(snowflake.getBitmap(), snowflake.getMatrix(), PAINT);
				}

				// draw debug
				if (DEBUG) {
					canvas.drawRect(player.getMouthRect(), DEBUG_PAINT_STORKE);
					canvas.drawCircle(player.getxPos(), player.getyPos(), 3, DEBUG_PAINT);
					for (Snowflake snowflake : gameManager.getSnowflakes()) {
						canvas.drawRect(snowflake.getBoundingBox(), DEBUG_PAINT_STORKE);
						canvas.drawCircle(snowflake.getCenter().x, snowflake.getCenter().y, 3, DEBUG_PAINT);
					}
				}

				surfaceHolder.unlockCanvasAndPost(canvas);

				// update game
				long timeDiff = gameManager.loop();

				try {

					long sleepTime = Math.max(0, msPerFrame + (msPerFrame - timeDiff));
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


}
