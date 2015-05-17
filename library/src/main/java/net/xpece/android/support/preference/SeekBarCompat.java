package net.xpece.android.support.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;
import android.util.StateSet;
import android.widget.AbsSeekBar;
import android.widget.SeekBar;

import java.lang.reflect.Field;

/**
 * Created by Eugen on 17. 5. 2015.
 */
public class SeekBarCompat {

    private static final Field FIELD_THUMB;

    static {
        Field thumb = null;
        if (Build.VERSION.SDK_INT < 16) {
            try {
                thumb = AbsSeekBar.class.getDeclaredField("mThumb");
                thumb.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        FIELD_THUMB = thumb;
    }

    private SeekBarCompat() {}

    public static void styleSeekBar(SeekBar seekBar) {
        Context context = seekBar.getContext();
        TypedArray ta = context.obtainStyledAttributes(new int[]{R.attr.colorControlNormal, R.attr.colorControlActivated});
        int colorControlNormal = ta.getColor(0, Color.GRAY);
        int colorControlActivated = ta.getColor(1, Color.CYAN);
        ta.recycle();

        if (Build.VERSION.SDK_INT < 21 && seekBar.getClass() == SeekBar.class) {
            LayerDrawable progressComposite = (LayerDrawable) seekBar.getProgressDrawable();

            Drawable progress = progressComposite.findDrawableByLayerId(android.R.id.progress);
            if (progress != null) {
                progress.mutate();
                progress.setColorFilter(colorControlActivated, PorterDuff.Mode.SRC_IN);
            }

            Drawable secondaryProgress = progressComposite.findDrawableByLayerId(android.R.id.secondaryProgress);
            if (secondaryProgress != null) {
                secondaryProgress.mutate();
                secondaryProgress.setColorFilter(colorControlActivated, PorterDuff.Mode.SRC_IN);
            }

            Drawable background = progressComposite.findDrawableByLayerId(android.R.id.background);
            if (background != null) {
                background.mutate();
                background.setColorFilter(colorControlNormal, PorterDuff.Mode.SRC_IN);
            }

            if (Build.VERSION.SDK_INT < 11) {
                colorControlActivated = ColorUtils.setAlphaComponent(colorControlActivated, 102);
                try {
                    Drawable thumbNormal = getThumb(seekBar).getCurrent().mutate();
                    Drawable thumbActivated = thumbNormal.getConstantState().newDrawable().mutate();

                    ColorFilter colorFilterActivated = new PorterDuffColorFilter(colorControlActivated, PorterDuff.Mode.SRC_ATOP);
//                    thumbActivated.setColorFilter(colorFilterActivated);

                    FilterableStateListDrawable thumb = new FilterableStateListDrawable();
                    thumb.addState(new int[]{android.R.attr.state_window_focused, android.R.attr.state_pressed}, thumbActivated, colorFilterActivated);
                    thumb.addState(new int[]{android.R.attr.state_window_focused, android.R.attr.state_focused}, thumbActivated, colorFilterActivated);
                    thumb.addState(new int[]{android.R.attr.state_window_focused, android.R.attr.state_selected}, thumbActivated, colorFilterActivated);
                    thumb.addState(StateSet.WILD_CARD, thumbNormal);
                    seekBar.setThumb(thumb);
                } catch (Exception ex) {
                    getThumb(seekBar).setColorFilter(colorControlActivated, PorterDuff.Mode.SRC_IN);
                }
            } else {
                getThumb(seekBar).setColorFilter(colorControlActivated, PorterDuff.Mode.SRC_IN);
            }
        }
    }

    @TargetApi(16)
    @NonNull
    public static Drawable getThumb(AbsSeekBar seekBar) {
        if (Build.VERSION.SDK_INT < 16) {
            try {
                return (Drawable) FIELD_THUMB.get(seekBar);
            } catch (Exception ex) {
                ex.printStackTrace();
                return new ColorDrawable(0);
            }
        } else {
            return seekBar.getThumb();
        }
    }

}
