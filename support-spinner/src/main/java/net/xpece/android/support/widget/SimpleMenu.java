package net.xpece.android.support.widget;

import android.support.annotation.IntDef;
import android.support.annotation.RestrictTo;
import android.support.v7.widget.AbstractXpListPopupWindow;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static net.xpece.android.support.widget.SimpleMenu.MaxWidth.FIT_ANCHOR;
import static net.xpece.android.support.widget.SimpleMenu.MaxWidth.FIT_SCREEN;
import static net.xpece.android.support.widget.SimpleMenu.WidthMode.MATCH_CONSTRAINT;
import static net.xpece.android.support.widget.SimpleMenu.WidthMode.WRAP_CONTENT;
import static net.xpece.android.support.widget.SimpleMenu.WidthMode.WRAP_CONTENT_UNIT;

/**
 * Class containing constants related to simple menu.
 *
 * Library consumers don't need to access this.
 */
@RestrictTo(LIBRARY_GROUP)
public abstract class SimpleMenu {
    private SimpleMenu() {
        throw new AssertionError("No instances!");
    }

    /**
     * @hide
     */
    @IntDef({FIT_ANCHOR, FIT_SCREEN})
    @RestrictTo(LIBRARY_GROUP)
    @Retention(RetentionPolicy.SOURCE)
    public @interface MaxWidth {
        /** Popup menu width is limited by screen width. */
        int FIT_SCREEN = AbstractXpListPopupWindow.MAX_WIDTH_FIT_SCREEN;
        /** Popup menu width is limited by anchor width. */
        int FIT_ANCHOR = AbstractXpListPopupWindow.MAX_WIDTH_FIT_ANCHOR;
    }

    /**
     * @hide
     */
    @IntDef({MATCH_CONSTRAINT, WRAP_CONTENT, WRAP_CONTENT_UNIT})
    @RestrictTo(LIBRARY_GROUP)
    @Retention(RetentionPolicy.SOURCE)
    public @interface WidthMode {
        /** Popup menu width is only limited by {@code maxWidth}. */
        int MATCH_CONSTRAINT = AbstractXpListPopupWindow.WIDTH_MATCH_CONSTRAINT;
        /**
         * Popup menu width is
         * * at least as wide as its content,
         * * limited by {@code maxWidth}.
         */
        int WRAP_CONTENT = AbstractXpListPopupWindow.WIDTH_WRAP_CONTENT;
        /**
         * Popup menu width is
         * <ul>
         * <li>at least as wide as its content rounded up to a multiple of {@code widthUnit},</li>
         * <li>at least as wide as {@code widthUnit * 1.5},</li>
         * <li>limited by {@code maxWidth}.</li>
         * </ul>
         */
        int WRAP_CONTENT_UNIT = AbstractXpListPopupWindow.WIDTH_WRAP_CONTENT_UNIT;
    }
}
