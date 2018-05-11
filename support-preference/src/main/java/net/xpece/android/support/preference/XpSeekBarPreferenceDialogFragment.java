package net.xpece.android.support.preference;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.SeekBar;

import javax.annotation.ParametersAreNonnullByDefault;

import static net.xpece.android.support.preference.Util.checkPreferenceNotNull;

/**
 * @author Eugen on 7. 12. 2015.
 */
@ParametersAreNonnullByDefault
public class XpSeekBarPreferenceDialogFragment extends XpPreferenceDialogFragment
        implements View.OnKeyListener {

    SeekBar mSeekBar;

    @NonNull
    public static XpSeekBarPreferenceDialogFragment newInstance(String key) {
        XpSeekBarPreferenceDialogFragment fragment = new XpSeekBarPreferenceDialogFragment();
        Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    public XpSeekBarPreferenceDialogFragment() {
    }

    @Nullable
    public SeekBarDialogPreference getSeekBarDialogPreference() {
        return (SeekBarDialogPreference) getPreference();
    }

    @NonNull
    protected SeekBarDialogPreference requireSeekBarDialogPreference() {
        return checkPreferenceNotNull(getSeekBarDialogPreference(), SeekBarDialogPreference.class, this);
    }

    @Nullable
    protected static SeekBar findSeekBar(View dialogView) {
        return (SeekBar) dialogView.findViewById(R.id.seekbar);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        // Show the icon next to seek bar.
        builder.setIcon(null);
    }

    @Override
    protected void onBindDialogView(final View view) {
        super.onBindDialogView(view);

        SeekBarDialogPreference preference = requireSeekBarDialogPreference();

        boolean hasTitle = false; //hasDialogTitle();

        final ImageView iconView = view.findViewById(android.R.id.icon);
        final Drawable icon = preference.getDialogIcon();
        if (icon != null && !hasTitle) {
            iconView.setImageDrawable(icon);
            iconView.setVisibility(View.VISIBLE);
        } else {
            iconView.setVisibility(View.GONE);
            iconView.setImageDrawable(null);
        }

        mSeekBar = findSeekBar(view);

        final int max = preference.getMax();
        final int min = preference.getMin();

        mSeekBar.setMax(max - min);
        mSeekBar.setProgress(preference.getProgress() - min);

        mKeyProgressIncrement = mSeekBar.getKeyProgressIncrement();
        mSeekBar.setOnKeyListener(this);

        setupAccessibilityDelegate(max, min);
    }

    private void setupAccessibilityDelegate(final int max, final int min) {
        mSeekBar.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override
            public void onInitializeAccessibilityEvent(final View host, final AccessibilityEvent event) {
                super.onInitializeAccessibilityEvent(host, event);

                final int progress = mSeekBar.getProgress() + min;
                event.setContentDescription(progress + "");

//                    event.setItemCount(max - min);
//                    event.setFromIndex(min);
//                    event.setToIndex(max);
//                    event.setCurrentItemIndex(progress);
            }

            @Override
            public void onInitializeAccessibilityNodeInfo(final View host, final AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);

                int progress = mSeekBar.getProgress() + min;
                info.setContentDescription(progress + "");
            }
        });
    }

    private boolean hasDialogTitle() {
        android.support.v7.preference.DialogPreference preference = requireSeekBarDialogPreference();
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
        SeekBarDialogPreference preference = requireSeekBarDialogPreference();
        if (positiveResult) {
            int progress = mSeekBar.getProgress() + preference.getMin();
            if (preference.callChangeListener(progress)) {
                preference.setProgress(progress);
            }
        }
    }
}
