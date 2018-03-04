package android.support.v7.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.Dimension;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import static android.support.annotation.Dimension.DP;

@RestrictTo(RestrictTo.Scope.LIBRARY)
final class Util {
    private static final ThreadLocal<int[]> TEMP_ARRAY = new ThreadLocal<int[]>() {
        @Override
        protected int[] initialValue() {
            return new int[]{0};
        }
    };

    private Util() {
        throw new AssertionError();
    }

    private static int[] getTempArray() {
        return TEMP_ARRAY.get();
    }

    @Dimension
    public static float resolveDimension(
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
    public static int resolveDimensionPixelSize(
            @NonNull Context context, @AttrRes int attr, @Dimension int fallback) {
        float dimen = resolveDimension(context, attr, fallback);
        return (int) (dimen + 0.5f);
    }

    @Dimension
    public static float dpToPx(@NonNull Context context, @Dimension(unit = DP) int dp) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

    @Dimension
    public static int dpToPxOffset(@NonNull Context context, @Dimension(unit = DP) int dp) {
        return (int) (dpToPx(context, dp));
    }

    @Dimension
    public static int dpToPxSize(@NonNull Context context, @Dimension(unit = DP) int dp) {
        return (int) (0.5f + dpToPx(context, dp));
    }
}
