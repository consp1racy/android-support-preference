package android.support.v7.preference;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.xpece.android.support.preference.ColorableTextPreference;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eugen on 17. 3. 2016.
 */
class XpPreferenceGroupAdapter extends PreferenceGroupAdapter {

    private static final int OFFSET = 0xffff;

    private final List<PreferenceLayout> mPreferenceLayouts = new ArrayList<>();

    private PreferenceLayout mTempPreferenceLayout = new PreferenceLayout();

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
        return offset + getItemViewTypeOriginal(position);
    }

    public int getItemViewTypeOriginal(int position) {
        Preference preference = this.getItem(position);
        this.mTempPreferenceLayout = this.createPreferenceLayout(preference, this.mTempPreferenceLayout);
        int viewType = this.mPreferenceLayouts.indexOf(this.mTempPreferenceLayout);
        if (viewType != -1) {
            return viewType;
        } else {
            viewType = this.mPreferenceLayouts.size();
            this.mPreferenceLayouts.add(new PreferenceLayout(this.mTempPreferenceLayout));
            return viewType;
        }
    }

    @Override
    public void onViewDetachedFromWindow(PreferenceViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        holder.itemView.setOnKeyListener(null);
    }

    @Override
    public PreferenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        viewType %= OFFSET;

        PreferenceLayout pl = this.mPreferenceLayouts.get(viewType);
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(pl.resId, parent, false);
        ViewGroup widgetFrame = (ViewGroup) view.findViewById(android.R.id.widget_frame);
        if (widgetFrame != null) {
            if (pl.widgetResId != 0) {
                inflater.inflate(pl.widgetResId, widgetFrame);
            } else {
                widgetFrame.setVisibility(View.GONE);
            }
        }

        return new PreferenceViewHolder(view);
    }

    private PreferenceLayout createPreferenceLayout(Preference preference, PreferenceLayout in) {
        PreferenceLayout pl = in != null ? in : new PreferenceLayout();
        pl.name = preference.getClass().getName();
        pl.resId = preference.getLayoutResource();
        pl.widgetResId = preference.getWidgetLayoutResource();
        return pl;
    }

    private static class PreferenceLayout {
        int resId;
        int widgetResId;
        String name;

        public PreferenceLayout() {
        }

        public PreferenceLayout(PreferenceLayout other) {
            this.resId = other.resId;
            this.widgetResId = other.widgetResId;
            this.name = other.name;
        }

        public boolean equals(Object o) {
            if (!(o instanceof PreferenceLayout)) {
                return false;
            } else {
                PreferenceLayout other = (PreferenceLayout) o;
                return this.resId == other.resId && this.widgetResId == other.widgetResId && TextUtils.equals(this.name, other.name);
            }
        }

        public int hashCode() {
            byte result = 17;
            int result1 = 31 * result + this.resId;
            result1 = 31 * result1 + this.widgetResId;
            result1 = 31 * result1 + this.name.hashCode();
            return result1;
        }
    }
}
