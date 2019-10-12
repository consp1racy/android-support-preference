/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.xpece.android.support.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.AttrRes;
import androidx.annotation.Dimension;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.Size;
import androidx.annotation.StyleRes;
import androidx.core.text.TextUtilsCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ListViewCompat;
import androidx.core.widget.PopupWindowCompat;
import androidx.appcompat.view.menu.ShowableListMenu;
import androidx.appcompat.widget.ForwardingListener;
import android.util.AttributeSet;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

import net.xpece.android.support.widget.spinner.R;

import java.lang.reflect.Method;
import java.util.Locale;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Static library support version of the framework's {@link android.widget.ListPopupWindow}.
 * Used to write apps that run on platforms prior to Android L. When running
 * on Android L or above, this implementation is still used; it does not try
 * to switch to the framework's implementation. See the framework SDK
 * documentation for a class overview.
 *
 * @hide
 * @see android.widget.ListPopupWindow
 */
@RestrictTo(LIBRARY)
@SuppressLint("RestrictedApi")
abstract class AbstractXpListPopupWindow implements ShowableListMenu {
    private static final String TAG = AbstractXpListPopupWindow.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static final boolean API_18 = Build.VERSION.SDK_INT >= 18;

    /**
     * This value controls the length of time that the user
     * must leave a pointer down without scrolling to expand
     * the autocomplete dropdown list to cover the IME.
     */
    private static final int EXPAND_LIST_TIMEOUT = 250;

    private static Method sSetEpicenterBoundsMethod;

