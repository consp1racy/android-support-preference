package android.support.v7.preference;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.RestrictTo;
import android.util.AttributeSet;

import net.xpece.android.support.preference.ColorableTextPreference;
import net.xpece.android.support.preference.CustomDialogIconPreference;
import net.xpece.android.support.preference.CustomIconPreference;
import net.xpece.android.support.preference.DialogPreferenceIconHelper;
import net.xpece.android.support.preference.PreferenceIconHelper;
import net.xpece.android.support.preference.PreferenceTextHelper;

import java.util.WeakHashMap;

/**
 * Created by Eugen on 27.11.2016.
 */

public final class XpPreferenceHelpers {

    static final WeakHashMap<Preference, PreferenceTextHelper> PREFERENCE_TEXT_HELPERS = new WeakHashMap<>();
    static final WeakHashMap<Preference, PreferenceIconHelper> PREFERENCE_ICON_HELPERS = new WeakHashMap<>();
    static final WeakHashMap<DialogPreference, DialogPreferenceIconHelper> PREFERENCE_DIALOG_ICON_HELPERS = new WeakHashMap<>();

    private XpPreferenceHelpers() {}

    @RestrictTo(RestrictTo.Scope.GROUP_ID)
    static void onCreatePreference(final Preference preference, final AttributeSet attrs) {
        final int defStyleAttr = getDefStyleAttr(preference);

        if (!(preference instanceof CustomIconPreference)) {
            final PreferenceIconHelper iconHelper = new PreferenceIconHelper(preference);
            iconHelper.loadFromAttributes(attrs, defStyleAttr, 0);
            PREFERENCE_ICON_HELPERS.put(preference, iconHelper);
        }

        if (preference instanceof DialogPreference && !(preference instanceof CustomDialogIconPreference)) {
            DialogPreference dialogPreference = (DialogPreference) preference;
            final DialogPreferenceIconHelper iconHelper = new DialogPreferenceIconHelper(dialogPreference);
            iconHelper.loadFromAttributes(attrs, defStyleAttr, 0);
            PREFERENCE_DIALOG_ICON_HELPERS.put(dialogPreference, iconHelper);
        }

        if (!(preference instanceof ColorableTextPreference)) {
            final PreferenceTextHelper textHelper = new PreferenceTextHelper();
            textHelper.init(preference.getContext(), attrs, defStyleAttr, 0);
            PREFERENCE_TEXT_HELPERS.put(preference, textHelper);
        }
    }

    private static int getDefStyleAttr(Preference preference) {
        final int defStyleAttr;
        if (preference instanceof PreferenceScreen) {
            defStyleAttr = R.attr.preferenceScreenStyle;
        } else if (preference instanceof PreferenceCategory) {
            defStyleAttr = R.attr.preferenceCategoryStyle;
        } else if (preference instanceof PreferenceGroup) {
            defStyleAttr = 0;
        } else {
            defStyleAttr = R.attr.preferenceStyle;
        }
        return defStyleAttr;
    }

    @RestrictTo(RestrictTo.Scope.GROUP_ID)
    static void onBindViewHolder(Preference preference, PreferenceViewHolder holder) {
        final PreferenceTextHelper textHelper = PREFERENCE_TEXT_HELPERS.get(preference);
        if (textHelper != null) {
            textHelper.onBindViewHolder(holder);
        }
    }

    public static void setTitleTextColor(Preference preference, ColorStateList titleTextColor) {
        if (preference instanceof ColorableTextPreference) {
            ((ColorableTextPreference) preference).setTitleTextColor(titleTextColor);
            preference.notifyChanged();
        } else {
            final PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
            if (pth != null) {
                pth.setTitleTextColor(titleTextColor);
                preference.notifyChanged();
            }
        }
    }

    public static void setTitleTextColor(Preference preference, @ColorInt int titleTextColor) {
        if (preference instanceof ColorableTextPreference) {
            ((ColorableTextPreference) preference).setTitleTextColor(titleTextColor);
            preference.notifyChanged();
        } else {
            final PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
            if (pth != null) {
                pth.setTitleTextColor(titleTextColor);
                preference.notifyChanged();
            }
        }
    }

    public static void setTitleTextAppearance(Preference preference, int titleTextAppearance) {
        if (preference instanceof ColorableTextPreference) {
            ((ColorableTextPreference) preference).setTitleTextAppearance(titleTextAppearance);
            preference.notifyChanged();
        } else {
            final PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
            if (pth != null) {
                pth.setTitleTextAppearance(titleTextAppearance);
                preference.notifyChanged();
            }
        }
    }

    public static void setSummaryTextColor(Preference preference, ColorStateList summaryTextColor) {
        if (preference instanceof ColorableTextPreference) {
            ((ColorableTextPreference) preference).setSummaryTextColor(summaryTextColor);
            preference.notifyChanged();
        } else {
            final PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
            if (pth != null) {
                pth.setSummaryTextColor(summaryTextColor);
                preference.notifyChanged();
            }
        }
    }

    public static void setSummaryTextColor(Preference preference, @ColorInt int summaryTextColor) {
        if (preference instanceof ColorableTextPreference) {
            ((ColorableTextPreference) preference).setSummaryTextColor(summaryTextColor);
            preference.notifyChanged();
        } else {
            final PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
            if (pth != null) {
                pth.setSummaryTextColor(summaryTextColor);
                preference.notifyChanged();
            }
        }
    }

