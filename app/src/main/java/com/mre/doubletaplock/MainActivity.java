package com.mre.doubletaplock;

import android.app.AlertDialog;
import android.app.StatusBarManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.util.Log;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private DevicePolicyManager devicePolicyManager;
    private StatusBarManager statusBarManager;
    private ComponentName deviceAdminComponent;
    private ComponentName tileServiceComponent;

    private static final int DEVICE_REQUEST_CODE = 1;
    private static final int ACCESSIBILITY_SETTINGS_REQUEST = 2;

    private static final String LOG_CLASS = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Initialize DevicePolicyManager and ComponentName
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        deviceAdminComponent = new ComponentName(this, LockDeviceAdminReceiver.class);

        statusBarManager = getSystemService(StatusBarManager.class);
        tileServiceComponent = new ComponentName(this, LockTileService.class);

        if (statusBarManager == null) {
            return;
        }

        checkAndEnableDeviceAdmin();

        Intent serviceIntent = new Intent(this, LockAccessibilityService.class);
        startService(serviceIntent);

    }

    private void checkAndEnableDeviceAdmin() {
        // Check if the app is a device admin
        if (!devicePolicyManager.isAdminActive(deviceAdminComponent)) {
            showSetDeviceAdminDialog();
        } else {

            // Check if accessibility service is enabled
            if (!isAccessibilityServiceEnabled()) {
                // Prompt the user to enable the accessibility service
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivityForResult(intent, ACCESSIBILITY_SETTINGS_REQUEST);
            } else {
                checkIfTileAdded();
            }
        }
    }

    private void checkIfTileAdded() {
        if (!LockTileService.isTileAdded) {
            Log.d(LOG_CLASS, "Tile is not added");
            showQuickTileAdditionDialog();
        } else {
            Log.d(LOG_CLASS, "Tile is added");
        }
    }

    private void showQuickTileAdditionDialog () {
        Executor resultSuccessExecutor = new Executor() {
            @Override
            public void execute(Runnable runnable) {
                Log.d(LOG_CLASS, "Screen Lock tile added to Quick Settings successfully");
            }
        };

        statusBarManager.requestAddTileService(
                tileServiceComponent,
                getString(R.string.app_name),
                Icon.createWithResource(MainActivity.this, R.mipmap.ic_launcher_foreground),
                resultSuccessExecutor,
                (resultCodeFailure) -> {
                    Log.d(LOG_CLASS, "requestAddTileService failure: resultCodeFailure: " + resultCodeFailure);
                }
        );
    }

    private void showSetDeviceAdminDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.set_as_device_admin);
        builder.setMessage(R.string.set_as_device_admin_desc);
        builder.setPositiveButton(R.string.set_as_device_admin, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Launch the device admin settings screen
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponent);
                startActivityForResult(intent, DEVICE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle cancel
                finish();
            }
        });
        builder.create().show();
    }

    private boolean isAccessibilityServiceEnabled() {
        int accessibilityEnabled = 0;
        final String serviceId = getPackageName() + "/" + LockAccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    this.getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    this.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

            return settingValue != null && settingValue.contains(serviceId);
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DEVICE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Perform actions based on the result from the second activity
                Log.d(LOG_CLASS, "Device admin request success");

                // Check if accessibility service is enabled
                if (!isAccessibilityServiceEnabled()) {
                    // Prompt the user to enable the accessibility service
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivityForResult(intent, ACCESSIBILITY_SETTINGS_REQUEST);
                } else {
                    checkIfTileAdded();
                }
            } else if (resultCode == RESULT_CANCELED) {
                // Handle if the user canceled the action in the second activity
                Log.d(LOG_CLASS, "Device admin request cancelled");
                checkAndEnableDeviceAdmin();
            }
        }

        if (requestCode == ACCESSIBILITY_SETTINGS_REQUEST) {
            // Check again if accessibility service is enabled after returning from settings
            if (isAccessibilityServiceEnabled()) {
                // The user has enabled the accessibility service
                checkIfTileAdded();
            } else {
                // The user did not enable the accessibility service
                finish();
            }
        }
    }

}