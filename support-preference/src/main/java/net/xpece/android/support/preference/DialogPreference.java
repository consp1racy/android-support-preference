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
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A base class for {@link Preference} objects that are
 * dialog-based. These preferences will, when clicked, open a dialog showing the
 * actual preference controls.
 */
@ParametersAreNonnullByDefault
public abstract class DialogPreference extends android.support.v7.preference.DialogPreference
    implements TintablePreference, TintableDialogPreference,
    CustomIconPreference, CustomDialogIconPreference, ColorableTextPreference,
    LongClickablePreference {

    private PreferenceTextHelper mPreferenceTextHelper;
    private PreferenceIconHelper mPreferenceIconHelper;
    private DialogPreferenceIconHelper mDialogPreferenceIconHelper;

    @SuppressWarnings("WeakerAccess")
    OnPreferenceLongClickListener mOnPreferenceLongClickListener;

    public DialogPreference(Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public DialogPreference(Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_Material_DialogPreference);
    }

    public DialogPreference(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public DialogPreference(Context context) {
        this(context, null);
    }

    private void init(Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        mPreferenceIconHelper = new PreferenceIconHelper(this);
        mPreferenceIconHelper.loadFromAttributes(attrs, defStyleAttr, defStyleRes);

        mDialogPreferenceIconHelper = new DialogPreferenceIconHelper(this);
        mDialogPreferenceIconHelper.loadFromAttributes(attrs, defStyleAttr, defStyleRes);

        mPreferenceTextHelper = new PreferenceTextHelper();
        mPreferenceTextHelper.init(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean isSupportIconPaddingEnabled() {
        return mPreferenceIconHelper.isIconPaddingEnabled();
    }

    @Override
    public void setSupportIconPaddingEnabled(boolean enabled) {
        mPreferenceIconHelper.setIconPaddingEnabled(enabled);
    }

    @Override
    public boolean isSupportDialogIconPaddingEnabled() {
        return mDialogPreferenceIconHelper.isIconPaddingEnabled();
    }

    @Override
    public void setSupportDialogIconPaddingEnabled(boolean enabled) {
        mDialogPreferenceIconHelper.setIconPaddingEnabled(enabled);
    }

    @Override
    public boolean isSupportIconTintEnabled() {
        return mPreferenceIconHelper.isIconTintEnabled();
    }

    @Override
    public void setSupportIconTintEnabled(boolean enabled) {
        mPreferenceIconHelper.setIconTintEnabled(enabled);
    }

    @Override
    public boolean isSupportDialogIconTintEnabled() {
        return mDialogPreferenceIconHelper.isIconTintEnabled();
    }

    @Override
    public void setSupportDialogIconTintEnabled(boolean enabled) {
        mDialogPreferenceIconHelper.setIconTintEnabled(enabled);
    }

    @Override
    public void setSupportDialogIcon(@Nullable final Drawable icon) {
        mDialogPreferenceIconHelper.setIcon(icon);
    }

    @Override
    public void setSupportDialogIcon(@DrawableRes final int icon) {
        mDialogPreferenceIconHelper.setIcon(icon);
    }

    @Nullable
    @Override
    public Drawable getSupportDialogIcon() {
        return mDialogPreferenceIconHelper.getIcon();
    }

    @Override
    public void setSupportIcon(@Nullable final Drawable icon) {
        mPreferenceIconHelper.setIcon(icon);
    }

    @Override
    public void setSupportIcon(@DrawableRes final int icon) {
        mPreferenceIconHelper.setIcon(icon);
    }

    @Nullable
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

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mPreferenceTextHelper.onBindViewHolder(holder);

        final boolean hasLongClickListener = hasOnPreferenceLongClickListener();
        if (hasLongClickListener) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(@NonNull View v) {
                    return mOnPreferenceLongClickListener.onLongClick(DialogPreference.this, v);
                }
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
        }
        holder.itemView.setLongClickable(hasLongClickListener && isSelectable());
    }

    @Override
    public void setTitleTextColor(ColorStateList titleTextColor) {
        mPreferenceTextHelper.setTitleTextColor(titleTextColor);
        notifyChanged();
    }

    @Override
    public void setTitleTextColor(@ColorInt int titleTextColor) {
        mPreferenceTextHelper.setTitleTextColor(titleTextColor);
        notifyChanged();
    }

    @Override
    public void setTitleTextAppearance(int titleTextAppearance) {
        mPreferenceTextHelper.setTitleTextAppearance(titleTextAppearance);
        notifyChanged();
    }

    @Override
    public void setSummaryTextColor(ColorStateList summaryTextColor) {
        mPreferenceTextHelper.setSummaryTextColor(summaryTextColor);
        notifyChanged();
    }

    @Override
    public void setSummaryTextColor(@ColorInt int summaryTextColor) {
        mPreferenceTextHelper.setSummaryTextColor(summaryTextColor);
        notifyChanged();
    }

    @Override
    public void setSummaryTextAppearance(int summaryTextAppearance) {
        mPreferenceTextHelper.setSummaryTextAppearance(summaryTextAppearance);
        notifyChanged();
    }

    @Override
    public boolean hasTitleTextColor() {
        return mPreferenceTextHelper.hasTitleTextColor();
    }

    @Override
    public boolean hasSummaryTextColor() {
        return mPreferenceTextHelper.hasSummaryTextColor();
    }

    @Override
    public boolean hasTitleTextAppearance() {
        return mPreferenceTextHelper.hasTitleTextAppearance();
    }

    @Override
    public boolean hasSummaryTextAppearance() {
        return mPreferenceTextHelper.hasSummaryTextAppearance();
    }

    @Override
    public void setOnPreferenceLongClickListener(@Nullable OnPreferenceLongClickListener listener) {
        if (listener != mOnPreferenceLongClickListener) {
            mOnPreferenceLongClickListener = listener;
            notifyChanged();
        }
    }

    @Override
    public boolean hasOnPreferenceLongClickListener() {
        return mOnPreferenceLongClickListener != null;
    }


    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(final Parcelable state) {
        super.onRestoreInstanceState(state);
    }
}
