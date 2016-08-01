package android.support.v7.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.SharedPreferencesCompat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Eugen on 6. 12. 2015.
 */
public final class XpPreferenceManager extends PreferenceManager {

    private static final Method METHOD_SET_NO_COMMIT;

    private static final String[] DEFAULT_PACKAGES;

    static {
        Method setNoCommit = null;
        try {
            setNoCommit = PreferenceManager.class.getDeclaredMethod("setNoCommit", boolean.class);
            setNoCommit.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        METHOD_SET_NO_COMMIT = setNoCommit;

        if (Build.VERSION.SDK_INT < 14) {
            DEFAULT_PACKAGES = new String[]{"net.xpece.android.support.preference.", "android.support.v7.preference."};
        } else {
            DEFAULT_PACKAGES = new String[]{"net.xpece.android.support.preference.", "android.support.v14.preference.", "android.support.v7.preference."};
        }
    }

    private String[] mCustomDefaultPackages;
    private String[] mAllDefaultPackages;

    private void setNoCommit(boolean noCommit) {
        try {
            METHOD_SET_NO_COMMIT.invoke(this, noCommit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @hide
     */
    public XpPreferenceManager(@NonNull final Context context, @Nullable final String[] customDefaultPackages) {
        this(context);
        mCustomDefaultPackages = customDefaultPackages;
    }

    /**
     * @hide
     */
    public XpPreferenceManager(@NonNull final Context context) {
        super(context);
    }

    @Override
    public PreferenceScreen inflateFromResource(@NonNull Context context, int resId, @Nullable PreferenceScreen rootPreferences) {
        this.setNoCommit(true);
        PreferenceInflater inflater = new PreferenceInflater(context, this);
        initPreferenceInflater(inflater);
        rootPreferences = (PreferenceScreen) inflater.inflate(resId, rootPreferences);
        rootPreferences.onAttachedToHierarchy(this);
        this.setNoCommit(false);
        return rootPreferences;
    }

    private void initPreferenceInflater(PreferenceInflater inflater) {
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
            SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
        }
    }

    private static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }

    private static int getDefaultSharedPreferencesMode() {
        return Context.MODE_PRIVATE;
    }
}
