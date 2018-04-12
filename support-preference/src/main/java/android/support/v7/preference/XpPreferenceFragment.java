package android.support.v7.preference;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;

import net.xpece.android.support.preference.EditTextPreference;
import net.xpece.android.support.preference.ListPreference;
import net.xpece.android.support.preference.MultiSelectListPreference;
import net.xpece.android.support.preference.RingtonePreference;
import net.xpece.android.support.preference.SeekBarDialogPreference;
import net.xpece.android.support.preference.XpEditTextPreferenceDialogFragment;
import net.xpece.android.support.preference.XpListPreferenceDialogFragment;
import net.xpece.android.support.preference.XpMultiSelectListPreferenceDialogFragment;
import net.xpece.android.support.preference.XpRingtonePreferenceDialogFragment;
import net.xpece.android.support.preference.XpSeekBarPreferenceDialogFragment;
import net.xpece.android.support.preference.plugins.XpSupportPreferencePlugins;

import java.lang.reflect.Field;

/**
 * @author Eugen on 6. 12. 2015.
 */
public abstract class XpPreferenceFragment extends PreferenceFragmentCompat {
    private static final String TAG = XpPreferenceFragment.class.getSimpleName();

    private static final TypedValue TYPED_VALUE = new TypedValue();

    public static final String DIALOG_FRAGMENT_TAG = "android.support.v7.preference.PreferenceFragment.DIALOG";

    private static final Field FIELD_PREFERENCE_MANAGER;
    private static final Field FIELD_STYLED_CONTEXT;

    static {
        Field f = null;
        try {
            f = PreferenceFragmentCompat.class.getDeclaredField("mPreferenceManager");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            XpSupportPreferencePlugins.onError(e, "mPreferenceManager not available.");
        }
        FIELD_PREFERENCE_MANAGER = f;

        try {
            f = PreferenceFragmentCompat.class.getDeclaredField("mStyledContext");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            XpSupportPreferencePlugins.onError(e, "mStyledContext not available.");
        }
        FIELD_STYLED_CONTEXT = f;
    }

    private static void printLeakWarning() {
        Log.w(TAG, "setUseActivityContext(true) and setRetainInstance(true) causes memory leaks.");
    }

    private boolean mUseActivityContext = false;

    /**
     * When use of Activity context is <em>disabled</em> (default) the PreferenceManager context
     * is created like so:
     * <ol>
     * <li>Take Application context (cannot leak)</li>
     * <li>Get Activity theme resource ID from the manifest</li>
     * <li>Wrap the Application context with Activity theme</li>
     * </ol>
     * When use of Activity context is <em>enabled</em> the PreferenceManager context is created
     * like so:
     * <ol>
     * <li>Take Activity context (can leak)</li>
     * </ol>
     * Typically you will never need this option.
     * <p></p>
     * In both cases the themed context is wrapped with {@code preferenceTheme}.
     *
     * @param useActivityContext Whether to use Activity context.
     * @see #setRetainInstance(boolean)
     */
    protected void setUseActivityContext(final boolean useActivityContext) {
        mUseActivityContext = useActivityContext;
        if (mUseActivityContext && getRetainInstance()) {
            printLeakWarning();
        }
    }

    /**
     * Control whether a fragment instance is retained across Activity
     * re-creation (such as from a configuration change).  This can only
     * be used with fragments not in the back stack.  If set, the fragment
     * lifecycle will be slightly different when an activity is recreated:
     * <ul>
     * <li> {@link #onDestroy()} will not be called (but {@link #onDetach()} still
     * will be, because the fragment is being detached from its current activity).
     * <li> {@link #onCreate(Bundle)} will not be called since the fragment
     * is not being re-created.
     * <li> {@link #onAttach(Activity)} and {@link #onActivityCreated(Bundle)} <b>will</b>
     * still be called.
     * </ul>
     * <p></p>
     * Retained fragment with {@link #setUseActivityContext(boolean)} set to {@code true} *will*
     * cause memory leaks.
     *
     * @see #setUseActivityContext(boolean)
     */
    @Override
    public void setRetainInstance(final boolean retain) {
        super.setRetainInstance(retain);
        if (retain && mUseActivityContext) {
            printLeakWarning();
        }
    }

    @NonNull
    private static Context resolveStyledContext(@NonNull final ContextThemeWrapper context) {
        final TypedValue tv = TYPED_VALUE;
        context.getTheme().resolveAttribute(R.attr.preferenceTheme, tv, true);
        final int theme = tv.resourceId;
        if (theme == 0) {
            throw new IllegalStateException("Must specify preferenceTheme in theme");
        }
        return new ContextThemeWrapper(context, theme);
    }

