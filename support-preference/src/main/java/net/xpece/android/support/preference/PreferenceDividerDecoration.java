package net.xpece.android.support.preference;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.DimenRes;
import android.support.annotation.Dimension;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceGroupAdapter;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.TintTypedArray;
import android.util.TypedValue;
import android.view.View;

import static android.support.annotation.Dimension.DP;

/**
 * Use this class to add dividers between {@link Preference} items.
 */
public class PreferenceDividerDecoration extends RecyclerView.ItemDecoration {

    private boolean mDrawTop = false;
    private boolean mDrawBottom = false;
    private boolean mDrawBetweenItems = true;
    private boolean mDrawBetweenCategories = true;

    private Drawable mDivider;
    private int mDividerHeight;

    private int mPadding;

    public PreferenceDividerDecoration(@Nullable final Drawable divider, @Dimension final int dividerHeight) {
        mDivider = divider;
        mDividerHeight = dividerHeight;
    }

    public PreferenceDividerDecoration(final @NonNull Context context, @DrawableRes final int divider, @DimenRes final int dividerHeight) {
        mDivider = Util.getDrawableCompat(context, divider);
        mDividerHeight = context.getResources().getDimensionPixelSize(dividerHeight);
    }

    @SuppressWarnings("RestrictedApi")
    public PreferenceDividerDecoration(final @NonNull Context context) {
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, null, new int[]{R.attr.dividerHorizontal});
        mDivider = a.getDrawable(0);
        a.recycle();

