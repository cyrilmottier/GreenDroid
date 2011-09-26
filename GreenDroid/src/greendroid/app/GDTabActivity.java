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
import greendroid.widget.ActionBar.OnActionBarListener;
import greendroid.widget.ActionBarHost;
import greendroid.widget.ActionBarItem;
import android.app.TabActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

/**
 * An equivalent to a TabActivity that manages fancy tabs and an
 * {@link ActionBar}
 * 
 * @author Cyril Mottier
 */
public class GDTabActivity extends TabActivity implements ActionBarActivity {

    private static final String LOG_TAG = GDTabActivity.class.getSimpleName();

    private ActionBarHost mActionBarHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(createLayout());
    }

    public int createLayout() {
        return R.layout.gd_tab_content;
    }

    public GDApplication getGDApplication() {
        return (GDApplication) getApplication();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();

        onPreContentChanged();
        onPostContentChanged();
    }

    public void onPreContentChanged() {
        mActionBarHost = (ActionBarHost) findViewById(R.id.gd_action_bar_host);
        if (mActionBarHost == null) {
            throw new RuntimeException("Your content must have an ActionBarHost whose id attribute is R.id.gd_action_bar_host");
        }
        mActionBarHost.getActionBar().setOnActionBarListener(mActionBarListener);
    }

    public void onPostContentChanged() {

        boolean titleSet = false;

        final Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra(ActionBarActivity.GD_ACTION_BAR_TITLE);
            if (title != null) {
                titleSet = true;
                setTitle(title);
            }
        }

        if (!titleSet) {
            // No title has been set via the Intent. Let's look in the
            // ActivityInfo
            try {
                final ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), 0);
                if (activityInfo.labelRes != 0) {
                    setTitle(activityInfo.labelRes);
                }
            } catch (NameNotFoundException e) {
                // Do nothing
            }
        }

        final int visibility = intent.getIntExtra(ActionBarActivity.GD_ACTION_BAR_VISIBILITY, View.VISIBLE);
        getActionBar().setVisibility(visibility);
    }

    // @Override
    // protected void onTitleChanged(CharSequence title, int color) {
    // setTitle(title);
    // }

    @Override
    public void setTitle(CharSequence title) {
        getActionBar().setTitle(title);
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getString(titleId));
    }

    public ActionBar getActionBar() {
        return mActionBarHost.getActionBar();
    }

    public ActionBarItem addActionBarItem(ActionBarItem item) {
        return getActionBar().addItem(item);
    }

    public ActionBarItem addActionBarItem(ActionBarItem item, int itemId) {
        return getActionBar().addItem(item, itemId);
    }

    public ActionBarItem addActionBarItem(ActionBarItem.Type actionBarItemType) {
        return getActionBar().addItem(actionBarItemType);
    }

    public ActionBarItem addActionBarItem(ActionBarItem.Type actionBarItemType, int itemId) {
        return getActionBar().addItem(actionBarItemType, itemId);
    }

    public FrameLayout getContentView() {
        return mActionBarHost.getContentView();
    }

    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
        return false;
    }

    private OnActionBarListener mActionBarListener = new OnActionBarListener() {
        public void onActionBarItemClicked(int position) {
            if (position == OnActionBarListener.HOME_ITEM) {

                final Class<?> klass = getGDApplication().getHomeActivityClass();
                if (klass != null && !klass.equals(GDTabActivity.this.getClass())) {
                    if (Config.GD_INFO_LOGS_ENABLED) {
                        Log.i(LOG_TAG, "Going back to the home activity");
                    }
                    Intent homeIntent = new Intent(GDTabActivity.this, klass);
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(homeIntent);
                }

            } else {
                if (!onHandleActionBarItemClick(getActionBar().getItem(position), position)) {
                    if (Config.GD_WARNING_LOGS_ENABLED) {
                        Log.w(LOG_TAG, "Click on item at position " + position + " dropped down to the floor");
                    }
                }
            }
        }
    };

    /*
     * GDTabActivity methods
     */

    /**
     * Add a new tab to this GDTabActivity
     * 
     * @param tag The tag associated this the new tab
     * @param labelId A resource ID to the label of the new tab
     * @param intent The Intent used to start the content of the tab
     */
    public void addTab(String tag, int labelId, Intent intent) {
        addTab(tag, getString(labelId), intent);
    }

    /**
     * Add a new tab to this GDTabActivity
     * 
     * @param tag The tag associated this the new tab
     * @param label The label of the new tab
     * @param intent The Intent used to start the content of the tab
     */
    public void addTab(String tag, CharSequence label, Intent intent) {
        final TabHost host = getTabHost();

        View indicator = createTabIndicator(tag, label, getTabWidget());
        if (indicator == null) {
            final TextView textIndicator = (TextView) getLayoutInflater().inflate(R.layout.gd_tab_indicator, getTabWidget(), false);
            textIndicator.setText(label);
            indicator = textIndicator;
        }

        host.addTab(host.newTabSpec(tag).setIndicator(indicator).setContent(intent));
    }

    @Deprecated
    protected View createTabIndicator(CharSequence label) {
        return createTabIndicator(null, label, getTabWidget());
    }

    /**
     * Callback allowing client to create their own tabs.
     * 
     * @param tag The tag of the tab to create
     * @param label The label that need to be displayed in the tab
     * @param parent The parent in which the tab will be added.Please note you
     *            shouldn't add the newly created/inflated View to the parent.
     *            GDTabActivity will deal with this automatically.
     * @return A newly allocated View representing the tab.
     */
    protected View createTabIndicator(String tag, CharSequence label, ViewGroup parent) {
        return null;
    }

}
