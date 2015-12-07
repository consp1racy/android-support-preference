package android.support.v7.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceGroupAdapter;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceCategory;
import android.view.View;

import net.xpece.android.support.preference.R;

/**
 * Use this class to add dividers between {@link Preference} items.
 */
public class PreferenceDividerDecoration extends RecyclerView.ItemDecoration {

    private boolean mDrawTop = false; // above first item, takes priority
    private boolean mDrawBottom = false; // at bottom of last item, takes priority
    private boolean mDrawBetweenItems = true; // at bottom of each item
    private boolean mDrawBetweenCategories = true; // above each PreferenceGroup

    private Drawable mDivider;
    private int mDividerHeight;

    public PreferenceDividerDecoration(Drawable divider, int dividerHeight) {
        mDivider = divider;
        mDividerHeight = dividerHeight;
    }

    public PreferenceDividerDecoration(Context context, @DrawableRes int divider, @DimenRes int dividerHeight) {
        mDivider = ContextCompat.getDrawable(context, divider);
        mDividerHeight = context.getResources().getDimensionPixelSize(dividerHeight);
    }

    public PreferenceDividerDecoration(Context context) {
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, null, new int[]{R.attr.dividerHorizontal});
        mDivider = a.getDrawable(0);
        a.recycle();

        mDividerHeight = mDivider.getIntrinsicHeight();
    }

    public boolean getDrawTop() {
        return mDrawTop;
    }

    /**
     * Controls whether to draw divider above the first item.
     *
     * @param drawTop
     * @return
     */
    public PreferenceDividerDecoration drawTop(boolean drawTop) {
        mDrawTop = drawTop;
        return this;
    }

    public boolean getDrawBottom() {
        return mDrawBottom;
    }

    /**
     * Controls whether to draw divider at the bottom of the last item.
     *
     * @param drawBottom
     * @return
     */
    public PreferenceDividerDecoration drawBottom(boolean drawBottom) {
        mDrawBottom = drawBottom;
        return this;
    }

    public boolean getDrawBetweenItems() {
        return mDrawBetweenItems;
    }

    /**
     * Controls whether to draw divider at the bottom of each {@link Preference} and {@link PreferenceScreen} item.
     *
     * @param drawBetweenItems
     * @return
     */
    public PreferenceDividerDecoration drawBetweenItems(boolean drawBetweenItems) {
        mDrawBetweenItems = drawBetweenItems;
        return this;
    }

    public boolean getDrawBetweenCategories() {
        return mDrawBetweenCategories;
    }

    /**
     * Controls whether to draw divider above each {@link PreferenceGroup} usually {@link PreferenceCategory}.
     * @param drawBetweenCategories
     * @return
     */
    public PreferenceDividerDecoration drawBetweenCategories(boolean drawBetweenCategories) {
        mDrawBetweenCategories = drawBetweenCategories;
        return this;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        final PreferenceGroupAdapter adapter = (PreferenceGroupAdapter) parent.getAdapter();
        final int adapterCount = adapter.getItemCount();

        boolean drawnPreviousBottom = false;
        for (int i = 0, childCount = parent.getChildCount(); i < childCount; i++) {
            final View child = parent.getChildAt(i);

            final int adapterPosition = parent.getChildAdapterPosition(child);
            Preference preference = adapter.getItem(adapterPosition);
            if (preference instanceof PreferenceScreen) {
                // Presents itself as item.
                drawnPreviousBottom = drawItem(c, left, right, adapterCount, child, adapterPosition);
            } else if (preference instanceof PreferenceGroup) {
                if (mDrawBetweenCategories && !drawnPreviousBottom && (mDrawTop || adapterPosition != 0)) {
                    drawAbove(c, left, right, child);
                }

                // Divider at bottom of last item.
                if (mDrawBottom && !drawnPreviousBottom && adapterPosition == adapterCount - 1) {
                    drawBottom(c, left, right, child);
                    // Last item, don't need to track drawnPreviousBottom.
                }

                drawnPreviousBottom = false;
            } else {
                drawnPreviousBottom = drawItem(c, left, right, adapterCount, child, adapterPosition);
            }
        }
    }

    public boolean drawItem(Canvas c, int left, int right, int adapterCount, View child, int adapterPosition) {
        boolean drawnPreviousBottom;

        // Divider above the first item.
        if (mDrawTop && adapterPosition == 0) {
            drawAbove(c, left, right, child);
        }

        if (mDrawBetweenItems && (mDrawBottom || adapterPosition != adapterCount - 1)) {
            drawBottom(c, left, right, child);
            drawnPreviousBottom = true;
        } else {
            drawnPreviousBottom = false;
        }

        return drawnPreviousBottom;
    }

    private void drawAbove(Canvas c, int left, int right, View child) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        final int top = child.getTop() - params.topMargin - mDividerHeight;
        final int bottom = top + mDividerHeight;
        mDivider.setBounds(left, top, right, bottom);
        mDivider.draw(c);
    }

    private void drawBottom(Canvas c, int left, int right, View child) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        final int top = child.getBottom() + params.bottomMargin - mDividerHeight;
        final int bottom = top + mDividerHeight;
        mDivider.setBounds(left, top, right, bottom);
        mDivider.draw(c);
    }
}
