package net.xpece.android.support.preference.plugins;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface ErrorInterceptor {
    void onError(@NonNull Throwable t, @Nullable String message);
}
