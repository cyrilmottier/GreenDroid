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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import com.cyrilmottier.android.greendroid.R;

/**
 * <p>
 * Visual indicator of a paged content. The PageIndicator usually displays a
 * line of dots. Each dot represents a page. The PageIndicator supports two
 * types of dots.
 * </p>
 * <ul>
 * <li>{@link DotType#SINGLE}: all dots are drawn but only one is in the
 * selected state at a time. The selected one represents the currently visible
 * page.</li>
 * <li>{@link DotType#MULTIPLE}: the selected page is actually represented by
 * the amount of dots currently being drawn. This behavior is similar to the one
 * visible on the Android Launcher application.</li>
 * </ul>
 * <p>
 * You can have a look at GDCatalog to get a sample code of how to use a
 * PageIndicator in addition to a {@link PagedView}.
 * </p>
 * 
 * @author Cyril Mottier
 */
public class PageIndicator extends View {

    /**
     * Constant that may be used to select none of the dots in the PageIndicator
     */
    public static final int NO_ACTIVE_DOT = -1;

    /**
     * Interface containing of dot types supported by the PageIndicator class.
     * 
     * @author Cyril Mottier
     */
    public interface DotType {

        /**
         * Represents the single dot type. Only one selected dot may be drawn at
         * a time.
         */

        int SINGLE = 0;

        /**
         * Represents the multiple dot type. Several selected dot may be drawn
         * at a time. The number of dots drawn represents the currently
         * remaining page count.
         */
        int MULTIPLE = 1;
    }

    private static final int MIN_DOT_COUNT = 1;

    private static Rect sInRect = new Rect();
    private static Rect sOutRect = new Rect();

    private int mGravity;
    private int mDotSpacing;
    private Drawable mDotDrawable;
    private int mDotCount;
    private int mDotType;

    private int mActiveDot;

    private int[] mExtraState;

    private boolean mInitializing;

    public PageIndicator(Context context) {
        this(context, null);
    }

