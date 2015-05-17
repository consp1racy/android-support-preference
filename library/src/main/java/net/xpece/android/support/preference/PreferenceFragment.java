package net.xpece.android.support.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import net.xpece.android.support.R;

/**
 * Created by Eugen on 13. 5. 2015.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PreferenceFragment extends android.preference.PreferenceFragment {

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = view.getContext();

        ListView list = (ListView) view.findViewById(android.R.id.list);
        list.setPadding(0, 0, 0, 0);
        list.setSelector(Util.resolveDrawable(context, R.attr.selectableItemBackground));
        list.setCacheColorHint(0);
    }
}
