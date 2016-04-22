package net.xpece.android.support.widget;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.AbstractXpAppCompatSpinner;
import android.util.AttributeSet;

/**
 * @author Eugen on 22. 4. 2016.
 */
public class XpAppCompatSpinner extends AbstractXpAppCompatSpinner {
    public XpAppCompatSpinner(final Context context) {
        super(context);
    }

    public XpAppCompatSpinner(final Context context, final int mode) {
        super(context, mode);
    }

    public XpAppCompatSpinner(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public XpAppCompatSpinner(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public XpAppCompatSpinner(final Context context, final AttributeSet attrs, final int defStyleAttr, final int mode) {
        super(context, attrs, defStyleAttr, mode);
    }

    public XpAppCompatSpinner(final Context context, final AttributeSet attrs, final int defStyleAttr, final int mode, final Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, mode, popupTheme);
    }
}
