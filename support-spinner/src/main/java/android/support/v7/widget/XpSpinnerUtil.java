package android.support.v7.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.Dimension;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import javax.annotation.ParametersAreNonnullByDefault;

import static android.support.annotation.Dimension.DP;
import static android.support.annotation.RestrictTo.Scope.LIBRARY;

/**
 * @hide
 */
@ParametersAreNonnullByDefault
@RestrictTo(LIBRARY)
@SuppressLint("RestrictedApi")
final class XpSpinnerUtil {
    private static final ThreadLocal<int[]> TEMP_ARRAY = new ThreadLocal<int[]>() {
        @Override
        protected int[] initialValue() {
            return new int[1];
        }
    };

    private XpSpinnerUtil() {
        throw new AssertionError();
    }

    @NonNull
    private static int[] getTempArray() {
        return TEMP_ARRAY.get();
    }

    @Dimension
    private static float resolveDimension(
            Context context, @AttrRes int attr, @Dimension float fallback) {
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
            Context context, @AttrRes int attr, @Dimension int fallback) {
        float dimen = resolveDimension(context, attr, fallback);
        return (int) (dimen + 0.5f);
    }

    @Dimension
    private static float dpToPx(Context context, @Dimension(unit = DP) int dp) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

    @Dimension
    static int dpToPxOffset(Context context, @Dimension(unit = DP) int dp) {
        return (int) (dpToPx(context, dp));
    }

    @Dimension
    static int dpToPxSize(Context context, @Dimension(unit = DP) int dp) {
        return (int) (0.5f + dpToPx(context, dp));
    }
}
