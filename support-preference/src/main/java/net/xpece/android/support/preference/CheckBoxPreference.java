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
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.CompoundButton;

/**
 * A {@link Preference} that provides checkbox widget
 * functionality.
 * <p></p>
 * This preference will store a boolean into the SharedPreferences.
 */
public class CheckBoxPreference extends TwoStatePreference {
    private final Listener mListener = new Listener();

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CheckBoxPreference(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public CheckBoxPreference(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_Asp_Material_CheckBoxPreference);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CheckBoxPreference, defStyleAttr, defStyleRes);
        setSummaryOn(a.getString(R.styleable.CheckBoxPreference_android_summaryOn));
        setSummaryOff(a.getString(R.styleable.CheckBoxPreference_android_summaryOff));
        setDisableDependentsState(a.getBoolean(R.styleable.CheckBoxPreference_android_disableDependentsState, false));
        a.recycle();
    }

    public CheckBoxPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.checkBoxPreferenceStyle);
    }

    public CheckBoxPreference(@NonNull Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(@NonNull final PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        syncCheckboxView(holder);
        syncSummaryView(holder);
    }

//    /**
//     * @hide
//     */
//    @Override
//    protected void performClick(View view) {
//        super.performClick(view);
//        syncViewIfAccessibilityEnabled(view);
//    }
//
//    private void syncViewIfAccessibilityEnabled(View view) {
//        AccessibilityManager accessibilityManager = (AccessibilityManager)
//            getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
//        if (!accessibilityManager.isEnabled()) {
//            return;
//        }
//        View checkboxView = view.findViewById(R.id.checkbox);
//        syncCheckboxView(checkboxView);
//        View summaryView = view.findViewById(android.R.id.summary);
//        syncSummaryView(summaryView);
//    }

    private void syncCheckboxView(final @NonNull PreferenceViewHolder holder) {
        View checkboxView = holder.findViewById(android.R.id.checkbox);
        if (checkboxView == null) {
            checkboxView = holder.findViewById(R.id.checkbox);
        }
        syncCheckboxView(checkboxView);
    }

    private void syncCheckboxView(@NonNull View view) {
        if (view instanceof CompoundButton) {
            ((CompoundButton) view).setOnCheckedChangeListener(null);
        }
        if (view instanceof Checkable) {
            ((Checkable) view).setChecked(mChecked);
        }
        if (view instanceof CompoundButton) {
            ((CompoundButton) view).setOnCheckedChangeListener(mListener);
        }
    }

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        Listener() {
        }

        @Override
        public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
            if (!callChangeListener(isChecked)) {
                // Listener didn't like it, change it back.
                // CompoundButton will make sure we don't recurse.
                buttonView.setChecked(!isChecked);
                return;
            }
            CheckBoxPreference.this.setChecked(isChecked);
        }
    }
}
