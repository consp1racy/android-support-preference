//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.xpece.android.support.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

public class ListPreference extends DialogPreference {
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private String mValue;
    private String mSummary;
    private boolean mValueSet;

    private boolean mSimple = true;

    public ListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ListPreference, defStyleAttr, defStyleRes);
        this.mEntries = a.getTextArray(R.styleable.ListPreference_android_entries);
        this.mEntryValues = a.getTextArray(R.styleable.ListPreference_android_entryValues);
        this.mSimple = a.getBoolean(R.styleable.ListPreference_asp_simpleMenu, false);
        a.recycle();
        a = context.obtainStyledAttributes(attrs, R.styleable.Preference, defStyleAttr, defStyleRes);
        this.mSummary = a.getString(R.styleable.Preference_android_summary);
        a.recycle();
    }

    public ListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ListPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public ListPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void performClick(View view) {
        if (mSimple) {
            if (this.isEnabled()) {
                showAsPopup(view);
            }
        } else {
            super.performClick(view);
        }
    }

    private void showAsPopup(View view) {
        final int layout = R.layout.asp_simple_spinner_dropdown_item;
        final Context context = getContext();

        final int position = findIndexOfValue(getValue());

        final CheckedItemAdapter adapter = new CheckedItemAdapter(context, layout, android.R.id.text1, mEntries);
        adapter.setSelection(position);

        final XpListPopupWindow popup = new XpListPopupWindow(context, null);
        popup.setModal(true);
        popup.setAnchorView(view);
        popup.setAdapter(adapter);

        repositionPopup(popup, view, position);

        popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListPreference.this.onItemSelected(position);
                popup.dismiss();
            }
        });

        popup.show();
    }

    private void repositionPopup(XpListPopupWindow popup, View anchor, int position) {
        final Context context = anchor.getContext();

        // Shadow is emulated below Lollipop, we have to account for that.
        final Rect backgroundPadding = new Rect();
        popup.getBackground().getPadding(backgroundPadding);
        final int backgroundPaddingStart;
        final int backgroundPaddingEnd;
        if (ViewCompat.getLayoutDirection(anchor) == ViewCompat.LAYOUT_DIRECTION_RTL) {
            backgroundPaddingStart = backgroundPadding.right;
            backgroundPaddingEnd = backgroundPadding.left;
        } else {
            backgroundPaddingStart = backgroundPadding.left;
            backgroundPaddingEnd = backgroundPadding.right;
        }
        final int backgroundPaddingTop = backgroundPadding.top;

        // Respect anchor view's padding.
        final int paddingStart = ViewCompat.getPaddingStart(anchor);
        final int paddingEnd = ViewCompat.getPaddingEnd(anchor);
        final int width = anchor.getWidth();
        final int preferredWidth = width - paddingEnd - paddingStart + backgroundPaddingEnd + backgroundPaddingStart;
        if (preferredWidth < width) {
            popup.setWidth(XpListPopupWindow.PREFERRED);
            popup.setMaxWidth(preferredWidth);
            popup.setHorizontalOffset(paddingStart - backgroundPaddingStart);
        }

        // Center selected item over anchor view.
        if (position < 0) position = 0;
        final int height = Util.resolveDimensionPixelSize(context, R.attr.dropdownListPreferredItemHeight, 0);
        final int viewHeight = anchor.getHeight();
        final int dropDownListViewStyle = Util.resolveResourceId(context, R.attr.dropDownListViewStyle, R.style.Widget_Material_ListView_DropDown);
        final int dropDownListViewPaddingTop = Util.resolveDimensionPixelOffset(context, dropDownListViewStyle, android.R.attr.paddingTop, 0);
        final int offset = -(height * (position + 1) + (viewHeight - height) / 2 + dropDownListViewPaddingTop + backgroundPaddingTop);
        popup.setVerticalOffset(offset);
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
        if (this.isPersistent()) {
            return superState;
        } else {
            ListPreference.SavedState myState = new ListPreference.SavedState(superState);
            myState.value = this.getValue();
            return myState;
        }
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state != null && state.getClass().equals(ListPreference.SavedState.class)) {
            ListPreference.SavedState myState = (ListPreference.SavedState) state;
            super.onRestoreInstanceState(myState.getSuperState());
            this.setValue(myState.value);
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    private static class SavedState extends BaseSavedState {
        String value;
        public static final Creator<SavedState> CREATOR = new Creator<ListPreference.SavedState>() {
            public ListPreference.SavedState createFromParcel(Parcel in) {
                return new ListPreference.SavedState(in);
            }

            public ListPreference.SavedState[] newArray(int size) {
                return new ListPreference.SavedState[size];
            }
        };

        public SavedState(Parcel source) {
            super(source);
            this.value = source.readString();
        }

        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(this.value);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    private static class CheckedItemAdapter extends ArrayAdapter<CharSequence> {
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
