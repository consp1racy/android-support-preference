/*
 * Copyright (C) 2015 The Android Open Source Project
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

package android.support.v7.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.StyleableRes;
import android.support.v7.appcompat.R;
import android.support.v7.text.AllCapsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.widget.TextView;

@SuppressWarnings("RestrictedApi")
class AspAppCompatTextHelper {

    // This has to be sorted!!!
    static final int[] R_styleable_AppCompatTextHelper = {
        android.R.attr.textAppearance,
        android.R.attr.drawableTop,
        android.R.attr.drawableBottom,
        android.R.attr.drawableLeft,
        android.R.attr.drawableRight,
        android.R.attr.drawableStart,
        android.R.attr.drawableEnd
    };
    @StyleableRes static final int R_styleable_AppCompatTextHelper_android_textAppearance = 0;
    @StyleableRes static final int R_styleable_AppCompatTextHelper_android_drawableTop = 1;
    @StyleableRes static final int R_styleable_AppCompatTextHelper_android_drawableBottom = 2;
    @StyleableRes static final int R_styleable_AppCompatTextHelper_android_drawableLeft = 3;
    @StyleableRes static final int R_styleable_AppCompatTextHelper_android_drawableRight = 4;
    @StyleableRes static final int R_styleable_AppCompatTextHelper_android_drawableStart = 5;
    @StyleableRes static final int R_styleable_AppCompatTextHelper_android_drawableEnd = 6;

    static AspAppCompatTextHelper create(TextView textView) {
        if (Build.VERSION.SDK_INT >= 17) {
            return new AspAppCompatTextHelperV17(textView);
        }
        return new AspAppCompatTextHelper(textView);
    }

    final TextView mView;

    private TintInfo mDrawableLeftTint;
    private TintInfo mDrawableTopTint;
    private TintInfo mDrawableRightTint;
    private TintInfo mDrawableBottomTint;

    AspAppCompatTextHelper(TextView view) {
        mView = view;
    }

    void loadFromAttributes(AttributeSet attrs, int defStyleAttr) {
        final Context context = mView.getContext();
        final AppCompatDrawableManager drawableManager = AppCompatDrawableManager.get();

        // First read the TextAppearance style id
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs,
            R_styleable_AppCompatTextHelper, defStyleAttr, 0);
        final int ap = a.getResourceId(R_styleable_AppCompatTextHelper_android_textAppearance, -1);
        // Now read the compound drawable and grab any tints
        if (a.hasValue(R_styleable_AppCompatTextHelper_android_drawableLeft)) {
            mDrawableLeftTint = createTintInfo(context, drawableManager,
                a.getResourceId(R_styleable_AppCompatTextHelper_android_drawableLeft, 0));
        }
        if (a.hasValue(R_styleable_AppCompatTextHelper_android_drawableTop)) {
            mDrawableTopTint = createTintInfo(context, drawableManager,
                a.getResourceId(R_styleable_AppCompatTextHelper_android_drawableTop, 0));
        }
        if (a.hasValue(R_styleable_AppCompatTextHelper_android_drawableRight)) {
            mDrawableRightTint = createTintInfo(context, drawableManager,
                a.getResourceId(R_styleable_AppCompatTextHelper_android_drawableRight, 0));
        }
        if (a.hasValue(R_styleable_AppCompatTextHelper_android_drawableBottom)) {
            mDrawableBottomTint = createTintInfo(context, drawableManager,
                a.getResourceId(R_styleable_AppCompatTextHelper_android_drawableBottom, 0));
        }
        a.recycle();

        // PasswordTransformationMethod wipes out all other TransformationMethod instances
        // in TextView's constructor, so we should only set a new transformation method
        // if we don't have a PasswordTransformationMethod currently...
        final boolean hasPwdTm =
            mView.getTransformationMethod() instanceof PasswordTransformationMethod;
        boolean allCaps = false;
        boolean allCapsSet = false;
        ColorStateList textColor = null;
        ColorStateList textColorHint = null;

        // First check TextAppearance's textAllCaps value
        if (ap != -1) {
            a = TintTypedArray.obtainStyledAttributes(context, null, R.styleable.TextAppearance, 0, ap);
            if (!hasPwdTm && a.hasValue(R.styleable.TextAppearance_textAllCaps)) {
                allCapsSet = true;
                allCaps = a.getBoolean(R.styleable.TextAppearance_textAllCaps, false);
            }
            if (Build.VERSION.SDK_INT < 23) {
                // If we're running on < API 23, the text color may contain theme references
                // so let's re-set using our own inflater
                if (a.hasValue(R.styleable.TextAppearance_android_textColor)) {
                    textColor = a.getColorStateList(R.styleable.TextAppearance_android_textColor);
                }
                try {
                    if (a.hasValue(R.styleable.TextAppearance_android_textColorHint)) {
                        textColorHint = a.getColorStateList(
                            R.styleable.TextAppearance_android_textColorHint);
                    }
                } catch (NoSuchFieldError ex) {
                    // Since support libs 25.3.0.
                }
            }
            a.recycle();
        }

        // Now read the style's values
        a = TintTypedArray.obtainStyledAttributes(context, attrs, R.styleable.TextAppearance,
            defStyleAttr, 0);
        if (!hasPwdTm && a.hasValue(R.styleable.TextAppearance_textAllCaps)) {
            allCapsSet = true;
            allCaps = a.getBoolean(R.styleable.TextAppearance_textAllCaps, false);
        }
        if (Build.VERSION.SDK_INT < 23) {
            // If we're running on < API 23, the text color may contain theme references
            // so let's re-set using our own inflater
            if (a.hasValue(R.styleable.TextAppearance_android_textColor)) {
                textColor = a.getColorStateList(R.styleable.TextAppearance_android_textColor);
            }
            try {
                if (a.hasValue(R.styleable.TextAppearance_android_textColorHint)) {
                    textColorHint = a.getColorStateList(
                        R.styleable.TextAppearance_android_textColorHint);
                }
            } catch (NoSuchFieldError ex) {
                // Since support libs 25.3.0.
            }
        }
        a.recycle();

        if (textColor != null) {
            mView.setTextColor(textColor);
        }
        if (textColorHint != null) {
            mView.setHintTextColor(textColorHint);
        }
        if (!hasPwdTm && allCapsSet) {
            setAllCaps(allCaps);
        }
    }

    void onSetTextAppearance(Context context, int resId) {
        ColorStateList textColor = null;
        ColorStateList textColorHint = null;

        final TintTypedArray a = TintTypedArray.obtainStyledAttributes(context,
            null, R.styleable.TextAppearance, 0, resId);
        if (a.hasValue(R.styleable.TextAppearance_textAllCaps)) {
            // This breaks away slightly from the logic in TextView.setTextAppearance that serves
            // as an "overlay" on the current state of the TextView. Since android:textAllCaps
            // may have been set to true in this text appearance, we need to make sure that
            // app:textAllCaps has the chance to override it
            setAllCaps(a.getBoolean(R.styleable.TextAppearance_textAllCaps, false));
        }
        if (Build.VERSION.SDK_INT < 23) {
            // If we're running on < API 23, the text color may contain theme references
            // so let's re-set using our own inflater
            if (a.hasValue(R.styleable.TextAppearance_android_textColor)) {
                textColor = a.getColorStateList(R.styleable.TextAppearance_android_textColor);
            }
            try {
                if (a.hasValue(R.styleable.TextAppearance_android_textColorHint)) {
                    textColorHint = a.getColorStateList(
                        R.styleable.TextAppearance_android_textColorHint);
                }
            } catch (NoSuchFieldError ex) {
                // Since support libs 25.3.0.
            }
        }
        a.recycle();

        if (textColor != null) {
            mView.setTextColor(textColor);
        }
        if (textColorHint != null) {
            mView.setHintTextColor(textColorHint);
        }
    }

    void setAllCaps(boolean allCaps) {
        mView.setTransformationMethod(allCaps
            ? new AllCapsTransformationMethod(mView.getContext())
            : null);
    }

    void applyCompoundDrawablesTints() {
        if (mDrawableLeftTint != null || mDrawableTopTint != null ||
            mDrawableRightTint != null || mDrawableBottomTint != null) {
            final Drawable[] compoundDrawables = mView.getCompoundDrawables();
            applyCompoundDrawableTint(compoundDrawables[0], mDrawableLeftTint);
            applyCompoundDrawableTint(compoundDrawables[1], mDrawableTopTint);
            applyCompoundDrawableTint(compoundDrawables[2], mDrawableRightTint);
            applyCompoundDrawableTint(compoundDrawables[3], mDrawableBottomTint);
        }
    }

    final void applyCompoundDrawableTint(Drawable drawable, TintInfo info) {
        if (drawable != null && info != null) {
            AppCompatDrawableManager.tintDrawable(drawable, info, mView.getDrawableState());
        }
    }

    protected static TintInfo createTintInfo(Context context,
                                             AppCompatDrawableManager drawableManager, int drawableId) {
        final ColorStateList tintList = drawableManager.getTintList(context, drawableId);
        if (tintList != null) {
            final TintInfo tintInfo = new TintInfo();
            tintInfo.mHasTintList = true;
            tintInfo.mTintList = tintList;
            return tintInfo;
        }
        return null;
    }
}
