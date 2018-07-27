package net.xpece.android.support.preference;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v7.preference.Preference;
import android.support.v7.preference.XpPreferenceCompat;

import java.util.Set;

/**
 * This class provides methods that can be used to retrieve and store String sets
 * from and to Preferences even on Android 2.x.
 * <p>
 * String sets were stored as JSON array on Android 2.x and these methods are required
 * to upgrade JSON to String set once the user runs the app after upgrading to a newer
 * version of Android.
 * <p>
 * If your app never supported Android 2.x or you never used
 * {@link MultiSelectListPreference} on Android 2.x
 * you don't need these methods.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("deprecation")
public final class XpPreference {

    /**
     * Attempts to persist a set of Strings to the {@link SharedPreferences}.
     * <p></p>
     * This will check if this Preference is persistent, get an editor from
     * the {@link android.preference.PreferenceManager}, put in the strings,
     * and check if we should commit (and commit if so).
     *
     * @param values The values to persist.
     * @return True if the Preference is persistent. (This is not whether the
     * value was persisted, since we may not necessarily commit if there
     * will be a batch commit later.)
     * @see #getPersistedStringSet(Preference, Set)
     */
    public static boolean persistStringSet(@NonNull Preference preference, @NonNull Set<String> values) {
        return XpPreferenceCompat.persistStringSet(preference, values);
    }

    /**
     * Attempts to get a persisted set of Strings from the
     * {@link SharedPreferences}.
     * <p></p>
     * This will check if this Preference is persistent, get the SharedPreferences
     * from the {@link android.preference.PreferenceManager}, and get the value.
     *
     * @param defaultReturnValue The default value to return if either the
     * Preference is not persistent or the Preference is not in the
     * shared preferences.
     * @return The value from the SharedPreferences or the default return
     * value.
     * @see #persistStringSet(Preference, Set)
     */
    @Nullable
    public static Set<String> getPersistedStringSet(@NonNull Preference preference, @Nullable Set<String> defaultReturnValue) {
        return XpPreferenceCompat.getPersistedStringSet(preference, defaultReturnValue);
    }

    private XpPreference() {
        throw new AssertionError();
    }
}
