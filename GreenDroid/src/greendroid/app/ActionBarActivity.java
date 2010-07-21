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

import android.app.Activity;
import android.widget.FrameLayout;
import greendroid.widget.ActionBar;
import greendroid.widget.ActionBar.OnActionBarListener;

/**
 * Defines all methods related to Activities embedding an {@link ActionBar}
 * 
 * @author Cyril Mottier
 */
public interface ActionBarActivity extends OnActionBarListener {

    /**
     * The key to use to set the title of the launched ActionBarActivity
     */
    public static final String GD_ACTION_BAR_TITLE = "greendroid.app.ActionBarActivity.GD_ACTION_BAR_TITLE";

    /**
     * @param position The position of the clicked item. This number is equal or
     *            greater to zero. 0 is the leftmost item.
     * @return true if the method has handled the click on the ActionBar item at
     *         position <em>position</em>. Otherwise it returns false
     */
    boolean onHandleActionBarItemClick(int position);

    /**
     * Returns the content view. Please note the content view is not the entire
     * view but a FrameLayout that contains everything except the ActionBar.
     * 
     * @return The content view
     */
    FrameLayout getContentView();

    /**
     * Returns the ActionBar. Listening to ActionBar events should be done via
     * the {@link GDActivity#onActionBarItemClicked(int)} method. Use this
     * method to add new items to the {@link ActionBar}
     * 
     * @see ActionBarActivity#onHandleActionBarItemClick(int)
     * @return The ActionBar displayed on screen
     */
    ActionBar getActionBar();

    /**
     * A simple utility that casts the {@link Application} returned by
     * {@link #getApplication()} into a {@link GDApplication}
     * 
     * @return The current {@link GDApplication}
     */
    GDApplication getGDApplication();

    /**
     * Returns the identifier of the layout that needs to be created for this
     * {@link ActionBarActivity}
     * 
     * @return The identifier of the layout to create
     */
    int createLayout();

    /**
     * Called at the beginning of the {@link Activity#onContentChanged()}
     * method. This should be used to initialize all references on elements.
     */
    void onPreContentChanged();

    /**
     * Called at the end of the {@link Activity#onContentChanged()} method. This
     * should be use to initialize the content of the layout (titles, etc.)
     */
    void onPostContentChanged();
}
