package net.xpece.android.support.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.util.StateSet;
import android.util.TypedValue;
import android.view.View;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created by Eugen on 13. 5. 2015.
 */
public class Util {

    private static final Method METHOD_CONTEXT_GET_THEME_RES_ID;

    static {
        Method contextGetThemeResId = null;
        try {
            contextGetThemeResId = Context.class.getMethod("getThemeResId");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        METHOD_CONTEXT_GET_THEME_RES_ID = contextGetThemeResId;
    }

    private Util() {}

    public static Drawable resolveDrawable(Context context, @AttrRes int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        Drawable d = ta.getDrawable(0);
        ta.recycle();
        return d;
    }

    public static int resolveColor(Context context, @AttrRes int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        int c = ta.getColor(0, 0);
        ta.recycle();
        return c;
    }

    public static float dpToPx(Context context, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int dpToPxOffset(Context context, int dp) {
        return (int) (dpToPx(context, dp));
    }

    public static int dpToPxSize(Context context, int dp) {
        return (int) (0.5f + dpToPx(context, dp));
    }

    public static Drawable addDrawablePadding(Drawable drawable, int paddingPx) {
        int totalPadding = paddingPx * 2;
        GradientDrawable shape = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0, 0});
        shape.setSize(drawable.getIntrinsicWidth() + totalPadding, drawable.getIntrinsicHeight() + totalPadding);
        Drawable inset = new InsetDrawable(drawable, paddingPx);
        return new LayerDrawable(new Drawable[]{shape, inset});
    }

    /**
     * Copies {@code newLength} elements from {@code original} into a new array.
     * If {@code newLength} is greater than {@code original.length}, the result is padded
     * with the value {@code false}.
     *
     * @param original the original array
     * @param newLength the length of the new array
     * @return the new array
     * @throws NegativeArraySizeException if {@code newLength < 0}
     * @throws NullPointerException if {@code original == null}
     * @since 1.6
     */
    public static boolean[] copyOf(boolean[] original, int newLength) {
        if (newLength < 0) {
            throw new NegativeArraySizeException(Integer.toString(newLength));
        }
        return copyOfRange(original, 0, newLength);
    }

    /**
     * Copies elements from {@code original} into a new array, from indexes start (inclusive) to
     * end (exclusive). The original order of elements is preserved.
     * If {@code end} is greater than {@code original.length}, the result is padded
     * with the value {@code false}.
     *
     * @param original the original array
     * @param start the start index, inclusive
     * @param end the end index, exclusive
     * @return the new array
     * @throws ArrayIndexOutOfBoundsException if {@code start < 0 || start > original.length}
     * @throws IllegalArgumentException if {@code start > end}
     * @throws NullPointerException if {@code original == null}
     * @since 1.6
     */
    public static boolean[] copyOfRange(boolean[] original, int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException();
        }
        int originalLength = original.length;
        if (start < 0 || start > originalLength) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        boolean[] result = new boolean[resultLength];
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Drawable createActivatedBackground(View layout) {
        Context context = layout.getContext();
        StateListDrawable d = new StateListDrawable();
        int activated = Util.resolveColor(context, R.attr.colorControlActivated);
        d.addState(new int[]{android.R.attr.state_activated}, new ColorDrawable(activated));
        d.addState(StateSet.WILD_CARD, new ColorDrawable(0));
        return null;
    }

    public static void skipCurrentTag(XmlPullParser parser)
        throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        int type;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
            && (type != XmlPullParser.END_TAG
            || parser.getDepth() > outerDepth)) {
        }
    }

    static Object tryInvoke(Method method, Object receiver, Object... args) {
        try {
            return method.invoke(receiver, args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /** Needed for some internal implementation...  not public because
     * you can't assume this actually means anything. */
    public static int getThemeResId(Context context) {
        try {
            return (int) METHOD_CONTEXT_GET_THEME_RES_ID.invoke(context);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}
