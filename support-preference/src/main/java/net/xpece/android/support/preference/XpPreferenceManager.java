package net.xpece.android.support.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.XmlRes;

/**
 * Used to help create {@link Preference} hierarchies
 * from activities or XML.
 * <p>
 * In most cases, clients should use
 * {@link XpPreferenceFragment#addPreferencesFromResource(int)}.
 *
 * @see XpPreferenceFragment
 */
@SuppressWarnings("deprecation")
public abstract class XpPreferenceManager {

    public static void setDefaultValues(@NonNull Context context, @XmlRes int resId, boolean readAgain, @Nullable final String[] customDefaultPackages) {
        android.support.v7.preference.XpPreferenceManager.setDefaultValues(context, resId, readAgain, customDefaultPackages);
    }

    public static void setDefaultValues(@NonNull Context context, @XmlRes int resId, boolean readAgain) {
        android.support.v7.preference.XpPreferenceManager.setDefaultValues(context, resId, readAgain);
    }

    public static void setDefaultValues(@NonNull Context context, @NonNull String sharedPreferencesName, int sharedPreferencesMode, @XmlRes int resId, boolean readAgain) {
        android.support.v7.preference.XpPreferenceManager.setDefaultValues(context, sharedPreferencesName, sharedPreferencesMode, resId, readAgain);
    }

    public static void setDefaultValues(@NonNull Context context, @NonNull String sharedPreferencesName, int sharedPreferencesMode, @XmlRes int resId, boolean readAgain, @Nullable final String[] customDefaultPackages) {
        android.support.v7.preference.XpPreferenceManager.setDefaultValues(context, sharedPreferencesName, sharedPreferencesMode, resId, readAgain, customDefaultPackages);
    }

    /**
     * Gets a SharedPreferences instance that points to the default file that is
     * used by the preference framework in the given context.
     *
     * @param context The context of the preferences whose values are wanted.
     * @return A SharedPreferences instance that can be used to retrieve and
     *         listen to values of the preferences.
     */
    @NonNull
    public static SharedPreferences getDefaultSharedPreferences(@NonNull Context context) {
        return android.support.v7.preference.XpPreferenceManager.getDefaultSharedPreferences(context);
    }
}
