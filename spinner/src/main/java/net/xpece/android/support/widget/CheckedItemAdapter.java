package net.xpece.android.support.widget;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.xpece.android.support.widget.spinner.R;

/**
 * Created by Eugen on 07.04.2016.
 */
public class CheckedItemAdapter extends ArrayAdapter<CharSequence> implements ThemedSpinnerAdapter {

    private final ThemedSpinnerAdapter.Helper mDropDownHelper;

    private final int mFieldId;
    private int mDropDownResource;

    private int mSelection = -1;

    public CheckedItemAdapter(Context context, int resource, int textViewResourceId,
                              CharSequence[] objects) {
        super(context, resource, textViewResourceId, objects);

        mDropDownHelper = new Helper(context);

        mFieldId = textViewResourceId;
        mDropDownResource = resource;
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
//        if (position == mSelection) {
//            int bgId = Util.resolveResourceId(view.getContext(), R.attr.colorControlHighlight, 0);
//            view.setBackgroundResource(bgId);
//        } else {
//            view.setBackgroundResource(0);
//        }
        return view;
    }

    @Override
    public View getDropDownView(final int position, final View convertView, final ViewGroup parent) {
        LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
        View view = createViewFromResource(inflater, position, convertView, parent, mDropDownResource);
        if (position == mSelection) {
            int bgId = Util.resolveResourceId(view.getContext(), R.attr.colorControlHighlight, 0);
            view.setBackgroundResource(bgId);
        } else {
            view.setBackgroundResource(0);
        }
        return view;
    }

    @Override
    public Resources.Theme getDropDownViewTheme() {
        return mDropDownHelper.getDropDownViewTheme();
    }

    @Override
    public void setDropDownViewTheme(final Resources.Theme theme) {
        mDropDownHelper.setDropDownViewTheme(theme);
    }

    @Override
    public void setDropDownViewResource(@LayoutRes final int resource) {
        super.setDropDownViewResource(resource);
        mDropDownResource = resource;
    }

    private View createViewFromResource(LayoutInflater inflater, int position, View convertView,
                                        ViewGroup parent, int resource) {
        View view;
        TextView text;

        if (convertView == null) {
            view = inflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }

        try {
            if (mFieldId == 0) {
                //  If no custom field is assigned, assume the whole resource is a TextView
                text = (TextView) view;
            } else {
                //  Otherwise, find the TextView field within the layout
                text = (TextView) view.findViewById(mFieldId);
            }
        } catch (ClassCastException e) {
            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                "ArrayAdapter requires the resource ID to be a TextView", e);
        }

        CharSequence item = getItem(position);
        text.setText(item);

        return view;
    }

}
