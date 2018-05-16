package net.xpece.android.support.preference.plugins;

import android.support.annotation.Nullable;

public interface ErrorInterceptor {
    void onError(Throwable t, @Nullable String message);
}
