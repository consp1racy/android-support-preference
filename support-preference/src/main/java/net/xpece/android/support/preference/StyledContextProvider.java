package net.xpece.android.support.preference;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.AnyRes;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v7.preference.XpPreferenceFragment;
import android.view.ContextThemeWrapper;

/**
 * Utilities that help you extract activity theme and apply it onto a long-lived context
 * such as the application context. This will prevent activity leaks when you use retained
 * preference fragments to save computing power while avoiding preference hierarchy
 * re-inflating on each configuration change.
 *
 * @see XpPreferenceFragment#onProvideCustomStyledContext()
 */
public final class StyledContextProvider {
    private StyledContextProvider() {
        throw new AssertionError();
    }

    /**
     * Provide application scoped context with a theme from supplied activity.
     * <p>
     * What this means:
     * <ul>
     * <li>keeping this context won't leak your activity instance,</li>
     * <li>views inflated using this context will look just like from the activity.</li>
     * </ul>
     * <p>
     * <i>Note:</i> The theme resource ID is taken from the manifest. It doesn't take into account
     * any changes you make to it at runtime, such as implementation of runtime theme switching.
     *
     * @see #getActivityThemeResource(Activity)
     */
    @NonNull
    public static ContextThemeWrapper getThemedApplicationContext(final @NonNull Activity activity) {
        final int activityThemeId = getActivityThemeResource(activity);
        final Context app = activity.getApplicationContext();
        return new ContextThemeWrapper(app, activityThemeId);
    }

    /**
     * Extract the theme resource ID of the activity.
     * <p>
     * <i>Note:</i> The theme resource ID is taken from the manifest. It doesn't take into account
     * any changes you make to it at runtime, such as implementation of runtime theme switching.
     * However you may use your own logic to extract that runtime theme using retrieved theme ID.
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
     */
    @AnyRes
    public static int resolveResourceId(final @NonNull Context context, @AttrRes final int attrId) {
        return Util.resolveResourceId(context, attrId, 0);
    }
}
