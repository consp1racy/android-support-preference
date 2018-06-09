package net.xpece.android.support.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import net.xpece.android.support.widget.spinner.R;

@SuppressLint("ViewConstructor")
final class XpDropDownListView extends ListView {
    public static final int NO_POSITION = -1;

    private static final int[] ATTRS = new int[]{android.R.attr.clipToPadding};

    private LruCache<Integer, View> mMeasuredViewCache = new LruCache<>(2);

    private boolean mHasMultiLineItems;

    private boolean mResolvingListPadding = false;

    /**
     * <p>Creates a new list view wrapper.</p>
     *
     * @param context this view's context
     */
    XpDropDownListView(@NonNull final Context context) {
        super(context, null, R.attr.dropDownListViewStyle);
        setCacheColorHint(0); // Transparent, since the background drawable could be anything.

        if (Build.VERSION.SDK_INT < 21) {
            // For the love of god clipToPadding just cannot be read on the first try on Android 4.
            TypedArray a = context.obtainStyledAttributes(null, ATTRS, R.attr.dropDownListViewStyle, 0);
            final boolean clipToPadding = a.getBoolean(0, true);
            a.recycle();
            setClipToPadding(clipToPadding);
        }
    }

    boolean hasMultiLineItems() {
        return mHasMultiLineItems;
    }

    // @formatter:off
    /**
     * Measures the height of the given range of children (inclusive) and returns the height
     * with divider heights included. If maxHeight is provided, the
     * measuring will stop when the current height reaches maxHeight.
     *
     * @param widthMeasureSpec             The width measure spec to be given to a child's
     *                                     {@link View#measure(int, int)}.
     * @param startPosition                The position of the first child to be shown.
     * @param endPosition                  The (inclusive) position of the last child to be
     *                                     shown. Specify {@link #NO_POSITION} if the last child
     *                                     should be the last available child from the adapter.
     * @param maxHeight                    The maximum height that will be returned (if all the
     *                                     children don't fit in this value, this value will be
     *                                     returned).
     * @param disallowPartialChildPosition In general, whether the returned height should only
     *                                     contain entire children. This is more powerful--it is
     *                                     the first inclusive position at which partial
     *                                     children will not be allowed. Example: it looks nice
     *                                     to have at least 3 completely visible children, and
     *                                     in portrait this will most likely fit; but in
     *                                     landscape there could be times when even 2 children
     *                                     can not be completely shown, so a value of 2
     *                                     (remember, inclusive) would be good (assuming
     *                                     startPosition is 0).
     * @return The height of this ListView with the given children.
     */
    public int measureHeightOfChildrenCompat(int widthMeasureSpec, int startPosition,
                                             int endPosition, final int maxHeight,
                                             int disallowPartialChildPosition) {

        final int reportedDividerHeight = getDividerHeight();
        final Drawable divider = getDivider();

        final ListAdapter adapter = getAdapter();

        if (adapter == null) {
            return 0;
        }

        // Include the padding of the list
        int returnedHeight = 0;
        final int dividerHeight = ((reportedDividerHeight > 0) && divider != null)
                ? reportedDividerHeight : 0;

        // The previous height value that was less than maxHeight and contained
        // no partial children
        int prevHeightWithoutPartialChild = 0;

        View child = null;
        int viewType = -1;
        int count = adapter.getCount();
        int start = startPosition;
        if (start < 0) {
            start = 0;
        }
        int end = endPosition;
        if (end < 0 || end > count) {
            end = count;
        }
        for (int i = start; i < end; i++) {
            int newType = adapter.getItemViewType(i);
            if (newType != viewType) {
                child = mMeasuredViewCache.get(newType);
                viewType = newType;
            }
            child = adapter.getView(i, child, this);
            mMeasuredViewCache.put(viewType, child);

            // Compute child height spec
            int heightMeasureSpec;
            ViewGroup.LayoutParams childLp = child.getLayoutParams();

            if (childLp == null) {
                childLp = generateDefaultLayoutParams();
                child.setLayoutParams(childLp);
            }

            if (childLp.height > 0) {
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(childLp.height,
                        MeasureSpec.EXACTLY);
            } else {
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            }
            child.measure(widthMeasureSpec, heightMeasureSpec);

            // Since this view was measured directly against the parent measure
            // spec, we must measure it again before reuse.
            child.forceLayout();

            if (i > 0) {
                // Count the divider for all but one child
                returnedHeight += dividerHeight;
            }

            returnedHeight += child.getMeasuredHeight();

            if (!mHasMultiLineItems) {
                int measuredHeight = child.getMeasuredHeight();
                int minimumHeight = ViewCompat.getMinimumHeight(child);
                if (measuredHeight > minimumHeight) {
                    mHasMultiLineItems = true;
                }
            }

            if (returnedHeight >= maxHeight) {
                // We went over, figure out which height to return.  If returnedHeight >
                // maxHeight, then the i'th position did not fit completely.
                return (disallowPartialChildPosition >= 0) // Disallowing is enabled (> -1)
                        && (i > disallowPartialChildPosition) // We've past the min pos
                        && (prevHeightWithoutPartialChild > 0) // We have a prev height
                        && (returnedHeight != maxHeight) // i'th child did not fit completely
                        ? prevHeightWithoutPartialChild
                        : maxHeight;
            }

            if ((disallowPartialChildPosition >= 0) && (i >= disallowPartialChildPosition)) {
                prevHeightWithoutPartialChild = returnedHeight;
            }
        }

        // At this point, we went through the range of children, and they each
        // completely fit, so return the returnedHeight
        return returnedHeight;
    }
    // @formatter:on

