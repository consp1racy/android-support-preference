package net.xpece.android.support.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.widget.TextViewCompat;
import androidx.preference.PreferenceViewHolder;
import androidx.appcompat.widget.TintTypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Eugen on 08.03.2016.
 */
@SuppressLint("RestrictedApi")
public class PreferenceTextHelper {

    private boolean mHasTitleTextAppearance = false;
    private int mTitleTextAppearance = 0;
    private boolean mHasTitleTextColor = false;
    private ColorStateList mTitleTextColor = null;
    private boolean mHasSubtitleTextAppearance = false;
    private int mSubtitleTextAppearance = 0;
    private boolean mHasSubtitleTextColor = false;
    private ColorStateList mSubtitleTextColor = null;

    public void init(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        final TintTypedArray ta = TintTypedArray.obtainStyledAttributes(context, attrs, R.styleable.Preference, defStyleAttr, defStyleRes);
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

    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
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

    public void setTitleTextColor(@NonNull ColorStateList titleTextColor) {
        mTitleTextColor = titleTextColor;
        mHasTitleTextColor = true;
    }

    public void setTitleTextColor(@ColorInt int titleTextColor) {
        mTitleTextColor = ColorStateList.valueOf(titleTextColor);
        mHasTitleTextColor = true;
    }

    public void setTitleTextAppearance(@StyleRes int titleTextAppearance) {
        mTitleTextAppearance = titleTextAppearance;
        mHasTitleTextAppearance = true;
    }

    public void setSummaryTextColor(@NonNull ColorStateList summaryTextColor) {
        mSubtitleTextColor = summaryTextColor;
        mHasSubtitleTextColor = true;
    }

    public void setSummaryTextColor(@ColorInt int summaryTextColor) {
        mSubtitleTextColor = ColorStateList.valueOf(summaryTextColor);
        mHasSubtitleTextColor = true;
    }

    public void setSummaryTextAppearance(@StyleRes int summaryTextAppearance) {
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
