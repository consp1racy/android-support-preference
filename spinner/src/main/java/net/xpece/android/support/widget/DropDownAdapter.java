package net.xpece.android.support.widget;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

/**
 * <p>Wrapper class for an Adapter. Transforms the embedded Adapter instance
 * into a ListAdapter.</p>
 *
 * @hide
 */
public class DropDownAdapter implements ListAdapter, SpinnerAdapter {
    private static final boolean IS_AT_LEAST_M = Build.VERSION.SDK_INT >= 23;

    private SpinnerAdapter mAdapter;

    private ListAdapter mListAdapter;

    /**
     * Creates a new ListAdapter wrapper for the specified adapter.
     *
     * @param adapter the SpinnerAdapter to transform into a ListAdapter
     * @param dropDownTheme the theme against which to inflate drop-down
     * views, may be {@null} to use default theme
     */
    @TargetApi(Build.VERSION_CODES.M)
    public DropDownAdapter(@Nullable SpinnerAdapter adapter,
                           @Nullable Resources.Theme dropDownTheme) {
        mAdapter = adapter;

        if (adapter instanceof ListAdapter) {
            mListAdapter = (ListAdapter) adapter;
        }

        if (dropDownTheme != null) {
            if (IS_AT_LEAST_M && adapter instanceof android.widget.ThemedSpinnerAdapter) {
                final android.widget.ThemedSpinnerAdapter themedAdapter =
                    (android.widget.ThemedSpinnerAdapter) adapter;
                if (themedAdapter.getDropDownViewTheme() != dropDownTheme) {
                    themedAdapter.setDropDownViewTheme(dropDownTheme);
                }
            } else if (adapter instanceof ThemedSpinnerAdapter) {
                final ThemedSpinnerAdapter themedAdapter = (ThemedSpinnerAdapter) adapter;
                if (themedAdapter.getDropDownViewTheme() == null) {
                    themedAdapter.setDropDownViewTheme(dropDownTheme);
                }
            }
        }
    }

    public int getCount() {
        return mAdapter == null ? 0 : mAdapter.getCount();
    }

    public Object getItem(int position) {
        return mAdapter == null ? null : mAdapter.getItem(position);
    }

    public long getItemId(int position) {
        return mAdapter == null ? -1 : mAdapter.getItemId(position);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return getDropDownView(position, convertView, parent);
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return (mAdapter == null) ? null
            : mAdapter.getDropDownView(position, convertView, parent);
    }

    public boolean hasStableIds() {
        return mAdapter != null && mAdapter.hasStableIds();
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(observer);
        }
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(observer);
        }
    }

    /**
     * If the wrapped SpinnerAdapter is also a ListAdapter, delegate this call.
     * Otherwise, return true.
     */
    public boolean areAllItemsEnabled() {
        final ListAdapter adapter = mListAdapter;
        if (adapter != null) {
            return adapter.areAllItemsEnabled();
        } else {
            return true;
        }
    }

    /**
     * If the wrapped SpinnerAdapter is also a ListAdapter, delegate this call.
     * Otherwise, return true.
     */
    public boolean isEnabled(int position) {
        final ListAdapter adapter = mListAdapter;
        if (adapter != null) {
            return adapter.isEnabled(position);
        } else {
            return true;
        }
    }

    public int getItemViewType(int position) {
        return 0;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public boolean isEmpty() {
        return getCount() == 0;
    }
}
