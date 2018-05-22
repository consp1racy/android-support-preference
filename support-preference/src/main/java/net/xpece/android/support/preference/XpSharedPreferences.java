package net.xpece.android.support.preference;

import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.Nullable;

import net.xpece.android.support.preference.plugins.XpSupportPreferencePlugins;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

/**
 * We used to store string sets as JSON array {@link String} on Android 2.x.
 * <p>
 * This class allows to read such JSON array as {@code Set<String>} and overwrite it.
 */
public final class XpSharedPreferences {

    // Let's assume the user didn't upgrade from Android 2 all the way to Lollipop.
    private static final boolean SUPPORTS_JSON_FALLBACK = Build.VERSION.SDK_INT < 21;

    /**
     * Stores supplied preference as {@code Set<String>}
     * while overwriting previous JSON array string.
     *
     * @param editor Preference editor
     * @param key    Preference key
     * @param values Data set
     * @deprecated Use {@link SharedPreferences.Editor#putStringSet(String, Set)} directly.
     */
    @Deprecated
    public static void putStringSet(SharedPreferences.Editor editor,
                                    String key,
                                    Set<String> values) {
        editor.putStringSet(key, values);
    }

    /**
     * Reads preference as {@code Set<String>} even when it's stored as JSON array string.
     *
     * @param prefs              Preferences
     * @param key                Preference key
     * @param defaultReturnValue Default value if not found
     * @return Data set
     */
    @Nullable
    public static Set<String> getStringSet(SharedPreferences prefs,
                                           String key,
                                           @Nullable Set<String> defaultReturnValue) {
        try {
            return prefs.getStringSet(key, defaultReturnValue);
        } catch (ClassCastException ex) {
            // We used to store string sets as JSON array on Android 2.x.
            if (SUPPORTS_JSON_FALLBACK) {
                // If user upgraded from Gingerbread to something higher read the stale JSON string.
                return getStringSetFromJson(prefs, key, defaultReturnValue);
            } else {
                throw ex;
            }
        }
    }

    @Nullable
    private static Set<String> getStringSetFromJson(SharedPreferences prefs,
                                                    String key,
                                                    @Nullable Set<String> defaultReturnValue) {
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

    private XpSharedPreferences() {
        throw new AssertionError();
    }
}
