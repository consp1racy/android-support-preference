package net.xpece.android.support.preference.sample;

import android.app.Application;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import net.xpece.android.support.preference.plugins.ErrorInterceptor;
import net.xpece.android.support.preference.plugins.XpSupportPreferencePlugins;

/**
 * Created by Eugen on 15.02.2017.
 */

public class SampleApplication extends Application {
    private static final String ASP_TAG = "xpece-support-prefs";

    @Override
    public void onCreate() {
        super.onCreate();

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());

        XpSupportPreferencePlugins.registerErrorInterceptor(new ErrorInterceptor() {
            @Override
            public void onError(@NonNull Throwable t, @Nullable String message) {
                if (message != null) {
                    Log.w(ASP_TAG, message, t);
                } else {
                    Log.w(ASP_TAG, t);
                }
            }
        });
    }
}
