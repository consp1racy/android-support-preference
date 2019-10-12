package net.xpece.android.support.preference;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import android.util.Log;

/**
 * Ringtone provides a quick method for playing a ringtone, notification, or
 * other similar types of sounds.
 * <p>
 * Retrieve {@link SafeRingtone} objects by {@link #obtain(Context, Uri)}.
 * <p>
 * This class works around some platform limitations:
 * <ul>
 * <li>Any ringtone can get title on API 23, otherwise external ringtone can only get title when
 * {@link android.Manifest.permission#READ_EXTERNAL_STORAGE} is granted.</li>
 * <li>Any ringtone can play on API 16, otherwise external ringtone can only play when
 * {@link android.Manifest.permission#READ_EXTERNAL_STORAGE} is granted.</li>
 * </ul>
 * Instead of throwing a {@link SecurityException}
 * <ul>
 * <li>if a sound cannot be played, there will be silence,</li>
 * <li>if a title cannot be obtained, localized "Unknown" will be returned.</li>
 * </ul>
 *
 * @see RingtoneManager
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SafeRingtone {
    private static final String TAG = SafeRingtone.class.getSimpleName();

    private static final int STREAM_NULL = Integer.MIN_VALUE;

    private static final String[] COLUMNS = new String[]{
            BaseColumns._ID
    };

    private final Context mContext;
    private final Uri mUri;

    private int mStreamType;

    private Ringtone mRingtone;

    @NonNull
    public static SafeRingtone obtain(@NonNull Context context, @Nullable Uri uri) {
        return new SafeRingtone(context.getApplicationContext(), uri);
    }

    @NonNull
    @SuppressWarnings("deprecation")
    public static SafeRingtone obtain(@NonNull Context context, @Nullable Uri uri, int streamType) {
        final SafeRingtone ringtone = new SafeRingtone(context.getApplicationContext(), uri);
        ringtone.setStreamType(streamType);
        return ringtone;
    }

    private static void peek(@NonNull Context context, @NonNull Uri uri) {
        if (Settings.AUTHORITY.equals(uri.getAuthority())) {
            final int type = RingtoneManager.getDefaultType(uri);
            // This can throw a SecurityException.
            final Uri actualUri = RingtoneManager.getActualDefaultRingtoneUri(context, type);
            if (actualUri != null) {
                // Actual Uri may be null on Android 4 emulators, where there are no ringtones.
                // Plus silent default ringtone sounds like a valid case.
                peek(context, actualUri);
            }
            return;
        }

        // This can throw a SecurityException.
        final ContentResolver res = context.getContentResolver();
        final Cursor cursor = res.query(uri, COLUMNS, null, null, null);
        if (cursor != null) {
            cursor.close();
        }
    }

    public static boolean canPlay(@NonNull Context context, @Nullable Uri uri) {
        if (uri == null) {
            // We can't play silence.
            return false;
        }
        if (Build.VERSION.SDK_INT >= 16) {
            return true;
        }
        try {
            peek(context, uri);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    public static boolean canGetTitle(@NonNull Context context, @Nullable Uri uri) {
        if (uri == null) {
            // We can display "None".
            return true;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            return true;
        }
        try {
            peek(context, uri);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    private SafeRingtone(final @NonNull Context context, @Nullable final Uri uri) {
        mContext = context;
        mUri = uri;
    }

    @Nullable
    private Ringtone getRingtone() {
        if (mRingtone == null) {
            final Ringtone ringtone = RingtoneManager.getRingtone(mContext, mUri);
            if (ringtone != null) {
                if (mStreamType != STREAM_NULL) {
                    ringtone.setStreamType(mStreamType);
                }
            }
            mRingtone = ringtone;
        }
        return mRingtone;
    }

    public boolean canPlay() {
        return canPlay(mContext, mUri);
    }

    public void play() {
        if (canPlay()) {
            final Ringtone ringtone = getRingtone();
            if (ringtone != null) {
                ringtone.play();
            } else {
                Log.w(TAG, "Ringtone at " + mUri + " cannot be played.");
            }
        } else {
            Log.w(TAG, "Ringtone at " + mUri + " cannot be played.");
        }
    }

    public void stop() {
        if (mRingtone != null) {
            mRingtone.stop();
        }
    }

    public boolean isPlaying() {
        return mRingtone != null && mRingtone.isPlaying();
    }

    /**
     * Sets the stream type where this ringtone will be played.
     *
     * @param streamType The stream, see {@link AudioManager}.
     */
    void setStreamType(int streamType) {
        if (streamType < -1) {
            throw new IllegalArgumentException("Invalid stream type: " + streamType);
        }
        mStreamType = streamType;
        if (mRingtone != null) {
            mRingtone.setStreamType(streamType);
        }
    }

    public boolean canGetTitle() {
        return canGetTitle(mContext, mUri);
    }

    @NonNull
    public String getTitle() {
        final Ringtone ringtone = getRingtone();
        if (ringtone != null) {
            if (Build.VERSION.SDK_INT >= 23) {
                // On API 23+ reading ringtone title is safe.
                return ringtone.getTitle(mContext);
            } else {
                // On API 16-22 Ringtone does nothing when reading from SD card without permission.
                // Below API 16 Ringtone crashes when we try to read from SD card without permission.
                // But that's just AOSP. Let's enforce any SecurityException here.
                try {
                    if (mUri != null) {
                        peek(mContext, mUri);
                    }
                    return ringtone.getTitle(mContext);
                } catch (SecurityException e) {
                    Log.w(TAG, "Cannot get title of ringtone at " + mUri + ".");
                    return RingtonePreference.getRingtoneUnknownString(mContext);
                }
            }
        } else {
            Log.w(TAG, "Cannot get title of ringtone at " + mUri + ".");
            return RingtonePreference.getRingtoneUnknownString(mContext);
        }
    }
}
