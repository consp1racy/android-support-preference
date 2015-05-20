package net.xpece.android.support.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

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
public class Preference extends android.preference.Preference {

    /**
     * mIconResId is overridden by mIcon, if mIcon is specified.
     */
    private int mIconResId;
    private Drawable mIcon;

    private ColorStateList mTintList = null;
    private PorterDuff.Mode mTintMode = PorterDuff.Mode.SRC_IN;
    private boolean mTintIcon = false;
    private boolean mIconPaddingEnabled = false;

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
     * @param context The Context this is associated with, through which it can
     * access the current theme, resources,
     * {@link SharedPreferences}, etc.
     * @param attrs The attributes of the XML tag that is inflating the
     * preference.
     * @param defStyleAttr An attribute in the current theme that contains a
     * reference to a style resource that supplies default values for
     * the view. Can be 0 to not look for defaults.
     * @param defStyleRes A resource identifier of a style resource that
     * supplies default values for the view, used only if
     * defStyleAttr is 0 or can not be found in the theme. Can be 0
     * to not look for defaults.
     * @see #Preference(Context, AttributeSet)
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Preference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
     * @param context The Context this is associated with, through which it can
     * access the current theme, resources,
     * {@link SharedPreferences}, etc.
     * @param attrs The attributes of the XML tag that is inflating the
     * preference.
     * @param defStyleAttr An attribute in the current theme that contains a
     * reference to a style resource that supplies default values for
     * the view. Can be 0 to not look for defaults.
     * @see #Preference(Context, AttributeSet)
     */
    public Preference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, R.style.Preference_Material);
    }

    /**
     * Constructor that is called when inflating a Preference from XML. This is
     * called when a Preference is being constructed from an XML file, supplying
     * attributes that were specified in the XML file. This version uses a
     * default style of 0, so the only attribute values applied are those in the
     * Context's Theme and the given AttributeSet.
     *
     * @param context The Context this is associated with, through which it can
     * access the current theme, resources, {@link SharedPreferences},
     * etc.
     * @param attrs The attributes of the XML tag that is inflating the
     * preference.
     * @see #Preference(Context, AttributeSet, int)
     */
    public Preference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.preferenceStyle);
    }

    /**
     * Constructor to create a Preference.
     *
     * @param context The Context in which to store Preference values.
     */
    public Preference(Context context) {
        this(context, null);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Preference, defStyleAttr, defStyleRes);
        for (int i = a.getIndexCount() - 1; i >= 0; i--) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.Preference_android_icon) {
                mIconResId = a.getResourceId(attr, 0);
                setIcon(mIconResId);
            } else if (attr == R.styleable.Preference_asp_tint) {
                mTintList = a.getColorStateList(attr);
            } else if (attr == R.styleable.Preference_asp_tintMode) {
                mTintMode = PorterDuff.Mode.values()[a.getInt(attr, 0)];
            } else if (attr == R.styleable.Preference_asp_tintIcon) {
                mTintIcon = a.getBoolean(attr, false);
            } else if (attr == R.styleable.Preference_asp_iconPaddingEnabled) {
                mIconPaddingEnabled = a.getBoolean(attr, false);
            }
        }
        a.recycle();

        if (getClass().getName().startsWith(BuildConfig.APPLICATION_ID)) {
            // We can recycle the shit out of these!
            // This also fixed no Switch and CheckBox animation issue on Lollipop.
            PreferenceCompat.setCanRecycleLayout(this, true);
        }

        if (Build.VERSION.SDK_INT >= 11) {
            super.setIcon(null); // Let's free that reference, we manage the icon here.
        }
    }

    public PorterDuff.Mode getTintMode() {
        return mTintMode;
    }

    public void setTintMode(PorterDuff.Mode tintMode) {
        mTintMode = tintMode;

        int iconRes = mIconResId;
        setIcon(null);
        setIcon(iconRes);
    }

    public ColorStateList getTintList() {
        return mTintList;
    }

    public void setTintList(ColorStateList tintList) {
        mTintList = tintList;

        int iconRes = mIconResId;
        setIcon(null);
        setIcon(iconRes);
    }

    /**
     * Creates the View to be shown for this Preference in the
     * {@link PreferenceActivity}. The default behavior is to inflate the main
     * layout of this Preference (see {@link #setLayoutResource(int)}. If
     * changing this behavior, please specify a {@link ViewGroup} with ID
     * {@link android.R.id#widget_frame}.
     * <p></p>
     * Make sure to call through to the superclass's implementation.
     *
     * @param parent The parent that this View will eventually be attached to.
     * @return The View that displays this Preference.
     * @see #onBindView(View)
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressWarnings("deprecation")
    protected View onCreateView(ViewGroup parent) {
        final LayoutInflater layoutInflater =
            (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View layout = layoutInflater.inflate(getLayoutResource(), parent, false);

        final ViewGroup widgetFrame = (ViewGroup) layout.findViewById(android.R.id.widget_frame);
        if (widgetFrame != null) {
            if (getWidgetLayoutResource() != 0) {
                layoutInflater.inflate(getWidgetLayoutResource(), widgetFrame);
            } else {
                widgetFrame.setVisibility(View.GONE);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            layout.setBackground(Util.createActivatedBackground(layout));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            layout.setBackgroundDrawable(Util.createActivatedBackground(layout));
        }
        return layout;
    }

    /**
     * Binds the created View to the data for this Preference.
     * <p></p>
     * This is a good place to grab references to custom Views in the layout and
     * set properties on them.
     * <p></p>
     * Make sure to call through to the superclass's implementation.
     *
     * @param view The View that shows this Preference.
     * @see #onCreateView(ViewGroup)
     */
    protected void onBindView(@NonNull View view) {
        final TextView titleView = (TextView) view.findViewById(android.R.id.title);
        if (titleView != null) {
            final CharSequence title = getTitle();
            if (!TextUtils.isEmpty(title)) {
                titleView.setText(title);
                titleView.setVisibility(View.VISIBLE);
            } else {
                titleView.setVisibility(View.GONE);
            }
        }

        final TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        if (summaryView != null) {
            final CharSequence summary = getSummary();
            if (!TextUtils.isEmpty(summary)) {
                summaryView.setText(summary);
                summaryView.setVisibility(View.VISIBLE);
            } else {
                summaryView.setVisibility(View.GONE);
            }
        }

        final ImageView imageView = (ImageView) view.findViewById(android.R.id.icon);
        if (imageView != null) {
            if (mIconResId != 0 || mIcon != null) {
                if (mIcon == null) {
                    mIcon = ContextCompat.getDrawable(getContext(), mIconResId);
                }
                if (mIcon != null) {
                    imageView.setImageDrawable(mIcon);
                }
            }
            imageView.setVisibility(mIcon != null ? View.VISIBLE : View.GONE);
        }

//        final View imageFrame = view.findViewById(R.id.icon_frame);
//        if (imageFrame != null) {
//            imageFrame.setVisibility(mIcon != null ? View.VISIBLE : View.GONE);
//        }

        if (getShouldDisableView()) {
            setEnabledStateOnViews(view, isEnabled());
        }
    }

    /**
     * Makes sure the view (and any children) get the enabled state changed.
     */
    private void setEnabledStateOnViews(View v, boolean enabled) {
        v.setEnabled(enabled);

        if (v instanceof ViewGroup) {
            final ViewGroup vg = (ViewGroup) v;
            for (int i = vg.getChildCount() - 1; i >= 0; i--) {
                setEnabledStateOnViews(vg.getChildAt(i), enabled);
            }
        }
    }

    /**
     * Sets the icon for this Preference with a Drawable.
     * This icon will be placed into the ID
     * {@link android.R.id#icon} within the View created by
     * {@link #onCreateView(ViewGroup)}.
     *
     * @param icon The optional icon for this Preference.
     */
    public void setIcon(Drawable icon) {
        if ((icon == null && mIcon != null) || (icon != null && mIcon != icon)) {

            if (mIconPaddingEnabled) {
                if (icon != null) {
                    int padding = Util.dpToPxOffset(getContext(), 4);
                    icon = Util.addDrawablePadding(icon, padding);
                }
            }

            mIcon = icon;

            if (mTintIcon) {
                if (mIcon != null && mTintList != null && mTintMode != null) {
                    mIcon = DrawableCompat.wrap(mIcon).mutate();
                    DrawableCompat.setTintList(mIcon, mTintList);
                    DrawableCompat.setTintMode(mIcon, mTintMode);
                }
            }

            notifyChanged();
        }
    }

    /**
     * Sets the icon for this Preference with a resource ID.
     *
     * @param iconResId The icon as a resource ID.
     * @see #setIcon(Drawable)
     */
    public void setIcon(int iconResId) {
        mIconResId = iconResId;
        setIcon(ContextCompat.getDrawable(getContext(), iconResId));
    }

    /**
     * Returns the icon of this Preference.
     *
     * @return The icon.
     * @see #setIcon(Drawable)
     */
    public Drawable getIcon() {
        return mIcon;
    }

    /**
     * Allows a Preference to intercept key events without having focus.
     * For example, SeekBarPreference uses this to intercept +/- to adjust
     * the progress.
     *
     * @return True if the Preference handled the key. Returns false by default.
     */
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    /**
     * Attempts to persist a set of Strings to the {@link android.content.SharedPreferences}.
     * <p></p>
     * This will check if this Preference is persistent, get an editor from
     * the {@link PreferenceManager}, put in the strings, and check if we should commit (and
     * commit if so).
     *
     * @param values The values to persist.
     * @return True if the Preference is persistent. (This is not whether the
     * value was persisted, since we may not necessarily commit if there
     * will be a batch commit later.)
     * @see #getPersistedStringSet2(Set)
     */
    protected boolean persistStringSet2(Set<String> values) {
        if (shouldPersist()) {
            // Shouldn't store null
            if (values.equals(getPersistedStringSet2(null))) {
                // It's already there, so the same as persisting
                return true;
            }

            SharedPreferences.Editor editor = getEditor();
            SharedPreferencesCompat.putStringSet(editor, getKey(), values);
            tryCommit(editor);
            return true;
        }
        return false;
    }

    /**
     * Attempts to get a persisted set of Strings from the
     * {@link android.content.SharedPreferences}.
     * <p></p>
     * This will check if this Preference is persistent, get the SharedPreferences
     * from the {@link PreferenceManager}, and get the value.
     *
     * @param defaultReturnValue The default value to return if either the
     * Preference is not persistent or the Preference is not in the
     * shared preferences.
     * @return The value from the SharedPreferences or the default return
     * value.
     * @see #persistStringSet2(Set)
     */
    protected Set<String> getPersistedStringSet2(Set<String> defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }
        return SharedPreferencesCompat.getStringSet(getSharedPreferences(), getKey(), defaultReturnValue);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void tryCommit(SharedPreferences.Editor editor) {
        if (shouldCommit()) {
            SharedPreferencesCompat.apply(editor);
        }
    }

}
