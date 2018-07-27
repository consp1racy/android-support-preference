package android.support.v7.preference;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.View;

import net.xpece.android.support.preference.ColorableTextPreference;
import net.xpece.android.support.preference.CustomDialogIconPreference;
import net.xpece.android.support.preference.CustomIconPreference;
import net.xpece.android.support.preference.DialogPreferenceIconHelper;
import net.xpece.android.support.preference.OnPreferenceLongClickListener;
import net.xpece.android.support.preference.PreferenceIconHelper;
import net.xpece.android.support.preference.PreferenceTextHelper;
import net.xpece.android.support.preference.R;

import java.util.WeakHashMap;

/**
 * @deprecated Use public API at {@link net.xpece.android.support.preference.XpPreferenceHelpers}.
 */
@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed")
public final class XpPreferenceHelpers {

    private static final WeakHashMap<Preference, PreferenceTextHelper> PREFERENCE_TEXT_HELPERS = new WeakHashMap<>();
    private static final WeakHashMap<Preference, PreferenceIconHelper> PREFERENCE_ICON_HELPERS = new WeakHashMap<>();
    private static final WeakHashMap<DialogPreference, DialogPreferenceIconHelper> PREFERENCE_DIALOG_ICON_HELPERS = new WeakHashMap<>();
    private static final WeakHashMap<Preference, OnPreferenceLongClickListener> PREFERENCE_LONG_CLICK_LISTENERS = new WeakHashMap<>();

    private XpPreferenceHelpers() {}

