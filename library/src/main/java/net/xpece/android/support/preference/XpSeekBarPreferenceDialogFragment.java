package net.xpece.android.support.preference;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

/**
 * @author Eugen on 7. 12. 2015.
 */
public class XpSeekBarPreferenceDialogFragment extends XpPreferenceDialogFragment
    implements View.OnKeyListener {

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
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        if (!hasDialogTitle()) {
            // If we don't have dialog title, show the icon next to seek bar.
            builder.setIcon(null);
        }
    }

    @Override
    protected void onBindDialogView(final View view) {
        super.onBindDialogView(view);

        SeekBarDialogPreference preference = getSeekBarDialogPreference();

        boolean hasTitle = hasDialogTitle();

        final ImageView iconView = (ImageView) view.findViewById(android.R.id.icon);
        final Drawable icon = preference.getDialogIcon();
        if (icon != null && !hasTitle) {
            iconView.setImageDrawable(icon);
            iconView.setVisibility(View.VISIBLE);
        } else {
            iconView.setVisibility(View.GONE);
            iconView.setImageDrawable(null);
        }

        mSeekBar = getSeekBar(view);

        mSeekBar.setMax(preference.getMax() - preference.getMin());
        mSeekBar.setProgress(preference.getProgress() - preference.getMin());

        mKeyProgressIncrement = mSeekBar.getKeyProgressIncrement();
        mSeekBar.setOnKeyListener(this);
    }

    private boolean hasDialogTitle() {
        android.support.v7.preference.DialogPreference preference = getPreference();
        CharSequence dialogTitle = preference.getDialogTitle();
        if (dialogTitle == null) dialogTitle = preference.getTitle();
        return !TextUtils.isEmpty(dialogTitle);
    }

    private int mKeyProgressIncrement;

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_UP) {
            final int step = mKeyProgressIncrement;
            if (keyCode == KeyEvent.KEYCODE_PLUS || keyCode == KeyEvent.KEYCODE_EQUALS) {
                mSeekBar.setProgress(mSeekBar.getProgress() + step);
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_MINUS) {
                mSeekBar.setProgress(mSeekBar.getProgress() - step);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        mSeekBar.setOnKeyListener(null);
        super.onDestroyView();
    }

    @Override
    public void onDialogClosed(final boolean positiveResult) {
        SeekBarDialogPreference preference = getSeekBarDialogPreference();
        if (positiveResult) {
            int progress = mSeekBar.getProgress() + preference.getMin();
            if (preference.callChangeListener(progress)) {
                preference.setProgress(progress);
            }
        }
    }
}
