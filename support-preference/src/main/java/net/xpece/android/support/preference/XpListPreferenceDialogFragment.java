package net.xpece.android.support.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.widget.SpinnerAdapter;

import net.xpece.android.support.widget.DropDownAdapter;

import static net.xpece.android.support.preference.Util.checkPreferenceNotNull;

/**
 * @author Eugen on 28. 12. 2015.
 */
public class XpListPreferenceDialogFragment extends XpPreferenceDialogFragment {
    int mClickedDialogEntryIndex;

    public XpListPreferenceDialogFragment() {
    }

    @NonNull
    public static XpListPreferenceDialogFragment newInstance(@NonNull String key) {
        XpListPreferenceDialogFragment fragment = new XpListPreferenceDialogFragment();
        Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    public ListPreference getListPreference() {
        return (ListPreference) getPreference();
    }

    @NonNull
    protected ListPreference requireListPreference() {
        return checkPreferenceNotNull(getListPreference(), ListPreference.class, this);
    }

    protected void onPrepareDialogBuilder(@NonNull AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        final ListPreference preference = this.requireListPreference();
        final boolean simple = preference.isSimple();

        if (preference.getEntries() == null || preference.getEntryValues() == null) {
            throw new IllegalStateException("ListPreference requires an entries array and an entryValues array.");
        }

        this.mClickedDialogEntryIndex = preference.findIndexOfValue(preference.getValue());
        final DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            public void onClick(@NonNull DialogInterface dialog, int which) {
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
        final ListPreference preference = this.requireListPreference();
        final int position = this.mClickedDialogEntryIndex;
        if (positiveResult && position >= 0) {
            preference.onItemSelected(position);
        }

    }
}
