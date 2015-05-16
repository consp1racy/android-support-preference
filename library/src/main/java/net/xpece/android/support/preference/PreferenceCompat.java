package net.xpece.android.support.preference;

import android.view.KeyEvent;

import java.lang.reflect.Method;

/**
 * Created by Eugen on 13. 5. 2015.
 */
class PreferenceCompat {

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

    public static void performClick(android.preference.Preference preference) {
        tryInvoke(METHOD_PERFORM_CLICK, preference);
    }

    public static boolean onKey(android.preference.Preference preference, int keyCode, KeyEvent event) {
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
}
