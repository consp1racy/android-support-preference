package net.xpece.android.support.preference;

/**
 * Created by Eugen on 08.03.2016.
 */
public interface LongClickablePreference {
    void setOnPreferenceLongClickListener(OnPreferenceLongClickListener listener);

    boolean hasOnPreferenceLongClickListener();
}
