package net.xpece.android.support.preference;

import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;

/**
 * @author Eugen on 6. 12. 2015.
 */
public interface CustomIconPreference {
    void setSupportIcon(Drawable icon);

    void setSupportIcon(@DrawableRes int icon);

    Drawable getSupportIcon();

    boolean isSupportIconPaddingEnabled();

    void setSupportIconPaddingEnabled(boolean enabled);
}
