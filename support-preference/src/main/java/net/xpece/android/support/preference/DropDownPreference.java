package net.xpece.android.support.preference;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;

import net.xpece.android.support.widget.XpListPopupWindow;

/**
 * A version of {@link android.support.v7.preference.ListPreference} that presents the options in a
 * drop down menu rather than a dialog.
 * <p></p>
 * <strong>Note:</strong> Android Support Library version uses an invisible spinner to invoke
 * the popup. This class uses {@link XpListPopupWindow} directly. There is no spinner.
 * If you need to supply your own spinner in a custom layout, also create a custom preference
 * that extends {@link android.support.v7.preference.DropDownPreference} instead of this class.
 * Support for advanced XML styling attributes will be retained.
 */
public class DropDownPreference extends ListPreference {
    public DropDownPreference(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        if (getMenuMode() != MENU_MODE_SIMPLE_MENU) {
            Log.w("DropDownPreference", "This version of DropDownPreference can only be used with menu mode 'simple_menu'." +
                    "\nIf you want other options use ListPreference.");
            setMenuMode(MENU_MODE_SIMPLE_MENU);
        }
    }

    public DropDownPreference(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_Asp_Material_DropDownPreference);
    }

    public DropDownPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.dropdownPreferenceStyle);
    }

    public DropDownPreference(@NonNull Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        if (holder.findViewById(R.id.spinner) != null) {
            Log.w("DropDownPreference", "This version of DropDownPreference doesn't work with a Spinner in the layout." +
                    "\na) Please remove the Spinner from your layout." +
                    "\nb) Extend and use Android Support Library DropDownPreference.");
        }
        super.onBindViewHolder(holder);
    }
}
