package net.xpece.android.colorpicker;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.v7.widget.TintTypedArray;

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
        TintTypedArray ta = TintTypedArray.obtainStyledAttributes(context, null, TEMP_ARRAY);
        try {
            return ta.getColor(0, fallback);
        } finally {
            ta.recycle();
        }
    }
}
