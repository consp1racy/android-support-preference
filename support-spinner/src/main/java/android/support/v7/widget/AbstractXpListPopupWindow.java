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

package android.support.v7.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.Size;
import android.support.annotation.StyleRes;
import android.support.v4.text.TextUtilsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v7.view.menu.ShowableListMenu;
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

import static android.support.annotation.RestrictTo.Scope.LIBRARY;
import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

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
public abstract class AbstractXpListPopupWindow implements ShowableListMenu {
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
    private int mDropDownMaxLength = -1;

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

    /**
     * Alias for {@link ViewGroup.LayoutParams#MATCH_PARENT}.
     * If used to specify a popup width, the popup will match the width of the anchor view.
     * If used to specify a popup height, the popup will fill available space.
     */
    public static final int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;

    /**
     * Alias for {@link ViewGroup.LayoutParams#WRAP_CONTENT}.
     * If used to specify a popup width, the popup will use the width of its content.
     */
    public static final int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;

    public static final int PREFERRED = -3;

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
        this(context, null, R.attr.listPopupWindowStyle);
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
        int defaultMargin = Util.dpToPxOffset(context, 8);
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

        final int dropDownMaxLength = b.getInt(R.styleable.XpListPopupWindow_android_rowCount, mDropDownMaxLength);
        setDropDownMaxLength(dropDownMaxLength);

        b.recycle();

