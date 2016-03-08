package android.support.v7.preference;

import android.content.Context;
import android.os.Build;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Eugen on 6. 12. 2015.
 */
final class XpPreferenceManager extends PreferenceManager {

    private static final Method METHOD_SET_NO_COMMIT;

    private static final String[] ICS_DEFAULT_PACKAGES = new String[]{"net.xpece.android.support.preference.", "android.support.v14.preference.", "android.support.v7.preference."};
    private static final String[] BASE_DEFAULT_PACKAGES = new String[]{"net.xpece.android.support.preference.", "android.support.v7.preference."};
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
            DEFAULT_PACKAGES = BASE_DEFAULT_PACKAGES;
        } else {
            DEFAULT_PACKAGES = ICS_DEFAULT_PACKAGES;
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

    public XpPreferenceManager(final Context context, final String[] customDefaultPackages) {
        this(context);
        mCustomDefaultPackages = customDefaultPackages;
    }

    public XpPreferenceManager(final Context context) {
        super(context);
    }

    public PreferenceScreen inflateFromResource(Context context, int resId, PreferenceScreen rootPreferences) {
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
                Set<String> allDefaultPackagesSet = new HashSet<>(mCustomDefaultPackages.length + DEFAULT_PACKAGES.length);
                Collections.addAll(allDefaultPackagesSet, mCustomDefaultPackages);
                Collections.addAll(allDefaultPackagesSet, DEFAULT_PACKAGES);
                mAllDefaultPackages = allDefaultPackagesSet.toArray(new String[allDefaultPackagesSet.size()]);
            }
        }
        inflater.setDefaultPackages(mAllDefaultPackages);
    }

}
