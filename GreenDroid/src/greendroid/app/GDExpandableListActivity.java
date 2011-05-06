package greendroid.app;

import com.cyrilmottier.android.greendroid.R;

import greendroid.util.Config;
import greendroid.widget.ActionBar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
/**
 * An equivalent to {@link ExpandableListActivity} that manages a ExpandableListView.
 * 
 * @see {@link ExpandableListActivity}
 */
public class GDExpandableListActivity extends GDActivity implements 
		OnCreateContextMenuListener,
		ExpandableListView.OnChildClickListener, ExpandableListView.OnGroupCollapseListener,
		ExpandableListView.OnGroupExpandListener {

	private static final String LOG_TAG = GDExpandableListActivity.class.getSimpleName();

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
     * Override this to populate the context menu when an item is long pressed. menuInfo
     * will contain an {@link android.widget.ExpandableListView.ExpandableListContextMenuInfo}
     * whose packedPosition is a packed position
     * that should be used with {@link ExpandableListView#getPackedPositionType(long)} and
     * the other similar methods.
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
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
            int childPosition, long id) {
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
     * Get the activity's expandable list view widget.  This can be used to get the selection,
     * set the selection, and many other useful functions.
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
        if (Config.GD_INFO_LOGS_ENABLED) {
            Log.d(LOG_TAG, "No layout specified : creating the default layout");
        }
        
        switch (getActionBarType()) {
            case Dashboard:
                return R.layout.gd_expandablelist_content_dashboard;
            case Empty:
                return R.layout.gd_expandablelist_content_empty;
            case Normal:
            default:
                return R.layout.gd_expandablelist_content_normal;
        }
    }
    
    @Override
    public void onPreContentChanged() {
        super.onPreContentChanged();
        
        mEmptyView = findViewById(android.R.id.empty);
        mList = (ExpandableListView)findViewById(android.R.id.list);
        if (mList == null) {
            throw new RuntimeException(
                    "Your content must have a ExpandableListView whose id attribute is " +
                    "'android.R.id.list'");
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
     * selected group or child. Use
     * {@link ExpandableListView#getPackedPositionType},
     * {@link ExpandableListView#getPackedPositionGroup}, and
     * {@link ExpandableListView#getPackedPositionChild} to unpack the returned
     * packed position.
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
     * @param groupPosition The position of the group that should be selected.
     */
    public void setSelectedGroup(int groupPosition) {
        mList.setSelectedGroup(groupPosition);
    }
}
