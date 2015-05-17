package net.xpece.android.support.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.util.StateSet;
import android.view.View;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Eugen on 13. 5. 2015.
 */
class Util {
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
}
