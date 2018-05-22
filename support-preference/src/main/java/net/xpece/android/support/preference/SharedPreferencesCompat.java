package net.xpece.android.support.preference;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.util.Set;

/**
 * We used to store string sets as JSON array {@link String} on Android 2.x.
 * <p>
 * This class allows to read such JSON array as {@code Set<String>} and overwrite it.
 * @see XpSharedPreferences
 */
@Deprecated
public final class SharedPreferencesCompat {

    /**
     * Stores supplied preference as {@code Set<String>}
     * while overwriting previous JSON array string.
     *
     * @param editor Preference editor
     * @param key Preference key
     * @param values Data set
     * @deprecated Use {@link SharedPreferences.Editor#putStringSet(String, Set)} directly.
     */
    @Deprecated
    public static void putStringSet(SharedPreferences.Editor editor,
                                    String key,
                                    Set<String> values) {
        XpSharedPreferences.putStringSet(editor, key, values);
    }

    /**
     * Reads preference as {@code Set<String>} even when it's stored as JSON array string.
     *
     * @param prefs Preferences
     * @param key Preference key
     * @param defaultReturnValue Default value if not found
     * @return Data set
     */
    @Nullable
    public static Set<String> getStringSet(SharedPreferences prefs,
                                           String key,
                                           @Nullable Set<String> defaultReturnValue) {
        return XpSharedPreferences.getStringSet(prefs, key, defaultReturnValue);
    }

    private SharedPreferencesCompat() {
        throw new AssertionError();
    }
}
