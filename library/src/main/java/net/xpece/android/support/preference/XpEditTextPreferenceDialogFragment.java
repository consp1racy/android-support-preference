//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.xpece.android.support.preference;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;

public class XpEditTextPreferenceDialogFragment extends XpPreferenceDialogFragment {
    private EditText mEditText;

    public XpEditTextPreferenceDialogFragment() {
    }

    public static XpEditTextPreferenceDialogFragment newInstance(String key) {
        XpEditTextPreferenceDialogFragment fragment = new XpEditTextPreferenceDialogFragment();
        Bundle b = new Bundle(1);
        b.putString("key", key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected View onCreateDialogView(Context context) {
        View view = super.onCreateDialogView(context);
        context = view.getContext();

        EditText editText = mEditText;
        if (editText == null) {
            editText = (EditText) view.findViewById(android.R.id.edit);
        }
        if (editText == null) {
            EditTextPreference preference = this.getEditTextPreference();
            editText = preference.createEditText(context);
        }
        ViewParent oldParent = editText.getParent();
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
    private void onAddEditTextToDialogView(View dialogView, EditText editText) {
        ViewGroup container = (ViewGroup) dialogView
            .findViewById(R.id.edittext_container);
        if (container != null) {
            container.addView(editText, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        this.mEditText = (EditText) view.findViewById(android.R.id.edit);
        if (this.mEditText == null) {
            throw new IllegalStateException("Dialog view must contain an EditText with id @android:id/edit");
        } else {
            this.mEditText.setText(this.getEditTextPreference().getText());
        }
    }

    private EditTextPreference getEditTextPreference() {
        return (EditTextPreference) this.getPreference();
    }

    protected boolean needInputMethod() {
        return true;
    }

    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String value = this.mEditText.getText().toString();
            EditTextPreference preference = getEditTextPreference();
            if (preference.callChangeListener(value)) {
                preference.setText(value);
            }
        }

    }
}
