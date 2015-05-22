package net.xpece.android.support.preference;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.lang.reflect.Method;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p></p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public abstract class AppCompatPreferenceActivity extends PreferenceActivity
    implements AppCompatCallback,
    GenericInflater.Factory<android.preference.Preference> {
    private static final String TAG = AppCompatPreferenceActivity.class.getSimpleName();

    private static final Method METHOD_REQUIRE_PREFERENCE_MANAGER;

    static {
        Method requirePreferenceManager = null;
        try {
            requirePreferenceManager = PreferenceActivity.class.getDeclaredMethod("requirePreferenceManager");
            requirePreferenceManager.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        METHOD_REQUIRE_PREFERENCE_MANAGER = requirePreferenceManager;
    }

    private AppCompatDelegate mAppCompatDelegate;

    private AppCompatDelegate getDelegate() {
        if (mAppCompatDelegate == null) {
            mAppCompatDelegate = AppCompatDelegate.create(this, this);
        }
        return mAppCompatDelegate;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);

        ListView list = (ListView) findViewById(android.R.id.list);
        list.setPadding(0, 0, 0, 0);
        list.setSelector(Util.resolveDrawable(this, R.attr.selectableItemBackground));
        list.setCacheColorHint(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    @Override
    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().setContentView(view, params);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().addContentView(view, params);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getDelegate().setTitle(title);
    }

    /**
     * Enable extended support library window features.
     * <p>
     * This is a convenience for calling
     * {@link android.view.Window#requestFeature getWindow().requestFeature()}.
     * </p>
     *
     * @param featureId The desired feature as defined in
     * {@link android.view.Window}.
     * @return Returns true if the requested feature is supported and now enabled.
     * @see android.app.Activity#requestWindowFeature
     * @see android.view.Window#requestFeature
     */
    public boolean supportRequestWindowFeature(int featureId) {
        return getDelegate().requestWindowFeature(featureId);
    }

    public void invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

    /**
     * Notifies the Activity that a support action mode has been started.
     * Activity subclasses overriding this method should call the superclass implementation.
     *
     * @param mode The new action mode.
     */
    public void onSupportActionModeStarted(ActionMode mode) {
    }

    /**
     * Notifies the activity that a support action mode has finished.
     * Activity subclasses overriding this method should call the superclass implementation.
     *
     * @param mode The action mode that just finished.
     */
    public void onSupportActionModeFinished(ActionMode mode) {
    }

    public ActionMode startSupportActionMode(ActionMode.Callback callback) {
        return getDelegate().startSupportActionMode(callback);
    }

    /**
     * Support library version of {@link android.app.Activity#getActionBar}.
     * <p></p>
     * <p>Retrieve a reference to this activity's ActionBar.
     *
     * @return The Activity's ActionBar, or null if it does not have one.
     */
    @Nullable
    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }

    /**
     * Set a {@link android.widget.Toolbar Toolbar} to act as the {@link android.app.ActionBar} for this
     * Activity window.
     * <p></p>
     * <p>When set to a non-null value the {@link #getActionBar()} method will return
     * an {@link android.app.ActionBar} object that can be used to control the given toolbar as if it were
     * a traditional window decor action bar. The toolbar's menu will be populated with the
     * Activity's options menu and the navigation button will be wired through the standard
     * {@link android.R.id#home home} menu select action.</p>
     * <p></p>
     * <p>In order to use a Toolbar within the Activity's window content the application
     * must not request the window feature {@link android.view.Window#FEATURE_ACTION_BAR FEATURE_ACTION_BAR}.</p>
     *
     * @param toolbar Toolbar to set as the Activity's action bar
     */
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }

    /**
     * Adds preferences from activities that match the given {@link Intent}.
     *
     * @param intent The {@link Intent} to query activities.
     * @deprecated This function is not relevant for a modern fragment-based
     * PreferenceActivity.
     */
    @Deprecated
    public void addPreferencesFromIntent(Intent intent) {
        requirePreferenceManager();

        setPreferenceScreen(PreferenceManagerCompat.inflateFromIntent(getPreferenceManager(), this, intent, getPreferenceScreen(), this));
    }

    /**
     * Inflates the given XML resource and adds the preference hierarchy to the current
     * preference hierarchy.
     *
     * @param preferencesResId The XML resource ID to inflate.
     * @deprecated This function is not relevant for a modern fragment-based
     * PreferenceActivity.
     */
    @Deprecated
    public void addPreferencesFromResource(int preferencesResId) {
        requirePreferenceManager();

        setPreferenceScreen(PreferenceManagerCompat.inflateFromResource(getPreferenceManager(), this, preferencesResId, getPreferenceScreen(), this));
    }

    @Override
    public Preference onCreateItem(String name, Context context, AttributeSet attrs) {
        return null;
    }

    private void requirePreferenceManager() {
        try {
            METHOD_REQUIRE_PREFERENCE_MANAGER.invoke(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
