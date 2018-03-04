package net.xpece.android.support.preference;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.AbsSeekBar;

import net.xpece.android.support.preference.plugins.XpSupportPreferencePlugins;

import java.lang.reflect.Field;

/**
 * Created by Eugen on 15.04.2016.
 */
class SeekBarCompat {
    private static final Field FIELD_THUMB;

    static {
        Field thumb = null;
        try {
            thumb = AbsSeekBar.class.getDeclaredField("mThumb");
            thumb.setAccessible(true);
        } catch (NoSuchFieldException e) {
            XpSupportPreferencePlugins.onError(e, "mThumb not available.");
        }
        FIELD_THUMB = thumb;
    }

    private SeekBarCompat() {
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static Drawable getThumb(AbsSeekBar seekBar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return seekBar.getThumb();
        } else if (FIELD_THUMB != null) {
            try {
                return (Drawable) FIELD_THUMB.get(seekBar);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return null;
    }
}