    public PageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.gdPageIndicatorStyle);
    }

    public PageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPageIndicator();

        mInitializing = true;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PageIndicator, defStyle, 0);

        setDotCount(a.getInt(R.styleable.PageIndicator_dotCount, mDotCount));
        setActiveDot(a.getInt(R.styleable.PageIndicator_activeDot, mActiveDot));
        setDotDrawable(a.getDrawable(R.styleable.PageIndicator_dotDrawable));
        setDotSpacing(a.getDimensionPixelSize(R.styleable.PageIndicator_dotSpacing, mDotSpacing));
        setGravity(a.getInt(R.styleable.PageIndicator_gravity, mGravity));
        setDotType(a.getInt(R.styleable.PageIndicator_dotType, mDotType));

        a.recycle();

        mInitializing = false;
    }

    private void initPageIndicator() {
        mDotCount = MIN_DOT_COUNT;
        mGravity = Gravity.CENTER;
        mActiveDot = 0;
        mDotSpacing = 0;
        mDotType = DotType.SINGLE;

        mExtraState = onCreateDrawableState(1);
        mergeDrawableStates(mExtraState, SELECTED_STATE_SET);
    }

    /**
     * Get the maximum number of dots to be drawn.
     * 
     * @return The maximum number of dots.
     * @see #setDotCount(int)
     */
    public int getDotCount() {
        return mDotCount;
    }

    /**
     * Set the number of dots.
     * 
     * @param dotCount The number oF dots
     * @see #getDotCount()
     */
    public void setDotCount(int dotCount) {
        if (dotCount < MIN_DOT_COUNT) {
            dotCount = MIN_DOT_COUNT;
        }

        if (mDotCount != dotCount) {
            mDotCount = dotCount;
            requestLayout();
            invalidate();
        }
    }

    /**
     * Return the current active dot. Depending on the current dot type of this
     * PageIndicator the current active dot may be the number of displayed dots
     * or the index of the selected dot
     * 
     * @return The current active dot index or dots count
     * @see #setActiveDot(int)
     */
    public int getActiveDot() {
        return mActiveDot;
    }

    /**
     * Set the index of the active dot or the number of active dots. Depending
     * on the current dot type of this PageIndicator the current active dot may
     * be the number of displayed dots or the index of the selected dot
     * 
     * @param activeDot The number/index of (the) active dot(s)
     * @see #getActiveDot()
     */
    public void setActiveDot(int activeDot) {
        if (activeDot < 0) {
            activeDot = NO_ACTIVE_DOT;
        }

        switch (mDotType) {
            case DotType.SINGLE:
                if (activeDot > mDotCount - 1) {
                    activeDot = NO_ACTIVE_DOT;
                }
                break;

            case DotType.MULTIPLE:
                if (activeDot > mDotCount) {
                    activeDot = NO_ACTIVE_DOT;
                }
        }

        mActiveDot = activeDot;
        invalidate();
    }

    /**
     * Return the Drawable currently used for each dot.
     * 
     * @return The Drawable used to draw each dot.
     */
    public Drawable getDotDrawable() {
        return mDotDrawable;
    }

    /**
     * Set the Drawable used for each dot. The given Drawable may be a
     * StateListDrawable in order to take advantage of the selection system. If
     * your StateListDrawable contains a android.R.attr.state_selected state,
     * the Drawable will be used to represent a selected dot.
     * <em><strong>Note :</strong> this methods does not support Drawable
     * that has no intrinsic dimensions.</em>
     * 
     * @param dotDrawable The Drawable used to represents a dot
     */
    public void setDotDrawable(Drawable dotDrawable) {
        if (dotDrawable != mDotDrawable) {
            if (mDotDrawable != null) {
                mDotDrawable.setCallback(null);
            }

            mDotDrawable = dotDrawable;

            if (dotDrawable != null) {

                if (dotDrawable.getIntrinsicHeight() == -1 || dotDrawable.getIntrinsicWidth() == -1) {
                    // Do not accept Drawable with no intrinsic dimensions.
                    return;
                }

                dotDrawable.setBounds(0, 0, dotDrawable.getIntrinsicWidth(), dotDrawable.getIntrinsicHeight());
                dotDrawable.setCallback(this);
                if (dotDrawable.isStateful()) {
                    dotDrawable.setState(getDrawableState());
                }
            }

            requestLayout();
            invalidate();
        }
    }

    /**
     * The spacing between each dot
     * 
     * @return The spacing between each dot
     */
    public int getDotSpacing() {
        return mDotSpacing;
    }

    /**
     * Set the spacing between each dot
     * 
     * @param dotSpacing The spacing between each dot.
     */
    public void setDotSpacing(int dotSpacing) {
        if (dotSpacing != mDotSpacing) {
            mDotSpacing = dotSpacing;
            requestLayout();
            invalidate();
        }
    }

    /**
     * Return the gravity used to draw dots/
     * 
     * @return The current gravity
     */
    public int getGravity() {
        return mGravity;
    }

    /**
     * Specifies how to align the dots by the view's x- and/or y-axis when the
     * space taken by the dots is smaller than the view.
     * 
     * @param gravity The gravity
     */
    public void setGravity(int gravity) {
        if (mGravity != gravity) {
            mGravity = gravity;
            invalidate();
        }
    }

    /**
     * The current dot type
     * 
     * @return The dot type of this {@link PageIndicator}
     * @see DotType
     */
    public int getDotType() {
        return mDotType;
    }

    /**
     * Specifies the type of dot actually drawn by this {@link PageIndicator}
     * 
     * @param dotType The dot type to use
     * @see DotType
     */
    public void setDotType(int dotType) {
        if (dotType == DotType.SINGLE || dotType == DotType.MULTIPLE) {
            if (mDotType != dotType) {
                mDotType = dotType;
                invalidate();
            }
        }
    }

    @Override
    public void requestLayout() {
        if (!mInitializing) {
            super.requestLayout();
        }
    }

    @Override
    public void invalidate() {
        if (!mInitializing) {
            super.invalidate();
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == mDotDrawable;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        mExtraState = onCreateDrawableState(1);
        mergeDrawableStates(mExtraState, SELECTED_STATE_SET);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        Drawable d = mDotDrawable;

        int width = 0;
        int height = 0;
        if (d != null) {
            width = mDotCount * (d.getIntrinsicWidth() + mDotSpacing) - mDotSpacing;
            height = d.getIntrinsicHeight();
        }

        width += getPaddingRight() + getPaddingLeft();
        height += getPaddingBottom() + getPaddingTop();

        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {

        final Drawable d = mDotDrawable;
        if (d != null) {

            final int count = mDotType == DotType.SINGLE ? mDotCount : mActiveDot;

            if (count <= 0) {
                return;
            }

            final int h = d.getIntrinsicHeight();
            final int w = Math.max(0, count * (d.getIntrinsicWidth() + mDotSpacing) - mDotSpacing);

            final int pRight = getPaddingRight();
            final int pLeft = getPaddingLeft();
            final int pTop = getPaddingTop();
            final int pBottom = getPaddingBottom();

            sInRect.set(pLeft, pTop, getWidth() - pRight, getHeight() - pBottom);
            Gravity.apply(mGravity, w, h, sInRect, sOutRect);

            canvas.save();
            canvas.translate(sOutRect.left, sOutRect.top);
            for (int i = 0; i < count; i++) {
                if (d.isStateful()) {
                    int[] state = getDrawableState();
                    if (mDotType == DotType.MULTIPLE || i == mActiveDot) {
                        state = mExtraState;
                    }
                    // HACK Cyril: The following code prevent the setState call
                    // from invalidating the View again (which will result in
                    // calling onDraw over and over again).
                    d.setCallback(null);
                    d.setState(state);
                    d.setCallback(this);
                }
                d.draw(canvas);
                canvas.translate(mDotSpacing + d.getIntrinsicWidth(), 0);
            }
            canvas.restore();
        }
    }

    static class SavedState extends BaseSavedState {

        int activeDot;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            activeDot = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(activeDot);
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

        ss.activeDot = mActiveDot;

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        mActiveDot = ss.activeDot;
    }
}
