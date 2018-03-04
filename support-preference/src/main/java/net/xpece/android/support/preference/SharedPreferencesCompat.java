package net.xpece.android.support.preference;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import net.xpece.android.support.preference.plugins.XpSupportPreferencePlugins;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

/**
 * We used to store string sets as JSON array {@link String} on Android 2.x.
 *
 * This class allows to read such JSON array as {@code Set<String>} and overwrite it.
 */
public final class SharedPreferencesCompat {

    /**
     * Stores supplied preference as {@code Set<String>}
     * while overwriting previous JSON array string.
     *
     * @param editor Preference editor
     * @param key Preference key
     * @param values Data set
     */
    public static void putStringSet(@NonNull SharedPreferences.Editor editor, String key, Set<String> values) {
        while (true) {
            try {
                editor.putStringSet(key, values);
                break;
            } catch (ClassCastException ex) {
                // We used to store string sets as JSON array on Android 2.x.
                // Clear stale JSON string from before system upgrade.
                editor.remove(key);
            }
        }
    }

    /**
     * Reads preference as {@code Set<String>} even when it's stored as JSON array string.
     *
     * @param prefs Preferences
     * @param key Preference key
     * @param defaultReturnValue Default value if not found
     * @return Data set
     */
    public static Set<String> getStringSet(SharedPreferences prefs, String key, Set<String> defaultReturnValue) {
        try {
            return prefs.getStringSet(key, defaultReturnValue);
        } catch (ClassCastException ex) {
            // We used to store string sets as JSON array on Android 2.x.
            // If user upgraded from Gingerbread to something higher read the stale JSON string.
            return getStringSetFromJson(prefs, key, defaultReturnValue);
        }
    }

    private static Set<String> getStringSetFromJson(SharedPreferences prefs, String key, Set<String> defaultReturnValue) {
        final String input = prefs.getString(key, null);
        if (input == null) return defaultReturnValue;

        try {
            HashSet<String> set = new HashSet<>();
            JSONArray json = new JSONArray(input);
            for (int i = 0, size = json.length(); i < size; i++) {
                String value = json.getString(i);
                set.add(value);
            }
            return set;
        } catch (JSONException e) {
            XpSupportPreferencePlugins.onError(e, "Couldn't read '" + key + "' preference as JSON.");
            return defaultReturnValue;
        }
    }

    private SharedPreferencesCompat() {
        throw new AssertionError("No instances!");
    }
}
