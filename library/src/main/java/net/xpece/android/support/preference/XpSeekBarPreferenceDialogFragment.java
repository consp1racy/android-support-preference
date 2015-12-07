package net.xpece.android.support.preference;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

/**
 * @author Eugen on 7. 12. 2015.
 */
public class XpSeekBarPreferenceDialogFragment extends PreferenceDialogFragmentCompat {

    private SeekBar mSeekBar;

    public static XpSeekBarPreferenceDialogFragment newInstance(String key) {
        XpSeekBarPreferenceDialogFragment fragment = new XpSeekBarPreferenceDialogFragment();
        Bundle b = new Bundle(1);
        b.putString("key", key);
        fragment.setArguments(b);
        return fragment;
    }

    public XpSeekBarPreferenceDialogFragment() {
    }

    public SeekBarDialogPreference getSeekBarDialogPreference() {
        return (SeekBarDialogPreference) getPreference();
    }

    protected static SeekBar getSeekBar(View dialogView) {
        return (SeekBar) dialogView.findViewById(R.id.seekbar);
    }

    @Override
    protected void onBindDialogView(final View view) {
        super.onBindDialogView(view);

        SeekBarDialogPreference preference = getSeekBarDialogPreference();

        final ImageView iconView = (ImageView) view.findViewById(android.R.id.icon);
        final Drawable icon = preference.getDialogIcon();
        if (icon != null) {
            iconView.setImageDrawable(icon);
            iconView.setVisibility(View.VISIBLE);
        } else {
            iconView.setVisibility(View.GONE);
            iconView.setImageDrawable(null);
        }

        mSeekBar = getSeekBar(view);

        mSeekBar.setMax(preference.getMax());
        mSeekBar.setProgress(preference.getProgress());
    }

    @Override
    public void onDialogClosed(final boolean positiveResult) {
        SeekBarDialogPreference preference = getSeekBarDialogPreference();
        if (positiveResult) {
            int progress = mSeekBar.getProgress();
            if (preference.callChangeListener(progress)) {
                preference.setProgress(progress);
            }
        }
    }
}
