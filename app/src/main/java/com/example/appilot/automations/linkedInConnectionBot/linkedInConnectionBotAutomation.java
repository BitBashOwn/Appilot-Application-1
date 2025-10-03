package com.example.appilot.automations.linkedInConnectionBot;

import android.content.Intent;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;
import android.content.Context;
import android.os.Handler;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;
import java.util.Random;
import com.example.appilot.services.MyAccessibilityService;
import com.example.appilot.utils.HelperFunctions;

public class linkedInConnectionBotAutomation {
    private static final String TAG = "LinkedInAutomation";
    private static final String LINKEDIN_PACKAGE = "com.linkedin.android";
    private final Context context;
    private final Handler handler;
    private final Random random;
    private final MyAccessibilityService service;
    private HelperFunctions helperFunctions;
    private String Task_id = null;
    private String job_id = null;


    public linkedInConnectionBotAutomation(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs) {
        this.context = service;
        this.service = service;
        this.Task_id = taskid;
        this.job_id = jobid;
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();
        this.helperFunctions = new HelperFunctions(context, Task_id, job_id);
    }

    public void launchApp() {
        Log.d(TAG, "Attempting to launch app: " + LINKEDIN_PACKAGE);
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(LINKEDIN_PACKAGE);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            try {
                Log.d(TAG, "Launching LinkedIn app using package intent.");
                context.startActivity(intent);

                // Wait a few seconds for the app to launch and then perform the connection sending
                handler.postDelayed(this::findAndClickMyNetwork, 5000);  // Adjust delay as needed
            } catch (Exception e) {
                Log.e(TAG, "Error launching LinkedIn app", e);
            }
        } else {
            Log.e(TAG, "Could not launch app: " + LINKEDIN_PACKAGE);
            launchLinkedInExplicitly();
        }
    }

    private void launchLinkedInExplicitly() {
        Log.d(TAG, "Launching LinkedIn explicitly via URL.");
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse("https://www.linkedin.com/"))
                .setPackage(LINKEDIN_PACKAGE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
            // Wait for app to load and then perform the connection action
            Log.d(TAG, "Going to enter into Send connection function.");
            handler.postDelayed(this::findAndClickMyNetwork, 5000);  // Adjust delay as needed
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch LinkedIn explicitly", e);
        }
    }


    private void findAndClickMyNetwork() {
        Log.i(TAG,"Entered findAndClickMyNetwork");
        AccessibilityNodeInfo myNetworkButton = helperFunctions.FindAndReturnNodeById("com.linkedin.android:id/tab_relationships", 10);

        if (myNetworkButton != null) {
            handler.postDelayed(() -> {
                boolean isClicked = false;
                try {
                    Log.d(TAG, "Found 'My Network' button. Attempting click.");
                    isClicked = myNetworkButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                } catch (Exception e) {
                    Log.e(TAG, "Error performing click action on My Network button: " + e.getMessage());
                    helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled. My Network Button Could not found", "error");
                    return;
                }

                // Recycle the node to avoid memory leaks
                helperFunctions.safelyRecycleNode(myNetworkButton);

                if (isClicked) {
                    Log.d(TAG, "'My Network' button clicked. Proceeding to next step...");
                    handler.postDelayed(()->{
                        helperFunctions.cleanupAndExit("Automation Completed.", "final");
                    },2000);
                } else {
                    Log.e(TAG, "Failed to click on 'My Network' button.");
                    helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled. My Network Button Could not found", "error");
                }
            }, 1000);  // 1-second delay before clicking the button
        } else {
            Log.e(TAG, "'My Network' button not found.");
            helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled. My Network Button Could not found", "error");
        }
    }

}