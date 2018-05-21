package net.xpece.android.support.preference;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

import java.lang.reflect.Method;

/**
 * @author Eugen on 21.05.2018.
 */
final class XpPreferenceManagerCompat {

    private static final Method METHOD_SHOULD_COMMIT;
    private static final Method METHOD_SET_NO_COMMIT;
    private static final Method METHOD_GET_EDITOR;

    static {
        try {
            METHOD_SHOULD_COMMIT = PreferenceManager.class.getDeclaredMethod("shouldCommit");
            METHOD_SHOULD_COMMIT.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
        try {
            METHOD_SET_NO_COMMIT = PreferenceManager.class.getDeclaredMethod("setNoCommit", boolean.class);
            METHOD_SET_NO_COMMIT.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
        try {
            METHOD_GET_EDITOR = PreferenceManager.class.getDeclaredMethod("getEditor");
            METHOD_GET_EDITOR.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean shouldCommit(final PreferenceManager preferenceManager) {
        try {
            return (boolean) METHOD_SHOULD_COMMIT.invoke(preferenceManager);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static void setNoCommit(final PreferenceManager preferenceManager, boolean noCommit) {
        try {
            METHOD_SET_NO_COMMIT.invoke(preferenceManager, noCommit);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @NonNull
    public static SharedPreferences.Editor getEditor(final PreferenceManager manager) {
        try {
            return (SharedPreferences.Editor) METHOD_GET_EDITOR.invoke(manager);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private XpPreferenceManagerCompat() {
        throw new AssertionError();
    }
}
