package net.xpece.android.support.preference;

import android.preference.PreferenceManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Eugen on 13. 5. 2015.
 */
class PreferenceCompat {
    private static final String TAG = PreferenceCompat.class.getSimpleName();

    private static final Method METHOD_ON_ATTACHED_TO_HIERARCHY;

    private static final Field FIELD_CAN_RECYCLE_LAYOUT;

    static {
        Method onAttachedToHierarchy = null;
        try {
            onAttachedToHierarchy = android.preference.Preference.class.getDeclaredMethod("onAttachedToHierarchy", PreferenceManager.class);
            onAttachedToHierarchy.setAccessible(true);
        } catch (Exception ex) {
            // don't care
        }
        METHOD_ON_ATTACHED_TO_HIERARCHY = onAttachedToHierarchy;

        Field canRecycleLayout = null;
        try {
            canRecycleLayout = android.preference.Preference.class.getDeclaredField("mCanRecycleLayout");
            canRecycleLayout.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // not on 2.3
        }
        FIELD_CAN_RECYCLE_LAYOUT = canRecycleLayout;
    }

    private PreferenceCompat() {}

    static void onAttachedToHierarchy(android.preference.Preference preference, PreferenceManager manager) {
        try {
            METHOD_ON_ATTACHED_TO_HIERARCHY.invoke(preference, manager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void setCanRecycleLayout(android.preference.Preference preference, boolean canRecycleLayout) {
        try {
            FIELD_CAN_RECYCLE_LAYOUT.set(preference, canRecycleLayout);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // N/A on 2.3
        }
    }
}
