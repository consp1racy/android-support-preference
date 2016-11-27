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
import android.support.annotation.LayoutRes;
import android.support.v7.preference.Preference;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * A base class for {@link Preference} objects that are
 * dialog-based. These preferences will, when clicked, open a dialog showing the
 * actual preference controls.
 */
public class EditTextPreference extends android.support.v7.preference.EditTextPreference {

    private String mText;

    @LayoutRes private int mEditTextLayout;

    private OnEditTextCreatedListener mOnEditTextCreatedListener;

    public EditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public EditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public EditTextPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextPreferenceStyle);
    }

    public EditTextPreference(Context context) {
        this(context, null);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EditTextPreference, defStyleAttr, defStyleRes);
        mEditTextLayout = a.getResourceId(R.styleable.EditTextPreference_asp_editTextLayout, 0);
        a.recycle();
    }

    public OnEditTextCreatedListener getOnEditTextCreatedListener() {
        return mOnEditTextCreatedListener;
    }

    public void setOnEditTextCreatedListener(OnEditTextCreatedListener onEditTextCreatedListener) {
        mOnEditTextCreatedListener = onEditTextCreatedListener;
    }

    /**
     * Creates a new edit text widget based on supplied context. If {@link OnEditTextCreatedListener}
     * is set it will be invoked.
     *
     * @param context
     * @return
     */
    public EditText createEditText(Context context) {
        EditText editText;

        if (mEditTextLayout == 0) {
            editText = new AppCompatEditText(context);
        } else {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(mEditTextLayout, null, false);
            if (view instanceof EditText) {
                editText = (EditText) view;
            } else {
                try {
                    editText = (EditText) view.findViewById(android.R.id.edit);
                    editText.getClass();
                } catch (Exception ex) {
                    throw new IllegalArgumentException("EditTextPreference asp_editTextLayout has no EditText with ID android.R.id.edit.");
                }
            }
        }

        if (mOnEditTextCreatedListener != null) {
            mOnEditTextCreatedListener.onEditTextCreated(editText);
        }

        // Give it an ID so it can be saved/restored
        editText.setId(android.R.id.edit);

        return editText;
    }

    public interface OnEditTextCreatedListener {
        void onEditTextCreated(EditText edit);
    }
}
