package net.xpece.android.support.preference;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.ColorInt;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Eugen on 08.03.2016.
 */
public class PreferenceCategory extends android.support.v7.preference.PreferenceCategory
    implements ColorableTextPreference, LongClickablePreference {

    private PreferenceTextHelper mPreferenceTextHelper;

    OnPreferenceLongClickListener mOnPreferenceLongClickListener;

    public PreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mPreferenceTextHelper = new PreferenceTextHelper();
        mPreferenceTextHelper.init(context, attrs, defStyleAttr, defStyleRes);
    }

    public PreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PreferenceCategory(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.preferenceCategoryStyle);
    }

    public PreferenceCategory(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mPreferenceTextHelper.onBindViewHolder(holder);

        if (hasOnPreferenceLongClickListener()) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return mOnPreferenceLongClickListener.onLongClick(PreferenceCategory.this, v);
                }
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
        }
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
    public void setOnPreferenceLongClickListener(OnPreferenceLongClickListener listener) {
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
