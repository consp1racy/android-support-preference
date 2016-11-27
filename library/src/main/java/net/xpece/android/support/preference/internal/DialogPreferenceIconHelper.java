package net.xpece.android.support.preference.internal;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.support.annotation.RestrictTo;
import android.support.v7.preference.DialogPreference;
import android.support.v7.widget.TintTypedArray;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;

import net.xpece.android.support.preference.R;

/**
 * @author Eugen on 6. 12. 2015.
 */
@RestrictTo(RestrictTo.Scope.GROUP_ID)
@SuppressWarnings("RestrictedApi")
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

        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs, R.styleable.Preference, defStyleAttr, defStyleRes);
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

        a = TintTypedArray.obtainStyledAttributes(context, attrs, R.styleable.DialogPreference, defStyleAttr, defStyleRes);
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
    protected ColorStateList getTintList(TintTypedArray a, int attr, Context context) {
        ColorStateList csl = a.getColorStateList(attr);
//        csl = withDisabled(csl, context);
        return csl;
    }

    @Override
    protected void onSetIcon() {
        mPreference.setDialogIcon(mIcon);
    }
}
