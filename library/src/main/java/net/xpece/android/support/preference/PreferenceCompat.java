package net.xpece.android.support.preference;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;

import java.lang.reflect.Method;

/**
 * Created by Eugen on 13. 5. 2015.
 */
public class PreferenceCompat {
    private static final String TAG = PreferenceCompat.class.getSimpleName();

    private static final Method METHOD_PERFORM_CLICK;
    private static final Method METHOD_ON_KEY;

    static {
        Method performClick = null;
        try {
            performClick = android.preference.Preference.class.getDeclaredMethod("performClick");
            performClick.setAccessible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        METHOD_PERFORM_CLICK = performClick;

        Method onKey = null;
        try {
            onKey = android.preference.Preference.class.getDeclaredMethod("onKey", int.class, KeyEvent.class);
            onKey.setAccessible(true);
        } catch (Exception ex) {
            // don't care
        }
        METHOD_ON_KEY = onKey;
    }

    private PreferenceCompat() {}

    /**
     * Not used.
     *
     * @param preference
     */
    static void performClick(android.preference.Preference preference) {
        tryInvoke(METHOD_PERFORM_CLICK, preference);
    }

    /**
     * Not used.
     *
     * @param preference
     */
    static boolean onKey(android.preference.Preference preference, int keyCode, KeyEvent event) {
        try {
            return (boolean) tryInvoke(METHOD_ON_KEY, preference, keyCode, event);
        } catch (Exception ex) {
            return false;
        }
    }

    private static Object tryInvoke(Method method, Object receiver, Object... args) {
        try {
            return method.invoke(receiver, args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void setChecked(android.preference.Preference preference, boolean checked) {
        if (preference instanceof net.xpece.android.support.preference.TwoStatePreference) {
            ((net.xpece.android.support.preference.TwoStatePreference) preference).setChecked(checked);
        } else if (preference instanceof android.preference.CheckBoxPreference) {
            ((android.preference.CheckBoxPreference) preference).setChecked(checked);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
            && preference instanceof android.preference.TwoStatePreference) {
            ((android.preference.TwoStatePreference) preference).setChecked(checked);
        } else {
            Log.e(TAG, "setChecked(Preference, boolean) called on non-checkable preference!");
        }
    }
}
