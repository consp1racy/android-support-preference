package android.support.v7.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.xpece.android.support.preference.plugins.XpSupportPreferencePlugins;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Eugen on 6. 12. 2015.
 */
public class XpPreferenceManager extends PreferenceManager {

    private static final Method METHOD_SET_NO_COMMIT;

    private static final String[] DEFAULT_PACKAGES;

    static {
        Method setNoCommit = null;
        try {
            setNoCommit = PreferenceManager.class.getDeclaredMethod("setNoCommit", boolean.class);
            setNoCommit.setAccessible(true);
        } catch (NoSuchMethodException e) {
            XpSupportPreferencePlugins.onError(e, "setNoCommit not available.");
        }
        METHOD_SET_NO_COMMIT = setNoCommit;

        DEFAULT_PACKAGES = new String[]{"net.xpece.android.support.preference.", "android.support.v14.preference.", "android.support.v7.preference."};
    }

    private String[] mCustomDefaultPackages;
    private String[] mAllDefaultPackages;

    private void setNoCommit(boolean noCommit) {
        try {
            METHOD_SET_NO_COMMIT.invoke(this, noCommit);
        } catch (IllegalAccessException e) {
            // This should never happen.
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            // This should never happen.
            throw new IllegalStateException(e);
        }
    }

    XpPreferenceManager(@NonNull final Context context, @Nullable final String[] customDefaultPackages) {
        this(context);
        mCustomDefaultPackages = customDefaultPackages;
    }

    @SuppressWarnings("RestrictedApi")
    XpPreferenceManager(@NonNull final Context context) {
        super(context);
    }

    @NonNull
    @Override
    public PreferenceScreen inflateFromResource(@NonNull final Context context, final int resId, @Nullable PreferenceScreen rootPreferences) {
        setNoCommit(true);
        PreferenceInflater inflater = new XpPreferenceInflater(context, this);
        initPreferenceInflater(inflater);
        rootPreferences = (PreferenceScreen) inflater.inflate(resId, rootPreferences);
        rootPreferences.onAttachedToHierarchy(this);
        setNoCommit(false);
        return rootPreferences;
    }

    private void initPreferenceInflater(@NonNull final PreferenceInflater inflater) {
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

    public static void setDefaultValues(@NonNull Context context, int resId, boolean readAgain, @Nullable final String[] customDefaultPackages) {
        setDefaultValues(context, getDefaultSharedPreferencesName(context), getDefaultSharedPreferencesMode(), resId, readAgain, customDefaultPackages);
    }

    public static void setDefaultValues(@NonNull Context context, int resId, boolean readAgain) {
        setDefaultValues(context, getDefaultSharedPreferencesName(context), getDefaultSharedPreferencesMode(), resId, readAgain, null);
    }

    public static void setDefaultValues(@NonNull Context context, @NonNull String sharedPreferencesName, int sharedPreferencesMode, int resId, boolean readAgain) {
        setDefaultValues(context, sharedPreferencesName, sharedPreferencesMode, resId, readAgain, null);
    }

    public static void setDefaultValues(@NonNull Context context, @NonNull String sharedPreferencesName, int sharedPreferencesMode, int resId, boolean readAgain, @Nullable final String[] customDefaultPackages) {
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
    private static String getDefaultSharedPreferencesName(@NonNull Context context) {
        return context.getPackageName() + "_preferences";
    }

    private static int getDefaultSharedPreferencesMode() {
        return Context.MODE_PRIVATE;
    }
}
