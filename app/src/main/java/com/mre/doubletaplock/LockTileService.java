package com.mre.doubletaplock;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.widget.Toast;

public class LockTileService extends TileService {

    private static final String LOG_CLASS = LockTileService.class.getName();

    private DevicePolicyManager devicePolicyManager;
    private ComponentName componentName;

    public static boolean isTileAdded = Boolean.FALSE;

    @Override
    public void onTileAdded() {
        isTileAdded = Boolean.TRUE;
    }

    @Override
    public void onTileRemoved() {
        isTileAdded = Boolean.FALSE;
    }

    @Override
    public void onClick() {
        // This method is called when the user clicks the tile
        // Implement the behavior you want when the tile is clicked
        // For example, toggle a setting or launch an activity
        Log.d(LOG_CLASS, "Quick action tile to lock clicked");
        if (devicePolicyManager.isAdminActive(componentName)) {
            Log.w(LOG_CLASS, "Locking screen using device admin privilege");
            devicePolicyManager.lockNow();
        } else {
            Log.w(LOG_CLASS, "Device Admin privilege is not enabled for the application");
            Toast.makeText(this, "Device admin privileges not granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStartListening() {
        // This method is called when the tile is added to the quick settings panel
        // Update the tile's state and appearance here

        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this, LockDeviceAdminReceiver.class);

        if (!devicePolicyManager.isAdminActive(componentName)) {
            Log.w(LOG_CLASS, "Device Admin privilege is not enabled for the application");
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "You need to enable device admin privileges to use this feature.");
            startActivity(intent);
        } else {
            Log.d(LOG_CLASS, "Device Admin privilege is enabled for the application");
        }

        Tile tile = getQsTile();
        if (tile != null) {
            isTileAdded = Boolean.TRUE;
            tile.setState(Tile.STATE_INACTIVE);  // Set the initial state
            tile.updateTile();
        }
    }
}
