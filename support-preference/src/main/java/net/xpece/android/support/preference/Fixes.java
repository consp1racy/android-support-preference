package net.xpece.android.support.preference;

import android.view.LayoutInflater;

/**
 * @author Eugen on 7. 12. 2015.
 */
public final class Fixes {
    private Fixes() {
        throw new AssertionError("No instances!");
    }

    /**
     * In appcompat-v7 r23.1.1 and r24.1.x there is a bug
     * which prevents tinting of checkmarks in lists.
     * <p>
     * This library now requires at least support library 27.0.0 so
     * this fix is no longer necessary and does nothing. This method will be removed.
     *
     * @param layoutInflater Layout inflater that should automatically inflate fixed widgets
     */
    @Deprecated
    public static void updateLayoutInflaterFactory(LayoutInflater layoutInflater) {
        // No-op.
    }
}
