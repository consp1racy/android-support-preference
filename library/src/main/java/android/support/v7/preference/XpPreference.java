package android.support.v7.preference;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.RestrictTo;
import android.util.AttributeSet;
import android.view.View;

import net.xpece.android.support.preference.*;
import net.xpece.android.support.preference.internal.DialogPreferenceIconHelper;
import net.xpece.android.support.preference.internal.PreferenceIconHelper;
import net.xpece.android.support.preference.internal.PreferenceTextHelper;

import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * Created by Eugen on 27.11.2016.
 */

public final class XpPreference {

    private static final WeakHashMap<Preference, PreferenceTextHelper> PREFERENCE_TEXT_HELPERS = new WeakHashMap<>();
    private static final WeakHashMap<Preference, PreferenceIconHelper> PREFERENCE_ICON_HELPERS = new WeakHashMap<>();
    private static final WeakHashMap<DialogPreference, DialogPreferenceIconHelper> PREFERENCE_DIALOG_ICON_HELPERS = new WeakHashMap<>();
    private static final WeakHashMap<Preference, OnPreferenceLongClickListener> PREFERENCE_LONG_CLICK_LISTENERS = new WeakHashMap<>();

    public static final HashMap<Class<? extends Preference>, Integer> PREFERENCE_DEF_STYLE_ATTRS = new HashMap<>();

    static {
        final HashMap<Class<? extends Preference>, Integer> defStyleAttrs = PREFERENCE_DEF_STYLE_ATTRS;
        defStyleAttrs.put(Preference.class, R.attr.preferenceStyle);
        defStyleAttrs.put(android.support.v7.preference.EditTextPreference.class, R.attr.editTextPreferenceStyle);
        defStyleAttrs.put(net.xpece.android.support.preference.EditTextPreference.class, R.attr.editTextPreferenceStyle);
    }

    private XpPreference() {}

