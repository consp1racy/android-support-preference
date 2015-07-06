/*
 * Copyright (C) 2007 The Android Open Source Project
 * Copyright (C) 2015 Eugen Pechanec
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

package net.xpece.android.support.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import net.xpece.android.support.preference.R;

/**
 * The {@link RingtonePickerActivity} allows the user to choose one from all of the
 * available ringtones. The chosen ringtone's URI will be persisted as a string.
 *
 * @see RingtoneManager#ACTION_RINGTONE_PICKER
 */
public final class RingtonePickerActivity extends AlertActivity implements
    AdapterView.OnItemSelectedListener, Runnable, DialogInterface.OnClickListener,
    AlertController.AlertParams.OnPrepareListViewListener {

    private static final int POS_UNKNOWN = -1;

    private static final String TAG = "RingtonePickerActivity";

    private static final int DELAY_MS_SELECTION_PLAYED = 300;

    private static final String SAVE_CLICKED_POS = "clicked_pos";

    private RingtoneManager mRingtoneManager;
    private int mType;

    private Cursor mCursor;
    private Handler mHandler;

    /** The position in the list of the 'Silent' item. */
    private int mSilentPos = POS_UNKNOWN;

    /** The position in the list of the 'Default' item. */
    private int mDefaultRingtonePos = POS_UNKNOWN;

    /** The position in the list of the last clicked item. */
    private int mClickedPos = POS_UNKNOWN;

    /** The position in the list of the ringtone to sample. */
    private int mSampleRingtonePos = POS_UNKNOWN;

    /** Whether this list has the 'Silent' item. */
    private boolean mHasSilentItem;

    /** The Uri to place a checkmark next to. */
    private Uri mExistingUri;

    /** The number of static items in the list. */
    private int mStaticItemCount;

    /** Whether this list has the 'Default' item. */
    private boolean mHasDefaultItem;

    /** The Uri to play when the 'Default' item is clicked. */
    private Uri mUriForDefaultItem;

    /**
     * A Ringtone for the default ringtone. In most cases, the RingtoneManager
     * will stop the previous ringtone. However, the RingtoneManager doesn't
     * manage the default ringtone for us, so we should stop this one manually.
     */
    private Ringtone mDefaultRingtone;

    /**
     * The ringtone that's currently playing, unless the currently playing one is the default
     * ringtone.
     */
    private Ringtone mCurrentRingtone;

    /**
     * Keep the currently playing ringtone around when changing orientation, so that it
     * can be stopped later, after the activity is recreated.
     */
    private static Ringtone sPlayingRingtone;

    private DialogInterface.OnClickListener mRingtoneClickListener =
        new DialogInterface.OnClickListener() {

            /*
             * On item clicked
             */
            public void onClick(DialogInterface dialog, int which) {
                // Save the position of most recently clicked item
                mClickedPos = which;

                // Play clip
                playRingtone(which, 0);
            }

        };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        Intent intent = getIntent();

        /*
         * Get whether to show the 'Default' item, and the URI to play when the
         * default is clicked
         */
        mHasDefaultItem = intent.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        mUriForDefaultItem = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI);
        if (mUriForDefaultItem == null) {
            mUriForDefaultItem = Settings.System.DEFAULT_RINGTONE_URI;
        }

        if (savedInstanceState != null) {
            mClickedPos = savedInstanceState.getInt(SAVE_CLICKED_POS, POS_UNKNOWN);
        }
        // Get whether to show the 'Silent' item
        mHasSilentItem = intent.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);

        // Give the Activity so it can do managed queries
        mRingtoneManager = new RingtoneManager(this);

        // Get the types of ringtones to show
        mType = intent.getIntExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, -1);
        if (mType != -1) {
            mRingtoneManager.setType(mType);
        }

        mCursor = mRingtoneManager.getCursor();

        // The volume keys will control the stream that we are choosing a ringtone for
        setVolumeControlStream(mRingtoneManager.inferStreamType());

        // Get the URI whose list item should have a checkmark
        mExistingUri = intent
            .getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI);

        final AlertController.AlertParams p = mAlertParams;
        p.mCursor = mCursor;
        p.mOnClickListener = mRingtoneClickListener;
        p.mLabelColumn = MediaStore.Audio.Media.TITLE;
        p.mIsSingleChoice = true;
        p.mOnItemSelectedListener = this;
        p.mPositiveButtonText = getString(android.R.string.ok);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(android.R.string.cancel);
        p.mPositiveButtonListener = this;
        p.mOnPrepareListViewListener = this;

        p.mTitle = intent.getCharSequenceExtra(RingtoneManager.EXTRA_RINGTONE_TITLE);
        if (p.mTitle == null) {
            p.mTitle = getRingtonePickerTitleString(this);
        }

        setupAlert();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_CLICKED_POS, mClickedPos);
    }

    public void onPrepareListView(ListView listView) {

        if (mHasDefaultItem) {
            mDefaultRingtonePos = addDefaultRingtoneItem(listView);

            if (mClickedPos == POS_UNKNOWN && RingtoneManager.isDefault(mExistingUri)) {
                mClickedPos = mDefaultRingtonePos;
            }
        }

        if (mHasSilentItem) {
            mSilentPos = addSilentItem(listView);

            // The 'Silent' item should use a null Uri
            if (mClickedPos == POS_UNKNOWN && mExistingUri == null) {
                mClickedPos = mSilentPos;
            }
        }

        if (mClickedPos == POS_UNKNOWN) {
            mClickedPos = getListPosition(mRingtoneManager.getRingtonePosition(mExistingUri));
        }

        // Put a checkmark next to an item.
        mAlertParams.mCheckedItem = mClickedPos;
    }

    /**
     * Adds a static item to the top of the list. A static item is one that is not from the
     * RingtoneManager.
     *
     * @param listView The ListView to add to.
     * @param text Text for the item.
     * @return The position of the inserted item.
     */
    private int addStaticItem(ListView listView, String text) {
        TextView textView = (TextView) getLayoutInflater().inflate(R.layout.select_dialog_singlechoice_material, listView, false);
        textView.setText(text);
        listView.addHeaderView(textView);
        mStaticItemCount++;
        return listView.getHeaderViewsCount() - 1;
    }

    private int addDefaultRingtoneItem(ListView listView) {
        if (mType == RingtoneManager.TYPE_NOTIFICATION) {
            return addStaticItem(listView, getNotificationSoundDefaultString(this));
        } else if (mType == RingtoneManager.TYPE_ALARM) {
            return addStaticItem(listView, getAlarmSoundDefaultString(this));
        }

        return addStaticItem(listView, getRingtoneDefaultString(this));
    }

    private int addSilentItem(ListView listView) {
        return addStaticItem(listView, getRingtoneSilentString(this));
    }

    /*
     * On click of Ok/Cancel buttons
     */
    public void onClick(DialogInterface dialog, int which) {
        boolean positiveResult = which == BUTTON_POSITIVE;

        // Stop playing the previous ringtone
        mRingtoneManager.stopPreviousRingtone();

        if (positiveResult) {
            Intent resultIntent = new Intent();
            Uri uri = null;

            if (mClickedPos == mDefaultRingtonePos) {
                // Set it to the default Uri that they originally gave us
                uri = mUriForDefaultItem;
            } else if (mClickedPos == mSilentPos) {
                // A null Uri is for the 'Silent' item
                uri = null;
            } else {
                uri = mRingtoneManager.getRingtoneUri(getRingtoneManagerPosition(mClickedPos));
            }

            resultIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, uri);
            setResult(RESULT_OK, resultIntent);
        } else {
            setResult(RESULT_CANCELED);
        }

        getWindow().getDecorView().post(new Runnable() {
            public void run() {
                mCursor.deactivate();
            }
        });

        finish();
    }

    /*
     * On item selected via keys
     */
    public void onItemSelected(AdapterView parent, View view, int position, long id) {
        playRingtone(position, DELAY_MS_SELECTION_PLAYED);
    }

    public void onNothingSelected(AdapterView parent) {
    }

    private void playRingtone(int position, int delayMs) {
        mHandler.removeCallbacks(this);
        mSampleRingtonePos = position;
        mHandler.postDelayed(this, delayMs);
    }

    public void run() {
        stopAnyPlayingRingtone();
        if (mSampleRingtonePos == mSilentPos) {
            return;
        }

        Ringtone ringtone;
        if (mSampleRingtonePos == mDefaultRingtonePos) {
            if (mDefaultRingtone == null) {
                mDefaultRingtone = RingtoneManager.getRingtone(this, mUriForDefaultItem);
            }
           /*
            * Stream type of mDefaultRingtone is not set explicitly here.
            * It should be set in accordance with mRingtoneManager of this Activity.
            */
            if (mDefaultRingtone != null) {
                mDefaultRingtone.setStreamType(mRingtoneManager.inferStreamType());
            }
            ringtone = mDefaultRingtone;
            mCurrentRingtone = null;
        } else {
            ringtone = mRingtoneManager.getRingtone(getRingtoneManagerPosition(mSampleRingtonePos));
            mCurrentRingtone = ringtone;
        }

        if (ringtone != null) {
            ringtone.play();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT < 11 || !isChangingConfigurations()) {
            stopAnyPlayingRingtone();
        } else {
            saveAnyPlayingRingtone();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT < 11 || !isChangingConfigurations()) {
            stopAnyPlayingRingtone();
        }
    }

    private void saveAnyPlayingRingtone() {
        if (mDefaultRingtone != null && mDefaultRingtone.isPlaying()) {
            sPlayingRingtone = mDefaultRingtone;
        } else if (mCurrentRingtone != null && mCurrentRingtone.isPlaying()) {
            sPlayingRingtone = mCurrentRingtone;
        }
    }

    private void stopAnyPlayingRingtone() {
        if (sPlayingRingtone != null && sPlayingRingtone.isPlaying()) {
            sPlayingRingtone.stop();
        }
        sPlayingRingtone = null;

        if (mDefaultRingtone != null && mDefaultRingtone.isPlaying()) {
            mDefaultRingtone.stop();
        }

        if (mRingtoneManager != null) {
            mRingtoneManager.stopPreviousRingtone();
        }
    }

    private int getRingtoneManagerPosition(int listPos) {
        return listPos - mStaticItemCount;
    }

    private int getListPosition(int ringtoneManagerPos) {

        // If the manager position is -1 (for not found), return that
        if (ringtoneManagerPos < 0) return ringtoneManagerPos;

        return ringtoneManagerPos + mStaticItemCount;
    }

    public static String getRingtonePickerTitleString(Context context) {
        int resId = context.getApplicationContext().getResources().getIdentifier("ringtone_picker_title", "string", "android");
        if (resId == 0) {
            resId = R.string.ringtone_picker_title;
        }
        return context.getApplicationContext().getString(resId);
    }

    public static String getNotificationSoundDefaultString(Context context) {
        try {
            Resources res = context.getApplicationContext().getPackageManager().getResourcesForApplication("com.android.providers.media");
            int resId = res.getIdentifier("notification_sound_default", "string", "com.android.providers.media");
            return res.getString(resId);
        } catch (PackageManager.NameNotFoundException e) {
            return context.getApplicationContext().getString(R.string.notification_sound_default);
        } catch (Resources.NotFoundException e) {
            return context.getApplicationContext().getString(R.string.notification_sound_default);
        }
    }

    public static String getAlarmSoundDefaultString(Context context) {
        try {
            Resources res = context.getApplicationContext().getPackageManager().getResourcesForApplication("com.android.providers.media");
            int resId = res.getIdentifier("alarm_sound_default", "string", "com.android.providers.media");
            return res.getString(resId);
        } catch (PackageManager.NameNotFoundException e) {
            return context.getApplicationContext().getString(R.string.alarm_sound_default);
        } catch (Resources.NotFoundException e) {
            return context.getApplicationContext().getString(R.string.notification_sound_default);
        }
    }

    public static String getRingtoneDefaultString(Context context) {
        int resId = context.getApplicationContext().getResources().getIdentifier("ringtone_default", "string", "android");
        if (resId == 0) {
            resId = R.string.ringtone_default;
        }
        return context.getApplicationContext().getString(resId);
    }

    public static String getRingtoneSilentString(Context context) {
        int resId = context.getApplicationContext().getResources().getIdentifier("ringtone_silent", "string", "android");
        if (resId == 0) {
            resId = R.string.ringtone_silent;
        }
        return context.getApplicationContext().getString(resId);
    }

}
