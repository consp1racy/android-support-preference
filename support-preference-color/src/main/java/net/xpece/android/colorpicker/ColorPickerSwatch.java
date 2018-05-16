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
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import net.xpece.android.support.preference.color.R;

/**
 * Creates a circular swatch of a specified color.  Adds a checkmark if marked as checked.
 */
@SuppressLint("ViewConstructor")
public class ColorPickerSwatch extends FrameLayout implements View.OnClickListener {
    private int mColor;
    private ImageView mSwatchImage;
    private ImageView mCheckmarkImage;
    private OnColorSelectedListener mOnColorSelectedListener;

    private int mPressedColor;

    /**
     * Interface for a callback when a color square is selected.
     */
    public interface OnColorSelectedListener {

        /**
         * Called when a specific color square has been selected.
         */
        void onColorSelected(@ColorInt int color);
    }

    public ColorPickerSwatch(
            final Context context,
            @ColorInt final int color,
            final boolean checked,
            final OnColorSelectedListener listener) {
        super(context);
        mColor = color;
        mOnColorSelectedListener = listener;

        mPressedColor = Util.resolveColor(context, R.attr.colorControlHighlight, 0);

        LayoutInflater.from(context).inflate(R.layout.color_picker_swatch, this);
        mSwatchImage = findViewById(R.id.color_picker_swatch);
        mCheckmarkImage = findViewById(R.id.color_picker_checkmark);
        setColor(color);
        setChecked(checked);
        setOnClickListener(this);
    }

    protected void setColor(@ColorInt final int color) {
        final Drawable[] colorDrawable = new Drawable[]{
                getContext().getResources().getDrawable(R.drawable.color_picker_swatch)
        };
        mSwatchImage.setImageDrawable(ColorStateDrawable.create(colorDrawable, color, mPressedColor));
    }

    private void setChecked(final boolean checked) {
        if (checked) {
            mCheckmarkImage.setVisibility(View.VISIBLE);
        } else {
            mCheckmarkImage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (mOnColorSelectedListener != null) {
            mOnColorSelectedListener.onColorSelected(mColor);
        }
    }
}
