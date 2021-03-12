package net.xpece.android.support.preference.sample;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatDrawableManager;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.TextViewCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;

import net.xpece.android.support.preference.ColorPreference;
import net.xpece.android.support.preference.EditTextPreference;
import net.xpece.android.support.preference.ListPreference;
import net.xpece.android.support.preference.MultiSelectListPreference;
import net.xpece.android.support.preference.OnPreferenceLongClickListener;
import net.xpece.android.support.preference.PreferenceCategory;
import net.xpece.android.support.preference.PreferenceDividerDecoration;
import net.xpece.android.support.preference.PreferenceScreenNavigationStrategy;
import net.xpece.android.support.preference.RingtonePreference;
import net.xpece.android.support.preference.SafeRingtone;
import net.xpece.android.support.preference.SeekBarPreference;
import net.xpece.android.support.preference.StyledContextProvider;
import net.xpece.android.support.preference.TwoStatePreference;
import net.xpece.android.support.preference.XpPreferenceFragment;
import net.xpece.android.support.preference.XpPreferenceHelpers;
import net.xpece.android.support.preference.XpSharedPreferences;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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
        public boolean onPreferenceChange(@NonNull Preference preference, @NonNull Object value) {
            String stringValue = value.toString();

            if (preference instanceof SeekBarPreference) {
                SeekBarPreference pref = (SeekBarPreference) preference;
                int progress = (int) value;
                pref.setInfo(progress + "%");
            } else if (preference instanceof ColorPreference) {
                ColorPreference colorPreference = (ColorPreference) preference;
                int color = (int) value;
//                String colorString = String.format("#%06X", 0xFFFFFF & color);
//                preference.setSummary(colorString);
                int index = colorPreference.findIndexOfValue(color);
                if (index < 0) {
                    preference.setSummary(null);
                } else {
                    final CharSequence name = colorPreference.getNameForColor(color);
                    preference.setSummary(name);
                }
            } else if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else if (preference instanceof MultiSelectListPreference) {
                String summary = stringValue.trim().substring(1, stringValue.length() - 1); // strip []
                preference.setSummary(summary);
            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);
                } else {
                    final Context context = preference.getContext();
                    final Uri selectedUri = Uri.parse(stringValue);
                    final SafeRingtone ringtone = SafeRingtone.obtain(context, selectedUri);
                    try {
                        final String name = ringtone.getTitle();

                        // Set the summary to reflect the new ringtone display name.
                        preference.setSummary(name);
                    } finally {
                        ringtone.stop();
                    }
                }
            } else if (preference instanceof TwoStatePreference) {
                // Fail 50% of the time.
                final boolean success = new Random().nextBoolean();
                if (!success) {
                    Toast.makeText(preference.getContext(), R.string.try_again, Toast.LENGTH_SHORT).show();
                }
                return success;
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    @NonNull
    public static SettingsFragment newInstance(@Nullable String rootKey) {
        Bundle args = new Bundle();
        args.putString(SettingsFragment.ARG_PREFERENCE_ROOT, rootKey);
        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SettingsFragment() {
        setRetainInstance(true);
    }

    @Override
    public String[] getCustomDefaultPackages() {
        return new String[]{getContext().getPackageName()};
    }

    @Nullable
    @Override
    protected ContextThemeWrapper onProvideCustomStyledContext() {
        // Read the javadocs for instructions why and how to use this feature.
        return StyledContextProvider.getThemedApplicationContext(getActivity());
    }

    @Override
    public void onCreatePreferences2(final Bundle savedInstanceState, final String rootKey) {
        // Set an empty screen so getPreferenceScreen doesn't return null -
        // so we can create fake headers from the get-go.
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getPreferenceManager().getContext()));

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        // Manually tint PreferenceScreen icon.
//        Preference subs = findPreference("subscreen");
//        PreferenceIconHelper subsHelper = PreferenceIconHelper.setup(subs,
//            R.drawable.ic_inbox_black_24dp,
//            Util.resolveResourceId(subs.getContext(), R.attr.asp_preferenceIconTint, R.color.accent),
//            true);

        // Add 'notifications' preferences, and a corresponding header.
        PreferenceCategory fakeHeader = new PreferenceCategory(getPreferenceManager().getContext());
        fakeHeader.setTitle(R.string.pref_header_notifications);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_notification);

        // Add 'data and sync' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(getPreferenceManager().getContext());
        fakeHeader.setTitle(R.string.pref_header_data_sync);
        fakeHeader.setTitleTextAppearance(R.style.TextAppearance_AppCompat_Button);
        fakeHeader.setTitleTextColor(ContextCompat.getColor(fakeHeader.getContext(), R.color.primary)); // No disabled color state please.
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_data_sync);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference("example_list"));
        bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        bindPreferenceSummaryToValue(findPreference("notif_color"));

        // Test checked state restoration.
        findPreference("example_checkbox").setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        findPreference("notifications_new_message").setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        findPreference("notifications_new_message_vibrate").setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Setup SeekBarPreference "info" text field.
        final SeekBarPreference volume2 = (SeekBarPreference) findPreference("notifications_new_message_volume2");
        volume2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volume2.setInfo(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Setup EditTextPreference input field.
        ((EditTextPreference) findPreference("example_text")).setOnEditTextCreatedListener(new EditTextPreference.OnEditTextCreatedListener() {
            @Override
            public void onEditTextCreated(@NonNull EditText edit) {
                Context context = edit.getContext();
                //noinspection RestrictedApi
                Drawable d = AppCompatDrawableManager.get().getDrawable(context, R.drawable.ic_create_black_24dp);
                d = DrawableCompat.wrap(d);
                DrawableCompat.setTintList(d, Util.resolveColorStateList(context, R.attr.colorControlNormal));
                TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(edit, null, null, d, null);

                // These are inflated from XML. Undocumented API.
//                edit.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
//                edit.setSingleLine(true);
//                edit.setSelectAllOnFocus(true);
            }
        });

        // Setup an OnPreferenceLongClickListener via XpPreferenceHelpers.
        XpPreferenceHelpers.setOnPreferenceLongClickListener(findPreference("example_text"), new OnLongClickListenerSample());

        // Setup root preference title.
        final PreferenceScreen root = getPreferenceScreen();
        if (TextUtils.isEmpty(root.getTitle())) {
            // Only set title *of the root preference* if it's empty.
            // getActivity().getTitle() doesn't work correctly with synthesized back stack.
//            final FragmentActivity activity = getActivity();
//            final PackageManager pm = activity.getPackageManager();
//            final CharSequence title = pm.getActivityInfo(activity.getComponentName(), 0).loadLabel(pm);
//            final CharSequence title = activity.getApplicationInfo().loadLabel(pm);
            final CharSequence title = getString(R.string.settings_activity_title);
            root.setTitle(title);
        }

        // Setup root preference.
        // Use with ReplaceFragment strategy.
        PreferenceScreenNavigationStrategy.ReplaceFragment.onCreatePreferences(this, rootKey);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Change activity title to preference title. Used with ReplaceFragment strategy.
        getActivity().setTitle(getPreferenceScreen().getTitle());
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
        final String key = preference.getKey();
        if (preference instanceof MultiSelectListPreference) {
            Set<String> summary = XpSharedPreferences.getStringSet(
                    PreferenceManager.getDefaultSharedPreferences(preference.getContext()),
                    key,
                    new HashSet<String>());
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, summary);
        } else if (preference instanceof ColorPreference) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, ((ColorPreference) preference).getColor());
        } else if (preference instanceof SeekBarPreference) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, ((SeekBarPreference) preference).getValue());
        } else {
            String value = PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getString(key, "");
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, value);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final RecyclerView listView = getListView();

        final int padding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        listView.setPadding(0, padding, 0, padding);

        // We're using alternative divider.
        listView.addItemDecoration(new PreferenceDividerDecoration(getContext())
                .drawBetweenItems(false).paddingDp(listView.getContext(), 8));
        setDivider(null);

        // We don't want this. The children are still focusable.
        listView.setFocusable(false);
    }

    /**
     * No chance of outer class leaks when using another static class.
     * Method args give us everything we need anyway.
     */
    static class OnLongClickListenerSample implements OnPreferenceLongClickListener {
        @Override
        public boolean onLongClick(@NonNull Preference preference, @NonNull View view) {
            final Toast toast = Toast.makeText(preference.getContext(), "This showcases long click listeners on preferences.", Toast.LENGTH_SHORT);
            toast.show();
            return true;
        }
    }
}
