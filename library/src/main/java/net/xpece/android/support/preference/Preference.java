package net.xpece.android.support.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.StateSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.xpece.android.support.R;

import java.lang.reflect.Method;
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
 * <p/>
 * <div class="special reference">
 * <h3>Developer Guides</h3>
 * <p>For information about building a settings UI with Preferences,
 * read the <a href="{@docRoot}guide/topics/ui/settings.html">Settings</a>
 * guide.</p>
 * </div>
 *
 * @attr ref android.R.styleable#Preference_icon
 * @attr ref android.R.styleable#Preference_key
 * @attr ref android.R.styleable#Preference_title
 * @attr ref android.R.styleable#Preference_summary
 * @attr ref android.R.styleable#Preference_order
 * @attr ref android.R.styleable#Preference_fragment
 * @attr ref android.R.styleable#Preference_layout
 * @attr ref android.R.styleable#Preference_widgetLayout
 * @attr ref android.R.styleable#Preference_enabled
 * @attr ref android.R.styleable#Preference_selectable
 * @attr ref android.R.styleable#Preference_dependency
 * @attr ref android.R.styleable#Preference_persistent
 * @attr ref android.R.styleable#Preference_defaultValue
 * @attr ref android.R.styleable#Preference_shouldDisableView
 */
public class Preference extends android.preference.Preference {
    private static final Method METHOD_TRY_COMMIT;

