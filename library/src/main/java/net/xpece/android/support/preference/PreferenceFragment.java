package net.xpece.android.support.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

/**
 * Created by Eugen on 13. 5. 2015.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class PreferenceFragment extends android.preference.PreferenceFragment {

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = view.getContext();

        ListView list = (ListView) view.findViewById(android.R.id.list);
        list.setPadding(0, 0, 0, 0);
        list.setSelector(Util.resolveDrawable(context, R.attr.selectableItemBackground));
        list.setCacheColorHint(0);
    }

    /**
     * Adds preferences from activities that match the given {@link Intent}.
     *
     * @param intent The {@link Intent} to query activities.
     */
    public void addPreferencesFromIntent(Intent intent) {
        requirePreferenceManager();

        setPreferenceScreen(PreferenceManagerCompat.inflateFromIntent(getPreferenceManager(), getActivity(), intent, getPreferenceScreen()));
    }

    /**
     * Inflates the given XML resource and adds the preference hierarchy to the current
     * preference hierarchy.
     *
     * @param preferencesResId The XML resource ID to inflate.
     */
    public void addPreferencesFromResource(int preferencesResId) {
        requirePreferenceManager();

        setPreferenceScreen(PreferenceManagerCompat.inflateFromResource(getPreferenceManager(), getActivity(), preferencesResId, getPreferenceScreen()));
    }

    private void requirePreferenceManager() {
        if (getPreferenceManager() == null) {
            throw new RuntimeException("This should be called after super.onCreate.");
        }
    }

}
