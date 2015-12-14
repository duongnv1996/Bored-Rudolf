package org.faudroids.boredrudolf.demo.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import org.faudroids.boredrudolf.ui.CustomSwipeRefreshLayout;
import org.faudroids.loooooading.R;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_demo)
public class DemoActivity extends RoboActionBarActivity implements CustomSwipeRefreshLayout.OnRefreshListener {

    @InjectView(R.id.layout_swipe) private CustomSwipeRefreshLayout refreshLayout;
	@InjectView(R.id.spinner_loading_time) private Spinner loadingTimesSpinner;
	@InjectView(R.id.btn_stop_loading) private Button stopLoadingButton;
	private int[] loadingTimeValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        refreshLayout.setOnRefreshListener(this);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.loading_time_strings, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		loadingTimesSpinner.setAdapter(adapter);
		loadingTimesSpinner.setSelection(4);

		loadingTimeValues = getResources().getIntArray(R.array.loading_time_values);

		stopLoadingButton.setEnabled(false);
		stopLoadingButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				stopRefreshing();
			}
		});
    }

    @Override
    public void onRefresh() {
		stopLoadingButton.setEnabled(true);
		int loadingTime= loadingTimeValues[loadingTimesSpinner.getSelectedItemPosition()] * 1000;
		if (loadingTime <= 0) return;
		refreshLayout.postDelayed(new Runnable() {
			@Override
			public void run() {
				stopRefreshing();
			}
		}, loadingTime);
    }

	@Override
	public void onPause() {
		super.onPause();
		if (refreshLayout.isRefreshing()) stopRefreshing();
	}

	@Override
	public void onResume() {
		super.onResume();
		stopLoadingButton.setEnabled(refreshLayout.isRefreshing());
	}

	private void stopRefreshing() {
		stopLoadingButton.setEnabled(false);
		refreshLayout.refreshComplete();
	}
}
