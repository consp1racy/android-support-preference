/*
 * Copyright (C) 2014 The Android Open Source Project
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
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

import net.xpece.android.support.preference.R;

/**
 * A {@link CheckedTextView} which supports compatible features on older version of the platform.
 * <p/>
 * <p>This will automatically be used when you use {@link CheckedTextView} in your layouts.
 * You should only need to manually use this class when writing custom views.</p>
 */
public class AppCompatCheckedTextView2 extends CheckedTextView {

    private static final int[] TINT_ATTRS = {
        android.R.attr.checkMark
    };

    private TintManager mTintManager;
    private AppCompatTextHelper2 mTextHelper;

    public AppCompatCheckedTextView2(Context context) {
        this(context, null);
    }

    public AppCompatCheckedTextView2(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.checkedTextViewStyle);
    }

    public AppCompatCheckedTextView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mTextHelper = AppCompatTextHelper2.create(this);
        mTextHelper.loadFromAttributes(attrs, defStyleAttr);
        mTextHelper.applyCompoundDrawablesTints();

        if (TintManager.SHOULD_BE_USED) {
            TintTypedArray a = TintTypedArray.obtainStyledAttributes(getContext(), attrs, TINT_ATTRS, defStyleAttr, 0);
            mTintManager = a.getTintManager();
            final Drawable drawable = a.getDrawable(0);
            setCheckMarkDrawable(drawable);
            a.recycle();
        }
    }

    @Override
    public void setCheckMarkDrawable(@DrawableRes int resId) {
        if (mTintManager != null) {
            setCheckMarkDrawable(mTintManager.getDrawable(resId));
        } else {
            super.setCheckMarkDrawable(resId);
        }
    }

    @Override
    public void setTextAppearance(Context context, int resId) {
        super.setTextAppearance(context, resId);
        if (mTextHelper != null) {
            mTextHelper.onSetTextAppearance(context, resId);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mTextHelper != null) {
            mTextHelper.applyCompoundDrawablesTints();
        }
    }
}
