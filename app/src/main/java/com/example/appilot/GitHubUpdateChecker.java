//
//package com.example.appilot;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.net.Uri;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//import androidx.core.content.FileProvider;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//
//public class GitHubUpdateChecker {
//    private static final String GITHUB_ASSETS_API_URL = "https://api.github.com/repos/BitBashOwn/appilot-APK/releases/latest";
//    private final String currentVersion;
//    private final String TAG = "GitHubUpdateChecker";
//    private final Context context;
//
//    public GitHubUpdateChecker(Context context, String currentVersion) {
//        this.context = context;
//        this.currentVersion = currentVersion;
//    }
//
//    private boolean isUpdateAvailable(String latestVersion, String currentVersion) {
//        try {
//            // Remove 'v' prefix if present
//            String current = currentVersion.replaceFirst("^v", "");
//            String latest = latestVersion.replaceFirst("^v", "");
//
//            // Split version numbers
//            String[] currentParts = current.split("\\.");
//            String[] latestParts = latest.split("\\.");
//
//            // Compare version numbers
//            for (int i = 0; i < Math.min(currentParts.length, latestParts.length); i++) {
//                int currentPart = Integer.parseInt(currentParts[i]);
//                int latestPart = Integer.parseInt(latestParts[i]);
//
//                if (latestPart > currentPart) {
//                    return true;
//                } else if (latestPart < currentPart) {
//                    return false;
//                }
//            }
//
//            // If all parts are equal, check if latest has more parts
//            return latestParts.length > currentParts.length;
//        } catch (Exception e) {
//            Log.e(TAG, "Error comparing versions: ", e);
//            return false;
//        }
//    }
//
//    public void checkForUpdates() {
//        Log.d(TAG, "Checking for updates...");
//        Log.d(TAG, "Current Version: " + this.currentVersion);
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//        Handler handler = new Handler(Looper.getMainLooper());
//
//        executor.execute(() -> {
//            try {
//                // Fetch the latest APK URL and version name
//                String[] updateInfo = fetchLatestApkInfo();
//                String latestApkUrl = updateInfo[0];
//                String latestVersion = updateInfo[1];
//
//                if (latestApkUrl != null && latestVersion != null && isUpdateAvailable(latestVersion, currentVersion)) {
//                    Log.d(TAG, "Update available. Current Version: " + currentVersion + ", Latest Version: " + latestVersion);
//                    Log.d(TAG, "Latest APK URL: " + latestApkUrl);
//
//                    handler.post(() -> {
//                        Log.d(TAG, "Starting update from version " + currentVersion + " to version " + latestVersion);
//                        downloadAndInstallUpdate(latestApkUrl, latestVersion);
//                    });
//                } else {
//                    Log.d(TAG, "No updates available. The app is up-to-date.");
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "Error checking for updates: ", e);
//            }
//        });
//    }
//
//    private String[] fetchLatestApkInfo() throws Exception {
//        Log.d(TAG, "Fetching the latest APK info from: " + GITHUB_ASSETS_API_URL);
//        OkHttpClient client = new OkHttpClient();
//        Request request = new Request.Builder()
//                .url(GITHUB_ASSETS_API_URL)
//                .header("Accept", "application/vnd.github.v3+json")
//                .build();
//
//        String latestApkUrl = null;
//        String latestVersion = null;
//
//        try (Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) {
//                Log.e(TAG, "Failed to fetch data from GitHub API. Response code: " + response.code());
//                throw new IOException("Unexpected response: " + response);
//            }
//
//            assert response.body() != null;
//            String jsonData = response.body().string();
//            Log.d(TAG, "GitHub API response: " + jsonData);
//
//            JSONObject jsonObject = new JSONObject(jsonData);
//            latestVersion = jsonObject.getString("tag_name");
//            JSONArray assets = jsonObject.getJSONArray("assets");
//
//            for (int i = 0; i < assets.length(); i++) {
//                JSONObject asset = assets.getJSONObject(i);
//                String name = asset.getString("name");
//                if (name.endsWith(".apk")) {
//                    latestApkUrl = asset.getString("browser_download_url");
//                    Log.d(TAG, "Found APK: " + name + " at URL: " + latestApkUrl);
//                    break;
//                }
//            }
//        }
//
//        if (latestApkUrl == null || latestVersion == null) {
//            throw new Exception("Could not find APK or version information in the release");
//        }
//
//        return new String[]{latestApkUrl, latestVersion};
//    }
//
//    private void downloadAndInstallUpdate(String apkUrl, String latestVersion) {
//        Log.d(TAG, "Starting download for APK: " + apkUrl);
//
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//        Handler handler = new Handler(Looper.getMainLooper());
//
//        executor.execute(() -> {
//            try {
//                OkHttpClient client = new OkHttpClient();
//                Request request = new Request.Builder()
//                        .url(apkUrl)
//                        .build();
//
//                try (Response response = client.newCall(request).execute()) {
//                    if (!response.isSuccessful()) {
//                        Log.e(TAG, "Failed to download APK. Response code: " + response.code());
//                        return;
//                    }
//
//                    // Create a unique file name for the update
//                    File outputFile = new File(context.getExternalFilesDir(null),
//                            "update_" + latestVersion + ".apk");
//
//                    // Delete existing update files
//                    File[] existingUpdates = context.getExternalFilesDir(null).listFiles(
//                            (dir, name) -> name.startsWith("update_") && name.endsWith(".apk"));
//                    if (existingUpdates != null) {
//                        for (File file : existingUpdates) {
//                            file.delete();
//                        }
//                    }
//
//                    assert response.body() != null;
//                    try (InputStream inputStream = response.body().byteStream();
//                         FileOutputStream outputStream = new FileOutputStream(outputFile)) {
//
//                        byte[] buffer = new byte[4096];
//                        int bytesRead;
//                        long totalBytesRead = 0;
//                        long fileSize = response.body().contentLength();
//
//                        while ((bytesRead = inputStream.read(buffer)) != -1) {
//                            outputStream.write(buffer, 0, bytesRead);
//                            totalBytesRead += bytesRead;
//
//                            // Log download progress
//                            if (fileSize > 0) {
//                                int progress = (int) ((totalBytesRead * 100) / fileSize);
//                                Log.d(TAG, "Download progress: " + progress + "%");
//                            }
//                        }
//                        outputStream.flush();
//                    }
//
//                    Log.d(TAG, "Download completed. APK saved to: " + outputFile.getAbsolutePath());
//
//                    handler.post(() -> {
//                        try {
//                            Intent intent = new Intent(Intent.ACTION_VIEW);
//                            Uri uri = FileProvider.getUriForFile(
//                                    context,
//                                    context.getPackageName() + ".fileprovider",
//                                    outputFile
//                            );
//                            intent.setDataAndType(uri, "application/vnd.android.package-archive");
//                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            context.startActivity(intent);
//                            Log.d(TAG, "Installation intent started for version " + latestVersion);
//                        } catch (Exception e) {
//                            Log.e(TAG, "Error during APK installation: ", e);
//                        }
//                    });
//
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "Error during download process: ", e);
//            }
//        });
//    }
//}







