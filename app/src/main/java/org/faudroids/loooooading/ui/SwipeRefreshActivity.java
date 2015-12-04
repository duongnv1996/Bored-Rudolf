package org.faudroids.loooooading.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.faudroids.loooooading.R;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_swipe_refresh)
public class SwipeRefreshActivity extends RoboActionBarActivity implements CustomSwipeRefreshLayout.OnRefreshListener {

    @InjectView(R.id.layout_swipe) private CustomSwipeRefreshLayout refreshLayout;
	@InjectView(R.id.list_view) private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        refreshLayout.setOnRefreshListener(this);
		ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		listView.setAdapter(listAdapter);
		listAdapter.addAll("pull", "down", "to", "refresh", "this", "layout", "!", "la", "la", "la", "la", "la", "...");
    }

    @Override
    public void onRefresh() {
		refreshLayout.postDelayed(new Runnable() {
			@Override
			public void run() {
				refreshLayout.refreshComplete();
			}
		}, 2000);
    }
}
