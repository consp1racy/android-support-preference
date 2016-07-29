package net.xpece.android.support.preference.sample;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import net.xpece.android.support.preference.ListPreference;
import net.xpece.android.support.widget.CheckedTypedItemAdapter;

/**
 * @author Eugen on 29.07.2016.
 */

public class FontSizeListPreference extends ListPreference {
    public FontSizeListPreference(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public FontSizeListPreference(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FontSizeListPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public FontSizeListPreference(final Context context) {
        super(context);
    }

    @NonNull
    @Override
    public SpinnerAdapter buildSimpleDialogAdapter(final Context context) {
        return buildAdapter(context, R.layout.asp_select_dialog_item);
    }

    @NonNull
    @Override
    public SpinnerAdapter buildSimpleMenuAdapter(final Context context) {
        return buildAdapter(context, R.layout.asp_simple_spinner_dropdown_item);
    }

    @NonNull
    private SpinnerAdapter buildAdapter(final Context context, @LayoutRes final int layout) {
        return new MyAdapter(context, layout, getEntries());
    }

    static class MyAdapter extends CheckedTypedItemAdapter<CharSequence> {

        public MyAdapter(final Context context, final int resource, final CharSequence[] objects) {
            super(context, resource, android.R.id.text1, objects);
        }

        @Override
        public View getDropDownView(final int position, final View convertView, final ViewGroup parent) {
            final View view = super.getDropDownView(position, convertView, parent);
            setup(position, view);
            return view;
        }

        @NonNull
        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final View view = super.getView(position, convertView, parent);
            setup(position, view);
            return view;
        }

        private void setup(final int position, final View view) {
            TextView text = (TextView) view.findViewById(android.R.id.text1);
            final int baseSp = 16;
            final float q = 1/2f + (float) position / getCount(); // <0.5;1.5>
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, baseSp * q);
        }
    }
}
