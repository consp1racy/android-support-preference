package net.xpece.android.support.preference.sample;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.v7.widget.TintTypedArray;
import android.util.TypedValue;

/**
 * Created by Eugen on 13. 5. 2015.
 * @hide
 */
final class Util {
    private static final int[] TEMP_ARRAY = new int[1];

    private Util() {}

    public static float dpToPx(Context context, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int dpToPxOffset(Context context, int dp) {
        return (int) (dpToPx(context, dp));
    }

    public static int dpToPxSize(Context context, int dp) {
        return (int) (0.5f + dpToPx(context, dp));
    }

    public static int resolveResourceId(Context context, @AttrRes int attr, int fallback) {
        TEMP_ARRAY[0] = attr;
        TypedArray ta = context.obtainStyledAttributes(TEMP_ARRAY);
        try {
            return ta.getResourceId(0, fallback);
        } finally {
            ta.recycle();
        }
    }

    public static ColorStateList resolveColorStateList(Context context, @AttrRes int attr) {
        TEMP_ARRAY[0] = attr;
        TintTypedArray ta = TintTypedArray.obtainStyledAttributes(context, null, TEMP_ARRAY);
        try {
            return ta.getColorStateList(0);
        } finally {
            ta.recycle();
        }
    }
}
