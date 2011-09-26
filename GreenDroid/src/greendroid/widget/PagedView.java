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
package greendroid.widget;

import greendroid.util.Config;

import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

/**
 * <p>
 * A View that shows items in a "paged" manner. Pages can be scrolled
 * horizontally by swiping the View. The PagedView uses a reuse mechanism
 * similar to the one used by the ListView widget. Pages come from a
 * {@link PagedAdapter}.
 * </p>
 * <p>
 * Clients may listen to PagedView changes (scrolling, page change, etc.) using
 * an {@link OnPagedViewChangeListener} .
 * </p>
 * <p>
 * It is usually a good idea to show the user which page is currently on screen.
 * This can be easily done with a {@link PageIndicator}.
 * </p>
 * 
 * @author Cyril Mottier
 */
public class PagedView extends ViewGroup {

    private static final String LOG_TAG = PagedView.class.getSimpleName();

    /**
     * Clients may listen to changes occurring on a PagedView via this
     * interface.
     * 
     * @author Cyril Mottier
     */
    public interface OnPagedViewChangeListener {

        /**
         * Notify the client the current page has changed.
         * 
         * @param pagedView The PagedView that changed its current page
         * @param previousPage The previously selected page
         * @param newPage The newly selected page
         */
        void onPageChanged(PagedView pagedView, int previousPage, int newPage);

        /**
         * Notify the client the user started tracking.
         * 
         * @param pagedView The PagedView the user started to track.
         */
        void onStartTracking(PagedView pagedView);

        /**
         * Notify the client the user ended tracking.
         * 
         * @param pagedView The PagedView the user ended to track.
         */
        void onStopTracking(PagedView pagedView);
    }

    private static final int INVALID_PAGE = -1;
    private static final int MINIMUM_PAGE_CHANGE_VELOCITY = 500;
    private static final int VELOCITY_UNITS = 1000;
    private static final int FRAME_RATE = 1000 / 60;

    private final Handler mHandler = new Handler();

    private int mPageCount;
    private int mCurrentPage;
    private int mTargetPage = INVALID_PAGE;

    private int mPagingTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int mPageSlop;

    private boolean mIsBeingDragged;

    private int mOffsetX;
    private int mStartMotionX;
    private int mStartOffsetX;

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    private OnPagedViewChangeListener mOnPageChangeListener;

    private PagedAdapter mAdapter;

    private SparseArray<View> mActiveViews = new SparseArray<View>();
    private Queue<View> mRecycler = new LinkedList<View>();

    public PagedView(Context context) {
        this(context, null);
    }

