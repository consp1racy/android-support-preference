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

import java.util.Arrays;
import java.util.List;

/**
 * Created by Eugen on 07.04.2016.
 */
public class CheckedTypedItemAdapter<T> extends ArrayAdapter<T> implements ThemedSpinnerAdapter {

    private final Helper mDropDownHelper;
    private final LayoutInflater mInflater;
    private final int mFieldId;
    private int mDropDownResource;
    private int mResource;

    private int mSelection = -1;

    public static <T> CheckedTypedItemAdapter newInstance(Context context, T[] objects, int selection) {
        return newInstance(context, Arrays.asList(objects), selection);
    }

    public static <T> CheckedTypedItemAdapter newInstance(Context context, List<T> objects, int selection) {
        CheckedTypedItemAdapter a = new CheckedTypedItemAdapter<>(context, android.R.layout.simple_spinner_item, android.R.id.text1, objects);
        a.setDropDownViewResource(net.xpece.android.support.widget.spinner.R.layout.asp_simple_spinner_dropdown_item);
        a.setSelection(selection);
        return a;
    }

    public CheckedTypedItemAdapter(Context context, int resource, int textViewResourceId, List<T> objects) {
        super(context, resource, textViewResourceId, objects);

        mDropDownHelper = new Helper(context);
        mInflater = LayoutInflater.from(context);
        mFieldId = textViewResourceId;
        mResource = mDropDownResource = resource;
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
    public View getDropDownView(final int position, final View convertView, final ViewGroup parent) {
        LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
        View view = createViewFromResource(inflater, convertView, parent, mDropDownResource);
        T item = getItem(position);
        bindDropDownView(view, item);
        if (position == mSelection) {
            int bgId = Util.resolveResourceId(view.getContext(), R.attr.colorControlHighlight, 0);
            view.setBackgroundResource(bgId);
        } else {
            view.setBackgroundResource(0);
        }
        return view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = createViewFromResource(mInflater, convertView, parent, mResource);
        T item = getItem(position);
        bindView(view, item);
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

    protected View createViewFromResource(LayoutInflater inflater, View convertView, ViewGroup parent, int resource) {
        View view;
        if (convertView == null) {
            view = inflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }
        return view;
    }

    protected TextView findTextView(View view) {
        TextView text;
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
        return text;
    }

    public void bindDropDownView(View view, T item) {
        TextView text = findTextView(view);
        final CharSequence value = getItemDropDownText(item);
        text.setText(value);
    }

    public void bindView(View view, T item) {
        TextView text = findTextView(view);
        final CharSequence value = getItemText(item);
        text.setText(value);
    }

    public CharSequence getItemText(T item) {
        return item.toString();
    }

    public CharSequence getItemDropDownText(T item) {
        return getItemText(item);
    }
}