package com.example.appilot;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GitHubUpdateChecker {
    private static final String GITHUB_ASSETS_API_URL = "https://api.github.com/repos/BitBashOwn/appilot-APK/releases/latest";
    private final String currentVersion;
    private final String TAG = "GitHubUpdateChecker";
    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public GitHubUpdateChecker(Context context, String currentVersion) {
        this.context = context;
        this.currentVersion = currentVersion;
    }

    public void checkForUpdates() {
        Log.d(TAG, "Checking for updates...");
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                String[] updateInfo = fetchLatestApkInfo();
                String latestApkUrl = updateInfo[0];
                String latestVersion = updateInfo[1];

                if (latestApkUrl != null && latestVersion != null && isUpdateAvailable(latestVersion, currentVersion)) {
                    Log.d(TAG, "Update available: " + latestVersion);

                    // Check if auto-update is enabled
                    SharedPreferences prefs = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
                    boolean autoUpdateEnabled = prefs.getBoolean("auto_update_enabled", false);

                    if (autoUpdateEnabled) {
                        Log.d(TAG, "Auto-update enabled. Downloading in background.");
                        startBackgroundDownload(latestApkUrl, latestVersion);
                    } else {
                        // Ask for confirmation
                        handler.post(() -> showUpdateDialog(latestApkUrl, latestVersion));
                    }
                } else {
                    Log.d(TAG, "No updates available. The app is up-to-date.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking for updates: ", e);
            }
        });
    }


    private String[] fetchLatestApkInfo() throws Exception {
        Log.d(TAG, "Fetching the latest APK info...");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(GITHUB_ASSETS_API_URL)
                .header("Accept", "application/vnd.github.v3+json")
                .build();

        String latestApkUrl = null;
        String latestVersion = null;

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e(TAG, "Failed to fetch data from GitHub API. Response code: " + response.code());
                throw new IOException("Unexpected response: " + response);
            }

            assert response.body() != null;
            String jsonData = response.body().string();
            Log.d(TAG, "GitHub API response: " + jsonData);

            // Parse JSON response
            JSONObject jsonObject = new JSONObject(jsonData);
            latestVersion = jsonObject.optString("tag_name", null);
            JSONArray assets = jsonObject.optJSONArray("assets");

            if (assets != null) {
                for (int i = 0; i < assets.length(); i++) {
                    JSONObject asset = assets.getJSONObject(i);
                    String name = asset.optString("name", "");
                    if (name.endsWith(".apk")) {
                        latestApkUrl = asset.optString("browser_download_url", null);
                        Log.d(TAG, "Found APK: " + name + " at URL: " + latestApkUrl);
                        break;
                    }
                }
            }
        }

        if (latestApkUrl == null || latestVersion == null) {
            throw new Exception("Could not find APK or version information in the release.");
        }

        return new String[]{latestApkUrl, latestVersion};
    }


    private void showUpdateDialog(String apkUrl, String latestVersion) {
        new AlertDialog.Builder(context)
                .setTitle("Update Available")
                .setMessage("A new version (" + latestVersion + ") is available. Do you want to update?")
                .setPositiveButton("Update", (dialog, which) -> startBackgroundDownload(apkUrl, latestVersion))
                .setNegativeButton("Cancel", (dialog, which) -> Log.d(TAG, "User declined the update."))
                .setCancelable(false)
                .show();
    }

    private void startBackgroundDownload(String apkUrl, String latestVersion) {
        Log.d(TAG, "Starting download...");

        // Show a progress dialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Downloading Update");
        progressDialog.setMessage("Please wait...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        handler.post(progressDialog::show);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(apkUrl).build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Failed to download APK. Response code: " + response.code());
                    handler.post(progressDialog::dismiss);
                    return;
                }

                // Create file for the APK
                File outputFile = new File(context.getExternalFilesDir(null), "update_" + latestVersion + ".apk");

                // Delete previous update files
                File[] existingUpdates = context.getExternalFilesDir(null).listFiles(
                        (dir, name) -> name.startsWith("update_") && name.endsWith(".apk"));
                if (existingUpdates != null) {
                    for (File file : existingUpdates) {
                        file.delete();
                    }
                }

                assert response.body() != null;
                try (InputStream inputStream = response.body().byteStream();
                     FileOutputStream outputStream = new FileOutputStream(outputFile)) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long totalBytesRead = 0;
                    long fileSize = response.body().contentLength();

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;

                        // Update progress bar
                        int progress = fileSize > 0 ? (int) ((totalBytesRead * 100) / fileSize) : -1;
                        handler.post(() -> progressDialog.setProgress(progress));
                    }
                    outputStream.flush();
                }

                Log.d(TAG, "Download completed.");
                handler.post(() -> {
                    progressDialog.dismiss();
                    installApk(outputFile);
                });

            } catch (Exception e) {
                Log.e(TAG, "Error during download process: ", e);
                handler.post(progressDialog::dismiss);
            }
        }).start();
    }

    private void installApk(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", apkFile);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        Log.d(TAG, "Installation started.");
    }

    private boolean isUpdateAvailable(String latestVersion, String currentVersion) {
        try {
            String[] currentParts = currentVersion.replaceFirst("^v", "").split("\\.");
            String[] latestParts = latestVersion.replaceFirst("^v", "").split("\\.");

            for (int i = 0; i < Math.min(currentParts.length, latestParts.length); i++) {
                int currentPart = Integer.parseInt(currentParts[i]);
                int latestPart = Integer.parseInt(latestParts[i]);

                if (latestPart > currentPart) return true;
                if (latestPart < currentPart) return false;
            }

            return latestParts.length > currentParts.length;
        } catch (Exception e) {
            Log.e(TAG, "Error comparing versions: ", e);
            return false;
        }
    }
}

