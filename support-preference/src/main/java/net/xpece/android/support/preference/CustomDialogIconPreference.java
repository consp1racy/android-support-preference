package net.xpece.android.support.preference;

import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

/**
 * @author Eugen on 6. 12. 2015.
 */
public interface CustomDialogIconPreference {
    void setSupportDialogIcon(@Nullable Drawable icon);

    void setSupportDialogIcon(@DrawableRes int icon);

    @Nullable
    Drawable getSupportDialogIcon();

    boolean isSupportDialogIconPaddingEnabled();

    void setSupportDialogIconPaddingEnabled(boolean enabled);
}
