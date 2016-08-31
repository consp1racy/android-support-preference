package net.xpece.android.support.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AbstractXpAppCompatSpinner;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

import net.xpece.android.support.widget.spinner.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Eugen on 22. 4. 2016.
 */
@TargetApi(23)
public class XpAppCompatSpinner extends AbstractXpAppCompatSpinner {

    @IntDef({SPINNER_MODE_ADAPTIVE, SPINNER_MODE_DIALOG, SPINNER_MODE_DROPDOWN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SpinnerMode {}

    public static final int SPINNER_MODE_ADAPTIVE = 0;
    public static final int SPINNER_MODE_DIALOG = 1;
    public static final int SPINNER_MODE_DROPDOWN = 2;

    @SpinnerMode private int mSpinnerMode;
    private float mSimpleMenuPreferredWidthUnit;

    private XpListPopupWindow mPopup;
    private AlertDialog.Builder mDialogBuilder;

    public XpAppCompatSpinner(final Context context) {
        this(context, null);
    }

    public XpAppCompatSpinner(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XpAppCompatSpinner(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    private void init(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.XpAppCompatSpinner, defStyleAttr, defStyleRes);
        this.mSimpleMenuPreferredWidthUnit = a.getDimension(R.styleable.XpAppCompatSpinner_asp_simpleMenuWidthUnit, 0f);
        //noinspection WrongConstant
        this.mSpinnerMode = a.getInt(R.styleable.XpAppCompatSpinner_asp_spinnerMode, SPINNER_MODE_ADAPTIVE);
        a.recycle();
    }

    public int getSpinnerMode() {
        return mSpinnerMode;
    }

    public void setSpinnerMode(final int spinnerMode) {
        mSpinnerMode = spinnerMode;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    @Override
    public boolean performClick() {
        if (ViewCompat.hasOnClickListeners(this)) { // TODO Support onClickListener pre API 15.
            playSoundEffect(SoundEffectConstants.CLICK);
            if (callOnClick()) {
                return true;
            }
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);

        final SpinnerAdapter adapter = getAdapter();
        if (adapter == null || adapter.isEmpty()) {
            return true;
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
        return true;
    }

    private void showAsDialog() {
        final Context context = getPopupContext();
        SpinnerAdapter adapter = getAdapter();
        if (!(adapter instanceof DropDownAdapter)) {
            adapter = new DropDownAdapter(adapter, context.getTheme());
        }

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
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
        popup.setAnimationStyle(R.style.Animation_Asp_Popup);

//        if (mAdjustViewBounds) {
//            popup.setBoundsView((View) anchor.getParent());
//        }

        if (mSimpleMenuPreferredWidthUnit >= 0) {
            popup.setPreferredWidthUnit(mSimpleMenuPreferredWidthUnit);
            popup.setWidth(XpListPopupWindow.PREFERRED);
        } else {
            popup.setWidth(XpListPopupWindow.WRAP_CONTENT);
        }
        popup.setMaxWidth(XpListPopupWindow.MATCH_PARENT);

        int preferredVerticalOffset = popup.getPreferredVerticalOffset(position);
        popup.setVerticalOffset(preferredVerticalOffset);

        View v = adapter.getView(0, null, this);
        if (v != null) {
            int preferredHorizontalOffset;
            if (GravityCompat.getAbsoluteGravity(popup.getDropDownGravity() & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK, ViewCompat.getLayoutDirection(this)) == Gravity.LEFT) {
                preferredHorizontalOffset = -(v.getPaddingLeft() + getPaddingLeft());
            } else {
                preferredHorizontalOffset = v.getPaddingRight() + getPaddingRight();
            }
            popup.setHorizontalOffset(preferredHorizontalOffset);
        }

        // Testing.
//        popup.setDropDownGravity(Gravity.LEFT);
//        popup.setMaxWidth(XpListPopupWindow.MATCH_PARENT);
//        popup.setWidth(1347);
//        marginV = Util.dpToPxOffset(context, 0);
//        popup.setMarginBottom(marginV);
//        popup.setMarginTop(marginV);

        if (!force) {
            // If we're not forced to show popup window measure the items...
            boolean hasMultiLineItems = popup.hasMultiLineItems();
            if (hasMultiLineItems) {
                // ...and if any are multiline show a dialog instead.
                return false;
            }
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
        popup.setSelectionInitial(position);

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
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//
//        if (mPopup != null && MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
//            final int measuredWidth = getMeasuredWidth();
//            setMeasuredDimension(Math.min(Math.max(measuredWidth,
//                compatMeasureContentWidth(getAdapter(), getBackground())),
//                MeasureSpec.getSize(widthMeasureSpec)),
//                getMeasuredHeight());
//        }
//    }
}
