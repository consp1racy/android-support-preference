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

package net.xpece.android.colorpicker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import net.xpece.android.colorpicker.ColorPickerSwatch.OnColorSelectedListener;
import net.xpece.android.support.preference.color.R;

/**
 * A dialog which takes in as input an array of colors and creates a palette allowing the user to
 * select a specific color swatch, which invokes a listener.
 */
@Deprecated
public class ColorPickerDialog extends DialogFragment implements OnColorSelectedListener {

    protected AlertDialog mAlertDialog;

    protected static final String KEY_TITLE_ID = "title_id";
    protected static final String KEY_COLORS = "colors";
    protected static final String KEY_COLOR_CONTENT_DESCRIPTIONS = "color_content_descriptions";
    protected static final String KEY_SELECTED_COLOR = "selected_color";
    protected static final String KEY_COLUMNS = "columns";
    protected static final String KEY_SIZE = "size";

    protected int mTitleResId = R.string.color_picker_default_title;
    protected int[] mColors = null;
    protected String[] mColorContentDescriptions = null;
    protected int mSelectedColor;
    protected int mColumns;
    @ColorPickerPalette.SwatchSize protected int mSize;

    private ColorPickerPalette mPalette;
    private ProgressBar mProgress;

    protected OnColorSelectedListener mListener;

    public ColorPickerDialog() {
        // Empty constructor required for dialog fragments.
    }

    @NonNull
    @SuppressWarnings("deprecation")
    public static ColorPickerDialog newInstance(@StringRes int titleResId, @NonNull @ColorInt int[] colors, @ColorInt int selectedColor,
                                                int columns, @ColorPickerPalette.SwatchSize int size) {
        ColorPickerDialog ret = new ColorPickerDialog();
        ret.initialize(titleResId, colors, selectedColor, columns, size);
        return ret;
    }

    public void initialize(int titleResId, @NonNull @ColorInt int[] colors, @ColorInt int selectedColor, int columns, @ColorPickerPalette.SwatchSize int size) {
        setArguments(titleResId, columns, size);
        setColors(colors, selectedColor);
    }

    public void setArguments(int titleResId, int columns, @ColorPickerPalette.SwatchSize int size) {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_TITLE_ID, titleResId);
        bundle.putInt(KEY_COLUMNS, columns);
        bundle.putInt(KEY_SIZE, size);
        setArguments(bundle);
    }

    public void setOnColorSelectedListener(@Nullable OnColorSelectedListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mTitleResId = getArguments().getInt(KEY_TITLE_ID);
            mColumns = getArguments().getInt(KEY_COLUMNS);
            //noinspection WrongConstant
            mSize = getArguments().getInt(KEY_SIZE);
        }

        if (savedInstanceState != null) {
            mColors = savedInstanceState.getIntArray(KEY_COLORS);
            mSelectedColor = savedInstanceState.getInt(KEY_SELECTED_COLOR);
            mColorContentDescriptions = savedInstanceState.getStringArray(
                KEY_COLOR_CONTENT_DESCRIPTIONS);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Activity activity = getActivity();

        @SuppressLint("InflateParams")
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.color_picker_dialog, null);
        mProgress = view.findViewById(android.R.id.progress);
        mPalette = view.findViewById(R.id.color_picker);
        mPalette.init(mSize, mColumns, this);

        if (mColors != null) {
            showPaletteView();
        }

        mAlertDialog = new AlertDialog.Builder(activity)
            .setTitle(mTitleResId)
            .setView(view)
            .create();

        return mAlertDialog;
    }

    @Override
    public void onColorSelected(@ColorInt int color) {
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

        dismiss();
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

    public void setColors(@NonNull @ColorInt int[] colors, @ColorInt int selectedColor) {
        if (mColors != colors || mSelectedColor != selectedColor) {
            mColors = colors;
            mSelectedColor = selectedColor;
            refreshPalette();
        }
    }

    public void setColors(@NonNull @ColorInt int[] colors) {
        if (mColors != colors) {
            mColors = colors;
            refreshPalette();
        }
    }

    public void setSelectedColor(@ColorInt int color) {
        if (mSelectedColor != color) {
            mSelectedColor = color;
            refreshPalette();
        }
    }

    public void setColorContentDescriptions(@NonNull String[] colorContentDescriptions) {
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

    @ColorInt @Nullable
    public int[] getColors() {
        return mColors;
    }

    @ColorInt
    public int getSelectedColor() {
        return mSelectedColor;
    }

    @Override
    public void onSaveInstanceState(final @NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray(KEY_COLORS, mColors);
        outState.putInt(KEY_SELECTED_COLOR, mSelectedColor);
        outState.putStringArray(KEY_COLOR_CONTENT_DESCRIPTIONS, mColorContentDescriptions);
    }
}
