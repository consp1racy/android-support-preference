package net.xpece.android.support.widget;

import android.content.Context;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import android.util.AttributeSet;

/**
 * @see androidx.appcompat.widget.ListPopupWindow
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
