package net.xpece.android.support.preference;

import android.content.SharedPreferences;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Eugen on 20. 5. 2015.
 */
public class SharedPreferencesCompat {

    public static void putStringSet(SharedPreferences.Editor editor, String key, Set<String> values) {
            if (Build.VERSION.SDK_INT >= 11) {
                while (true) {
                    try {
                        editor.putStringSet(key, values);
                        break;
                    } catch (ClassCastException ex) {
                        // Clear stale JSON string from before system upgrade
                        editor.remove(key);
                    }
                }
            } else {
                putStringSetToJson(editor, key, values);
            }
    }

    public static Set<String> getStringSet(SharedPreferences prefs, String key, Set<String> defaultReturnValue) {
        if (Build.VERSION.SDK_INT >= 11) {
            try {
                return prefs.getStringSet(key, defaultReturnValue);
            } catch (ClassCastException ex) {
                // If user upgraded from Gingerbread to something higher read the stale JSON string
                return getStringSetFromJson(prefs, key, defaultReturnValue);
            }
        } else {
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
            e.printStackTrace();
            return defaultReturnValue;
        }
    }

    private static void putStringSetToJson(SharedPreferences.Editor editor, String key, Set<String> values) {
        JSONArray json = new JSONArray(values);
        editor.putString(key, json.toString());
    }

    private SharedPreferencesCompat() {}

}
