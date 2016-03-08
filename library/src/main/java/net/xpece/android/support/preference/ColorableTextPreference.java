package net.xpece.android.support.preference;

import android.content.res.ColorStateList;
import android.support.annotation.ColorInt;

/**
 * Created by Eugen on 08.03.2016.
 */
public interface ColorableTextPreference {
    void setTitleTextColor(ColorStateList titleTextColor);

    void setTitleTextColor(@ColorInt int titleTextColor);

    void setTitleTextAppearance(int titleTextAppearance);

    void setSummaryTextColor(ColorStateList summaryTextColor);

    void setSummaryTextColor(@ColorInt int summaryTextColor);

    void setSummaryTextAppearance(int summaryTextAppearance);
}
