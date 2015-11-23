package org.faudroids.loooooading;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import org.faudroids.loooooading.game.Player;
import org.faudroids.loooooading.game.Snowflake;
import org.faudroids.loooooading.utils.RandomUtils;
import org.roboguice.shaded.goole.common.base.Optional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import timber.log.Timber;

@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActionBarActivity implements SurfaceHolder.Callback {


	@InjectView(R.id.surface_view) private SurfaceView surfaceView;
    @InjectView(R.id.xCoord) private TextView xCoordTV;
    @InjectView(R.id.yCoord) private TextView yCoordTV;

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
	public boolean onTouchEvent(MotionEvent event) {
		//return super.onTouchEvent(event);
		int action = event.getAction();
		switch(action){
			case MotionEvent.ACTION_DOWN:
                xCoordTV.setText("x: " + event.getX());
                yCoordTV.setText("y: " + event.getY());
				break;
			default:
		}

		return true;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		surfaceHolder = Optional.of(holder);
		startSnowflakes();
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

		private final SurfaceHolder surfaceHolder;
		private final Bitmap snowflakeBitmap;

		private final Player player;
		private final List<Snowflake> snowflakes = new ArrayList<>();

		private volatile boolean isRunning = true;
		private int nextSnowflakeCountdown = 0;


		public DrawSnowflakesRunnable(SurfaceHolder surfaceHolder) {
			this.surfaceHolder = surfaceHolder;
			this.snowflakeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.snowflake);
			Bitmap playerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.player);
			this.player = new Player.Builder(playerBitmap).xPos(100).yPos(playerBitmap.getHeight()).build();
		}

		@Override
		public void run() {
			long lastRunTimestamp = System.currentTimeMillis();
			while (isRunning) {
				final Canvas canvas = surfaceHolder.lockCanvas();
				canvas.drawColor(0, PorterDuff.Mode.CLEAR);

				final long currentTimestamp = System.currentTimeMillis();
				final long timeDiff = currentTimestamp - lastRunTimestamp;

				if (nextSnowflakeCountdown <= 0 && snowflakes.size() < 30) {
					Snowflake snowflake = new Snowflake.Builder(snowflakeBitmap)
							.xPos(RandomUtils.randomInt(-snowflakeBitmap.getWidth(), canvas.getWidth()))
							.yPos(-snowflakeBitmap.getHeight())
							.fallSpeed(RandomUtils.randomInt(50, 100))
							.scale(RandomUtils.randomInt(400, 1000) / 1000f)
							.rotation(RandomUtils.randomInt(0, 90))
							.build();

					snowflakes.add(snowflake);
					nextSnowflakeCountdown = RandomUtils.randomInt(20, 50);
				} else {
					--nextSnowflakeCountdown;
				}

				// draw snowflakes
				Iterator<Snowflake> iterator = snowflakes.iterator();
				while (iterator.hasNext()) {
					Snowflake snowflake = iterator.next();
					canvas.drawBitmap(snowflakeBitmap, snowflake.getMatrix(), PAINT);
					snowflake.onTimePassed(timeDiff);
					if (snowflake.getyPos() > canvas.getHeight()) {
						iterator.remove();
					}
				}

				// draw player
				canvas.drawBitmap(player.getBitmap(), player.getMatrix(), PAINT);

				surfaceHolder.unlockCanvasAndPost(canvas);

				try {
					lastRunTimestamp = currentTimestamp;
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
