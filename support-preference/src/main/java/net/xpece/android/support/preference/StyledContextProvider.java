package net.xpece.android.support.preference;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.ContextThemeWrapper;

import androidx.annotation.AnyRes;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

/**
 * Utilities that help you extract activity theme and apply it onto a long-lived context
 * such as the application context. This will prevent activity leaks when you use retained
 * preference fragments to save computing power while avoiding preference hierarchy
 * re-inflating on each configuration change.
 *
 * @see XpPreferenceFragment#onProvideCustomStyledContext()
 * @deprecated No longer useful.
 */
@Deprecated
public final class StyledContextProvider {
    private StyledContextProvider() {
        throw new AssertionError();
    }

    /**
     * @deprecated No longer useful.
     */
    @NonNull
    public static ContextThemeWrapper getThemedApplicationContext(final @NonNull Activity activity) {
        return activity;
    }

    /**
     * Extract the theme resource ID of the activity.
     * <p>
     * <i>Note:</i> The theme resource ID is taken from the manifest. It doesn't take into account
     * any changes you make to it at runtime, such as implementation of runtime theme switching.
     * However you may use your own logic to extract that runtime theme using retrieved theme ID.
     *
     * @deprecated No longer useful.
     */
    @StyleRes
    public static int getActivityThemeResource(final @NonNull Activity activity) {
        try {
            return activity.getPackageManager()
                    .getActivityInfo(activity.getComponentName(), 0)
                    .getThemeResource();
        } catch (PackageManager.NameNotFoundException ignore) {
            // This should never happen.
            throw new RuntimeException(ignore);
        }
    }

    /**
     * Returns the resource ID referenced by the supplied attribute in supplied context.
     * <p>
     * You can use this e.g. to extract a runtime theme resource ID from your activity theme.
     *
     * @return Resource ID, if found, {@code 0} otherwise.
     * @deprecated No longer useful.
     */
    @AnyRes
    public static int resolveResourceId(final @NonNull Context context, @AttrRes final int attrId) {
        return Util.resolveResourceId(context, attrId, 0);
    }
}
