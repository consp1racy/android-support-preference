package android.support.v7.preference;

import android.content.Context;
import android.support.annotation.RestrictTo;
import android.util.AttributeSet;

/**
 * Created by Eugen on 27.11.2016.
 */
@RestrictTo(RestrictTo.Scope.GROUP_ID)
class XpPreferenceInflater extends PreferenceInflater {

    public XpPreferenceInflater(final Context context, final PreferenceManager preferenceManager) {
        super(context, preferenceManager);
    }

    @Override
    protected Preference onCreateItem(final String name, final AttributeSet attrs) throws ClassNotFoundException {
        final Preference preference = super.onCreateItem(name, attrs);
        XpPreferenceHelpers.onCreatePreference(preference, attrs);
        return preference;
    }
}
