package net.xpece.android.support.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;

/**
 * Created by Eugen on 13. 5. 2015.
 * @hide
 */
final class Util {
    private static final int[] TEMP_ARRAY = new int[1];

    private Util() {}

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
