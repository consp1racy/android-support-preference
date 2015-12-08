package net.xpece.android.support.preference;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;

/**
 * @author Eugen on 7. 12. 2015.
 */
class InsetDrawable2 extends InsetDrawable {

    private final Rect mInset = new Rect();

    public InsetDrawable2(final Drawable drawable, final int inset) {
        this(drawable, inset, inset, inset, inset);
    }

    public InsetDrawable2(final Drawable drawable, final int insetLeft, final int insetTop, final int insetRight, final int insetBottom) {
        super(drawable, insetLeft, insetTop, insetRight, insetBottom);
        if (Build.VERSION.SDK_INT < 21) {
            mInset.set(insetLeft, insetTop, insetRight, insetBottom);
        }
    }

    @Override
    public int getIntrinsicHeight() {
        return super.getIntrinsicHeight() + mInset.top + mInset.bottom;
    }

    @Override
    public int getIntrinsicWidth() {
        return super.getIntrinsicWidth() + mInset.left + mInset.right;
    }
}
