package com.cyrilmottier.android.gdcatalog;

import com.cyrilmottier.android.gdcatalog.util.ColorUtils;

import greendroid.widget.SegmentedAdapter;
import greendroid.widget.SegmentedHost;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class SegmentedActivity extends Activity {

    private final Handler mHandler = new Handler();
    private PeopleSegmentedAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.segmented_controls);

        SegmentedHost segmentedHost = (SegmentedHost) findViewById(R.id.segmentedHost);

        mAdapter = new PeopleSegmentedAdapter();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter.mReverse = true;
                mAdapter.notifyDataSetChanged();
            }
        }, 4000);

        segmentedHost.setAdapter(mAdapter);
    }

    private class PeopleSegmentedAdapter extends SegmentedAdapter {

        public boolean mReverse = false;

        @Override
        public View getView(int position, ViewGroup parent) {

            TextView textView = new TextView(SegmentedActivity.this);
            textView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
            textView.setGravity(Gravity.CENTER);

            final int color = getColor(mReverse ? ((getCount() - 1) - position) : position);
            textView.setBackgroundColor(color);
            textView.setTextColor(ColorUtils.negativeColor(color));
            // It's not necessary to compute the "reversed" position as the
            // getSegmentTitle will do it automatically
            textView.setText(getSegmentTitle(position));

            return textView;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public String getSegmentTitle(int position) {

            switch (mReverse ? ((getCount() - 1) - position) : position) {
                case 0:
                    return getString(R.string.segment_1);
                case 1:
                    return getString(R.string.segment_2);
                case 2:
                    return getString(R.string.segment_3);
                case 3:
                    return getString(R.string.segment_4);
            }
            
            return null;
        }
        
        private int getColor(int position) {
            switch (position) {
                case 0:
                    return Color.RED;
                case 1:
                    return Color.GREEN;
                case 2:
                    return Color.BLUE;
                case 3:
                    return Color.CYAN;
            }
            return Color.TRANSPARENT;
        }
    }

}
