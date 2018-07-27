package net.xpece.android.support.preference;

import android.content.res.ColorStateList;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;

/**
 * Created by Eugen on 08.03.2016.
 */
public interface ColorableTextPreference {
    void setTitleTextColor(@NonNull ColorStateList titleTextColor);

    void setTitleTextColor(@ColorInt int titleTextColor);

    void setTitleTextAppearance(@StyleRes int titleTextAppearance);

    void setSummaryTextColor(@NonNull ColorStateList summaryTextColor);

    void setSummaryTextColor(@ColorInt int summaryTextColor);

    void setSummaryTextAppearance(@StyleRes int summaryTextAppearance);

    boolean hasTitleTextColor();

    boolean hasSummaryTextColor();

    boolean hasTitleTextAppearance();

    boolean hasSummaryTextAppearance();
}
