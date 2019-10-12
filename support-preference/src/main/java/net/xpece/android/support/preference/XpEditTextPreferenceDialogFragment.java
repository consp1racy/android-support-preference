//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.xpece.android.support.preference;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;

import static net.xpece.android.support.preference.Util.checkPreferenceNotNull;

public class XpEditTextPreferenceDialogFragment extends XpPreferenceDialogFragment {
    private EditText mEditText;

    public XpEditTextPreferenceDialogFragment() {
    }

    @NonNull
    public static XpEditTextPreferenceDialogFragment newInstance(@NonNull String key) {
        XpEditTextPreferenceDialogFragment fragment = new XpEditTextPreferenceDialogFragment();
        Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @NonNull
    @Override
    protected View onCreateDialogView(@NonNull Context context) {
        View view = super.onCreateDialogView(context);
        context = view.getContext();

        EditText editText = mEditText;
        if (editText == null) {
            editText = view.findViewById(android.R.id.edit);
        }
        if (editText == null) {
            EditTextPreference preference = this.requireEditTextPreference();
            editText = preference.createEditText(context);
        }
        final ViewParent oldParent = editText.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(editText);
            }
            onAddEditTextToDialogView(view, editText);
        }

        return view;
    }

    /**
     * Adds the EditText widget of this preference to the dialog's view.
     *
     * @param dialogView The dialog view.
     */
    private void onAddEditTextToDialogView(@NonNull View dialogView, @NonNull EditText editText) {
        ViewGroup container = dialogView.findViewById(R.id.edittext_container);
        if (container != null) {
            final ViewGroup.LayoutParams lp = editText.getLayoutParams();
            if (lp == null) {
                container.addView(editText, ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            } else {
                container.addView(editText, lp);
            }
        } else {
            throw new IllegalStateException("EditTextPreference dialog layout needs to contain" +
                    " a layout with id @id/edittext_container.");
        }
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        mEditText = view.findViewById(android.R.id.edit);
        mEditText.requestFocus();

        if (mEditText == null) {
            throw new IllegalStateException("Dialog view must contain an EditText with id" +
                    " @android:id/edit");
        }

        mEditText.setText(requireEditTextPreference().getText());
        // Place cursor at the end
        mEditText.setSelection(mEditText.getText().length());
    }

    @Nullable
    public EditTextPreference getEditTextPreference() {
        return (EditTextPreference) this.getPreference();
    }

    @NonNull
    protected EditTextPreference requireEditTextPreference() {
        return checkPreferenceNotNull(getEditTextPreference(), EditTextPreference.class, this);
    }

    protected boolean needInputMethod() {
        return true;
    }

    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String value = this.mEditText.getText().toString();
            EditTextPreference preference = requireEditTextPreference();
            if (preference.callChangeListener(value)) {
                preference.setText(value);
            }
        }

    }
}
