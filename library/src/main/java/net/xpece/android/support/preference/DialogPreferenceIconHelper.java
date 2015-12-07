package net.xpece.android.support.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

/**
 * @author Eugen on 6. 12. 2015.
 */
class DialogPreferenceIconHelper extends PreferenceIconHelper {

    private final DialogPreference mPreference;

    public DialogPreferenceIconHelper(DialogPreference preference) {
        super(preference);
        mPreference = preference;
    }

    @Override
    public void loadFromAttributes(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        Context context = mPreference.getContext();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.XpPreference, defStyleAttr, defStyleRes);
        for (int i = a.getIndexCount() - 1; i >= 0; i--) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.XpPreference_asp_tint) {
                ensureTintInfo();
                mTintInfo.mTintList = a.getColorStateList(attr);
            } else if (attr == R.styleable.XpPreference_asp_tintMode) {
                ensureTintInfo();
                mTintInfo.mTintMode = PorterDuff.Mode.values()[a.getInt(attr, 0)];
            }
        }
        a.recycle();

        a = context.obtainStyledAttributes(attrs, R.styleable.XpDialogPreference, defStyleAttr, defStyleRes);
        for (int i = a.getIndexCount() - 1; i >= 0; i--) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.XpDialogPreference_android_dialogIcon) {
                mIconResId = a.getResourceId(attr, 0);
            } else if (attr == R.styleable.XpDialogPreference_asp_dialogTintEnabled) {
                mIconTintEnabled = a.getBoolean(attr, false);
            } else if (attr == R.styleable.XpDialogPreference_asp_dialogTint) {
                ensureTintInfo();
                mTintInfo.mTintList = a.getColorStateList(attr);
            } else if (attr == R.styleable.XpDialogPreference_asp_dialogTintMode) {
                ensureTintInfo();
                mTintInfo.mTintMode = PorterDuff.Mode.values()[a.getInt(attr, 0)];
            } else if (attr == R.styleable.XpDialogPreference_asp_dialogIconPaddingEnabled) {
                mIconPaddingEnabled = a.getBoolean(attr, false);
            }
        }
        a.recycle();

        if (mIconResId != 0) {
            setIcon(mIconResId);
        }
    }

    @Override
    protected void onSetIcon() {
        mPreference.setDialogIcon(mIcon);
    }
}
