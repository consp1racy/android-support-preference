package net.xpece.android.support.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.XmlRes;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Used to help create {@link Preference} hierarchies
 * from activities or XML.
 * <p>
 * In most cases, clients should use
 * {@link XpPreferenceFragment#addPreferencesFromResource(int)}.
 *
 * @see XpPreferenceFragment
 */
public final class XpPreferenceManager extends PreferenceManager {

    private static final String[] DEFAULT_PACKAGES;

    static {
        final Set<String> defaultPackages = new LinkedHashSet<>(); // Preserve order.
        defaultPackages.add(net.xpece.android.support.preference.Preference.class.getPackage().getName() + ".");
        // Support the AndroidX relocated classes.
        defaultPackages.add(Preference.class.getPackage().getName() + ".");
        try {
            defaultPackages.add(SwitchPreference.class.getPackage().getName() + ".");
        } catch (NoClassDefFoundError ignore) {
            // preference-v14 is an optional dependency; classes were merged in 27.0.0.
        }
        // For backwards compatibility. Someone may have put their classes in these packages.
        defaultPackages.add("android.support.v7.preference.");
        defaultPackages.add("android.support.v14.preference.");
        DEFAULT_PACKAGES = defaultPackages.toArray(new String[defaultPackages.size()]);
    }

    private String[] mCustomDefaultPackages;
    private String[] mAllDefaultPackages;

    XpPreferenceManager(final Context context, @Nullable final String[] customDefaultPackages) {
        this(context);
        mCustomDefaultPackages = customDefaultPackages;
    }

    @SuppressWarnings("RestrictedApi")
    XpPreferenceManager(final Context context) {
        super(context);
    }

    /**
     * Inflates a preference hierarchy from XML. If a preference hierarchy is
     * given, the new preference hierarchies will be merged in.
     *
     * @param context The context of the resource.
     * @param resId The resource ID of the XML to inflate.
     * @param rootPreferences Optional existing hierarchy to merge the new
     *            hierarchies into.
     * @return The root hierarchy (if one was not provided, the new hierarchy's
     *         root).
     */
    @NonNull
    @Override
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public PreferenceScreen inflateFromResource(
            final Context context,
            @XmlRes final int resId,
            @Nullable PreferenceScreen rootPreferences) {
        XpPreferenceManagerCompat.setNoCommit(this, true);
        XpPreferenceInflater inflater = new XpPreferenceInflater(context, this);
        initPreferenceInflater(inflater);
        rootPreferences = (PreferenceScreen) inflater.inflate(resId, rootPreferences);
        XpPreference.onAttachedToHierarchy(rootPreferences, this);
        XpPreferenceManagerCompat.setNoCommit(this, false);
        return rootPreferences;
    }

    private void initPreferenceInflater(final XpPreferenceInflater inflater) {
        if (mAllDefaultPackages == null) {
            if (mCustomDefaultPackages == null || mCustomDefaultPackages.length == 0) {
                mAllDefaultPackages = DEFAULT_PACKAGES;
            } else {
                List<String> allDefaultPackagesSet = new ArrayList<>(mCustomDefaultPackages.length + DEFAULT_PACKAGES.length);
                Collections.addAll(allDefaultPackagesSet, mCustomDefaultPackages);
                Collections.addAll(allDefaultPackagesSet, DEFAULT_PACKAGES);
                mAllDefaultPackages = allDefaultPackagesSet.toArray(new String[allDefaultPackagesSet.size()]);
            }
        }
        inflater.setDefaultPackages(mAllDefaultPackages);
    }

    /**
     * Sets the default values from an XML preference file by reading the values defined
     * by each {@link android.support.v7.preference.Preference} item's {@code android:defaultValue} attribute. This should
     * be called by the application's main activity.
     * <p>
     *
     * @param context The context of the shared preferences.
     * @param resId The resource ID of the preference XML file.
     * @param readAgain Whether to re-read the default values.
     * If false, this method sets the default values only if this
     * method has never been called in the past (or if the
     * {@link #KEY_HAS_SET_DEFAULT_VALUES} in the default value shared
     * preferences file is false). To attempt to set the default values again
     * bypassing this check, set {@code readAgain} to true.
     *            <p class="note">
     *            Note: this will NOT reset preferences back to their default
     *            values. For that functionality, use
     *            {@link PreferenceManager#getDefaultSharedPreferences(Context)}
     *            and clear it followed by a call to this method with this
     *            parameter set to true.
     * @param customDefaultPackages Set the default package that will be searched for classes to
     * construct for tag names that have no explicit package.
     * @see #setDefaultValues(Context, int, boolean)
     * @see #setDefaultValues(Context, String, int, int, boolean)
     * @see #setDefaultValues(Context, String, int, int, boolean, String[])
     */
    public static void setDefaultValues(Context context, @XmlRes int resId, boolean readAgain, @Nullable final String[] customDefaultPackages) {
        setDefaultValues(context, getDefaultSharedPreferencesName(context), getDefaultSharedPreferencesMode(), resId, readAgain, customDefaultPackages);
    }

    /**
     * Sets the default values from an XML preference file by reading the values defined
     * by each {@link Preference} item's {@code android:defaultValue} attribute. This should
     * be called by the application's main activity.
     * <p>
     *
     * @param context The context of the shared preferences.
     * @param resId The resource ID of the preference XML file.
     * @param readAgain Whether to re-read the default values.
     * If false, this method sets the default values only if this
     * method has never been called in the past (or if the
     * {@link #KEY_HAS_SET_DEFAULT_VALUES} in the default value shared
     * preferences file is false). To attempt to set the default values again
     * bypassing this check, set {@code readAgain} to true.
     *            <p class="note">
     *            Note: this will NOT reset preferences back to their default
     *            values. For that functionality, use
     *            {@link PreferenceManager#getDefaultSharedPreferences(Context)}
     *            and clear it followed by a call to this method with this
     *            parameter set to true.
     * @see #setDefaultValues(Context, int, boolean, String[])
     * @see #setDefaultValues(Context, String, int, int, boolean)
     * @see #setDefaultValues(Context, String, int, int, boolean, String[])
     */
    public static void setDefaultValues(Context context, @XmlRes int resId, boolean readAgain) {
        setDefaultValues(context, getDefaultSharedPreferencesName(context), getDefaultSharedPreferencesMode(), resId, readAgain, null);
    }

    /**
     * Similar to {@link #setDefaultValues(Context, int, boolean)} but allows
     * the client to provide the filename and mode of the shared preferences
     * file.
     *
     * @param context The context of the shared preferences.
     * @param sharedPreferencesName A custom name for the shared preferences file.
     * @param sharedPreferencesMode The file creation mode for the shared preferences file, such
     * as {@link android.content.Context#MODE_PRIVATE} or {@link
     * android.content.Context#MODE_PRIVATE}
     * @param resId The resource ID of the preference XML file.
     * @param readAgain Whether to re-read the default values.
     * If false, this method will set the default values only if this
     * method has never been called in the past (or if the
     * {@link #KEY_HAS_SET_DEFAULT_VALUES} in the default value shared
     * preferences file is false). To attempt to set the default values again
     * bypassing this check, set {@code readAgain} to true.
     * <p class="note">
     * Note: this will NOT reset preferences back to their default
     * values. For that functionality, use
     * {@link PreferenceManager#getDefaultSharedPreferences(Context)}
     * and clear it followed by a call to this method with this
     * parameter set to true.
     * @see #setDefaultValues(Context, int, boolean)
     * @see #setDefaultValues(Context, int, boolean, String[])
     * @see #setDefaultValues(Context, String, int, int, boolean, String[])
     * @see #setSharedPreferencesName(String)
     * @see #setSharedPreferencesMode(int)
     */
    public static void setDefaultValues(Context context, String sharedPreferencesName, int sharedPreferencesMode, @XmlRes int resId, boolean readAgain) {
        setDefaultValues(context, sharedPreferencesName, sharedPreferencesMode, resId, readAgain, null);
    }

    /**
     * Similar to {@link #setDefaultValues(Context, int, boolean)} but allows
     * the client to provide the filename and mode of the shared preferences
     * file.
     *
     * @param context The context of the shared preferences.
     * @param sharedPreferencesName A custom name for the shared preferences file.
     * @param sharedPreferencesMode The file creation mode for the shared preferences file, such
     * as {@link android.content.Context#MODE_PRIVATE} or {@link
     * android.content.Context#MODE_PRIVATE}
     * @param resId The resource ID of the preference XML file.
     * @param readAgain Whether to re-read the default values.
     * If false, this method will set the default values only if this
     * method has never been called in the past (or if the
     * {@link #KEY_HAS_SET_DEFAULT_VALUES} in the default value shared
     * preferences file is false). To attempt to set the default values again
     * bypassing this check, set {@code readAgain} to true.
     * <p class="note">
     * Note: this will NOT reset preferences back to their default
     * values. For that functionality, use
     * {@link PreferenceManager#getDefaultSharedPreferences(Context)}
     * and clear it followed by a call to this method with this
     * parameter set to true.
     * @param customDefaultPackages Set the default package that will be searched for classes to
     * construct for tag names that have no explicit package.
     * @see #setDefaultValues(Context, int, boolean)
     * @see #setDefaultValues(Context, int, boolean, String[])
     * @see #setDefaultValues(Context, String, int, int, boolean)
     * @see #setSharedPreferencesName(String)
     * @see #setSharedPreferencesMode(int)
     */
    public static void setDefaultValues(Context context, String sharedPreferencesName, int sharedPreferencesMode, @XmlRes int resId, boolean readAgain, @Nullable final String[] customDefaultPackages) {
        SharedPreferences defaultValueSp = context.getSharedPreferences(KEY_HAS_SET_DEFAULT_VALUES, 0);
        if (readAgain || !defaultValueSp.getBoolean(KEY_HAS_SET_DEFAULT_VALUES, false)) {
            XpPreferenceManager pm = new XpPreferenceManager(context, customDefaultPackages);
            pm.setSharedPreferencesName(sharedPreferencesName);
            pm.setSharedPreferencesMode(sharedPreferencesMode);
            pm.inflateFromResource(context, resId, null);
            SharedPreferences.Editor editor = defaultValueSp.edit().putBoolean(KEY_HAS_SET_DEFAULT_VALUES, true);
            editor.apply();
        }
    }

    @NonNull
    private static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }

    private static int getDefaultSharedPreferencesMode() {
        return Context.MODE_PRIVATE;
    }
}
