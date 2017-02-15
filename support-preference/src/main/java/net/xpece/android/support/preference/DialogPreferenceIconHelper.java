package net.xpece.android.support.preference;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.support.v7.preference.DialogPreference;
import android.support.v7.widget.TintTypedArray;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;

import java.lang.ref.WeakReference;

/**
 * @author Eugen on 6. 12. 2015.
 */
public class DialogPreferenceIconHelper extends PreferenceIconHelper {

    private final WeakReference<DialogPreference> mPreference;

    public DialogPreferenceIconHelper(DialogPreference preference) {
        super(preference);
        mPreference = new WeakReference<>(preference);
    }

    @Override
    public Context getContext() {
        final Context context = super.getContext();
        int alertDialogTheme = Util.resolveResourceId(context, R.attr.alertDialogTheme, 0);
        return new ContextThemeWrapper(context, alertDialogTheme);
    }

    @Override
    protected DialogPreference getPreference() {
        return mPreference.get();
    }

    @SuppressWarnings("RestrictedApi")
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

    @SuppressWarnings("RestrictedApi")
    @Override
    protected ColorStateList getTintList(TintTypedArray a, int attr, Context context) {
        ColorStateList csl = a.getColorStateList(attr);
//        csl = withDisabled(csl, context);
        return csl;
    }

    @Override
    protected void onSetIcon() {
        getPreference().setDialogIcon(mIcon);
    }
}
