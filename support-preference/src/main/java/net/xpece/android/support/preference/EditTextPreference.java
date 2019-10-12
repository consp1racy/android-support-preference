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
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.AttrRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.widget.AppCompatEditText;

/**
 * A base class for {@link Preference} objects that are
 * dialog-based. These preferences will, when clicked, open a dialog showing the
 * actual preference controls.
 */
public class EditTextPreference extends DialogPreference {

    private String mText;

    @LayoutRes
    private int mEditTextLayout;

    @Nullable
    private androidx.preference.EditTextPreference.OnBindEditTextListener mOnBindEditTextListener;

    public EditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public EditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_Asp_Material_DialogPreference_EditTextPreference);
    }

    public EditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.editTextPreferenceStyle);
    }

    public EditTextPreference(@NonNull Context context) {
        this(context, null);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        initXpece(context, attrs, defStyleAttr, defStyleRes);
        initAndroidX(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initXpece(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EditTextPreference, defStyleAttr, defStyleRes);
        try {
            mEditTextLayout = a.getResourceId(R.styleable.EditTextPreference_asp_editTextLayout, 0);
        } finally {
            a.recycle();
        }
    }

    private void initAndroidX(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        final TypedArray a = context.obtainStyledAttributes(attrs, androidx.preference.R.styleable.EditTextPreference, defStyleAttr, defStyleRes);
        try {
            if (a.getBoolean(androidx.preference.R.styleable.EditTextPreference_useSimpleSummaryProvider, false)) {
                setSummaryProvider(SimpleSummaryProvider.getInstance());
            }
        } finally {
            a.recycle();
        }
    }

    /**
     * Set a listener that will be invoked when the corresponding dialog
     * view for this preference is bound. Set {@code null} to remove the existing
     * OnBindEditTextListener.
     *
     * @param onEditTextCreatedListener The listener that will be invoked when
     *                                  the corresponding dialog view for this preference is bound
     * @see androidx.preference.EditTextPreference.OnBindEditTextListener
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public void setOnEditTextCreatedListener(@Nullable final OnEditTextCreatedListener onEditTextCreatedListener) {
        if (onEditTextCreatedListener == null) {
            mOnBindEditTextListener = null;
        } else {
            mOnBindEditTextListener = new androidx.preference.EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    onEditTextCreatedListener.onEditTextCreated(editText);
                }
            };
        }
    }

    /**
     * Set an {@link androidx.preference.EditTextPreference.OnBindEditTextListener} that will be invoked when the corresponding dialog
     * view for this preference is bound. Set {@code null} to remove the existing
     * OnBindEditTextListener.
     *
     * @param onBindEditTextListener The {@link androidx.preference.EditTextPreference.OnBindEditTextListener} that will be invoked when
     *                               the corresponding dialog view for this preference is bound
     * @see androidx.preference.EditTextPreference.OnBindEditTextListener
     */
    public void setOnBindEditTextListener(@Nullable androidx.preference.EditTextPreference.OnBindEditTextListener onBindEditTextListener) {
        mOnBindEditTextListener = onBindEditTextListener;
    }

    /**
     * Creates a new edit text widget based on supplied context. If {@link OnEditTextCreatedListener}
     * is set it will be invoked.
     */
    @SuppressWarnings("deprecation")
    @NonNull
    EditText createEditText(@NonNull Context context) {
        final EditText editText;

        if (mEditTextLayout == 0) {
            editText = new AppCompatEditText(context);
        } else {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(mEditTextLayout, null, false);
            if (view instanceof EditText) {
                editText = (EditText) view;
            } else {
                try {
                    editText = view.findViewById(android.R.id.edit);
                    editText.getClass();
                } catch (Exception ex) {
                    throw new IllegalArgumentException("EditTextPreference asp_editTextLayout has no EditText with ID android.R.id.edit.");
                }
            }
        }

        if (Build.VERSION.SDK_INT >= 17) {
            editText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        }

        if (mOnBindEditTextListener != null) {
            mOnBindEditTextListener.onBindEditText(editText);
        }

        // Give it an ID so it can be saved/restored
        editText.setId(android.R.id.edit);

        return editText;
    }

    public void setText(@Nullable String text) {
        boolean wasBlocking = shouldDisableDependents();

        mText = text;

        persistString(text);

        boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }

        notifyChanged();
    }

    @Nullable
    public String getText() {
        return this.mText;
    }

    @Nullable
    @Override
    protected String onGetDefaultValue(@NonNull TypedArray a, int index) {
        return a.getString(index);
    }

    protected void onSetInitialValue(boolean restoreValue, @Nullable Object defaultValue) {
        this.setText(restoreValue ? this.getPersistedString(this.mText) : (String) defaultValue);
    }

    public boolean shouldDisableDependents() {
        return TextUtils.isEmpty(this.mText) || super.shouldDisableDependents();
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        assert superState != null;
        if (this.isPersistent()) {
            return superState;
        } else {
            EditTextPreference.SavedState myState = new EditTextPreference.SavedState(superState);
            myState.text = this.getText();
            return myState;
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Parcelable state) {
        if (state.getClass().equals(EditTextPreference.SavedState.class)) {
            EditTextPreference.SavedState myState = (EditTextPreference.SavedState) state;
            super.onRestoreInstanceState(myState.getSuperState());
            this.setText(myState.text);
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    private static class SavedState extends BaseSavedState {
        String text;
        public static final Creator<EditTextPreference.SavedState> CREATOR = new Creator<EditTextPreference.SavedState>() {
            @NonNull
            public EditTextPreference.SavedState createFromParcel(@NonNull Parcel in) {
                return new EditTextPreference.SavedState(in);
            }

            @NonNull
            public EditTextPreference.SavedState[] newArray(int size) {
                return new EditTextPreference.SavedState[size];
            }
        };

        public SavedState(@NonNull Parcel source) {
            super(source);
            this.text = source.readString();
        }

        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(this.text);
        }

        public SavedState(@NonNull Parcelable superState) {
            super(superState);
        }
    }

    /**
     * @deprecated Use {@link androidx.preference.EditTextPreference.OnBindEditTextListener OnBindEditTextListener}.
     */
    @Deprecated
    public interface OnEditTextCreatedListener {
        void onEditTextCreated(@NonNull EditText edit);
    }

    /**
     * A simple {@link androidx.preference.Preference.SummaryProvider} implementation for an
     * {@link EditTextPreference}. If no value has been set, the summary displayed will be 'Not
     * set', otherwise the summary displayed will be the value set for this preference.
     */
    public static final class SimpleSummaryProvider implements SummaryProvider<EditTextPreference> {

        private static SimpleSummaryProvider sSimpleSummaryProvider;

        private SimpleSummaryProvider() {}

        /**
         * Retrieve a singleton instance of this simple
         * {@link androidx.preference.Preference.SummaryProvider} implementation.
         *
         * @return a singleton instance of this simple
         * {@link androidx.preference.Preference.SummaryProvider} implementation
         */
        public static SimpleSummaryProvider getInstance() {
            if (sSimpleSummaryProvider == null) {
                sSimpleSummaryProvider = new SimpleSummaryProvider();
            }
            return sSimpleSummaryProvider;
        }

        @Override
        public CharSequence provideSummary(EditTextPreference preference) {
            if (TextUtils.isEmpty(preference.getText())) {
                return (preference.getContext().getString(R.string.not_set));
            } else {
                return preference.getText();
            }
        }
    }
}
