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
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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

    public static final int SIZE_LARGE = 1;
    public static final int SIZE_SMALL = 2;

    protected static final String KEY_COLORS = "colors";
    protected static final String KEY_COLOR_CONTENT_DESCRIPTIONS = "color_content_descriptions";
    protected static final String KEY_SELECTED_COLOR = "selected_color";
    protected static final String KEY_COLUMNS = "columns";
    protected static final String KEY_SIZE = "size";

    protected int[] mColors = null;
    protected String[] mColorContentDescriptions = null;
    protected int mSelectedColor;
    protected int mColumns;
    protected int mSize;

    private ColorPickerPalette mPalette;
    private ProgressBar mProgress;

    protected OnColorSelectedListener mListener;

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

    public void initialize(int[] colors, int selectedColor, int columns, int size) {
        setArguments(columns, size);
        setColors(colors, selectedColor);
    }

    public void setArguments(int columns, int size) {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_COLUMNS, columns);
        bundle.putInt(KEY_SIZE, size);
        setArguments(bundle);
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumns = getArguments().getInt(KEY_COLUMNS);
            mSize = getArguments().getInt(KEY_SIZE);
        }

        if (savedInstanceState != null) {
            mColors = savedInstanceState.getIntArray(KEY_COLORS);
            mSelectedColor = (Integer) savedInstanceState.getSerializable(KEY_SELECTED_COLOR);
            mColorContentDescriptions = savedInstanceState.getStringArray(
                KEY_COLOR_CONTENT_DESCRIPTIONS);
        }
    }

    @Override
    protected void onPrepareDialogBuilder(final AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        final Context context = builder.getContext();

        View view = LayoutInflater.from(context).inflate(R.layout.color_picker_dialog, null);
        mProgress = (ProgressBar) view.findViewById(android.R.id.progress);
        mPalette = (ColorPickerPalette) view.findViewById(R.id.color_picker);
        mPalette.init(mSize, mColumns, this);

        if (mColors != null) {
            showPaletteView();
        }

        builder.setView(view);
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

//    @Override
//    public void onClick(final DialogInterface dialog, final int which) {
//        super.onClick(dialog, which);
//        if (which == DialogInterface.BUTTON_POSITIVE) {
//            save();
//        }
//    }

    @Override
    public void onColorSelected(int color) {
        if (mListener != null) {
            mListener.onColorSelected(color);
        }

        if (getTargetFragment() instanceof OnColorSelectedListener) {
            final OnColorSelectedListener listener =
                (OnColorSelectedListener) getTargetFragment();
            listener.onColorSelected(color);
        }

        if (color != mSelectedColor) {
            mSelectedColor = color;
            // Redraw palette to show checkmark on newly selected color before dismissing.
            mPalette.drawPalette(mColors, mSelectedColor);
        }

        if (getPreference().getPositiveButtonText() == null) {
            dismiss();
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

    public void setColors(int[] colors, int selectedColor) {
        if (mColors != colors || mSelectedColor != selectedColor) {
            mColors = colors;
            mSelectedColor = selectedColor;
            refreshPalette();
        }
    }

    public void setColors(int[] colors) {
        if (mColors != colors) {
            mColors = colors;
            refreshPalette();
        }
    }

    public void setSelectedColor(int color) {
        if (mSelectedColor != color) {
            mSelectedColor = color;
            refreshPalette();
        }
    }

    public void setColorContentDescriptions(String[] colorContentDescriptions) {
        if (mColorContentDescriptions != colorContentDescriptions) {
            mColorContentDescriptions = colorContentDescriptions;
            refreshPalette();
        }
    }

    private void refreshPalette() {
        if (mPalette != null && mColors != null) {
            mPalette.drawPalette(mColors, mSelectedColor, mColorContentDescriptions);
        }
    }

    public int[] getColors() {
        return mColors;
    }

    public int getSelectedColor() {
        return mSelectedColor;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray(KEY_COLORS, mColors);
        outState.putSerializable(KEY_SELECTED_COLOR, mSelectedColor);
        outState.putStringArray(KEY_COLOR_CONTENT_DESCRIPTIONS, mColorContentDescriptions);
    }
}