    public PagedView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPagedView();
    }

    private void initPagedView() {

        final Context context = getContext();

        mScroller = new Scroller(context, new DecelerateInterpolator());

        final ViewConfiguration conf = ViewConfiguration.get(context);
        // getScaledPagingTouchSlop() only available in API Level 8
        mPagingTouchSlop = conf.getScaledTouchSlop() * 2;
        mMaximumVelocity = conf.getScaledMaximumFlingVelocity();

        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mMinimumVelocity = (int) (metrics.density * MINIMUM_PAGE_CHANGE_VELOCITY + 0.5f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int childWidth = 0;
        int childHeight = 0;

        int itemCount = mAdapter == null ? 0 : mAdapter.getCount();
        if (itemCount > 0) {

            if (widthMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.UNSPECIFIED) {

                final View child = obtainView(mCurrentPage);

                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                childWidth = child.getMeasuredWidth();
                childHeight = child.getMeasuredHeight();
            }

            if (widthMode == MeasureSpec.UNSPECIFIED) {
                widthSize = childWidth;
            }

            if (heightMode == MeasureSpec.UNSPECIFIED) {
                heightSize = childHeight;
            }
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPageSlop = (int) (w * 0.5);
        // Make sure the offset adapts itself to mCurrentPage
        mOffsetX = getOffsetForPage(mCurrentPage);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        if (mPageCount <= 0) {
            return;
        }

        final int startPage = getPageForOffset(mOffsetX);
        final int endPage = getPageForOffset(mOffsetX - getWidth() + 1);

        recycleViews(startPage, endPage);

        for (int i = startPage; i <= endPage; i++) {
            View child = mActiveViews.get(i);
            if (child == null) {
                child = obtainView(i);
            }
            setupView(child, i);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        /*
         * Shortcut the most recurring case: the user is in the dragging state
         * and he is moving his finger. We want to intercept this motion.
         */
        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_MOVE && mIsBeingDragged) {
            return true;
        }

        final int x = (int) ev.getX();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mStartMotionX = x;
                /*
                 * If currently scrolling and user touches the screen, initiate
                 * drag; otherwise don't. mScroller.isFinished should be false
                 * when being flinged.
                 */
                mIsBeingDragged = !mScroller.isFinished();
                if (mIsBeingDragged) {
                    mScroller.forceFinished(true);
                    mHandler.removeCallbacks(mScrollerRunnable);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have
                 * caught it. Check whether the user has moved far enough from
                 * his original down touch.
                 */

                final int xDiff = (int) Math.abs(x - mStartMotionX);
                if (xDiff > mPagingTouchSlop) {
                    mIsBeingDragged = true;
                    performStartTracking(x);
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                /*
                 * Release the drag
                 */
                mIsBeingDragged = false;
                break;
        }

        /*
         * Motion events are only intercepted during dragging mode.
         */
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        final int action = ev.getAction();
        final int x = (int) ev.getX();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.forceFinished(true);
                    mHandler.removeCallbacks(mScrollerRunnable);
                }
                performStartTracking(x);
                break;

            case MotionEvent.ACTION_MOVE:
                // Scroll to follow the motion event
                final int newOffset = mStartOffsetX - (mStartMotionX - x);
                if (newOffset > 0 || newOffset < getOffsetForPage(mPageCount - 1)) {
                    mStartOffsetX = mOffsetX;
                    mStartMotionX = x;
                } else {
                    setOffsetX(newOffset);
                }

                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                setOffsetX(mStartOffsetX - (mStartMotionX - x));

                int direction = 0;

                final int slop = mStartMotionX - x;
                if (Math.abs(slop) > mPageSlop) {
                    direction = (slop > 0) ? 1 : -1;
                } else {
                    mVelocityTracker.computeCurrentVelocity(VELOCITY_UNITS, mMaximumVelocity);
                    final int initialVelocity = (int) mVelocityTracker.getXVelocity();
                    if (Math.abs(initialVelocity) > mMinimumVelocity) {
                        direction = (initialVelocity > 0) ? -1 : 1;
                    }
                }

                if (mOnPageChangeListener != null) {
                    mOnPageChangeListener.onStopTracking(this);
                }

                smoothScrollToPage(getActualCurrentPage() + direction);

                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }

                break;
        }

        return true;
    }

    /**
     * Set a listener to be notified of changes that may occur in this
     * {@link PagedView}.
     * 
     * @param listener The listener to callback.
     */
    public void setOnPageChangeListener(OnPagedViewChangeListener listener) {
        mOnPageChangeListener = listener;
    }

    /**
     * Sets the {@link PagedAdapter} used to fill this {@link PagedView} with
     * some basic information : the number of displayed pages, the pages, etc.
     * 
     * @param adapter The {@link PagedAdapter} to set to this {@link PagedView}
     */
    public void setAdapter(PagedAdapter adapter) {

        if (null != mAdapter) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }

        // Reset
        mRecycler.clear();
        mActiveViews.clear();
        removeAllViews();

        mAdapter = adapter;

        mTargetPage = INVALID_PAGE;
        mCurrentPage = 0;
        mOffsetX = 0;

        if (null != mAdapter) {
            mAdapter.registerDataSetObserver(mDataSetObserver);
            mPageCount = mAdapter.getCount();
        }

        requestLayout();
        invalidate();
    }

    /**
     * Returns the current page.
     * 
     * @return The current page
     */
    public int getCurrentPage() {
        return mCurrentPage;
    }

    private int getActualCurrentPage() {
        return mTargetPage != INVALID_PAGE ? mTargetPage : mCurrentPage;
    }

    /**
     * Initiate an animated scrolling from the current position to the given
     * page
     * 
     * @param page The page to scroll to.
     */
    public void smoothScrollToPage(int page) {
        scrollToPage(page, true);
    }

    /**
     * Initiate an animated scrolling to the next page
     */
    public void smoothScrollToNext() {
        smoothScrollToPage(getActualCurrentPage() + 1);
    }

    /**
     * Initiate an animated scrolling to the previous page
     */
    public void smoothScrollToPrevious() {
        smoothScrollToPage(getActualCurrentPage() - 1);
    }

    /**
     * Instantly moves the PagedView from the current position to the given
     * page.
     * 
     * @param page The page to scroll to.
     */
    public void scrollToPage(int page) {
        scrollToPage(page, false);
    }

    /**
     * Instantly moves to the next page
     */
    public void scrollToNext() {
        scrollToPage(getActualCurrentPage() + 1);
    }

    /**
     * Instantly moves to the previous page
     */
    public void scrollToPrevious() {
        scrollToPage(getActualCurrentPage() - 1);
    }

    private void scrollToPage(int page, boolean animated) {

        // Make sure page is bound to correct values
        page = Math.max(0, Math.min(page, mPageCount - 1));

        final int targetOffset = getOffsetForPage(page);

        final int dx = targetOffset - mOffsetX;
        if (dx == 0) {
            performPageChange(page);
            return;
        }

        if (animated) {
            mTargetPage = page;
            mScroller.startScroll(mOffsetX, 0, dx, 0);
            mHandler.post(mScrollerRunnable);
        } else {
            setOffsetX(targetOffset);
            performPageChange(page);
        }
    }

    private void setOffsetX(int offsetX) {

        if (offsetX == mOffsetX) {
            return;
        }

        final int startPage = getPageForOffset(offsetX);
        final int endPage = getPageForOffset(offsetX - getWidth() + 1);

        recycleViews(startPage, endPage);

        final int leftAndRightOffset = offsetX - mOffsetX;
        for (int i = startPage; i <= endPage; i++) {

            View child = mActiveViews.get(i);
            if (child == null) {
                child = obtainView(i);
                setupView(child, i);
            }

            child.offsetLeftAndRight(leftAndRightOffset);
        }

        mOffsetX = offsetX;
        invalidate();
    }

    private int getOffsetForPage(int page) {
        return -(page * getWidth());
    }

    private int getPageForOffset(int offset) {
        return -offset / getWidth();
    }

    private void recycleViews(int start, int end) {
        // [start, end] <=> range of pages that needs to be displayed
        final SparseArray<View> activeViews = mActiveViews;

        final int count = activeViews.size();
        for (int i = 0; i < count; i++) {
            final int key = activeViews.keyAt(i);
            if (key < start || key > end) {
                final View recycled = activeViews.valueAt(i);
                removeView(recycled);
                mRecycler.add(recycled);

                activeViews.delete(key);
            }
        }
    }

    private View obtainView(int position) {
        // Get a view from the recycler
        final View recycled = mRecycler.poll();

        View child = mAdapter.getView(position, recycled, this);

        if (child == null) {
            throw new NullPointerException("PagedAdapter.getView must return a non-null View");
        }
        if (recycled != null && child != recycled) {
            if (Config.GD_WARNING_LOGS_ENABLED) {
                Log.w(LOG_TAG, "Not reusing the convertView may impact PagedView performance.");
            }
        }

        addView(child);
        mActiveViews.put(position, child);

        return child;
    }

    private void setupView(View child, int position) {

        if (child == null) {
            return;
        }

        LayoutParams lp = child.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        }

        // Measure the view
        final int childWidthSpec = getChildMeasureSpec(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY), 0, lp.width);
        final int childHeightSpec = getChildMeasureSpec(MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY), 0, lp.height);
        child.measure(childWidthSpec, childHeightSpec);

        // Layout the view
        final int childLeft = mOffsetX - getOffsetForPage(position);
        child.layout(childLeft, 0, childLeft + child.getMeasuredWidth(), child.getMeasuredHeight());
    }

    private void performStartTracking(int startMotionX) {
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onStartTracking(this);
        }
        mStartMotionX = startMotionX;
        mStartOffsetX = mOffsetX;
    }

    private void performPageChange(int newPage) {
        if (mCurrentPage != newPage) {
            if (mOnPageChangeListener != null) {
                mOnPageChangeListener.onPageChanged(this, mCurrentPage, newPage);
            }
            mCurrentPage = newPage;
        }
    }

    static class SavedState extends BaseSavedState {

        int currentPage;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPage = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(currentPage);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.currentPage = mCurrentPage;

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        mCurrentPage = ss.currentPage;
    }

    private DataSetObserver mDataSetObserver = new DataSetObserver() {

        public void onInvalidated() {
            // Not handled
        };

        public void onChanged() {
            // TODO Cyril : When data has changed we should normally
            // look for the position that as the same id is case
            // Adapter.hasStableIds() returns true.
            final int currentPage = mCurrentPage;
            setAdapter(mAdapter);
            mCurrentPage = currentPage;
            setOffsetX(getOffsetForPage(currentPage));
        };

    };

    private Runnable mScrollerRunnable = new Runnable() {
        @Override
        public void run() {
            final Scroller scroller = mScroller;
            if (!scroller.isFinished()) {
                scroller.computeScrollOffset();
                setOffsetX(scroller.getCurrX());
                mHandler.postDelayed(this, FRAME_RATE);
            } else {
                performPageChange(mTargetPage);
            }
        }
    };

}
