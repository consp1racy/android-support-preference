package net.xpece.android.support.preference;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;

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
}
