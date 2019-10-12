package net.xpece.android.support.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import androidx.annotation.ArrayRes;
import androidx.annotation.AttrRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;

import net.xpece.android.support.widget.spinner.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

/**
 * A {@link Spinner} which supports compatible features on older version of the platform,
 * including:
 * <ul>
 * <li>Allows dynamic tint of it background via the background tint methods in
 * {@link androidx.core.view.ViewCompat}.</li>
 * <li>Allows setting of the background tint using {@link R.attr#backgroundTint} and
 * {@link R.attr#backgroundTintMode}.</li>
 * <li>Allows setting of the popups theme using {@link R.attr#popupTheme}.</li>
 * <li>Uses material theme when displaying simple popup menu or simple dialog.</li>
 * </ul>
 */
@TargetApi(23)
public class XpAppCompatSpinner extends AbstractXpAppCompatSpinner {
    private static final String TAG = XpAppCompatSpinner.class.getSimpleName();

    /**
     * @hide
     */
    @IntDef({SPINNER_MODE_ADAPTIVE, SPINNER_MODE_DIALOG, SPINNER_MODE_DROPDOWN})
    @RestrictTo(LIBRARY)
    @Retention(RetentionPolicy.SOURCE)
    @interface SpinnerMode {
    }

    /**
     * Simple dialog is shown instead of simple popup,
     * when there are multiline items.
     */
    public static final int SPINNER_MODE_ADAPTIVE = 0;
    /**
     * Always show as simple dialog.
     */
    public static final int SPINNER_MODE_DIALOG = 1;
    /**
     * Always show as simple popup.
     */
    public static final int SPINNER_MODE_DROPDOWN = 2;

    @SpinnerMode
    private int mSpinnerMode;
    private float mSimpleMenuWidthUnit;
    @SimpleMenu.MaxWidth
    private int mSimpleMenuMaxWidth;
    @SimpleMenu.WidthMode
    private int mSimpleMenuWidthMode;
    private int mSimpleMenuMaxItemCount = -1;

    private XpListPopupWindow mPopup;
    private AlertDialog.Builder mDialogBuilder;

    private OnClickListener mOnClickListener;

