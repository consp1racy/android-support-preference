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

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.android.colorpicker.ColorPickerPalette;
import com.android.colorpicker.ColorPickerSwatch.OnColorSelectedListener;
import com.android.colorpicker.R;

/**
 * A dialog which takes in as input an array of colors and creates a palette allowing the user to
 * select a specific color swatch, which invokes a listener.
 */
public class XpColorPreferenceDialogFragment extends PreferenceDialogFragmentCompat
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

    public static XpColorPreferenceDialogFragment newInstance(String key) {
        XpColorPreferenceDialogFragment fragment = new XpColorPreferenceDialogFragment();
        Bundle b = new Bundle(1);
        b.putString("key", key);
        fragment.setArguments(b);
        return fragment;
    }

    public XpColorPreferenceDialogFragment() {
        // Empty constructor required for dialog fragments.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
//            mColors = savedInstanceState.getIntArray(KEY_COLORS);
            mSelectedColor = savedInstanceState.getInt(KEY_SELECTED_COLOR);
//            mColorContentDescriptions = savedInstanceState.getTextArray(KEY_COLOR_CONTENT_DESCRIPTIONS);
        } else {
            ColorPreference pref = getColorPreference();
            mSelectedColor = pref.getColor();
        }
    }

    @Override
    protected View onCreateDialogView(final Context context) {
        final ColorPreference colorPreference = getColorPreference();

        View view = LayoutInflater.from(context).inflate(R.layout.color_picker_dialog, null);
        mProgress = (ProgressBar) view.findViewById(android.R.id.progress);
        mPalette = (ColorPickerPalette) view.findViewById(R.id.color_picker);

        int size = colorPreference.getSwatchSize();
        int columns = colorPreference.getColumnCount();
        mPalette.init(size, columns, this);

        int[] colors = colorPreference.getColorValues();
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

    private void save() {ColorPreference preference = (ColorPreference) getPreference();
        int color = getSelectedColor();
        if (preference.callChangeListener(color)) {
            preference.setColor(color);
        }
    }

    public ColorPreference getColorPreference()  {
        return (ColorPreference) getPreference();
    }

    @Override
    protected void onBindDialogView(final View view) {
        super.onBindDialogView(view);
    }

    @Override
    public void onColorSelected(int color) {
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
            mPalette.drawPalette(getColorPreference().getColorValues(), mSelectedColor);
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
        ColorPreference pref = getColorPreference();
        int[] colors = pref.getColorValues();
        CharSequence[] names = pref.getColorNames();
        if (mPalette != null && colors != null) {
            mPalette.drawPalette(colors, mSelectedColor, names);
        }
    }

    public int getSelectedColor() {
        return mSelectedColor;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putIntArray(KEY_COLORS, mColors);
        outState.putInt(KEY_SELECTED_COLOR, mSelectedColor);
//        outState.putTextArray(KEY_COLOR_CONTENT_DESCRIPTIONS, mColorContentDescriptions);
    }
}
