package net.xpece.android.support.preference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.support.annotation.RestrictTo;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Eugen on 14.12.2015.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressLint("PrivateApi")
public final class RingtoneManagerCompat extends RingtoneManager {
    private static final String TAG = RingtoneManagerCompat.class.getSimpleName();

    private static final Field FIELD_CURSOR;
    private static final Method METHOD_GET_INTERNAL_RINGTONES;

    static {
        Field cursor = null;
        try {
            cursor = RingtoneManager.class.getDeclaredField("mCursor");
            cursor.setAccessible(true);
        } catch (NoSuchFieldException ignore) {
        }
        FIELD_CURSOR = cursor;

        Method getInternalRingtones = null;
        try {
            getInternalRingtones = RingtoneManager.class.getDeclaredMethod("getInternalRingtones");
            getInternalRingtones.setAccessible(true);
        } catch (NoSuchMethodException ignore) {
        }
        METHOD_GET_INTERNAL_RINGTONES = getInternalRingtones;
    }

    private void setCursorInternal(Cursor cursor) {
        try {
            FIELD_CURSOR.set(this, cursor);
        } catch (Exception e) {
            throw new IllegalStateException("Platform implementation is different from AOSP.", e);
        }
    }

    private Cursor getInternalRingtones() {
        try {
            return (Cursor) METHOD_GET_INTERNAL_RINGTONES.invoke(this);
        } catch (Exception e) {
            throw new IllegalStateException("Platform implementation is different from AOSP.", e);
        }
    }

    public RingtoneManagerCompat(Activity activity) {
        super(activity);
    }

    public RingtoneManagerCompat(Context context) {
        super(context);
    }

    @Override
    public Cursor getCursor() {
        try {
            return super.getCursor();
        } catch (SecurityException ex) {
            Log.w(TAG, "No READ_EXTERNAL_STORAGE permission, ignoring ringtones on ext storage");
            if (getIncludeDrm()) {
                Log.w(TAG, "DRM ringtones are ignored.");
            }

            final Cursor cursor = getInternalRingtones();
            setCursorInternal(cursor);
            return cursor;
        }
    }
}
