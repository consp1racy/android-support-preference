package android.support.v7.preference;

import android.content.Context;
import android.os.Build;

import java.lang.reflect.Method;

/**
 * @author Eugen on 6. 12. 2015.
 */
class XpPreferenceManager extends PreferenceManager {

    private static final Method METHOD_SET_NO_COMMIT;

    static {
        Method setNoCommit = null;
        try {
            setNoCommit = PreferenceManager.class.getDeclaredMethod("setNoCommit", boolean.class);
            setNoCommit.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        METHOD_SET_NO_COMMIT = setNoCommit;
    }

    private void setNoCommit(boolean noCommit) {
        try {
            METHOD_SET_NO_COMMIT.invoke(this, noCommit);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (Build.VERSION.SDK_INT >= 14) {
            inflater.setDefaultPackages(new String[]{"net.xpece.android.support.preference.", "android.support.v14.preference.", "android.support.v7.preference."});
        } else {
            inflater.setDefaultPackages(new String[]{"net.xpece.android.support.preference.", "android.support.v7.preference."});
        }

    }

}
