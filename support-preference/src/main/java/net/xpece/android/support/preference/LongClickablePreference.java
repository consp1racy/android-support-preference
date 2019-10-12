package net.xpece.android.support.preference;

import androidx.annotation.Nullable;

/**
 * Created by Eugen on 08.03.2016.
 */
public interface LongClickablePreference {
    void setOnPreferenceLongClickListener(@Nullable OnPreferenceLongClickListener listener);

    boolean hasOnPreferenceLongClickListener();
}
