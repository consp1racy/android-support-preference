package net.xpece.android.support.preference;

import java.io.Serializable;

/**
 * Created by Eugen on 16.04.2016.
 */
class Tuple<T> implements Serializable {
    public final T first, second;

    public Tuple(final T first, final T second) {
        this.first = first;
        this.second = second;
    }
}
