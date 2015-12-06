package org.faudroids.loooooading.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.faudroids.loooooading.R;
import org.faudroids.loooooading.game.GameManager;
import org.roboguice.shaded.goole.common.base.Optional;

import timber.log.Timber;


public class GameView extends LinearLayout implements
		CustomSwipeRefreshLayout.CustomSwipeRefreshHeadLayout,
		SurfaceHolder.Callback {

	private GameManager gameManager;
	private SurfaceView surfaceView;

	private Optional<DrawGameRunnable> drawRunnable = Optional.absent();
	private Optional<SurfaceHolder> surfaceHolder = Optional.absent();

	public GameView(Context context) {
        super(context);
		addView(
				LayoutInflater.from(getContext()).inflate(R.layout.game_view, null),
				new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

		this.gameManager = new GameManager(getContext());

		this.surfaceView = (SurfaceView) findViewById(R.id.surface_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
		surfaceView.setBackgroundColor(Color.TRANSPARENT);
		surfaceView.setZOrderOnTop(true);
    }


	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		surfaceHolder = Optional.of(holder);
		surfaceView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_MOVE:
					case MotionEvent.ACTION_DOWN:
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
			drawRunnable = Optional.of(new DrawGameRunnable(getContext(), gameManager, surfaceHolder.get()));
			new Thread(drawRunnable.get()).start();
		}
	}


	private void stopSnowflakes() {
		if (drawRunnable.isPresent()) {
			drawRunnable.get().stop();
			drawRunnable = Optional.absent();
		}

	}


    @Override
    public void onStateChange(CustomSwipeRefreshLayout.State state, CustomSwipeRefreshLayout.State lastState) {
        int stateCode = state.getRefreshState();
        int lastStateCode = lastState.getRefreshState();
        if (stateCode == lastStateCode) {
            return;
        }

        switch (stateCode) {
            case CustomSwipeRefreshLayout.State.STATE_NORMAL:
				Timber.d("normal state");
                break;

            case CustomSwipeRefreshLayout.State.STATE_READY:
				Timber.d("ready state");
				break;

            case CustomSwipeRefreshLayout.State.STATE_REFRESHING:
				surfaceView.setVisibility(View.VISIBLE);
				startSnowflakes();
				Timber.d("refreshing state");
                break;

            case CustomSwipeRefreshLayout.State.STATE_COMPLETE:
				Timber.d("complete state");
				stopSnowflakes();
				surfaceView.setVisibility(View.INVISIBLE);
                break;

            default:
				Timber.d("default");
				break;
        }
    }

}
