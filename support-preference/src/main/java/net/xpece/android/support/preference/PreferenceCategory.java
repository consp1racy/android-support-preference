package net.xpece.android.support.preference;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.preference.PreferenceViewHolder;

/**
 * Created by Eugen on 08.03.2016.
 */
public class PreferenceCategory extends androidx.preference.PreferenceCategory
        implements ColorableTextPreference, LongClickablePreference,
        TintablePreference, CustomIconPreference {

    private PreferenceTextHelper mPreferenceTextHelper;
    private PreferenceIconHelper mPreferenceIconHelper;

    OnPreferenceLongClickListener mOnPreferenceLongClickListener;

    public PreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public PreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.preferenceCategoryStyle);
    }

    public PreferenceCategory(@NonNull Context context) {
        this(context, null);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        mPreferenceTextHelper = new PreferenceTextHelper();
        mPreferenceTextHelper.init(context, attrs, defStyleAttr, defStyleRes);
        mPreferenceIconHelper = new PreferenceIconHelper(this);
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
    public boolean isSupportIconTintEnabled() {
        return mPreferenceIconHelper.isIconTintEnabled();
    }

    @Override
    public void setSupportIconTintEnabled(boolean enabled) {
        mPreferenceIconHelper.setIconTintEnabled(enabled);
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

    @Nullable
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
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mPreferenceTextHelper.onBindViewHolder(holder);

        LongClickBinder.bindLongClickListener(this, holder, mOnPreferenceLongClickListener);
    }

    @Override
    public void setTitleTextColor(@NonNull ColorStateList titleTextColor) {
        mPreferenceTextHelper.setTitleTextColor(titleTextColor);
        notifyChanged();
    }

    @Override
    public void setTitleTextColor(@ColorInt int titleTextColor) {
        mPreferenceTextHelper.setTitleTextColor(titleTextColor);
        notifyChanged();
    }

    @Override
    public void setTitleTextAppearance(@StyleRes int titleTextAppearance) {
        mPreferenceTextHelper.setTitleTextAppearance(titleTextAppearance);
        notifyChanged();
    }

    @Override
    public void setSummaryTextColor(@NonNull ColorStateList summaryTextColor) {
        mPreferenceTextHelper.setSummaryTextColor(summaryTextColor);
        notifyChanged();
    }

    @Override
    public void setSummaryTextColor(@ColorInt int summaryTextColor) {
        mPreferenceTextHelper.setSummaryTextColor(summaryTextColor);
        notifyChanged();
    }

    @Override
    public void setSummaryTextAppearance(@StyleRes int summaryTextAppearance) {
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
}
