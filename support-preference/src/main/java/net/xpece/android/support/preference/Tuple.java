package net.xpece.android.support.preference;

import android.support.annotation.RestrictTo;

import java.io.Serializable;

/**
 * Created by Eugen on 16.04.2016.
 */
@RestrictTo(RestrictTo.Scope.GROUP_ID)
class Tuple<T> implements Serializable {
    public final T first, second;

    public Tuple(final T first, final T second) {
        this.first = first;
        this.second = second;
    }
}
