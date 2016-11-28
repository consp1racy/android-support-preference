package net.xpece.android.support.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.SpinnerAdapter;

import net.xpece.android.support.widget.DropDownAdapter;

/**
 * @author Eugen on 28. 12. 2015.
 */
public class XpListPreferenceDialogFragment extends XpPreferenceDialogFragment {
    int mClickedDialogEntryIndex;

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
        final ListPreference preference = this.getListPreference();
        final boolean simple = preference.isSimple();

        if (preference.getEntries() == null || preference.getEntryValues() == null) {
            throw new IllegalStateException("ListPreference requires an entries array and an entryValues array.");
        }

        this.mClickedDialogEntryIndex = preference.findIndexOfValue(preference.getValue());
        final DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                XpListPreferenceDialogFragment.this.mClickedDialogEntryIndex = which;
                XpListPreferenceDialogFragment.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);

                if (simple || preference.getPositiveButtonText() == null) {
                    // If there's no OK button, dismiss dialog after making a choice.
                    dialog.dismiss();
                }
            }
        };

        if (simple) {
            final Context context = builder.getContext();
            final SpinnerAdapter adapter = preference.buildSimpleDialogAdapter(context);

            // Convert getDropDownView to getView.
            final DropDownAdapter adapter2 = new DropDownAdapter(adapter, context.getTheme());

            builder.setSingleChoiceItems(adapter2, this.mClickedDialogEntryIndex, onClickListener);

            builder.setPositiveButton(null, null);
            builder.setNegativeButton(null, null);
            builder.setTitle(null);
        } else {
            builder.setSingleChoiceItems(preference.getEntries(), this.mClickedDialogEntryIndex, onClickListener);
        }
    }

    public void onDialogClosed(boolean positiveResult) {
        final ListPreference preference = this.getListPreference();
        final int position = this.mClickedDialogEntryIndex;
        if (positiveResult && position >= 0) {
            preference.onItemSelected(position);
        }

    }
}