    /**
     * Compute preferred, or unconstrained, width of the largest child view plus list padding.
     * <p>
     * Note: This method measures all items in the adapter, don't use with large data sets.
     *
     * @return Preferred width of the list view
     */
    public int compatMeasureContentWidth() {
        ensureListPaddingResolved();

        ListAdapter adapter = getAdapter();

        if (adapter == null) {
            return getAllHorizontalPadding();
        }

        int returnedWidth = 0;

        final int widthMeasureSpec =
                MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.UNSPECIFIED);
        final int heightMeasureSpec =
                MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.UNSPECIFIED);

        View child = null;
        int viewType = -1;
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            final int newType = adapter.getItemViewType(i);
            if (newType != viewType) {
                child = mMeasuredViewCache.get(newType);
                viewType = newType;
            }
            child = adapter.getView(i, child, this);
            mMeasuredViewCache.put(viewType, child);

            ViewGroup.LayoutParams childLp = child.getLayoutParams();
            if (childLp == null) {
                childLp = generateDefaultLayoutParams();
                child.setLayoutParams(childLp);
            }
            childLp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            childLp.width = ViewGroup.LayoutParams.WRAP_CONTENT;

            child.measure(widthMeasureSpec, heightMeasureSpec);

            // Since this view was measured directly against the parent measure
            // spec, we must measure it again before reuse.
            child.forceLayout();

            returnedWidth = Math.max(returnedWidth, child.getMeasuredWidth());
        }

        // Add ListView's own padding to measured width
        returnedWidth += getAllHorizontalPadding();

        return returnedWidth;
    }

    private int getAllHorizontalPadding() {
        return getListPaddingLeft() + getListPaddingRight();
    }

    /**
     * {@code #getListPadding*()} returns incorrect number before the first measurement.
     * Call this method to manually resolve list padding.
     */
    @SuppressLint("WrongCall")
    public void ensureListPaddingResolved() {
        mResolvingListPadding = true;
        final int transcriptMode = getTranscriptMode();
        try {
            // Make sure we intercept some call and terminate measurement after what we need.
            // The first call is #getChildCount when transcript mode is normal.
            setTranscriptMode(TRANSCRIPT_MODE_NORMAL);

            // List padding is resolved as part of AbsListView#onMeasure.
            // Do NOT call measure! We're not actually measuring and setting measured dimension.
            onMeasure(0, 0);
        } catch (StopExecution ignored) {
            // See comments in getChildCount().
        } finally {
            setTranscriptMode(transcriptMode);
            mResolvingListPadding = false;
        }
    }

    @Override
    public int getChildCount() {
        if (mResolvingListPadding) {
            throw new StopExecution();
        }
        return super.getChildCount();
    }

    private class StopExecution extends RuntimeException {
        StopExecution() {
        }
    }
}
