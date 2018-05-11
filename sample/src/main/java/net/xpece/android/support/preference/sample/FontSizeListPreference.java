package net.xpece.android.support.preference.sample;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import net.xpece.android.support.preference.ListPreference;
import net.xpece.android.support.widget.CheckedTypedItemAdapter;

/**
 * This is a sample custom preference.
 *
 * It is identical to {@link ListPreference} but it grows text size with increasing index.
 */
public class FontSizeListPreference extends ListPreference {
    public FontSizeListPreference(@NonNull final Context context, @NonNull final AttributeSet attrs, @AttrRes final int defStyleAttr, @StyleRes final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public FontSizeListPreference(@NonNull final Context context, @NonNull final AttributeSet attrs, @AttrRes final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FontSizeListPreference(@NonNull final Context context, @NonNull final AttributeSet attrs) {
        super(context, attrs);
    }

    public FontSizeListPreference(@NonNull final Context context) {
        super(context);
    }

    @NonNull
    @Override
    public SpinnerAdapter buildSimpleDialogAdapter(@NonNull final Context context) {
        return buildAdapter(context, R.layout.asp_select_dialog_item);
    }

    @NonNull
    @Override
    public SpinnerAdapter buildSimpleMenuAdapter(@NonNull final Context context) {
        return buildAdapter(context, R.layout.asp_simple_spinner_dropdown_item);
    }

    @NonNull
    private SpinnerAdapter buildAdapter(@NonNull final Context context, @LayoutRes final int layout) {
        return new MyAdapter(context, layout, getEntries());
    }

    static class MyAdapter extends CheckedTypedItemAdapter<CharSequence> {

        public MyAdapter(@NonNull final Context context, @LayoutRes final int resource, @NonNull final CharSequence[] objects) {
            super(context, resource, android.R.id.text1, objects);
        }

        @Override
        @NonNull
        public View getDropDownView(final int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {
            final View view = super.getDropDownView(position, convertView, parent);
            setup(position, view);
            return view;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {
            final View view = super.getView(position, convertView, parent);
            setup(position, view);
            return view;
        }

        private void setup(final int position, final View view) {
            TextView text = view.findViewById(android.R.id.text1);
            final int baseSp = 16;
            final float q = 1/2f + (float) position / getCount(); // <0.5;1.5>
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, baseSp * q);
        }
    }
}
