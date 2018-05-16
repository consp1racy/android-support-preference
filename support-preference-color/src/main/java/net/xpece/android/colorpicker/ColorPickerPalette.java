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

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import net.xpece.android.colorpicker.ColorPickerSwatch.OnColorSelectedListener;
import net.xpece.android.support.preference.color.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

/**
 * A color picker custom view which creates an grid of color squares.  The number of squares per
 * row (and the padding between the squares) is determined by the user.
 */
public class ColorPickerPalette extends TableLayout {
    private static final String TAG = ColorPickerPalette.class.getSimpleName();

    /**
     * @hide
     */
    @IntDef({SIZE_LARGE, SIZE_SMALL})
    @RestrictTo(LIBRARY)
    @Retention(RetentionPolicy.SOURCE)
    public @interface SwatchSize {
    }

    public static final int SIZE_LARGE = 1;
    public static final int SIZE_SMALL = 2;

    public OnColorSelectedListener mOnColorSelectedListener;

    private String mDescription;
    private String mDescriptionSelected;

    private int mSwatchLength;
    private int mMarginSize;
    private int mPreferredNumColumns;

    private int mComputedNumColumns;
    private boolean mRedrawPalette;

    private int[] mColors;
    private int mSelectedColor;
    private CharSequence[] mColorContentDescriptions;

