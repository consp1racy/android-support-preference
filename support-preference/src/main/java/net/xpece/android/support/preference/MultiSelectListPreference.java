/*
 * Copyright (C) 2010 The Android Open Source Project
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

package net.xpece.android.support.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ArrayRes;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A {@link Preference} that displays a list of entries as
 * a dialog.
 * <p></p>
 * This preference will store a set of strings into the SharedPreferences.
 * This set will contain one or more values from the
 * {@link #setEntryValues(CharSequence[])} array.
 */
public class MultiSelectListPreference extends DialogPreference {
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;

    private final Set<String> mValues = new HashSet<>();

    public MultiSelectListPreference(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public MultiSelectListPreference(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_Asp_Material_DialogPreference);
    }

    public MultiSelectListPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.multiSelectListPreferenceStyle);
    }

    public MultiSelectListPreference(@NonNull Context context) {
        this(context, null);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ListPreference, defStyleAttr, defStyleRes);
        mEntries = a.getTextArray(R.styleable.ListPreference_android_entries);
        mEntryValues = a.getTextArray(R.styleable.ListPreference_android_entryValues);
        a.recycle();
    }

    /**
     * Sets the human-readable entries to be shown in the list. This will be
     * shown in subsequent dialogs.
     * <p></p>
     * Each entry must have a corresponding index in
     * {@link #setEntryValues(CharSequence[])}.
     *
     * @param entries The entries.
     * @see #setEntryValues(CharSequence[])
     */
    public void setEntries(@NonNull CharSequence[] entries) {
        mEntries = entries;
    }

    /**
     * @param entriesResId The entries array as a resource.
     * @see #setEntries(CharSequence[])
     */
    public void setEntries(@ArrayRes int entriesResId) {
        setEntries(getContext().getResources().getTextArray(entriesResId));
    }

    /**
     * The list of entries to be shown in the list in subsequent dialogs.
     *
     * @return The list as an array.
     */
    @Nullable
    public CharSequence[] getEntries() {
        return mEntries;
    }

    /**
     * The array to find the value to save for a preference when an entry from
     * entries is selected. If a user clicks on the second item in entries, the
     * second item in this array will be saved to the preference.
     *
     * @param entryValues The array to be used as values to save for the preference.
     */
    public void setEntryValues(@NonNull CharSequence[] entryValues) {
        mEntryValues = entryValues;
    }

    /**
     * @param entryValuesResId The entry values array as a resource.
     * @see #setEntryValues(CharSequence[])
     */
    public void setEntryValues(@ArrayRes int entryValuesResId) {
        setEntryValues(getContext().getResources().getTextArray(entryValuesResId));
    }

    /**
     * Returns the array of values to be saved for the preference.
     *
     * @return The array of values.
     */
    @Nullable
    public CharSequence[] getEntryValues() {
        return mEntryValues;
    }

    /**
     * Sets the value of the key. This should contain entries in
     * {@link #getEntryValues()}.
     *
     * @param values The values to set for the key.
     */
    public void setValues(@NonNull Set<String> values) {
        mValues.clear();
        mValues.addAll(values);

        XpPreference.persistStringSet(this, values);
    }

    /**
     * Retrieves the current value of the key.
     */
    @NonNull
    public Set<String> getValues() {
        return Collections.unmodifiableSet(mValues);
    }

    /**
     * Returns the index of the given value (in the entry values array).
     *
     * @param value The value whose index should be returned.
     * @return The index of the value, or -1 if not found.
     */
    public int findIndexOfValue(@Nullable String value) {
        final CharSequence[] entryValues = getEntryValues();
        if (value != null && entryValues != null) {
            for (int i = entryValues.length - 1; i >= 0; --i) {
                if (value.contentEquals(entryValues[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    @NonNull
    public boolean[] getSelectedItems() {
        final CharSequence[] entries = mEntryValues;
        final int entryCount = entries.length;
        final Set<String> values = mValues;
        boolean[] result = new boolean[entryCount];

        for (int i = 0; i < entryCount; i++) {
            result[i] = values.contains(entries[i].toString());
        }

        return result;
    }

    @NonNull
    @Override
    protected Set<String> onGetDefaultValue(@NonNull TypedArray a, int index) {
        final Set<String> result = new HashSet<>();
        try {
            final CharSequence[] defaultValues = a.getTextArray(index);
            final int valueCount = defaultValues == null ? 0 : defaultValues.length;

            for (int i = 0; i < valueCount; i++) {
                result.add(defaultValues[i].toString());
            }
        } catch (NullPointerException ignore) {
            // TADA! Now you don't need to specify an empty array in XML.
        }
        return result;
    }

    @Override
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    protected void onSetInitialValue(boolean restoreValue, @Nullable Object defaultValue) {
        // Values are never null here. Preference class code has checks against it.
        // We either restore value, and if not found it falls back to non-null mValues,
        // or we don't restore value and this method is only called when defaultValue is not null.
        setValues(restoreValue ? XpPreference.getPersistedStringSet(this, mValues) : (Set<String>) defaultValue);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        assert superState != null;
        if (isPersistent()) {
            // No need to save instance state
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.values = getValues();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Parcelable state) {
        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setValues(myState.values);
    }

    private static class SavedState extends BaseSavedState {
        Set<String> values;

        public SavedState(@NonNull Parcel source) {
            super(source);
            values = new HashSet<>();
            String[] strings = source.createStringArray();
            Collections.addAll(values, strings);
        }

        public SavedState(@NonNull Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeStringArray(values.toArray(new String[values.size()]));
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @NonNull
                    public SavedState createFromParcel(@NonNull Parcel in) {
                        return new SavedState(in);
                    }

                    @NonNull
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
