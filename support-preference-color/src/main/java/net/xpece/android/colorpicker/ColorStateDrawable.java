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

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;

/**
 * A drawable which sets its color filter to a color specified by the user, and changes to a
 * slightly darker color when pressed or focused.
 */
public class ColorStateDrawable extends LayerDrawable {

//    private static final float PRESSED_STATE_MULTIPLIER = 0.70f;

    private int mColor;
    private int mPressed;

    @NonNull
    public static Drawable create(
            @NonNull final Drawable[] layers,
            @ColorInt final int color,
            @ColorInt final int pressed) {
        if (Build.VERSION.SDK_INT >= 21) {
            final LayerDrawable ld = new LayerDrawable(layers);
            ld.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            return new RippleDrawable(ColorStateList.valueOf(pressed), ld, null);
        } else {
            return new ColorStateDrawable(layers, color, pressed);
        }
    }

    public ColorStateDrawable(
            @NonNull final Drawable[] layers,
            @ColorInt final int color,
            @ColorInt final int pressed) {
        super(layers);
        mColor = color;
        mPressed = pressed;
    }

    @Override
    protected boolean onStateChange(@NonNull int[] states) {
        boolean pressedOrFocused = false;
        for (int state : states) {
            if (state == android.R.attr.state_pressed || state == android.R.attr.state_focused) {
                pressedOrFocused = true;
                break;
            }
        }

        if (pressedOrFocused) {
//            super.setColorFilter(getPressedColor(mColor), PorterDuff.Mode.SRC_IN);
            super.setColorFilter(getPressedColor(), PorterDuff.Mode.SRC_IN);
        } else {
            super.setColorFilter(mColor, PorterDuff.Mode.SRC_IN);
        }

        return super.onStateChange(states);
    }

    @ColorInt
    private int getPressedColor() {
        return ColorUtils.compositeColors(mPressed, mColor);
    }

//    /**
//     * Given a particular color, adjusts its value by a multiplier.
//     */
//    private static int getPressedColor(int color) {
//        float[] hsv = new float[3];
//        Color.colorToHSV(color, hsv);
//        hsv[2] = hsv[2] * PRESSED_STATE_MULTIPLIER;
//        return Color.HSVToColor(hsv);
//    }

    @Override
    public boolean isStateful() {
        return true;
    }
}
