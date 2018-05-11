package net.xpece.android.support.preference;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Eugen on 6. 12. 2015.
 */
public class XpMultiSelectListPreferenceDialogFragment extends XpPreferenceDialogFragment {
    private static final String TAG = XpMultiSelectListPreferenceDialogFragment.class.getSimpleName();

    final HashSet<String> mNewValues = new HashSet<>();

    boolean mPreferenceChanged;
    boolean[] mSelectedItems = new boolean[0];
    private boolean mRestoredState = false;

    public static XpMultiSelectListPreferenceDialogFragment newInstance(String key) {
        XpMultiSelectListPreferenceDialogFragment fragment = new XpMultiSelectListPreferenceDialogFragment();
        Bundle b = new Bundle(1);
        b.putString("key", key);
        fragment.setArguments(b);
        return fragment;
    }

    public XpMultiSelectListPreferenceDialogFragment() {
    }

    public MultiSelectListPreference getMultiSelectListPreference() {
        return (MultiSelectListPreference) getPreference();
    }

    @Override
    protected void onPrepareDialogBuilder(final AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        MultiSelectListPreference preference = this.getMultiSelectListPreference();

        final CharSequence[] entries = preference.getEntries();
        final CharSequence[] entryValues = preference.getEntryValues();
        if (entries == null || entryValues == null) {
            throw new IllegalStateException(
                "MultiSelectListPreference requires an entries array and " +
                    "an entryValues array.");
        }

        setupSelectedItems(preference);
        builder.setMultiChoiceItems(entries, mSelectedItems,
            new DialogInterface.OnMultiChoiceClickListener() {
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    mSelectedItems[which] = isChecked;
                    if (isChecked) {
                        mPreferenceChanged |= mNewValues.add(entryValues[which].toString());
                    } else {
                        mPreferenceChanged |= mNewValues.remove(entryValues[which].toString());
                    }
                }
            });

        setupInitialValues(preference);
    }

    private void setupSelectedItems(final MultiSelectListPreference preference) {
        if (!mRestoredState) {
            mSelectedItems = preference.getSelectedItems();
        }
    }

    private void setupInitialValues(final MultiSelectListPreference preference) {
        if (!mRestoredState) {
            mNewValues.clear();
            mNewValues.addAll(preference.getValues());
        }
    }

    @Override
    public void onDialogClosed(final boolean positiveResult) {
        MultiSelectListPreference preference = this.getMultiSelectListPreference();
        if (positiveResult && mPreferenceChanged) {
            final Set<String> values = mNewValues;
            if (preference.callChangeListener(values)) {
                preference.setValues(values);
            }
        }
        mPreferenceChanged = false;
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(TAG + ".mNewValues", mNewValues);
        outState.putBooleanArray(TAG + ".mSelectedItems", mSelectedItems);
        outState.putBoolean(TAG + ".mPreferenceChanged", mPreferenceChanged);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mNewValues.clear();
            mNewValues.addAll((Set<String>) savedInstanceState.getSerializable(TAG + ".mNewValues"));
            mSelectedItems = savedInstanceState.getBooleanArray(TAG + ".mSelectedItems");
            mPreferenceChanged = savedInstanceState.getBoolean(TAG + ".mPreferenceChanged");
            mRestoredState = true;
        }
    }
}
