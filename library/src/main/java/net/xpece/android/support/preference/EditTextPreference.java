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
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * A base class for {@link Preference} objects that are
 * dialog-based. These preferences will, when clicked, open a dialog showing the
 * actual preference controls.
 */
public class EditTextPreference extends DialogPreference {

    private String mText;

    private AttributeSet mAttrs;

    public EditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public EditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_Material_DialogPreference_EditTextPreference);
    }

    public EditTextPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextPreferenceStyle);
    }

    public EditTextPreference(Context context) {
        this(context, null);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        createEditText(context, attrs);
        mAttrs = attrs;
    }

    /**
     * Will use supplied context and stored attribute set to create a new edit text widget.
     * @param context
     * @return
     */
    public EditText createEditText(Context context) {
        EditText editText = new AppCompatEditText(context, mAttrs);

        // Give it an ID so it can be saved/restored
        editText.setId(android.R.id.edit);

        /*
         * The preference framework and view framework both have an 'enabled'
         * attribute. Most likely, the 'enabled' specified in this XML is for
         * the preference framework, but it was also given to the view framework.
         * We reset the enabled state.
         */
        editText.setEnabled(true);

        return editText;
    }

    public void setText(String text) {
        boolean wasBlocking = this.shouldDisableDependents();
        this.mText = text;
        this.persistString(text);
        boolean isBlocking = this.shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            this.notifyDependencyChange(isBlocking);
        }

    }

    public String getText() {
        return this.mText;
    }

    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        this.setText(restoreValue ? this.getPersistedString(this.mText) : (String) defaultValue);
    }

    public boolean shouldDisableDependents() {
        return TextUtils.isEmpty(this.mText) || super.shouldDisableDependents();
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (this.isPersistent()) {
            return superState;
        } else {
            EditTextPreference.SavedState myState = new EditTextPreference.SavedState(superState);
            myState.text = this.getText();
            return myState;
        }
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state != null && state.getClass().equals(EditTextPreference.SavedState.class)) {
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
            public EditTextPreference.SavedState createFromParcel(Parcel in) {
                return new EditTextPreference.SavedState(in);
            }

            public EditTextPreference.SavedState[] newArray(int size) {
                return new EditTextPreference.SavedState[size];
            }
        };

        public SavedState(Parcel source) {
            super(source);
            this.text = source.readString();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(this.text);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }
}