    public static void setSummaryTextAppearance(Preference preference, int summaryTextAppearance) {
        if (preference instanceof ColorableTextPreference) {
            ((ColorableTextPreference) preference).setSummaryTextAppearance(summaryTextAppearance);
            preference.notifyChanged();
        } else {
            final PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
            if (pth != null) {
                pth.setSummaryTextAppearance(summaryTextAppearance);
                preference.notifyChanged();
            }
        }
    }

    public static boolean hasTitleTextColor(Preference preference) {
        if (preference instanceof ColorableTextPreference) {
            return ((ColorableTextPreference) preference).hasTitleTextColor();
        }
        final PreferenceTextHelper mPreferenceTextHelper = PREFERENCE_TEXT_HELPERS.get(preference);
        if (mPreferenceTextHelper != null) {
            return mPreferenceTextHelper.hasTitleTextColor();
        }
        return false;
    }

    public static boolean hasSummaryTextColor(Preference preference) {
        if (preference instanceof ColorableTextPreference) {
            return ((ColorableTextPreference) preference).hasSummaryTextColor();
        }
        final PreferenceTextHelper mPreferenceTextHelper = PREFERENCE_TEXT_HELPERS.get(preference);
        if (mPreferenceTextHelper != null) {
            return mPreferenceTextHelper.hasSummaryTextColor();
        }
        return false;
    }

    public static boolean hasTitleTextAppearance(Preference preference) {
        if (preference instanceof ColorableTextPreference) {
            return ((ColorableTextPreference) preference).hasTitleTextAppearance();
        }
        final PreferenceTextHelper mPreferenceTextHelper = PREFERENCE_TEXT_HELPERS.get(preference);
        if (mPreferenceTextHelper != null) {
            return mPreferenceTextHelper.hasTitleTextAppearance();
        }
        return false;
    }

    public static boolean hasSummaryTextAppearance(Preference preference) {
        if (preference instanceof ColorableTextPreference) {
            return ((ColorableTextPreference) preference).hasSummaryTextAppearance();
        }
        final PreferenceTextHelper mPreferenceTextHelper = PREFERENCE_TEXT_HELPERS.get(preference);
        if (mPreferenceTextHelper != null) {
            return mPreferenceTextHelper.hasSummaryTextAppearance();
        }
        return false;
    }

    public static void setSupportIcon(final Preference preference, final Drawable icon) {
        if (preference instanceof CustomIconPreference) {
            ((CustomIconPreference) preference).setSupportIcon(icon);
            return;
        }
        final PreferenceIconHelper iconHelper = PREFERENCE_ICON_HELPERS.get(preference);
        if (iconHelper != null) {
            iconHelper.setIcon(icon);
            return;
        }
        preference.setIcon(icon);
    }

    public static void setSupportIcon(final Preference preference, @DrawableRes final int icon) {
        if (preference instanceof CustomIconPreference) {
            ((CustomIconPreference) preference).setSupportIcon(icon);
            return;
        }
        final PreferenceIconHelper iconHelper = PREFERENCE_ICON_HELPERS.get(preference);
        if (iconHelper != null) {
            iconHelper.setIcon(icon);
            return;
        }
        preference.setIcon(icon);
    }

    public static Drawable getSupportIcon(final Preference preference) {
        if (preference instanceof CustomIconPreference) {
            return ((CustomIconPreference) preference).getSupportIcon();
        }
        final PreferenceIconHelper iconHelper = PREFERENCE_ICON_HELPERS.get(preference);
        if (iconHelper != null) {
            return iconHelper.getIcon();
        }
        return preference.getIcon();
    }

    public static void setSupportDialogIcon(final DialogPreference preference, final Drawable icon) {
        if (preference instanceof CustomDialogIconPreference) {
            ((CustomIconPreference) preference).setSupportIcon(icon);
            return;
        }
        final DialogPreferenceIconHelper iconHelper = PREFERENCE_DIALOG_ICON_HELPERS.get(preference);
        if (iconHelper != null) {
            iconHelper.setIcon(icon);
            return;
        }
        preference.setIcon(icon);
    }

    public static void setSupportDialogIcon(final DialogPreference preference, @DrawableRes final int icon) {
        if (preference instanceof CustomDialogIconPreference) {
            ((CustomDialogIconPreference) preference).setSupportDialogIcon(icon);
            return;
        }
        final DialogPreferenceIconHelper iconHelper = PREFERENCE_DIALOG_ICON_HELPERS.get(preference);
        if (iconHelper != null) {
            iconHelper.setIcon(icon);
            return;
        }
        preference.setDialogIcon(icon);
    }

    public static Drawable getSupportDialogIcon(final DialogPreference preference) {
        if (preference instanceof CustomDialogIconPreference) {
            return ((CustomDialogIconPreference) preference).getSupportDialogIcon();
        }
        final DialogPreferenceIconHelper iconHelper = PREFERENCE_DIALOG_ICON_HELPERS.get(preference);
        if (iconHelper != null) {
            return iconHelper.getIcon();
        }
        return preference.getDialogIcon();
    }

}
