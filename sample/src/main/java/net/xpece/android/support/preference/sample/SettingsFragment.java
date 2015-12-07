package net.xpece.android.support.preference.sample;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.XpPreferenceFragment;
import android.text.TextUtils;

import net.xpece.android.support.preference.PreferenceIconHelper;
import net.xpece.android.support.preference.SharedPreferencesCompat;
import net.xpece.android.support.preference.XpListPreference;
import net.xpece.android.support.preference.XpMultiSelectListPreference;
import net.xpece.android.support.preference.XpRingtonePreference;

import java.util.HashSet;

/**
 * @author Eugen on 7. 12. 2015.
 */
public class SettingsFragment extends XpPreferenceFragment {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof XpListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                XpListPreference listPreference = (XpListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                    index >= 0
                        ? listPreference.getEntries()[index]
                        : null);
            } else if (preference instanceof XpRingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                        preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    public static SettingsFragment newInstance() {
        Bundle args = new Bundle();
        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreatePreferences2(final Bundle bundle, final String s) {

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        // Manually tint PreferenceScreen icon.
        Preference subs = findPreference("subs_screen");
        PreferenceIconHelper subsHelper = PreferenceIconHelper.wrap(subs);
        subsHelper.setIconPaddingEnabled(true);
        subsHelper.setTintList(ContextCompat.getColorStateList(getPreferenceManager().getContext(), R.color.accent));
        subs.setIcon(R.drawable.ic_volume_up_black_24dp);

        // Add 'notifications' preferences, and a corresponding header.
        PreferenceCategory fakeHeader = new PreferenceCategory(getPreferenceManager().getContext());
        fakeHeader.setTitle(R.string.pref_header_notifications);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_notification);

        // Add 'data and sync' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(getPreferenceManager().getContext());
        fakeHeader.setTitle(R.string.pref_header_data_sync);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_data_sync);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference("example_text"));
        bindPreferenceSummaryToValue(findPreference("example_list"));
        bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        bindPreferenceSummaryToValue(findPreference("sync_frequency"));
    }

    @Override
    public boolean onPreferenceTreeClick(final Preference preference) {
        if ("notifications_new_message_ringtone".equals(preference.getKey())) {
            ((XpRingtonePreference)preference).startActivityForResult(this, 555, false);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == 555) {
            ((XpRingtonePreference)findPreference("notifications_new_message_ringtone")).onActivityResult(data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        if (preference instanceof XpMultiSelectListPreference) {
            String summary = SharedPreferencesCompat.getStringSet(
                PreferenceManager.getDefaultSharedPreferences(preference.getContext()),
                preference.getKey(),
                new HashSet<String>())
                .toString();
            summary = summary.trim().substring(1, summary.length() - 1); // strip []
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, summary);
        } else {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getString(preference.getKey(), ""));
        }
    }
}
