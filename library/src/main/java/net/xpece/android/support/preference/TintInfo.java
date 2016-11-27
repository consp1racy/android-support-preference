package net.xpece.android.support.preference;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.support.annotation.RestrictTo;

/**
 * @author Eugen on 6. 12. 2015.
 */
@RestrictTo(RestrictTo.Scope.GROUP_ID)
final class TintInfo {
    public ColorStateList mTintList;
    public PorterDuff.Mode mTintMode;
}
