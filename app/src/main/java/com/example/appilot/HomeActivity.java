package com.example.appilot;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appilot.managers.WebSocketClientManager;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private static final String PREFS_NAME = "DevicePrefs";
    private ImageView connectionStatusLight;
    private ImageView accessibilityStatusLight;
    private TextView versionText;
    private static WebSocketClientManager webSocketClientManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private String deviceCode;
    private SharedPreferences sharedPreferences;
    private Handler statusHandler = new Handler();
    private Runnable statusChecker;
    private Runnable connectionStatusChecker;
    private static final int CONNECTION_STATUS_CHECK_INTERVAL = 20000; // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");
        setContentView(R.layout.activity_home);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        deviceCode = sharedPreferences.getString("deviceCode", null);
        if (deviceCode == null) {
            Log.e(TAG, "No device code found in SharedPreferences");
            redirectToLogin();
            return;
        }

        Log.d(TAG, "Retrieved device code: " + deviceCode);
        initializeViews();
        initializeWebSocket();
        checkForUpdates();

        // Start polling for accessibility status
        startAccessibilityStatusPolling();
        // Start polling for connection status
        startConnectionStatusPolling();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initializeViews() {
        connectionStatusLight = findViewById(R.id.webAppStatusLight);
        accessibilityStatusLight = findViewById(R.id.accessibilityStatusLight);
        versionText = findViewById(R.id.version);
        setVersionText();
        updateConnectionStatusLight(false);
        updateAccessibilityStatusLight(isAccessibilityServiceEnabled());
    }

    private void updateAccessibilityStatusLight(boolean isEnabled) {
        Log.d(TAG, "Updating Accessibility Light: " + isEnabled);
        int resourceId = isEnabled ? R.drawable.ic_light_green : R.drawable.ic_light_red;
        accessibilityStatusLight.setImageResource(resourceId);
    }

    private void startAccessibilityStatusPolling() {
        statusChecker = new Runnable() {
            @Override
            public void run() {
                boolean isEnabled = isAccessibilityServiceEnabled();
                updateAccessibilityStatusLight(isEnabled);
                statusHandler.postDelayed(this, 2000); // Check every 2 seconds
            }
        };
        statusHandler.post(statusChecker);
    }

    private void startConnectionStatusPolling() {
        connectionStatusChecker = new Runnable() {
            @Override
            public void run() {
                if (webSocketClientManager != null) {
                    boolean isConnected = webSocketClientManager.isConnected();
                    Log.d(TAG, "Connection status polling: " + isConnected);
                    updateConnectionStatusLight(isConnected);
//                    updateDeviceStatusOnServer(isConnected);
                }
                statusHandler.postDelayed(this, CONNECTION_STATUS_CHECK_INTERVAL);
            }
        };
        statusHandler.post(connectionStatusChecker);
    }

    private void stopAccessibilityStatusPolling() {
        statusHandler.removeCallbacks(statusChecker);
    }

    private void stopConnectionStatusPolling() {
        statusHandler.removeCallbacks(connectionStatusChecker);
    }

    private boolean isAccessibilityServiceEnabled() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo serviceInfo : enabledServices) {
            if (serviceInfo.getId().contains(getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private void checkForUpdates() {
        String currentAppVersion;
        try {
            currentAppVersion = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        GitHubUpdateChecker updateChecker = new GitHubUpdateChecker(this, currentAppVersion);
        updateChecker.checkForUpdates();
    }

    private void initializeWebSocket() {
        webSocketClientManager = new WebSocketClientManager(this);
        setupWebSocketListener();

        mainHandler.post(() -> {
            Log.d(TAG, "Connecting WebSocket with device code: " + deviceCode);
            webSocketClientManager.connectWebSocket(deviceCode);
        });
        Log.d(TAG, "WebSocket initialization completed");
    }

    private void setupWebSocketListener() {
        Log.d(TAG, "Setting up WebSocket listener");
        webSocketClientManager.setOnConnectionStatusChangedListener(isConnected -> {
            Log.d(TAG, "Connection status changed callback received: " + isConnected);
            mainHandler.post(() -> {
                updateConnectionStatusLight(isConnected);
                updateDeviceStatusOnServer(isConnected);
            });
        });
    }

    private void updateConnectionStatusLight(boolean isConnected) {
        Log.d(TAG, "updateConnectionStatusLight called with status: " + isConnected);
        int resourceId = isConnected ? R.drawable.ic_light_green : R.drawable.ic_light_red;
        connectionStatusLight.setImageResource(resourceId);
        Log.d(TAG, "Connection light updated to: " + (isConnected ? "GREEN" : "RED"));
    }

    private void setVersionText() {
        String currentAppVersion;
        try {
            currentAppVersion = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            versionText.setText(currentAppVersion);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateDeviceStatusOnServer(final boolean status) {
        Log.d(TAG, "Updating device status on server: " + status);
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            String url = "https://server.appilot.app/update_status/" + deviceCode + "?status=" + status;
//            String url = "http://192.168.0.188:8000/update_status/" + deviceCode + "?status=" + status;
            Request request = new Request.Builder()
                    .url(url)
                    .put(RequestBody.create(null, new byte[0]))
                    .addHeader("accept", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                Log.d(TAG, "Server response code: " + response.code());
                if (response.isSuccessful()) {
                    Log.d(TAG, "Device status updated successfully");
                } else {
                    Log.e(TAG, "Failed to update device status: " + response.message());
                    if (response.code() == 404 || response.code() == 401) {
                        mainHandler.post(this::clearLoginStateAndRedirect);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating device status", e);
            }
        }).start();
    }

    private void clearLoginStateAndRedirect() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.remove("deviceCode");
        editor.apply();
        redirectToLogin();
    }

    public void sendMessage(String message, String taskId, String jobId, String type) {
        if (webSocketClientManager == null) {
            Log.e(TAG, "websocket is null");
        } else {
            webSocketClientManager.sendMessage(message, taskId, jobId, type);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");

        // Check connection status
        if (webSocketClientManager != null) {
            updateConnectionStatusLight(webSocketClientManager.isConnected());
        }

        // Start polling for accessibility status
        startAccessibilityStatusPolling();
        // Start polling for connection status
        startConnectionStatusPolling();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop polling for accessibility status
        stopAccessibilityStatusPolling();
        // Stop polling for connection status
        stopConnectionStatusPolling();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");
        if (webSocketClientManager != null) {
            webSocketClientManager.closeWebSocket();
        }
        stopAccessibilityStatusPolling();
        stopConnectionStatusPolling();
        super.onDestroy();
    }
}
