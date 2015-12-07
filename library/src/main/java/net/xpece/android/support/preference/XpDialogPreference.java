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

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

/**
 * A base class for {@link XpPreference} objects that are
 * dialog-based. These preferences will, when clicked, open a dialog showing the
 * actual preference controls.
 */
public abstract class XpDialogPreference extends DialogPreference
    implements TintablePreference, TintableDialogPreference,
    CustomIconPreference, CustomDialogIconPreference {

    private PreferenceIconHelper mPreferenceIconHelper;
    private DialogPreferenceIconHelper mDialogPreferenceIconHelper;

    public XpDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public XpDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_Material_DialogPreference);
    }

    public XpDialogPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public XpDialogPreference(Context context) {
        this(context, null);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mPreferenceIconHelper = new PreferenceIconHelper(this);
        mPreferenceIconHelper.loadFromAttributes(attrs, defStyleAttr, defStyleRes);

        mDialogPreferenceIconHelper = new DialogPreferenceIconHelper(this);
        mDialogPreferenceIconHelper.loadFromAttributes(attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setSupportDialogIcon(final Drawable icon) {
        mDialogPreferenceIconHelper.setIcon(icon);
    }

    @Override
    public void setSupportDialogIcon(@DrawableRes final int icon) {
        mDialogPreferenceIconHelper.setIcon(icon);
    }

    @Override
    public Drawable getSupportDialogIcon() {
        return mDialogPreferenceIconHelper.getIcon();
    }

    @Override
    public void setSupportIcon(final Drawable icon) {
        mPreferenceIconHelper.setIcon(icon);
    }

    @Override
    public void setSupportIcon(@DrawableRes final int icon) {
        mPreferenceIconHelper.setIcon(icon);
    }

    @Override
    public Drawable getSupportIcon() {
        return mPreferenceIconHelper.getIcon();
    }

    @Override
    public void setSupportIconTintList(@Nullable final ColorStateList tint) {
        mPreferenceIconHelper.setTintList(tint);
    }

    @Nullable
    @Override
    public ColorStateList getSupportIconTintList() {
        return mPreferenceIconHelper.getTintList();
    }

    @Override
    public void setSupportIconTintMode(@Nullable final PorterDuff.Mode tintMode) {
        mPreferenceIconHelper.setTintMode(tintMode);
    }

    @Nullable
    @Override
    public PorterDuff.Mode getSupportIconTintMode() {
        return mPreferenceIconHelper.getTintMode();
    }

    @Override
    public void setSupportDialogIconTintList(@Nullable final ColorStateList tint) {
        mDialogPreferenceIconHelper.setTintList(tint);
    }

    @Nullable
    @Override
    public ColorStateList getSupportDialogIconTintList() {
        return mDialogPreferenceIconHelper.getTintList();
    }

    @Override
    public void setSupportDialogIconTintMode(@Nullable final PorterDuff.Mode tintMode) {
        mDialogPreferenceIconHelper.setTintMode(tintMode);
    }

    @Nullable
    @Override
    public PorterDuff.Mode getSupportDialogIconTintMode() {
        return mDialogPreferenceIconHelper.getTintMode();
    }
}
