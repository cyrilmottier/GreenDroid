/*
 * Copyright (C) 2011 Cyril Mottier (http://www.cyrilmottier.com)
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

import greendroid.widget.ActionBar;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.cyrilmottier.android.greendroid.R;

/**
 * A {@link GDActivity} equivalent to ExpandableListActivity that manages an
 * ExpandableListView.
 */
public class GDExpandableListActivity extends GDActivity implements OnCreateContextMenuListener, ExpandableListView.OnChildClickListener,
        ExpandableListView.OnGroupCollapseListener, ExpandableListView.OnGroupExpandListener {

    private ExpandableListAdapter mAdapter;
    private ExpandableListView mList;
    private View mEmptyView;

    boolean mFinishedStart = false;

    public GDExpandableListActivity() {
        super();
    }

    public GDExpandableListActivity(ActionBar.Type actionBarType) {
        super(actionBarType);
    }

    /**
     * Override this to populate the context menu when an item is long pressed.
     * menuInfo will contain an
     * android.widget.ExpandableListView.ExpandableListContextMenuInfo whose
     * packedPosition is a packed position that should be used with
     * ExpandableListView#getPackedPositionType(long) and the other similar
     * methods.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    }

    /**
     * Override this for receiving callbacks when a child has been clicked.
     * <p>
     * {@inheritDoc}
     */
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        return false;
    }

    /**
     * Override this for receiving callbacks when a group has been collapsed.
     */
    public void onGroupCollapse(int groupPosition) {
    }

    /**
     * Override this for receiving callbacks when a group has been expanded.
     */
    public void onGroupExpand(int groupPosition) {
    }

    /**
     * Provide the adapter for the expandable list.
     */
    public void setListAdapter(ExpandableListAdapter adapter) {
        synchronized (this) {
            ensureLayout();
            mAdapter = adapter;
            mList.setAdapter(adapter);
        }
    }

    /**
     * Get the activity's expandable list view widget. This can be used to get
     * the selection, set the selection, and many other useful functions.
     * 
     * @see ExpandableListView
     */
    public ExpandableListView getExpandableListView() {
        ensureLayout();
        return mList;
    }

    /**
     * Get the ExpandableListAdapter associated with this activity's
     * ExpandableListView.
     */
    public ExpandableListAdapter getExpandableListAdapter() {
        return mAdapter;
    }

    @Override
    public int createLayout() {
        switch (getActionBarType()) {
            case Dashboard:
                return R.layout.gd_expandable_list_content_dashboard;
            case Empty:
                return R.layout.gd_expandable_list_content_empty;
            case Normal:
            default:
                return R.layout.gd_expandable_list_content_normal;
        }
    }

    @Override
    public void onPreContentChanged() {
        super.onPreContentChanged();

        mEmptyView = findViewById(android.R.id.empty);
        mList = (ExpandableListView) findViewById(android.R.id.list);
        if (mList == null) {
            throw new RuntimeException("Your content must have a ExpandableListView whose id attribute is " + "'android.R.id.list'");
        }
    }

    @Override
    public void onPostContentChanged() {
        super.onPostContentChanged();

        if (mEmptyView != null) {
            mList.setEmptyView(mEmptyView);
        }
        mList.setOnChildClickListener(this);
        mList.setOnGroupExpandListener(this);
        mList.setOnGroupCollapseListener(this);

        if (mFinishedStart) {
            setListAdapter(mAdapter);
        }
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

    @Override
    protected boolean verifyLayout() {
        return super.verifyLayout() && mList != null;
    }

    /**
     * Gets the ID of the currently selected group or child.
     * 
     * @return The ID of the currently selected group or child.
     */
    public long getSelectedId() {
        return mList.getSelectedId();
    }

    /**
     * Gets the position (in packed position representation) of the currently
     * selected group or child. Use ExpandableListView#getPackedPositionType,
     * ExpandableListView#getPackedPositionGroup, and
     * ExpandableListView#getPackedPositionChild to unpack the returned packed
     * position.
     * 
     * @return A packed position representation containing the currently
     *         selected group or child's position and type.
     */
    public long getSelectedPosition() {
        return mList.getSelectedPosition();
    }

    /**
     * Sets the selection to the specified child. If the child is in a collapsed
     * group, the group will only be expanded and child subsequently selected if
     * shouldExpandGroup is set to true, otherwise the method will return false.
     * 
     * @param groupPosition The position of the group that contains the child.
     * @param childPosition The position of the child within the group.
     * @param shouldExpandGroup Whether the child's group should be expanded if
     *            it is collapsed.
     * @return Whether the selection was successfully set on the child.
     */
    public boolean setSelectedChild(int groupPosition, int childPosition, boolean shouldExpandGroup) {
        return mList.setSelectedChild(groupPosition, childPosition, shouldExpandGroup);
    }

    /**
     * Sets the selection to the specified group.
     * 
     * @param groupPosition The position of the group that should be selected.
     */
    public void setSelectedGroup(int groupPosition) {
        mList.setSelectedGroup(groupPosition);
    }
}
