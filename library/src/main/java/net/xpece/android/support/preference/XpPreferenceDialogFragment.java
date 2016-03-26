package net.xpece.android.support.preference;

import android.content.Context;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.ContextThemeWrapper;
import android.view.View;

/**
 * Created by Eugen on 26.03.2016.
 */
public abstract class XpPreferenceDialogFragment extends PreferenceDialogFragmentCompat {
    @Override
    protected View onCreateDialogView(Context context) {
        int alertDialogTheme = Util.resolveResourceId(context, R.attr.alertDialogTheme, 0);
        context = new ContextThemeWrapper(context, alertDialogTheme);
        return super.onCreateDialogView(context);
    }
}
