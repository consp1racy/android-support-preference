package net.xpece.android.support.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcelable;
import android.preference.PreferenceActivity;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

/**
 * Represents the basic Preference UI building
 * block displayed by a {@link PreferenceActivity} in the form of a
 * {@link ListView}. This class provides the {@link View} to be displayed in
 * the activity and associates with a {@link SharedPreferences} to
 * store/retrieve the preference data.
 * <p>
 * When specifying a preference hierarchy in XML, each element can point to a
 * subclass of {@link Preference}, similar to the view hierarchy and layouts.
 * <p>
 * This class contains a {@code key} that will be used as the key into the
 * {@link SharedPreferences}. It is up to the subclass to decide how to store
 * the value.
 * <p></p>
 * <div class="special reference">
 * <h3>Developer Guides</h3>
 * <p>For information about building a settings UI with Preferences,
 * read the <a href="{@docRoot}guide/topics/ui/settings.html">Settings</a>
 * guide.</p>
 * </div>
 */
public class Preference extends android.support.v7.preference.Preference
        implements TintablePreference, CustomIconPreference, ColorableTextPreference, LongClickablePreference {

    private PreferenceTextHelper mPreferenceTextHelper;
    private PreferenceIconHelper mPreferenceIconHelper;

    @SuppressWarnings("WeakerAccess")
    OnPreferenceLongClickListener mOnPreferenceLongClickListener;

    /**
     * Perform inflation from XML and apply a class-specific base style. This
     * constructor of Preference allows subclasses to use their own base style
     * when they are inflating. For example, a {@link android.preference.CheckBoxPreference}
     * constructor calls this version of the super class constructor and
     * supplies {@code android.R.attr.checkBoxPreferenceStyle} for
     * <var>defStyleAttr</var>. This allows the theme's checkbox preference
     * style to modify all of the base preference attributes as well as the
     * {@link android.preference.CheckBoxPreference} class's attributes.
     *
     * @param context      The Context this is associated with, through which it can
     *                     access the current theme, resources,
     *                     {@link SharedPreferences}, etc.
     * @param attrs        The attributes of the XML tag that is inflating the
     *                     preference.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     *                     the view. Can be 0 to not look for defaults.
     * @param defStyleRes  A resource identifier of a style resource that
     *                     supplies default values for the view, used only if
     *                     defStyleAttr is 0 or can not be found in the theme. Can be 0
     *                     to not look for defaults.
     * @see #Preference(Context, AttributeSet)
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Preference(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Perform inflation from XML and apply a class-specific base style. This
     * constructor of Preference allows subclasses to use their own base style
     * when they are inflating. For example, a {@link android.preference.CheckBoxPreference}
     * constructor calls this version of the super class constructor and
     * supplies {@code android.R.attr.checkBoxPreferenceStyle} for
     * <var>defStyleAttr</var>. This allows the theme's checkbox preference
     * style to modify all of the base preference attributes as well as the
     * {@link android.preference.CheckBoxPreference} class's attributes.
     *
     * @param context      The Context this is associated with, through which it can
     *                     access the current theme, resources,
     *                     {@link SharedPreferences}, etc.
     * @param attrs        The attributes of the XML tag that is inflating the
     *                     preference.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     *                     the view. Can be 0 to not look for defaults.
     * @see #Preference(Context, AttributeSet)
     */
    public Preference(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_Asp_Material);
    }

    /**
     * Constructor that is called when inflating a Preference from XML. This is
     * called when a Preference is being constructed from an XML file, supplying
     * attributes that were specified in the XML file. This version uses a
     * default style of 0, so the only attribute values applied are those in the
     * Context's Theme and the given AttributeSet.
     *
     * @param context The Context this is associated with, through which it can
     *                access the current theme, resources, {@link SharedPreferences},
     *                etc.
     * @param attrs   The attributes of the XML tag that is inflating the
     *                preference.
     * @see #Preference(Context, AttributeSet, int)
     */
    public Preference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.preferenceStyle);
    }

    /**
     * Constructor to create a Preference.
     *
     * @param context The Context in which to store Preference values.
     */
    public Preference(@NonNull Context context) {
        this(context, null);
    }

    private void init(final @NonNull Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr, @StyleRes final int defStyleRes) {
        mPreferenceIconHelper = new PreferenceIconHelper(this);
        mPreferenceIconHelper.loadFromAttributes(attrs, defStyleAttr, defStyleRes);

        mPreferenceTextHelper = new PreferenceTextHelper();
        mPreferenceTextHelper.init(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean isSupportIconPaddingEnabled() {
        return mPreferenceIconHelper.isIconPaddingEnabled();
    }

    @Override
    public void setSupportIconPaddingEnabled(boolean enabled) {
        mPreferenceIconHelper.setIconPaddingEnabled(enabled);
    }

    @Override
    public boolean isSupportIconTintEnabled() {
        return mPreferenceIconHelper.isIconTintEnabled();
    }

    @Override
    public void setSupportIconTintEnabled(boolean enabled) {
        mPreferenceIconHelper.setIconTintEnabled(enabled);
    }

    @Override
    public void setSupportIconTintList(@Nullable final ColorStateList tint) {
        mPreferenceIconHelper.setTintList(tint);
    }

    @Nullable
    @Override
    public ColorStateList getSupportIconTintList() {
        return mPreferenceIconHelper.getTintList();
    }

    @Override
    public void setSupportIconTintMode(@Nullable final PorterDuff.Mode tintMode) {
        mPreferenceIconHelper.setTintMode(tintMode);
    }

    @Nullable
    @Override
    public PorterDuff.Mode getSupportIconTintMode() {
        return mPreferenceIconHelper.getTintMode();
    }

    @Override
    public void setSupportIcon(@Nullable final Drawable icon) {
        mPreferenceIconHelper.setIcon(icon);
    }

    @Override
    public void setSupportIcon(@DrawableRes final int icon) {
        mPreferenceIconHelper.setIcon(icon);
    }

    @Nullable
    @Override
    public Drawable getSupportIcon() {
        return mPreferenceIconHelper.getIcon();
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mPreferenceTextHelper.onBindViewHolder(holder);

        final boolean hasLongClickListener = hasOnPreferenceLongClickListener();
        if (hasLongClickListener) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return mOnPreferenceLongClickListener.onLongClick(Preference.this, v);
                }
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
        }
        holder.itemView.setLongClickable(hasLongClickListener && isSelectable());
    }

    @Override
    public void setTitleTextColor(@NonNull ColorStateList titleTextColor) {
        mPreferenceTextHelper.setTitleTextColor(titleTextColor);
        notifyChanged();
    }

    @Override
    public void setTitleTextColor(@ColorInt int titleTextColor) {
        mPreferenceTextHelper.setTitleTextColor(titleTextColor);
        notifyChanged();
    }

    @Override
    public void setTitleTextAppearance(@StyleRes int titleTextAppearance) {
        mPreferenceTextHelper.setTitleTextAppearance(titleTextAppearance);
        notifyChanged();
    }

    @Override
    public void setSummaryTextColor(@NonNull ColorStateList summaryTextColor) {
        mPreferenceTextHelper.setSummaryTextColor(summaryTextColor);
        notifyChanged();
    }

    @Override
    public void setSummaryTextColor(@ColorInt int summaryTextColor) {
        mPreferenceTextHelper.setSummaryTextColor(summaryTextColor);
        notifyChanged();
    }

    @Override
    public void setSummaryTextAppearance(@StyleRes int summaryTextAppearance) {
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
    public void setOnPreferenceLongClickListener(@Nullable OnPreferenceLongClickListener listener) {
        if (listener != mOnPreferenceLongClickListener) {
            mOnPreferenceLongClickListener = listener;
            notifyChanged();
        }
    }

    @Override
    public boolean hasOnPreferenceLongClickListener() {
        return mOnPreferenceLongClickListener != null;
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(final @NonNull Parcelable state) {
        super.onRestoreInstanceState(state);
    }
}
