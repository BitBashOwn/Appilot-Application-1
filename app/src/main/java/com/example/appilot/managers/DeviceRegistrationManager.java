package com.example.appilot.managers;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.example.appilot.UnsafeHttpClient;

import org.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import java.text.DateFormat;
import java.util.Date;

public class DeviceRegistrationManager {

    private static final String TAG = "DeviceRegistrationManager";

    public DeviceRegistrationManager(Context context) {
        // Constructor logic if needed
    }

    // Method to check if the device is already registered
    private boolean isDeviceRegistered(String deviceId) throws Exception {
        OkHttpClient client = new OkHttpClient();

        // Prepare the request to check if the device is registered
        Request request = new Request.Builder()
                .url("https://server.appilot.app/device_registration/" + deviceId)
                .get()
                .build();

        // Execute the request
        Response response = client.newCall(request).execute();

        if (response.isSuccessful()) {
            return true; // Device is registered
        } else if (response.code() == 404) {
            return false; // Device is not registered
        } else {
            throw new Exception("Failed to check device registration: " + response.message());
        }
    }

    // Method to register the device
    public void registerDevice(Context context, String email, DeviceRegistrationCallback callback) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
//                OkHttpClient client = UnsafeHttpClient.getUnsafeOkHttpClient();
                // Get device details
                String deviceName = android.os.Build.MODEL;
                String modelNumber = android.os.Build.PRODUCT;
                String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

                // First, check if the device is already registered
                if (isDeviceRegistered(androidId)) {
                    Log.d(TAG, "Device is already registered.");
                    callback.onSuccess(androidId);
                    return;
                }

                // If device is not registered, proceed with registration
                String activationDate = DateFormat.getDateTimeInstance().format(new Date());

                // Create the JSON body for the registration request
                JSONObject registrationBody = new JSONObject();
                registrationBody.put("deviceName", deviceName);
                registrationBody.put("model", modelNumber);
                registrationBody.put("deviceId", androidId);
                registrationBody.put("activationDate", activationDate);
                registrationBody.put("email", email);
                JSONArray botNameArray = new JSONArray();
                botNameArray.put("X (formerly Twitter) Bot");
                registrationBody.put("botName", botNameArray);

                // Prepare the registration request
                RequestBody body = RequestBody.create(
                        registrationBody.toString(), MediaType.parse("application/json"));
                Request request = new Request.Builder()
                        .url("https://server.appilot.app/register_device")
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .build();

                // Execute the registration request
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    Log.d(TAG, "Device registered successfully.");
                    callback.onSuccess(androidId);
                } else {
                    Log.e(TAG, "Device registration failed: " + response.message());
                    callback.onFailure("Registration failed: " + response.message());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
                callback.onFailure("Error: " + e.getMessage());
            }
        }).start();
    }

    // Callback interface to handle success or failure
    public interface DeviceRegistrationCallback {
        void onSuccess(String deviceCode);
        void onFailure(String errorMessage);
    }
}
