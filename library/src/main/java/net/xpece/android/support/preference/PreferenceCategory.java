package net.xpece.android.support.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.afollestad.materialdialogs.util.DialogUtils;

/**
 * Created by Anggrayudi on 22/02/2016.
 */
public class PreferenceCategory extends android.support.v7.preference.PreferenceCategory {

    public PreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PreferenceCategory);
        int titleColor = a.getColor(R.styleable.PreferenceCategory_titleColor, 0);
        a.recycle();

        if (titleColor != 0)
            color = titleColor;
    }

    public PreferenceCategory(Context context) {
        super(context);
    }

    private int color = DialogUtils.resolveColor(getContext(), R.attr.colorAccent);
    private TextView textView;

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        textView = (TextView) holder.findViewById(android.R.id.title);
        textView.setTextColor(color);
    }

    public void setColor(int color){
        this.color = color;
        if (textView != null)
            textView.setTextColor(color);
    }
}
