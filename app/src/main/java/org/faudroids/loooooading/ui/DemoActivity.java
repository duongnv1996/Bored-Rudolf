package org.faudroids.loooooading.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.faudroids.loooooading.R;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_demo)
public class DemoActivity extends RoboActionBarActivity implements CustomSwipeRefreshLayout.OnRefreshListener {

    @InjectView(R.id.layout_swipe) private CustomSwipeRefreshLayout refreshLayout;
	@InjectView(R.id.spinner_loading_time) private Spinner loadingTimesSpinner;
	private int[] loadingTimeValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        refreshLayout.setOnRefreshListener(this);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.loading_time_strings, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		loadingTimesSpinner.setAdapter(adapter);
		loadingTimesSpinner.setSelection(2);

		loadingTimeValues = getResources().getIntArray(R.array.loading_time_values);
    }

    @Override
    public void onRefresh() {
		int loadingTime= loadingTimeValues[loadingTimesSpinner.getSelectedItemPosition()] * 1000;
		refreshLayout.postDelayed(new Runnable() {
			@Override
			public void run() {
				refreshLayout.refreshComplete();
			}
		}, loadingTime);
    }
}
