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
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.PopupWindow;

import net.xpece.android.support.widget.XpListPopupWindow;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ListPreference extends DialogPreference {

    private static boolean sSimpleMenuPreIcsEnabled = true;

    /**
     * Simple menu variant of {@link ListPreference} is broken on Android 2 so it's disabled by default in favor of simple dialog variant.
     * It can be enabled if you're feeling lucky.
     *
     * @param enabled
     */
    public static void setSimpleMenuPreIcsEnabled(boolean enabled) {
        sSimpleMenuPreIcsEnabled = enabled;
    }

    private static boolean isSimpleMenuEnabled() {
        return Build.VERSION.SDK_INT >= 14 || sSimpleMenuPreIcsEnabled;
    }

    private static final boolean SUPPORTS_ON_WINDOW_ATTACH_LISTENER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;

    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private String mValue;
    private String mSummary;
    private boolean mValueSet;

    @IntDef({MENU_MODE_DIALOG, MENU_MODE_SIMPLE_ADAPTIVE, MENU_MODE_SIMPLE_DIALOG, MENU_MODE_SIMPLE_MENU})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MenuMode {}

    public static final int MENU_MODE_DIALOG = 0;
    public static final int MENU_MODE_SIMPLE_DIALOG = 1;
    public static final int MENU_MODE_SIMPLE_MENU = 2;
    public static final int MENU_MODE_SIMPLE_ADAPTIVE = 3;

    @MenuMode private int mMenuMode;
    private float mSimpleMenuPreferredWidthUnit;

    private boolean mSimpleMenuShowing;

    public ListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ListPreference, defStyleAttr, defStyleRes);
        this.mEntries = a.getTextArray(R.styleable.ListPreference_android_entries);
        this.mEntryValues = a.getTextArray(R.styleable.ListPreference_android_entryValues);
        //noinspection WrongConstant
        this.mMenuMode = a.getInt(R.styleable.ListPreference_asp_menuMode, MENU_MODE_DIALOG);
        this.mSimpleMenuPreferredWidthUnit = a.getDimension(R.styleable.ListPreference_asp_simpleMenuWidthUnit, 0f);
        a.recycle();
        a = context.obtainStyledAttributes(attrs, R.styleable.Preference, defStyleAttr, defStyleRes);
        this.mSummary = a.getString(R.styleable.Preference_android_summary);
        a.recycle();
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean showAsPopup(final View anchor, final boolean force) {
        final Context context = getContext();

        final int position = findIndexOfValue(getValue());

        final int layout = R.layout.asp_simple_spinner_dropdown_item;
        final CheckedItemAdapter adapter = new CheckedItemAdapter(context, layout, android.R.id.text1, mEntries);
        adapter.setSelection(position);

        final XpListPopupWindow popup = new XpListPopupWindow(context, null);
        popup.setModal(true);
        popup.setAnchorView(anchor);
        popup.setAdapter(adapter);
        popup.setAnimationStyle(R.style.Animation_Asp_Popup);

        int marginV = Util.dpToPxOffset(context, 16); // TODO outsource
        popup.setMarginBottom(marginV);
        popup.setMarginTop(marginV);
        popup.setMarginLeft(anchor.getPaddingLeft());
        popup.setMarginRight(anchor.getPaddingRight());
        popup.setBoundsView((View) anchor.getParent());

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
        popup.setSelectionInitial(position);

        return true;
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

    private void onItemSelected(int position) {
        String value = mEntryValues[position].toString();
        if (callChangeListener(value)) {
            setValue(value);
        }
    }

    public void setEntries(CharSequence[] entries) {
        this.mEntries = entries;
    }

    public void setEntries(@ArrayRes int entriesResId) {
        this.setEntries(this.getContext().getResources().getTextArray(entriesResId));
    }

    public CharSequence[] getEntries() {
        return this.mEntries;
    }

    public void setEntryValues(CharSequence[] entryValues) {
        this.mEntryValues = entryValues;
    }

    public void setEntryValues(@ArrayRes int entryValuesResId) {
        this.setEntryValues(this.getContext().getResources().getTextArray(entryValuesResId));
    }

    public CharSequence[] getEntryValues() {
        return this.mEntryValues;
    }

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

    public CharSequence getSummary() {
        CharSequence entry = this.getEntry();
        return this.mSummary == null ? super.getSummary() : String.format(this.mSummary, entry == null ? "" : entry);
    }

    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
        if (summary == null && this.mSummary != null) {
            this.mSummary = null;
        } else if (summary != null && !summary.equals(this.mSummary)) {
            this.mSummary = summary.toString();
        }

    }

    public void setValueIndex(int index) {
        if (this.mEntryValues != null) {
            this.setValue(this.mEntryValues[index].toString());
        }

    }

    public String getValue() {
        return this.mValue;
    }

    public CharSequence getEntry() {
        int index = this.getValueIndex();
        return index >= 0 && this.mEntries != null ? this.mEntries[index] : null;
    }

    public int findIndexOfValue(String value) {
        if (value != null && this.mEntryValues != null) {
            for (int i = this.mEntryValues.length - 1; i >= 0; --i) {
                if (this.mEntryValues[i].equals(value)) {
                    return i;
                }
            }
        }

        return -1;
    }

    private int getValueIndex() {
        return this.findIndexOfValue(this.mValue);
    }

    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

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

    static class CheckedItemAdapter extends ArrayAdapter<CharSequence> {
        private int mSelection = -1;

        public CheckedItemAdapter(Context context, int resource, int textViewResourceId,
                                  CharSequence[] objects) {
            super(context, resource, textViewResourceId, objects);
        }

        public void setSelection(int selection) {
            mSelection = selection;
            notifyDataSetChanged();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            if (position == mSelection) {
                int bgId = Util.resolveResourceId(view.getContext(), R.attr.colorControlHighlight, 0);
                view.setBackgroundResource(bgId);
            } else {
                view.setBackgroundResource(0);
            }
            return view;
        }
    }
}
