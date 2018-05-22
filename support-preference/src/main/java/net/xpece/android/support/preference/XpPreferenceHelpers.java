package net.xpece.android.support.preference;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;

@SuppressWarnings("deprecation")
public final class XpPreferenceHelpers {

    private XpPreferenceHelpers() {
        throw new AssertionError();
    }

    public static void setTitleTextColor(Preference preference, ColorStateList titleTextColor) {
        android.support.v7.preference.XpPreferenceHelpers.setTitleTextColor(preference, titleTextColor);
    }

    public static void setTitleTextColor(Preference preference, @ColorInt int titleTextColor) {
        android.support.v7.preference.XpPreferenceHelpers.setTitleTextColor(preference, titleTextColor);
    }

    public static void setTitleTextAppearance(Preference preference, @StyleRes int titleTextAppearance) {
        android.support.v7.preference.XpPreferenceHelpers.setTitleTextColor(preference, titleTextAppearance);
    }

    public static void setSummaryTextColor(Preference preference, ColorStateList summaryTextColor) {
        android.support.v7.preference.XpPreferenceHelpers.setTitleTextColor(preference, summaryTextColor);
    }

    public static void setSummaryTextColor(Preference preference, @ColorInt int summaryTextColor) {
        android.support.v7.preference.XpPreferenceHelpers.setTitleTextColor(preference, summaryTextColor);
    }

    public static void setSummaryTextAppearance(Preference preference, @StyleRes int summaryTextAppearance) {
        android.support.v7.preference.XpPreferenceHelpers.setTitleTextColor(preference, summaryTextAppearance);
    }

    public static boolean hasTitleTextColor(Preference preference) {
        return android.support.v7.preference.XpPreferenceHelpers.hasTitleTextColor(preference);
    }

    public static boolean hasSummaryTextColor(Preference preference) {
        return android.support.v7.preference.XpPreferenceHelpers.hasSummaryTextColor(preference);
    }

    public static boolean hasTitleTextAppearance(Preference preference) {
        return android.support.v7.preference.XpPreferenceHelpers.hasTitleTextAppearance(preference);
    }

    public static boolean hasSummaryTextAppearance(Preference preference) {
        return android.support.v7.preference.XpPreferenceHelpers.hasSummaryTextAppearance(preference);
    }

    public static void setIcon(final Preference preference, @Nullable final Drawable icon) {
        android.support.v7.preference.XpPreferenceHelpers.setIcon(preference, icon);
    }

    public static void setIcon(final Preference preference, @DrawableRes final int icon) {
        android.support.v7.preference.XpPreferenceHelpers.setIcon(preference, icon);
    }

    @Nullable
    public static Drawable getIcon(final Preference preference) {
        return android.support.v7.preference.XpPreferenceHelpers.getIcon(preference);
    }

    public static void setDialogIcon(final DialogPreference preference, @Nullable final Drawable icon) {
        android.support.v7.preference.XpPreferenceHelpers.setDialogIcon(preference, icon);
    }

    public static void setDialogIcon(final DialogPreference preference, @DrawableRes final int icon) {
        android.support.v7.preference.XpPreferenceHelpers.setDialogIcon(preference, icon);
    }

    @Nullable
    public static Drawable getDialogIcon(final DialogPreference preference) {
        return android.support.v7.preference.XpPreferenceHelpers.getDialogIcon(preference);
    }

    public static void setOnPreferenceLongClickListener(final Preference preference, @Nullable final OnPreferenceLongClickListener listener) {
        android.support.v7.preference.XpPreferenceHelpers.setOnPreferenceLongClickListener(preference, listener);
    }

    public static boolean hasOnPreferenceLongClickListener(final Preference preference) {
        return android.support.v7.preference.XpPreferenceHelpers.hasOnPreferenceLongClickListener(preference);
    }
}
