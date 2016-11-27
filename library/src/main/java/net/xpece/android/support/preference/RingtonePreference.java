/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.xpece.android.support.preference;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings.System;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;

/**
 * A {@link Preference} that allows the user to choose a ringtone from those on the device.
 * The chosen ringtone's URI will be persisted as a string.
 * <p></p>
 * If the user chooses the "Default" item, the saved string will be one of
 * {@link System#DEFAULT_RINGTONE_URI},
 * {@link System#DEFAULT_NOTIFICATION_URI}, or
 * {@link System#DEFAULT_ALARM_ALERT_URI}. If the user chooses the "Silent"
 * item, the saved string will be an empty string.
 * <p/>
 * See https://code.google.com/p/android/issues/detail?id=183255.
 */
public class RingtonePreference extends DialogPreference {

    private static final String TAG = "RingtonePreference";

    private int mRingtoneType;
    private boolean mShowDefault;
    private boolean mShowSilent;

    public RingtonePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public RingtonePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RingtonePreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.ringtonePreferenceStyle);
    }

    public RingtonePreference(Context context) {
        this(context, null);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RingtonePreference, defStyleAttr, defStyleRes);
        mRingtoneType = a.getInt(R.styleable.RingtonePreference_android_ringtoneType, RingtoneManager.TYPE_RINGTONE);
        mShowDefault = a.getBoolean(R.styleable.RingtonePreference_android_showDefault, true);
        mShowSilent = a.getBoolean(R.styleable.RingtonePreference_android_showSilent, true);
        a.recycle();
    }

    /**
     * Returns the sound type(s) that are shown in the picker.
     *
     * @return The sound type(s) that are shown in the picker.
     * @see #setRingtoneType(int)
     */
    public int getRingtoneType() {
        return mRingtoneType;
    }

    /**
     * Sets the sound type(s) that are shown in the picker.
     *
     * @param type The sound type(s) that are shown in the picker.
     * @see RingtoneManager#EXTRA_RINGTONE_TYPE
     */
    public void setRingtoneType(int type) {
        mRingtoneType = type;
    }

    /**
     * Returns whether to a show an item for the default sound/ringtone.
     *
     * @return Whether to show an item for the default sound/ringtone.
     */
    public boolean getShowDefault() {
        return mShowDefault;
    }

    /**
     * Sets whether to show an item for the default sound/ringtone. The default
     * to use will be deduced from the sound type(s) being shown.
     *
     * @param showDefault Whether to show the default or not.
     * @see RingtoneManager#EXTRA_RINGTONE_SHOW_DEFAULT
     */
    public void setShowDefault(boolean showDefault) {
        mShowDefault = showDefault;
    }

    /**
     * Returns whether to a show an item for 'Silent'.
     *
     * @return Whether to show an item for 'Silent'.
     */
    public boolean getShowSilent() {
        return mShowSilent;
    }

    /**
     * Sets whether to show an item for 'Silent'.
     *
     * @param showSilent Whether to show 'Silent'.
     * @see RingtoneManager#EXTRA_RINGTONE_SHOW_SILENT
     */
    public void setShowSilent(boolean showSilent) {
        mShowSilent = showSilent;
    }

    /**
     * Called when a ringtone is chosen.
     * <p></p>
     * By default, this saves the ringtone URI to the persistent storage as a
     * string.
     *
     * @param ringtoneUri The chosen ringtone's {@link Uri}. Can be null.
     */
    public void onSaveRingtone(Uri ringtoneUri) {
        persistString(ringtoneUri != null ? ringtoneUri.toString() : "");
    }

    /**
     * Called when the chooser is about to be shown and the current ringtone
     * should be marked. Can return null to not mark any ringtone.
     * <p></p>
     * By default, this restores the previous ringtone URI from the persistent
     * storage.
     *
     * @return The ringtone to be marked as the current ringtone.
     */
    public Uri onRestoreRingtone() {
        final String uriString = getPersistedString(null);
        return !TextUtils.isEmpty(uriString) ? Uri.parse(uriString) : null;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValueObj) {
        String defaultValue = (String) defaultValueObj;

        /*
         * This method is normally to make sure the internal state and UI
         * matches either the persisted value or the default value. Since we
         * don't show the current value in the UI (until the dialog is opened)
         * and we don't keep local state, if we are restoring the persisted
         * value we don't need to do anything.
         */
        if (restorePersistedValue) {
            return;
        }

        // If we are setting to the default value, we should persist it.
        if (!TextUtils.isEmpty(defaultValue)) {
            onSaveRingtone(Uri.parse(defaultValue));
        }
    }

    /**
     * Creates system ringtone picker intent for manual use.
     * @return
     */
    public Intent buildRingtonePickerIntent() {
        int type = getRingtoneType();
        Intent i = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, onRestoreRingtone());
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(type));
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, getShowDefault());
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, getShowSilent());
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, type);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getNonEmptyDialogTitle());
        return i;
    }

    /**
     * Use this method to process selected ringtone if you manually opened system ringtone picker
     * by {@link RingtoneManager#ACTION_RINGTONE_PICKER}.
     * @param data
     */
    public void onActivityResult(Intent data) {
        if (data != null) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

            if (callChangeListener(uri != null ? uri.toString() : "")) {
                onSaveRingtone(uri);
            }
        }
    }

    CharSequence getNonEmptyDialogTitle() {
        CharSequence title = getDialogTitle();
        if (title == null) {
            title = getTitle();
        }
        if (TextUtils.isEmpty(title)) {
            title = getRingtonePickerTitleString(getContext());
        }
        return title;
    }

    public static String getRingtoneTitle(Context context, Uri uri) {
        if (uri == null) {
            return null;
        } else {
            return RingtoneManager.getRingtone(context, uri).getTitle(context);
        }
    }

    public static String getNotificationSoundDefaultString(Context context) {
        context = context.getApplicationContext();
        try {
            Resources res = context.getPackageManager().getResourcesForApplication("com.android.providers.media");
            int resId = res.getIdentifier("notification_sound_default", "string", "com.android.providers.media");
            return res.getString(resId);
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            return context.getString(R.string.notification_sound_default);
        }
    }

    public static String getAlarmSoundDefaultString(Context context) {
        context = context.getApplicationContext();
        try {
            Resources res = context.getPackageManager().getResourcesForApplication("com.android.providers.media");
            int resId = res.getIdentifier("alarm_sound_default", "string", "com.android.providers.media");
            return res.getString(resId);
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            return context.getString(R.string.alarm_sound_default);
        }
    }

    public static String getRingtoneDefaultString(Context context) {
        context = context.getApplicationContext();
        int resId = context.getResources().getIdentifier("ringtone_default", "string", "android");
        if (resId == 0) {
            resId = R.string.ringtone_default;
        }
        return context.getString(resId);
    }

    public static String getRingtoneSilentString(Context context) {
        context = context.getApplicationContext();
        int resId = context.getResources().getIdentifier("ringtone_silent", "string", "android");
        if (resId == 0) {
            resId = R.string.ringtone_silent;
        }
        return context.getString(resId);
    }

    public static String getRingtonePickerTitleString(Context context) {
        int resId = context.getApplicationContext().getResources().getIdentifier("ringtone_picker_title", "string", "android");
        if (resId == 0) {
            resId = R.string.ringtone_picker_title;
        }
        return context.getApplicationContext().getString(resId);
    }

}
