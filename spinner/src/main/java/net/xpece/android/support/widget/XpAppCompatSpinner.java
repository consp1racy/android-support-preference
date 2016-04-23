package net.xpece.android.support.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AbstractXpAppCompatSpinner;
import android.util.AttributeSet;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

import net.xpece.android.support.widget.spinner.R;

/**
 * @author Eugen on 22. 4. 2016.
 */
public class XpAppCompatSpinner extends AbstractXpAppCompatSpinner {

    private float mSimpleMenuPreferredWidthUnit;

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
        a.recycle();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    @Override
    public boolean performClick() {
        if (ViewCompat.hasOnClickListeners(this)) {
            playSoundEffect(SoundEffectConstants.CLICK);
            if (callOnClick()) {
                return true;
            }
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);

        if (!showAsPopup(false)) {
            showAsDialog();
        }
        return true;
    }

    private void showAsDialog() {
        final Context context = getPopupContext();
        SpinnerAdapter adapter = getAdapter();
        if (!(adapter instanceof DropDownAdapter)) {
            adapter = new DropDownAdapter(adapter, context.getTheme());
        }

        final ListAdapter listAdapter = (ListAdapter) adapter;

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

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getPrompt());
        builder.setSingleChoiceItems((ListAdapter) adapter, getSelectedItemPosition(), listener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean showAsPopup(final boolean force) {
        final View anchor = this;
        final Context context = getPopupContext();

        final int position = getSelectedItemPosition();
        SpinnerAdapter adapter = getAdapter();

        if (adapter instanceof CheckedItemAdapter) {
            CheckedItemAdapter a2 = (CheckedItemAdapter) adapter;
            a2.setSelection(position);
        }

        if (!(adapter instanceof DropDownAdapter)) {
            adapter = new DropDownAdapter(adapter, context.getTheme());
        }

        final XpListPopupWindow popup = new XpListPopupWindow(context, null);
        popup.setModal(true);
        popup.setAnchorView(anchor);
        popup.setPromptPosition(XpListPopupWindow.POSITION_PROMPT_ABOVE);
        popup.setAdapter((ListAdapter) adapter);
        popup.setAnimationStyle(R.style.Animation_Asp_Popup);

//        int marginV = Util.dpToPxOffset(context, 8); // TODO outsource
//        popup.setMarginBottom(marginV);
//        popup.setMarginTop(marginV);

        popup.setMarginLeft(anchor.getPaddingLeft());
        popup.setMarginRight(anchor.getPaddingRight());

//        if (mAdjustViewBounds) {
//            popup.setBoundsView((View) anchor.getParent());
//        }

        if (mSimpleMenuPreferredWidthUnit >= 0) {
            popup.setPreferredWidthUnit(mSimpleMenuPreferredWidthUnit);
            popup.setWidth(XpListPopupWindow.PREFERRED);
        } else {
            popup.setWidth(XpListPopupWindow.WRAP_CONTENT);
        }
//        popup.setMaxWidth(XpListPopupWindow.MATCH_PARENT);
        popup.setMaxWidth(Integer.MAX_VALUE);

        int preferredVerticalOffset = popup.getPreferredVerticalOffset(position);
        popup.setVerticalOffset(preferredVerticalOffset);

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

        return true;
    }

}
