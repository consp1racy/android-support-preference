package android.support.v7.preference;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;

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

    public static final String DIALOG_FRAGMENT_TAG = "android.support.v7.preference.PreferenceFragment.DIALOG";

    private static final Field FIELD_PREFERENCE_MANAGER;

    static {
        Field preferenceManager = null;
        try {
            preferenceManager = PreferenceFragmentCompat.class.getDeclaredField("mPreferenceManager");
            preferenceManager.setAccessible(true);
        } catch (NoSuchFieldException e) {
            XpSupportPreferencePlugins.onError(e, "mPreferenceManager not available.");
        }
        FIELD_PREFERENCE_MANAGER = preferenceManager;
    }

    private void setPreferenceManager(@NonNull final PreferenceManager manager) {
        try {
            FIELD_PREFERENCE_MANAGER.set(this, manager);
        } catch (IllegalAccessException e) {
            // This should never happen.
            throw new IllegalStateException(e);
        }
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

        // Setup custom Preference Manager
        manager = new XpPreferenceManager(getStyledContext(), getCustomDefaultPackages());
        setPreferenceManager(manager);
        manager.setOnNavigateToScreenListener(this);
    }

    @Nullable
    public String[] getCustomDefaultPackages() {
        return null;
    }

    public abstract void onCreatePreferences2(@Nullable final Bundle savedInstanceState, @Nullable final String rootKey);

    @NonNull
    private Context getStyledContext() {
        return getPreferenceManager().getContext();
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
