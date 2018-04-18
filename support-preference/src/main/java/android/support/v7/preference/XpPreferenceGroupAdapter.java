package android.support.v7.preference;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import net.xpece.android.support.preference.ColorableTextPreference;

/**
 * @author Eugen on 17. 3. 2016.
 */
@SuppressWarnings("RestrictedApi")
class XpPreferenceGroupAdapter extends PreferenceGroupAdapter {

    private static final int OFFSET = 0xffff;

    public XpPreferenceGroupAdapter(final PreferenceGroup preferenceGroup) {
        super(preferenceGroup);
    }

    @Override
    public int getItemViewType(final int position) {
        int offset = 0;
        Preference preference = this.getItem(position);
        if (preference instanceof ColorableTextPreference) {
            ColorableTextPreference p = (ColorableTextPreference) preference;
            if (p.hasTitleTextAppearance()) {
                offset += OFFSET;
            }
            if (p.hasTitleTextColor()) {
                offset += OFFSET;
            }
            if (p.hasSummaryTextAppearance()) {
                offset += OFFSET;
            }
            if (p.hasSummaryTextColor()) {
                offset += OFFSET;
            }
        }
        return offset + superGetItemViewType(position);
    }

    private int superGetItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull PreferenceViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        // Clear key listener from SeekBarPreference.
        holder.itemView.setOnKeyListener(null);
    }

    @NonNull
    @Override
    public PreferenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return super.onCreateViewHolder(parent, viewType % OFFSET);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        final Preference preference = getItem(position);
        XpPreferenceHelpers.onBindViewHolder(preference, holder);
    }
}
