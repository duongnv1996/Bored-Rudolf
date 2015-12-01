package org.faudroids.loooooading.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.faudroids.loooooading.R;

/**
 * Created by dex on 12/1/15.
 */
public class MySwipeRefreshHeadView extends LinearLayout implements CustomSwipeRefreshLayout.CustomSwipeRefreshHeadLayout {

    private LinearLayout mContainer;

    public MySwipeRefreshHeadView(Context context) {
        super(context);
        setWillNotDraw(false);
        setupLayout();
    }

    public void setupLayout() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.my_swiperefresh_head_layout, null);
        addView(mContainer, lp);
        setGravity(Gravity.BOTTOM);
    }


    // left as a scaffold
    @Override
    public void onStateChange(CustomSwipeRefreshLayout.State state, CustomSwipeRefreshLayout.State lastState) {
        int stateCode = state.getRefreshState();
        int lastStateCode = lastState.getRefreshState();
        if (stateCode == lastStateCode) {
            return;
        }
        if (stateCode == CustomSwipeRefreshLayout.State.STATE_COMPLETE) {
        } else if (stateCode == CustomSwipeRefreshLayout.State.STATE_REFRESHING) {
        } else {
        }

        switch (stateCode) {
            case CustomSwipeRefreshLayout.State.STATE_NORMAL:
                if (lastStateCode == CustomSwipeRefreshLayout.State.STATE_READY) {
                }
                if (lastStateCode == CustomSwipeRefreshLayout.State.STATE_REFRESHING) {
                }
                break;
            case CustomSwipeRefreshLayout.State.STATE_READY:
                if (lastStateCode != CustomSwipeRefreshLayout.State.STATE_READY) {
                }
                break;
            case CustomSwipeRefreshLayout.State.STATE_REFRESHING:
                break;

            case CustomSwipeRefreshLayout.State.STATE_COMPLETE:
                break;
            default:
        }
    }

}
