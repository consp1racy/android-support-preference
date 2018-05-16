package net.xpece.android.colorpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.TintTypedArray;

final class Util {
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

    private Util() {
        throw new AssertionError();
    }

    @ColorInt
    @SuppressLint("RestrictedApi")
    static int resolveColor(final Context context, @AttrRes final int attr, @ColorInt final int fallback) {
        final int[] tempArray = getTempArray();
        tempArray[0] = attr;
        final TintTypedArray ta = TintTypedArray.obtainStyledAttributes(context, null, tempArray);
        try {
            return ta.getColor(0, fallback);
        } finally {
            ta.recycle();
        }
    }
}
