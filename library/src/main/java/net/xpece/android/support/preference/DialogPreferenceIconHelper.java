package net.xpece.android.support.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;

/**
 * @author Eugen on 6. 12. 2015.
 */
public class DialogPreferenceIconHelper extends PreferenceIconHelper {

    private final DialogPreference mPreference;

    private final Context mAlertDialogContext;

    public DialogPreferenceIconHelper(DialogPreference preference) {
        super(preference);
        mPreference = preference;

        final Context context = mPreference.getContext();
        int alertDialogTheme = Util.resolveResourceId(context, R.attr.alertDialogTheme, 0);
        mAlertDialogContext = new ContextThemeWrapper(context, alertDialogTheme);
    }

    @Override
    public Context getContext() {
        return mAlertDialogContext;
    }

    @Override
    public void loadFromAttributes(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        Context context = getContext();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Preference, defStyleAttr, defStyleRes);
        for (int i = a.getIndexCount() - 1; i >= 0; i--) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.Preference_asp_tint) {
                ensureTintInfo();
                mTintInfo.mTintList = getTintList(a, attr, context);
            } else if (attr == R.styleable.Preference_asp_tintMode) {
                ensureTintInfo();
                mTintInfo.mTintMode = PorterDuff.Mode.values()[a.getInt(attr, 0)];
            }
        }
        a.recycle();

        a = context.obtainStyledAttributes(attrs, R.styleable.DialogPreference, defStyleAttr, defStyleRes);
        for (int i = a.getIndexCount() - 1; i >= 0; i--) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.DialogPreference_android_dialogIcon) {
                mIconResId = a.getResourceId(attr, 0);
            } else if (attr == R.styleable.DialogPreference_asp_dialogTintEnabled) {
                mIconTintEnabled = a.getBoolean(attr, false);
            } else if (attr == R.styleable.DialogPreference_asp_dialogTint) {
                ensureTintInfo();
                mTintInfo.mTintList = getTintList(a, attr, context);
            } else if (attr == R.styleable.DialogPreference_asp_dialogTintMode) {
                ensureTintInfo();
                mTintInfo.mTintMode = PorterDuff.Mode.values()[a.getInt(attr, 0)];
            } else if (attr == R.styleable.DialogPreference_asp_dialogIconPaddingEnabled) {
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