    static void onCreatePreference(final @NonNull Preference preference, @Nullable final AttributeSet attrs) {
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

    private static int getDefStyleAttr(final @NonNull Preference preference) {
        final int defStyleAttr;
        if (preference instanceof PreferenceScreen) {
            defStyleAttr = net.xpece.android.support.preference.R.attr.preferenceScreenStyle;
        } else if (preference instanceof PreferenceCategory) {
            defStyleAttr = net.xpece.android.support.preference.R.attr.preferenceCategoryStyle;
        } else if (preference instanceof PreferenceGroup) {
            defStyleAttr = 0;
        } else {
            defStyleAttr = R.attr.preferenceStyle;
        }
        return defStyleAttr;
    }

    static void onBindViewHolder(final @NonNull Preference preference, final @NonNull PreferenceViewHolder holder) {
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
                    public boolean onLongClick(@NonNull View v) {
                        return longClickListener.onLongClick(preference, v);
                    }
                });
            } else {
                holder.itemView.setOnLongClickListener(null);
            }
            holder.itemView.setLongClickable(hasLongClickListener && preference.isSelectable());
        }
    }

    public static void setTitleTextColor(@NonNull Preference preference, @NonNull ColorStateList titleTextColor) {
        if (preference instanceof ColorableTextPreference) {
            ((ColorableTextPreference) preference).setTitleTextColor(titleTextColor);
            preference.notifyChanged();
        } else {
            PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
            if (pth == null) {
                pth = new PreferenceTextHelper();
                PREFERENCE_TEXT_HELPERS.put(preference, pth);
            }
            pth.setTitleTextColor(titleTextColor);
            preference.notifyChanged();
        }
    }

    public static void setTitleTextColor(@NonNull Preference preference, @ColorInt int titleTextColor) {
        if (preference instanceof ColorableTextPreference) {
            ((ColorableTextPreference) preference).setTitleTextColor(titleTextColor);
            preference.notifyChanged();
        } else {
            PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
            if (pth == null) {
                pth = new PreferenceTextHelper();
                PREFERENCE_TEXT_HELPERS.put(preference, pth);
            }
            pth.setTitleTextColor(titleTextColor);
            preference.notifyChanged();
        }
    }

    public static void setTitleTextAppearance(@NonNull Preference preference, @StyleRes int titleTextAppearance) {
        if (preference instanceof ColorableTextPreference) {
            ((ColorableTextPreference) preference).setTitleTextAppearance(titleTextAppearance);
            preference.notifyChanged();
        } else {
            PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
            if (pth == null) {
                pth = new PreferenceTextHelper();
                PREFERENCE_TEXT_HELPERS.put(preference, pth);
            }
            pth.setTitleTextAppearance(titleTextAppearance);
            preference.notifyChanged();
        }
    }

    public static void setSummaryTextColor(@NonNull Preference preference, @NonNull ColorStateList summaryTextColor) {
        if (preference instanceof ColorableTextPreference) {
            ((ColorableTextPreference) preference).setSummaryTextColor(summaryTextColor);
            preference.notifyChanged();
        } else {
            PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
            if (pth == null) {
                pth = new PreferenceTextHelper();
                PREFERENCE_TEXT_HELPERS.put(preference, pth);
            }
            pth.setSummaryTextColor(summaryTextColor);
            preference.notifyChanged();
        }
    }

    public static void setSummaryTextColor(@NonNull Preference preference, @ColorInt int summaryTextColor) {
        if (preference instanceof ColorableTextPreference) {
            ((ColorableTextPreference) preference).setSummaryTextColor(summaryTextColor);
            preference.notifyChanged();
        } else {
            PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
            if (pth == null) {
                pth = new PreferenceTextHelper();
                PREFERENCE_TEXT_HELPERS.put(preference, pth);
            }
            pth.setSummaryTextColor(summaryTextColor);
            preference.notifyChanged();
        }
    }

    public static void setSummaryTextAppearance(@NonNull Preference preference, @StyleRes int summaryTextAppearance) {
        if (preference instanceof ColorableTextPreference) {
            ((ColorableTextPreference) preference).setSummaryTextAppearance(summaryTextAppearance);
            preference.notifyChanged();
        } else {
            PreferenceTextHelper pth = PREFERENCE_TEXT_HELPERS.get(preference);
            if (pth == null) {
                pth = new PreferenceTextHelper();
                PREFERENCE_TEXT_HELPERS.put(preference, pth);
            }
            pth.setSummaryTextAppearance(summaryTextAppearance);
            preference.notifyChanged();
        }
    }

    public static boolean hasTitleTextColor(@NonNull Preference preference) {
        if (preference instanceof ColorableTextPreference) {
            return ((ColorableTextPreference) preference).hasTitleTextColor();
        }
        final PreferenceTextHelper mPreferenceTextHelper = PREFERENCE_TEXT_HELPERS.get(preference);
        if (mPreferenceTextHelper != null) {
            return mPreferenceTextHelper.hasTitleTextColor();
        }
        return false;
    }

    public static boolean hasSummaryTextColor(@NonNull Preference preference) {
        if (preference instanceof ColorableTextPreference) {
            return ((ColorableTextPreference) preference).hasSummaryTextColor();
        }
        final PreferenceTextHelper mPreferenceTextHelper = PREFERENCE_TEXT_HELPERS.get(preference);
        if (mPreferenceTextHelper != null) {
            return mPreferenceTextHelper.hasSummaryTextColor();
        }
        return false;
    }

    public static boolean hasTitleTextAppearance(@NonNull Preference preference) {
        if (preference instanceof ColorableTextPreference) {
            return ((ColorableTextPreference) preference).hasTitleTextAppearance();
        }
        final PreferenceTextHelper mPreferenceTextHelper = PREFERENCE_TEXT_HELPERS.get(preference);
        if (mPreferenceTextHelper != null) {
            return mPreferenceTextHelper.hasTitleTextAppearance();
        }
        return false;
    }

    public static boolean hasSummaryTextAppearance(@NonNull Preference preference) {
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
    public static void setSupportIcon(final @NonNull Preference preference, @Nullable final Drawable icon) {
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
    public static void setSupportIcon(final @NonNull Preference preference, @DrawableRes final int icon) {
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
    public static Drawable getSupportIcon(final @NonNull Preference preference) {
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
    public static void setSupportDialogIcon(final @NonNull DialogPreference preference, @Nullable final Drawable icon) {
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
    public static void setSupportDialogIcon(final @NonNull DialogPreference preference, @DrawableRes final int icon) {
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
    public static Drawable getSupportDialogIcon(final @NonNull DialogPreference preference) {
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
    public static void setIcon(final @NonNull Preference preference, @Nullable final Drawable icon) {
        setSupportIcon(preference, icon);
    }

    @SuppressWarnings("deprecation")
    public static void setIcon(final @NonNull Preference preference, @DrawableRes final int icon) {
        setSupportIcon(preference, icon);
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public static Drawable getIcon(final @NonNull Preference preference) {
        return getSupportIcon(preference);
    }

    @SuppressWarnings("deprecation")
    public static void setDialogIcon(final @NonNull DialogPreference preference, @Nullable final Drawable icon) {
        setSupportDialogIcon(preference, icon);
    }

    @SuppressWarnings("deprecation")
    public static void setDialogIcon(final @NonNull DialogPreference preference, @DrawableRes final int icon) {
        setSupportDialogIcon(preference, icon);
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public static Drawable getDialogIcon(final @NonNull DialogPreference preference) {
        return getSupportDialogIcon(preference);
    }

    public static void setOnPreferenceLongClickListener(final @NonNull Preference preference, @Nullable final OnPreferenceLongClickListener listener) {
        final OnPreferenceLongClickListener oldListener = PREFERENCE_LONG_CLICK_LISTENERS.get(preference);
        if (listener != oldListener) {
            PREFERENCE_LONG_CLICK_LISTENERS.put(preference, listener);
            preference.notifyChanged();
        }
    }

    public static boolean hasOnPreferenceLongClickListener(final @NonNull Preference preference) {
        final OnPreferenceLongClickListener listener = PREFERENCE_LONG_CLICK_LISTENERS.get(preference);
        return listener != null;
    }
}
