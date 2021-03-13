package net.xpece.android.support.preference;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;

import net.xpece.android.support.preference.plugins.XpSupportPreferencePlugins;

import java.lang.reflect.Field;

public abstract class XpPreferenceFragment extends PreferenceFragmentCompat {
    private static final String TAG = XpPreferenceFragment.class.getSimpleName();

    public static final String DIALOG_FRAGMENT_TAG = "android.support.v7.preference.PreferenceFragment.DIALOG";

    private static final Field FIELD_PREFERENCE_MANAGER;

    static {
        Field f = null;
        try {
            f = PreferenceFragmentCompat.class.getDeclaredField("mPreferenceManager");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            XpSupportPreferencePlugins.onError(e, "mPreferenceManager not available.");
        }
        FIELD_PREFERENCE_MANAGER = f;
    }

    /**
     * For tracking whether {@link #getContext()} should return the real thing
     * or styled {@link PreferenceManager#getContext()}.
     * <p>
     * AndroidX Preference 1.1.0 injects the preference theme overlay into its activity.
     * We support the case of themed application context with retained fragments.
     * This allows us to achieve that.
     */
    private boolean mCreatingViews = false;

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
    }

    /**
     * If you use retained fragment you won't have to re-inflate the preference hierarchy
     * with each orientation change. On the other hand your original activity context will leak.
     * <p>
     * Use this method to provide your own themed long-lived context.
     *
     * @return Your own base styled context or {@code null} to use standard activity context.
     * @deprecated The provided Context will be ignored. Don't use retained fragments.
     */
    @Deprecated
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

        //noinspection deprecation
        if (getRetainInstance()) {
            printActivityLeakWarning();
        }

        // Setup custom Preference Manager
        final Context styledContext = getStyledContext();
        manager = new XpPreferenceManager(styledContext, getCustomDefaultPackages());
        setPreferenceManager(manager);
        manager.setOnNavigateToScreenListener(this);
    }

    public abstract void onCreatePreferences2(@Nullable final Bundle savedInstanceState, @Nullable final String rootKey);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mCreatingViews = true;
        try {
            return super.onCreateView(inflater, container, savedInstanceState);
        } finally {
            mCreatingViews = false;
        }
    }

    @Override
    public void addPreferencesFromResource(int preferencesResId) {
        mCreatingViews = true;
        try {
            super.addPreferencesFromResource(preferencesResId);
        } finally {
            mCreatingViews = false;
        }
    }

    @Override
    public void setPreferencesFromResource(int preferencesResId, @Nullable String key) {
        mCreatingViews = true;
        try {
            super.setPreferencesFromResource(preferencesResId, key);
        } finally {
            mCreatingViews = false;
        }
    }

    @Nullable
    @Override
    public Context getContext() {
        if (mCreatingViews) {
            return getStyledContext();
        } else {
            return super.getContext();
        }
    }

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