    @NonNull
    private static Context newStyledContext(@NonNull final Activity activity) {
        try {
            final int activityThemeId = activity.getPackageManager()
                    .getActivityInfo(activity.getComponentName(), 0)
                    .getThemeResource();
            final Context app = activity.getApplicationContext();
            final ContextThemeWrapper themedContext = new ContextThemeWrapper(app, activityThemeId);
            return resolveStyledContext(themedContext);
        } catch (PackageManager.NameNotFoundException e) {
            // This should never happen.
            throw new IllegalStateException(e);
        }
    }

    // TODO Should this be public API?
    @NonNull
    private Context getStyledContext() {
        return getPreferenceManager().getContext();
    }

    private void setStyledContext(@NonNull final Context context) {
        try {
            FIELD_STYLED_CONTEXT.set(this, context);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private void setPreferenceManager(@NonNull final PreferenceManager manager) {
        try {
            FIELD_PREFERENCE_MANAGER.set(this, manager);
        } catch (IllegalAccessException e) {
            // This should never happen.
            throw new IllegalStateException(e);
        }
    }

    @Nullable
    public String[] getCustomDefaultPackages() {
        return null;
    }

    @Override
    public final void onCreatePreferences(@Nullable final Bundle bundle, @Nullable final String s) {
        onCreatePreferences1();
        onCreatePreferences2(bundle, s);
    }

    void onCreatePreferences1() {
        // Clear the original Preference Manager
        PreferenceManager manager = getPreferenceManager();
        manager.setOnNavigateToScreenListener(null);

        // Use application context styled with activity theme to avoid memory leaks. Or don't.
        final Context styledContext;
        if (mUseActivityContext) {
            styledContext = getStyledContext();
        } else {
            // noinspection ConstantConditions
            styledContext = newStyledContext(getActivity());
            setStyledContext(styledContext);
        }

        // Setup custom Preference Manager
        manager = new XpPreferenceManager(styledContext, getCustomDefaultPackages());
        setPreferenceManager(manager);
        manager.setOnNavigateToScreenListener(this);
    }

    public abstract void onCreatePreferences2(@Nullable final Bundle savedInstanceState, @Nullable final String rootKey);

    @Override
    public void onDisplayPreferenceDialog(@NonNull final Preference preference) {
        boolean handled = false;

        // This has to be done first. Doubled call in super :(
        if (this.getCallbackFragment() instanceof PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback) {
            handled = ((PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback) this.getCallbackFragment()).onPreferenceDisplayDialog(this, preference);
        }
        if (!handled && this.getActivity() instanceof PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback) {
            handled = ((PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback) this.getActivity()).onPreferenceDisplayDialog(this, preference);
        }

        // Handling custom preferences.
        if (!handled) {
            if (this.getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) == null) {
                DialogFragment f;
                if (preference instanceof EditTextPreference) {
                    f = XpEditTextPreferenceDialogFragment.newInstance(preference.getKey());
                } else if (preference instanceof ListPreference) {
                    f = XpListPreferenceDialogFragment.newInstance(preference.getKey());
                } else if (preference instanceof MultiSelectListPreference) {
                    f = XpMultiSelectListPreferenceDialogFragment.newInstance(preference.getKey());
                } else if (preference instanceof SeekBarDialogPreference) {
                    f = XpSeekBarPreferenceDialogFragment.newInstance(preference.getKey());
                } else if (preference instanceof RingtonePreference) {
                    final RingtonePreference ringtonePreference = (RingtonePreference) preference;
                    final Context context = ringtonePreference.getContext();
                    final boolean canPlayDefault = ringtonePreference.canPlayDefaultRingtone(context);
                    final boolean canShowSelectedTitle = ringtonePreference.canShowSelectedRingtoneTitle(context);
                    if ((!canPlayDefault || !canShowSelectedTitle) &&
                        ringtonePreference.getOnFailedToReadRingtoneListener() != null) {
                        ringtonePreference.getOnFailedToReadRingtoneListener()
                            .onFailedToReadRingtone(ringtonePreference, canPlayDefault, canShowSelectedTitle);
                        return;
                    } else {
                        f = XpRingtonePreferenceDialogFragment.newInstance(preference.getKey());
                    }
                } else {
                    super.onDisplayPreferenceDialog(preference);
                    return;
                }

                f.setTargetFragment(this, 0);
                f.show(this.getFragmentManager(), DIALOG_FRAGMENT_TAG);
            }
        }
    }

    @NonNull
    @Override
    protected RecyclerView.Adapter onCreateAdapter(@NonNull final PreferenceScreen preferenceScreen) {
        return new XpPreferenceGroupAdapter(preferenceScreen);
    }

    @Nullable
    @Override
    public Fragment getCallbackFragment() {
        return this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setPreferenceScreen(null);
    }
}
