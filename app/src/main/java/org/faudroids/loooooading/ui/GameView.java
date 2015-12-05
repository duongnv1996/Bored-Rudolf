package org.faudroids.loooooading.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.faudroids.loooooading.R;

import timber.log.Timber;


public class GameView extends LinearLayout implements CustomSwipeRefreshLayout.CustomSwipeRefreshHeadLayout {

	public GameView(Context context) {
        super(context);
		addView(
				LayoutInflater.from(getContext()).inflate(R.layout.game_view, null),
				new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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
                if (lastStateCode == CustomSwipeRefreshLayout.State.STATE_READY) {
                }
                if (lastStateCode == CustomSwipeRefreshLayout.State.STATE_REFRESHING) {
                }
                break;

            case CustomSwipeRefreshLayout.State.STATE_READY:
				Timber.d("ready state");
				break;

            case CustomSwipeRefreshLayout.State.STATE_REFRESHING:
				Timber.d("refreshing state");
                break;

            case CustomSwipeRefreshLayout.State.STATE_COMPLETE:
				Timber.d("complete state");
                break;

            default:
				Timber.d("default");
				break;
        }
    }

}
