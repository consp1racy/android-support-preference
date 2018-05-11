package android.support.v7.preference;

import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.view.View;
import android.widget.ListView;

import net.xpece.android.support.preference.SharedPreferencesCompat;

import java.util.Set;

/**
 * Represents the basic Preference UI building
 * block displayed by a {@link PreferenceActivity} in the form of a
 * {@link ListView}. This class provides the {@link View} to be displayed in
 * the activity and associates with a {@link SharedPreferences} to
 * store/retrieve the preference data.
 * <p>
 * When specifying a preference hierarchy in XML, each element can point to a
 * subclass of {@link XpPreferenceCompat}, similar to the view hierarchy and layouts.
 * <p>
 * This class contains a {@code key} that will be used as the key into the
 * {@link SharedPreferences}. It is up to the subclass to decide how to store
 * the value.
 * <p></p>
 * <div class="special reference">
 * <h3>Developer Guides</h3>
 * <p>For information about building a settings UI with Preferences,
 * read the <a href="{@docRoot}guide/topics/ui/settings.html">Settings</a>
 * guide.</p>
 * </div>
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.GROUP_ID)
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
    public static boolean persistStringSet(@NonNull Preference preference, @NonNull Set<String> values) {
        if (preference.shouldPersist()) {
            // Shouldn't store null
            if (values.equals(getPersistedStringSet(preference, null))) {
                // It's already there, so the same as persisting
                return true;
            }

            SharedPreferences.Editor editor = preference.getPreferenceManager().getEditor();
            SharedPreferencesCompat.putStringSet(editor, preference.getKey(), values);
            tryCommit(preference, editor);
            return true;
        }
        return false;
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
        if (!preference.shouldPersist()) {
            return defaultReturnValue;
        }
        return SharedPreferencesCompat.getStringSet(preference.getSharedPreferences(), preference.getKey(), defaultReturnValue);
    }

    private static void tryCommit(@NonNull Preference preference, @NonNull SharedPreferences.Editor editor) {
        if (preference.getPreferenceManager().shouldCommit()) {
            editor.apply();
        }
    }
}
