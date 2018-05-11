package android.support.v7.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.support.v4.view.ViewCompat;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionSet;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@SuppressLint("PrivateApi")
class XpAppCompatPopupWindow extends AppCompatPopupWindow {
    private static final String TAG = XpAppCompatPopupWindow.class.getSimpleName();

    private static final int ATTR_POPUP_ENTER_TRANSITION = 0x0101051f;
    private static final int ATTR_POPUP_EXIT_TRANSITION = 0x01010520;
    private static final int[] ATTRS = {ATTR_POPUP_ENTER_TRANSITION, ATTR_POPUP_EXIT_TRANSITION};

    // Exit transition doesn't work on API 24-25 unless anchor root attached state is in sync.
    // Otherwise only mAnchor would be enough for <insert when you remember>.
    private static final Field sAnchorField;
    private static final Field sAnchorRootField;
    private static final Field sIsAnchorRootAttachedField;
    private static final Field sOnAnchorRootDetachedListenerField;

//    private static final boolean SHOULD_FIX_TRANSITION = Build.VERSION.SDK_INT >= 23;
    private static final boolean SHOULD_FIX_TRANSITION = Build.VERSION.SDK_INT == 24 || Build.VERSION.SDK_INT == 25;

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

        if (SHOULD_FIX_TRANSITION) {
            try {
                f = cls.getDeclaredField("mAnchorRoot");
                f.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Log.i(TAG, "Could not find field mAnchorRoot on PopupWindow. Oh well.");
            }
            sAnchorRootField = f;

            try {
                f = cls.getDeclaredField("mIsAnchorRootAttached");
                f.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Log.i(TAG, "Could not find field mIsAnchorRootAttached on PopupWindow. Oh well.");
            }
            sIsAnchorRootAttachedField = f;

            try {
                f = cls.getDeclaredField("mOnAnchorRootDetachedListener");
                f.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Log.i(TAG, "Could not find field mOnAnchorRootDetachedListener on PopupWindow. Oh well.");
            }
            sOnAnchorRootDetachedListenerField = f;
        } else {
            sAnchorRootField = null;
            sIsAnchorRootAttachedField = null;
            sOnAnchorRootDetachedListenerField = null;
        }
    }

    private final Context mApplicationContext;

    public XpAppCompatPopupWindow(
            Context context,
            @Nullable AttributeSet attrs,
            @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mApplicationContext = context.getApplicationContext();
        init(context, attrs, defStyleAttr, 0);
    }

    public XpAppCompatPopupWindow(
            Context context,
            @Nullable AttributeSet attrs,
            @AttrRes int defStyleAttr,
            @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mApplicationContext = context.getApplicationContext();
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    @SuppressLint({"RestrictedApi", "ResourceType"})
    private void init(
            Context context,
            @Nullable AttributeSet attrs,
            @AttrRes int defStyleAttr,
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
    public void showAtLocation(View anchor, int gravity, int x, int y) {
        super.showAtLocation(anchor, gravity, x, y);
        setAnchorInternal(anchor);
    }

    @TargetApi(23)
    private void setAnchorInternal(final View anchor) {
        if (SHOULD_FIX_TRANSITION) {
            setAnchorMarshmallow(anchor);
        } else {
            setAnchorLegacy(anchor);
        }
    }

    private void setAnchorLegacy(final View anchor) {
        try {
            sAnchorField.set(this, new WeakReference<>(anchor));
        } catch (Exception e) {
            Log.i(TAG, "Could not set anchor on PopupWindow. Oh well.");
        }
    }

    @RequiresApi(23)
    private void setAnchorMarshmallow(final View anchor) {
        final View anchorRoot = anchor.getRootView();
        final boolean isAnchorRootAttached = ViewCompat.isAttachedToWindow(anchorRoot);

        try {
            final View.OnAttachStateChangeListener listener =
                    (View.OnAttachStateChangeListener) sOnAnchorRootDetachedListenerField.get(this);
            anchorRoot.addOnAttachStateChangeListener(listener);

            sAnchorField.set(this, new WeakReference<>(anchor));
            sAnchorRootField.set(this, new WeakReference<>(anchorRoot));
            sIsAnchorRootAttachedField.set(this, isAnchorRootAttached);
        } catch (Exception e) {
            Log.i(TAG, "Could not set anchor on PopupWindow. Oh well.");
        }
    }

    @Nullable
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
