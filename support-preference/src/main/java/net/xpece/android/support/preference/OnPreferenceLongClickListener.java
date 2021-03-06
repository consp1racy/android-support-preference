package net.xpece.android.support.preference;

import androidx.annotation.NonNull;
import android.view.View;

/**
 * Interface definition for a callback to be invoked when a {@link androidx.preference.Preference} is
 * clicked and held.
 */
public interface OnPreferenceLongClickListener {
    /**
     * Called when a view has been clicked and held.
     *
     * @param preference The preference that was clicked and held.
     * @param view       The view that was clicked and held.
     * @return true if the callback consumed the long click, false otherwise.
     */
    boolean onLongClick(@NonNull androidx.preference.Preference preference, @NonNull View view);
}
