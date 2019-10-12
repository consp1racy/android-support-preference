/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import net.xpece.android.colorpicker.ColorPickerPalette;
import net.xpece.android.colorpicker.ColorPickerSwatch.OnColorSelectedListener;
import net.xpece.android.support.preference.color.R;

import static net.xpece.android.support.preference.Util.checkPreferenceNotNull;

/**
 * A dialog which takes in as input an array of colors and creates a palette allowing the user to
 * select a specific color swatch, which invokes a listener.
 */
public class XpColorPreferenceDialogFragment extends XpPreferenceDialogFragment
        implements OnColorSelectedListener {

    //    protected static final String KEY_COLORS = "colors";
//    protected static final String KEY_COLOR_CONTENT_DESCRIPTIONS = "color_content_descriptions";
    protected static final String KEY_SELECTED_COLOR = "selected_color";

    //    protected int[] mColors = null;
//    protected String[] mColorContentDescriptions = null;
    protected int mSelectedColor;
//    protected int mColumns;
//    protected int mSize;

    private ColorPickerPalette mPalette;
    private ProgressBar mProgress;

    public static boolean onPreferenceDisplayDialog(
            final @NonNull PreferenceFragmentCompat preferenceFragment,
            final @NonNull androidx.preference.Preference preference) {
        if (preference instanceof ColorPreference) {
            final String key = preference.getKey();
            final DialogFragment f = XpColorPreferenceDialogFragment.newInstance(key);
            f.setTargetFragment(preferenceFragment, 0);
            final FragmentManager fm = preferenceFragment.getFragmentManager();
            assert fm != null;
            f.show(fm, key);
            return true;
        }
        return false;
    }

    @NonNull
    public static XpColorPreferenceDialogFragment newInstance(final String key) {
        XpColorPreferenceDialogFragment fragment = new XpColorPreferenceDialogFragment();
        Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    public XpColorPreferenceDialogFragment() {
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
//            mColors = savedInstanceState.getIntArray(KEY_COLORS);
            mSelectedColor = savedInstanceState.getInt(KEY_SELECTED_COLOR);
//            mColorContentDescriptions = savedInstanceState.getTextArray(KEY_COLOR_CONTENT_DESCRIPTIONS);
        } else {
            ColorPreference pref = requireColorPreference();
            mSelectedColor = pref.getColor();
        }
    }

    @NonNull
    @Override
    protected View onCreateDialogView(final @NonNull Context context) {
        final ColorPreference colorPreference = requireColorPreference();

        @SuppressLint("InflateParams") final View view = LayoutInflater.from(context).inflate(R.layout.color_picker_dialog, null);
        mProgress = view.findViewById(android.R.id.progress);
        mPalette = view.findViewById(R.id.color_picker);

        final int size = colorPreference.getSwatchSize();
        final int columns = colorPreference.getColumnCount();
        mPalette.init(size, columns, this);

        final int[] colors = colorPreference.getColorValues();
        if (colors != null) {
            showPaletteView();
        }

        return view;
    }

    @Override
    public void onDialogClosed(final boolean b) {
        if (b) {
            save();
        }
    }

    private void save() {
        ColorPreference preference = (ColorPreference) getPreference();
        int color = getSelectedColor();
        if (preference.callChangeListener(color)) {
            preference.setColor(color);
        }
    }

    @Nullable
    public ColorPreference getColorPreference() {
        return (ColorPreference) getPreference();
    }

    @NonNull
    protected ColorPreference requireColorPreference() {
        return checkPreferenceNotNull(getColorPreference(), ColorPreference.class, this);
    }

    @Override
    protected void onBindDialogView(final @NonNull View view) {
        super.onBindDialogView(view);
    }

    @Override
    public void onColorSelected(@ColorInt int color) {
        if (getTargetFragment() instanceof OnColorSelectedListener) {
            final OnColorSelectedListener listener =
                    (OnColorSelectedListener) getTargetFragment();
            listener.onColorSelected(color);
        }

        boolean redraw = false;

        if (color != mSelectedColor) {
            mSelectedColor = color;
            redraw = true;
        }

        if (getPreference().getPositiveButtonText() == null) {
            onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
            dismiss();
        } else if (redraw) {
            // Redraw palette to show checkmark on newly selected color before dismissing.
            mPalette.drawPalette(requireColorPreference().getColorValues(), mSelectedColor);
        }
    }

    public void showPaletteView() {
        if (mProgress != null && mPalette != null) {
            mProgress.setVisibility(View.GONE);
            refreshPalette();
            mPalette.setVisibility(View.VISIBLE);
        }
    }

    public void showProgressBarView() {
        if (mProgress != null && mPalette != null) {
            mProgress.setVisibility(View.VISIBLE);
            mPalette.setVisibility(View.GONE);
        }
    }

    private void refreshPalette() {
        ColorPreference pref = requireColorPreference();
        int[] colors = pref.getColorValues();
        CharSequence[] names = pref.getColorNames();
        if (mPalette != null && colors != null) {
            mPalette.drawPalette(colors, mSelectedColor, names);
        }
    }

    @ColorInt
    public int getSelectedColor() {
        return mSelectedColor;
    }

    @Override
    public void onSaveInstanceState(final @NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putIntArray(KEY_COLORS, mColors);
        outState.putInt(KEY_SELECTED_COLOR, mSelectedColor);
//        outState.putTextArray(KEY_COLOR_CONTENT_DESCRIPTIONS, mColorContentDescriptions);
    }
}
