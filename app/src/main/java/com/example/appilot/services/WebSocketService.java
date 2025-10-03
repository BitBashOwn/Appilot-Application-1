package com.example.appilot.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.appilot.MainActivity;
import com.example.appilot.R;
import com.example.appilot.managers.WebSocketClientManager;

public class WebSocketService extends Service {

    private static final String CHANNEL_ID = "WebSocketServiceChannel";
    private static final String PREFS_NAME = "WebSocketPrefs";
    private static final String PREF_DEVICE_CODE = "deviceCode";
    private WebSocketClientManager webSocketClientManager;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize WebSocketClientManager with service context
        webSocketClientManager = new WebSocketClientManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String deviceCode;

        // Retrieve deviceCode from intent or SharedPreferences
        if (intent != null && intent.getStringExtra("deviceCode") != null) {
            deviceCode = intent.getStringExtra("deviceCode");
            saveDeviceCode(deviceCode);
        } else {
            deviceCode = getSavedDeviceCode();
        }

        // Check if WebSocket is already connected before reconnecting
        if (deviceCode != null && !webSocketClientManager.isConnected()) {
            // Only connect WebSocket if not already connected
            webSocketClientManager.connectWebSocket(deviceCode);
        }

        createNotification();
        return START_STICKY; // Ensure the service is restarted if killed
    }

    private void createNotification() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "WebSocket Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("WebSocket Service")
                .setContentText("WebSocket connection is active")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Close the WebSocket connection when the service is destroyed
        if (webSocketClientManager.isConnected()) {
            webSocketClientManager.closeWebSocket();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Save the device code to SharedPreferences
    private void saveDeviceCode(String deviceCode) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_DEVICE_CODE, deviceCode);
        editor.apply();
    }

    // Retrieve the saved device code from SharedPreferences
    private String getSavedDeviceCode() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(PREF_DEVICE_CODE, null);
    }
}
