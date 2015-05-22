package net.xpece.android.support.preference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Eugen on 13. 5. 2015.
 */
public class PreferenceManagerCompat {
    private static final String TAG = PreferenceManagerCompat.class.getSimpleName();

    private static final Method METHOD_REGISTER_ON_ACTIVITY_DESTROY_LISTENER;
    private static final Method METHOD_REGISTER_ON_ACTIVITY_RESULT_LISTENER;
    private static final Method METHOD_REGISTER_ON_ACTIVITY_STOP_LISTENER;

    private static final Method METHOD_UNREGISTER_ON_ACTIVITY_DESTROY_LISTENER;
    private static final Method METHOD_UNREGISTER_ON_ACTIVITY_RESULT_LISTENER;
    private static final Method METHOD_UNREGISTER_ON_ACTIVITY_STOP_LISTENER;

    private static final Method METHOD_SET_NO_COMMIT;
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

        Method setNoCommit = null;
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
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        try {
            setNoCommit = PreferenceManager.class.getDeclaredMethod("setNoCommit", boolean.class);
            setNoCommit.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        try {
            getNextRequestCode = PreferenceManager.class.getDeclaredMethod("getNextRequestCode");
            getNextRequestCode.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        try {
            getActivity = PreferenceManager.class.getDeclaredMethod("getActivity");
            getActivity.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        try {
            getFragment = PreferenceManager.class.getDeclaredMethod("getFragment");
            getFragment.setAccessible(true);
        } catch (NoSuchMethodException e) {
            //
        }

        METHOD_REGISTER_ON_ACTIVITY_DESTROY_LISTENER = registerOnActivityDestroyListener;
        METHOD_REGISTER_ON_ACTIVITY_RESULT_LISTENER = registerOnActivityResultListener;
        METHOD_REGISTER_ON_ACTIVITY_STOP_LISTENER = registerOnActivityStopListener;
        METHOD_UNREGISTER_ON_ACTIVITY_DESTROY_LISTENER = unregisterOnActivityDestroyListener;
        METHOD_UNREGISTER_ON_ACTIVITY_RESULT_LISTENER = unregisterOnActivityResultListener;
        METHOD_UNREGISTER_ON_ACTIVITY_STOP_LISTENER = unregisterOnActivityStopListener;

        METHOD_SET_NO_COMMIT = setNoCommit;
        METHOD_GET_NEXT_REQUEST_CODE = getNextRequestCode;

        METHOD_GET_ACTIVITY = getActivity;
        METHOD_GET_FRAGMENT = getFragment;
    }

    private PreferenceManagerCompat() {}

    public static void registerOnActivityDestroyListener(PreferenceManager manager, PreferenceManager.OnActivityDestroyListener listener) {
        Util.tryInvoke(METHOD_REGISTER_ON_ACTIVITY_DESTROY_LISTENER, manager, listener);
    }

    public static void registerOnActivityResultListener(PreferenceManager manager, PreferenceManager.OnActivityResultListener listener) {
        Util.tryInvoke(METHOD_REGISTER_ON_ACTIVITY_RESULT_LISTENER, manager, listener);
    }

    public static void registerOnActivityStopListener(PreferenceManager manager, PreferenceManager.OnActivityStopListener listener) {
        Util.tryInvoke(METHOD_REGISTER_ON_ACTIVITY_STOP_LISTENER, manager, listener);
    }

    public static void unregisterOnActivityDestroyListener(PreferenceManager manager, PreferenceManager.OnActivityDestroyListener listener) {
        Util.tryInvoke(METHOD_UNREGISTER_ON_ACTIVITY_DESTROY_LISTENER, manager, listener);
    }

    public static void unregisterOnActivityResultListener(PreferenceManager manager, PreferenceManager.OnActivityResultListener listener) {
        Util.tryInvoke(METHOD_UNREGISTER_ON_ACTIVITY_RESULT_LISTENER, manager, listener);
    }

    public static void unregisterOnActivityStopListener(PreferenceManager manager, PreferenceManager.OnActivityStopListener listener) {
        Util.tryInvoke(METHOD_UNREGISTER_ON_ACTIVITY_STOP_LISTENER, manager, listener);
    }

    public static int getNextRequestCode(PreferenceManager manager) {
        return (int) Util.tryInvoke(METHOD_GET_NEXT_REQUEST_CODE, manager);
    }

    public static Activity getActivity(PreferenceManager manager) {
        return (Activity) Util.tryInvoke(METHOD_GET_ACTIVITY, manager);
    }

    public static PreferenceFragment getFragment(PreferenceManager manager) {
        return (PreferenceFragment) Util.tryInvoke(METHOD_GET_FRAGMENT, manager);
    }

    private static void setNoCommit(PreferenceManager manager, boolean value) {
        Util.tryInvoke(METHOD_SET_NO_COMMIT, manager, value);
    }

    private static List<ResolveInfo> queryIntentActivities(Context context, Intent queryIntent) {
        return context.getApplicationContext().getPackageManager().queryIntentActivities(queryIntent,
            PackageManager.GET_META_DATA);
    }

    static PreferenceScreen inflateFromIntent(PreferenceManager manager, Context context, Intent queryIntent, PreferenceScreen rootPreferences) {
        return inflateFromIntent(manager, context, queryIntent, rootPreferences, null);
    }

    static PreferenceScreen inflateFromIntent(PreferenceManager manager, Context context, Intent queryIntent, PreferenceScreen rootPreferences, GenericInflater.Factory<android.preference.Preference> factory) {
        final List<ResolveInfo> activities = queryIntentActivities(context, queryIntent);
        final HashSet<String> inflatedRes = new HashSet<String>();

        for (int i = activities.size() - 1; i >= 0; i--) {
            final ActivityInfo activityInfo = activities.get(i).activityInfo;
            final Bundle metaData = activityInfo.metaData;

            if ((metaData == null) || !metaData.containsKey(PreferenceManager.METADATA_KEY_PREFERENCES)) {
                continue;
            }

            // Need to concat the package with res ID since the same res ID
            // can be re-used across contexts
            final String uniqueResId = activityInfo.packageName + ":"
                + activityInfo.metaData.getInt(PreferenceManager.METADATA_KEY_PREFERENCES);

            if (!inflatedRes.contains(uniqueResId)) {
                inflatedRes.add(uniqueResId);

                try {
                    context = context.createPackageContext(activityInfo.packageName, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w(TAG, "Could not create context for " + activityInfo.packageName + ": "
                        + Log.getStackTraceString(e));
                    continue;
                }

                final PreferenceInflater inflater = new PreferenceInflater(context, manager);
                if (factory != null) {
                    inflater.setFactory(factory);
                }

                final XmlResourceParser parser = activityInfo.loadXmlMetaData(context.getApplicationContext()
                    .getPackageManager(), PreferenceManager.METADATA_KEY_PREFERENCES);
                rootPreferences = (PreferenceScreen) inflater
                    .inflate(parser, rootPreferences, true);
                parser.close();
            }
        }

        PreferenceCompat.onAttachedToHierarchy(rootPreferences, manager);

        return rootPreferences;
    }

    public static PreferenceScreen inflateFromResource(PreferenceManager manager, Context context, int resId, PreferenceScreen rootPreferences) {
        return inflateFromResource(manager, context, resId, rootPreferences, null);
    }

    public static PreferenceScreen inflateFromResource(PreferenceManager manager, Context context, int resId, PreferenceScreen rootPreferences, GenericInflater.Factory<android.preference.Preference> factory) {
// Block commits
//        manager.setNoCommit(true);
        setNoCommit(manager, true);

//        final PreferenceInflater inflater = new PreferenceInflater(context, this);
//        rootPreferences = (PreferenceScreen) inflater.inflate(resId, rootPreferences, true);
//        rootPreferences.onAttachedToHierarchy(this);
        final PreferenceInflater inflater = new PreferenceInflater(context, manager);
        if (factory != null) {
            inflater.setFactory(factory);
        }

        android.preference.Preference prefs = inflater.inflate(resId, rootPreferences, true);
        rootPreferences = (PreferenceScreen) prefs;
        PreferenceCompat.onAttachedToHierarchy(rootPreferences, manager);

        // Unblock commits
//        manager.setNoCommit(false);
        setNoCommit(manager, false);

        return rootPreferences;
    }
}
