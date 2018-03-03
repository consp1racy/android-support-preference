package net.xpece.android.support.preference;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.os.Build;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class RingtoneManagerLegacy extends RingtoneManager {
    private static final Field FIELD_CURSOR;
    private static final Method METHOD_GET_INTERNAL_RINGTONES;

    static {
        Field cursor = null;
        try {
            cursor = RingtoneManager.class.getDeclaredField("mCursor");
            cursor.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        FIELD_CURSOR = cursor;

        Method getInternalRingtones = null;
        try {
            getInternalRingtones = RingtoneManager.class.getDeclaredMethod("getInternalRingtones");
            getInternalRingtones.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        METHOD_GET_INTERNAL_RINGTONES = getInternalRingtones;
    }

    private void setCursorInternal(Cursor cursor) {
        try {
            FIELD_CURSOR.set(this, cursor);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Cursor getInternalRingtones() {
        try {
            return (Cursor) METHOD_GET_INTERNAL_RINGTONES.invoke(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void checkAndroidVersion() {
        if (Build.VERSION.SDK_INT < 23) {
            throw new IllegalStateException("Use RingtoneManager on API 23+ instead.");
        }
    }

    public RingtoneManagerLegacy(Activity activity) {
        super(activity);
        checkAndroidVersion();
    }

    public RingtoneManagerLegacy(Context context) {
        super(context);
        checkAndroidVersion();
    }

    @Override
    public Cursor getCursor() {
        try {
            return super.getCursor();
        } catch (SecurityException ex) {
            final Cursor cursor = getInternalRingtones();
            setCursorInternal(cursor);
            return cursor;
        }
    }
}
