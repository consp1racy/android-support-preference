package android.support.v7.preference;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextThemeWrapper;

import net.xpece.android.support.preference.EditTextPreference;
import net.xpece.android.support.preference.ListPreference;
import net.xpece.android.support.preference.MultiSelectListPreference;
import net.xpece.android.support.preference.RingtonePreference;
import net.xpece.android.support.preference.SeekBarDialogPreference;
import net.xpece.android.support.preference.StyledContextProvider;
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

    /**
     * Read and apply the {@link R.attr#preferenceTheme} overlay on top of supplied context.
     */
    @NonNull
    private Context resolveStyledContext(@NonNull final Context context) {
        final int theme = StyledContextProvider.resolveResourceId(context, R.attr.preferenceTheme);
        if (theme == 0) {
            throw new IllegalStateException("Must specify preferenceTheme in theme");
        }
        return new ContextThemeWrapper(context, theme);
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

    private void printActivityLeakWarning() {
        Log.w(TAG, "When using setRetainInstance(true) your Activity instance will leak on configuration change.");
        Log.w(TAG, "Override onProvideCustomStyledContext() and provide a custom long-lived context.");
        Log.w(TAG, "You can use methods in " + StyledContextProvider.class + " class.");
    }

    /**
     * If you use retained fragment you won't have to re-inflate the preference hierarchy
     * with each orientation change. On the other hand your original activity context will leak.
     * <p>
     * Use this method to provide your own themed long-lived context.
     *
     * @return Your own base styled context or {@code null} to use standard activity context.
     * @see StyledContextProvider#getThemedApplicationContext(Activity)
     * @see StyledContextProvider#getActivityThemeResource(Activity)
     */
    @Nullable
    protected ContextThemeWrapper onProvideCustomStyledContext() {
        return null;
    }

    @NonNull
    private Context getStyledContext() {
        return getPreferenceManager().getContext();
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

        final Context styledContext;
        final ContextThemeWrapper customStyledContext = onProvideCustomStyledContext();
        if (customStyledContext != null) {
            styledContext = resolveStyledContext(customStyledContext);
            setStyledContext(styledContext);
        } else {
            if (getRetainInstance()) {
                printActivityLeakWarning();
            }
            styledContext = getStyledContext();
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
