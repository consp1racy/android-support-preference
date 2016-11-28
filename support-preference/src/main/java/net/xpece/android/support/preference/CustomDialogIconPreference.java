package net.xpece.android.support.preference;

import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;

/**
 * @author Eugen on 6. 12. 2015.
 */
public interface CustomDialogIconPreference {
    void setSupportDialogIcon(Drawable icon);

    void setSupportDialogIcon(@DrawableRes int icon);

    Drawable getSupportDialogIcon();
}
