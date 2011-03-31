package greendroid.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;

import com.cyrilmottier.android.greendroid.R;
import com.google.android.maps.MapView;

/**
 * A {@link Drawable} dedicated to {@link MapView}s. A {@link MapPinDrawable}
 * displays a rounded pin with a dot in the middle. This class lets you easily
 * change the color of the pin as well as the dot in the center of the pin.
 * 
 * @author Cyril Mottier
 */
public class MapPinDrawable extends Drawable {

    private static final int COLOR_MODE_UNKNOWN = -1;
    private static final int COLOR_MODE_COLOR = 1;
    private static final int COLOR_MODE_COLOR_STATE_LIST = 2;

    private static final Paint sClearerPaint;

    static {
        sClearerPaint = new Paint();
        sClearerPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    private int mColorMode = COLOR_MODE_UNKNOWN;
    private int mPinColor;
    private int mDotColor;
    private ColorStateList mPinColorStateList;
    private ColorStateList mDotColorStateList;

    private int mCurrentPinColor;
    private int mCurrentDotColor;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mMapPinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Bitmap mBase;
    private Bitmap mPin;
    private Bitmap mDot;
    private Bitmap mMapPin;

    private boolean mNeedRebuild;

    /**
     * Create a new {@link MapPinDrawable} that has a single color.
     * 
     * @param res The application resources
     * @param color The color of the pin/dot
     */
    public MapPinDrawable(Resources res, int color) {
        this(res, color, color);
    }

    /**
     * Create a new {@link MapPinDrawable}.
     * 
     * @param res The application resources
     * @param pinClor The color of the pin
     * @param dotColor The color of the dot
     */
    public MapPinDrawable(Resources res, int pinColor, int dotColor) {
        initBitmaps(res);
        setColors(pinColor, dotColor);
    }

    /**
     * Create a new {@link MapPinDrawable} that may change color depending on
     * its current state.
     * 
     * @param res The application resources
     * @param color A {@link ColorStateList} object giving a set of colors
     *            changing depending on the current {@link Drawable}'s state
     */
    public MapPinDrawable(Resources res, ColorStateList color) {
        this(res, color, color);
    }

    /**
     * Create a new {@link MapPinDrawable} that may change color depending on
     * its current state.
     * 
     * @param res The application resources
     * @param pinColor A {@link ColorStateList} object giving a set of colors
     *            for the pin changing depending on the current {@link Drawable}
     *            's state
     * @param dotColor A {@link ColorStateList} object giving a set of colors
     *            for the dot changing depending on the current {@link Drawable}
     *            's state
     */
    public MapPinDrawable(Resources res, ColorStateList pinColor, ColorStateList dotColor) {
        initBitmaps(res);
        setColors(pinColor, dotColor);
    }

    private void initBitmaps(Resources res) {
        // TODO Cyril: Share those Bitmaps between all instances of
        // MapPinDrawable in order to save memory
        mBase = BitmapFactory.decodeResource(res, R.drawable.gd_map_pin_base);
        mPin = BitmapFactory.decodeResource(res, R.drawable.gd_map_pin_pin);
        mDot = BitmapFactory.decodeResource(res, R.drawable.gd_map_pin_dot);
    }

    /**
     * Set the color for the pin/dot
     * 
     * @param pinClor The color of the pin
     * @param dotColor The color of the dot
     */
    public void setColors(int pinColor, int dotColor) {
        if (mColorMode != COLOR_MODE_COLOR || mPinColor != pinColor || mDotColor != dotColor) {
            mColorMode = COLOR_MODE_COLOR;
            mPinColor = mCurrentPinColor = pinColor;
            mDotColor = mCurrentDotColor = dotColor;
            mNeedRebuild = true;
        }
    }

    /**
     * Set the color for the pin/dot
     * 
     * @param pinClor The color of the pin
     * @param dotColor The color of the dot
     */
    public void setColors(ColorStateList pinColor, ColorStateList dotColor) {
        if (mColorMode != COLOR_MODE_COLOR_STATE_LIST || mPinColorStateList != pinColor || mDotColorStateList != dotColor) {
            mColorMode = COLOR_MODE_COLOR_STATE_LIST;
            mPinColorStateList = pinColor;
            mDotColorStateList = dotColor;
            mNeedRebuild = true;
        }
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    public int getIntrinsicWidth() {
        return (mBase != null) ? mBase.getWidth() : -1;
    }

    @Override
    public int getIntrinsicHeight() {
        return (mBase != null) ? mBase.getHeight() : -1;
    }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        if (mColorMode == COLOR_MODE_COLOR_STATE_LIST) {
            int pinColor = (mPinColorStateList != null) ? mPinColorStateList.getColorForState(stateSet, Color.BLACK) : Color.BLACK;
            int dotColor = (mDotColorStateList != null) ? mDotColorStateList.getColorForState(stateSet, Color.BLACK) : Color.BLACK;
            if (mCurrentPinColor != pinColor || mCurrentDotColor != dotColor) {
                mCurrentPinColor = pinColor;
                mCurrentDotColor = dotColor;
                mNeedRebuild = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public void draw(Canvas canvas) {
        if (mNeedRebuild) {

            if (mMapPin == null) {
                mMapPin = Bitmap.createBitmap(mBase.getWidth(), mBase.getHeight(), Bitmap.Config.ARGB_8888);
            }
            Canvas c = new Canvas(mMapPin);
            c.drawRect(0, 0, mMapPin.getWidth(), mMapPin.getHeight(), sClearerPaint);

            // 1 - Draw the base
            c.drawBitmap(mBase, 0, 0, null);
            // 2 - Draw the pin on top of it
            mPaint.setColorFilter(new LightingColorFilter(Color.BLACK, mCurrentPinColor));
            c.drawBitmap(mPin, 0, 0, mPaint);
            // 3 - Draw the dot on top of everything
            mPaint.setColorFilter(new LightingColorFilter(Color.BLACK, mCurrentDotColor));
            c.drawBitmap(mDot, 0, 0, mPaint);

            mNeedRebuild = false;
        }

        canvas.drawBitmap(mMapPin, null, getBounds(), mMapPinPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mMapPinPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mMapPinPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
