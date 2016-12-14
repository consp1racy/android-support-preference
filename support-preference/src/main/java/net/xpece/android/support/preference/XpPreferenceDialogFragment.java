package net.xpece.android.support.preference;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.RestrictTo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by Eugen on 26.03.2016.
 */
public abstract class XpPreferenceDialogFragment extends PreferenceDialogFragmentCompat {

    @Override
    protected View onCreateDialogView(Context context) {
        Context context2 = new AlertDialog.Builder(context).getContext();
//        Context context2 = new AlertDialog.Builder(context, getTheme()).getContext();
        return super.onCreateDialogView(context2);
    }

    /**
     * Copied from {@link AppCompatDialogFragment}.
     *
     * @hide
     */
    @SuppressWarnings("RestrictedApi")
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void setupDialog(Dialog dialog, int style) {
        if (dialog instanceof AppCompatDialog) {
            // If the dialog is an AppCompatDialog, we'll handle it
            AppCompatDialog acd = (AppCompatDialog) dialog;
            switch (style) {
                case STYLE_NO_INPUT:
                    dialog.getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    // fall through...
                case STYLE_NO_FRAME:
                case STYLE_NO_TITLE:
                    acd.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
            }
        } else {
            // Else, just let super handle it
            super.setupDialog(dialog, style);
        }
    }
}
