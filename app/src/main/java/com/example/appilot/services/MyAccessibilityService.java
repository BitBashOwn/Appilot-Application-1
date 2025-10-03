package com.example.appilot.services;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import com.example.appilot.HomeActivity;
import com.example.appilot.MainActivity;
import com.example.appilot.R;

import com.example.appilot.Handlers.CommandHandler;
import com.example.appilot.utils.AccessibilityMonitor;

import org.json.JSONException;
import org.json.JSONObject;


public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "MyAccessibilityService";
    private static final int FOREGROUND_SERVICE_ID = 1001;
    private CommandHandler commandHandler;
    private AccessibilityMonitor monitor;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Accessibility Service Created");
    }

    @Override
    public void onAccessibilityEvent(android.view.accessibility.AccessibilityEvent event) {
        // Handle accessibility events if needed
    }

    @Override
    public void onInterrupt() {
        // Handle interrupt scenarios if needed
        if (monitor != null) {
            monitor.stopMonitoring();
            monitor = null;
        }
        Log.d(TAG, "Accessibility Service Interrupted");
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Accessibility Service Connected");

        commandHandler = new CommandHandler(this);

        monitor= new AccessibilityMonitor(this);
        monitor.startMonitoring(this);

        // Create the notification channel to keep the service running in the foreground
        createNotificationChannel();

        // Start the service in the foreground with the notification
        startForegroundService();
    }

    // Create a notification channel for the foreground service
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                "ForegroundServiceChannel",
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    // Start the service with a foreground notification
    private void startForegroundService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, "ForegroundServiceChannel")
                .setContentTitle("Appilot Running")
                .setContentText("Accessibility Service Active")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(FOREGROUND_SERVICE_ID, notification);
    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        // Handle commands here from WebSocket or other sources
//        if (intent != null && "START_AUTOMATION".equals(intent.getAction())) {
//            String command = intent.getStringExtra("command");
//
//            if (command != null) {
//                // Trigger the appropriate automation based on the command
//                commandHandler.executeAutomation(command);
//            }
//        }
//        return START_STICKY; // Make service restart if killed
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (intent != null && "START_AUTOMATION".equals(intent.getAction())) {
                String command = intent.getStringExtra("command");

                if (command != null) {
                    try {
                        JSONObject commandJson = new JSONObject(command);
                        String Task_Id = commandJson.optString("task_id", null);
                        String job_id = commandJson.optString("job_id", null);

                        if (commandHandler != null) {
                            commandHandler.executeAutomation(command);
                        } else {
                            Log.e("MyAccessibilityService", "commandHandler is null, attempting to send message via HomeActivity.");

                            // Create an instance of HomeActivity and send the message
                            HomeActivity home = new HomeActivity();
                            home.sendMessage("Automation could not start, Please Enable Accessibility on device.", Task_Id, job_id, "error");
                        }
                    } catch (JSONException e) {
                        Log.e("MyAccessibilityService", "Failed to parse command JSON", e);
                    }
                } else {
                    Log.e("MyAccessibilityService", "Command is null, ignoring.");
                }
            }
        } catch (Exception e) {
            Log.e("MyAccessibilityService", "Unhandled error in onStartCommand", e);
        }

        return START_STICKY; // Prevent service from getting killed automatically
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Accessibility Service Destroyed");
        if (monitor != null) {
            monitor.stopMonitoring();
            monitor = null;
        }

        super.onDestroy();
    }
}