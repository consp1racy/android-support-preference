package net.xpece.android.support.preference;

import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;

/**
 * @author Eugen on 6. 12. 2015.
 */
public interface CustomIconPreference {
    void setSupportIcon(@Nullable Drawable icon);

    void setSupportIcon(@DrawableRes int icon);

    @Nullable
    Drawable getSupportIcon();

    boolean isSupportIconPaddingEnabled();

    void setSupportIconPaddingEnabled(boolean enabled);
}
