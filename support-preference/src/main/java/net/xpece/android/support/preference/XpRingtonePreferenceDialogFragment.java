package net.xpece.android.support.preference;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import net.xpece.android.support.preference.plugins.XpSupportPreferencePlugins;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static net.xpece.android.support.preference.Util.checkPreferenceNotNull;

/**
 * Created by Eugen on 07.12.2015.
 */
public class XpRingtonePreferenceDialogFragment extends XpPreferenceDialogFragment
        implements Runnable, AdapterView.OnItemSelectedListener {

    private static int RC_FALLBACK_RINGTONE_PICKER = 0xff00; // <0; 0xffff>

    private static String KEY_FALLBACK_RINGTONE_PICKER = "net.xpece.android.support.preference.FALLBACK_RINGTONE_PICKER";

    private static final int POS_UNKNOWN = -1;

    private static final int DELAY_MS_SELECTION_PLAYED = 300;

    private static final String SAVE_CLICKED_POS = "clicked_pos";

    private RingtoneManager mRingtoneManager;
    private int mType;

    private Cursor mCursor;
    private Handler mHandler;

    private int mUnknownPos = POS_UNKNOWN;

    /**
     * The position in the list of the 'Silent' item.
     */
    private int mSilentPos = POS_UNKNOWN;

    /**
     * The position in the list of the 'Default' item.
     */
    private int mDefaultRingtonePos = POS_UNKNOWN;

    /**
     * The position in the list of the last clicked item.
     */
    int mClickedPos = POS_UNKNOWN;

    /**
     * The position in the list of the ringtone to sample.
     */
    private int mSampleRingtonePos = POS_UNKNOWN;

    /**
     * Whether this list has the 'Silent' item.
     */
    private boolean mHasSilentItem;

    /**
     * The Uri to place a checkmark next to.
     */
    private Uri mExistingUri;

    /**
     * The number of static items in the list.
     */
    private final ArrayList<XpHeaderViewListAdapter.FixedViewInfo> mStaticItems = new ArrayList<>();

    /**
     * Whether this list has the 'Default' item.
     */
    private boolean mHasDefaultItem;

    /**
     * The Uri to play when the 'Default' item is clicked.
     */
    private Uri mUriForDefaultItem;

    private Ringtone mUnknownRingtone;

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

    private final DialogInterface.OnClickListener mRingtoneClickListener =
            new DialogInterface.OnClickListener() {

                /*
                 * On item clicked
                 */
                public void onClick(@NonNull DialogInterface dialog, int which) {
                    // Save the position of most recently clicked item
                    mClickedPos = which;

                    // Play clip
                    playRingtone(which, 0);
                }

            };

    private boolean mActivityCreated = false;

    @NonNull
    public static XpRingtonePreferenceDialogFragment newInstance(@NonNull String key) {
        XpRingtonePreferenceDialogFragment fragment = new XpRingtonePreferenceDialogFragment();
        Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mActivityCreated = true;

        loadRingtoneManager(savedInstanceState);

        if (getDialog() instanceof DummyAlertDialog) {
            // Reinstall the real dialog now if we don't have custom view.
            // The resulting layout inflater will be discarded. First call result is preserved.
            // Fragment-Dialog listeners are attached in super.onActivityCreated. Do this before.
            getDialog().dismiss();
            onGetLayoutInflater(savedInstanceState);
        }

        super.onActivityCreated(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (mActivityCreated) {
            return super.onCreateDialog(savedInstanceState);
        } else {
            // Dummy. Will be replaced with real dialog in onActivityCreated.
            // LayoutInflater from the dialog builder will remain cached in this fragment.
            return new DummyAlertDialog(getContext());
        }
    }

    private void loadRingtoneManager(@Nullable Bundle savedInstanceState) {
        // Give the Activity so it can do managed queries
        mRingtoneManager = new RingtoneManagerCompat(getActivity());

        final boolean fallbackRingtonePicker;
        if (savedInstanceState != null) {
            mClickedPos = savedInstanceState.getInt(SAVE_CLICKED_POS, POS_UNKNOWN);
            fallbackRingtonePicker = savedInstanceState.getBoolean(KEY_FALLBACK_RINGTONE_PICKER);
        } else {
            fallbackRingtonePicker = false;
        }

        if (fallbackRingtonePicker) {
            setShowsDialog(false);
        } else {
            RingtonePreference preference = requireRingtonePreference();

            /*
             * Get whether to show the 'Default' item, and the URI to play when the
             * default is clicked
             */
            mHasDefaultItem = preference.getShowDefault();
            mUriForDefaultItem = RingtoneManager.getDefaultUri(preference.getRingtoneType());

            // Get whether to show the 'Silent' item
            mHasSilentItem = preference.getShowSilent();

            // Get the types of ringtones to show
            mType = preference.getRingtoneType();
            if (mType != -1) {
                mRingtoneManager.setType(mType);
            }

            // Get the URI whose list item should have a checkmark
            mExistingUri = preference.onRestoreRingtone();

            try {
                mCursor = mRingtoneManager.getCursor();

                // Check if cursor is valid.
                mCursor.getColumnNames();
            } catch (IllegalStateException ex) {
                // We throw this when there's an error with reflective method lookup.
                recover(preference, ex);
            } catch (IllegalArgumentException ex) {
                recover(preference, ex);
            } catch (Exception ex) {
                // You know what, fallback to system picker on any error. Example:
                // https://github.com/consp1racy/android-support-preference/issues/117
                recover(preference, ex);
            }
        }
    }

    private void recover(final @NonNull RingtonePreference preference, final @NonNull Throwable ex) {
        XpSupportPreferencePlugins.onError(ex, "RingtoneManager returned unexpected cursor.");

        mCursor = null;
        setShowsDialog(false);

        // Alternatively try starting system picker.
        Intent i = preference.buildRingtonePickerIntent();
        try {
            startActivityForResult(i, RC_FALLBACK_RINGTONE_PICKER);
        } catch (ActivityNotFoundException ex2) {
            onRingtonePickerNotFound(RC_FALLBACK_RINGTONE_PICKER);
        }
    }

    /**
     * Called when there's no ringtone picker available in the system.
     * Let the user know (using e.g. a Toast).
     * Just dismisses this fragment by default.
     *
     * @param requestCode You can use this code to launch another activity instead of dismissing
     *                    this fragment. The result must contain
     *                    {@link RingtoneManager#EXTRA_RINGTONE_PICKED_URI} extra.
     */
    public void onRingtonePickerNotFound(final int requestCode) {
        dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_FALLBACK_RINGTONE_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                requireRingtonePreference().onActivityResult(data);
            }
            dismiss();
        }
    }

    private void onOnPrepareDialogBuilder(@NonNull AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        RingtonePreference preference = requireRingtonePreference();

        // The volume keys will control the stream that we are choosing a ringtone for
        getActivity().setVolumeControlStream(mRingtoneManager.inferStreamType());

        CharSequence title = preference.getNonEmptyDialogTitle();
        builder.setTitle(title);

        final Context context = builder.getContext();
        TypedArray a = context.obtainStyledAttributes(null, R.styleable.AlertDialog, R.attr.alertDialogStyle, 0);
        int singleChoiceItemLayout = a.getResourceId(R.styleable.AlertDialog_singleChoiceItemLayout, 0);
        a.recycle();

        final LayoutInflater inflater = LayoutInflater.from(context);
        final boolean isDefault = RingtoneManager.isDefault(mExistingUri);
        if (mHasDefaultItem /*|| isDefault*/) {
            mDefaultRingtonePos = addDefaultRingtoneItem(inflater, singleChoiceItemLayout);

            if (mClickedPos == POS_UNKNOWN && isDefault) {
                mClickedPos = mDefaultRingtonePos;
            }
        }

        final boolean isSilent = mExistingUri == null;
        if (mHasSilentItem /*|| isSilent*/) {
            mSilentPos = addSilentItem(inflater, singleChoiceItemLayout);

            // The 'Silent' item should use a null Uri
            if (mClickedPos == POS_UNKNOWN && isSilent) {
                mClickedPos = mSilentPos;
            }
        }

        if (mClickedPos == POS_UNKNOWN) {
            try {
            mClickedPos = getListPosition(mRingtoneManager.getRingtonePosition(mExistingUri));
            } catch (NumberFormatException e) {
                // This can happen on Android Q Beta 6 if the Uri doesn't end with a number.
                // https://github.com/consp1racy/android-support-preference/issues/120
                // https://issuetracker.google.com/issues/139935440
                final String message = "Couldn't resolve ringtone position: " + mExistingUri;
                XpSupportPreferencePlugins.onError(e, message);
            }
        }

        // If we still don't have selected item, but we're not silent, show the 'Unknown' item.
        if (mClickedPos == POS_UNKNOWN && mExistingUri != null) {
            final String ringtoneTitle;
            final SafeRingtone ringtone = SafeRingtone.obtain(context, mExistingUri);
            try {
                // We may not be able to list external ringtones
                // but we may be able to show selected external ringtone title.
                if (ringtone.canGetTitle()) {
                    ringtoneTitle = ringtone.getTitle();
                } else {
                    ringtoneTitle = null;
                }
            } finally {
                ringtone.stop();
            }
            if (ringtoneTitle == null) {
                mUnknownPos = addUnknownItem(inflater, singleChoiceItemLayout);
            } else {
                mUnknownPos = addStaticItem(inflater, singleChoiceItemLayout, ringtoneTitle);
            }
            mClickedPos = mUnknownPos;
        }

        SimpleCursorAdapter ringtoneAdapter = new SimpleCursorAdapter(context, singleChoiceItemLayout, mCursor,
                new String[]{MediaStore.Audio.Media.TITLE}, new int[]{android.R.id.text1});

        XpHeaderViewListAdapter adapter = new XpHeaderViewListAdapter(mStaticItems, null, ringtoneAdapter);

        // Put a checkmark next to an item.
        builder.setSingleChoiceItems(adapter, mClickedPos, mRingtoneClickListener);

        builder.setOnItemSelectedListener(this);
    }

    @Override
    protected void onPrepareDialogBuilder(@NonNull AlertDialog.Builder builder) {
        try {
            onOnPrepareDialogBuilder(builder);
        } catch (Throwable e) {
            recover(getRingtonePreference(), e);
        }
    }

    /**
     * Adds a static item to the top of the list. A static item is one that is not from the
     * RingtoneManager.
     *
     * @param text Text for the item.
     * @return The position of the inserted item.
     */
    private int addStaticItem(@NonNull LayoutInflater inflater, @LayoutRes int layout, CharSequence text) {
        TextView textView = (TextView) inflater.inflate(layout, null, false);
        textView.setText(text);

        XpHeaderViewListAdapter.FixedViewInfo item = new XpHeaderViewListAdapter.FixedViewInfo();
        item.view = textView;
        item.isSelectable = true;

        mStaticItems.add(item);
        return mStaticItems.size() - 1;
    }

    private int addDefaultRingtoneItem(@NonNull LayoutInflater inflater, @LayoutRes int layout) {
        switch (mType) {
            case RingtoneManager.TYPE_NOTIFICATION:
                return addStaticItem(inflater, layout, RingtonePreference.getNotificationSoundDefaultString(getContext()));
            case RingtoneManager.TYPE_ALARM:
                return addStaticItem(inflater, layout, RingtonePreference.getAlarmSoundDefaultString(getContext()));
            default:
                return addStaticItem(inflater, layout, RingtonePreference.getRingtoneDefaultString(getContext()));
        }
    }

    private int addSilentItem(@NonNull LayoutInflater inflater, @LayoutRes int layout) {
        return addStaticItem(inflater, layout, RingtonePreference.getRingtoneSilentString(getContext()));
    }

    private int addUnknownItem(@NonNull LayoutInflater inflater, @LayoutRes int layout) {
        return addStaticItem(inflater, layout, RingtonePreference.getRingtoneUnknownString(getContext()));
    }

    private int getListPosition(int ringtoneManagerPos) {
        // If the manager position is -1 (for not found), return that
        if (ringtoneManagerPos < 0) return POS_UNKNOWN;

        return ringtoneManagerPos + mStaticItems.size();
    }

    @Override
    public void onItemSelected(@NonNull AdapterView<?> parent, @NonNull View view, int position, long id) {
        playRingtone(position, DELAY_MS_SELECTION_PLAYED);
    }

    @Override
    public void onNothingSelected(@NonNull AdapterView<?> parent) {
        // No-op.
    }

    @Override
    public void onStart() {
        if (!getShowsDialog() && getDialog() != null) {
            try {
                // Don't show the dialog if we failed during onPrepareDialogBuilder.
                final Field f = DialogFragment.class.getDeclaredField("mDialog");
                f.setAccessible(true);
                f.set(this, null);
            } catch (IllegalAccessException ignore) {
            } catch (NoSuchFieldException ignore) {
            }
        }

        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!getActivity().isChangingConfigurations()) {
            stopAnyPlayingRingtone();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!getActivity().isChangingConfigurations()) {
            stopAnyPlayingRingtone();
        } else {
            saveAnyPlayingRingtone();
        }
    }

    @Override
    public void onSaveInstanceState(final @NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_CLICKED_POS, mClickedPos);
        outState.putBoolean(KEY_FALLBACK_RINGTONE_PICKER, !getShowsDialog());
    }

    @Nullable
    public RingtonePreference getRingtonePreference() {
        return (RingtonePreference) getPreference();
    }

    @NonNull
    protected RingtonePreference requireRingtonePreference() {
        return checkPreferenceNotNull(getRingtonePreference(), RingtonePreference.class, this);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        // Stop playing the previous ringtone
        if (sPlayingRingtone == null) {
            mRingtoneManager.stopPreviousRingtone();
        }

        // The volume keys will control the default stream
        if (getActivity() != null) {
            getActivity().setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
        }

        if (positiveResult) {
            Uri uri;
            if (mClickedPos == mDefaultRingtonePos) {
                // Set it to the default Uri that they originally gave us
                uri = mUriForDefaultItem;
            } else if (mClickedPos == mSilentPos) {
                // A null Uri is for the 'Silent' item
                uri = null;
            } else if (mClickedPos == mUnknownPos) {
                // 'Unknown' was shown because it was persisted before showing the picker.
                // There's no change to persist, return immediately.
                return;
            } else {
                uri = mRingtoneManager.getRingtoneUri(getRingtoneManagerPosition(mClickedPos));
            }

            requireRingtonePreference().saveRingtone(uri);
        }
    }

    void playRingtone(int position, int delayMs) {
        mHandler.removeCallbacks(this);
        mSampleRingtonePos = position;
        mHandler.postDelayed(this, delayMs);
    }

    @NonNull
    private <T> T checkNotNull(final @Nullable T thing, final @NonNull String name) {
        if (thing == null) throw new IllegalStateException(name + " was null.");
        return thing;
    }

    public void run() {
        stopAnyPlayingRingtone();
        if (mSampleRingtonePos == mSilentPos) {
            return;
        }

//        final int oldSampleRingtonePos = mSampleRingtonePos;
        try {
            Ringtone ringtone = null;
            if (mSampleRingtonePos == mDefaultRingtonePos) {
                if (mDefaultRingtone == null) {
                    try {
                        checkNotNull(mUriForDefaultItem, "mUriForDefaultItem");
                        mDefaultRingtone = RingtoneManager.getRingtone(getContext(), mUriForDefaultItem);
                    } catch (SecurityException | IllegalStateException ex) {
                        XpSupportPreferencePlugins.onError(ex, "Failed to create default Ringtone from " + mUriForDefaultItem + ".");
                    }
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
            } else if (mSampleRingtonePos == mUnknownPos) {
                if (mUnknownRingtone == null) {
                    try {
                        checkNotNull(mExistingUri, "mExistingUri");
                        mUnknownRingtone = RingtoneManager.getRingtone(getContext(), mExistingUri);
                    } catch (SecurityException | IllegalStateException ex) {
                        XpSupportPreferencePlugins.onError(ex, "Failed to create unknown Ringtone from " + mExistingUri + ".");
                    }
                }
                if (mUnknownRingtone != null) {
                    mUnknownRingtone.setStreamType(mRingtoneManager.inferStreamType());
                }
                ringtone = mUnknownRingtone;
                mCurrentRingtone = null;
            } else {
                final int position = getRingtoneManagerPosition(mSampleRingtonePos);
                try {
                    ringtone = mRingtoneManager.getRingtone(position);
                } catch (SecurityException ex) {
                    XpSupportPreferencePlugins.onError(ex, "Failed to create selected Ringtone from " + mRingtoneManager.getRingtoneUri(position) + ".");
                }
                mCurrentRingtone = ringtone;
            }

            if (ringtone != null) {
                try {
                    ringtone.play();
                } catch (NullPointerException ex) {
                    XpSupportPreferencePlugins.onError(ex, "RingtoneManager produced a Ringtone with null Uri.");
                    // https://github.com/consp1racy/android-support-preference/issues/105
                    // RingtoneManager can produce Ringtones with null Uri. Attempts to play fail.
                    mCurrentRingtone = null;
                    ringtone.stop();
                }
            }
        } catch (SecurityException ex) {
            // Don't play the inaccessible default ringtone.
            XpSupportPreferencePlugins.onError(ex, "Failed to play Ringtone.");
//            mSampleRingtonePos = oldSampleRingtonePos;
        }
    }

    private void saveAnyPlayingRingtone() {
        if (mDefaultRingtone != null && mDefaultRingtone.isPlaying()) {
            sPlayingRingtone = mDefaultRingtone;
        } else if (mUnknownRingtone != null && mUnknownRingtone.isPlaying()) {
            sPlayingRingtone = mUnknownRingtone;
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

        if (mUnknownRingtone != null && mUnknownRingtone.isPlaying()) {
            mUnknownRingtone.stop();
        }

        if (mRingtoneManager != null) {
            mRingtoneManager.stopPreviousRingtone();
        }
    }

    private int getRingtoneManagerPosition(int listPos) {
        return listPos - mStaticItems.size();
    }

    private static class DummyAlertDialog extends AlertDialog {
        DummyAlertDialog(@NonNull Context context) {
            super(context);
        }
    }
}
