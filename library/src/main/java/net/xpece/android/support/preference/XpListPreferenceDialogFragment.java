package net.xpece.android.support.preference;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceDialogFragmentCompat;

/**
 * @author Eugen on 28. 12. 2015.
 */
public class XpListPreferenceDialogFragment extends PreferenceDialogFragmentCompat {
    private int mClickedDialogEntryIndex;

    public XpListPreferenceDialogFragment() {
    }

    public static XpListPreferenceDialogFragment newInstance(String key) {
        XpListPreferenceDialogFragment fragment = new XpListPreferenceDialogFragment();
        Bundle b = new Bundle(1);
        b.putString("key", key);
        fragment.setArguments(b);
        return fragment;
    }

    private ListPreference getListPreference() {
        return (ListPreference) this.getPreference();
    }

    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        ListPreference preference = this.getListPreference();
        if (preference.getEntries() != null && preference.getEntryValues() != null) {
            this.mClickedDialogEntryIndex = preference.findIndexOfValue(preference.getValue());
            builder.setSingleChoiceItems(preference.getEntries(), this.mClickedDialogEntryIndex, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    XpListPreferenceDialogFragment.this.mClickedDialogEntryIndex = which;
                    XpListPreferenceDialogFragment.this.onClick(dialog, -1);
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(null, null);
        } else {
            throw new IllegalStateException("ListPreference requires an entries array and an entryValues array.");
        }
    }

    public void onDialogClosed(boolean positiveResult) {
        ListPreference preference = this.getListPreference();
        if (positiveResult && this.mClickedDialogEntryIndex >= 0 && preference.getEntryValues() != null) {
            String value = preference.getEntryValues()[this.mClickedDialogEntryIndex].toString();
            if (preference.callChangeListener(value)) {
                preference.setValue(value);
            }
        }

    }
}