    static {
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                sSetEpicenterBoundsMethod = PopupWindow.class.getDeclaredMethod(
                        "setEpicenterBounds", Rect.class);
            } catch (NoSuchMethodException e) {
                Log.i(TAG, "Could not find method setEpicenterBounds(Rect) on PopupWindow. Oh well.");
            }
        }
    }

    private Context mContext;
    XpAppCompatPopupWindow mPopup;
    private ListAdapter mAdapter;
    XpDropDownListView mDropDownList;
    boolean mListMeasureDirty;
    private int mListMeasuredHeight = -1;

    private int mDropDownMaxWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
    private float mDropDownPreferredWidthUnit = 0;
    private int mMaxItemCount = -1;

    private int mDropDownHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
    private int mDropDownWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
    private int mDropDownHorizontalOffset;
    private int mDropDownVerticalOffset;
    private int mDropDownWindowLayoutType = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL;

    private int mDropDownGravity = Gravity.NO_GRAVITY;

    private boolean mDropDownAlwaysVisible = false;
    private boolean mForceIgnoreOutsideTouch = false;
    int mListItemExpandMaximum = Integer.MAX_VALUE;

    private View mPromptView;
    private int mPromptPosition = POSITION_PROMPT_ABOVE;

    private DataSetObserver mObserver;

    private View mDropDownAnchorView;
    private View mDropDownBoundsView;
    private final Rect mMargins = new Rect();

    private Drawable mDropDownListHighlight;

    private AdapterView.OnItemClickListener mItemClickListener;
    private AdapterView.OnItemSelectedListener mItemSelectedListener;

    final ResizePopupRunnable mResizePopupRunnable = new ResizePopupRunnable();
    private final PopupTouchInterceptor mTouchInterceptor = new PopupTouchInterceptor();
    private final PopupScrollListener mScrollListener = new PopupScrollListener();
    private final ListSelectorHider mHideSelector = new ListSelectorHider();
    private Runnable mShowDropDownRunnable;

    final Handler mHandler;

    private final Rect mTempRect = new Rect();
    private final int[] mTempLocation = new int[2];

    /**
     * Optional anchor-relative bounds to be used as the transition epicenter.
     * When {@code null}, the anchor bounds are used as the epicenter.
     */
    private Rect mEpicenterBounds;

    private boolean mModal;

    private int mLayoutDirection;

    /**
     * The provided prompt view should appear above list content.
     *
     * @see #setPromptPosition(int)
     * @see #getPromptPosition()
     * @see #setPromptView(View)
     */
    public static final int POSITION_PROMPT_ABOVE = 0;

    /**
     * The provided prompt view should appear below list content.
     *
     * @see #setPromptPosition(int)
     * @see #getPromptPosition()
     * @see #setPromptView(View)
     */
    public static final int POSITION_PROMPT_BELOW = 1;

    /** Popup menu width is only limited by {@code maxWidth}. */
    public static final int WIDTH_MATCH_CONSTRAINT = -1;

    /**
     * Popup menu width is
     * * at least as wide as its content,
     * * limited by {@code maxWidth}.
     */
    public static final int WIDTH_WRAP_CONTENT = -2;

    /**
     * Popup menu width is
     * <ul>
     * <li>at least as wide as its content rounded up to a multiple of {@code widthUnit},</li>
     * <li>at least as wide as {@code widthUnit * 1.5},</li>
     * <li>limited by {@code maxWidth}.</li>
     * </ul>
     */
    public static final int WIDTH_WRAP_CONTENT_UNIT = -3;

    /**
     * Popup menu width is limited by screen width.
     *
     * @see #setMaxWidth(int)
     */
    public static final int MAX_WIDTH_FIT_SCREEN = -1;

    /**
     * Popup menu width is limited by anchor width.
     *
     * @see #setMaxWidth(int)
     */
    public static final int MAX_WIDTH_FIT_ANCHOR = -2;

    public static final int MATCH_PARENT = -1;

    public static final int WRAP_CONTENT = -2;

    @Deprecated
    public static final int PREFERRED = WIDTH_WRAP_CONTENT_UNIT;

    /**
     * Mode for {@link #setInputMethodMode(int)}: the requirements for the
     * input method should be based on the focusability of the popup.  That is
     * if it is focusable than it needs to work with the input method, else
     * it doesn't.
     */
    public static final int INPUT_METHOD_FROM_FOCUSABLE = PopupWindow.INPUT_METHOD_FROM_FOCUSABLE;

    /**
     * Mode for {@link #setInputMethodMode(int)}: this popup always needs to
     * work with an input method, regardless of whether it is focusable.  This
     * means that it will always be displayed so that the user can also operate
     * the input method while it is shown.
     */
    public static final int INPUT_METHOD_NEEDED = PopupWindow.INPUT_METHOD_NEEDED;

    /**
     * Mode for {@link #setInputMethodMode(int)}: this popup never needs to
     * work with an input method, regardless of whether it is focusable.  This
     * means that it will always be displayed to use as much space on the
     * screen as needed, regardless of whether this covers the input method.
     */
    public static final int INPUT_METHOD_NOT_NEEDED = PopupWindow.INPUT_METHOD_NOT_NEEDED;

    public void setMarginTop(int px) {
        if (mMargins.top != px) {
            mMargins.top = px;
            mListMeasureDirty = true;
        }
    }

    public int getMarginTop() {
        return mMargins.top;
    }

    public void setMarginBottom(int px) {
        if (mMargins.bottom != px) {
            mMargins.bottom = px;
            mListMeasureDirty = true;
        }
    }

    public int getMarginBottom() {
        return mMargins.bottom;
    }

    public void setMarginLeft(int px) {
        if (mMargins.left != px) {
            mMargins.left = px;
            mListMeasureDirty = true;
        }
    }

    public void setMarginStart(int px) {
        if (mLayoutDirection == LayoutDirection.RTL) {
            setMarginRight(px);
        } else {
            setMarginLeft(px);
        }
    }

    @Deprecated
    @SuppressWarnings("unused")
    public int getMarginStart(int px) {
        return getMarginStart();
    }

    public int getMarginStart() {
        if (mLayoutDirection == LayoutDirection.RTL) {
            return getMarginRight();
        } else {
            return getMarginLeft();
        }
    }

    public int getMarginLeft() {
        return mMargins.left;
    }

    public void setMarginRight(int px) {
        if (mMargins.right != px) {
            mMargins.right = px;
            mListMeasureDirty = true;
        }
    }

    public void setMarginEnd(int px) {
        if (mLayoutDirection == LayoutDirection.RTL) {
            setMarginLeft(px);
        } else {
            setMarginRight(px);
        }
    }

    @Deprecated
    @SuppressWarnings("unused")
    public int getMarginEnd(int px) {
        return getMarginEnd();
    }

    public int getMarginEnd() {
        if (mLayoutDirection == LayoutDirection.RTL) {
            return mMargins.left;
        } else {
            return mMargins.right;
        }
    }

    public int getMarginRight() {
        return mMargins.right;
    }

    public void setMargin(int margin) {
        mMargins.set(margin, margin, margin, margin);
    }

    public void setMargin(int horizontal, int vertical) {
        mMargins.set(horizontal, vertical, horizontal, vertical);
    }

    public void setMargin(int left, int top, int right, int bottom) {
        mMargins.set(left, top, right, bottom);
    }

    public void setMarginRelative(int start, int top, int end, int bottom) {
        int left, right;
        if (mLayoutDirection == LayoutDirection.RTL) {
            right = start;
            left = end;
        } else {
            left = start;
            right = end;
        }
        mMargins.set(left, top, right, bottom);
    }

    /**
     * @return Whether the list in current setup would show any multiline items.
     */
    @SuppressWarnings("deprecation")
    public boolean hasMultilineItems() {
        if (mDropDownList == null || mListMeasureDirty) {
            buildDropDown();
        }
        return mDropDownList.hasMultiLineItems();
    }

    /**
     * @return Whether the list in current setup would show any multiline items.
     */
    @Deprecated
    public boolean hasMultiLineItems() {
        return hasMultilineItems();
    }

    int measureItem(int position) {
        return measureItems(position, position + 1);
    }

    int measureItems(int fromIncl, int toExcl) {
        if (mDropDownList == null || mListMeasureDirty) {
            buildDropDown();
        }
        int widthSpec = MeasureSpec.makeMeasureSpec(getListWidthSpec(), MeasureSpec.AT_MOST);
        return mDropDownList.measureHeightOfChildrenCompat(widthSpec, fromIncl, toExcl, Integer.MAX_VALUE, 1);
    }

    /**
     * Create a new, empty popup window capable of displaying items from a ListAdapter.
     * Backgrounds should be set using {@link #setBackgroundDrawable(Drawable)}.
     *
     * @param context Context used for contained views.
     */
    public AbstractXpListPopupWindow(@NonNull Context context) {
        this(context, null);
    }

    /**
     * Create a new, empty popup window capable of displaying items from a ListAdapter.
     * Backgrounds should be set using {@link #setBackgroundDrawable(Drawable)}.
     *
     * @param context Context used for contained views.
     * @param attrs Attributes from inflating parent views used to style the popup.
     */
    public AbstractXpListPopupWindow(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.listPopupWindowStyle);
    }

    /**
     * Create a new, empty popup window capable of displaying items from a ListAdapter.
     * Backgrounds should be set using {@link #setBackgroundDrawable(Drawable)}.
     *
     * @param context Context used for contained views.
     * @param attrs Attributes from inflating parent views used to style the popup.
     * @param defStyleAttr Default style attribute to use for popup content.
     */
    public AbstractXpListPopupWindow(
            @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    /**
     * Create a new, empty popup window capable of displaying items from a ListAdapter.
     * Backgrounds should be set using {@link #setBackgroundDrawable(Drawable)}.
     *
     * @param context Context used for contained views.
     * @param attrs Attributes from inflating parent views used to style the popup.
     * @param defStyleAttr Style attribute to read for default styling of popup content.
     * @param defStyleRes Style resource ID to use for default styling of popup content.
     */
    public AbstractXpListPopupWindow(
            @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr,
            @StyleRes int defStyleRes) {
        mContext = context;
        mHandler = new Handler(context.getMainLooper());

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ListPopupWindow,
                defStyleAttr, defStyleRes);
        mDropDownHorizontalOffset = a.getDimensionPixelOffset(
                R.styleable.ListPopupWindow_android_dropDownHorizontalOffset, 0);
        mDropDownVerticalOffset = a.getDimensionPixelOffset(
                R.styleable.ListPopupWindow_android_dropDownVerticalOffset, 0);
        a.recycle();

        final TypedArray b = context.obtainStyledAttributes(attrs, R.styleable.XpListPopupWindow, defStyleAttr, defStyleRes);

        // Set the default layout direction to match the default locale one
        final Locale locale = mContext.getResources().getConfiguration().locale;
        mLayoutDirection = TextUtilsCompat.getLayoutDirectionFromLocale(locale);

        // Margin is the space reserved for shadow.
        int defaultMargin = XpSpinnerUtil.dpToPxOffset(context, 8);
        if (b.hasValue(R.styleable.XpListPopupWindow_android_layout_margin)) {
            int margin = b.getDimensionPixelOffset(R.styleable.XpListPopupWindow_android_layout_margin, defaultMargin);
            mMargins.bottom = margin;
            mMargins.top = margin;
            mMargins.left = margin;
            mMargins.right = margin;
        } else {
            if (API_18 && b.hasValue(R.styleable.XpListPopupWindow_android_layout_marginEnd)) {
                int margin = b.getDimensionPixelOffset(R.styleable.XpListPopupWindow_android_layout_marginEnd, 0);
                if (mLayoutDirection == LayoutDirection.RTL) {
                    mMargins.left = margin;
                } else {
                    mMargins.right = margin;
                }
            } else {
                mMargins.right = b.getDimensionPixelOffset(R.styleable.XpListPopupWindow_android_layout_marginRight, defaultMargin);
            }
            if (API_18 && b.hasValue(R.styleable.XpListPopupWindow_android_layout_marginStart)) {
                int margin = b.getDimensionPixelOffset(R.styleable.XpListPopupWindow_android_layout_marginStart, 0);
                if (mLayoutDirection == LayoutDirection.RTL) {
                    mMargins.right = margin;
                } else {
                    mMargins.left = margin;
                }
            } else {
                mMargins.left = b.getDimensionPixelOffset(R.styleable.XpListPopupWindow_android_layout_marginLeft, defaultMargin);
            }
            mMargins.top = b.getDimensionPixelOffset(R.styleable.XpListPopupWindow_android_layout_marginTop, defaultMargin);
            mMargins.bottom = b.getDimensionPixelOffset(R.styleable.XpListPopupWindow_android_layout_marginBottom, defaultMargin);
        }

        final float simpleMenuWidthUnit = a.getDimension(R.styleable.XpListPopupWindow_asp_widthUnit, 0f);
        final int simpleMenuWidthMode = a.getInt(R.styleable.XpListPopupWindow_asp_width, 0);
        final int simpleMenuMaxWidth = a.getInt(R.styleable.XpListPopupWindow_asp_maxWidth, 0);
        initWidth(simpleMenuWidthMode, simpleMenuMaxWidth, simpleMenuWidthUnit);

        final int maxItemCount = b.getInt(R.styleable.XpListPopupWindow_asp_maxItemCount, mMaxItemCount);
        setMaxItemCount(maxItemCount);

        b.recycle();

        mPopup = new XpAppCompatPopupWindow(context, attrs, defStyleAttr);
        mPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
    }

    /**
     * This method exists for compatibility reasons.
     * In version 1.x.x there was only {@code asp_simpleMenuWidthUnit} attribute and
     * other values were inferred from its value.
     */
    private void initWidth(final int width, final int maxWidth, float widthUnit) {
        setWidth(width);
        setMaxWidth(maxWidth);
        setWidthUnit(widthUnit);
    }

    // TODO Do we want this public? What about setTextAlignment and setTextDirection?
    void setLayoutDirection(int layoutDirection) {
        if (layoutDirection != LayoutDirection.LTR && layoutDirection != LayoutDirection.RTL) {
            throw new IllegalArgumentException("Layout direction must be LTR or RTL.");
        }
        if (mLayoutDirection != layoutDirection) {
            mLayoutDirection = layoutDirection;

            final int temp = mMargins.left;
            mMargins.left = mMargins.right;
            mMargins.right = temp;
        }
    }

    /**
     * Sets the adapter that provides the data and the views to represent the data
     * in this popup window.
     *
     * @param adapter The adapter to use to create this window's content.
     */
    public void setAdapter(@Nullable ListAdapter adapter) {
        if (mObserver == null) {
            mObserver = new PopupDataSetObserver();
        } else if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mObserver);
        }
        mAdapter = adapter;
        if (mAdapter != null) {
            adapter.registerDataSetObserver(mObserver);
        }

        if (mDropDownList != null) {
            mDropDownList.setAdapter(mAdapter);
        }
        mListMeasureDirty = true;
    }

    /**
     * Set where the optional prompt view should appear. The default is
     * {@link #POSITION_PROMPT_ABOVE}.
     *
     * @param position A position constant declaring where the prompt should be displayed.
     * @see #POSITION_PROMPT_ABOVE
     * @see #POSITION_PROMPT_BELOW
     */
    public void setPromptPosition(int position) {
        mPromptPosition = position;
    }

    /**
     * @return Where the optional prompt view should appear.
     * @see #POSITION_PROMPT_ABOVE
     * @see #POSITION_PROMPT_BELOW
     */
    public int getPromptPosition() {
        return mPromptPosition;
    }

    /**
     * Set whether this window should be modal when shown.
     * <p/>
     * <p>If a popup window is modal, it will receive all touch and key input.
     * If the user touches outside the popup window's content area the popup window
     * will be dismissed.
     *
     * @param modal {@code true} if the popup window should be modal, {@code false} otherwise.
     */
    public void setModal(boolean modal) {
        mModal = modal;
        mPopup.setFocusable(modal);
    }

    /**
     * Returns whether the popup window will be modal when shown.
     *
     * @return {@code true} if the popup window will be modal, {@code false} otherwise.
     */
    public boolean isModal() {
        return mModal;
    }

    /**
     * Forces outside touches to be ignored. Normally if {@link #isDropDownAlwaysVisible()} is
     * false, we allow outside touch to dismiss the dropdown. If this is set to true, then we
     * ignore outside touch even when the drop down is not set to always visible.
     *
     * @hide Used only by AutoCompleteTextView to handle some internal special cases.
     */
    @RestrictTo(LIBRARY_GROUP)
    public void setForceIgnoreOutsideTouch(boolean forceIgnoreOutsideTouch) {
        mForceIgnoreOutsideTouch = forceIgnoreOutsideTouch;
    }

    /**
     * Sets whether the drop-down should remain visible under certain conditions.
     * <p/>
     * The drop-down will occupy the entire screen below {@link #getAnchorView} regardless
     * of the size or content of the list.  {@link #getBackground()} will fill any space
     * that is not used by the list.
     *
     * @param dropDownAlwaysVisible Whether to keep the drop-down visible.
     * @hide Only used by AutoCompleteTextView under special conditions.
     */
    @RestrictTo(LIBRARY_GROUP)
    public void setDropDownAlwaysVisible(boolean dropDownAlwaysVisible) {
        mDropDownAlwaysVisible = dropDownAlwaysVisible;
    }

    /**
     * @return Whether the drop-down is visible under special conditions.
     * @hide Only used by AutoCompleteTextView under special conditions.
     */
    @RestrictTo(LIBRARY_GROUP)
    public boolean isDropDownAlwaysVisible() {
        return mDropDownAlwaysVisible;
    }

    /**
     * Sets the operating mode for the soft input area.
     *
     * @param mode The desired mode, see
     * {@link WindowManager.LayoutParams#softInputMode}
     * for the full list
     * @see WindowManager.LayoutParams#softInputMode
     * @see #getSoftInputMode()
     */
    public void setSoftInputMode(int mode) {
        mPopup.setSoftInputMode(mode);
    }

    /**
     * Returns the current value in {@link #setSoftInputMode(int)}.
     *
     * @see #setSoftInputMode(int)
     * @see WindowManager.LayoutParams#softInputMode
     */
    public int getSoftInputMode() {
        return mPopup.getSoftInputMode();
    }

    /**
     * Sets a drawable to use as the list item selector.
     *
     * @param selector List selector drawable to use in the popup.
     */
    public void setListSelector(@Nullable Drawable selector) {
        mDropDownListHighlight = selector;
        mListMeasureDirty = true;
    }

    /**
     * @return The background drawable for the popup window.
     */
    @Nullable
    public Drawable getBackground() {
        return mPopup.getBackground();
    }

    /**
     * Sets a drawable to be the background for the popup window.
     *
     * @param d A drawable to set as the background.
     */
    public void setBackgroundDrawable(@Nullable Drawable d) {
        mPopup.setBackgroundDrawable(d);
        mListMeasureDirty = true;
    }

    /**
     * Set an animation style to use when the popup window is shown or dismissed.
     *
     * @param animationStyle Animation style to use.
     */
    public void setAnimationStyle(int animationStyle) {
        mPopup.setAnimationStyle(animationStyle);
    }

    /**
     * Returns the animation style that will be used when the popup window is shown or dismissed.
     *
     * @return Animation style that will be used.
     */
    @StyleRes
    public int getAnimationStyle() {
        return mPopup.getAnimationStyle();
    }

    /**
     * Returns the view that will be used to anchor this popup.
     *
     * @return The popup's anchor view
     */
    @Nullable
    public View getAnchorView() {
        return mDropDownAnchorView;
    }

    /**
     * Sets the popup's anchor view. This popup will always be positioned relative to the anchor
     * view when shown.
     *
     * @param anchor The view to use as an anchor.
     */
    public void setAnchorView(@Nullable View anchor) {
        if (mDropDownAnchorView != anchor) {
            mDropDownAnchorView = anchor;
            mListMeasureDirty = true; // BoundsView may be derived.
        }
    }

    @Nullable
    public View getBoundsView() {
        return mDropDownBoundsView;
    }

    public void setBoundsView(@Nullable View bounds) {
        if (mDropDownBoundsView != bounds) {
            mDropDownBoundsView = bounds;
            mListMeasureDirty = true;
        }
    }

    /**
     * @return The horizontal offset of the popup from its anchor in pixels.
     */
    public int getHorizontalOffset() {
        return mDropDownHorizontalOffset;
    }

    /**
     * Set the horizontal offset of this popup from its anchor view in pixels.
     *
     * @param offset The horizontal offset of the popup from its anchor.
     */
    public void setHorizontalOffset(int offset) {
        mDropDownHorizontalOffset = offset;
    }

    /**
     * @return The vertical offset of the popup from its anchor in pixels.
     */
    public int getVerticalOffset() {
        return mDropDownVerticalOffset;
    }

    /**
     * Set the vertical offset of this popup from its anchor view in pixels.
     *
     * @param offset The vertical offset of the popup from its anchor.
     */
    public void setVerticalOffset(int offset) {
        mDropDownVerticalOffset = offset;
    }

    /**
     * Specifies the anchor-relative bounds of the popup's transition
     * epicenter.
     *
     * @param bounds anchor-relative bounds
     */
    @RestrictTo(LIBRARY_GROUP)
    public void setEpicenterBounds(@Nullable Rect bounds) {
        mEpicenterBounds = bounds;
    }

    /**
     * Set the gravity of the dropdown list. This is commonly used to
     * set gravity to START or END for alignment with the anchor.
     *
     * @param gravity Gravity value to use
     */
    public void setDropDownGravity(int gravity) {
        mDropDownGravity = gravity;
    }

    public int getDropDownGravity() {
        if (mDropDownGravity == Gravity.NO_GRAVITY) {
            return Gravity.TOP | GravityCompat.START;
        }
        return mDropDownGravity;
    }

    /**
     * @return The width of the popup window in pixels.
     */
    public int getWidth() {
        return mDropDownWidth;
    }

    public int getMaxWidth() {
        return mDropDownMaxWidth;
    }

    @Deprecated
    @Dimension
    public float getPreferredWidthUnit() {
        return mDropDownPreferredWidthUnit;
    }

    /**
     * @return Min width unit size.
     */
    @Dimension
    public float getWidthUnit() {
        return mDropDownPreferredWidthUnit;
    }

    /**
     * Sets the <i>preferred</i> width of the popup window in pixels.
     * Can also be {@link #WIDTH_MATCH_CONSTRAINT} or {@link #WIDTH_WRAP_CONTENT}
     * or {@link #WIDTH_WRAP_CONTENT_UNIT}.
     *
     * @param width Preferred width of the popup window.
     */
    public void setWidth(int width) {
        if (width < -3) {
            throw new IllegalArgumentException("width must be a dimension or match_constraint or wrap_content or wrap_content_unit.");
        }
        if (mDropDownWidth != width) {
            mDropDownWidth = width;
            mListMeasureDirty = true;
        }
    }

    /**
     * Sets the <i>maximum</i> width of the popup menu in pixels.
     * Can also be {@link #MAX_WIDTH_FIT_SCREEN} or {@link #MAX_WIDTH_FIT_ANCHOR}.
     *
     * @param maxWidth Maximum width of the popup window.
     */
    public void setMaxWidth(int maxWidth) {
        if (maxWidth < -2) {
            throw new IllegalArgumentException("maxWidth must be a dimension or fit_screen or fit_anchor.");
        }
        if (mDropDownMaxWidth != maxWidth) {
            mDropDownMaxWidth = maxWidth;
            mListMeasureDirty = true;
        }
    }

    /**
     * @see #setWidthUnit(float)
     */
    @Deprecated
    public void setPreferredWidthUnit(@Dimension float unit) {
        setWidthUnit(unit);
    }

    /**
     * @param widthUnit When {@link #getWidth()} is set to {@link #WIDTH_WRAP_CONTENT_UNIT}
     * popup width will be
     * <ul>
     * <li>at least as wide as its content rounded up to a multiple of {@code widthUnit},</li>
     * <li>at least as wide as {@code widthUnit * 1.5},</li>
     * <li>limited by {@link #getMaxWidth()}.</li>
     * </ul>
     * @see #WIDTH_WRAP_CONTENT_UNIT
     */
    public void setWidthUnit(@Dimension float widthUnit) {
        if (widthUnit < 0) {
            throw new IllegalArgumentException("widthUnit must be a dimension greater than zero.");
        }
        if (mDropDownPreferredWidthUnit != widthUnit) {
            mDropDownPreferredWidthUnit = widthUnit;
            mListMeasureDirty = true;
        }
    }

    /**
     * Sets the width of the popup window by the size of its content. The final width may be
     * larger to accommodate styled window dressing.
     *
     * @param width Desired width of content in pixels.
     */
    // TODO Do we need this? At least better document diff between this and setWidth()
    public void setContentWidth(int width) {
        Drawable popupBackground = mPopup.getBackground();
        if (popupBackground != null) {
            popupBackground.getPadding(mTempRect);
            setWidth(mTempRect.left + mTempRect.right + width);
        } else {
            setWidth(width);
        }
    }

    /**
     * @return The height of the popup window in pixels.
     */
    public int getHeight() {
        return mDropDownHeight;
    }

    /**
     * Sets the height of the popup window in pixels. Can also be {@link #MATCH_PARENT}.
     *
     * @param height Height of the popup window.
     * @throws IllegalArgumentException if height is set to negative value
     */
    public void setHeight(int height) {
        if (height < 0 && WRAP_CONTENT != height
                && MATCH_PARENT != height) {
            throw new IllegalArgumentException(
                    "Invalid height. Must be a positive value, MATCH_PARENT, or WRAP_CONTENT.");
        }
        if (mDropDownHeight != height) {
            mDropDownHeight = height;
            mListMeasureDirty = true;
        }
    }

    /**
     * @param maxItemCount Popup menu will adjust its height to display at most this many items.
     */
    public void setMaxItemCount(int maxItemCount) {
        if (maxItemCount == 0 || maxItemCount < -1) {
            throw new IllegalArgumentException("Max length must be = -1 or > 0.");
        }
        if (mMaxItemCount != maxItemCount) {
            mMaxItemCount = maxItemCount;
            mListMeasureDirty = true;
        }
    }

    /**
     * Set the layout type for this popup window.
     * <p/>
     * See {@link WindowManager.LayoutParams#type} for possible values.
     *
     * @param layoutType Layout type for this window.
     * @see WindowManager.LayoutParams#type
     */
    public void setWindowLayoutType(int layoutType) {
        mDropDownWindowLayoutType = layoutType;
    }

    /**
     * Sets a listener to receive events when a list item is clicked.
     *
     * @param clickListener Listener to register
     * @see ListView#setOnItemClickListener(AdapterView.OnItemClickListener)
     */
    public void setOnItemClickListener(@Nullable AdapterView.OnItemClickListener clickListener) {
        mItemClickListener = clickListener;
        XpDropDownListView list = mDropDownList;
        if (list != null) {
            list.setOnItemClickListener(clickListener);
        }
    }

    /**
     * Sets a listener to receive events when a list item is selected.
     *
     * @param selectedListener Listener to register.
     * @see ListView#setOnItemSelectedListener(AdapterView.OnItemSelectedListener)
     */
    public void setOnItemSelectedListener(@Nullable AdapterView.OnItemSelectedListener selectedListener) {
        mItemSelectedListener = selectedListener;
        final XpDropDownListView list = mDropDownList;
        if (list != null) {
            list.setOnItemSelectedListener(selectedListener);
        }
    }

    /**
     * Set a view to act as a user prompt for this popup window. Where the prompt view will appear
     * is controlled by {@link #setPromptPosition(int)}.
     *
     * @param prompt View to use as an informational prompt.
     */
    public void setPromptView(@Nullable View prompt) {
        boolean showing = isShowing();
        if (showing) {
            removePromptView();
        }
        mPromptView = prompt;
        mListMeasureDirty = true;
        if (showing) {
            show();
        }
    }

    /**
     * Post a {@link #show()} call to the UI thread.
     */
    public void postShow() {
        mHandler.post(mShowDropDownRunnable);
    }

    /**
     * Show the popup list. If the list is already showing, this method
     * will do nothing.
     */
    @MainThread
    @Override
    public void show() {
        final int height;
        if (mDropDownList == null || mListMeasureDirty) {
            height = buildDropDown();
        } else {
            height = mListMeasuredHeight;
        }
        final int widthSpec = getListWidthSpec();

        boolean noInputMethod = isInputMethodNotNeeded();
        PopupWindowCompat.setWindowLayoutType(mPopup, mDropDownWindowLayoutType);

        final int marginsLeft = mMargins.left;
        final int marginsTop = mMargins.top;
        final int marginsBottom = mMargins.bottom;
        final int marginsRight = mMargins.right;

        getBackgroundPadding(mTempRect);
        final int backgroundLeft = mTempRect.left;
        final int backgroundTop = mTempRect.top;
        final int backgroundBottom = mTempRect.bottom;
        final int backgroundRight = mTempRect.right;

        int verticalOffset = mDropDownVerticalOffset;
        int horizontalOffset = mDropDownHorizontalOffset;

        final boolean rightAligned = GravityCompat.getAbsoluteGravity(getDropDownGravity() & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK, mLayoutDirection) == Gravity.RIGHT;
        final boolean leftAligned = !rightAligned;

        final int anchorWidth = mDropDownAnchorView.getWidth();
        final int anchorHeight = mDropDownAnchorView.getHeight();

        getLocationInWindow(mDropDownAnchorView, mTempLocation);
        final int anchorLeft = mTempLocation[0];
        final int anchorRight = anchorLeft + anchorWidth;
        final int anchorTop = mTempLocation[1];
        final int anchorBottom = anchorTop + anchorHeight;

        final int bottomDecorations = getWindowFrame(mDropDownAnchorView, noInputMethod, mTempRect);
        final int windowLeft = mTempRect.left;
        final int windowRight = mTempRect.right;
        final int windowTop = mTempRect.top;
        final int windowBottom = mTempRect.bottom;

        final int windowWidth = windowRight - windowLeft;
        final int windowHeight = windowBottom - windowTop;

        getBoundsInWindow(mTempRect);
        final int boundsTop = mTempRect.top;
        final int boundsRight = mTempRect.right;
        final int boundsLeft = mTempRect.left;
        final int boundsBottom = mTempRect.bottom;

        final int screenRight = windowRight - (marginsRight - backgroundRight) - boundsRight;
        final int screenLeft = windowLeft + (marginsLeft - backgroundLeft) + boundsLeft;
        final int screenBottom = windowBottom - (marginsBottom - backgroundBottom) - boundsBottom;
        final int screenTop = windowTop + (marginsTop - backgroundTop) + boundsTop;

        final int screenWidth = screenRight - screenLeft;
        final int screenHeight = screenBottom - screenTop;

        // Preferred popup height.
        int preferredHeight = getMaxAvailableHeight(mDropDownAnchorView, noInputMethod) + backgroundTop + backgroundBottom;
        preferredHeight -= marginsTop - backgroundTop;
        preferredHeight -= marginsBottom - backgroundBottom;

        final int limitHeight = Math.min(screenHeight, preferredHeight);

        final int heightSpec;
        if (mDropDownHeight == MATCH_PARENT) {
            heightSpec = limitHeight;
        } else if (mDropDownHeight == WRAP_CONTENT) {
            heightSpec = Math.min(height, limitHeight);
        } else {
            heightSpec = Math.min(mDropDownHeight, limitHeight);
        }

//        verticalOffset -= bottomDecorations;

        // Preferred vertical offset is counted from the bottom of the anchor view.
        verticalOffset += anchorTop + anchorHeight;

        if (verticalOffset < screenTop) {
            verticalOffset = screenTop;
        } else if (verticalOffset + heightSpec > screenBottom) {
            verticalOffset = screenBottom - heightSpec;
        }

        if (leftAligned) {
            horizontalOffset += anchorLeft - backgroundLeft;
        } else {
            horizontalOffset += anchorRight - widthSpec + backgroundRight;
        }

        if (horizontalOffset < screenLeft) {
            horizontalOffset = screenLeft;
        } else if (horizontalOffset + widthSpec > screenRight) {
            horizontalOffset = screenRight - widthSpec;
        }

        if (mPopup.isShowing()) {
            mPopup.setOutsideTouchable(!mForceIgnoreOutsideTouch && !mDropDownAlwaysVisible);
            mPopup.update(
                    horizontalOffset, verticalOffset,
                    (widthSpec < 0) ? -1 : widthSpec, (heightSpec < 0) ? -1 : heightSpec);
        } else {
            mPopup.setWidth(widthSpec);
            mPopup.setHeight(heightSpec);
            mPopup.setClippingEnabled(false);

            // use outside touchable to dismiss drop down when touching outside of it, so
            // only set this if the dropdown is not always visible
            mPopup.setOutsideTouchable(!mForceIgnoreOutsideTouch && !mDropDownAlwaysVisible);
            mPopup.setTouchInterceptor(mTouchInterceptor);
            setEpicenterBoundsInternal(mEpicenterBounds);

            // We handle gravity manually. Just as everything else.
            mPopup.showAtLocation(getAnchorView(), Gravity.NO_GRAVITY, horizontalOffset, verticalOffset);

            mDropDownList.setSelection(ListView.INVALID_POSITION);

            if (!mModal || mDropDownList.isInTouchMode()) {
                clearListSelection();
            }
            if (!mModal) {
                mHandler.post(mHideSelector);
            }

            mComputedPopupY = verticalOffset;
        }
    }

    private int mComputedPopupY;

    private int getListWidthSpec() {
        final int displayWidth = mContext.getResources().getDisplayMetrics().widthPixels;

        final int margins = mMargins.left + mMargins.right;
        final int paddings = getBackgroundHorizontalPadding();
        final int mps = margins - paddings;

        final int widthSpec;
        if (mDropDownWidth == WIDTH_MATCH_CONSTRAINT) {
            // The call to PopupWindow's update method below can accept -1 for any
            // value you do not want to update.
            if (mDropDownMaxWidth == MAX_WIDTH_FIT_SCREEN) {
                widthSpec = displayWidth - mps;//-1;
            } else if (mDropDownMaxWidth == MAX_WIDTH_FIT_ANCHOR) {
                widthSpec = getAnchorView().getWidth() - mps;
            } else {
                widthSpec = mDropDownMaxWidth - mps;
            }
        } else if (mDropDownWidth == WIDTH_WRAP_CONTENT) {
            if (mDropDownMaxWidth < 0) { // MAX_WIDTH_FIT_SCREEN or MAX_WIDTH_FIT_ANCHOR
                widthSpec = getAnchorView().getWidth() - mps;
            } else {
                widthSpec = mDropDownMaxWidth - mps;
            }
        } else if (mDropDownWidth == WIDTH_WRAP_CONTENT_UNIT) {
            int preferredWidth = mDropDownList.compatMeasureContentWidth();
            if (mDropDownPreferredWidthUnit > 0) {
                int units = (int) Math.ceil(preferredWidth / mDropDownPreferredWidthUnit);
                if (units == 1) {
                    preferredWidth = (int) (1.5f * mDropDownPreferredWidthUnit);
                } else {
                    preferredWidth = (int) (units * mDropDownPreferredWidthUnit);
                }
                preferredWidth += paddings;
            }
            if (mDropDownMaxWidth < 0) { // MAX_WIDTH_FIT_SCREEN or MAX_WIDTH_FIT_ANCHOR
                int anchorWidthTemp = getAnchorView().getWidth() - mps;
                if (preferredWidth > anchorWidthTemp) {
                    if (mDropDownMaxWidth == MAX_WIDTH_FIT_SCREEN) {
                        widthSpec = Math.min(preferredWidth, displayWidth - mps);//-1;
                    } else { // MAX_WIDTH_FIT_ANCHOR
                        widthSpec = anchorWidthTemp;
                    }
                } else {
                    widthSpec = preferredWidth;
                }
            } else {
                if (preferredWidth > mDropDownMaxWidth - mps) {
                    widthSpec = mDropDownMaxWidth - mps;
                } else {
                    widthSpec = preferredWidth;
                }
            }
        } else {
            if (mDropDownMaxWidth < 0) { // MAX_WIDTH_FIT_SCREEN or MAX_WIDTH_FIT_ANCHOR
                int anchorWidthTemp = getAnchorView().getWidth() - mps;
                if (mDropDownMaxWidth == WIDTH_WRAP_CONTENT && mDropDownWidth > anchorWidthTemp) {
                    widthSpec = anchorWidthTemp;
                } else { // MAX_WIDTH_FIT_ANCHOR
                    widthSpec = mDropDownWidth;
                }
            } else {
                if (mDropDownWidth > mDropDownMaxWidth - mps) {
                    widthSpec = mDropDownMaxWidth - mps;
                } else {
                    widthSpec = mDropDownWidth;
                }
            }
        }

        return widthSpec;
    }

    private int getBackgroundHorizontalPadding() {
        Drawable background = mPopup.getBackground();
        if (background != null) {
            background.getPadding(mTempRect);
            return mTempRect.left + mTempRect.right;
        }
        return 0;
    }

    private void getBackgroundPadding(@NonNull Rect out) {
        Drawable background = mPopup.getBackground();
        if (background != null) {
            background.getPadding(out);
        } else {
            out.setEmpty();
        }
    }

    @Deprecated
    private int getBackgroundLeftPadding() {
        Drawable background = mPopup.getBackground();
        if (background != null) {
            background.getPadding(mTempRect);
            return mTempRect.left;
        }
        return 0;
    }

    @Deprecated
    private int getBackgroundRightPadding() {
        Drawable background = mPopup.getBackground();
        if (background != null) {
            background.getPadding(mTempRect);
            return mTempRect.right;
        }
        return 0;
    }

    @Deprecated
    private int getBackgroundBottomPadding() {
        Drawable background = mPopup.getBackground();
        if (background != null) {
            background.getPadding(mTempRect);
            return mTempRect.bottom;
        }
        return 0;
    }

    @Deprecated
    private int getBackgroundTopPadding() {
        Drawable background = mPopup.getBackground();
        if (background != null) {
            background.getPadding(mTempRect);
            return mTempRect.top;
        }
        return 0;
    }

    private int getBackgroundVerticalPadding() {
        Drawable background = mPopup.getBackground();
        if (background != null) {
            background.getPadding(mTempRect);
            return mTempRect.top + mTempRect.bottom;
        }
        return 0;
    }

    /**
     * Dismiss the popup window.
     */
    @Override
    public void dismiss() {
        mPopup.dismiss();
        removePromptView();
        mPopup.setContentView(null);
        mDropDownList = null;
        mHandler.removeCallbacks(mResizePopupRunnable);
    }

    /**
     * Set a listener to receive a callback when the popup is dismissed.
     *
     * @param listener Listener that will be notified when the popup is dismissed.
     */
    public void setOnDismissListener(@Nullable PopupWindow.OnDismissListener listener) {
        mPopup.setOnDismissListener(listener);
    }

    private void removePromptView() {
        if (mPromptView != null) {
            final ViewParent parent = mPromptView.getParent();
            if (parent instanceof ViewGroup) {
                final ViewGroup group = (ViewGroup) parent;
                group.removeView(mPromptView);
            }
        }
    }

    /**
     * Control how the popup operates with an input method: one of
     * {@link #INPUT_METHOD_FROM_FOCUSABLE}, {@link #INPUT_METHOD_NEEDED},
     * or {@link #INPUT_METHOD_NOT_NEEDED}.
     * <p>
     * <p>If the popup is showing, calling this method will take effect only
     * the next time the popup is shown or through a manual call to the {@link #show()}
     * method.</p>
     *
     * @see #getInputMethodMode()
     * @see #show()
     */
    public void setInputMethodMode(int mode) {
        mPopup.setInputMethodMode(mode);
    }

    /**
     * Return the current value in {@link #setInputMethodMode(int)}.
     *
     * @see #setInputMethodMode(int)
     */
    public int getInputMethodMode() {
        return mPopup.getInputMethodMode();
    }

    /**
     * Mark item on specified position checked, selected and positioned over anchor view.
     *
     * @param position Selected item index.
     */
    public void setSelection(int position) {
        setSelection(position, 0);
    }

    // TODO Make public?
    void setSelection(int position, int offsetY) {
        final XpDropDownListView list = mDropDownList;
        if (isShowing() && list != null) {
//            list.setListSelectionHidden(false);
            setSelectionOverAnchor(list, position, offsetY);

            if (list.getChoiceMode() != ListView.CHOICE_MODE_NONE) {
                list.setItemChecked(position, true);
            }
        }
    }

    private void setSelectionOverAnchor(
            @NonNull final XpDropDownListView list, final int position, final int offsetY) {
        // Assuming all items have the same height.
        final int itemHeight = getSelectedItemViewHeight(position);

        // Before setting selection make sure list padding is resolved.
        list.ensureListPaddingResolved();

        final int listTop = mComputedPopupY + getBackgroundTopPadding();
        final int listPaddingTop = list.getListPaddingTop();

        final View anchor = getAnchorView();
        anchor.getLocationOnScreen(mTempLocation);
        final int anchorTop = mTempLocation[1];
        final int anchorPaddingTop = anchor.getPaddingTop();
        final int anchorHeight = anchor.getHeight() - anchorPaddingTop - anchor.getPaddingBottom();
        final int anchorInset = (anchorHeight - itemHeight) / 2 + anchorPaddingTop;

        final int realOffsetY = anchorTop - listTop + anchorInset
                + offsetY // Apply user supplied offset.
                - listPaddingTop; // Negate any ListView enforced offset.
        list.setSelectionFromTop(position, realOffsetY);

        ensureSelectionAtBottomVisible(list, position);
    }

    private void ensureSelectionAtBottomVisible(@NonNull final XpDropDownListView list,
                                                final int selectionAdapterPosition) {

        list.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(final View v, final int left, final int top, final int right,
                                       final int bottom, final int oldLeft, final int oldTop,
                                       final int oldRight, final int oldBottom) {

                list.removeOnLayoutChangeListener(this);

                final int lastVisibleAdapterPosition = list.getLastVisiblePosition();
                final int lastVisibleLayoutPosition = list.getChildCount() - 1;
                final int selectionLayoutPosition = lastVisibleLayoutPosition -
                        (lastVisibleAdapterPosition - selectionAdapterPosition);

                final View child = list.getChildAt(selectionLayoutPosition);
                if (child != null) {
                    // Don't attempt to offset based on invalid input. This shouldn't happen.

                    final int childBottom = child.getBottom();
                    final int childHeight = child.getHeight();

                    final int listHeight = list.getHeight();
                    if (childHeight < listHeight) {
                        // Don't attempt to offset if it wouldn't fit anyway. This could happen.

                        final int listBottom = listHeight - list.getListPaddingBottom();
                        if (childBottom > listBottom) {
                            ListViewCompat.scrollListBy(list, childBottom - listBottom);
                        }
                    }
                }
            }
        });
    }

    private int getSelectedItemViewHeight(final int position) {
        if (mMeasuredSelectedItemPosition == position) {
            return mMeasuredSelectedItemViewHeight;
        }
        return measureItem(position);
    }

    private int mMeasuredPreferredVerticalOffset = -1;
    private int mMeasuredSelectedItemViewHeight = -1;
    private int mMeasuredSelectedItemPosition = -1;

    /**
     * Get cached preferred vertical offset of popup window top from anchor bottom so that selected
     * item in the popup window is drawn precisely over the anchor.
     *
     * To get meaningful data the value must first be computed by
     * {@link #measurePreferredVerticalOffset(int)}.
     *
     * @return Cached measured vertical offset for popup window.
     * @see #measurePreferredVerticalOffset(int)
     * @see #getMeasuredSelectedItemViewHeight()
     */
    public int getMeasuredPreferredVerticalOffset() {
        return mMeasuredPreferredVerticalOffset;
    }

    /**
     * To get meaningful data the value must first be computed by
     * {@link #measurePreferredVerticalOffset(int)}.
     *
     * @return Measured height of selected item view.
     * @see #measurePreferredVerticalOffset(int)
     * @see #getMeasuredPreferredVerticalOffset()
     */
    public int getMeasuredSelectedItemViewHeight() {
        return mMeasuredSelectedItemViewHeight;
    }

    /**
     * Compute preferred vertical offset of popup window top from anchor bottom so that selected
     * item in the popup window is drawn precisely over the anchor. It also calculates selected
     * item view height.
     *
     * @param position Which item is supposed to be selected, and preferably aligned over anchor.
     * @see #getMeasuredPreferredVerticalOffset()
     * @see #getMeasuredSelectedItemViewHeight()
     */
    public void measurePreferredVerticalOffset(int position) {
        if (mDropDownList == null || mListMeasureDirty) {
            buildDropDown();
        }
        measurePreferredVerticalOffsetInternal(position);
    }

    /**
     * Measures popup offset and selected item scroll offset
     * for selected item to be positioned exactly over anchor.
     *
     * @param position Which item is supposed to be selected, and preferably aligned over anchor.
     * @return Measured vertical offset for popup window.
     * @see #measurePreferredVerticalOffset(int)
     * @see #getMeasuredPreferredVerticalOffset()
     * @see #getMeasuredSelectedItemViewHeight()
     * @deprecated This method pre-calculates multiple values. Use specialized accessor methods.
     */
    @Deprecated
    public int getPreferredVerticalOffset(int position) {
        measurePreferredVerticalOffset(position);
        return mMeasuredPreferredVerticalOffset;
    }

    private void measurePreferredVerticalOffsetInternal(int realPosition) {
        final View anchor = getAnchorView();
        final AbstractXpListPopupWindow popup = this;

        final Context context = anchor.getContext();

        // Shadow is emulated below Lollipop, we have to account for that.
        final int backgroundPaddingTop = getBackgroundTopPadding();

        // Center selected item over anchor view.
        if (realPosition < 0) realPosition = 0;

        int position = realPosition;

        // If we're allowed to show at most X items, cap position at X, prefer below.
        final int maxLength = mMaxItemCount;
        if (maxLength > 0) {
            position = Math.max(0, position - mAdapter.getCount() + maxLength);
        }

        // Before measuring list padding make sure it is resolved.
        mDropDownList.ensureListPaddingResolved();

        final int viewHeight = anchor.getHeight();
        final int dropDownListViewPaddingTop = mDropDownList.getListPaddingTop();
        final int selectedItemHeight = popup.measureItem(position);
        final int beforeSelectedItemHeight = popup.measureItems(realPosition - position, realPosition + 1);

        final int viewHeightAdjustedHalf = (viewHeight - anchor.getPaddingTop() - anchor.getPaddingBottom()) / 2 + anchor.getPaddingBottom();

        final int offset;
        if (selectedItemHeight >= 0 && beforeSelectedItemHeight >= 0) {
            offset = -(beforeSelectedItemHeight + (viewHeightAdjustedHalf - selectedItemHeight / 2) + dropDownListViewPaddingTop + backgroundPaddingTop);
        } else {
            final int height = XpSpinnerUtil.resolveDimensionPixelSize(context, R.attr.dropdownListPreferredItemHeight, 0);
            offset = -(height * (position + 1) + (viewHeightAdjustedHalf - height / 2) + dropDownListViewPaddingTop + backgroundPaddingTop);
        }

        mMeasuredPreferredVerticalOffset = offset;
        mMeasuredSelectedItemViewHeight = selectedItemHeight;
        mMeasuredSelectedItemPosition = position;
    }

    /**
     * Clear any current list selection.
     * Only valid when {@link #isShowing()} == {@code true}.
     */
    public void clearListSelection() {
        final XpDropDownListView list = mDropDownList;
        if (list != null) {
            // WARNING: Please read the comment where mListSelectionHidden is declared
//            list.setListSelectionHidden(true);
//            list.hideSelector();
            list.requestLayout();
        }
    }

    /**
     * @return {@code true} if the popup is currently showing, {@code false} otherwise.
     */
    @Override
    public boolean isShowing() {
        return mPopup.isShowing();
    }

    /**
     * @return {@code true} if this popup is configured to assume the user does not need
     * to interact with the IME while it is showing, {@code false} otherwise.
     */
    public boolean isInputMethodNotNeeded() {
        return mPopup.getInputMethodMode() == INPUT_METHOD_NOT_NEEDED;
    }

    /**
     * Perform an item click operation on the specified list adapter position.
     *
     * @param position Adapter position for performing the click
     * @return true if the click action could be performed, false if not.
     * (e.g. if the popup was not showing, this method would return false.)
     */
    public boolean performItemClick(int position) {
        if (isShowing()) {
            if (mItemClickListener != null) {
                final XpDropDownListView list = mDropDownList;
                final View child = list.getChildAt(position - list.getFirstVisiblePosition());
                final ListAdapter adapter = list.getAdapter();
                mItemClickListener.onItemClick(list, child, position, adapter.getItemId(position));
            }
            return true;
        }
        return false;
    }

    /**
     * @return The currently selected item or null if the popup is not showing.
     */
    @Nullable
    public Object getSelectedItem() {
        if (!isShowing()) {
            return null;
        }
        return mDropDownList.getSelectedItem();
    }

    /**
     * @return The position of the currently selected item or {@link ListView#INVALID_POSITION}
     * if {@link #isShowing()} == {@code false}.
     * @see ListView#getSelectedItemPosition()
     */
    public int getSelectedItemPosition() {
        if (!isShowing()) {
            return ListView.INVALID_POSITION;
        }
        return mDropDownList.getSelectedItemPosition();
    }

    /**
     * @return The ID of the currently selected item or {@link ListView#INVALID_ROW_ID}
     * if {@link #isShowing()} == {@code false}.
     * @see ListView#getSelectedItemId()
     */
    public long getSelectedItemId() {
        if (!isShowing()) {
            return ListView.INVALID_ROW_ID;
        }
        return mDropDownList.getSelectedItemId();
    }

    /**
     * @return The View for the currently selected item or null if
     * {@link #isShowing()} == {@code false}.
     * @see ListView#getSelectedView()
     */
    @Nullable
    public View getSelectedView() {
        if (!isShowing()) {
            return null;
        }
        return mDropDownList.getSelectedView();
    }

    /**
     * @return The {@link ListView} displayed within the popup window.
     * Only valid when {@link #isShowing()} == {@code true}.
     */
    @Nullable
    @Override
    public XpDropDownListView getListView() {
        return mDropDownList;
    }

    @NonNull
    XpDropDownListView createDropDownListView(@NonNull final Context context, final boolean hijackFocus) {
        final XpDropDownListView listView = new XpDropDownListView(context);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        return listView;
    }

    /**
     * The maximum number of list items that can be visible and still have
     * the list expand when touched.
     *
     * @param max Max number of items that can be visible and still allow the list to expand.
     */
    void setListItemExpandMax(int max) {
        mListItemExpandMaximum = max;
    }

    /**
     * Returns an {@link OnTouchListener} that can be added to the source view
     * to implement drag-to-open behavior. Generally, the source view should be
     * the same view that was passed to {@link #setAnchorView}.
     * <p>
     * When the listener is set on a view, touching that view and dragging
     * outside of its bounds will open the popup window. Lifting will select the
     * currently touched list item.
     * <p>
     * Example usage:
     * <pre>
     * ListPopupWindow myPopup = new ListPopupWindow(context);
     * myPopup.setAnchor(myAnchor);
     * OnTouchListener dragListener = myPopup.createDragToOpenListener(myAnchor);
     * myAnchor.setOnTouchListener(dragListener);
     * </pre>
     *
     * @param src the view on which the resulting listener will be set
     * @return a touch listener that controls drag-to-open behavior
     * @deprecated This feature is currently not supported.
     */
    @Deprecated
    @NonNull
    public OnTouchListener createDragToOpenListener(@NonNull View src) {
        return new ForwardingListener(src) {
            @NonNull
            @Override
            public AbstractXpListPopupWindow getPopup() {
                return AbstractXpListPopupWindow.this;
            }
        };
    }

    /**
     * <p>Builds the popup window's content and returns the height the popup
     * should have.</p>
     *
     * @return the content's height
     */
    private int buildDropDown() {
        ViewGroup dropDownView;
        int otherHeights = 0;

        if (mDropDownList == null) {
            Context context = mContext;

            /**
             * This Runnable exists for the sole purpose of checking if the view layout has got
             * completed and if so call showDropDown to display the drop down. This is used to show
             * the drop down as soon as possible after user opens up the search dialog, without
             * waiting for the normal UI pipeline to do it's job which is slower than this method.
             */
            mShowDropDownRunnable = new Runnable() {
                @Override
                public void run() {
                    // View layout should be all done before displaying the drop down.
                    View view = getAnchorView();
                    if (view != null && view.getWindowToken() != null) {
                        show();
                    }
                }
            };

            mDropDownList = createDropDownListView(context, !mModal);
            if (mDropDownListHighlight != null) {
                mDropDownList.setSelector(mDropDownListHighlight);
            }
            mDropDownList.setAdapter(mAdapter);
            mDropDownList.setOnItemClickListener(mItemClickListener);
            mDropDownList.setFocusable(true);
            mDropDownList.setFocusableInTouchMode(true);
            mDropDownList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(
                        AdapterView<?> parent, View view,
                        int position, long id) {

                    if (position != -1) {
                        XpDropDownListView dropDownList = mDropDownList;

                        if (dropDownList != null) {
//                            dropDownList.setListSelectionHidden(false);
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            mDropDownList.setOnScrollListener(mScrollListener);

            if (mItemSelectedListener != null) {
                mDropDownList.setOnItemSelectedListener(mItemSelectedListener);
            }

            dropDownView = mDropDownList;

            View hintView = mPromptView;
            if (hintView != null) {
                // if a hint has been specified, we accomodate more space for it and
                // add a text view in the drop down menu, at the bottom of the list
                LinearLayout hintContainer = new LinearLayout(context);
                hintContainer.setOrientation(LinearLayout.VERTICAL);

                LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f
                );

                switch (mPromptPosition) {
                    case POSITION_PROMPT_BELOW:
                        hintContainer.addView(dropDownView, hintParams);
                        hintContainer.addView(hintView);
                        break;

                    case POSITION_PROMPT_ABOVE:
                        hintContainer.addView(hintView);
                        hintContainer.addView(dropDownView, hintParams);
                        break;

                    default:
                        Log.e(TAG, "Invalid hint position " + mPromptPosition);
                        break;
                }

                // Measure the hint's height to find how much more vertical
                // space we need to add to the drop down's height.
                final int widthSize;
                final int widthMode;
                if (mDropDownWidth >= 0) {
                    widthMode = MeasureSpec.AT_MOST;
                    widthSize = mDropDownWidth > mDropDownMaxWidth ? mDropDownMaxWidth : mDropDownWidth;
//                    widthSize = mDropDownWidth;
                } else {
                    if (mDropDownMaxWidth >= 0) {
                        widthMode = MeasureSpec.AT_MOST;
                        widthSize = mDropDownMaxWidth;
                    } else {
                        widthMode = MeasureSpec.UNSPECIFIED;
                        widthSize = 0;
                    }
                }
                //noinspection Range
                final int widthSpec = MeasureSpec.makeMeasureSpec(widthSize, widthMode);
                final int heightSpec = MeasureSpec.UNSPECIFIED;
                hintView.measure(widthSpec, heightSpec);

                hintParams = (LinearLayout.LayoutParams) hintView.getLayoutParams();
                otherHeights = hintView.getMeasuredHeight() + hintParams.topMargin
                        + hintParams.bottomMargin;

                dropDownView = hintContainer;
            }

            mPopup.setContentView(dropDownView);
        } else {
            dropDownView = (ViewGroup) mPopup.getContentView();
            final View view = mPromptView;
            if (view != null) {
                LinearLayout.LayoutParams hintParams =
                        (LinearLayout.LayoutParams) view.getLayoutParams();
                otherHeights = view.getMeasuredHeight() + hintParams.topMargin
                        + hintParams.bottomMargin;
            }
        }

        // getMaxAvailableHeight() subtracts the padding, so we put it back
        // to get the available height for the whole window
        int padding = 0;
        Drawable background = mPopup.getBackground();
        if (background != null) {
            background.getPadding(mTempRect);
            padding = mTempRect.top + mTempRect.bottom;

            // If we don't have an explicit vertical offset, determine one from the window
            // background so that content will line up.
//            if (!mDropDownVerticalOffsetSet) {
//                mDropDownVerticalOffset = -mTempRect.top;
//            }
        } else {
            mTempRect.setEmpty();
        }

        final int verticalMargin = mMargins.top + mMargins.bottom;

        // Max height available on the screen for a popup.
        final boolean ignoreBottomDecorations =
                mPopup.getInputMethodMode() == PopupWindow.INPUT_METHOD_NOT_NEEDED;
//        final int maxHeight = getMaxAvailableHeight(getAnchorView(), mDropDownVerticalOffset, ignoreBottomDecorations);
        final int maxHeight = getMaxAvailableHeight(getAnchorView(), ignoreBottomDecorations);
        if (mDropDownAlwaysVisible || mDropDownHeight == MATCH_PARENT) {
            return maxHeight - verticalMargin + padding;
        }

        final int childWidthSpec;
        switch (mDropDownWidth) {
            case WIDTH_WRAP_CONTENT:
                childWidthSpec = MeasureSpec.makeMeasureSpec(
                        getAnchorView().getWidth() -
                                (mMargins.left + mMargins.right) -
                                (mTempRect.left + mTempRect.right),
                        MeasureSpec.AT_MOST);
                break;
            case WIDTH_MATCH_CONSTRAINT:
                childWidthSpec = MeasureSpec.makeMeasureSpec(
                        mContext.getResources().getDisplayMetrics().widthPixels -
                                (mMargins.left + mMargins.right) -
                                (mTempRect.left + mTempRect.right),
                        MeasureSpec.EXACTLY);
                break;
            case WIDTH_WRAP_CONTENT_UNIT:
                int widthSize;
                int widthMode;
                if (mDropDownMaxWidth >= 0) {
                    widthSize = mDropDownMaxWidth -
                            (mMargins.left + mMargins.right) -
                            (mTempRect.left + mTempRect.right);
                    widthMode = MeasureSpec.AT_MOST;
                    childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, widthMode);
                } else {
                    widthMode = MeasureSpec.AT_MOST;
                    if (mDropDownMaxWidth == WIDTH_WRAP_CONTENT) {
                        widthSize = getAnchorView().getWidth() -
                                (mMargins.left + mMargins.right) -
                                (mTempRect.left + mTempRect.right);
                    } else { // MATCH_CONSTRAINT
                        widthSize = mContext.getResources().getDisplayMetrics().widthPixels -
                                (mMargins.left + mMargins.right) -
                                (mTempRect.left + mTempRect.right);
                    }
                    childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, widthMode);
                }
                break;
            default:
                //noinspection Range
                childWidthSpec = MeasureSpec.makeMeasureSpec(mDropDownWidth, MeasureSpec.EXACTLY);
                break;
        }

        mDropDownList.ensureListPaddingResolved();
        final int listPadding = mDropDownList.getListPaddingTop() + mDropDownList.getListPaddingBottom();
        final int listContent = mDropDownList.measureHeightOfChildrenCompat(childWidthSpec,
                0, mMaxItemCount, maxHeight - otherHeights - verticalMargin - listPadding + padding, -1);
        // add padding only if the list has items in it, that way we don't show
        // the popup if it is not needed
        if (otherHeights > 0 || listContent > 0) otherHeights += padding + listPadding;

        final int result = listContent + otherHeights;
        mListMeasuredHeight = result;
        mListMeasureDirty = false;

        return result;
    }

    private class PopupDataSetObserver extends DataSetObserver {
        PopupDataSetObserver() {
        }

        @Override
        public void onChanged() {
            mListMeasureDirty = true;
            if (isShowing()) {
                // Resize the popup to fit new content
                show();
            }
        }

        @Override
        public void onInvalidated() {
            dismiss();
        }
    }

    private class ListSelectorHider implements Runnable {
        ListSelectorHider() {
        }

        @Override
        public void run() {
            clearListSelection();
        }
    }

    private class ResizePopupRunnable implements Runnable {
        ResizePopupRunnable() {
        }

        @Override
        public void run() {
            if (mDropDownList != null && ViewCompat.isAttachedToWindow(mDropDownList)
                    && mDropDownList.getCount() > mDropDownList.getChildCount()
                    && mDropDownList.getChildCount() <= mListItemExpandMaximum) {
                mPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
                mListMeasureDirty = true; // TODO Verify we need this.
                show();
            }
        }
    }

    private class PopupTouchInterceptor implements OnTouchListener {
        PopupTouchInterceptor() {
        }

        @Override
        public boolean onTouch(@NonNull View v, @NonNull MotionEvent event) {
            final int action = event.getAction();
            final int x = (int) event.getX();
            final int y = (int) event.getY();

            if (action == MotionEvent.ACTION_DOWN &&
                    mPopup != null && mPopup.isShowing() &&
                    (x >= 0 && x < mPopup.getWidth() && y >= 0 && y < mPopup.getHeight())) {
                mHandler.postDelayed(mResizePopupRunnable, EXPAND_LIST_TIMEOUT);
            } else if (action == MotionEvent.ACTION_UP) {
                mHandler.removeCallbacks(mResizePopupRunnable);
            }
            return false;
        }
    }

    private class PopupScrollListener implements ListView.OnScrollListener {
        @Override
        public void onScroll(
                AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == SCROLL_STATE_TOUCH_SCROLL &&
                    !isInputMethodNotNeeded() && mPopup.getContentView() != null) {
                mHandler.removeCallbacks(mResizePopupRunnable);
                mResizePopupRunnable.run();
            }
        }

    }

    private static boolean isConfirmKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER;
    }

    private void setEpicenterBoundsInternal(@Nullable Rect epicenterBounds) {
        if (sSetEpicenterBoundsMethod != null) {
            try {
                sSetEpicenterBoundsMethod.invoke(mPopup, epicenterBounds);
            } catch (Exception e) {
                Log.i(TAG, "Could not call setEpicenterBounds() on PopupWindow. Oh well.");
            }
        }
    }

    private int getMaxAvailableHeight(@NonNull View anchor, boolean ignoreBottomDecorations) {
        final View bounds = mDropDownBoundsView;
        if (bounds != null) {
            int returnedHeight = bounds.getHeight();
            returnedHeight -= getBackgroundVerticalPadding();
            return returnedHeight;
        }

        getWindowFrame(anchor, ignoreBottomDecorations, mTempRect);
        int returnedHeight = mTempRect.height();
        returnedHeight -= getBackgroundVerticalPadding();

        return returnedHeight;
    }

    private int getWindowFrame(
            @NonNull final View anchor, final boolean ignoreBottomDecorations, final Rect out) {
        int bottomDecorations = 0;
        anchor.getWindowVisibleDisplayFrame(out);
//        if (ignoreBottomDecorations) {
//            Resources res = anchor.getContext().getResources();
//            int bottomEdge = res.getDisplayMetrics().heightPixels;
//            bottomDecorations = bottomEdge - out.bottom;
//            out.bottom = bottomEdge;
//        }
        return bottomDecorations;
    }

    private void getLocationInWindow(@NonNull View anchor, @NonNull @Size(2) int[] out) {
        anchor.getLocationInWindow(out);
    }

    /**
     * @param out Margins relative to left, top, right and bottom of the window.
     */
    private void getBoundsInWindow(@NonNull Rect out) {
        final View bounds = mDropDownBoundsView;
        if (bounds != null) {
            bounds.getWindowVisibleDisplayFrame(mTempRect);
            final int windowTop = mTempRect.top;
            final int windowRight = mTempRect.right;
            final int windowLeft = mTempRect.left;
            final int windowBottom = mTempRect.bottom;

            bounds.getLocationInWindow(mTempLocation);
            final int boundsTop = mTempLocation[1];
            final int boundsLeft = mTempLocation[0];

            final int boundsHeight = bounds.getHeight();
            final int boundsWidth = bounds.getWidth();

            out.top = boundsTop - windowTop;
            out.left = boundsLeft - windowLeft;
            out.bottom = windowBottom - (boundsTop + boundsHeight);
            out.right = windowRight - (boundsLeft + boundsWidth);
            return;
        }

        out.set(0, 0, 0, 0);
    }
}
