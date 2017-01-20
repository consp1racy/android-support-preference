package net.xpece.android.support.preference;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatDrawableManager;
import android.util.TypedValue;

/**
 * Created by Eugen on 13. 5. 2015.
 */
final class Util {
    public static final int[] DISABLED_STATE_SET = new int[]{-android.R.attr.state_enabled};
    public static final int[] EMPTY_STATE_SET = new int[0];

    public static final int[][] DISABLED_STATE_LIST = new int[][]{
        DISABLED_STATE_SET,
        EMPTY_STATE_SET
    };

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

    public static float resolveFloat(Context context, @AttrRes int attr, float fallback) {
        TEMP_ARRAY[0] = attr;
        TypedArray ta = context.obtainStyledAttributes(TEMP_ARRAY);
        try {
            return ta.getFloat(0, fallback);
        } finally {
            ta.recycle();
        }
    }

    public static ColorStateList withDisabled(int color, int disabledAlpha) {
        int disabledColor = ColorUtils.setAlphaComponent(color, disabledAlpha);
        return new ColorStateList(DISABLED_STATE_LIST, new int[]{disabledColor, color});
    }

    @Nullable
    public static Drawable getDrawableCompat(@NonNull final Context context, @DrawableRes final int resId) {
        try {
            return AppCompatResources.getDrawable(context, resId);
        } catch (NoSuchMethodError ex) {
            try {
                //noinspection RestrictedApi
                return AppCompatDrawableManager.get().getDrawable(context, resId);
            } catch (Exception ex2) {
                return ContextCompat.getDrawable(context, resId);
            }
        }
    }

    @NonNull
    public static ColorStateList getColorStateListCompat(@NonNull final Context context, @ColorRes final int resId) {
        try {
            return AppCompatResources.getColorStateList(context, resId);
        } catch (NoSuchMethodError ex) {
            return ContextCompat.getColorStateList(context, resId);
        }
    }
}
