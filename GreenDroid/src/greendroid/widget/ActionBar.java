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
package greendroid.widget;

import greendroid.util.Config;

import java.util.LinkedList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

public class ActionBar extends LinearLayout {

    private static final String LOG_TAG = ActionBar.class.getSimpleName();

    private static final int MAX_ITEMS_COUNT = 3;

    public enum Type {
        Normal, Dashboard
    }
    
    public interface OnActionBarListener {

        static final int HOME_ITEM = -1;

        /**
         * Clients may listen to this method in order to be notified the user
         * has clicked on an item.
         * 
         * @param position The position of the item in the action bar. -1 means
         *            the user pressed the "Home" button. 0 means the user
         *            clicked the first action bar item (the leftmost item) and
         *            so on.
         */
        void onActionBarItemClicked(int position);
    }

    private TextView mTitleView;
    private ImageButton mHomeButton;

    private boolean mMerging = false;

    private String mTitle;
    private ActionBar.Type mType;
    private OnActionBarListener mOnActionBarListener;
    private LinkedList<ActionBarItem> mItems;

    private Drawable mDividerDrawable;
    private Drawable mHomeDrawable;
    private int mDividerWidth;

    public ActionBar(Context context) {
        this(context, null);
    }

    public ActionBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.gdActionBarStyle);
    }

    public ActionBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

        initActionBar();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionBar, defStyle, 0);

        mTitle = a.getString(R.styleable.ActionBar_title);

        mDividerDrawable = a.getDrawable(R.styleable.ActionBar_dividerDrawable);
        mDividerWidth = a.getDimensionPixelSize(R.styleable.ActionBar_dividerWidth, -1);
        mHomeDrawable = a.getDrawable(R.styleable.ActionBar_homeDrawable);

        int layoutID;
        int type = a.getInteger(R.styleable.ActionBar_type, -1);
        switch (type) {
            case 1:
                mType = Type.Dashboard;
                layoutID = R.layout.gd_action_bar_dashboard;
                break;
            case 0:
            default:
                mType = Type.Normal;
                layoutID = R.layout.gd_action_bar_normal;
                break;
        }

        // HACK cyril: Without this, the onFinishInflate is called twice !?!
        // This issue is due to a bug when Android inflates a layout with a
        // parent - which is compulsory with a <merge /> tag. I've reported this
        // bug to Romain Guy who fixed it (patch will probably be available in
        // the Gingerbread release).
        mMerging = true;
        LayoutInflater.from(context).inflate(layoutID, this);
        mMerging = false;

        a.recycle();
    }

    private void initActionBar() {
        mItems = new LinkedList<ActionBarItem>();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (!mMerging) {

            if (Config.GD_INFO_LOGS_ENABLED) {
                Log.i(LOG_TAG, "onFinishInflate() - not merging");
            }

            // Work done for both Dashboard and Normal type
            mHomeButton = (ImageButton) findViewById(R.id.gd_action_bar_home_item);
            mHomeButton.setOnClickListener(mClickHandler);

            switch (mType) {
                case Normal:
                    mHomeButton.setImageDrawable(mHomeDrawable);
                    mHomeButton.setContentDescription(getContext().getString(R.string.gd_go_home));
                    mTitleView = (TextView) findViewById(R.id.gd_action_bar_title);
                    if (mTitle != null) {
                        setTitle(mTitle);
                    }
                    
                default:
                    //Do nothing
                    break;
            }
        }

    }

    public void setOnActionBarListener(OnActionBarListener listener) {
        mOnActionBarListener = listener;
    }

    public void setTitle(CharSequence title) {
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
    }

    public ActionBarItem addItem(ActionBarItem.Type actionBarItemType) {
        ActionBarItem item = ActionBarItem.createWithType(this, actionBarItemType);
        addItem(item);
        return item;
    }

    public void addItem(ActionBarItem item) {

        if (mItems.size() >= MAX_ITEMS_COUNT) {
            /*
             * An ActionBar must contain as few items as possible. So let's keep
             * a limit :)
             */
            return;
        }

        if (mDividerDrawable != null) {
            ImageView divider = new ImageView(getContext());
            int dividerWidth = (mDividerWidth > 0) ? mDividerWidth : mDividerDrawable.getIntrinsicWidth();
            final LinearLayout.LayoutParams lp = new LayoutParams(dividerWidth, LayoutParams.FILL_PARENT);
            divider.setLayoutParams(lp);
            divider.setBackgroundDrawable(mDividerDrawable);
            addView(divider);
        }

        final View itemView = item.getItemView();
        itemView.findViewById(R.id.gd_action_bar_item).setOnClickListener(mClickHandler);

        final int size = (int) getResources().getDimension(R.dimen.gd_action_bar_height);
        addView(itemView, new LayoutParams(size, LayoutParams.FILL_PARENT));

        mItems.add(item);
    }

    public ActionBarItem getItem(int position) {
        if (position < 0 || position >= mItems.size()) {
            return null;
        }
        return mItems.get(position);
    }

    public void removeItem(int position) {

        if (position < 0 || position >= mItems.size()) {
            return;
        }

        final int viewIndex = indexOfChild(mItems.get(position).getItemView());
        final int increment = (mDividerDrawable != null) ? 1 : 0;
        removeViews(viewIndex - increment, 1 + increment);
        mItems.remove(position);
    }

    /**
     * @hide TODO cyril: To be tested.
     */
    public void setType(Type type) {
        if (type != mType) {

            removeAllViews();

            int layoutId = 0;
            switch (type) {
                case Dashboard:
                    layoutId = R.layout.gd_action_bar_dashboard;
                    break;
                case Normal:
                    layoutId = R.layout.gd_action_bar_normal;
                    break;
            }

            mMerging = true;
            LayoutInflater.from(getContext()).inflate(layoutId, this);
            mMerging = false;

            // Reset all items
            LinkedList<ActionBarItem> itemsCopy = new LinkedList<ActionBarItem>(mItems);
            mItems.clear();
            for (ActionBarItem item : itemsCopy) {
                addItem(item);
            }
        }
    }

    public ActionBarItem newActionBarItem(Class<? extends ActionBarItem> klass) {
        try {
            ActionBarItem item = klass.newInstance();
            item.setActionBar(this);
            return item;
        } catch (Exception e) {
            throw new IllegalArgumentException("The given klass must have a default constructor");
        }
    }
    
    private OnClickListener mClickHandler = new OnClickListener() {

        public void onClick(View v) {
            if (mOnActionBarListener != null) {

                if (v == mHomeButton) {
                    mOnActionBarListener.onActionBarItemClicked(OnActionBarListener.HOME_ITEM);
                    return;
                }

                final int itemCount = mItems.size();
                for (int i = 0; i < itemCount; i++) {
                    final ActionBarItem item = mItems.get(i);
                    final View itemButton = item.getItemView().findViewById(R.id.gd_action_bar_item);
                    if (v == itemButton) {
                        item.onItemClicked();
                        mOnActionBarListener.onActionBarItemClicked(i);
                        break;
                    }
                }
            }
        }

    };

}
