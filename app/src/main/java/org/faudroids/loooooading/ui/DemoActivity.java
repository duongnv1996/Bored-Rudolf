package org.faudroids.loooooading.ui;

import android.os.Bundle;

import org.faudroids.loooooading.R;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_demo)
public class DemoActivity extends RoboActionBarActivity implements CustomSwipeRefreshLayout.OnRefreshListener {

    @InjectView(R.id.layout_swipe) private CustomSwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        refreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
		refreshLayout.postDelayed(new Runnable() {
			@Override
			public void run() {
				refreshLayout.refreshComplete();
			}
		}, 15000);
    }
}
