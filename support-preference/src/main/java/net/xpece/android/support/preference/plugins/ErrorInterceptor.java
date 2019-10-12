package net.xpece.android.support.preference.plugins;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ErrorInterceptor {
    void onError(@NonNull Throwable t, @Nullable String message);
}
