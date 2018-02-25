package net.xpece.android.support.preference;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Eugen on 14.12.2015.
 */
public final class RingtoneManagerCompat extends RingtoneManager {
    static final String TAG = RingtoneManagerCompat.class.getSimpleName();

    private static final Field FIELD_CURSOR;
    private static final Method METHOD_GET_INTERNAL_RINGTONES;
    private static final Method METHOD_GET_MEDIA_RINGTONES;

    private static final RingtoneManagerImpl sImpl;

    private final Context mContext;

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

        Method getMediaRingtones = null;
        try {
            getMediaRingtones = RingtoneManager.class.getDeclaredMethod("getMediaRingtones");
            getMediaRingtones.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        METHOD_GET_MEDIA_RINGTONES = getMediaRingtones;

        if (Build.VERSION.SDK_INT >= 23) {
            sImpl = new RingtoneManagerImplV23();
        } else {
            sImpl = new RingtoneManagerImplBase();
        }
    }

    private void setCursorInternal(Cursor cursor) {
        try {
            FIELD_CURSOR.set(this, cursor);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Cursor getCursorInternal() {
        try {
            return (Cursor) FIELD_CURSOR.get(this);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Cursor getInternalRingtonesInternal() {
        try {
            return (Cursor) METHOD_GET_INTERNAL_RINGTONES.invoke(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    Cursor getMediaRingtonesInternal() {
        try {
            return (Cursor) METHOD_GET_MEDIA_RINGTONES.invoke(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public RingtoneManagerCompat(Activity activity) {
        super(activity);
        mContext = activity;
    }

    public RingtoneManagerCompat(Context context) {
        super(context);
        mContext = context;
    }

    /**
     * Returns a valid ringtone/notification/alarm URI. No guarantees on which it returns. If it
     * cannot find one, returns null. If it can only find one on external storage and the caller
     * doesn't have the {@link android.Manifest.permission#READ_EXTERNAL_STORAGE} permission,
     * returns null.
     *
     * @return A ringtone/notification/alarm URI, or null if one cannot be found.
     */
    @Nullable
    public Uri getValidRingtoneUri() {
        return getValidRingtoneUri(mContext, this);
    }


    /**
     * Returns a valid ringtone/notification/alarm URI. No guarantees on which it returns. If it
     * cannot find one, returns null. If it can only find one on external storage and the caller
     * doesn't have the {@link android.Manifest.permission#READ_EXTERNAL_STORAGE} permission,
     * returns null.
     *
     * @param context The context to use for querying.
     * @return A ringtone/notification/alarm URI, or null if one cannot be found.
     */
    @Nullable
    private static Uri getValidRingtoneUri(@NonNull Context context, @NonNull RingtoneManagerCompat rm) {
        Uri uri = getValidRingtoneUriFromCursorAndClose(context, rm.getInternalRingtones());

        if (uri == null) {
            uri = getValidRingtoneUriFromCursorAndClose(context, rm.getMediaRingtones());
        }

        return uri;
    }

    /**
     * Returns a valid ringtone URI. No guarantees on which it returns. If it
     * cannot find one, returns null. If it can only find one on external storage and the caller
     * doesn't have the {@link android.Manifest.permission#READ_EXTERNAL_STORAGE} permission,
     * returns null.
     *
     * @param context The context to use for querying.
     * @return A ringtone URI, or null if one cannot be found.
     */
    @Deprecated
    @Nullable
    public static Uri getValidRingtoneUri(@NonNull Context context) {
        return RingtoneManager.getValidRingtoneUri(context);
    }

    @Nullable
    private static Uri getValidRingtoneUriFromCursorAndClose(@NonNull Context context, @Nullable Cursor cursor) {
        if (cursor != null) {
            Uri uri = null;

            if (cursor.moveToFirst()) {
                uri = getUriFromCursor(cursor);
            }
            cursor.close();

            return uri;
        } else {
            return null;
        }
    }

    @NonNull
    private static Uri getUriFromCursor(@NonNull Cursor cursor) {
        return ContentUris.withAppendedId(Uri.parse(cursor.getString(URI_COLUMN_INDEX)), cursor
                .getLong(ID_COLUMN_INDEX));
    }

    @Override
    public Cursor getCursor() {
        Cursor mCursor = getCursorInternal();
        if (mCursor != null && mCursor.requery()) {
            return mCursor;
        }

        final Cursor internalCursor = getInternalRingtones();
        final Cursor mediaCursor = getMediaRingtones();

        mCursor = new SortCursor(new Cursor[]{internalCursor, mediaCursor},
            MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        setCursorInternal(mCursor);
        return mCursor;
    }

    private Cursor getInternalRingtones() {
        return getInternalRingtonesInternal();
    }

    private Cursor getMediaRingtones() {
        return sImpl.getMediaRingtones(mContext, this);
    }

    interface RingtoneManagerImpl {
        Cursor getMediaRingtones(Context context, RingtoneManagerCompat rm);
    }

    static class RingtoneManagerImplBase implements RingtoneManagerImpl {
        @Override
        public Cursor getMediaRingtones(Context context, RingtoneManagerCompat rm) {
            if (PackageManager.PERMISSION_GRANTED != context.checkPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Process.myPid(), Process.myUid())) {
                Log.w(TAG, "No READ_EXTERNAL_STORAGE permission, ignoring ringtones on ext storage");
                return null;
            }
            return rm.getMediaRingtonesInternal();
        }
    }

    static class RingtoneManagerImplV23 extends RingtoneManagerImplBase {
        @Override
        public Cursor getMediaRingtones(Context context, RingtoneManagerCompat rm) {
            return rm.getMediaRingtonesInternal();
        }
    }
}