    public ColorPickerPalette(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorPickerPalette(Context context) {
        super(context);
    }

    /**
     * Initialize the size, columns, and listener.  Size should be a pre-defined size (SIZE_LARGE
     * or SIZE_SMALL) from ColorPickerDialogFragment.
     */
    public void init(@SwatchSize int size, int columns, @Nullable OnColorSelectedListener listener) {
        mPreferredNumColumns = columns;
        Resources res = getResources();
        if (size == SIZE_LARGE) {
            mSwatchLength = res.getDimensionPixelSize(R.dimen.color_swatch_large);
            mMarginSize = res.getDimensionPixelSize(R.dimen.color_swatch_margins_large);
        } else {
            mSwatchLength = res.getDimensionPixelSize(R.dimen.color_swatch_small);
            mMarginSize = res.getDimensionPixelSize(R.dimen.color_swatch_margins_small);
        }
        mOnColorSelectedListener = listener;

        mDescription = res.getString(R.string.color_swatch_description);
        mDescriptionSelected = res.getString(R.string.color_swatch_description_selected);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mPreferredNumColumns < 0) {
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            int width, height;

            if (widthMode == MeasureSpec.EXACTLY) {
                width = widthSize;
            } else if (widthMode == MeasureSpec.AT_MOST) {
                width = widthSize;
            } else {
                width = -1;
            }

            int computedNumColumns;
            if (width < 0) {
                computedNumColumns = Integer.MAX_VALUE;
            } else {
                int columnSize = mSwatchLength + 2 * mMarginSize;
                width -= getPaddingLeft() + getPaddingRight();
                computedNumColumns = width / columnSize;
            }
            if (mComputedNumColumns != computedNumColumns) {
                mComputedNumColumns = computedNumColumns;
                mRedrawPalette = true;
            }
        }

        if (mRedrawPalette) {
            mRedrawPalette = false;
            drawPalette();
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @NonNull
    private TableRow createTableRow() {
        final TableRow row = new TableRow(getContext());
        final ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(params);
        return row;
    }

    private void drawPalette() {
        drawPalette(mColors, mSelectedColor, mColorContentDescriptions);
    }

    /**
     * Adds swatches to table in a serpentine format.
     */
    public void drawPalette(@Nullable @ColorInt int[] colors, @ColorInt int selectedColor) {
        drawPalette(colors, selectedColor, null);
    }

    /**
     * Adds swatches to table in a serpentine format.
     */
    public void drawPalette(
            @Nullable @ColorInt int[] colors,
            @ColorInt int selectedColor,
            @Nullable CharSequence[] colorContentDescriptions) {
        if (colors == null) {
            return;
        }

        mColors = colors;
        mSelectedColor = selectedColor;
        mColorContentDescriptions = colorContentDescriptions;

        final int effectiveNumColumns;
        try {
            effectiveNumColumns = getEffectiveNumColumns();
        } catch (Exception ex) {
            mRedrawPalette = true;
            return;
        }

        this.removeAllViews();
        int tableElements = 0;
        int rowElements = 0;
        int rowNumber = 0;

        // Fills the table with swatches based on the array of colors.
        TableRow row = createTableRow();
        for (int color : colors) {
            View colorSwatch = createColorSwatch(color, selectedColor);
            setSwatchDescription(rowNumber, tableElements, rowElements, color == selectedColor,
                    colorSwatch, colorContentDescriptions, effectiveNumColumns);
            addSwatchToRow(row, colorSwatch, rowNumber);

            tableElements++;
            rowElements++;
            if (rowElements == effectiveNumColumns) {
                addView(row);
                row = createTableRow();
                rowElements = 0;
                rowNumber++;
            }
        }

        // Create blank views to fill the row if the last row has not been filled.
        if (rowElements > 0) {
            while (rowElements != effectiveNumColumns) {
                addSwatchToRow(row, createBlankSpace(), rowNumber);
                rowElements++;
            }
            addView(row);
        }
    }

    public int getEffectiveNumColumns() {
        if (mPreferredNumColumns > 0) {
            return mPreferredNumColumns;
        }
        if (mComputedNumColumns > 0) {
            return mComputedNumColumns;
        }
        throw new IllegalStateException("Cannot have zero columns.");
    }

    /**
     * Appends a swatch to the end of the row for even-numbered rows (starting with row 0),
     * to the beginning of a row for odd-numbered rows.
     */
    private static void addSwatchToRow(TableRow row, View swatch, int rowNumber) {
        if (rowNumber % 2 == 0) {
            row.addView(swatch);
        } else {
            row.addView(swatch, 0);
        }
    }

    /**
     * Add a content description to the specified swatch view. Because the colors get added in a
     * snaking form, every other row will need to compensate for the fact that the colors are added
     * in an opposite direction from their left->right/top->bottom order, which is how the system
     * will arrange them for accessibility purposes.
     */
    private void setSwatchDescription(
            int rowNumber, int index, int rowElements, boolean selected,
            View swatch, @Nullable CharSequence[] contentDescriptions, int numColumns) {
        CharSequence description;
        if (contentDescriptions != null && contentDescriptions.length > index) {
            description = contentDescriptions[index];
        } else {
            int accessibilityIndex;
            if (rowNumber % 2 == 0) {
                // We're in a regular-ordered row
                accessibilityIndex = index + 1;
            } else {
                // We're in a backwards-ordered row.
                int rowMax = ((rowNumber + 1) * numColumns);
                accessibilityIndex = rowMax - rowElements;
            }

            if (selected) {
                description = String.format(mDescriptionSelected, accessibilityIndex);
            } else {
                description = String.format(mDescription, accessibilityIndex);
            }
        }
        swatch.setContentDescription(description);
    }

    /**
     * Creates a blank space to fill the row.
     */
    @NonNull
    private ImageView createBlankSpace() {
        final ImageView view = new ImageView(getContext());
        final TableRow.LayoutParams params = new TableRow.LayoutParams(mSwatchLength, mSwatchLength);
        params.setMargins(mMarginSize, mMarginSize, mMarginSize, mMarginSize);
        view.setLayoutParams(params);
        return view;
    }

    /**
     * Creates a color swatch.
     */
    @NonNull
    private ColorPickerSwatch createColorSwatch(@ColorInt int color, @ColorInt int selectedColor) {
        ColorPickerSwatch view = new ColorPickerSwatch(getContext(), color,
                color == selectedColor, mOnColorSelectedListener);
        TableRow.LayoutParams params = new TableRow.LayoutParams(mSwatchLength, mSwatchLength);
        params.setMargins(mMarginSize, mMarginSize, mMarginSize, mMarginSize);
        view.setLayoutParams(params);
        return view;
    }
}
