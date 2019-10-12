package net.xpece.android.support.preference;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

class LongClickBinder {

    static void bindLongClickListener(@NonNull final Preference preference, @NonNull final PreferenceViewHolder holder, @Nullable final OnPreferenceLongClickListener listener) {
        final boolean copyingEnabled = preference.isCopyingEnabled();
        final boolean hasLongClickListener = listener != null;
        if (hasLongClickListener) {
            if (copyingEnabled) {
                final String className = preference.getClass().getSimpleName();
                final String key = preference.getKey();
                final String debugTag = className + "(key=" + key + ") " + preference;
                Log.w("Preference", "You can't have both setCopyingEnabled(true) and an OnPreferenceLongClickListener. Will override copying. Please fix your preference.\n" + debugTag);
                holder.itemView.setOnCreateContextMenuListener(null);
            }
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(@NonNull View v) {
                    return listener.onLongClick(preference, v);
                }
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
        }
        if (!copyingEnabled) {
            // If copying is enabled we've already enabled long clicks.
            holder.itemView.setLongClickable(hasLongClickListener && preference.isSelectable());
        }
    }


    private LongClickBinder() {
    }
}
