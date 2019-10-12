package net.xpece.android.support.preference.plugins;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class XpSupportPreferencePlugins {
    private static final Set<ErrorInterceptor> sErrorInterceptors;

    static {
        sErrorInterceptors = Collections.newSetFromMap(new ConcurrentHashMap<ErrorInterceptor, Boolean>(1));
    }

    public static void registerErrorInterceptor(final @NonNull ErrorInterceptor interceptor) {
        sErrorInterceptors.add(requireNotNull(interceptor));
    }

    public static void unregisterErrorInterceptor(final @NonNull ErrorInterceptor interceptor) {
        sErrorInterceptors.remove(requireNotNull(interceptor));
    }

    public static void reset() {
        sErrorInterceptors.clear();
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static void onError(final @NonNull Throwable t, @Nullable final String message) {
        for (ErrorInterceptor interceptor : sErrorInterceptors) {
            interceptor.onError(t, message);
        }
    }

    @NonNull
    private static <T> T requireNotNull(@Nullable final T o) {
        if (o == null) throw new IllegalStateException("Expected non-null argument.");
        else return o;
    }
}
