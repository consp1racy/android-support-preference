package net.xpece.android.support.preference.sample;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.XpPreferenceFragment;
import android.support.v7.widget.PreferenceDividerDecoration;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import net.xpece.android.support.preference.ListPreference;
import net.xpece.android.support.preference.MultiSelectListPreference;
import net.xpece.android.support.preference.PreferenceIconHelper;
import net.xpece.android.support.preference.RingtonePreference;
import net.xpece.android.support.preference.SharedPreferencesCompat;

import java.util.HashSet;
import java.util.Stack;

/**
 * @author Eugen on 7. 12. 2015.
 */
public class SettingsFragment extends XpPreferenceFragment implements ICanPressBack {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    // These are used to navigate back and forth between subscreens.
    private PreferenceScreen mRootPreferenceScreen;
    private Stack<String> mPreferenceScreenStack;

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                    index >= 0
                        ? listPreference.getEntries()[index]
                        : null);
            } else if (preference instanceof RingtonePreference) {
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
    public void onCreatePreferences2(final Bundle savedInstanceState, final String rootKey) {
        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        // Manually tint PreferenceScreen icon.
        Preference subs = findPreference("subs_screen");
        PreferenceIconHelper subsHelper = new PreferenceIconHelper(subs);
        subsHelper.setIconPaddingEnabled(true);
        subsHelper.setIcon(R.drawable.abc_ic_menu_selectall_mtrl_alpha);
        subsHelper.setTintList(ContextCompat.getColorStateList(getPreferenceManager().getContext(), R.color.accent));
        subsHelper.setIconTintEnabled(true);

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

        mRootPreferenceScreen = getPreferenceScreen();
        mRootPreferenceScreen.setKey("root"); // Non-null key!
        mRootPreferenceScreen.setTitle(getActivity().getTitle()); // Store default title.

        if (savedInstanceState == null) {
            mPreferenceScreenStack = new Stack<>();
            mPreferenceScreenStack.push(mRootPreferenceScreen.getKey()); // Store root key.
        } else {
            //noinspection unchecked
            mPreferenceScreenStack = (Stack<String>) savedInstanceState.getSerializable(TAG + ".mPreferenceScreenStack");
            if (mPreferenceScreenStack.size() > 1) {
                // We're deeper than root preference screen. Load appropriate screen.
                String key = mPreferenceScreenStack.peek(); // Get screen key.
                PreferenceScreen preference = (PreferenceScreen) findPreference(key);
                navigateToPreferenceScreen(preference);
            }
        }
    }

    public void navigateToPreferenceScreen(PreferenceScreen preference) {
        setPreferenceScreen(preference);
        getActivity().setTitle(preference.getTitle());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(TAG + ".mPreferenceScreenStack", mPreferenceScreenStack);
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
        if (preference instanceof MultiSelectListPreference) {
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

    @Override
    public void onRecyclerViewCreated(RecyclerView list) {
        list.addItemDecoration(new PreferenceDividerDecoration(getContext()).drawBottom(true));
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof PreferenceScreen) {
            mPreferenceScreenStack.push(preference.getKey()); // Store new screen key.
            navigateToPreferenceScreen((PreferenceScreen) preference);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onBackPressed() {
        if (mPreferenceScreenStack.size() > 1) {
            mPreferenceScreenStack.pop(); // Pop the screen we're leaving.
            String key = mPreferenceScreenStack.peek(); // Lookup new screen key.
            PreferenceScreen preference = (PreferenceScreen) mRootPreferenceScreen.findPreference(key);
            navigateToPreferenceScreen(preference);
            return true;
        }
        return false;
    }
}
