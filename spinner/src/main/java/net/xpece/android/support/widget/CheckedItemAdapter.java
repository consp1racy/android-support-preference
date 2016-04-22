package net.xpece.android.support.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import net.xpece.android.support.widget.spinner.R;

/**
 * Created by Eugen on 07.04.2016.
 */
public class CheckedItemAdapter extends ArrayAdapter<CharSequence> {
    private int mSelection = -1;

    public CheckedItemAdapter(Context context, int resource, int textViewResourceId,
                              CharSequence[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public void setSelection(int selection) {
        mSelection = selection;
        notifyDataSetChanged();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        if (position == mSelection) {
            int bgId = Util.resolveResourceId(view.getContext(), R.attr.colorControlHighlight, 0);
            view.setBackgroundResource(bgId);
        } else {
            view.setBackgroundResource(0);
        }
        return view;
    }
}
