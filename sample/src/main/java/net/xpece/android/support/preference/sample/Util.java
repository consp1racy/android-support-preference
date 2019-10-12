package net.xpece.android.support.preference.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import androidx.annotation.AnyRes;
import androidx.annotation.AttrRes;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.TintTypedArray;
import android.util.TypedValue;

import static androidx.annotation.Dimension.DP;

/**
 * Created by Eugen on 13. 5. 2015.
 * @hide
 */
@SuppressLint("RestrictedApi")
final class Util {
    private static final int[] TEMP_ARRAY = new int[1];

    private Util() {}

    @Dimension
    public static float dpToPx(@NonNull Context context, @Dimension(unit = DP) int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    @Dimension
    public static int dpToPxOffset(@NonNull Context context, @Dimension(unit = DP) int dp) {
        return (int) (dpToPx(context, dp));
    }

    @Dimension
    public static int dpToPxSize(@NonNull Context context, @Dimension(unit = DP) int dp) {
        return (int) (0.5f + dpToPx(context, dp));
    }

    @AnyRes
    public static int resolveResourceId(@NonNull Context context, @AttrRes int attr, @AnyRes int fallback) {
        TEMP_ARRAY[0] = attr;
        TypedArray ta = context.obtainStyledAttributes(TEMP_ARRAY);
        try {
            return ta.getResourceId(0, fallback);
        } finally {
            ta.recycle();
        }
    }

    @Nullable
    public static ColorStateList resolveColorStateList(@NonNull Context context, @AttrRes int attr) {
        TEMP_ARRAY[0] = attr;
        TintTypedArray ta = TintTypedArray.obtainStyledAttributes(context, null, TEMP_ARRAY);
        try {
            return ta.getColorStateList(0);
        } finally {
            ta.recycle();
        }
    }
}
