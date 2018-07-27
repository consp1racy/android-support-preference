package net.xpece.android.support.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.TintTypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import net.xpece.android.support.widget.spinner.R;

/**
 * @author Eugen on 08.06.2018.
 */
abstract class AbstractXpAppCompatSpinner extends AppCompatSpinner {
    private static final String TAG = "XpAppCompatSpinner";

    /** Ignore setAdapter() calls from the super constructor. */
    private boolean mCanSetAdapter = false;

    /** Context used to inflate the popup window or dialog. */
    private final Context mPopupContext;

    public AbstractXpAppCompatSpinner(final Context context) {
        this(context, null);
    }

    public AbstractXpAppCompatSpinner(final Context context, final int mode) {
        this(context, null, R.attr.spinnerStyle, mode);
    }

    public AbstractXpAppCompatSpinner(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.spinnerStyle);
    }

    public AbstractXpAppCompatSpinner(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        this(context, attrs, defStyleAttr, -1);
    }

    public AbstractXpAppCompatSpinner(final Context context, final AttributeSet attrs, final int defStyleAttr, final int mode) {
        this(context, attrs, defStyleAttr, mode, null);
    }

    @SuppressLint("RestrictedApi")
    public AbstractXpAppCompatSpinner(final Context context, final AttributeSet attrs, final int defStyleAttr, final int mode, final Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, mode, popupTheme);

        mCanSetAdapter = true;

        final TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs,
                R.styleable.Spinner, defStyleAttr, 0);

        try {
            final int realMode;
            if (mode == -1) {
                realMode = a.getInt(R.styleable.Spinner_android_spinnerMode, Spinner.MODE_DIALOG);
            } else {
                realMode = mode;
            }
            if (realMode != Spinner.MODE_DIALOG) {
                throw new IllegalArgumentException(TAG + " only works with android:spinnerMode=\"dialog\"");
            }

            if (popupTheme != null) {
                mPopupContext = new ContextThemeWrapper(context, popupTheme);
            } else {
                final int popupThemeResId = a.getResourceId(R.styleable.Spinner_popupTheme, 0);
                if (popupThemeResId != 0) {
                    mPopupContext = new ContextThemeWrapper(context, popupThemeResId);
                } else {
                    // If we're running on a < M device, we'll use the current context and still handle
                    // any dropdown popup
                    mPopupContext = !(Build.VERSION.SDK_INT >= 23) ? context : null;
                }
            }
            if (getPopupContext() == null) {
                throw new IllegalArgumentException("You need to set app:popupTheme.");
            }

            final CharSequence[] entries = a.getTextArray(R.styleable.Spinner_android_entries);
            if (entries != null) {
                final CheckedTypedItemAdapter<CharSequence> adapter = new CheckedTypedItemAdapter<>(context, android.R.layout.simple_spinner_item, android.R.id.text1, entries);
                adapter.setDropDownViewResource(R.layout.asp_simple_spinner_dropdown_item);
                setAdapter(adapter);
            }
        } finally {
            a.recycle();
        }
    }

    /**
     * @return the context used to inflate the Spinner's popup or dialog window
     */
    @NonNull
    @Override
    public Context getPopupContext() {
        if (mPopupContext != null ) {
            return mPopupContext;
        } else {
            return super.getPopupContext();
        }
    }

    @Override
    public void setAdapter(final SpinnerAdapter adapter) {
        if (mCanSetAdapter) {
            super.setAdapter(adapter);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final SpinnerAdapter adapter = getAdapter();
        if (adapter != null && MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
            final int measuredWidth = getMeasuredWidth();
            setMeasuredDimension(Math.min(Math.max(measuredWidth,
                    measureContentWidth()),
                    MeasureSpec.getSize(widthMeasureSpec)),
                    getMeasuredHeight());
        }
    }

    private int measureContentWidth() {
        final SpinnerAdapter adapter = getAdapter();

        if (adapter == null) {
            return getPaddingLeft() + getPaddingRight();
        }

        int width = 0;

        final int widthMeasureSpec =
                MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.UNSPECIFIED);
        final int heightMeasureSpec =
                MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.UNSPECIFIED);

        View child = null;
        int itemType = 0;
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            final int newType = adapter.getItemViewType(i);
            if (newType != itemType) {
                itemType = newType;
                child = null;
            }
            child = adapter.getView(i, child, this);

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

            width = Math.max(width, child.getMeasuredWidth());
        }

        width += getPaddingLeft() + getPaddingRight();

        return width;
    }
}