    static {
        Method tryCommit = null;
        try {
            tryCommit = PreferenceManager.class.getDeclaredMethod("tryCommit", SharedPreferences.Editor.class);
            tryCommit.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        METHOD_TRY_COMMIT = tryCommit;
    }

    private static void tryCommit(android.preference.Preference preference, SharedPreferences.Editor editor) {
        tryInvoke(METHOD_TRY_COMMIT, preference, editor);
    }

    private static Object tryInvoke(Method method, Object receiver, Object... args) {
        try {
            return method.invoke(receiver, args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * mIconResId is overridden by mIcon, if mIcon is specified.
     */
    private int mIconResId;
    private Drawable mIcon;

    /**
     * @see #setShouldDisableView(boolean)
     */
    private boolean mShouldDisableView = true;

    private int mLayoutResId = R.layout.preference_material;
    private int mWidgetLayoutResId;
    private boolean mCanRecycleLayout = true;

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
            } else if (attr == R.styleable.Preference_android_layout) {
                mLayoutResId = a.getResourceId(attr, mLayoutResId);
            } else if (attr == R.styleable.Preference_android_widgetLayout) {
                mWidgetLayoutResId = a.getResourceId(attr, mWidgetLayoutResId);
            } else if (attr == R.styleable.Preference_android_shouldDisableView) {
                mShouldDisableView = a.getBoolean(attr, mShouldDisableView);
            }
        }
        a.recycle();
    }

    /**
     * Sets the layout resource that is inflated as the {@link View} to be shown
     * for this Preference. In most cases, the default layout is sufficient for
     * custom Preference objects and only the widget layout needs to be changed.
     * <p/>
     * This layout should contain a {@link ViewGroup} with ID
     * {@link android.R.id#widget_frame} to be the parent of the specific widget
     * for this Preference. It should similarly contain
     * {@link android.R.id#title} and {@link android.R.id#summary}.
     *
     * @param layoutResId The layout resource ID to be inflated and returned as
     * a {@link View}.
     * @see #setWidgetLayoutResource(int)
     */
    public void setLayoutResource(int layoutResId) {
        if (layoutResId != mLayoutResId) {
            // Layout changed
            mCanRecycleLayout = false;
        }

        mLayoutResId = layoutResId;
    }

    /**
     * Gets the layout resource that will be shown as the {@link View} for this Preference.
     *
     * @return The layout resource ID.
     */
    public int getLayoutResource() {
        return mLayoutResId;
    }

    /**
     * Sets the layout for the controllable widget portion of this Preference. This
     * is inflated into the main layout. For example, a {@link android.preference.CheckBoxPreference}
     * would specify a custom layout (consisting of just the CheckBox) here,
     * instead of creating its own main layout.
     *
     * @param widgetLayoutResId The layout resource ID to be inflated into the
     * main layout.
     * @see #setLayoutResource(int)
     */
    public void setWidgetLayoutResource(int widgetLayoutResId) {
        if (widgetLayoutResId != mWidgetLayoutResId) {
            // Layout changed
            mCanRecycleLayout = false;
        }
        mWidgetLayoutResId = widgetLayoutResId;
    }

    /**
     * Gets the layout resource for the controllable widget portion of this Preference.
     *
     * @return The layout resource ID.
     */
    public int getWidgetLayoutResource() {
        return mWidgetLayoutResId;
    }

    /**
     * Creates the View to be shown for this Preference in the
     * {@link PreferenceActivity}. The default behavior is to inflate the main
     * layout of this Preference (see {@link #setLayoutResource(int)}. If
     * changing this behavior, please specify a {@link ViewGroup} with ID
     * {@link android.R.id#widget_frame}.
     * <p/>
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

        final View layout = layoutInflater.inflate(mLayoutResId, parent, false);

        final ViewGroup widgetFrame = (ViewGroup) layout
            .findViewById(R.id.widget_frame);
        if (widgetFrame != null) {
            if (mWidgetLayoutResId != 0) {
                layoutInflater.inflate(mWidgetLayoutResId, widgetFrame);
            } else {
                widgetFrame.setVisibility(View.GONE);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            layout.setBackgroundDrawable(createActivatedBackground(layout));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            layout.setBackground(createActivatedBackground(layout));
        }
        return layout;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static Drawable createActivatedBackground(View layout) {
        Context context = layout.getContext();
        StateListDrawable d = new StateListDrawable();
        int activated = Util.resolveColor(context, R.attr.colorControlActivated);
        d.addState(new int[]{android.R.attr.state_activated}, new ColorDrawable(activated));
        d.addState(StateSet.WILD_CARD, new ColorDrawable(0));
        return null;
    }

    /**
     * Binds the created View to the data for this Preference.
     * <p/>
     * This is a good place to grab references to custom Views in the layout and
     * set properties on them.
     * <p/>
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

        final TextView summaryView = (TextView) view.findViewById(R.id.summary);
        if (summaryView != null) {
            final CharSequence summary = getSummary();
            if (!TextUtils.isEmpty(summary)) {
                summaryView.setText(summary);
                summaryView.setVisibility(View.VISIBLE);
            } else {
                summaryView.setVisibility(View.GONE);
            }
        }

        final ImageView imageView = (ImageView) view.findViewById(R.id.icon);
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

        final View imageFrame = view.findViewById(R.id.icon_frame);
        if (imageFrame != null) {
            imageFrame.setVisibility(mIcon != null ? View.VISIBLE : View.GONE);
        }

        if (mShouldDisableView) {
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
            mIcon = icon;

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
     * @hide
     */
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    private void tryCommit(SharedPreferences.Editor editor) {
        tryCommit(this, editor);
    }

    /**
     * Attempts to persist a set of Strings to the {@link android.content.SharedPreferences}.
     * <p/>
     * This will check if this Preference is persistent, get an editor from
     * the {@link PreferenceManager}, put in the strings, and check if we should commit (and
     * commit if so).
     *
     * @param values The values to persist.
     * @return True if the Preference is persistent. (This is not whether the
     * value was persisted, since we may not necessarily commit if there
     * will be a batch commit later.)
     * @hide Pending API approval
     * @see #getPersistedStringSet(Set)
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected boolean persistStringSet(Set<String> values) {
        if (shouldPersist()) {
            // Shouldn't store null
            if (values.equals(getPersistedStringSet(null))) {
                // It's already there, so the same as persisting
                return true;
            }

            SharedPreferences.Editor editor = PreferenceManagerCompat.getEditor(getPreferenceManager());
            editor.putStringSet(getKey(), values);
            tryCommit(editor);
            return true;
        }
        return false;
    }

    /**
     * Attempts to get a persisted set of Strings from the
     * {@link android.content.SharedPreferences}.
     * <p/>
     * This will check if this Preference is persistent, get the SharedPreferences
     * from the {@link PreferenceManager}, and get the value.
     *
     * @param defaultReturnValue The default value to return if either the
     * Preference is not persistent or the Preference is not in the
     * shared preferences.
     * @return The value from the SharedPreferences or the default return
     * value.
     * @hide Pending API approval
     * @see #persistStringSet(Set)
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected Set<String> getPersistedStringSet(Set<String> defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }

        return getPreferenceManager().getSharedPreferences().getStringSet(getKey(), defaultReturnValue);
    }

    public boolean canRecycleLayout() {
        return mCanRecycleLayout;
    }

}
