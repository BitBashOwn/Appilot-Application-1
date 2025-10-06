package com.example.appilot.managers;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.appilot.services.MyAccessibilityService;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.Response;
import org.json.JSONObject;

public class WebSocketClientManager {
    private static final String TAG = "WebSocketClientManager";
    private WebSocket webSocket;
//    private static WebSocketClientManager instance;
    private final OkHttpClient client;
    private boolean isConnected = false;
    private ConnectionStatusListener connectionStatusListener;
    private final Context context;
    private final Handler heartbeatHandler = new Handler(Looper.getMainLooper());
//    private final int HEARTBEAT_INTERVAL = 35000;
//    private final int PING_TIMEOUT = 30000;
    private final int HEARTBEAT_INTERVAL = 30000;

    private String currentDeviceCode;
    private final AtomicBoolean awaitingPong = new AtomicBoolean(false);
    private Runnable heartbeatRunnable;

    // Add reconnection tracking variables
    private boolean isReconnecting = false;
    private int reconnectAttempts = 0;
    private final int[] RECONNECT_BACKOFF_MS = {5000, 10000, 15000, 30000, 60000}; // Progressive backoff

    // Track if we've manually closed the connection
    private boolean manualDisconnect = false;

    public WebSocketClientManager(Context context) {
        this.context = context.getApplicationContext();

        // Modify OkHttpClient with more conservative timeouts
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS) // Reduced from 30s
                .readTimeout(60, TimeUnit.SECONDS)    // Reduced from 30s
                .writeTimeout(60, TimeUnit.SECONDS)   // Red+++++++++++++++++++++ uced from 30s
                .retryOnConnectionFailure(true)
                .build();
        Log.d(TAG, "WebSocketClientManager initialized");
        initializeHeartbeat();
    }

    private void initializeHeartbeat() {
        heartbeatRunnable = new Runnable() {
            @Override
            public void run() {
                if (isConnected) {
                    if (awaitingPong.get()) {
                        // If we're still waiting for a pong from the previous ping, the connection is likely dead
                        Log.d(TAG, "No pong received within timeout, connection is likely dead");
                        handleConnectionFailure();
                    } else {
                        // Send a ping
                        sendPing();
                    }
                }
                // Schedule the next heartbeat
                heartbeatHandler.postDelayed(this, HEARTBEAT_INTERVAL);
            }
        };
    }

    private void sendPing() {
        if (webSocket != null && isConnected) {
            try {
                JSONObject pingPayload = new JSONObject();
                pingPayload.put("type", "ping");
                pingPayload.put("timestamp", System.currentTimeMillis());

                String jsonString = pingPayload.toString();
                Log.d(TAG, "Sending ping: " + jsonString);

                boolean sent = webSocket.send(jsonString);
                if (sent) {
                    awaitingPong.set(true);

                    // Set a timeout to check if we receive a pong
                    int PING_TIMEOUT = 10000;
                    heartbeatHandler.postDelayed(() -> {
                        if (awaitingPong.get()) {
                            Log.d(TAG, "Ping timeout reached, connection is dead");
                            handleConnectionFailure();
                        }
                    }, PING_TIMEOUT);
                } else {
                    Log.e(TAG, "Failed to send ping");
                    handleConnectionFailure();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sending ping", e);
                handleConnectionFailure();
            }
        }
    }

    private void handleConnectionFailure() {
        if (isConnected) {
            Log.d(TAG, "Handling connection failure");
            isConnected = false;
            awaitingPong.set(false);
            isReconnecting = false; // Reset reconnecting flag to allow new reconnection attempts

            // Release resources
            if (webSocket != null) {
                try {
                    webSocket.cancel(); // Force close the socket
                } catch (Exception e) {
                    Log.e(TAG, "Error cancelling webSocket", e);
                } finally {
                    webSocket = null;
                }
            }

            notifyConnectionStatusChanged(false);

            // Try to reconnect only if not manually disconnected
            if (!manualDisconnect && currentDeviceCode != null) {
                scheduleReconnect(currentDeviceCode);
            }
        }
    }

//    public static synchronized WebSocketClientManager getInstance(Context context) {
//        if (instance == null) {
//            instance = new WebSocketClientManager(context);
//        }
//        return instance;
//    }

    public void connectWebSocket(String deviceCode) {
        this.currentDeviceCode = deviceCode;
        manualDisconnect = false; // Reset manual disconnect flag

        if (isConnected) {
            Log.d(TAG, "WebSocket is already connected, skipping connection attempt.");
            return;
        }

        isReconnecting = true;

        Log.d(TAG, "Attempting to connect WebSocket with device code: " + deviceCode);

        // Try alternate URL formats if the primary one keeps failing
        //String wsUrl = "ws://server.appilot.app/ws/" + deviceCode;
        String wsUrl = "ws://192.168.1.121:8000/ws/" + deviceCode;
        Log.d(TAG, "Using WebSocket URL: " + wsUrl);

        Request request = new Request.Builder().url(wsUrl).build();

        // Ensure old socket is closed properly
        if (webSocket != null) {
            Log.d(TAG, "Closing existing WebSocket connection before creating a new one");
            try {
                webSocket.close(1000, "Creating new connection");
            } catch (Exception e) {
                Log.e(TAG, "Error closing existing WebSocket", e);
            } finally {
                webSocket = null;
            }
        }

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                Log.d(TAG, "WebSocket Opened successfully");
                isConnected = true;
                isReconnecting = false;
                reconnectAttempts = 0; // Reset reconnect counter on successful connection
                awaitingPong.set(false);
                notifyConnectionStatusChanged(true);

                // Start the heartbeat after successful connection
                stopHeartbeat();
                startHeartbeat();
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                Log.w(TAG, "WebSocket received message: " + text);

                try {
                    JSONObject message = new JSONObject(text);
                    String type = message.optString("type", "");

                    if ("pong".equals(type)) {
                        Log.d(TAG, "Received pong response");
                        awaitingPong.set(false);
                        return;
                    }

                    // Handle other messages
                    handleReceivedMessage(text);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing message", e);
                    // Still handle the message even if it's not JSON
                    handleReceivedMessage(text);
                }
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                Log.d(TAG, "WebSocket Closed - Code: " + code + ", Reason: " + reason);
                isConnected = false;
                isReconnecting = false;
                awaitingPong.set(false);
                notifyConnectionStatusChanged(false);
                stopHeartbeat();

                // Attempt reconnect if not manually closed
                if (!manualDisconnect && currentDeviceCode != null) {
                    scheduleReconnect(currentDeviceCode);
                }
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
                isConnected = false;
                isReconnecting = false;
                awaitingPong.set(false);

                Log.e(TAG, "WebSocket Failure: " + t.getMessage(), t);
                if (response != null) {
                    Log.e(TAG, "Response: " + response.message());
                }

                notifyConnectionStatusChanged(false);
                stopHeartbeat();

                // Attempt reconnect if not manually closed
                if (!manualDisconnect && currentDeviceCode != null) {
                    scheduleReconnect(currentDeviceCode);
                }
            }
        });
        Log.d(TAG, "WebSocket connection request sent");
    }



    private void handleReceivedMessage(String message) {
        Log.d(TAG, "Handling received WebSocket message");
        Intent intent = new Intent(context, MyAccessibilityService.class);
        intent.setAction("START_AUTOMATION");
        intent.putExtra("command", message);
        context.startService(intent);
        Log.d(TAG, "Intent sent to MyAccessibilityService");
    }



    private void scheduleReconnect(final String deviceCode) {
        // Don't schedule reconnect if we're already reconnecting
        if (isReconnecting) {
            Log.d(TAG, "Already in reconnecting state, skipping duplicate reconnect schedule");
            return;
        }

        isReconnecting = true;

        // Check if we've hit the maximum number of reconnect attempts
        int MAX_RECONNECT_ATTEMPTS = 5;
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            Log.w(TAG, "Maximum reconnection attempts reached (" + MAX_RECONNECT_ATTEMPTS +
                    "). Resetting and trying again.");
            reconnectAttempts = 0; // Reset the counter to try again instead of giving up
            // Alternatively, you could keep the original behavior by uncommenting the next line
            // isReconnecting = false;
            // return;
        }

        // Get the appropriate backoff time based on number of attempts
        int backoffTime = RECONNECT_BACKOFF_MS[Math.min(reconnectAttempts, RECONNECT_BACKOFF_MS.length - 1)];

        Log.d(TAG, "Scheduling reconnection attempt " + (reconnectAttempts + 1) +
                " in " + backoffTime + "ms");

        new Handler(context.getMainLooper()).postDelayed(() -> {
            if (!isConnected && !manualDisconnect) {
                Log.d(TAG, "Attempting to reconnect WebSocket (attempt " + (reconnectAttempts + 1) + ")");
                reconnectAttempts++;
                isReconnecting = false; // Reset the flag here
                connectWebSocket(deviceCode);
            } else {
                isReconnecting = false;
                Log.d(TAG, "Skipping scheduled reconnect - already connected or manually disconnected");
            }
        }, backoffTime);
    }

    public void closeWebSocket() {
        Log.d(TAG, "Closing WebSocket connection");
        manualDisconnect = true;
        stopHeartbeat();

        if (webSocket != null) {
            try {
                boolean success = webSocket.close(1000, "Normal closure");
                Log.d(TAG, "WebSocket close initiated: " + success);

                // Force cancel after a delay if graceful close doesn't work
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (webSocket != null) {
                        try {
                            webSocket.cancel();
                            Log.d(TAG, "WebSocket forcefully cancelled after grace period");
                        } catch (Exception e) {
                            Log.e(TAG, "Error cancelling webSocket", e);
                        }
                    }
                }, 2000);
            } catch (Exception e) {
                Log.e(TAG, "Error closing WebSocket", e);
                // Try to force cancel
                try {
                    webSocket.cancel();
                } catch (Exception cancelEx) {
                    Log.e(TAG, "Error cancelling webSocket", cancelEx);
                }
            } finally {
                webSocket = null;
                isConnected = false;
                isReconnecting = false;
                notifyConnectionStatusChanged(false);
            }
        } else {
            Log.d(TAG, "WebSocket was already null");
        }

        // Reset reconnection state
        reconnectAttempts = 0;
    }

    public boolean isConnected() {
        Log.d(TAG, "Checking connection status: " + isConnected);
        return isConnected;
    }

    public void setOnConnectionStatusChangedListener(ConnectionStatusListener listener) {
        Log.d(TAG, "Setting new connection status listener");
        this.connectionStatusListener = listener;
        if (listener != null) {
            listener.onConnectionStatusChanged(isConnected);
        }
    }

    private void notifyConnectionStatusChanged(boolean isConnected) {
        Log.d(TAG, "Notifying connection status changed: " + isConnected);
        if (connectionStatusListener != null) {
            connectionStatusListener.onConnectionStatusChanged(isConnected);
        } else {
            Log.w(TAG, "No connection status listener registered");
        }
    }

    public void sendMessage(String message, String taskId, String jobId, String type) {
        if (webSocket == null) {
            Log.d(TAG, "WebSocket is null and isConnected is " + isConnected);
        }
        if (webSocket != null && isConnected) {
            try {
                // Create JSON payload with task_id, job_id, and message
                JSONObject payload = new JSONObject();
                payload.put("message", message);
                payload.put("task_id", taskId);
                payload.put("job_id", jobId);
                payload.put("type", type);

                String jsonString = payload.toString();
                Log.d(TAG, "Sending WebSocket message: " + jsonString);
                webSocket.send(jsonString);
            } catch (Exception e) {
                Log.e(TAG, "Failed to send WebSocket message", e);
            }
        } else {
            Log.e(TAG, "WebSocket is not connected. Unable to send message.");
        }
    }

    private void startHeartbeat() {
        Log.d(TAG, "Starting heartbeat");
        // Remove any pending heartbeats
        heartbeatHandler.removeCallbacks(heartbeatRunnable);
        // Start the heartbeat
        heartbeatHandler.postDelayed(heartbeatRunnable, HEARTBEAT_INTERVAL);
    }

    private void stopHeartbeat() {
        Log.d(TAG, "Stopping heartbeat");
        heartbeatHandler.removeCallbacks(heartbeatRunnable);
        awaitingPong.set(false);
    }

    public interface ConnectionStatusListener {
        void onConnectionStatusChanged(boolean isConnected);
    }
}