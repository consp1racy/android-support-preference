package android.support.v7.preference;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import net.xpece.android.support.preference.SharedPreferencesCompat;

import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * This class provides methods that can be used to retrieve and store String sets
 * from and to Preferences even on Android 2.x.
 *
 * String sets were stored as JSON array on Android 2.x and these methods are required
 * to upgrade JSON to String set once the user runs the app after upgrading to a newer
 * version of Android.
 *
 * If your app never supported Android 2.x or you never used
 * {@link net.xpece.android.support.preference.MultiSelectListPreference} on Android 2.x
 * you don't need these methods.
 */
@ParametersAreNonnullByDefault
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class XpPreferenceCompat {

    public XpPreferenceCompat() {
        throw new AssertionError();
    }

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
    public static boolean persistStringSet(Preference preference, Set<String> values) {
        //noinspection ConstantConditions
        if (values == null) {
            throw new IllegalArgumentException("Cannot persist null string set.");
        }

        if (!preference.shouldPersist()) {
            return false;
        }

        try {
            // Shouldn't store null
            if (values.equals(getPersistedStringSet(preference, null))) {
                // It's already there, so the same as persisting
                return true;
            }
        } catch (ClassCastException ignore) {
            // We were checking for equality or null. We got a different type. Overwrite that.
        }

        PreferenceDataStore dataStore = preference.getPreferenceDataStore();
        if (dataStore != null) {
            dataStore.putStringSet(preference.getKey(), values);
        } else {
            SharedPreferences.Editor editor = preference.getPreferenceManager().getEditor();
            editor.putStringSet(preference.getKey(), values);
            tryCommit(preference, editor);
        }
        return true;
    }

    /**
     * Attempts to get a persisted set of Strings from the
     * {@link SharedPreferences}.
     * <p></p>
     * This will check if this Preference is persistent, get the SharedPreferences
     * from the {@link android.preference.PreferenceManager}, and get the value.
     *
     * @param defaultReturnValue The default value to return if either the
     *                           Preference is not persistent or the Preference is not in the
     *                           shared preferences.
     * @return The value from the SharedPreferences or the default return
     * value.
     * @see #persistStringSet(Preference, Set)
     */
    @Nullable
    public static Set<String> getPersistedStringSet(Preference preference, @Nullable Set<String> defaultReturnValue) {
        if (!preference.shouldPersist()) {
            return defaultReturnValue;
        }

        PreferenceDataStore dataStore = preference.getPreferenceDataStore();
        if (dataStore != null) {
            return dataStore.getStringSet(preference.getKey(), defaultReturnValue);
        }

        return SharedPreferencesCompat.getStringSet(preference.getSharedPreferences(), preference.getKey(), defaultReturnValue);
    }

    private static void tryCommit(Preference preference, SharedPreferences.Editor editor) {
        if (preference.getPreferenceManager().shouldCommit()) {
            editor.apply();
        }
    }
}
