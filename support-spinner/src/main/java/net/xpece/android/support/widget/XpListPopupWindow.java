package net.xpece.android.support.widget;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;

/**
 * @see android.support.v7.widget.ListPopupWindow
 * @see android.widget.ListPopupWindow
 */
public class XpListPopupWindow extends AbstractXpListPopupWindow {
    public XpListPopupWindow(@NonNull final Context context) {
        super(context);
    }

    public XpListPopupWindow(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public XpListPopupWindow(
            @NonNull final Context context,
            @Nullable final AttributeSet attrs,
            @AttrRes final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public XpListPopupWindow(
            @NonNull final Context context,
            @Nullable final AttributeSet attrs,
            @AttrRes final int defStyleAttr,
            @StyleRes final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
