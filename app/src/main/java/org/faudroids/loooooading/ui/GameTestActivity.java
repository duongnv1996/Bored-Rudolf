package org.faudroids.loooooading.ui;

import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import org.faudroids.loooooading.R;
import org.faudroids.loooooading.game.GameManager;
import org.roboguice.shaded.goole.common.base.Optional;

import javax.inject.Inject;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_game_test)
public class GameTestActivity extends RoboActionBarActivity implements SurfaceHolder.Callback {

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


	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (drawRunnable.isPresent()) drawRunnable.get().stop();
	}


	private void startSnowflakes() {
		if (surfaceHolder.isPresent()) {
			drawRunnable = Optional.of(new DrawSnowflakesRunnable(this, gameManager, surfaceHolder.get()));
			new Thread(drawRunnable.get()).start();
		}
	}


	private void stopSnowflakes() {
		if (drawRunnable.isPresent()) {
			drawRunnable.get().stop();
			drawRunnable = Optional.absent();
		}

	}


}
