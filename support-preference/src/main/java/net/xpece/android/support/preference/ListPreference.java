//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.xpece.android.support.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ArrayRes;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.PopupWindow;
import android.widget.SpinnerAdapter;

import net.xpece.android.support.widget.CheckedTypedItemAdapter;
import net.xpece.android.support.widget.DropDownAdapter;
import net.xpece.android.support.widget.XpListPopupWindow;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

/**
 * A {@link android.support.v7.preference.Preference} that displays a list of entries as
 * a dialog (or a simple menu or simple dialog).
 * <p>
 * This preference will store a string into the SharedPreferences. This string will be the value
 * from the {@link #setEntryValues(CharSequence[])} array (by default).
 *
 * @attr name android:entries
 * @attr name android:entryValues
 * @attr name asp_menuMode
 * @attr name popupTheme
 */
public class ListPreference extends DialogPreference {

    private static boolean sSimpleMenuPreIcsEnabled = true;

    public static void setSimpleMenuPreIcsEnabled(boolean enabled) {
        sSimpleMenuPreIcsEnabled = enabled;
    }

    private static boolean isSimpleMenuEnabled() {
        return Build.VERSION.SDK_INT >= 14 || sSimpleMenuPreIcsEnabled;
    }

    static final boolean SUPPORTS_ON_WINDOW_ATTACH_LISTENER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;

    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private String mValue;
    private String mSummary;
    private boolean mValueSet;

    /**
     * @hide
     */
    @IntDef({MENU_MODE_DIALOG, MENU_MODE_SIMPLE_ADAPTIVE, MENU_MODE_SIMPLE_DIALOG, MENU_MODE_SIMPLE_MENU})
    @RestrictTo(LIBRARY)
    @Retention(RetentionPolicy.SOURCE)
    public @interface MenuMode {}

    public static final int MENU_MODE_DIALOG = 0;
    public static final int MENU_MODE_SIMPLE_DIALOG = 1;
    public static final int MENU_MODE_SIMPLE_MENU = 2;
    public static final int MENU_MODE_SIMPLE_ADAPTIVE = 3;

    @MenuMode private int mMenuMode;
    private float mSimpleMenuPreferredWidthUnit;

    boolean mSimpleMenuShowing;

    private boolean mAdjustViewBounds;

    private Context mPopupContext;

