package org.faudroids.loooooading;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import org.roboguice.shaded.goole.common.base.Optional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

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
		private final List<Snowflake> visibleSnowflakes = new ArrayList<>();
		private final LinkedList<Snowflake> hiddenSnowflakes = new LinkedList<>();
		private final Random random = new Random();

		private volatile boolean isRunning = true;
		private int nextSnowflakeCountdown = 0;


		public DrawSnowflakesRunnable(SurfaceHolder surfaceHolder) {
			this.surfaceHolder = surfaceHolder;
			this.snowflakeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.snowflake);
			for (int i = 0; i < 30; ++i) {
				hiddenSnowflakes.add(new Snowflake());
			}
		}

		@Override
		public void run() {
			long lastRunTimestamp = System.currentTimeMillis();
			while (isRunning) {
				Canvas canvas = surfaceHolder.lockCanvas();
				canvas.drawColor(0, PorterDuff.Mode.CLEAR);

				if (nextSnowflakeCountdown <= 0 && !hiddenSnowflakes.isEmpty()) {
					Snowflake snowflake = hiddenSnowflakes.removeFirst();
					snowflake.reset(canvas.getWidth());
					visibleSnowflakes.add(snowflake);
					nextSnowflakeCountdown = randomInt(20, 50);
				} else {
					--nextSnowflakeCountdown;
				}

				Iterator<Snowflake> iterator = visibleSnowflakes.iterator();
				while (iterator.hasNext()) {
					Snowflake snowflake = iterator.next();
					canvas.drawBitmap(snowflakeBitmap, snowflake.getMatrix(), PAINT);
					snowflake.y = snowflake.y + snowflake.fallSpeed;
					if (snowflake.y > canvas.getHeight()) {
						iterator.remove();
						hiddenSnowflakes.addLast(snowflake);
					}
				}
				surfaceHolder.unlockCanvasAndPost(canvas);

				try {
					long currentTimestamp = System.currentTimeMillis();
					long timeDiff = currentTimestamp - lastRunTimestamp;
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


		public int randomInt(int min, int max) {
			return random.nextInt((max - min) + 1) + min;
		}


		private class Snowflake {

			private final Matrix matrix = new Matrix();
			private int x, y;
			private float scale;
			private int fallSpeed;
			private float rotation;

			public void reset(int canvasWidth) {
				scale = randomInt(400, 1000) / 1000f;
				x = randomInt(-snowflakeBitmap.getWidth(), canvasWidth);
				y = -snowflakeBitmap.getHeight();
				fallSpeed = randomInt(2, 5);
				rotation = randomInt(0, 90);
			}

			public Matrix getMatrix() {
				matrix.reset();
				matrix.postScale(scale, scale, snowflakeBitmap.getWidth() / 2, snowflakeBitmap.getHeight() / 2);
				matrix.postRotate(rotation);
				matrix.postTranslate(x, y);
				return matrix;
			}

		}

	}


}
