package net.xpece.android.support.preference;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDataStore;
import android.support.v7.preference.PreferenceManager;

import java.lang.reflect.Method;
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
public final class XpPreference {

    private static final Method METHOD_ON_ATTACHED_TO_HIERARCHY;
    private static final Method METHOD_NOTIFY_CHANGED;

    static {
        try {
            METHOD_ON_ATTACHED_TO_HIERARCHY = Preference.class.getDeclaredMethod("onAttachedToHierarchy", PreferenceManager.class);
            METHOD_ON_ATTACHED_TO_HIERARCHY.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        try {
            METHOD_NOTIFY_CHANGED = Preference.class.getDeclaredMethod("notifyChanged");
            METHOD_NOTIFY_CHANGED.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private XpPreference() {
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
    public static boolean persistStringSet(final @NonNull Preference preference, final @NonNull Set<String> values) {
        //noinspection ConstantConditions
        if (values == null) {
            throw new IllegalArgumentException("Cannot persist null string set.");
        }

        if (!shouldPersist(preference)) {
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
            SharedPreferences.Editor editor = XpPreferenceManagerCompat.getEditor(preference.getPreferenceManager());
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
     * Preference is not persistent or the Preference is not in the
     * shared preferences.
     * @return The value from the SharedPreferences or the default return
     * value.
     * @see #persistStringSet(Preference, Set)
     */
    @Nullable
    public static Set<String> getPersistedStringSet(
            final @NonNull Preference preference, @Nullable final Set<String> defaultReturnValue) {
        if (!shouldPersist(preference)) {
            return defaultReturnValue;
        }

        PreferenceDataStore dataStore = preference.getPreferenceDataStore();
        if (dataStore != null) {
            return dataStore.getStringSet(preference.getKey(), defaultReturnValue);
        }

        return XpSharedPreferences.getStringSet(preference.getSharedPreferences(), preference.getKey(), defaultReturnValue);
    }

    private static void tryCommit(
            final @NonNull Preference preference, final @NonNull SharedPreferences.Editor editor) {
        if (XpPreferenceManagerCompat.shouldCommit(preference.getPreferenceManager())) {
            editor.apply();
        }
    }

    /**
     * Checks whether, at the given time this method is called, this Preference should store/restore
     * its value(s) into the {@link SharedPreferences} or into {@link PreferenceDataStore} if
     * assigned. This, at minimum, checks whether this Preference is persistent and it currently has
     * a key. Before you save/restore from the storage, check this first.
     *
     * @return {@code true} if it should persist the value
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean shouldPersist(final @NonNull Preference p) {
        return p.getPreferenceManager() != null && p.isPersistent() && p.hasKey();
    }

    /**
     * Called when this Preference has been attached to a Preference hierarchy.
     * Make sure to call the super implementation.
     *
     * @param preferenceManager The PreferenceManager of the hierarchy.
     */
    static void onAttachedToHierarchy(
            final @NonNull Preference preference, final @NonNull PreferenceManager preferenceManager) {
        try {
            METHOD_ON_ATTACHED_TO_HIERARCHY.invoke(preference, preferenceManager);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static void notifyChanged(final @NonNull Preference preference) {
        try {
            METHOD_NOTIFY_CHANGED.invoke(preference);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
