package net.xpece.android.support.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.colorpicker.R;

/**
 * @author Eugen on 12. 3. 2016.
 */
public class ColorPreference extends DialogPreference {
    @ColorInt private final static int DEFAULT_COLOR = Color.BLACK;

    @ColorInt private int mColor;

    private Drawable mDrawable;

    @ColorInt
    public int getColor() {
        return mColor;
    }

    public void setColorResource(@ColorRes final int colorRes) {
        int color = ContextCompat.getColor(getContext(), colorRes);
        setColor(color);
    }

    public void setColor(@ColorInt final int color) {
        if (mColor != color) {
            mDrawable = null;

            mColor = color;
            persistInt(color);
            notifyChanged();
        }
    }

    public ColorPreference(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public ColorPreference(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorPreference(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.colorPreferenceStyle);
    }

    public ColorPreference(final Context context) {
        this(context, null);
    }

    private void init(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
    }

    @Override
    public void onBindViewHolder(final PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        if (mDrawable == null) {
            mDrawable = buildColorDrawable(mColor);
        }

        ImageView imageView = (ImageView) holder.findViewById(R.id.image);
        imageView.setImageDrawable(mDrawable);
    }

    public Drawable buildColorDrawable(@ColorInt int color) {
        Context context = getContext();

        int size = Util.dpToPxSize(context, 40);
        GradientDrawable active = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{color, color});
        active.setShape(GradientDrawable.OVAL);
        active.setSize(size, size);

        int stroke = Util.dpToPxSize(context, 1);
        int dark = ColorUtils.blendARGB(color, Color.BLACK, 0.25f);
        active.setStroke(stroke, dark);

        int disabledAlpha = (int) (Util.resolveFloat(context, android.R.attr.disabledAlpha, 0.5f) * 255);
        int disabledColor = ColorUtils.setAlphaComponent(color, disabledAlpha);

        GradientDrawable passive = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{disabledColor, disabledColor});
        passive.setShape(GradientDrawable.OVAL);
        passive.setSize(size, size);

        StateListDrawable sld = new StateListDrawable();
        sld.addState(Util.DISABLED_STATE_SET, passive);
        sld.addState(Util.EMPTY_STATE_SET, active);
        return sld;

//        int padding = Util.dpToPxOffset(context, 2);
//        Drawable id = XpInsetDrawable.create(active, padding);
//        return id;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, DEFAULT_COLOR);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        //noinspection ResourceAsColor
        setColor(restoreValue ? getPersistedInt(mColor) : (int) defaultValue);
    }

}
