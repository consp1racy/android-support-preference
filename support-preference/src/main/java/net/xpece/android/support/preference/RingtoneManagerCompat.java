package net.xpece.android.support.preference;

import android.app.Activity;
import android.content.Context;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.annotation.RestrictTo;

/**
 * Created by Eugen on 14.12.2015.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class RingtoneManagerCompat {

    public static RingtoneManager from(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            return new RingtoneManager(context);
        } else {
            return new RingtoneManagerLegacy(context);
        }
    }

    public static RingtoneManager from(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            return new RingtoneManager(activity);
        } else {
            return new RingtoneManagerLegacy(activity);
        }
    }
}
