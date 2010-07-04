package com.cyrilmottier.android.gdcatalog.util;

import android.graphics.Color;

public class ColorUtils {
    
    public static int negativeColor(int color) {
        return Color.argb(Color.alpha(color), 255 - Color.red(color), 255 - Color.green(color), 255 - Color.blue(color));
    }

}
