package net.xpece.android.support.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import java.util.WeakHashMap;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An adapter that's responsible for transforming its items to text representation that's used
 * <ul>
 *     <li>in a {@link android.widget.ListView} or a {@link android.widget.Spinner},</li>
 *     <li>in {@link android.widget.Spinner} popup menu.</li>
 * </ul>
 * Additionally checked items will be highlighted.
 */
@ParametersAreNonnullByDefault
public class CheckedTypedItemAdapter<T> extends ArrayAdapter<T> implements ThemedSpinnerAdapter {

    private static final int[] DISABLED_STATE_SET = {-android.R.attr.state_enabled};
    @SuppressLint("InlinedApi")
    private static final int[] ACTIVATED_STATE_SET = {android.R.attr.state_activated};
    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
    private static final int[] EMPTY_STATE_SET = {};

    private static final WeakHashMap<View, Drawable> sCheckedBackgroundMap = new WeakHashMap<>();

    private final Helper mDropDownHelper;
    private final LayoutInflater mInflater;
    private final int mFieldId;
    private int mDropDownResource;
    private int mResource;

    @NonNull
    public static <T> CheckedTypedItemAdapter newInstance(Context context, T[] objects) {
        return newInstance(context, Arrays.asList(objects));
    }

    @NonNull
    public static <T> CheckedTypedItemAdapter newInstance(Context context, List<T> objects) {
        CheckedTypedItemAdapter a = new CheckedTypedItemAdapter<>(context, android.R.layout.simple_spinner_item, android.R.id.text1, objects);
        a.setDropDownViewResource(R.layout.asp_simple_spinner_dropdown_item);
        return a;
    }

    public CheckedTypedItemAdapter(
            Context context,
            @LayoutRes int resource,
            @IdRes int textViewResourceId,
            T[] objects) {
        this(context, resource, textViewResourceId, Arrays.asList(objects));
    }

    public CheckedTypedItemAdapter(
            Context context,
            @LayoutRes int resource,
            @IdRes int textViewResourceId,
            List<T> objects) {
        super(context, resource, textViewResourceId, objects);

        mDropDownHelper = new Helper(context);
        mInflater = LayoutInflater.from(context);
        mFieldId = textViewResourceId;
        mResource = mDropDownResource = resource;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getDropDownView(
            final int position, @Nullable final View convertView, final ViewGroup parent) {
        LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
        View view = createViewFromResource(inflater, convertView, parent, mDropDownResource);
        T item = getItem(position);
        assert item != null;
        bindDropDownView(view, item);
        //noinspection deprecation
        view.setBackgroundDrawable(getCheckedBackgroundDrawable(view));
        return view;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        View view = createViewFromResource(mInflater, convertView, parent, mResource);
        T item = getItem(position);
        assert item != null;
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

    @NonNull
    protected View createViewFromResource(
            LayoutInflater inflater, @Nullable View convertView,
            ViewGroup parent, @LayoutRes int resource) {
        View view;
        if (convertView == null) {
            view = inflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }
        return view;
    }

    @NonNull
    protected TextView findTextView(View view) {
        TextView text;
        try {
            if (mFieldId == 0) {
                //  If no custom field is assigned, assume the whole resource is a TextView
                text = (TextView) view;
            } else {
                //  Otherwise, find the TextView field within the layout
                text = view.findViewById(mFieldId);
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

    /**
     * The method is responsible for transforming an object from the list into a string
     * representation that's used in a {@link android.widget.ListView} or
     * a {@link android.widget.Spinner}.
     *
     * @param item A data object
     * @return String representation of {@code item}.
     */
    @NonNull
    public CharSequence getItemText(T item) {
        return item.toString();
    }

    /**
     * The method is responsible for transforming an object from the list into a string
     * representation that's used in a a popup menu of a {@link android.widget.Spinner}
     * or simple menu / simple dialog of a {@link XpAppCompatSpinner}.
     *
     * @param item A data object
     * @return String representation of {@code item}.
     */
    @NonNull
    public CharSequence getItemDropDownText(T item) {
        return getItemText(item);
    }

    @NonNull
    private Drawable getCheckedBackgroundDrawable(final View view) {
        Drawable d = sCheckedBackgroundMap.get(view);
        if (d == null) {
            d = createCheckedBackgroundDrawable(view.getContext());
            sCheckedBackgroundMap.put(view, d);
        }
        return d;
    }

    @NonNull
    private Drawable createCheckedBackgroundDrawable(Context context) {
        final int highlight = XpSpinnerUtil.resolveColor(context, R.attr.colorControlHighlight, 0);
        final int[][] states = new int[4][];
        final Drawable[] drawables = new Drawable[4];
        int i = 0;

        // Disabled state
        states[i] = DISABLED_STATE_SET;
        drawables[i] = new ColorDrawable(0);
        i++;

        states[i] = CHECKED_STATE_SET;
        drawables[i] = new ColorDrawable(highlight);
        i++;

        states[i] = ACTIVATED_STATE_SET;
        drawables[i] = new ColorDrawable(highlight);
        i++;

        // Default enabled state
        states[i] = EMPTY_STATE_SET;
        drawables[i] = new ColorDrawable(0);
        i++;

        StateListDrawable d = new StateListDrawable();
        for (int j = 0, size = states.length; j < size; j++) {
            d.addState(states[j], drawables[j]);
        }
        return d;
    }

}
