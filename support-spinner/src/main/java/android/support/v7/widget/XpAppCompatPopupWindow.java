package android.support.v7.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionSet;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;

import net.xpece.android.support.widget.spinner.R;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

@SuppressLint("PrivateApi")
class XpAppCompatPopupWindow extends AppCompatPopupWindow {
    private static final String TAG = XpAppCompatPopupWindow.class.getSimpleName();

    private static final Field sAnchorField;

    static {
        final Class<PopupWindow> cls = PopupWindow.class;

        Field f = null;
        try {
            f = cls.getDeclaredField("mAnchor");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Log.i(TAG, "Could not find field mAnchor on PopupWindow. Oh well.");
        }
        sAnchorField = f;
    }

    private final Context mApplicationContext;

    public XpAppCompatPopupWindow(
            @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mApplicationContext = context.getApplicationContext();
        init();
    }

    public XpAppCompatPopupWindow(
            @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr,
            @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mApplicationContext = context.getApplicationContext();
        init();
    }

    private void init() {
        if (Build.VERSION.SDK_INT == 23) {
            // android:popupEnter/ExitTransition attributes are supported since API 24.
            // Popup transitions are supported since API 23. Choose reasonable defaults for API 23:
            setEnterTransition(getTransition(R.transition.asp_popup_window_enter));
            setExitTransition(getTransition(R.transition.asp_popup_window_exit));
        }
    }

    @Override
    public void showAtLocation(@NonNull View anchor, int gravity, int x, int y) {
        super.showAtLocation(anchor, gravity, x, y);
        setAnchorInternal(anchor);
    }

    private void setAnchorInternal(@Nullable View anchor) {
        try {
            sAnchorField.set(this, new WeakReference<>(anchor));
        } catch (Exception e) {
            Log.i(TAG, "Could not set mAnchor on PopupWindow. Oh well.");
        }
    }

    @RequiresApi(21)
    private Transition getTransition(int resId) {
        if (resId != 0) {
            final TransitionInflater inflater = TransitionInflater.from(mApplicationContext);
            final Transition transition = inflater.inflateTransition(resId);
            if (transition != null) {
                final boolean isEmpty = transition instanceof TransitionSet
                        && ((TransitionSet) transition).getTransitionCount() == 0;
                if (!isEmpty) {
                    return transition;
                }
            }
        }
        return null;
    }
}
