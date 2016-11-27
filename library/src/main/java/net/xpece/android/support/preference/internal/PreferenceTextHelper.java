package net.xpece.android.support.preference.internal;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.ColorInt;
import android.support.annotation.RestrictTo;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.widget.TintTypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import net.xpece.android.support.preference.R;

/**
 * Created by Eugen on 08.03.2016.
 */
@RestrictTo(RestrictTo.Scope.GROUP_ID)
@SuppressWarnings("RestrictedApi")
public class PreferenceTextHelper {

    private boolean mHasTitleTextAppearance = false;
    private int mTitleTextAppearance = 0;
    private boolean mHasTitleTextColor = false;
    private ColorStateList mTitleTextColor = null;
    private boolean mHasSubtitleTextAppearance = false;
    private int mSubtitleTextAppearance = 0;
    private boolean mHasSubtitleTextColor = false;
    private ColorStateList mSubtitleTextColor = null;

    public void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TintTypedArray ta = TintTypedArray.obtainStyledAttributes(context, attrs, R.styleable.Preference, defStyleAttr, defStyleRes);
        if (ta.hasValue(R.styleable.Preference_titleTextAppearance)) {
            mTitleTextAppearance = ta.getResourceId(R.styleable.Preference_titleTextAppearance, 0);
            mHasTitleTextAppearance = true;
        }
        if (ta.hasValue(R.styleable.Preference_titleTextColor)) {
            mTitleTextColor = ta.getColorStateList(R.styleable.Preference_titleTextColor);
            mHasTitleTextColor = true;
        }
        if (ta.hasValue(R.styleable.Preference_subtitleTextAppearance)) {
            mSubtitleTextAppearance = ta.getResourceId(R.styleable.Preference_subtitleTextAppearance, 0);
            mHasSubtitleTextAppearance = true;
        }
        if (ta.hasValue(R.styleable.Preference_subtitleTextColor)) {
            mSubtitleTextColor = ta.getColorStateList(R.styleable.Preference_subtitleTextColor);
            mHasSubtitleTextColor = true;
        }
        ta.recycle();
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        if (titleView != null) {
            if (mHasTitleTextAppearance) {
                TextViewCompat.setTextAppearance(titleView, mTitleTextAppearance);
            }
            if (mHasTitleTextColor) {
                titleView.setTextColor(mTitleTextColor);
            }
        }

        TextView subtitleView = (TextView) holder.findViewById(android.R.id.summary);
        if (subtitleView != null) {
            if (mHasSubtitleTextAppearance) {
                TextViewCompat.setTextAppearance(subtitleView, mSubtitleTextAppearance);
            }
            if (mHasSubtitleTextColor) {
                subtitleView.setTextColor(mSubtitleTextColor);
            }
        }
    }

    public void setTitleTextColor(ColorStateList titleTextColor) {
        mTitleTextColor = titleTextColor;
        mHasTitleTextColor = true;
    }

    public void setTitleTextColor(@ColorInt int titleTextColor) {
        mTitleTextColor = ColorStateList.valueOf(titleTextColor);
        mHasTitleTextColor = true;
    }

    public void setTitleTextAppearance(int titleTextAppearance) {
        mTitleTextAppearance = titleTextAppearance;
        mHasTitleTextAppearance = true;
    }

    public void setSummaryTextColor(ColorStateList summaryTextColor) {
        mSubtitleTextColor = summaryTextColor;
        mHasSubtitleTextColor = true;
    }

    public void setSummaryTextColor(@ColorInt int summaryTextColor) {
        mSubtitleTextColor = ColorStateList.valueOf(summaryTextColor);
        mHasSubtitleTextColor = true;
    }

    public void setSummaryTextAppearance(int summaryTextAppearance) {
        mSubtitleTextAppearance = summaryTextAppearance;
        mHasSubtitleTextAppearance = true;
    }

    public boolean hasTitleTextColor() {
        return mHasTitleTextColor;
    }

    public boolean hasSummaryTextColor() {
        return mHasSubtitleTextColor;
    }

    public boolean hasTitleTextAppearance() {
        return mHasTitleTextAppearance;
    }

    public boolean hasSummaryTextAppearance() {
        return mHasSubtitleTextAppearance;
    }
}
