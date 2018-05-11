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
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ArrayRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.view.TintableBackgroundView;
import android.support.v7.view.ContextThemeWrapper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import net.xpece.android.support.widget.CheckedTypedItemAdapter;
import net.xpece.android.support.widget.spinner.R;

import java.lang.reflect.Field;

import javax.annotation.ParametersAreNonnullByDefault;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;
import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * A {@link Spinner} which supports compatible features on older version of the platform,
 * including:
 * <ul>
 * <li>Allows dynamic tint of it background via the background tint methods in
 * {@link android.support.v4.view.ViewCompat}.</li>
 * <li>Allows setting of the background tint using {@link R.attr#backgroundTint} and
 * {@link R.attr#backgroundTintMode}.</li>
 * <li>Allows setting of the popups theme using {@link R.attr#popupTheme}.</li>
 * </ul>
 *
 * @hide
 */
@ParametersAreNonnullByDefault
@RestrictTo(LIBRARY)
@SuppressLint("AppCompatCustomView")
@TargetApi(23)
public abstract class AbstractXpAppCompatSpinner extends Spinner implements TintableBackgroundView {

    private static final boolean IS_AT_LEAST_K = Build.VERSION.SDK_INT >= 19;
    private static final boolean IS_AT_LEAST_M = Build.VERSION.SDK_INT >= 23;

    private static final Field FIELD_FORWARDING_LISTENER;

    static {
        Field f = null;
        if (IS_AT_LEAST_K) {
            try {
                f = Spinner.class.getDeclaredField("mForwardingListener");
                f.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        FIELD_FORWARDING_LISTENER = f;
    }

    private AppCompatBackgroundHelper mBackgroundTintHelper;

    /** Context used to inflate the popup window or dialog. */
    private Context mPopupContext;

    private CharSequence mPrompt;

    /**
     * Construct a new spinner with the given context's theme.
     *
     * @param context The Context the view is running in, through which it can
     * access the current theme, resources, etc.
     */
    public AbstractXpAppCompatSpinner(Context context) {
        this(context, null);
    }

    /**
     * Construct a new spinner with the given context's theme and the supplied
     * mode of displaying choices. <code>mode</code> may be one of
     * {@link #MODE_DIALOG} or {@link #MODE_DROPDOWN}.
     *
     * @param context The Context the view is running in, through which it can
     * access the current theme, resources, etc.
     * @param mode Constant describing how the user will select choices from the spinner.
     * @see #MODE_DIALOG
     * @see #MODE_DROPDOWN
     */
    public AbstractXpAppCompatSpinner(Context context, int mode) {
        this(context, null, R.attr.spinnerStyle, mode);
    }

    /**
     * Construct a new spinner with the given context's theme and the supplied attribute set.
     *
     * @param context The Context the view is running in, through which it can
     * access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    public AbstractXpAppCompatSpinner(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.spinnerStyle);
    }

    /**
     * Construct a new spinner with the given context's theme, the supplied attribute set,
     * and default style attribute.
     *
     * @param context The Context the view is running in, through which it can
     * access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     * reference to a style resource that supplies default values for
     * the view. Can be 0 to not look for defaults.
     */
    public AbstractXpAppCompatSpinner(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, -1);
    }

    /**
     * Construct a new spinner with the given context's theme, the supplied attribute set,
     * and default style. <code>mode</code> may be one of {@link #MODE_DIALOG} or
     * {@link #MODE_DROPDOWN} and determines how the user will select choices from the spinner.
     *
     * @param context The Context the view is running in, through which it can
     * access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     * reference to a style resource that supplies default values for
     * the view. Can be 0 to not look for defaults.
     * @param mode Constant describing how the user will select choices from the spinner.
     * @see #MODE_DIALOG
     * @see #MODE_DROPDOWN
     */
    public AbstractXpAppCompatSpinner(
            Context context, @Nullable AttributeSet attrs, int defStyleAttr, int mode) {
        this(context, attrs, defStyleAttr, mode, null);
    }

    /**
     * Constructs a new spinner with the given context's theme, the supplied
     * attribute set, default styles, popup mode (one of {@link #MODE_DIALOG}
     * or {@link #MODE_DROPDOWN}), and the context against which the popup
     * should be inflated.
     *
     * @param context The context against which the view is inflated, which
     * provides access to the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     * reference to a style resource that supplies default
     * values for the view. Can be 0 to not look for
     * defaults.
     * @param mode Constant describing how the user will select choices from
     * the spinner.
     * @param popupTheme The theme against which the dialog or dropdown popup
     * should be inflated. May be {@code null} to use the
     * view theme. If set, this will override any value
     * specified by
     * {@link R.styleable#Spinner_popupTheme}.
     * @see #MODE_DIALOG
     * @see #MODE_DROPDOWN
     */
    @SuppressWarnings("RestrictedApi")
    public AbstractXpAppCompatSpinner(
            Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr,
            int mode,
            @Nullable Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr);

        if (!sCompatibilityDone) {
            final int targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
            sUseZeroUnspecifiedMeasureSpec = targetSdkVersion < Build.VERSION_CODES.M;
            sCompatibilityDone = true;
        }

        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs,
                R.styleable.Spinner, defStyleAttr, 0);

        mBackgroundTintHelper = new AppCompatBackgroundHelper(this);

        if (popupTheme != null) {
            mPopupContext = new ContextThemeWrapper(context, popupTheme);
        } else {
            final int popupThemeResId = a.getResourceId(R.styleable.Spinner_popupTheme, 0);
            if (popupThemeResId != 0) {
                mPopupContext = new ContextThemeWrapper(context, popupThemeResId);
            } else {
                // If we're running on a < M device, we'll use the current context and still handle
                // any dropdown popup
                mPopupContext = !IS_AT_LEAST_M ? context : null;
            }
        }

        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.loadFromAttributes(attrs, defStyleAttr);
        }

        final CharSequence[] entries = a.getTextArray(R.styleable.Spinner_android_entries);
        if (entries != null) {
            final CheckedTypedItemAdapter<CharSequence> adapter = new CheckedTypedItemAdapter<>(context, android.R.layout.simple_spinner_item, android.R.id.text1, entries);
            adapter.setDropDownViewResource(R.layout.asp_simple_spinner_dropdown_item);
            setAdapter(adapter);
        }
        mPrompt = a.getText(R.styleable.Spinner_android_prompt);

        a.recycle();

        if (IS_AT_LEAST_K) {
            // Disable native forwarding listener.
            try {
                FIELD_FORWARDING_LISTENER.set(this, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void setEntries(final Spinner spinner, @ArrayRes final int entriesResId) {
        final Context context = spinner.getContext();
        final CharSequence[] entries = context.getResources().getTextArray(entriesResId);
        final CheckedTypedItemAdapter<CharSequence> adapter = new CheckedTypedItemAdapter<>(context, android.R.layout.simple_spinner_item, android.R.id.text1, entries);
        adapter.setDropDownViewResource(R.layout.asp_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public static void setEntries(final Spinner spinner, final CharSequence[] entries) {
        final Context context = spinner.getContext();
        final CheckedTypedItemAdapter<CharSequence> adapter = new CheckedTypedItemAdapter<>(context, android.R.layout.simple_spinner_item, android.R.id.text1, entries);
        adapter.setDropDownViewResource(R.layout.asp_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    /**
     * @return the context used to inflate the Spinner's popup or dialog window
     */
    @NonNull
    @Override
    public Context getPopupContext() {
        if (mPopupContext != null) {
            return mPopupContext;
        } else if (IS_AT_LEAST_M) {
            return super.getPopupContext();
        }
        return null;
    }

    @Override
    public void setBackgroundResource(@DrawableRes int resId) {
        super.setBackgroundResource(resId);
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.onSetBackgroundResource(resId);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setBackgroundDrawable(@Nullable Drawable background) {
        super.setBackgroundDrawable(background);
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.onSetBackgroundDrawable(background);
        }
    }

    /**
     * This should be accessed via
     * {@link android.support.v4.view.ViewCompat#setBackgroundTintList(View,
     * ColorStateList)}
     *
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void setSupportBackgroundTintList(@Nullable ColorStateList tint) {
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.setSupportBackgroundTintList(tint);
        }
    }

    /**
     * This should be accessed via
     * {@link android.support.v4.view.ViewCompat#getBackgroundTintList(View)}
     *
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    @Nullable
    public ColorStateList getSupportBackgroundTintList() {
        return mBackgroundTintHelper != null
                ? mBackgroundTintHelper.getSupportBackgroundTintList() : null;
    }

    /**
     * This should be accessed via
     * {@link android.support.v4.view.ViewCompat#setBackgroundTintMode(View,
     * PorterDuff.Mode)}
     *
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void setSupportBackgroundTintMode(@Nullable PorterDuff.Mode tintMode) {
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.setSupportBackgroundTintMode(tintMode);
        }
    }

    /**
     * This should be accessed via
     * {@link android.support.v4.view.ViewCompat#getBackgroundTintMode(View)}
     *
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    @Nullable
    public PorterDuff.Mode getSupportBackgroundTintMode() {
        return mBackgroundTintHelper != null
                ? mBackgroundTintHelper.getSupportBackgroundTintMode() : null;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.applySupportBackgroundTint();
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public void setPrompt(@Nullable final CharSequence prompt) {
        super.setPrompt(prompt);
        mPrompt = prompt;
    }

    @Override
    public void setPromptId(final int promptId) {
        super.setPromptId(promptId);
        mPrompt = getContext().getText(promptId);
    }

    @Nullable
    @Override
    public CharSequence getPrompt() {
        return mPrompt;
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
                makeSafeMeasureSpec(getMeasuredWidth(), MeasureSpec.UNSPECIFIED);
        final int heightMeasureSpec =
                makeSafeMeasureSpec(getMeasuredHeight(), MeasureSpec.UNSPECIFIED);

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

    /**
     * Signals that compatibility booleans have been initialized according to
     * target SDK versions.
     */
    private static boolean sCompatibilityDone = false;

    /**
     * Always return a size of 0 for MeasureSpec values with a mode of UNSPECIFIED
     */
    private static boolean sUseZeroUnspecifiedMeasureSpec = false;

    /**
     * Like {@link MeasureSpec#makeMeasureSpec(int, int)}, but any spec with a mode of UNSPECIFIED
     * will automatically get a size of 0. Older apps expect this.
     */
    private int makeSafeMeasureSpec(int size, int mode) {
        if (sUseZeroUnspecifiedMeasureSpec && mode == MeasureSpec.UNSPECIFIED) {
            return 0;
        }
        return MeasureSpec.makeMeasureSpec(size, mode);
    }
}
