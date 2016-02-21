package net.xpece.android.support.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.StyleRes;
import android.util.TypedValue;

/**
 * Created by Eugen on 13. 5. 2015.
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

    public static float resolveDimension(Context context, @AttrRes int attr, float fallback) {
        TEMP_ARRAY[0] = attr;
        TypedArray ta = context.obtainStyledAttributes(TEMP_ARRAY);
        try {
            return ta.getDimension(0, fallback);
        } finally {
            ta.recycle();
        }
    }

    public static int resolveDimensionPixelOffset(Context context, @AttrRes int attr, int fallback) {
        float dimen = resolveDimension(context, attr, fallback);
        return (int) (dimen);
    }

    public static int resolveDimensionPixelSize(Context context, @AttrRes int attr, int fallback) {
        float dimen = resolveDimension(context, attr, fallback);
        return (int) (dimen + 0.5f);
    }

    public static float resolveDimension(Context context, @StyleRes int style, @AttrRes int attr, float fallback) {
        TEMP_ARRAY[0] = attr;
        TypedArray ta = context.obtainStyledAttributes(style, TEMP_ARRAY);
        try {
            return ta.getDimension(0, fallback);
        } finally {
            ta.recycle();
        }
    }

    public static int resolveDimensionPixelOffset(Context context, @StyleRes int style, @AttrRes int attr, int fallback) {
        float dimen = resolveDimension(context, style, attr, fallback);
        return (int) (dimen);
    }

    public static int resolveDimensionPixelSize(Context context, @StyleRes int style, @AttrRes int attr, int fallback) {
        float dimen = resolveDimension(context, style, attr, fallback);
        return (int) (dimen + 0.5f);
    }

}
