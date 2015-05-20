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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;

/**
 * A {@link Preference} that provides checkbox widget
 * functionality.
 * <p></p>
 * This preference will store a boolean into the SharedPreferences.
 */
public class CheckBoxPreference extends TwoStatePreference {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public CheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, R.style.Preference_Material_CheckBoxPreference);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CheckBoxPreference, defStyleAttr, defStyleRes);
        setSummaryOn(a.getString(R.styleable.CheckBoxPreference_android_summaryOn));
        setSummaryOff(a.getString(R.styleable.CheckBoxPreference_android_summaryOff));
        setDisableDependentsState(a.getBoolean(R.styleable.CheckBoxPreference_android_disableDependentsState, false));
        a.recycle();
    }

    public CheckBoxPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.checkBoxPreferenceStyle);
    }

    public CheckBoxPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        View checkboxView = view.findViewById(android.R.id.checkbox);
        if (checkboxView != null && checkboxView instanceof Checkable) {
            ((Checkable) checkboxView).setChecked(mChecked);
        }

        syncSummaryView(view);
    }
}
