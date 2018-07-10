package net.xpece.android.support.preference;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;

import java.util.WeakHashMap;

/**
 * Created by Eugen on 27.11.2016.
 */

@SuppressWarnings("DeprecatedIsStillUsed")
public final class XpPreferenceHelpers {

    private static final WeakHashMap<Preference, PreferenceTextHelper> PREFERENCE_TEXT_HELPERS = new WeakHashMap<>();
    private static final WeakHashMap<Preference, PreferenceIconHelper> PREFERENCE_ICON_HELPERS = new WeakHashMap<>();
    private static final WeakHashMap<DialogPreference, DialogPreferenceIconHelper> PREFERENCE_DIALOG_ICON_HELPERS = new WeakHashMap<>();
    private static final WeakHashMap<Preference, OnPreferenceLongClickListener> PREFERENCE_LONG_CLICK_LISTENERS = new WeakHashMap<>();

    private XpPreferenceHelpers() {}

    static void onCreatePreference(final Preference preference, @Nullable final AttributeSet attrs) {
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

    private static int getDefStyleAttr(final Preference preference) {
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

    static void onBindViewHolder(final Preference preference, final PreferenceViewHolder holder) {
        final PreferenceTextHelper textHelper = PREFERENCE_TEXT_HELPERS.get(preference);
        if (textHelper != null) {
            textHelper.onBindViewHolder(holder);
        }

        if (PREFERENCE_LONG_CLICK_LISTENERS.containsKey(preference)) {
            final OnPreferenceLongClickListener longClickListener = PREFERENCE_LONG_CLICK_LISTENERS.get(preference);
            final boolean hasLongClickListener = longClickListener != null;
            if (hasLongClickListener) {
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return longClickListener.onLongClick(preference, v);
                    }
                });
            } else {
                holder.itemView.setOnLongClickListener(null);
            }
            holder.itemView.setLongClickable(hasLongClickListener && preference.isSelectable());
        }
    }

    public static void setTitleTextColor(Preference preference, ColorStateList titleTextColor) {
        if (preference instanceof ColorableTextPreference) {
            ((ColorableTextPreference) preference).setTitleTextColor(titleTextColor);
            XpPreference.notifyChanged(preference);
        } else {
            PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
            if (pth == null) {
                pth = new PreferenceTextHelper();
                PREFERENCE_TEXT_HELPERS.put(preference, pth);
            }
            pth.setTitleTextColor(titleTextColor);
            XpPreference.notifyChanged(preference);
        }
    }

    public static void setTitleTextColor(Preference preference, @ColorInt int titleTextColor) {
        if (preference instanceof ColorableTextPreference) {
            ((ColorableTextPreference) preference).setTitleTextColor(titleTextColor);
            XpPreference.notifyChanged(preference);
        } else {
            PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
            if (pth == null) {
                pth = new PreferenceTextHelper();
                PREFERENCE_TEXT_HELPERS.put(preference, pth);
            }
            pth.setTitleTextColor(titleTextColor);
            XpPreference.notifyChanged(preference);
        }
    }

    public static void setTitleTextAppearance(Preference preference, @StyleRes int titleTextAppearance) {
        if (preference instanceof ColorableTextPreference) {
            ((ColorableTextPreference) preference).setTitleTextAppearance(titleTextAppearance);
            XpPreference.notifyChanged(preference);
        } else {
            PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
            if (pth == null) {
                pth = new PreferenceTextHelper();
                PREFERENCE_TEXT_HELPERS.put(preference, pth);
            }
            pth.setTitleTextAppearance(titleTextAppearance);
            XpPreference.notifyChanged(preference);
        }
    }

    public static void setSummaryTextColor(Preference preference, ColorStateList summaryTextColor) {
        if (preference instanceof ColorableTextPreference) {
            ((ColorableTextPreference) preference).setSummaryTextColor(summaryTextColor);
            XpPreference.notifyChanged(preference);
        } else {
            PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
            if (pth == null) {
                pth = new PreferenceTextHelper();
                PREFERENCE_TEXT_HELPERS.put(preference, pth);
            }
            pth.setSummaryTextColor(summaryTextColor);
            XpPreference.notifyChanged(preference);
        }
    }

    public static void setSummaryTextColor(Preference preference, @ColorInt int summaryTextColor) {
        if (preference instanceof ColorableTextPreference) {
            ((ColorableTextPreference) preference).setSummaryTextColor(summaryTextColor);
            XpPreference.notifyChanged(preference);
        } else {
            PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
            if (pth == null) {
                pth = new PreferenceTextHelper();
                PREFERENCE_TEXT_HELPERS.put(preference, pth);
            }
            pth.setSummaryTextColor(summaryTextColor);
            XpPreference.notifyChanged(preference);
        }
    }

    public static void setSummaryTextAppearance(Preference preference, @StyleRes int summaryTextAppearance) {
        if (preference instanceof ColorableTextPreference) {
            ((ColorableTextPreference) preference).setSummaryTextAppearance(summaryTextAppearance);
            XpPreference.notifyChanged(preference);
        } else {
            PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
            if (pth == null) {
                pth = new PreferenceTextHelper();
                PREFERENCE_TEXT_HELPERS.put(preference, pth);
            }
            pth.setSummaryTextAppearance(summaryTextAppearance);
            XpPreference.notifyChanged(preference);
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

    /**
     * @see #setIcon(Preference, Drawable)
     */
    @Deprecated
    public static void setSupportIcon(final Preference preference, @Nullable final Drawable icon) {
        if (preference instanceof CustomIconPreference) {
            ((CustomIconPreference) preference).setSupportIcon(icon);
        } else {
            PreferenceIconHelper iconHelper = PREFERENCE_ICON_HELPERS.get(preference);
            if (iconHelper == null) {
                iconHelper = new PreferenceIconHelper(preference);
                PREFERENCE_ICON_HELPERS.put(preference, iconHelper);
            }
            iconHelper.setIcon(icon);
        }
    }

    /**
     * @see #setIcon(Preference, int)
     */
    @Deprecated
    public static void setSupportIcon(final Preference preference, @DrawableRes final int icon) {
        if (preference instanceof CustomIconPreference) {
            ((CustomIconPreference) preference).setSupportIcon(icon);
        } else {
            PreferenceIconHelper iconHelper = PREFERENCE_ICON_HELPERS.get(preference);
            if (iconHelper == null) {
                iconHelper = new PreferenceIconHelper(preference);
                PREFERENCE_ICON_HELPERS.put(preference, iconHelper);
            }
            iconHelper.setIcon(icon);
        }
    }

    /**
     * @see #getIcon(Preference)
     */
    @Deprecated
    @Nullable
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

    /**
     * @see #setDialogIcon(DialogPreference, Drawable)
     */
    @Deprecated
    public static void setSupportDialogIcon(final DialogPreference preference, @Nullable final Drawable icon) {
        if (preference instanceof CustomDialogIconPreference) {
            ((CustomIconPreference) preference).setSupportIcon(icon);
        } else {
            DialogPreferenceIconHelper iconHelper = PREFERENCE_DIALOG_ICON_HELPERS.get(preference);
            if (iconHelper == null) {
                iconHelper = new DialogPreferenceIconHelper(preference);
                PREFERENCE_DIALOG_ICON_HELPERS.put(preference, iconHelper);
            }
            iconHelper.setIcon(icon);
        }
    }

    /**
     * @see #setDialogIcon(DialogPreference, int)
     */
    @Deprecated
    public static void setSupportDialogIcon(final DialogPreference preference, @DrawableRes final int icon) {
        if (preference instanceof CustomDialogIconPreference) {
            ((CustomDialogIconPreference) preference).setSupportDialogIcon(icon);
        } else {
            DialogPreferenceIconHelper iconHelper = PREFERENCE_DIALOG_ICON_HELPERS.get(preference);
            if (iconHelper == null) {
                iconHelper = new DialogPreferenceIconHelper(preference);
                PREFERENCE_DIALOG_ICON_HELPERS.put(preference, iconHelper);
            }
            iconHelper.setIcon(icon);
        }
    }

    /**
     * @see #getDialogIcon(DialogPreference)
     */
    @Deprecated
    @Nullable
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

    @SuppressWarnings("deprecation")
    public static void setIcon(final Preference preference, @Nullable final Drawable icon) {
        setSupportIcon(preference, icon);
    }

    @SuppressWarnings("deprecation")
    public static void setIcon(final Preference preference, @DrawableRes final int icon) {
        setSupportIcon(preference, icon);
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public static Drawable getIcon(final Preference preference) {
        return getSupportIcon(preference);
    }

    @SuppressWarnings("deprecation")
    public static void setDialogIcon(final DialogPreference preference, @Nullable final Drawable icon) {
        setSupportDialogIcon(preference, icon);
    }

    @SuppressWarnings("deprecation")
    public static void setDialogIcon(final DialogPreference preference, @DrawableRes final int icon) {
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public static Drawable getDialogIcon(final DialogPreference preference) {
        return getSupportDialogIcon(preference);
    }

    public static void setOnPreferenceLongClickListener(final Preference preference, @Nullable final OnPreferenceLongClickListener listener) {
        final OnPreferenceLongClickListener oldListener = PREFERENCE_LONG_CLICK_LISTENERS.get(preference);
        if (listener != oldListener) {
            PREFERENCE_LONG_CLICK_LISTENERS.put(preference, listener);
            XpPreference.notifyChanged(preference);
        }
    }

    public static boolean hasOnPreferenceLongClickListener(final Preference preference) {
        final OnPreferenceLongClickListener listener = PREFERENCE_LONG_CLICK_LISTENERS.get(preference);
        return listener != null;
    }
}
