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
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;

public class SeekBarDialogPreference extends DialogPreference {
    static final String TAG = SeekBarDialogPreference.class.getSimpleName();

    private int mProgress;
    private int mPreferredMax = 100;
    private int mPreferredMin = 0;

    public SeekBarDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public SeekBarDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_Material_DialogPreference_SeekBarDialogPreference);
    }

    public SeekBarDialogPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.seekBarDialogPreferenceStyle);
    }

    public SeekBarDialogPreference(Context context) {
        this(context, null);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference, defStyleAttr, defStyleRes);

        final boolean hasAspMin = a.hasValue(R.styleable.SeekBarPreference_asp_min);
        if (hasAspMin) {
            Log.w(TAG, "app:asp_min is deprecated. Use app:min instead.");

            setMin(a.getInt(R.styleable.SeekBarPreference_asp_min, mPreferredMin));
        }

        final boolean hasMin = a.hasValue(R.styleable.SeekBarPreference_min);
        if (hasMin && hasAspMin) {
            Log.w(TAG, "You've specified both app:asp_min and app:min. app:asp_min takes precedence.");
        } else {
            setMin(a.getInt(R.styleable.SeekBarPreference_min, mPreferredMin));
        }

        setMax(a.getInt(R.styleable.SeekBarPreference_android_max, mPreferredMax));

        a.recycle();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setProgress(restoreValue ? getPersistedInt(mProgress) : (int) defaultValue);
    }

    @Override
    public boolean shouldDisableDependents() {
        return mProgress == 0 || super.shouldDisableDependents();
    }

    public void setProgress(int progress) {
        setProgress(progress, true);
    }

    public void setProgress(int preferredProgress, boolean notifyChanged) {
        final boolean wasBlocking = shouldDisableDependents();

        if (preferredProgress > mPreferredMax) {
            preferredProgress = mPreferredMax;
        }
        if (preferredProgress < mPreferredMin) {
            preferredProgress = mPreferredMin;
        }
        if (preferredProgress != mProgress) {
            mProgress = preferredProgress;
            persistInt(preferredProgress);
//            Log.d("SBDP", "preferredProgress=" + preferredProgress);
            if (notifyChanged) {
                notifyChanged();
            }
        }

        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
    }

    public int getProgress() {
        return mProgress;
    }

    public void setMax(int max) {
        if (max != mPreferredMax) {
            mPreferredMax = max;
            notifyChanged();
        }
    }

    public void setMin(int min) {
        if (min != mPreferredMin) {
            mPreferredMin = min;
            notifyChanged();
        }
    }

    public int getMax() {
        return mPreferredMax;
    }

    public int getMin() {
        return mPreferredMin;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.progress = mProgress;
        myState.max = mPreferredMax;
        myState.min = mPreferredMin;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mPreferredMax = myState.max;
        mPreferredMin = myState.min;
        setProgress(myState.progress, true);
    }

    private static class SavedState extends BaseSavedState {
        int progress;
        int max;
        int min;

        public SavedState(Parcel source) {
            super(source);
            progress = source.readInt();
            max = source.readInt();
            min = source.readInt();
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(progress);
            dest.writeInt(max);
            dest.writeInt(min);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

}
