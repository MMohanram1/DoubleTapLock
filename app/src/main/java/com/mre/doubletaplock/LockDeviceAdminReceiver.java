package com.mre.doubletaplock;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

public class LockDeviceAdminReceiver extends DeviceAdminReceiver {

    private static final String LOG_CLASS = LockDeviceAdminReceiver.class.getName();

    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        super.onEnabled(context, intent);
        Log.d(LOG_CLASS, "Device admin enabled");
    }

    @Override
    public void onDisabled(@NonNull Context context, @NonNull Intent intent) {
        super.onDisabled(context, intent);
        Log.d(LOG_CLASS, "Device admin disabled");
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        Log.d(LOG_CLASS, "Received device admin event");
    }
}
