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
import java.util.List;

import com.cyrilmottier.android.greendroid.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActionBar extends LinearLayout implements OnClickListener {

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
         *            pressed the "Home" button. 0 means the user clicked the
         *            first action bar item and so on.
         */
        void onActionBarItemClicked(int position);
    }

    private TextView mTitleView;
    private ImageButton mHomeButton;

    private boolean mMerging = false;

    private String mTitle;
    private ActionBar.Type mType;
    private OnActionBarListener mOnActionBarListener;
    private LinkedList<ImageButton> mItems;

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
        LayoutInflater.from(getContext()).inflate(layoutID, this);
        mMerging = false;

        a.recycle();
    }

    private void initActionBar() {
        mItems = new LinkedList<ImageButton>();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (!mMerging) {

            if (Config.GD_INFO_LOGS_ENABLED) {
                Log.i(LOG_TAG, "onFinishInflate() - not merging");
            }

            mHomeButton = (ImageButton) findViewById(R.id.gd_action_bar_home_item);
            mHomeButton.setOnClickListener(this);

            switch (mType) {
                case Normal:
                    mTitleView = (TextView) findViewById(R.id.gd_action_bar_title);
                    if (mTitle != null) {
                        setTitle(mTitle);
                    }

                    mHomeButton.setImageDrawable(mHomeDrawable);
                    break;

                default:
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

    public void setItems(List<Drawable> items) {

        final int itemCount = mItems.size();
        int removeCount = itemCount;
        if (mDividerDrawable != null) {
            /*
             * If we have a divider we'll have to remove itemCount - 1
             * ImageViews
             */
            removeCount += (itemCount - 1);
        }
        removeViews(getChildCount() - removeCount, removeCount);

        for (Drawable drawable : items) {
            addItem(drawable);
        }
    }

    public void addItem(Drawable d) {

        if (mItems.size() >= MAX_ITEMS_COUNT) {
            return;
        }

        if (mDividerDrawable != null) {
            ImageView divider = new ImageView(getContext());
            int dividerWidth = (mDividerWidth > 0) ? mDividerWidth : mDividerDrawable.getIntrinsicWidth();
            final LinearLayout.LayoutParams lp = new LayoutParams(dividerWidth, LayoutParams.FILL_PARENT);
            lp.setMargins(0, 0, 0, 0);
            divider.setLayoutParams(lp);
            divider.setBackgroundDrawable(mDividerDrawable);
            addView(divider);
        }

        final LayoutInflater inflater = LayoutInflater.from(getContext());
        ImageButton button = (ImageButton) inflater.inflate(R.layout.gd_action_bar_item, this, false);
        button.setImageDrawable(d);
        button.setOnClickListener(this);

        addView(button);
        mItems.add(button);
    }

    // public void setType(Type type) {
    // if (type != mType) {
    //            
    // // Saves all drawables
    // LinkedList<Drawable> drawables = new LinkedList<Drawable>();
    // for (ImageButton imageButton : mItems) {
    // drawables.add(imageButton.getDrawable());
    // }
    // mItems.clear();
    //            
    // removeAllViews();
    // mMerging = true;
    // LayoutInflater.from(getContext()).inflate(R.layout.gd_action_bar_item,
    // this);
    // mMerging = false;
    //            
    // // Reset all items
    // setItems(drawables);
    // }
    // }

    public void addItem(int drawableId) {
        addItem(getContext().getResources().getDrawable(drawableId));
    }

    public void removeItemAt(int position) {

        if (position < 0 || position >= mItems.size()) {
            return;
        }

        final int viewIndex = indexOfChild(mItems.get(position));
        removeViews(viewIndex - 1, 2);
        mItems.remove(position);
    }

    public void onClick(View v) {

        if (mOnActionBarListener != null) {

            if (v == mHomeButton) {
                mOnActionBarListener.onActionBarItemClicked(OnActionBarListener.HOME_ITEM);
                return;
            }

            final int itemCount = mItems.size();
            for (int i = 0; i < itemCount; i++) {
                if (v == mItems.get(i)) {
                    mOnActionBarListener.onActionBarItemClicked(i);
                    break;
                }
            }

        }

    }

}