        if (mDivider != null) {
            mDividerHeight = mDivider.getIntrinsicHeight();
        }
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
    @NonNull
    public PreferenceDividerDecoration drawTop(final boolean drawTop) {
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
    @NonNull
    public PreferenceDividerDecoration drawBottom(final boolean drawBottom) {
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
    @NonNull
    public PreferenceDividerDecoration drawBetweenItems(final boolean drawBetweenItems) {
        mDrawBetweenItems = drawBetweenItems;
        return this;
    }

    public boolean getDrawBetweenCategories() {
        return mDrawBetweenCategories;
    }

    /**
     * Controls whether to draw divider above each {@link PreferenceGroup} usually {@link PreferenceCategory}.
     *
     * @param drawBetweenCategories
     * @return
     */
    @NonNull
    public PreferenceDividerDecoration drawBetweenCategories(final boolean drawBetweenCategories) {
        mDrawBetweenCategories = drawBetweenCategories;
        return this;
    }

    public int getPadding() {
        return mPadding;
    }

    /**
     * Controls padding around dividers.
     *
     * @param padding Padding above and below a divider in pixels.
     * @return
     */
    @NonNull
    public PreferenceDividerDecoration padding(@Dimension final int padding) {
        mPadding = padding;
        return this;
    }

    /**
     * Controls padding around dividers.
     *
     * @param context
     * @param paddingDp Padding above and below a divider in dips.
     * @return
     */
    @NonNull
    public PreferenceDividerDecoration paddingDp(final @NonNull Context context, @Dimension(unit = DP) final float paddingDp) {
        int paddingPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, paddingDp, context.getResources().getDisplayMetrics());
        return padding(paddingPx);
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    public void getItemOffsets(final @NonNull Rect outRect, final @NonNull View view, final @NonNull RecyclerView parent, final @NonNull RecyclerView.State state) {
        if (mDivider == null || mDividerHeight == 0) {
            outRect.setEmpty();
            return;
        }

//        if (mPadding == 0) {
//            // Don't draw dividers below the preference if there is no padding.
//            outRect.setEmpty();
//            return;
//        }

        final PreferenceGroupAdapter adapter = (PreferenceGroupAdapter) parent.getAdapter();
        final int adapterCount = adapter.getItemCount();

        final int adapterPosition = parent.getChildAdapterPosition(view);
        final Preference preference = adapter.getItem(adapterPosition);

        int topPadding = 0;
        int bottomPadding = 0;
        if (adapterCount == 1) {
            if (mDrawTop) topPadding = mDividerHeight;
            if (mDrawBottom) bottomPadding = mDividerHeight;
        } else if (adapterPosition == 0) {
            final Preference nextPreference = adapter.getItem(adapterPosition + 1);
            if ((nextPreference instanceof PreferenceCategory && mDrawBetweenCategories) ||
                    (!(preference instanceof PreferenceCategory) && mDrawBetweenItems)) {
                bottomPadding = mPadding + mDividerHeight;
            }
            if (mDrawTop) topPadding += mDividerHeight;
        } else if (adapterPosition == adapterCount - 1) {
            final Preference previousPreference = adapter.getItem(adapterPosition - 1);
            if ((preference instanceof PreferenceCategory && mDrawBetweenCategories) ||
                    (!(previousPreference instanceof PreferenceCategory) && mDrawBetweenItems)) {
                topPadding = mPadding;
            }
            if (mDrawBottom) bottomPadding += mDividerHeight;
        } else {
            final Preference previousPreference = adapter.getItem(adapterPosition - 1);
            if ((preference instanceof PreferenceCategory && mDrawBetweenCategories) ||
                    (!(previousPreference instanceof PreferenceCategory) && mDrawBetweenItems)) {
                topPadding = mPadding;
            }
            final Preference nextPreference = adapter.getItem(adapterPosition + 1);
            if ((nextPreference instanceof PreferenceCategory && mDrawBetweenCategories) ||
                    (!(preference instanceof PreferenceCategory) && mDrawBetweenItems)) {
                bottomPadding = mPadding + mDividerHeight;
            }
        }
        outRect.set(0, topPadding, 0, bottomPadding);
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    public void onDrawOver(final @NonNull Canvas c, final @NonNull RecyclerView parent, final @NonNull RecyclerView.State state) {
        if (mDivider == null || mDividerHeight == 0) return;

        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        final PreferenceGroupAdapter adapter = (PreferenceGroupAdapter) parent.getAdapter();
        if (adapter == null) return;
        final int adapterCount = adapter.getItemCount();

        boolean wasLastPreferenceGroup = false;
        for (int i = 0, childCount = parent.getChildCount(); i < childCount; i++) {
            final View child = parent.getChildAt(i);

            final int adapterPosition = parent.getChildAdapterPosition(child);
            final Preference preference = adapter.getItem(adapterPosition);

            boolean skipNextAboveDivider = false;
            if (adapterPosition == 0) {
                if (mDrawTop) {
                    final int decoratedTop = parent.getLayoutManager().getDecoratedTop(child);
                    drawAbove(c, left, right, child, decoratedTop);
                }
                skipNextAboveDivider = true;
            }

            if (preference instanceof PreferenceGroup
                    && !(preference instanceof PreferenceScreen)) {
                if (mDrawBetweenCategories) {
                    if (!skipNextAboveDivider) {
                        final int decoratedTop = parent.getLayoutManager().getDecoratedTop(child);
                        drawAbove(c, left, right, child, decoratedTop);
                        skipNextAboveDivider = true;
                    }
                }
                wasLastPreferenceGroup = true;
            } else {
                if (mDrawBetweenItems && !wasLastPreferenceGroup) {
                    if (!skipNextAboveDivider) {
                        final int decoratedTop = parent.getLayoutManager().getDecoratedTop(child);
                        drawAbove(c, left, right, child, decoratedTop);
                        skipNextAboveDivider = true;
                    }
                }
                wasLastPreferenceGroup = false;
            }

            if (adapterPosition == adapterCount - 1) {
                if (mDrawBottom) {
                    final int decoratedBottom = parent.getLayoutManager().getDecoratedBottom(child);
                    drawBottom(c, left, right, child, decoratedBottom);
                }
            }
        }
    }

    private void drawAbove(final @NonNull Canvas c, final int left, final int right, final @NonNull View child, final int decoratedTop) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        final int top = decoratedTop - params.topMargin - mDividerHeight;
        final int bottom = top + mDividerHeight;
        mDivider.setBounds(left, top, right, bottom);
        mDivider.draw(c);
    }

    private void drawBottom(final @NonNull Canvas c, final int left, final int right, final @NonNull View child, final int decoratedBottom) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        final int top = decoratedBottom + params.bottomMargin - mDividerHeight;
        final int bottom = top + mDividerHeight;
        mDivider.setBounds(left, top, right, bottom);
        mDivider.draw(c);
    }
}
