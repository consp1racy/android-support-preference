package net.xpece.android.support.preference.plugins;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class XpSupportPreferencePlugins {
    private static final Set<ErrorInterceptor> sErrorInterceptors;

    static {
        sErrorInterceptors = Collections.newSetFromMap(new ConcurrentHashMap<ErrorInterceptor, Boolean>(1));
    }

    public static void registerErrorInterceptor(@NonNull ErrorInterceptor interceptor) {
        requireNotNull(interceptor);
        sErrorInterceptors.add(interceptor);
    }

    public static void unregisterErrorInterceptor(@NonNull ErrorInterceptor interceptor) {
        requireNotNull(interceptor);
        sErrorInterceptors.remove(interceptor);
    }

    public static void reset() {
        sErrorInterceptors.clear();
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static void onError(@NonNull Throwable t, @Nullable String message) {
        for (ErrorInterceptor interceptor : sErrorInterceptors) {
            interceptor.onError(t, message);
        }
    }

    private static void requireNotNull(@Nullable Object o) {
        if (o == null) throw new IllegalStateException("Expected non-null argument.");
    }
}
