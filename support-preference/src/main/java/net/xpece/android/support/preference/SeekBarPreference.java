/*
 * Copyright (C) 2011 The Android Open Source Project
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
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.util.Map;
import java.util.WeakHashMap;

public class SeekBarPreference extends Preference {
    static final String TAG = SeekBarPreference.class.getSimpleName();

    // A filthy hack so we can update info text while dragging seek bar thumb.
    private static final WeakHashMap<TextView, SeekBarPreference> mInfoViews = new WeakHashMap<>();

    int mSeekBarValue;
    int mMin = 0;
    private int mMax = 100;
    private int mSeekBarIncrement = 0;
    boolean mTrackingTouch;

    // whether the seekbar should respond to the left/right keys
    boolean mAdjustable = true;
    // whether to show the seekbar value TextView next to the bar
    // preference-v7 has true, here we set false for backwards compatibility with previous releases.
    private boolean mShowSeekBarValue = false;

    private CharSequence mInfo;
    OnSeekBarChangeListener mUserSeekBarChangeListener;
    private int mInfoAnchorId;

    /**
     * Listener reacting to the SeekBar changing value by the user
     */
    private final OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && !mTrackingTouch) {
                syncValueInternal(seekBar);
            }
            if (mUserSeekBarChangeListener != null) {
                mUserSeekBarChangeListener.onProgressChanged(seekBar, progress + mMin, fromUser);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mTrackingTouch = true;
            if (mUserSeekBarChangeListener != null) {
                mUserSeekBarChangeListener.onStartTrackingTouch(seekBar);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mTrackingTouch = false;
            if (seekBar.getProgress() + mMin != mSeekBarValue) {
                syncValueInternal(seekBar);
            }
            if (mUserSeekBarChangeListener != null) {
                mUserSeekBarChangeListener.onStopTrackingTouch(seekBar);
            }
        }
    };

    /**
     * Listener reacting to the user pressing DPAD left/right keys if {@code
     * adjustable} attribute is set to true; it transfers the key presses to the SeekBar
     * to be handled accordingly.
     */
    private View.OnKeyListener buildSeekBarKeyListener(final SeekBar seekBar) {
        return new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    return false;
                }

                if (!mAdjustable && (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                        || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
                    // Right or left keys are pressed when in non-adjustable mode; Skip the keys.
                    return false;
                }

                // We don't want to propagate the click keys down to the seekbar view since it will
                // create the ripple effect for the thumb.
                if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                    return false;
                }

                if (seekBar == null) {
                    Log.e(TAG, "SeekBar view is null and hence cannot be adjusted.");
                    return false;
                }
                return seekBar.onKeyDown(keyCode, event);
            }
        };
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_Material_SeekBarPreference);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.seekBarPreferenceStyle);
    }

    public SeekBarPreference(Context context) {
        this(context, null);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference, defStyleAttr, defStyleRes);

        final boolean hasAspMin = a.hasValue(R.styleable.SeekBarPreference_asp_min);
        if (hasAspMin) {
            Log.w(TAG, "app:asp_min is deprecated. Use app:min instead.");

            /*
             * The ordering of these two statements are important. If we want to set max first,
             * we need to perform the same steps by changing min/max to max/min as following:
             * mMax = a.getInt(...) and setMin(...).
             */
            mMin = a.getInt(R.styleable.SeekBarPreference_asp_min, mMin);
            setMax(a.getInt(R.styleable.SeekBarPreference_android_max, mMax));
        }

        try {
            final boolean hasMin = a.hasValue(R.styleable.SeekBarPreference_min);
            if (hasAspMin && hasMin) {
                Log.w(TAG, "You've specified both app:asp_min and app:min. app:asp_min takes precedence.");
            } else {
                mMin = a.getInt(R.styleable.SeekBarPreference_min, mMin);
                setMax(mMax);
            }

            setSeekBarIncrement(a.getInt(R.styleable.SeekBarPreference_seekBarIncrement, mSeekBarIncrement));
            mAdjustable = a.getBoolean(R.styleable.SeekBarPreference_adjustable, mAdjustable);
            mShowSeekBarValue = a.getBoolean(R.styleable.SeekBarPreference_showSeekBarValue, mShowSeekBarValue);
        } catch (NoSuchFieldError e) {
            // These are only available since support libs 25.1.0.
        }

        mInfoAnchorId = a.getResourceId(R.styleable.SeekBarPreference_asp_infoAnchor, 0);
        setInfo(a.getText(R.styleable.SeekBarPreference_asp_info));

        a.recycle();
    }

    @Override
    public void onBindViewHolder(final PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        final SeekBar seekBar = (SeekBar) holder.findViewById(R.id.seekbar);

        holder.itemView.setOnKeyListener(buildSeekBarKeyListener(seekBar));

        final TextView info = (TextView) holder.findViewById(R.id.seekbar_value);
        if (info != null) {
            mInfoViews.put(info, this);
            bindInfo(info);
            bindInfoAnchor(info);
        }

        if (seekBar == null) {
            Log.e(TAG, "SeekBar view is null in onBindViewHolder.");
            return;
        }
        seekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        seekBar.setMax(mMax - mMin);
        // If the increment is not zero, use that. Otherwise, use the default mKeyProgressIncrement
        // in AbsSeekBar when it's zero. This default increment value is set by AbsSeekBar
        // after calling setMax. That's why it's important to call setKeyProgressIncrement after
        // calling setMax() since setMax() can change the increment value.
        if (mSeekBarIncrement != 0) {
            seekBar.setKeyProgressIncrement(mSeekBarIncrement);
        } else {
            mSeekBarIncrement = seekBar.getKeyProgressIncrement();
        }
        seekBar.setProgress(mSeekBarValue - mMin);
        seekBar.setEnabled(isEnabled());

        fixDrawableStateOnAndroid2(seekBar);
    }

    private static void fixDrawableStateOnAndroid2(final SeekBar seekBar) {// Fix drawable state on Android 2.
        if (Build.VERSION.SDK_INT < 14) {
            final int[] state = seekBar.getDrawableState();
            Drawable d;
            d = SeekBarCompat.getThumb(seekBar);
            if (d != null) d.setState(state);
            d = seekBar.getProgressDrawable();
            if (d != null) d.setState(state);
            d = seekBar.getIndeterminateDrawable();
            if (d != null) d.setState(state);
            d = seekBar.getBackground();
            if (d != null) d.setState(state);
        }
    }

    private void bindInfo(@NonNull TextView info) {
        if (!TextUtils.isEmpty(mInfo)) {
            info.setText(mInfo);
            info.setVisibility(View.VISIBLE);
        } else if (mShowSeekBarValue) {
            info.setText(String.valueOf(mSeekBarValue));
            info.setVisibility(View.VISIBLE);
        } else {
            info.setVisibility(View.GONE);
            info.setText(null);
        }
    }

    private void bindInfoAnchor(@NonNull TextView info) {
        try {
            final RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) info.getLayoutParams();
            if (mInfoAnchorId != 0) {
                lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0); // Remove rule.
                lp.addRule(RelativeLayout.ALIGN_BASELINE, mInfoAnchorId);
            } else {
                lp.addRule(RelativeLayout.ALIGN_BASELINE, 0); // Remove rule.
                lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            }
        } catch (ClassCastException ex) {
            // Not applicable.
        }
    }

    public CharSequence getInfo() {
        return mInfo;
    }

    /**
     * Will show {@code info} when not null.
     * Otherwise shows {@code seekBarValue} when {@link #isShowSeekBarValue()} is {@code true}.
     *
     * @param info
     */
    public void setInfo(@Nullable CharSequence info) {
        if (info != mInfo) {
            mInfo = info;
            onInfoChanged();
        }
    }

    public void onInfoChanged() {
        // DO NOT call notifyChanged()!
        for (Map.Entry<TextView, SeekBarPreference> entry : mInfoViews.entrySet()) {
            TextView tv = entry.getKey();
            SeekBarPreference pref = entry.getValue();
            if (pref == this) {
                bindInfo(tv);
            }
        }
    }

    public OnSeekBarChangeListener getOnSeekBarChangeListener() {
        return mUserSeekBarChangeListener;
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        mUserSeekBarChangeListener = listener;
        onInfoChanged();
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(mSeekBarValue)
                : (Integer) defaultValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    public void setMin(int min) {
        if (min > mMax) {
            min = mMax;
        }
        if (min != mMin) {
            mMin = min;
            notifyChanged();
        }
    }

    public int getMin() {
        return mMin;
    }

    public void setMax(int max) {
        if (max < mMin) {
            max = mMin;
        }
        if (max != mMax) {
            mMax = max;
            notifyChanged();
        }
    }

    public int getMax() {
        return mMax;
    }

    /**
     * Returns the amount of increment change via each arrow key click. This value is derived from
     * user's specified increment value if it's not zero. Otherwise, the default value is picked
     * from the default mKeyProgressIncrement value in {@link android.widget.AbsSeekBar}.
     *
     * @return The amount of increment on the SeekBar performed after each user's arrow key press.
     */
    public final int getSeekBarIncrement() {
        return mSeekBarIncrement;
    }

    /**
     * Sets the increment amount on the SeekBar for each arrow key press.
     *
     * @param seekBarIncrement The amount to increment or decrement when the user presses an
     * arrow key.
     */
    public final void setSeekBarIncrement(int seekBarIncrement) {
        if (seekBarIncrement != mSeekBarIncrement) {
            mSeekBarIncrement = Math.min(mMax - mMin, Math.abs(seekBarIncrement));
            notifyChanged();
        }
    }

    public void setAdjustable(boolean adjustable) {
        mAdjustable = adjustable;
    }

    public boolean isAdjustable() {
        return mAdjustable;
    }

    public boolean isShowSeekBarValue() {
        return mShowSeekBarValue;
    }

    public void setShowSeekBarValue(final boolean showSeekBarValue) {
        if (showSeekBarValue != mShowSeekBarValue) {
            mShowSeekBarValue = showSeekBarValue;
            notifyChanged();
        }
    }

    public void setValue(int progress) {
        setValueInternal(progress, true);
    }

    private void setValueInternal(int preferredProgress, boolean notifyChanged) {
        if (preferredProgress > mMax) {
            preferredProgress = mMax;
        }
        if (preferredProgress < mMin) {
            preferredProgress = mMin;
        }
        if (preferredProgress != mSeekBarValue) {
            mSeekBarValue = preferredProgress;
            // seekBarValueTextView is updated in notifyChanged().
            persistInt(preferredProgress);
            if (notifyChanged) {
                notifyChanged();
            }
        }
    }

    public int getValue() {
        return mSeekBarValue;
    }

    /**
     * Persist the seekBar's progress value if callChangeListener
     * returns true, otherwise set the seekBar's progress to the stored value
     */
    void syncValueInternal(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        if (progress != mSeekBarValue - mMin) {
            if (callChangeListener(progress)) {
                setValueInternal(progress + mMin, false);
            } else {
                seekBar.setProgress(mSeekBarValue - mMin);
            }
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        // Save the instance state
        final SavedState myState = new SavedState(superState);
        myState.seekBarValue = mSeekBarValue;
        myState.max = mMax;
        myState.min = mMin;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // Restore the instance state
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mMax = myState.max;
        mMin = myState.min;
        setValueInternal(myState.seekBarValue, true);
    }

    /**
     * SavedState, a subclass of {@link BaseSavedState}, will store the state
     * of MyPreference, a subclass of Preference.
     * <p/>
     * It is important to always call through to super methods.
     */
    private static class SavedState extends BaseSavedState {
        int seekBarValue;
        int max;
        int min;

        public SavedState(Parcel source) {
            super(source);

            // Restore the click counter
            seekBarValue = source.readInt();
            max = source.readInt();
            min = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            // Save the click counter
            dest.writeInt(seekBarValue);
            dest.writeInt(max);
            dest.writeInt(min);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
