package net.xpece.android.support.preference;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.lang.reflect.Method;

/**
 * Created by Eugen on 13. 5. 2015.
 */
public class PreferenceManagerCompat {

    private static final Method METHOD_REGISTER_ON_ACTIVITY_DESTROY_LISTENER;
    private static final Method METHOD_REGISTER_ON_ACTIVITY_RESULT_LISTENER;
    private static final Method METHOD_REGISTER_ON_ACTIVITY_STOP_LISTENER;

    private static final Method METHOD_UNREGISTER_ON_ACTIVITY_DESTROY_LISTENER;
    private static final Method METHOD_UNREGISTER_ON_ACTIVITY_RESULT_LISTENER;
    private static final Method METHOD_UNREGISTER_ON_ACTIVITY_STOP_LISTENER;

    private static final Method METHOD_GET_EDITOR;
    private static final Method METHOD_GET_NEXT_REQUEST_CODE;

    private static final Method METHOD_GET_ACTIVITY;
    private static final Method METHOD_GET_FRAGMENT;

    static {
        Method registerOnActivityDestroyListener = null;
        Method registerOnActivityResultListener = null;
        Method registerOnActivityStopListener = null;
        Method unregisterOnActivityDestroyListener = null;
        Method unregisterOnActivityResultListener = null;
        Method unregisterOnActivityStopListener = null;

        Method getEditor = null;
        Method getNextRequestCode = null;

        Method getActivity = null;
        Method getFragment = null;

        try {
            registerOnActivityDestroyListener = PreferenceManager.class.getDeclaredMethod("registerOnActivityDestroyListener", PreferenceManager.OnActivityDestroyListener.class);
            registerOnActivityResultListener = PreferenceManager.class.getDeclaredMethod("registerOnActivityResultListener", PreferenceManager.OnActivityResultListener.class);
            registerOnActivityStopListener = PreferenceManager.class.getDeclaredMethod("registerOnActivityStopListener", PreferenceManager.OnActivityStopListener.class);
            unregisterOnActivityDestroyListener = PreferenceManager.class.getDeclaredMethod("unregisterOnActivityDestroyListener", PreferenceManager.OnActivityDestroyListener.class);
            unregisterOnActivityResultListener = PreferenceManager.class.getDeclaredMethod("unregisterOnActivityResultListener", PreferenceManager.OnActivityResultListener.class);
            unregisterOnActivityStopListener = PreferenceManager.class.getDeclaredMethod("unregisterOnActivityStopListener", PreferenceManager.OnActivityStopListener.class);


            registerOnActivityDestroyListener.setAccessible(true);
            registerOnActivityResultListener.setAccessible(true);
            registerOnActivityStopListener.setAccessible(true);
            unregisterOnActivityDestroyListener.setAccessible(true);
            unregisterOnActivityResultListener.setAccessible(true);
            unregisterOnActivityStopListener.setAccessible(true);

            getEditor = PreferenceManager.class.getDeclaredMethod("getEditor");
            getEditor.setAccessible(true);

            getNextRequestCode = PreferenceManager.class.getDeclaredMethod("getNextRequestCode");
            getNextRequestCode.setAccessible(true);

            getActivity = PreferenceManager.class.getDeclaredMethod("getActivity");
            getActivity.setAccessible(true);

            // this one may actually fail, so do it last
            getFragment = PreferenceManager.class.getDeclaredMethod("getFragment");
            getFragment.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        METHOD_REGISTER_ON_ACTIVITY_DESTROY_LISTENER = registerOnActivityDestroyListener;
        METHOD_REGISTER_ON_ACTIVITY_RESULT_LISTENER = registerOnActivityResultListener;
        METHOD_REGISTER_ON_ACTIVITY_STOP_LISTENER = registerOnActivityStopListener;
        METHOD_UNREGISTER_ON_ACTIVITY_DESTROY_LISTENER = unregisterOnActivityDestroyListener;
        METHOD_UNREGISTER_ON_ACTIVITY_RESULT_LISTENER = unregisterOnActivityResultListener;
        METHOD_UNREGISTER_ON_ACTIVITY_STOP_LISTENER = unregisterOnActivityStopListener;

        METHOD_GET_EDITOR = getEditor;
        METHOD_GET_NEXT_REQUEST_CODE = getNextRequestCode;

        METHOD_GET_ACTIVITY = getActivity;
        METHOD_GET_FRAGMENT = getFragment;
    }

    private PreferenceManagerCompat() {}

    public static void registerOnActivityDestroyListener(PreferenceManager manager, PreferenceManager.OnActivityDestroyListener listener) {
        tryInvoke(METHOD_REGISTER_ON_ACTIVITY_DESTROY_LISTENER, manager, listener);
    }

    public static void registerOnActivityResultListener(PreferenceManager manager, PreferenceManager.OnActivityResultListener listener) {
        tryInvoke(METHOD_REGISTER_ON_ACTIVITY_RESULT_LISTENER, manager, listener);
    }

    public static void registerOnActivityStopListener(PreferenceManager manager, PreferenceManager.OnActivityStopListener listener) {
        tryInvoke(METHOD_REGISTER_ON_ACTIVITY_STOP_LISTENER, manager, listener);
    }

    public static void unregisterOnActivityDestroyListener(PreferenceManager manager, PreferenceManager.OnActivityDestroyListener listener) {
        tryInvoke(METHOD_UNREGISTER_ON_ACTIVITY_DESTROY_LISTENER, manager, listener);
    }

    public static void unregisterOnActivityResultListener(PreferenceManager manager, PreferenceManager.OnActivityResultListener listener) {
        tryInvoke(METHOD_UNREGISTER_ON_ACTIVITY_RESULT_LISTENER, manager, listener);
    }

    public static void unregisterOnActivityStopListener(PreferenceManager manager, PreferenceManager.OnActivityStopListener listener) {
        tryInvoke(METHOD_UNREGISTER_ON_ACTIVITY_STOP_LISTENER, manager, listener);
    }

    public static SharedPreferences.Editor getEditor(PreferenceManager manager) {
        return (SharedPreferences.Editor) tryInvoke(METHOD_GET_EDITOR, manager);
    }

    public static int getNextRequestCode(PreferenceManager manager) {
        return (int) tryInvoke(METHOD_GET_NEXT_REQUEST_CODE, manager);
    }

    public static Activity getActivity(PreferenceManager manager) {
        return (Activity) tryInvoke(METHOD_GET_ACTIVITY, manager);
    }

    public static PreferenceFragment getFragment(PreferenceManager manager) {
        return (PreferenceFragment) tryInvoke(METHOD_GET_FRAGMENT, manager);
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
