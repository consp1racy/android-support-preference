package net.xpece.android.support.preference;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Before Lollipop insets didn't count to intrinsic size. This class aims to fix this issue.
 * @author Eugen on 7. 12. 2015.
 */
@ParametersAreNonnullByDefault
final class XpInsetDrawable extends InsetDrawable {
    private static final boolean NEEDS_FIXING = Build.VERSION.SDK_INT < 21;

    private final Rect mInset = new Rect();

    public static InsetDrawable create(final Drawable drawable, final int insetLeft, final int insetTop, final int insetRight, final int insetBottom) {
        if (NEEDS_FIXING) {
            return new XpInsetDrawable(drawable, insetLeft, insetTop, insetRight, insetBottom);
        } else {
            return new InsetDrawable(drawable, insetLeft, insetTop, insetRight, insetBottom);
        }
    }

    public static InsetDrawable create(final Drawable drawable, final int inset) {
        if (NEEDS_FIXING) {
            return new XpInsetDrawable(drawable, inset);
        } else {
            return new InsetDrawable(drawable, inset);
        }
    }

    private XpInsetDrawable(final Drawable drawable, final int inset) {
        super(drawable, inset);
        mInset.set(inset, inset, inset, inset);
    }

    private XpInsetDrawable(final Drawable drawable, final int insetLeft, final int insetTop, final int insetRight, final int insetBottom) {
        super(drawable, insetLeft, insetTop, insetRight, insetBottom);
        mInset.set(insetLeft, insetTop, insetRight, insetBottom);
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
