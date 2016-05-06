package net.xpece.android.support.widget;

import android.content.Context;

import net.xpece.android.support.widget.spinner.R;

import java.util.Arrays;

/**
 * Created by Eugen on 07.04.2016.
 */
@Deprecated
public class CheckedItemAdapter extends CheckedTypedItemAdapter<CharSequence> {
    public static CheckedItemAdapter newInstance(Context context, CharSequence[] objects, int selection) {
        CheckedItemAdapter a = new CheckedItemAdapter(context, android.R.layout.simple_spinner_item, android.R.id.text1, objects);
        a.setDropDownViewResource(R.layout.asp_simple_spinner_dropdown_item);
        a.setSelection(selection);
        return a;
    }

    public CheckedItemAdapter(Context context, int resource, int textViewResourceId, CharSequence[] objects) {
        super(context, resource, textViewResourceId, Arrays.asList(objects));
    }
}
