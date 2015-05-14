package net.xpece.android.support.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;

/**
 * Created by Eugen on 13. 5. 2015.
 */
class Util {
    private Util() {}

    public static Drawable resolveDrawable(Context context, @AttrRes int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        Drawable d = ta.getDrawable(0);
        ta.recycle();
        return d;
    }
}
