package net.xpece.android.support.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.appcompat.widget.TintTypedArray;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import static androidx.annotation.Dimension.DP;
import static androidx.annotation.RestrictTo.Scope.LIBRARY;

/**
 * @hide
 */
@RestrictTo(LIBRARY)
@SuppressLint("RestrictedApi")
final class XpSpinnerUtil {
    private static final ThreadLocal<int[]> TEMP_ARRAY = new ThreadLocal<int[]>() {
        @Override
        protected int[] initialValue() {
            return new int[1];
        }
    };

    @NonNull
    private static int[] getTempArray() {
        return TEMP_ARRAY.get();
    }

    private XpSpinnerUtil() {
        throw new AssertionError();
    }

    @ColorInt
    public static int resolveColor(@NonNull Context context, @AttrRes int attr, @ColorInt int fallback) {
        final int[] tempArray = getTempArray();
        tempArray[0] = attr;
        final TintTypedArray ta = TintTypedArray.obtainStyledAttributes(context, null, tempArray);
        try {
            return ta.getColor(0, fallback);
        } finally {
            ta.recycle();
        }
    }

    @Dimension
    private static float resolveDimension(
            @NonNull Context context, @AttrRes int attr, @Dimension float fallback) {
        final int[] tempArray = getTempArray();
        tempArray[0] = attr;
        TypedArray ta = context.obtainStyledAttributes(tempArray);
        try {
            return ta.getDimension(0, fallback);
        } finally {
            ta.recycle();
        }
    }

    @Dimension
    static int resolveDimensionPixelSize(
            @NonNull Context context, @AttrRes int attr, @Dimension int fallback) {
        float dimen = resolveDimension(context, attr, fallback);
        return (int) (dimen + 0.5f);
    }

    @Dimension
    private static float dpToPx(@NonNull Context context, @Dimension(unit = DP) int dp) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

    @Dimension
    static int dpToPxOffset(@NonNull Context context, @Dimension(unit = DP) int dp) {
        return (int) (dpToPx(context, dp));
    }
}
