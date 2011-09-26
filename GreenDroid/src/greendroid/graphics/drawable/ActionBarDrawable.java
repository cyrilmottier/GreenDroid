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
package greendroid.graphics.drawable;

import greendroid.widget.ActionBar;
import greendroid.widget.ActionBarItem;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.StateSet;
import android.util.TypedValue;

import com.cyrilmottier.android.greendroid.R;

/**
 * <p>
 * A specialized Drawable dedicated to {@link ActionBarItem}s. It automatically
 * adapts its color depending on its current state. By default, the colors are:
 * </p>
 * <ul>
 * <li>Color.BLACK in alternative (pressed/focused)</li>
 * <li>Color.WHITE in otherwise</li>
 * </ul>
 * <p>
 * The ActionBarDrawable is a great replacement to the StateListDrawable that
 * should be used in {@link ActionBar}s.
 * </p>
 * <p>
 * GreenDroid offers a smart way to change the default alternative and normal
 * colors in an application-wide manner. In order to do that, override the
 * {@link R.attr#gdActionBarItemColorNormal} and
 * {@link R.attr#gdActionBarItemColorAlt} attributes in your application theme.
 * </p>
 * 
 * @see R.attr#gdActionBarItemColorNormal
 * @see R.attr#gdActionBarItemColorAlt
 * @author Cyril Mottier
 */
public class ActionBarDrawable extends BitmapDrawable {

    private static final TypedValue sTypedValue = new TypedValue();

    private ColorFilter mNormalCf;
    private ColorFilter mAltCf;

    /**
     * Create a new ActionBarDrawable
     * 
     * @param res The Resources from which the given icon is retrieved
     * @param resId The icon's resource ID
     * @deprecated Use {@link ActionBarDrawable#ActionBarDrawable(Context, int)}
     *             as it looks for the default alternative/normal colors in the
     *             theme.
     */
    @Deprecated
    public ActionBarDrawable(Resources res, int resId) {
        this(res, res.getDrawable(resId));
    }

    /**
     * Create a new ActionBarDrawable
     * 
     * @param res The Resources from which the given icon is retrieved
     * @param d The icon's Drawable
     * @deprecated Use
     *             {@link ActionBarDrawable#ActionBarDrawable(Context, Drawable)}
     *             as it looks for the default alternative/normal colors in the
     *             theme.
     */
    @Deprecated
    public ActionBarDrawable(Resources res, Drawable d) {
        this(res, d, Color.WHITE, Color.BLACK);
    }

    /**
     * Create a new ActionBarDrawable
     * 
     * @param res The Resources from which the given icon is retrieved
     * @param resId The icon's resource ID
     * @param normalColor The color used to color the icon in normal mode
     * @param altColor The color used to color the icon in alternative mode
     * @deprecated Use
     *             {@link ActionBarDrawable#ActionBarDrawable(Context, int, int, int)}
     *             as it looks for the default alternative/normal colors in the
     *             theme.
     */
    @Deprecated
    public ActionBarDrawable(Resources res, int resId, int normalColor, int altColor) {
        // TODO Cyril: Remove this constructor or the similar Context-based one.
        // They are actually the same ...
        this(res, res.getDrawable(resId), normalColor, altColor);
    }

    /**
     * Create a new ActionBarDrawable
     * 
     * @param res The Resources from which the given icon is retrieved
     * @param d The icon's Drawable
     * @param normalColor The color used to color the icon in normal mode
     * @param altColor The color used to color the icon in alternative mode
     * @deprecated Use
     *             {@link ActionBarDrawable#ActionBarDrawable(Context, Drawable, int, int)}
     *             as it looks for the default alternative/normal colors in the
     *             theme.
     */
    @Deprecated
    public ActionBarDrawable(Resources res, Drawable d, int normalColor, int altColor) {
        // TODO Cyril: Remove this constructor or the similar Context-based one.
        // They are actually the same ...
        super(res, (d instanceof BitmapDrawable) ? ((BitmapDrawable) d).getBitmap() : null);
        mNormalCf = new LightingColorFilter(Color.BLACK, normalColor);
        mAltCf = new LightingColorFilter(Color.BLACK, altColor);
    }

    /**
     * Create a new ActionBarDrawable using the specified resource identifier.
     * 
     * @param context The Context used to retrieve resources (Bitmap/theme)
     * @param resId The resource identifier pointing to the icon's Bitmap
     */
    public ActionBarDrawable(Context context, int resId) {
        this(context, context.getResources().getDrawable(resId));
    }

    /**
     * Create a new ActionBarDrawable using the specified Drawable.
     * 
     * @param context The Context used to retrieve resources (Bitmap/theme)
     * @param d The icon's Drawable (should be a BitmapDrawable)
     */
    public ActionBarDrawable(Context context, Drawable d) {
        // TODO Cyril: Should use a Bitmap instead of a Drawable ...
        this(context, d, getColorFromTheme(context, R.attr.gdActionBarItemColorNormal, Color.WHITE), getColorFromTheme(context,
                R.attr.gdActionBarItemColorAlt, Color.BLACK));
    }

    /**
     * Create a new ActionBarDrawable using the specified resource identifier.
     * 
     * @param context The Context used to retrieve resources (Bitmap/theme)
     * @param resId The resource identifier pointing to the icon's Bitmap
     * @param normalColor The color used to color the icon in normal mode
     * @param altColor The color used to color the icon in alternative mode
     */
    public ActionBarDrawable(Context context, int resId, int normalColor, int altColor) {
        this(context, context.getResources().getDrawable(resId), normalColor, altColor);
    }

    /**
     * Create a new ActionBarDrawable using the specified Drawable.
     * 
     * @param context The Context used to retrieve resources (Bitmap/theme)
     * @param d The icon's Drawable (should be a BitmapDrawable)
     * @param normalColor The color used to color the icon in normal mode
     * @param altColor The color used to color the icon in alternative mode
     */
    public ActionBarDrawable(Context context, Drawable d, int normalColor, int altColor) {
        // TODO Cyril: Should use a Bitmap instead of a Drawable ...
        super(context.getResources(), (d instanceof BitmapDrawable) ? ((BitmapDrawable) d).getBitmap() : null);
        mNormalCf = new LightingColorFilter(Color.BLACK, normalColor);
        mAltCf = new LightingColorFilter(Color.BLACK, altColor);
    }

    private static int getColorFromTheme(Context context, int attr, int defaultColor) {
        synchronized (sTypedValue) {
            final TypedValue value = sTypedValue;
            final Theme theme = context.getTheme();
            if (theme != null) {
                theme.resolveAttribute(attr, value, true);
                if (value.type >= TypedValue.TYPE_FIRST_INT && value.type <= TypedValue.TYPE_LAST_INT) {
                    return value.data;
                }
            }

            return defaultColor;
        }
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        final boolean useAlt = StateSet.stateSetMatches(DrawableStateSet.ENABLED_PRESSED_STATE_SET, stateSet)
                || StateSet.stateSetMatches(DrawableStateSet.ENABLED_FOCUSED_STATE_SET, stateSet);
        setColorFilter(useAlt ? mAltCf : mNormalCf);
        return true;
    }
}
