package net.xpece.android.support.widget;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.widget.AbstractXpListPopupWindow;
import android.util.AttributeSet;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @see android.support.v7.widget.ListPopupWindow
 * @see android.widget.ListPopupWindow
 */
@ParametersAreNonnullByDefault
public class XpListPopupWindow extends AbstractXpListPopupWindow {
    public XpListPopupWindow(final Context context) {
        super(context);
    }

    public XpListPopupWindow(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public XpListPopupWindow(
            final Context context,
            @Nullable final AttributeSet attrs,
            @AttrRes final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public XpListPopupWindow(
            final Context context,
            @Nullable final AttributeSet attrs,
            @AttrRes final int defStyleAttr,
            @StyleRes final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
