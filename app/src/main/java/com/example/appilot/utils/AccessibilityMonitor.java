package com.example.appilot.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import java.util.List;
import android.provider.Settings;

public class AccessibilityMonitor {
    private static final String TAG = "AccessibilityMonitor";
    private Context context;
    private Handler handler;
    private Runnable connectionChecker;
    private boolean isMonitoring = false;

    public AccessibilityMonitor(Context context) {
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void startMonitoring(AccessibilityService service) {
        isMonitoring = true;
        Log.d(TAG, "Starting accessibility monitoring");

        connectionChecker = new Runnable() {
            @Override
            public void run() {
                if (!isAccessibilityServiceEnabled() || !isNetworkConnected()) {
                    Log.w(TAG, "Connection lost - stopping automation");
                    handleConnectionLoss(service);
                    return;
                }
                if (isMonitoring) {
                    handler.postDelayed(this, 2000); // Check every 2 seconds
                }
            }
        };
        handler.post(connectionChecker);
    }

    private boolean isAccessibilityServiceEnabled() {
        AccessibilityManager manager = (AccessibilityManager)
                context.getSystemService(Context.ACCESSIBILITY_SERVICE);

        String settingValue = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

        if (settingValue != null) {
            String packageName = context.getPackageName();
            return settingValue.contains(packageName);
        }
        return false;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void handleConnectionLoss(AccessibilityService service) {
        isMonitoring = false;
        // Exit current activity and stop automation
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }

    public void stopMonitoring() {
        isMonitoring = false;
        Log.d(TAG, "Stopping accessibility monitoring");
        if (connectionChecker != null) {
            handler.removeCallbacks(connectionChecker);
        }
    }
}