    @SuppressWarnings("RestrictedApi")
    public ListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ListPreference, defStyleAttr, defStyleRes);
        this.mEntries = a.getTextArray(R.styleable.ListPreference_android_entries);
        this.mEntryValues = a.getTextArray(R.styleable.ListPreference_android_entryValues);
        //noinspection WrongConstant
        this.mMenuMode = a.getInt(R.styleable.ListPreference_asp_menuMode, MENU_MODE_DIALOG);
        this.mSimpleMenuPreferredWidthUnit = a.getDimension(R.styleable.ListPreference_asp_simpleMenuWidthUnit, 0f);
        this.mAdjustViewBounds = a.getBoolean(R.styleable.ListPreference_android_adjustViewBounds, false);
        final int popupThemeResId = a.getResourceId(R.styleable.ListPreference_popupTheme, 0);
        a.recycle();
        a = context.obtainStyledAttributes(attrs, R.styleable.Preference, defStyleAttr, defStyleRes);
        this.mSummary = a.getString(R.styleable.Preference_android_summary);
        a.recycle();

        if (popupThemeResId != 0) {
            mPopupContext = new ContextThemeWrapper(context, popupThemeResId);
        } else {
            mPopupContext = context;
        }
    }

    public ListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ListPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.listPreferenceStyle);
    }

    public ListPreference(Context context) {
        this(context, null);
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    protected void performClick(View view) {
        switch (mMenuMode) {
            case MENU_MODE_SIMPLE_ADAPTIVE:
                boolean shown = false;
                if (isSimpleMenuEnabled()) {
                    if (this.isEnabled()) {
                        shown = showAsPopup(view, false);
                    }
                }
                if (!shown) {
                    super.performClick(view);
                }
                break;
            case MENU_MODE_SIMPLE_MENU:
                if (isSimpleMenuEnabled()) {
                    if (this.isEnabled()) {
                        showAsPopup(view, true);
                    }
                } else {
                    super.performClick(view);
                }
                break;
            case MENU_MODE_DIALOG:
            case MENU_MODE_SIMPLE_DIALOG:
                super.performClick(view);
                break;
        }
    }

    /**
     * @return The context used to inflate the ListPreference's simple menu.
     */
    public Context getPopupContext() {
        return mPopupContext;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean showAsPopup(final View anchor, final boolean force) {
        if (getEntries() == null || getEntryValues() == null) {
            throw new IllegalStateException("ListPreference requires an entries array and an entryValues array.");
        }

        final Context context = getPopupContext();

        final int position = findIndexOfValue(getValue());

        final SpinnerAdapter adapter = buildSimpleMenuAdapter(context);

        // Convert getDropDownView to getView.
        final DropDownAdapter adapter2 = new DropDownAdapter(adapter, context.getTheme());

        final XpListPopupWindow popup = new XpListPopupWindow(context, null);
        popup.setModal(true);
        popup.setAnchorView(anchor);
        popup.setAdapter(adapter2);

        popup.setMarginLeft(anchor.getPaddingLeft());
        popup.setMarginRight(anchor.getPaddingRight());

        if (mAdjustViewBounds) {
            popup.setBoundsView((View) anchor.getParent());
        }

        if (mSimpleMenuPreferredWidthUnit >= 0) {
            popup.setPreferredWidthUnit(mSimpleMenuPreferredWidthUnit);
            popup.setWidth(XpListPopupWindow.PREFERRED);
        } else {
            popup.setWidth(XpListPopupWindow.WRAP_CONTENT);
        }
        popup.setMaxWidth(XpListPopupWindow.WRAP_CONTENT);

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
                ListPreference.this.onItemSelected(position);
                popup.dismiss();
            }
        });

        final Object attachListener = preventPopupWindowLeak(anchor, popup);
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (SUPPORTS_ON_WINDOW_ATTACH_LISTENER) {
                    anchor.getViewTreeObserver().removeOnWindowAttachListener((ViewTreeObserver.OnWindowAttachListener) attachListener);
                }

                mSimpleMenuShowing = false;
            }
        });

        if (SUPPORTS_ON_WINDOW_ATTACH_LISTENER) {
            anchor.getViewTreeObserver().addOnWindowAttachListener((ViewTreeObserver.OnWindowAttachListener) attachListener);
        }

        mSimpleMenuShowing = true;

        popup.show();
        popup.setSelection(position);

        return true;
    }

    /**
     * Override if you want to supply your own {@link SpinnerAdapter}. Used in simple dialogs.
     *
     * @param context
     * @return
     */
    @NonNull
    public SpinnerAdapter buildSimpleDialogAdapter(final Context context) {
        return buildAdapter(context, R.layout.asp_select_dialog_item);
    }

    /**
     * Override if you want to supply your own {@link SpinnerAdapter}. Used in simple menus.
     * <p>
     * If you override this, override {@link #onItemSelected(int)} as well.
     *
     * @param context
     * @return
     */
    @NonNull
    public SpinnerAdapter buildSimpleMenuAdapter(final Context context) {
        //noinspection deprecation
        return buildAdapter(context);
    }

    /**
     * If you want to supply your own {@link SpinnerAdapter}
     * override {@link #buildSimpleMenuAdapter(Context)}
     * and {@link #buildSimpleDialogAdapter(Context)} instead.
     *
     * @param context
     * @return
     */
    @NonNull
    @Deprecated
    public SpinnerAdapter buildAdapter(final Context context) {
        return buildAdapter(context, R.layout.asp_simple_spinner_dropdown_item);
    }

    @NonNull
    private SpinnerAdapter buildAdapter(final Context context, @LayoutRes final int layout) {
        return new CheckedTypedItemAdapter<>(context, layout, android.R.id.text1, getEntries());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private Object preventPopupWindowLeak(final View anchor, final XpListPopupWindow popup) {
        if (SUPPORTS_ON_WINDOW_ATTACH_LISTENER) {
            return new ViewTreeObserver.OnWindowAttachListener() {
                @Override
                public void onWindowAttached() {}

                @Override
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                public void onWindowDetached() {
                    anchor.getViewTreeObserver().removeOnWindowAttachListener(this);
                    if (popup.isShowing()) {
                        popup.setOnDismissListener(null);
                        popup.dismiss();
                    }
                }
            };
        }
        return null;
    }

    /**
     * Triggered when an item is selected from menu or dialog.
     * <p>
     * Override if you supplied your own {@link SpinnerAdapter}
     * in {@link #buildSimpleMenuAdapter(Context)} and {@link #buildSimpleDialogAdapter(Context)}
     * if your {@link SpinnerAdapter} does not use {@link #getEntries()} as data set.
     * <p>
     * Call the following code to store the new {@code value}:
     * <pre>
     * if (callChangeListener(value)) {
     * setValue(value);
     * }
     * </pre>
     *
     * @param position
     */
    public void onItemSelected(int position) {
        String value = getEntryValues()[position].toString();
        if (callChangeListener(value)) {
            setValue(value);
        }
    }

    /**
     * Sets the human-readable entries to be shown in the list. This will be
     * shown in subsequent dialogs.
     * <p>
     * Each entry must have a corresponding index in
     * {@link #setEntryValues(CharSequence[])}.
     *
     * @param entries The entries.
     * @see #setEntryValues(CharSequence[])
     */
    public void setEntries(CharSequence[] entries) {
        this.mEntries = entries;
    }

    /**
     * @param entriesResId The entries array as a resource.
     * @see #setEntries(CharSequence[])
     */
    public void setEntries(@ArrayRes int entriesResId) {
        this.setEntries(this.getContext().getResources().getTextArray(entriesResId));
    }

    /**
     * The list of entries to be shown in the list in subsequent dialogs.
     * <p/>
     * Override if you supplied your own {@link SpinnerAdapter}
     * in {@link #buildSimpleMenuAdapter(Context)} and {@link #buildSimpleDialogAdapter(Context)}
     * if your {@link SpinnerAdapter} does not use {@link #getEntries()} as data set.
     *
     * @return The list as an array.
     */
    public CharSequence[] getEntries() {
        return this.mEntries;
    }

    /**
     * The array to find the value to save for a preference when an entry from
     * entries is selected. If a user clicks on the second item in entries, the
     * second item in this array will be saved to the preference.
     *
     * @param entryValues The array to be used as values to save for the preference.
     */
    public void setEntryValues(CharSequence[] entryValues) {
        this.mEntryValues = entryValues;
    }

    /**
     * @param entryValuesResId The entry values array as a resource.
     * @see #setEntryValues(CharSequence[])
     */
    public void setEntryValues(@ArrayRes int entryValuesResId) {
        this.setEntryValues(this.getContext().getResources().getTextArray(entryValuesResId));
    }

    /**
     * Returns the array of values to be saved for the preference.
     * <p/>
     * Override if you supplied your own {@link SpinnerAdapter}
     * in {@link #buildSimpleMenuAdapter(Context)} and {@link #buildSimpleDialogAdapter(Context)}
     * if your {@link SpinnerAdapter} does not use {@link #getEntries()} as data set.
     *
     * @return The array of values.
     */
    public CharSequence[] getEntryValues() {
        return this.mEntryValues;
    }

    /**
     * Sets the value of the key. This should be one of the entries in
     * {@link #getEntryValues()}.
     *
     * @param value The value to set for the key.
     */
    public void setValue(String value) {
        boolean changed = !TextUtils.equals(this.mValue, value);
        if (changed || !this.mValueSet) {
            this.mValue = value;
            this.mValueSet = true;
            this.persistString(value);
            if (changed) {
                this.notifyChanged();
            }
        }

    }

    /**
     * Returns the summary of this ListPreference. If the summary
     * has a {@linkplain java.lang.String#format String formatting}
     * marker in it (i.e. "%s" or "%1$s"), then the current entry
     * value will be substituted in its place.
     *
     * @return the summary with appropriate string substitution
     */
    public CharSequence getSummary() {
        CharSequence entry = this.getEntry();
        return this.mSummary == null ? super.getSummary() : String.format(this.mSummary, entry == null ? "" : entry);
    }

    /**
     * Sets the summary for this Preference with a CharSequence.
     * If the summary has a
     * {@linkplain java.lang.String#format String formatting}
     * marker in it (i.e. "%s" or "%1$s"), then the current entry
     * value will be substituted in its place when it's retrieved.
     *
     * @param summary The summary for the preference.
     */
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
        if (summary == null && this.mSummary != null) {
            this.mSummary = null;
        } else if (summary != null && !summary.equals(this.mSummary)) {
            this.mSummary = summary.toString();
        }

    }

    /**
     * Sets the value to the given index from the entry values.
     *
     * @param index The index of the value to set.
     */
    public void setValueIndex(int index) {
        final CharSequence[] entryValues = getEntryValues();
        if (entryValues != null) {
            this.setValue(entryValues[index].toString());
        }

    }

    /**
     * Returns the value of the key. This should be one of the entries in
     * {@link #getEntryValues()}.
     *
     * @return The value of the key.
     */
    public String getValue() {
        return this.mValue;
    }

    /**
     * Returns the entry corresponding to the current value.
     *
     * @return The entry corresponding to the current value, or null.
     */
    public CharSequence getEntry() {
        final int index = this.getValueIndex();
        final CharSequence[] entries = getEntries();
        return index >= 0 && entries != null ? entries[index] : null;
    }

    /**
     * Returns the index of the given value (in the entry values array).
     *
     * @param value The value whose index should be returned.
     * @return The index of the value, or -1 if not found.
     */
    public int findIndexOfValue(String value) {
        final CharSequence[] entryValues = getEntryValues();
        if (value != null && entryValues != null) {
            for (int i = entryValues.length - 1; i >= 0; --i) {
                if (value.equals(entryValues[i])) {
                    return i;
                }
            }
        }

        return -1;
    }

    private int getValueIndex() {
        return this.findIndexOfValue(this.mValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        this.setValue(restoreValue ? this.getPersistedString(this.mValue) : (String) defaultValue);
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
//        if (this.isPersistent()) {
//            return superState;
//        } else {
        ListPreference.SavedState myState = new ListPreference.SavedState(superState);
        myState.value = this.getValue();
        myState.simpleMenuShowing = this.mSimpleMenuShowing;
        return myState;
//        }
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state != null && state.getClass().equals(ListPreference.SavedState.class)) {
            ListPreference.SavedState myState = (ListPreference.SavedState) state;
            super.onRestoreInstanceState(myState.getSuperState());
            if (!isPersistent()) {
                this.setValue(myState.value);
            }
            this.mSimpleMenuShowing = myState.simpleMenuShowing;
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        if (mSimpleMenuShowing) {
            mSimpleMenuShowing = false;

            final View view = holder.itemView;
            view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    view.getViewTreeObserver().removeOnPreDrawListener(this);
                    performClick(view);
//                    showAsPopup(view);
                    return true;
                }
            });
        }
    }

    public int getMenuMode() {
        return mMenuMode;
    }

    public void setMenuMode(@MenuMode final int menuMode) {
        mMenuMode = menuMode;
    }

    public boolean isSimple() {
        return mMenuMode != MENU_MODE_DIALOG;
    }

    public float getSimpleMenuPreferredWidthUnit() {
        return mSimpleMenuPreferredWidthUnit;
    }

    public void setSimpleMenuPreferredWidthUnit(final float simplePreferredWidthUnit) {
        mSimpleMenuPreferredWidthUnit = simplePreferredWidthUnit;
    }

    private static class SavedState extends BaseSavedState {
        boolean simpleMenuShowing;
        String value;
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        public SavedState(Parcel source) {
            super(source);
            this.value = source.readString();
            this.simpleMenuShowing = source.readInt() != 0;
        }

        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(this.value);
            dest.writeInt(this.simpleMenuShowing ? 1 : 0);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

}