        mPopup = new XpAppCompatPopupWindow(context, attrs, defStyleAttr);
        mPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
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
    public void setListSelector(Drawable selector) {
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
    public void setEpicenterBounds(Rect bounds) {
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

    public float getPreferredWidthUnit() {
        return mDropDownPreferredWidthUnit;
    }

    /**
     * Sets the width of the popup window in pixels. Can also be {@link #MATCH_PARENT}
     * or {@link #WRAP_CONTENT}.
     *
     * @param width Width of the popup window.
     */
    public void setWidth(int width) {
        if (mDropDownWidth != width) {
            mDropDownWidth = width;
            mListMeasureDirty = true;
        }
    }

    public void setMaxWidth(int maxWidth) {
        if (mDropDownMaxWidth != maxWidth) {
            mDropDownMaxWidth = maxWidth;
            mListMeasureDirty = true;
        }
    }

    public void setPreferredWidthUnit(float unit) {
        if (mDropDownPreferredWidthUnit != unit) {
            mDropDownPreferredWidthUnit = unit;
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
        if (height < 0 && ViewGroup.LayoutParams.WRAP_CONTENT != height
                && ViewGroup.LayoutParams.MATCH_PARENT != height) {
            throw new IllegalArgumentException(
                    "Invalid height. Must be a positive value, MATCH_PARENT, or WRAP_CONTENT.");
        }
        if (mDropDownHeight != height) {
            mDropDownHeight = height;
            mListMeasureDirty = true;
        }
    }

    /**
     * @param dropDownMaxLength Max number of items that can be displayed in popup menu.
     */
    public void setDropDownMaxLength(int dropDownMaxLength) {
        if (dropDownMaxLength == 0 || dropDownMaxLength < -1) {
            throw new IllegalArgumentException("Max length must be = -1 or > 0.");
        }
        if (mDropDownMaxLength != dropDownMaxLength) {
            mDropDownMaxLength = dropDownMaxLength;
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
    public void setOnItemClickListener(AdapterView.OnItemClickListener clickListener) {
        mItemClickListener = clickListener;
        final XpDropDownListView list = mDropDownList;
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
    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener selectedListener) {
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
    @Override
    public void show() {
//        final int height = buildDropDown();
//        final int widthSpec = getListWidthSpec();

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

        final int anchorWidth = mDropDownAnchorView.getWidth();
        final int anchorHeight = mDropDownAnchorView.getHeight();

        getLocationInWindow(mDropDownAnchorView, mTempLocation);
        final int anchorLeft = mTempLocation[0];
        final int anchorRight = anchorLeft + anchorWidth;
        final int anchorTop = mTempLocation[1];
        final int anchorBottom = anchorTop + anchorHeight;

        final boolean rightAligned = GravityCompat.getAbsoluteGravity(getDropDownGravity() & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK, mLayoutDirection) == Gravity.RIGHT;
        if (rightAligned) {
            horizontalOffset += anchorWidth - widthSpec - (marginsRight - backgroundRight);
        } else {
            horizontalOffset += (marginsLeft - backgroundLeft);
        }

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
        final int screenWidth = screenRight - screenLeft;

        if (!rightAligned && windowWidth < anchorLeft + horizontalOffset + widthSpec) {
            // When right aligned due to insufficient space ignore negative horizontal offset.
            horizontalOffset = mDropDownHorizontalOffset < 0 ? 0 : mDropDownHorizontalOffset;
            horizontalOffset -= widthSpec - (windowWidth - anchorLeft);
            horizontalOffset -= marginsRight - backgroundRight;
        } else if (rightAligned && 0 > anchorLeft + horizontalOffset) {
            // When left aligned due to insufficient space ignore positive horizontal offset.
            horizontalOffset = mDropDownHorizontalOffset > 0 ? 0 : mDropDownHorizontalOffset;
            horizontalOffset -= anchorLeft;
            horizontalOffset += marginsLeft - backgroundLeft;
        }

        // Width spec should always be resolved to concrete value. widthSpec > 0;
        if (windowWidth < widthSpec + horizontalOffset + anchorLeft) {
            int diff = Math.abs(windowWidth - (widthSpec + horizontalOffset + anchorLeft));
            horizontalOffset -= diff;
        } else if (0 > anchorLeft + horizontalOffset) {
            int diff = Math.abs(horizontalOffset + anchorLeft);
            horizontalOffset += diff;
        }

        int maxHeight = getMaxAvailableHeight(mDropDownAnchorView, noInputMethod) + backgroundTop + backgroundBottom;
        int availableHeight = maxHeight;
//        availableHeight -= Math.max(0, marginsTop - backgroundTop);
//        availableHeight -= Math.max(0, marginsBottom - backgroundBottom);
        availableHeight -= marginsTop - backgroundTop;
        availableHeight -= marginsBottom - backgroundBottom;

        int limitHeight = Math.min(windowHeight, availableHeight);

        final int heightSpec;
        if (mDropDownHeight == ViewGroup.LayoutParams.MATCH_PARENT) {
            heightSpec = limitHeight;
        } else if (mDropDownHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            heightSpec = Math.min(height, limitHeight);
        } else {
            heightSpec = Math.min(mDropDownHeight, limitHeight);
        }

        final int screenBottom = windowBottom - (marginsBottom - backgroundBottom) - boundsBottom;
        final int screenTop = windowTop + (marginsTop - backgroundTop) + boundsTop;

        {
            // Position within bounds.

            final int popupTop = anchorBottom + verticalOffset;
            final int popupBottom = popupTop + heightSpec;
            final int popupHeight = popupBottom - popupTop;

            if (popupBottom > screenBottom) {
                verticalOffset -= (popupBottom - screenBottom);
            } else if (popupTop < screenTop) {
                verticalOffset += (screenTop - popupTop);
            }
        }

        {
            // Account for background padding.

            final int popupTop = anchorBottom + verticalOffset;
            final int popupBottom = popupTop + heightSpec;
            final int popupHeight = popupBottom - popupTop;

            if (windowBottom < popupBottom) {
                int diff = Math.abs(windowBottom - popupBottom);
                verticalOffset -= diff;
            } else if (windowTop > popupTop) {
                int diff = Math.abs(windowTop - popupTop);
                verticalOffset += diff;
            }
        }

//        verticalOffset -= bottomDecorations;
//        verticalOffset += Util.dpToPxOffset(mContext, 8);

        // TODO Optimize position calculation.
        // These two lines only exist so we can reuse the old calculation
        // which relied on showAsDropDown instead of showAtLocation.
        verticalOffset += anchorTop + anchorHeight;
        horizontalOffset += anchorLeft; // TODO RTL is broken. Spinner sample is broken.

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
        if (mDropDownWidth == ViewGroup.LayoutParams.MATCH_PARENT) {
            // The call to PopupWindow's update method below can accept -1 for any
            // value you do not want to update.
            if (mDropDownMaxWidth == MATCH_PARENT) {
                widthSpec = displayWidth - mps;//-1;
            } else if (mDropDownMaxWidth == WRAP_CONTENT) {
                widthSpec = getAnchorView().getWidth() - mps;
            } else {
                widthSpec = mDropDownMaxWidth - mps;
            }
        } else if (mDropDownWidth == ViewGroup.LayoutParams.WRAP_CONTENT) {
            if (mDropDownMaxWidth < 0) {
                widthSpec = getAnchorView().getWidth() - mps;
            } else {
                widthSpec = mDropDownMaxWidth - mps;
            }
        } else if (mDropDownWidth == PREFERRED) {
            int preferredWidth = mDropDownList.compatMeasureContentWidth() + getBackgroundHorizontalPadding();
            if (mDropDownPreferredWidthUnit > 0) {
                int units = (int) Math.ceil(preferredWidth / mDropDownPreferredWidthUnit);
                if (units == 1) {
                    preferredWidth = (int) (1.5f * mDropDownPreferredWidthUnit);
                } else {
                    preferredWidth = (int) (units * mDropDownPreferredWidthUnit);
                }
            }
            if (mDropDownMaxWidth < 0) {
                int anchorWidthTemp = getAnchorView().getWidth() - mps;
                if (preferredWidth > anchorWidthTemp) {
                    if (mDropDownMaxWidth == MATCH_PARENT) {
                        widthSpec = Math.min(preferredWidth, displayWidth - mps);//-1;
                    } else { // WRAP_CONTENT
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
            if (mDropDownMaxWidth < 0) {
                int anchorWidthTemp = getAnchorView().getWidth() - mps;
                if (mDropDownMaxWidth == WRAP_CONTENT && mDropDownWidth > anchorWidthTemp) {
                    widthSpec = anchorWidthTemp;
                } else {
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
    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
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
            list.setListSelectionHidden(false);
            setSelectionOverAnchor(list, position, offsetY);

            if (list.getChoiceMode() != ListView.CHOICE_MODE_NONE) {
                list.setItemChecked(position, true);
            }
        }
    }

//    @Deprecated
//    private static void setSelectionWithPaddingTop(@NonNull final XpDropDownListView list, final int position) {
//        // getListPaddingTop returns zero when popup is invoked for the first time
//        // and when it's invoked after making a selection in previous invokation.
//        final int realOffsetY = list.getPaddingTop() + list.mSelectionTopPadding - list.getListPaddingTop();
//        list.setSelectionFromTop(position, realOffsetY);
//    }

    private void setSelectionOverAnchor(final XpDropDownListView list, final int position, final int offsetY) {
        final View anchor = getAnchorView();

        final int listTop = mComputedPopupY + getBackgroundTopPadding();
        anchor.getLocationOnScreen(mTempLocation);
        final int anchorTop = mTempLocation[1];
        final int anchorPaddingTop = anchor.getPaddingTop();
        final int anchorHeight = anchor.getHeight() - anchorPaddingTop - anchor.getPaddingBottom();
        final int itemHeight = getSelectedItemViewHeight(position);
        final int anchorInset = (anchorHeight - itemHeight) / 2 + anchorPaddingTop;

        final int realOffsetY = anchorTop - listTop + anchorInset
                + offsetY // Apply user supplied offset.
                - list.getListPaddingTop(); // Negate any ListView enforced offset.
        list.setSelectionFromTop(position, realOffsetY);
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
     * @return Measured vertical offset for popup window.
     * @see #measurePreferredVerticalOffset(int)
     * @see #getMeasuredSelectedItemViewHeight()
     */
    public int getMeasuredPreferredVerticalOffset() {
        return mMeasuredPreferredVerticalOffset;
    }

    /**
     * @return Measured height of selected item view.
     * @see #measurePreferredVerticalOffset(int)
     * @see #getMeasuredPreferredVerticalOffset()
     */
    public int getMeasuredSelectedItemViewHeight() {
        return mMeasuredSelectedItemViewHeight;
    }

    /**
     * Measures popup offset and selected item scroll offset
     * for selected item to be positioned exactly over anchor.
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
        final int maxLength = mDropDownMaxLength;
        if (maxLength > 0) {
            position = Math.max(0, position - mAdapter.getCount() + maxLength);
        }

        final int viewHeight = anchor.getHeight();
        final int dropDownListViewPaddingTop = mDropDownList.getPaddingTop();
        final int selectedItemHeight = popup.measureItem(position);
        final int beforeSelectedItemHeight = popup.measureItems(realPosition - position, realPosition + 1);

        final int viewHeightAdjustedHalf = (viewHeight - anchor.getPaddingTop() - anchor.getPaddingBottom()) / 2 + anchor.getPaddingBottom();

        final int offset;
        if (selectedItemHeight >= 0 && beforeSelectedItemHeight >= 0) {
            offset = -(beforeSelectedItemHeight + (viewHeightAdjustedHalf - selectedItemHeight / 2) + dropDownListViewPaddingTop + backgroundPaddingTop);
        } else {
            final int height = Util.resolveDimensionPixelSize(context, R.attr.dropdownListPreferredItemHeight, 0);
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
            list.setListSelectionHidden(true);
            //list.hideSelector();
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
    @Override
    public XpDropDownListView getListView() {
        return mDropDownList;
    }

    @NonNull
    XpDropDownListView createDropDownListView(final Context context, final boolean hijackFocus) {
        final XpDropDownListView listView = new XpDropDownListView(context, hijackFocus);
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
     * Filter key down events. By forwarding key down events to this function,
     * views using non-modal ListPopupWindow can have it handle key selection of items.
     *
     * @param keyCode keyCode param passed to the host view's onKeyDown
     * @param event event param passed to the host view's onKeyDown
     * @return true if the event was handled, false if it was ignored.
     * @see #setModal(boolean)
     * @see #onKeyUp(int, KeyEvent)
     */
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        // when the drop down is shown, we drive it directly
        if (isShowing()) {
            // the key events are forwarded to the list in the drop down view
            // note that ListView handles space but we don't want that to happen
            // also if selection is not currently in the drop down, then don't
            // let center or enter presses go there since that would cause it
            // to select one of its items
            if (keyCode != KeyEvent.KEYCODE_SPACE
                    && (mDropDownList.getSelectedItemPosition() >= 0
                    || !isConfirmKey(keyCode))) {
                int curIndex = mDropDownList.getSelectedItemPosition();
                boolean consumed;

                final boolean below = !mPopup.isAboveAnchor();

                final ListAdapter adapter = mAdapter;

                boolean allEnabled;
                int firstItem = Integer.MAX_VALUE;
                int lastItem = Integer.MIN_VALUE;

                if (adapter != null) {
                    allEnabled = adapter.areAllItemsEnabled();
                    firstItem = allEnabled ? 0 :
                            mDropDownList.lookForSelectablePosition(0, true);
                    lastItem = allEnabled ? adapter.getCount() - 1 :
                            mDropDownList.lookForSelectablePosition(adapter.getCount() - 1, false);
                }

                if ((below && keyCode == KeyEvent.KEYCODE_DPAD_UP && curIndex <= firstItem) ||
                        (!below && keyCode == KeyEvent.KEYCODE_DPAD_DOWN && curIndex >= lastItem)) {
                    // When the selection is at the top, we block the key
                    // event to prevent focus from moving.
                    clearListSelection();
                    mPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
                    show();
                    return true;
                } else {
                    // WARNING: Please read the comment where mListSelectionHidden
                    //          is declared
                    mDropDownList.setListSelectionHidden(false);
                }

                consumed = mDropDownList.onKeyDown(keyCode, event);
                if (DEBUG) Log.v(TAG, "Key down: code=" + keyCode + " list consumed=" + consumed);

                if (consumed) {
                    // If it handled the key event, then the user is
                    // navigating in the list, so we should put it in front.
                    mPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
                    // Here's a little trick we need to do to make sure that
                    // the list view is actually showing its focus indicator,
                    // by ensuring it has focus and getting its window out
                    // of touch mode.
                    mDropDownList.requestFocusFromTouch();
                    show();

                    switch (keyCode) {
                        // avoid passing the focus from the text view to the
                        // next component
                        case KeyEvent.KEYCODE_ENTER:
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                        case KeyEvent.KEYCODE_DPAD_UP:
                            return true;
                    }
                } else {
                    if (below && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        // when the selection is at the bottom, we block the
                        // event to avoid going to the next focusable widget
                        if (curIndex == lastItem) {
                            return true;
                        }
                    } else if (!below && keyCode == KeyEvent.KEYCODE_DPAD_UP &&
                            curIndex == firstItem) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Filter key down events. By forwarding key up events to this function,
     * views using non-modal ListPopupWindow can have it handle key selection of items.
     *
     * @param keyCode keyCode param passed to the host view's onKeyUp
     * @param event event param passed to the host view's onKeyUp
     * @return true if the event was handled, false if it was ignored.
     * @see #setModal(boolean)
     */
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (isShowing() && mDropDownList.getSelectedItemPosition() >= 0) {
            boolean consumed = mDropDownList.onKeyUp(keyCode, event);
            if (consumed && isConfirmKey(keyCode)) {
                // if the list accepts the key events and the key event was a click, the text view
                // gets the selected item from the drop down as its content
                dismiss();
            }
            return consumed;
        }
        return false;
    }

    /**
     * Filter pre-IME key events. By forwarding {@link View#onKeyPreIme(int, KeyEvent)}
     * events to this function, views using ListPopupWindow can have it dismiss the popup
     * when the back key is pressed.
     *
     * @param keyCode keyCode param passed to the host view's onKeyPreIme
     * @param event event param passed to the host view's onKeyPreIme
     * @return true if the event was handled, false if it was ignored.
     * @see #setModal(boolean)
     */
    public boolean onKeyPreIme(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isShowing()) {
            // special case for the back key, we do not even try to send it
            // to the drop down list but instead, consume it immediately
            final View anchorView = mDropDownAnchorView;
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                KeyEvent.DispatcherState state = anchorView.getKeyDispatcherState();
                if (state != null) {
                    state.startTracking(event, this);
                }
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                KeyEvent.DispatcherState state = anchorView.getKeyDispatcherState();
                if (state != null) {
                    state.handleUpEvent(event);
                }
                if (event.isTracking() && !event.isCanceled()) {
                    dismiss();
                    return true;
                }
            }
        }
        return false;
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
     */
    public OnTouchListener createDragToOpenListener(View src) {
        return new ForwardingListener(src) {
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
                            dropDownList.setListSelectionHidden(false);
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
        if (mDropDownAlwaysVisible || mDropDownHeight == ViewGroup.LayoutParams.MATCH_PARENT) {
            return maxHeight - verticalMargin + padding;
        }

        final int childWidthSpec;
        switch (mDropDownWidth) {
            case ViewGroup.LayoutParams.WRAP_CONTENT:
                childWidthSpec = MeasureSpec.makeMeasureSpec(
                        getAnchorView().getWidth() -
                                (mMargins.left + mMargins.right) -
                                (mTempRect.left + mTempRect.right),
                        MeasureSpec.AT_MOST);
                break;
            case ViewGroup.LayoutParams.MATCH_PARENT:
                childWidthSpec = MeasureSpec.makeMeasureSpec(
                        mContext.getResources().getDisplayMetrics().widthPixels -
                                (mMargins.left + mMargins.right) -
                                (mTempRect.left + mTempRect.right),
                        MeasureSpec.EXACTLY);
                break;
            case PREFERRED:
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
                    if (mDropDownMaxWidth == WRAP_CONTENT) {
                        widthSize = getAnchorView().getWidth() -
                                (mMargins.left + mMargins.right) -
                                (mTempRect.left + mTempRect.right);
                    } else { // MATCH_PARENT
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

        final int listPadding = mDropDownList.getPaddingTop() + mDropDownList.getPaddingBottom();
        final int listContent = mDropDownList.measureHeightOfChildrenCompat(childWidthSpec,
                0, mDropDownMaxLength, maxHeight - otherHeights - verticalMargin - listPadding + padding, -1);
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
        public boolean onTouch(View v, MotionEvent event) {
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

    private void setEpicenterBoundsInternal(Rect epicenterBounds) {
        if (sSetEpicenterBoundsMethod != null) {
            try {
                sSetEpicenterBoundsMethod.invoke(mPopup, epicenterBounds);
            } catch (Exception e) {
                Log.i(TAG, "Could not call setEpicenterBounds() on PopupWindow. Oh well.");
            }
        }
    }

    private int getMaxAvailableHeight(View anchor, boolean ignoreBottomDecorations) {
        final View bounds = mDropDownBoundsView;
        if (bounds != null) {
            int returnedHeight = bounds.getHeight();
            returnedHeight -= getBackgroundVerticalPadding();
            return returnedHeight;
        }

        getWindowFrame(anchor, ignoreBottomDecorations, mTempRect);
        int returnedHeight = mTempRect.height();
        returnedHeight -= getBackgroundVerticalPadding();

        // 1 dp extra as part of 25 dp status bar. Prevents 1 dp scrolling when landscape 360dp.
        if (Build.VERSION.SDK_INT < 23) returnedHeight += Util.dpToPxSize(mContext, 1);

        return returnedHeight;
    }

    private int getWindowFrame(
            final View anchor, final boolean ignoreBottomDecorations, final Rect out) {
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

    private void getLocationInWindow(View anchor, @Size(2) int[] out) {
        anchor.getLocationInWindow(out);
    }

    /**
     * @param out Margins relative to left, top, right and bottom of the window.
     */
    private void getBoundsInWindow(Rect out) {
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
