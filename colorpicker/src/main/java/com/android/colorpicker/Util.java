package com.android.colorpicker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;

/**
 * @author Eugen on 25. 3. 2016.
 */
@Deprecated
final class Util {
    private static final int[] TEMP_ARRAY = new int[1];

    private Util() {}

    public static ColorStateList resolveColorStateList(Context context, @AttrRes int attr) {
        TEMP_ARRAY[0] = attr;
        TypedArray ta = context.obtainStyledAttributes(TEMP_ARRAY);
        try {
            return ta.getColorStateList(0);
        } finally {
            ta.recycle();
        }
    }

    @ColorInt
    public static int resolveColor(Context context, @AttrRes int attr, @ColorInt int fallback) {
        TEMP_ARRAY[0] = attr;
        TypedArray ta = context.obtainStyledAttributes(TEMP_ARRAY);
        try {
            return ta.getColor(0, fallback);
        } finally {
            ta.recycle();
        }
    }

}