    @RestrictTo(RestrictTo.Scope.GROUP_ID)
    static void onCreatePreference(final Preference preference, final AttributeSet attrs) {
        final int defStyleAttr = getDefStyleAttr(preference);

        {
            final PreferenceIconHelper iconHelper = new PreferenceIconHelper(preference);
            iconHelper.loadFromAttributes(attrs, defStyleAttr, 0);
            PREFERENCE_ICON_HELPERS.put(preference, iconHelper);
        }

        if (preference instanceof DialogPreference) {
            DialogPreference dialogPreference = (DialogPreference) preference;
            final DialogPreferenceIconHelper iconHelper = new DialogPreferenceIconHelper(dialogPreference);
            iconHelper.loadFromAttributes(attrs, defStyleAttr, 0);
            PREFERENCE_DIALOG_ICON_HELPERS.put(dialogPreference, iconHelper);
        }

        {
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
    static void onBindViewHolder(final Preference preference, final PreferenceViewHolder holder) {
        final PreferenceTextHelper textHelper = PREFERENCE_TEXT_HELPERS.get(preference);
        if (textHelper != null) {
            textHelper.onBindViewHolder(holder);
        }

        if (PREFERENCE_LONG_CLICK_LISTENERS.containsKey(preference)) {
            final OnPreferenceLongClickListener longClickListener = PREFERENCE_LONG_CLICK_LISTENERS.get(preference);
            if (longClickListener != null) {
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return longClickListener.onLongClick(preference, v);
                    }
                });
            } else {
                holder.itemView.setOnLongClickListener(null);
            }
        }
    }

    public static void setTitleTextColor(Preference preference, ColorStateList titleTextColor) {
        PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
        if (pth == null) {
            pth = new PreferenceTextHelper();
            PREFERENCE_TEXT_HELPERS.put(preference, pth);
        }
        pth.setTitleTextColor(titleTextColor);
        preference.notifyChanged();
    }

    public static void setTitleTextColor(Preference preference, @ColorInt int titleTextColor) {
        PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
        if (pth == null) {
            pth = new PreferenceTextHelper();
            PREFERENCE_TEXT_HELPERS.put(preference, pth);
        }
        pth.setTitleTextColor(titleTextColor);
        preference.notifyChanged();
    }

    public static void setTitleTextAppearance(Preference preference, int titleTextAppearance) {
        PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
        if (pth == null) {
            pth = new PreferenceTextHelper();
            PREFERENCE_TEXT_HELPERS.put(preference, pth);
        }
        pth.setTitleTextAppearance(titleTextAppearance);
        preference.notifyChanged();
    }

    public static void setSummaryTextColor(Preference preference, ColorStateList summaryTextColor) {
        PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
        if (pth == null) {
            pth = new PreferenceTextHelper();
            PREFERENCE_TEXT_HELPERS.put(preference, pth);
        }
        pth.setSummaryTextColor(summaryTextColor);
        preference.notifyChanged();
    }

    public static void setSummaryTextColor(Preference preference, @ColorInt int summaryTextColor) {
        PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
        if (pth == null) {
            pth = new PreferenceTextHelper();
            PREFERENCE_TEXT_HELPERS.put(preference, pth);
        }
        pth.setSummaryTextColor(summaryTextColor);
        preference.notifyChanged();
    }

    public static void setSummaryTextAppearance(Preference preference, int summaryTextAppearance) {
        PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
        if (pth == null) {
            pth = new PreferenceTextHelper();
            PREFERENCE_TEXT_HELPERS.put(preference, pth);
        }
        pth.setSummaryTextAppearance(summaryTextAppearance);
        preference.notifyChanged();
    }

    public static boolean hasTitleTextColor(Preference preference) {
        final PreferenceTextHelper mPreferenceTextHelper = PREFERENCE_TEXT_HELPERS.get(preference);
        if (mPreferenceTextHelper != null) {
            return mPreferenceTextHelper.hasTitleTextColor();
        }
        return false;
    }

    public static boolean hasSummaryTextColor(Preference preference) {
        final PreferenceTextHelper mPreferenceTextHelper = PREFERENCE_TEXT_HELPERS.get(preference);
        if (mPreferenceTextHelper != null) {
            return mPreferenceTextHelper.hasSummaryTextColor();
        }
        return false;
    }

    public static boolean hasTitleTextAppearance(Preference preference) {
        final PreferenceTextHelper mPreferenceTextHelper = PREFERENCE_TEXT_HELPERS.get(preference);
        if (mPreferenceTextHelper != null) {
            return mPreferenceTextHelper.hasTitleTextAppearance();
        }
        return false;
    }

    public static boolean hasSummaryTextAppearance(Preference preference) {
        final PreferenceTextHelper mPreferenceTextHelper = PREFERENCE_TEXT_HELPERS.get(preference);
        if (mPreferenceTextHelper != null) {
            return mPreferenceTextHelper.hasSummaryTextAppearance();
        }
        return false;
    }

    public static void setIcon(final Preference preference, final Drawable icon) {
        PreferenceIconHelper iconHelper = PREFERENCE_ICON_HELPERS.get(preference);
        if (iconHelper == null) {
            iconHelper = new PreferenceIconHelper(preference);
            PREFERENCE_ICON_HELPERS.put(preference, iconHelper);
        }
        iconHelper.setIcon(icon);
    }

    public static void setIcon(final Preference preference, @DrawableRes final int icon) {
        PreferenceIconHelper iconHelper = PREFERENCE_ICON_HELPERS.get(preference);
        if (iconHelper == null) {
            iconHelper = new PreferenceIconHelper(preference);
            PREFERENCE_ICON_HELPERS.put(preference, iconHelper);
        }
        iconHelper.setIcon(icon);
    }

    public static Drawable getIcon(final Preference preference) {
        final PreferenceIconHelper iconHelper = PREFERENCE_ICON_HELPERS.get(preference);
        if (iconHelper != null) {
            return iconHelper.getIcon();
        }
        return preference.getIcon();
    }

    public static void setDialogIcon(final DialogPreference preference, final Drawable icon) {
        DialogPreferenceIconHelper iconHelper = PREFERENCE_DIALOG_ICON_HELPERS.get(preference);
        if (iconHelper == null) {
            iconHelper = new DialogPreferenceIconHelper(preference);
            PREFERENCE_DIALOG_ICON_HELPERS.put(preference, iconHelper);
        }
        iconHelper.setIcon(icon);
    }

    public static void setDialogIcon(final DialogPreference preference, @DrawableRes final int icon) {
        DialogPreferenceIconHelper iconHelper = PREFERENCE_DIALOG_ICON_HELPERS.get(preference);
        if (iconHelper == null) {
            iconHelper = new DialogPreferenceIconHelper(preference);
            PREFERENCE_DIALOG_ICON_HELPERS.put(preference, iconHelper);
        }
        iconHelper.setIcon(icon);
    }

    public static Drawable getDialogIcon(final DialogPreference preference) {
        final DialogPreferenceIconHelper iconHelper = PREFERENCE_DIALOG_ICON_HELPERS.get(preference);
        if (iconHelper != null) {
            return iconHelper.getIcon();
        }
        return preference.getDialogIcon();
    }

    public static void setOnPreferenceLongClickListener(final Preference preference, final OnPreferenceLongClickListener listener) {
        final OnPreferenceLongClickListener oldListener = PREFERENCE_LONG_CLICK_LISTENERS.get(preference);
        if (listener != oldListener) {
            PREFERENCE_LONG_CLICK_LISTENERS.put(preference, listener);
            preference.notifyChanged();
        }
    }

    public static boolean hasOnPreferenceLongClickListener(final Preference preference) {
        final OnPreferenceLongClickListener listener = PREFERENCE_LONG_CLICK_LISTENERS.get(preference);
        return listener != null;
    }
}