    public static void setEntries(@NonNull final Spinner spinner, @ArrayRes final int entriesResId) {
        final Context context = spinner.getContext();
        final CharSequence[] entries = context.getResources().getTextArray(entriesResId);
        final CheckedTypedItemAdapter<CharSequence> adapter = new CheckedTypedItemAdapter<>(context, android.R.layout.simple_spinner_item, android.R.id.text1, entries);
        adapter.setDropDownViewResource(R.layout.asp_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public static void setEntries(@NonNull final Spinner spinner, @NonNull final CharSequence[] entries) {
        final Context context = spinner.getContext();
        final CheckedTypedItemAdapter<CharSequence> adapter = new CheckedTypedItemAdapter<>(context, android.R.layout.simple_spinner_item, android.R.id.text1, entries);
        adapter.setDropDownViewResource(R.layout.asp_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public XpAppCompatSpinner(@NonNull final Context context) {
        this(context, null);
    }

    public XpAppCompatSpinner(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, R.attr.spinnerStyle);
    }

    public XpAppCompatSpinner(
            @NonNull final Context context, @Nullable final AttributeSet attrs,
            @AttrRes final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    private void init(
            @NonNull final Context context, @Nullable final AttributeSet attrs,
            @AttrRes final int defStyleAttr, @StyleRes final int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.XpAppCompatSpinner, defStyleAttr, defStyleRes);
        try {
            final float simpleMenuWidthUnit = a.getDimension(R.styleable.XpAppCompatSpinner_asp_simpleMenuWidthUnit, 0f);
            final int simpleMenuWidthMode = a.getInt(R.styleable.XpAppCompatSpinner_asp_simpleMenuWidthMode, 0);
            final int simpleMenuMaxWidth = a.getInt(R.styleable.XpAppCompatSpinner_asp_simpleMenuMaxWidth, 0);
            initWidth(simpleMenuWidthMode, simpleMenuMaxWidth, simpleMenuWidthUnit);

            //noinspection WrongConstant
            this.mSpinnerMode = a.getInt(R.styleable.XpAppCompatSpinner_asp_spinnerMode, SPINNER_MODE_ADAPTIVE);

            final int maxItemCount = a.getInt(R.styleable.XpAppCompatSpinner_asp_simpleMenuMaxItemCount, mSimpleMenuMaxItemCount);
            setSimpleMenuMaxItemCount(maxItemCount);
        } finally {
            a.recycle();
        }
    }

    /**
     * This method exists for compatibility reasons.
     * In version 1.x.x there was only {@code asp_simpleMenuWidthUnit} attribute and
     * other values were inferred from its value.
     */
    private void initWidth(
            @SimpleMenu.WidthMode final int widthMode, @SimpleMenu.MaxWidth final int maxWidth,
            float widthUnit) {
        if (maxWidth == 0 && widthMode == 0) {
            setSimpleMenuWidthUnitCompat(widthUnit);
        } else {
            setSimpleMenuWidthMode(widthMode);
            setSimpleMenuMaxWidth(maxWidth);
            setSimpleMenuWidthUnit(widthUnit);
        }
    }

    private void setSimpleMenuWidthUnitCompat(float widthUnit) {
        Log.w(TAG, "Applying width unit in compat mode. Max width is now fit_screen.");
        setSimpleMenuMaxWidth(SimpleMenu.MaxWidth.FIT_SCREEN);
        if (widthUnit < 0) {
            setSimpleMenuWidthMode(SimpleMenu.WidthMode.WRAP_CONTENT);
            setSimpleMenuWidthUnit(0);
        } else {
            setSimpleMenuWidthMode(SimpleMenu.WidthMode.WRAP_CONTENT_UNIT);
            setSimpleMenuWidthUnit(widthUnit);
        }
    }

    /**
     * @see #SPINNER_MODE_ADAPTIVE
     * @see #SPINNER_MODE_DIALOG
     * @see #SPINNER_MODE_DROPDOWN
     */
    @SpinnerMode
    public int getSpinnerMode() {
        return mSpinnerMode;
    }

    /**
     * @see #SPINNER_MODE_ADAPTIVE
     * @see #SPINNER_MODE_DIALOG
     * @see #SPINNER_MODE_DROPDOWN
     */
    public void setSpinnerMode(@SpinnerMode final int spinnerMode) {
        mSpinnerMode = spinnerMode;
    }

    /**
     * @param maxWidth Maximum allowed width of the popup menu in pixels or one of constants.
     * @see SimpleMenu.MaxWidth#FIT_SCREEN
     * @see SimpleMenu.MaxWidth#FIT_ANCHOR
     */
    public void setSimpleMenuMaxWidth(@SimpleMenu.MaxWidth int maxWidth) {
        if (maxWidth < -2) {
            throw new IllegalArgumentException("simpleMenuMaxWidth must be fit_screen, fit_anchor or a valid dimension.");
        }
        mSimpleMenuMaxWidth = maxWidth;
    }

    /**
     * @param widthMode Preferred measuring mode for the popup menu.
     * @see SimpleMenu.WidthMode#MATCH_CONSTRAINT
     * @see SimpleMenu.WidthMode#WRAP_CONTENT
     * @see SimpleMenu.WidthMode#WRAP_CONTENT_UNIT
     */
    public void setSimpleMenuWidthMode(@SimpleMenu.WidthMode int widthMode) {
        if (widthMode > -1 || widthMode < -3) {
            throw new IllegalArgumentException("simpleMenuWidthMode must be match_parent, wrap_content or wrap_content_unit.");
        }
        mSimpleMenuWidthMode = widthMode;
    }

    /**
     * @param widthUnit When {@link #setSimpleMenuWidthMode(int)}
     *                  is set to {@link SimpleMenu.WidthMode#WRAP_CONTENT_UNIT}
     *                  popup width will be
     *                  <ul>
     *                  <li>at least as wide as its content rounded up to a multiple of {@code widthUnit},</li>
     *                  <li>at least as wide as {@code widthUnit * 1.5},</li>
     *                  <li>limited by {@link #setSimpleMenuMaxWidth(int)}.</li>
     *                  </ul>
     * @see SimpleMenu.WidthMode#WRAP_CONTENT_UNIT
     */
    public void setSimpleMenuWidthUnit(float widthUnit) {
        if (widthUnit < 0) {
            throw new IllegalArgumentException("Width unit must be greater than zero.");
        }
        mSimpleMenuWidthUnit = widthUnit;
    }

    /**
     * @param simpleMenuMaxItemCount Popup menu will adjust its height to display at most this many items.
     */
    public void setSimpleMenuMaxItemCount(int simpleMenuMaxItemCount) {
        if (simpleMenuMaxItemCount == 0 || simpleMenuMaxItemCount < -1) {
            throw new IllegalArgumentException("Max length must be greater than zero, or -1, which represents infinity.");
        }
        mSimpleMenuMaxItemCount = simpleMenuMaxItemCount;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean performClick() {
        if (hasOnClickListeners()) {
            playSoundEffect(SoundEffectConstants.CLICK);
            if (callOnClick()) {
                return true;
            }
        }

        onClickDefault();
        return true;
    }

    @Override
    public boolean hasOnClickListeners() {
        return mOnClickListener != null;
    }

    @Override
    public void setOnClickListener(@Nullable final OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    @Override
    public boolean callOnClick() {
        if (hasOnClickListeners()) {
            mOnClickListener.onClick(this);
            return true;
        }
        return false;
    }

    public void onClickDefault() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);

        final SpinnerAdapter adapter = getAdapter();
        if (adapter == null || adapter.isEmpty()) {
            return;
        }

        switch (mSpinnerMode) {
            case SPINNER_MODE_ADAPTIVE: {
                if (!showAsPopup(false)) {
                    showAsDialog();
                }
                break;
            }
            case SPINNER_MODE_DIALOG: {
                showAsDialog();
                break;
            }
            case SPINNER_MODE_DROPDOWN: {
                showAsPopup(true);
                break;
            }
        }
    }

    private void showAsDialog() {
        final Context context = getPopupContext();
        SpinnerAdapter adapter = getAdapter();
        if (!(adapter instanceof DropDownAdapter)) {
            adapter = new DropDownAdapter(adapter, context.getTheme());
        }

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(@NonNull final DialogInterface dialog, final int which) {
                setSelection(which);
                if (getOnItemClickListener() != null) {
                    performItemClick(null, which, getItemIdAtPosition(which));
                }
                dialog.dismiss();
            }
        };

        final AlertDialog.Builder builder;
        if (mDialogBuilder != null) {
            builder = mDialogBuilder;
        } else {
            builder = new AlertDialog.Builder(getContext());
        }
        builder.setTitle(getPrompt());
        builder.setSingleChoiceItems((ListAdapter) adapter, getSelectedItemPosition(), listener);

        mDialogBuilder = builder;

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean showAsPopup(final boolean force) {
        final View anchor = this;
        final Context context = getPopupContext();

        final int position = getSelectedItemPosition();
        SpinnerAdapter adapter = getAdapter();

        if (!(adapter instanceof DropDownAdapter)) {
            adapter = new DropDownAdapter(adapter, context.getTheme());
        }

        final XpListPopupWindow popup;
        if (mPopup != null) {
            popup = mPopup;
        } else {
            popup = new XpListPopupWindow(context, null);
        }
        popup.setModal(true);
        popup.setAnchorView(anchor);
        popup.setPromptPosition(XpListPopupWindow.POSITION_PROMPT_ABOVE);
        popup.setAdapter((ListAdapter) adapter);

//        if (mAdjustViewBounds) {
//            popup.setBoundsView((View) anchor.getParent());
//        }

        popup.setWidthUnit(mSimpleMenuWidthUnit);
        popup.setWidth(mSimpleMenuWidthMode);
        popup.setMaxWidth(mSimpleMenuMaxWidth);

        popup.setMaxItemCount(mSimpleMenuMaxItemCount);

        // Testing.
//        popup.setDropDownGravity(Gravity.RIGHT);
//        popup.setMaxWidth(XpListPopupWindow.MATCH_CONSTRAINT);
//        popup.setWidth(1347);
//        final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, context.getResources().getDisplayMetrics());
//        popup.setMarginBottom(margin);
//        popup.setMarginTop(margin);
//        popup.setMarginLeft(margin);
//        popup.setMarginRight(margin);

        if (!force) {
            // If we're not forced to show popup window measure the items...
            boolean hasMultiLineItems = popup.hasMultilineItems();
            if (hasMultiLineItems) {
                // ...and if any are multiline show a dialog instead.
                return false;
            }
        }

        popup.measurePreferredVerticalOffset(position);
        int preferredVerticalOffset = popup.getMeasuredPreferredVerticalOffset();
        popup.setVerticalOffset(preferredVerticalOffset);

        final ListView list = popup.getListView();
        assert list != null;

        final View view = adapter.getView(0, null, this); // In dropdown.
        final View spinnerItemView = getSelectedView(); // In spinner.
        if (view != null && spinnerItemView != null) {
            int preferredHorizontalOffset;
            if (GravityCompat.getAbsoluteGravity(popup.getDropDownGravity() & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK, ViewCompat.getLayoutDirection(this)) == Gravity.LEFT) {
                preferredHorizontalOffset = -(view.getPaddingLeft() + list.getListPaddingLeft() - this.getPaddingLeft() - spinnerItemView.getPaddingLeft());
            } else {
                preferredHorizontalOffset = view.getPaddingRight() + list.getListPaddingRight() - this.getPaddingRight() - spinnerItemView.getPaddingRight();
            }
            popup.setHorizontalOffset(preferredHorizontalOffset);
        }

        popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setSelection(position);
                if (getOnItemClickListener() != null) {
                    performItemClick(view, position, id);
                }
                popup.dismiss();
            }
        });

        popup.show();

        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list.setTextAlignment(getTextAlignment());
        list.setTextDirection(getTextDirection());

        popup.setSelection(position);

        mPopup = popup;

        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mPopup != null && mPopup.isShowing()) {
            mPopup.dismiss();
        }
        super.onDetachedFromWindow();
    }

//    @Override
//    public void onRtlPropertiesChanged(int layoutDirection) {
//        super.onRtlPropertiesChanged(layoutDirection);
//        if (mPopup != null) {
//            mPopup.setLayoutDirection(layoutDirection);
//        }
//    }
}
