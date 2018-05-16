package net.xpece.android.support.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.v7.widget.TintTypedArray;

import javax.annotation.ParametersAreNonnullByDefault;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

/**
 * Created by Eugen on 13. 5. 2015.
 *
 * @hide
 */
@RestrictTo(LIBRARY)
@SuppressLint("RestrictedApi")
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
    public static int resolveColor(Context context, @AttrRes int attr, @ColorInt int fallback) {
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
