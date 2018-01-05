package android.support.v7.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
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

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

@SuppressLint("PrivateApi")
class XpAppCompatPopupWindow extends AppCompatPopupWindow {
    private static final String TAG = XpAppCompatPopupWindow.class.getSimpleName();

    private static final int ATTR_POPUP_ENTER_TRANSITION = 0x0101051f;
    private static final int ATTR_POPUP_EXIT_TRANSITION = 0x01010520;
    private static final int[] ATTRS = {ATTR_POPUP_ENTER_TRANSITION, ATTR_POPUP_EXIT_TRANSITION};

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
        init(context, attrs, defStyleAttr, 0);
    }

    public XpAppCompatPopupWindow(
            @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr,
            @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mApplicationContext = context.getApplicationContext();
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    @SuppressLint({"RestrictedApi", "ResourceType"})
    private void init(
            @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr,
            @StyleRes int defStyleRes) {
        if (Build.VERSION.SDK_INT == 23) {
            final TypedArray a = context.obtainStyledAttributes(attrs, ATTRS, defStyleAttr, defStyleRes);
            try {
                final Transition enterTransition = getTransition(a.getResourceId(0, 0));
                final Transition exitTransition;
                if (a.hasValueOrEmpty(1)) {
                    exitTransition = getTransition(a.getResourceId(1, 0));
                } else {
                    exitTransition = enterTransition == null ? null : enterTransition.clone();
                }
                setEnterTransition(enterTransition);
                setExitTransition(exitTransition);
            } finally {
                a.recycle();
            }
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
