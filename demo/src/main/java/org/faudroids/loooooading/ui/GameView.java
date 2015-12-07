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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.faudroids.loooooading.R;
import org.faudroids.loooooading.game.GameManager;


public class GameView extends LinearLayout implements
		CustomSwipeRefreshLayout.CustomSwipeRefreshHeadLayout,
		SurfaceHolder.Callback {

	private GameManager gameManager;
	private SurfaceView surfaceView;
	private TextView scoreView, highScoreView;
	private View whiteView; // clouds overlay when game over
	private ImageView downArrowView;
	private Animation arrowFadeOutAnim;

	private DrawGameRunnable drawRunnable = null;
	private SurfaceHolder surfaceHolder = null;

	public GameView(Context context) {
        super(context);
		addView(
				LayoutInflater.from(getContext()).inflate(R.layout.game_view, null),
				new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

		this.gameManager = new GameManager(getContext());
		this.surfaceView = (SurfaceView) findViewById(R.id.surface_view);
		this.scoreView = (TextView) findViewById(R.id.txt_score);
		this.highScoreView = (TextView) findViewById(R.id.txt_high_score);
		this.whiteView = findViewById(R.id.view_white);
		this.downArrowView = (ImageView) findViewById(R.id.img_arrow_down);
		this.arrowFadeOutAnim = AnimationUtils.loadAnimation(context, R.anim.arrow_fade_out);

		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
		surfaceView.setBackgroundColor(Color.TRANSPARENT);
		surfaceView.setZOrderOnTop(true);
    }


	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		surfaceHolder = holder;
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
		surfaceHolder = null;
		stopSnowflakes();
	}


	private void startSnowflakes() {
		if (surfaceHolder != null) {
			drawRunnable = new DrawGameRunnable(getContext(), gameManager, surfaceHolder, scoreView);
			new Thread(drawRunnable).start();
		}
	}


	private void stopSnowflakes() {
		if (drawRunnable != null) {
			drawRunnable.stop(new Runnable() {
				@Override
				public void run() {
					// post action
					surfaceView.post(new Runnable() {
						@Override
						public void run() {
							// ui thread
							whiteView.setVisibility(View.VISIBLE);
							surfaceView.post(new Runnable() {
								@Override
								public void run() {
									// avoid flickering
									surfaceView.setVisibility(View.INVISIBLE);
								}
							});
						}
					});
				}
			});
			drawRunnable = null;
		}

	}


    @Override
    public void onStateChange(CustomSwipeRefreshLayout.State state, CustomSwipeRefreshLayout.State lastState) {
		float alpha = Math.min(state.getPercent() + 0.1f, 1f);
		downArrowView.setAlpha(alpha);
		float scale = Math.min(0.5f + state.getPercent() / 2f, 1f);
		downArrowView.setScaleX(scale);
		downArrowView.setScaleY(scale);

        int stateCode = state.getRefreshState();
        int lastStateCode = lastState.getRefreshState();
        if (stateCode == lastStateCode) {
            return;
        }

        switch (stateCode) {
            case CustomSwipeRefreshLayout.State.STATE_NORMAL:
				whiteView.setVisibility(View.GONE);
				downArrowView.setVisibility(View.VISIBLE);
				setHighScore();
				scoreView.setText(String.valueOf(0));
                break;

            case CustomSwipeRefreshLayout.State.STATE_REFRESHING:
				surfaceView.setVisibility(View.VISIBLE);

				arrowFadeOutAnim.reset();
				downArrowView.clearAnimation();
				downArrowView.startAnimation(arrowFadeOutAnim);
				arrowFadeOutAnim.setAnimationListener(new Animation.AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) { }

					@Override
					public void onAnimationEnd(Animation animation) {
						downArrowView.setVisibility(View.GONE);
						startSnowflakes();
					}

					@Override
					public void onAnimationRepeat(Animation animation) { }
				});
                break;

            case CustomSwipeRefreshLayout.State.STATE_COMPLETE:
				stopSnowflakes();
                break;
        }
    }


	private void setHighScore() {
		int highScore = gameManager.getScore().getNumbericHighScore();
		if (highScore == 0) {
			highScoreView.setVisibility(View.GONE);
		} else {
			highScoreView.setVisibility(View.VISIBLE);
			highScoreView.setText(getContext().getString(R.string.high_score_value, highScore));
		}
	}

}
