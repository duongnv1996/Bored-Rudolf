package org.faudroids.loooooading.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.faudroids.loooooading.R;

public class SwipeRefreshActivity extends AppCompatActivity implements CustomSwipeRefreshLayout.OnRefreshListener {

    CustomSwipeRefreshLayout mCSRL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_refresh);
        mCSRL = (CustomSwipeRefreshLayout) findViewById(R.id.swipelayout);
        mCSRL.setOnRefreshListener(this);

        // let's say refresh task takes 10secs
        AsyncTask t = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                synchronized(this) {
                    try {
                        wait(1000 * 10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                mCSRL.refreshComplete();
            }
        };
        t.execute();

        // when refreshing done call
        // mCSRL.refreshComplete();
    }

    @Override
    public void onRefresh() {
        Toast.makeText(this, "Refreshing...", Toast.LENGTH_LONG).show();
    }
}
