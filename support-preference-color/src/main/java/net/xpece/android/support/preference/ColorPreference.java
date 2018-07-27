package net.xpece.android.support.preference;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.ArrayRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

import net.xpece.android.colorpicker.ColorPickerPalette;
import net.xpece.android.support.preference.color.R;

/**
 * @author Eugen on 12. 3. 2016.
 */
public class ColorPreference extends DialogPreference {
    @ColorInt private final static int DEFAULT_COLOR = Color.BLACK;
    @ColorPickerPalette.SwatchSize private static final int DEFAULT_SWATCH_SIZE = ColorPickerPalette.SIZE_SMALL;
    private static final int DEFAULT_COLUMN_COUNT = 4;

    private int[] mColorValues;
    private CharSequence[] mColorNames;
    @ColorPickerPalette.SwatchSize private int mSwatchSize;
    private int mColumnCount;

    @ColorInt private int mColor = DEFAULT_COLOR;

    private Drawable mDrawable;

    @ColorInt
    public int getColor() {
        return mColor;
    }

    public void setColorResource(@ColorRes final int colorRes) {
//        final ColorStateList colors = Util.getColorStateListCompat(getContext(), colorRes);
//        final int color = colors.getDefaultColor();
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

    public int findIndexOfValue(@ColorInt int color) {
        if (mColorValues != null) {
            final int size = mColorValues.length;
            for (int i = 0; i < size; i++) {
                if (mColorValues[i] == color) return i;
            }
        }
        return -1;
    }

    @Nullable
    public CharSequence getNameForColor(@ColorInt int color) {
        CharSequence[] names = getColorNames();
        if (names != null) {
            int index = findIndexOfValue(color);
            return names[index];
        }
        return null;
    }

    public ColorPreference(final @NonNull Context context, @Nullable final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public ColorPreference(final @NonNull Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_Asp_Material_DialogPreference_ColorPreference);
    }

    public ColorPreference(final @NonNull Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, R.attr.colorPreferenceStyle);
    }

    public ColorPreference(final @NonNull Context context) {
        this(context, null);
    }

    private void init(final @NonNull Context context, @Nullable final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorPreference, defStyleAttr, defStyleRes);
        final int colorValuesRes = a.getResourceId(R.styleable.ColorPreference_android_entryValues, 0);
        if (colorValuesRes != 0) {
            mColorValues = arrayResToColors(context, colorValuesRes);
        }
        mColorNames = a.getTextArray(R.styleable.ColorPreference_android_entries);
        //noinspection WrongConstant
        mSwatchSize = a.getInteger(R.styleable.ColorPreference_asp_swatchSize, DEFAULT_SWATCH_SIZE);
        mColumnCount = a.getInteger(R.styleable.ColorPreference_asp_columnCount, DEFAULT_COLUMN_COUNT);
        a.recycle();
    }

    @NonNull
    private int[] arrayResToColors(final @NonNull Context context, @ArrayRes final int colorValuesRes) {
        final Resources.Theme theme = context.getTheme();
        final TypedArray b = context.getResources().obtainTypedArray(colorValuesRes);
        final int size = b.length();
        int[] colorValues = new int[size];
        for (int i = 0; i < size; i++) {
            colorValues[i] = resolveColor(b, i, theme);
        }
        b.recycle();
        return colorValues;
    }

    public void setColorValues(@ArrayRes int arrayRes) {
        mColorValues = arrayResToColors(getContext(), arrayRes);
    }

    public void setColorValues(@Nullable int[] array) {
        mColorValues = array;
    }

    @Nullable
    public int[] getColorValues() {
        return mColorValues;
    }

    @Nullable
    public CharSequence[] getColorNames() {
        return mColorNames;
    }

    public void setColorNames(@ArrayRes int arrayRes) {
        mColorNames = getContext().getResources().getTextArray(arrayRes);
    }

    public void setColorNames(@Nullable CharSequence[] array) {
        mColorNames = array;
    }

    private static int resolveColor(@NonNull TypedArray ca, int i, @NonNull Resources.Theme theme) {
        if (ca.peekValue(i).type == TypedValue.TYPE_ATTRIBUTE) {
            final TypedValue typedValue = new TypedValue();
            theme.resolveAttribute(ca.peekValue(i).data, typedValue, true);
            return typedValue.data;
        } else {
            return ca.getColor(i, 0);
        }
    }

    @ColorPickerPalette.SwatchSize
    public int getSwatchSize() {
        return mSwatchSize;
    }

    public int getColumnCount() {
        return mColumnCount;
    }

    public void setSwatchSize(@ColorPickerPalette.SwatchSize final int swatchSize) {
        mSwatchSize = swatchSize;
    }

    public void setColumnCount(final int columnCount) {
        mColumnCount = columnCount;
    }

    @Override
    public void onBindViewHolder(final @NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        if (mDrawable == null) {
            mDrawable = buildColorDrawable(mColor);
        }

        final ImageView imageView = (ImageView) holder.findViewById(R.id.image);
        imageView.setImageDrawable(mDrawable);
    }

    @NonNull
    public Drawable buildColorDrawable(@ColorInt int color) {
        Context context = getContext();

        int size = Util.dpToPxSize(context, 40);
        GradientDrawable active = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{color, color});
        active.setShape(GradientDrawable.OVAL);
        active.setSize(size, size);

        int stroke = Util.dpToPxSize(context, 1);
        int dark = ColorUtils.blendARGB(color, Color.BLACK, 0.12f);
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
    }

    @ColorInt
    @Override
    protected Integer onGetDefaultValue(final @NonNull TypedArray a, final int index) {
        return a.getInt(index, DEFAULT_COLOR);
    }

    @Override
    protected void onSetInitialValue(final boolean restoreValue, final @Nullable Object defaultValue) {
        //noinspection ResourceAsColor
        setColor(restoreValue ? getPersistedInt(mColor) : (int) defaultValue);
    }
}
