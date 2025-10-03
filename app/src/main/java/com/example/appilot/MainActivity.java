package com.example.appilot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appilot.managers.DeviceRegistrationManager;

import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private DeviceRegistrationManager deviceRegistrationManager;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "DevicePrefs";
    private Button loginButton;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String deviceCode = sharedPreferences.getString("deviceCode", null);

        if (isLoggedIn && deviceCode != null) {
            // Show the home Screen
            showHomeScreen();
        } else {
            // Show login screen if not logged in
            setContentView(R.layout.activity_main);
            initializeUIComponents();
        }
//        String currentAppVersion = null;
//        try {
//            currentAppVersion = getPackageManager()
//                    .getPackageInfo(getPackageName(), 0).versionName;
//        } catch (PackageManager.NameNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//
//        GitHubUpdateChecker updateChecker = new GitHubUpdateChecker(
//                this,
//                currentAppVersion
//        );
//        updateChecker.checkForUpdates();
    }

    // Method to initialize the UI components
    private void initializeUIComponents() {
        emailEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        // Initialize the DeviceRegistrationManager with the context
        deviceRegistrationManager = new DeviceRegistrationManager(this); // Pass context here

        // Check if Accessibility Service is enabled
        if (!isAccessibilityServiceEnabled()) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, "Please enable the Accessibility Service.", Toast.LENGTH_LONG).show();
        }

        // Set click listener for login button
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            if (!email.isEmpty() && !password.isEmpty()) {
                loginButton.setEnabled(false);
                authenticateUser(email, password);
            } else {
                Toast.makeText(MainActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to check if the Accessibility Service is enabled
    private boolean isAccessibilityServiceEnabled() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo serviceInfo : enabledServices) {
            if (serviceInfo.getId().contains(getPackageName())) {
                Log.d("MainActivity", "Accessibility Service enabled");
                return true;
            }
        }
        return false;
    }

    // Authenticate user with email and password
    private void authenticateUser(String email, String password) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                // Create the JSON body for the login request
                JSONObject loginBody = new JSONObject();
                loginBody.put("email", email);
                loginBody.put("password", password);

                // Prepare the request
                RequestBody body = RequestBody.create(loginBody.toString(), MediaType.parse("application/json"));
                Request request = new Request.Builder()
                        .url("https://server.appilot.app/login")
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .build();

                // Execute the request
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        deviceRegistrationManager.registerDevice(MainActivity.this, email, new DeviceRegistrationManager.DeviceRegistrationCallback()  {
                            @Override
                            public void onSuccess(String deviceCode) {
                                runOnUiThread(() -> {
                                    saveLoginState(deviceCode);
                                    showHomeScreen();  // Proceed to the Home Screen
                                });
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this, "Device registration failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                                    loginButton.setEnabled(true);
                                });
                            }
                        });
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                        loginButton.setEnabled(true);
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    loginButton.setEnabled(true);
                });
            }
        }).start();
    }

//    // Check the device status
//    private void checkDeviceStatus(String deviceCode) {
//        new Thread(() -> {
//            try {
//                OkHttpClient client = new OkHttpClient();
//                Request request = new Request.Builder()
//                        .url("https://server.appilot.app/device_status/" + deviceCode)
//                        .get()
//                        .build();
//
//                Response response = client.newCall(request).execute();
//                if (response.isSuccessful()) {
//                    String responseBody = response.body().string();
//                    JSONObject jsonResponse = new JSONObject(responseBody);
//                    boolean status = jsonResponse.getBoolean("status");
//
//                    if (!status) {
//                        runOnUiThread(() -> logout());
//                    } else {
//                        runOnUiThread(this::showHomeScreen);
//                    }
//                } else {
//                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to check device status.", Toast.LENGTH_SHORT).show());
//                }
//            } catch (Exception e) {
//                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
//            }
//        }).start();
//    }

    // Save login state
    private void saveLoginState(String deviceCode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("deviceCode", deviceCode);
        editor.apply();
    }
//
//    // Logout the user
//    private void logout() {
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean("isLoggedIn", false);
//        editor.remove("deviceCode");
//        editor.apply();
//
//        // Redirect to login screen
//        Intent intent = new Intent(MainActivity.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        finish();
//    }

    // Navigate to the Home Screen
    private void showHomeScreen() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}