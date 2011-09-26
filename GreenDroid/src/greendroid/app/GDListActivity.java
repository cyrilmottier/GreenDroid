/*
 * Copyright (C) 2010 Cyril Mottier (http://www.cyrilmottier.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greendroid.app;

import greendroid.util.Config;
import greendroid.widget.ActionBar;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.cyrilmottier.android.greendroid.R;

/**
 * An equivalent to ListActivity that manages a ListView.
 * 
 * @author Cyril Mottier
 */
public class GDListActivity extends GDActivity {

    private static final String LOG_TAG = GDListActivity.class.getSimpleName();

    private ListAdapter mAdapter;
    private ListView mListView;
    private View mEmptyView;

    private Handler mHandler = new Handler();
    private boolean mFinishedStart = false;

    private Runnable mRequestFocus = new Runnable() {
        public void run() {
            mListView.focusableViewAvailable(mListView);
        }
    };

    public GDListActivity() {
        super();
    }

    public GDListActivity(ActionBar.Type actionBarType) {
        super(actionBarType);
    }

    /**
     * This method will be called when an item in the list is selected.
     * Subclasses should override. Subclasses can call
     * getListView().getItemAtPosition(position) if they need to access the data
     * associated with the selected item.
     * 
     * @param l The ListView where the click happened
     * @param v The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id The row id of the item that was clicked
     */
    protected void onListItemClick(ListView l, View v, int position, long id) {
    }

    /**
     * Set the currently selected list item to the specified position with the
     * adapter's data
     * 
     * @param position The position to select in the managed ListView
     */
    public void setSelection(int position) {
        mListView.setSelection(position);
    }

    /**
     * Get the position of the currently selected list item.
     * 
     * @return The position of the currently selected ListView item.
     */
    public int getSelectedItemPosition() {
        return mListView.getSelectedItemPosition();
    }

    /**
     * Get the ListAdapter row ID of the currently selected list item.
     * 
     * @return The identifier of the selected ListView item.
     */
    public long getSelectedItemId() {
        return mListView.getSelectedItemId();
    }

    /**
     * Get the activity's ListView widget.
     * 
     * @return The ListView managed by the current {@link GDListActivity}
     */
    public ListView getListView() {
        ensureLayout();
        return mListView;
    }

    /**
     * Get the ListAdapter associated with this activity's ListView.
     * 
     * @return The ListAdapter currently associated to the underlying ListView
     */
    public ListAdapter getListAdapter() {
        return mAdapter;
    }

    /**
     * Provides the Adapter for the ListView handled by this
     * {@link GDListActivity}
     * 
     * @param adapter The ListAdapter to set.
     */
    public void setListAdapter(ListAdapter adapter) {
        synchronized (this) {
            ensureLayout();
            mAdapter = adapter;
            mListView.setAdapter(adapter);
        }
    }

    @Override
    public int createLayout() {
        if (Config.GD_INFO_LOGS_ENABLED) {
            Log.i(LOG_TAG, "No layout specified : creating the default layout");
        }

        switch (getActionBarType()) {
            case Dashboard:
                return R.layout.gd_list_content_dashboard;
            case Empty:
                return R.layout.gd_list_content_empty;
            case Normal:
            default:
                return R.layout.gd_list_content_normal;
        }
    }

    @Override
    protected boolean verifyLayout() {
        return super.verifyLayout() && mListView != null;
    }

    @Override
    public void onPreContentChanged() {
        super.onPreContentChanged();

        mEmptyView = findViewById(android.R.id.empty);
        mListView = (ListView) findViewById(android.R.id.list);
        if (mListView == null) {
            throw new RuntimeException("Your content must have a ListView whose id attribute is " + "'android.R.id.list'");
        }
    }

    @Override
    public void onPostContentChanged() {
        super.onPostContentChanged();

        if (mEmptyView != null) {
            mListView.setEmptyView(mEmptyView);
        }
        mListView.setOnItemClickListener(mOnItemClickListener);
        if (mFinishedStart) {
            setListAdapter(mAdapter);
        }
        mHandler.post(mRequestFocus);
        mFinishedStart = true;
    }

    @Override
    public void setActionBarContentView(int resID) {
        throwSetActionBarContentViewException();
    }

    @Override
    public void setActionBarContentView(View view, LayoutParams params) {
        throwSetActionBarContentViewException();
    }

    @Override
    public void setActionBarContentView(View view) {
        throwSetActionBarContentViewException();
    }

    private void throwSetActionBarContentViewException() {
        throw new UnsupportedOperationException(
                "The setActionBarContentView method is not supported for GDListActivity. In order to get a custom layout you must return a layout identifier in createLayout");
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            onListItemClick((ListView) parent, v, position, id);
        }
    };

}
