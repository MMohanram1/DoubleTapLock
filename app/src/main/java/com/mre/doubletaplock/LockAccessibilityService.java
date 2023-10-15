package com.mre.doubletaplock;

import android.accessibilityservice.AccessibilityService;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class LockAccessibilityService extends AccessibilityService {

    private static final String LOG_CLASS = AccessibilityService.class.getName();

    private DevicePolicyManager devicePolicyManager;
    private ComponentName componentName;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        int eventType = accessibilityEvent.getEventType();

        Log.i(LOG_CLASS, "Inside accessibility event: " + accessibilityEvent.toString());

        /*if (eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            handleTap();
        }*/
    }

    private void handleTap() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            Log.w(LOG_CLASS, "Locking screen using device admin privilege");
            devicePolicyManager.lockNow();
        } else {
            Log.w(LOG_CLASS, "Device Admin privilege is not enabled for the application");
            Toast.makeText(this, "Device admin privileges not granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this, LockDeviceAdminReceiver.class);

        if (!devicePolicyManager.isAdminActive(componentName)) {
            Log.w(LOG_CLASS, "Device Admin privilege is not enabled for the application");
        } else {
            Log.d(LOG_CLASS, "Device Admin privilege is enabled for the application");
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

}
