package com.example.appilot.automations.Twitter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;
import com.example.appilot.services.MyAccessibilityService;
import com.example.appilot.utils.HelperFunctions;

import java.util.List;
import java.util.Random;

public class JoinSpaces {
    private static final String TAG = "TwitterAutomation";
    private final Context context;
    private final Handler handler;
    private final Random random;
    private final PopUpHandler popUpHandler;
    private final MyAccessibilityService service;
    private HelperFunctions helperFunctions;
    private String Task_id = null;
    private String job_id = null;
    private String URL;
    private List<Object> AccountInputs;
    private int duration;
    private long startTime;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;


    public JoinSpaces(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs, int duration, String URL) {
        this.context = service;
        this.service = service;
        this.Task_id = taskid;
        this.job_id = jobid;
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();
        this.popUpHandler = new PopUpHandler(this.service, this.handler, this.random, this.helperFunctions);
        this.helperFunctions = new HelperFunctions(context, Task_id, job_id);
        this.AccountInputs = AccountInputs;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
        this.URL = URL;

    }
    public void startSpaceAutomation() {
        Log.d(TAG, "Starting Twitter Join Spaces Automation: " + URL);
        handler.postDelayed(this::launchIntent, 1000 + random.nextInt(3000));
    }
    private void launchIntent() {
        try {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(URL));
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            Log.d(TAG, "Twitter Spaces link: " + URL);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Twitter Spaces link: " + e.getMessage());
        }
        handler.postDelayed(this::findAndClickStartListening, 5000 + random.nextInt(5000));
    }
    private void findAndClickStartListening() {
        Log.d(TAG, "Searching for Listening Button...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickStartListening", "error");
            return;
        }

        String startButton = "com.twitter.android:id/room_ticket_button";
        AccessibilityNodeInfo startListening = HelperFunctions.findNodeByResourceId(rootNode, startButton);
        if (startListening != null) {
            Log.d(TAG, "Found Start button, attempting click...");
            boolean clickSuccess = performClick(startListening);
            if (clickSuccess) {
                Log.d(TAG, "Start button clicked. Waiting to join Space...");
                handler.postDelayed(this::onWait, 2000+ random.nextInt(3000));
            }
        } else {
            Log.d(TAG, "Start button not found");
            if (retryCount < MAX_RETRIES) {
                retryCount++;
                Log.d(TAG, "Retrying to launch intent, attempt: " + retryCount);
                handler.postDelayed(this::launchIntent, 2000 + random.nextInt(3000));
            } else {
                helperFunctions.cleanupAndExit("Automation Could not be Completed, Start button not found after retries", "error");
            }
        }
        rootNode.recycle();
    }
    public void onWait() {
        Log.d(TAG, "Duration : " + duration + " minutes");
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000 / 60;
        Log.d(TAG, "Elapsed Time: " + elapsedTime + " minutes");

        if (elapsedTime >= duration) {
            Log.d(TAG, "Reached duration. Clicking leave...");
            handler.postDelayed(this::findAndClickLeaveButton, 2000 + random.nextInt(3000));
        } else {
            Log.d(TAG, "Continuing activity...");
            handler.postDelayed(this::findAndClickClosePopup, 500+ random.nextInt(1000));
        }
    }
    private void findAndClickLeaveButton() {
        Log.d(TAG, "Searching for Leave Button...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickLeaveButton", "error");
            return;
        }

        String endButton = "com.twitter.android:id/header_text_end";
        AccessibilityNodeInfo leaveSpace = HelperFunctions.findNodeByResourceId(rootNode, endButton);
        if (leaveSpace != null) {
            Log.d(TAG, "Found leave button, attempting click...");
            boolean clickSuccess = performClick(leaveSpace);
            if (clickSuccess) {
                Log.d(TAG, "Leave button clicked. Waiting to exit space...");
                handler.postDelayed(()->{
                    helperFunctions.performScroll(0.8f, 0.3f);
                    handler.postDelayed(()->{helperFunctions.cleanupAndExit("Duration Completed, Exiting the space...", "final");},2000);
                },2000+ random.nextInt(3000));
            }
        } else {
            Log.d(TAG, "Leave button not found");
            int randomDelay = 2000 + random.nextInt(3000);
            handler.postDelayed(() -> {
                helperFunctions.cleanupAndExit("Leave button not found, Means the space has been ended", "final");
            }, randomDelay);
        }
        rootNode.recycle();
    }
    private void findAndClickClosePopup() {
        Log.d(TAG, "Searching for Close Popup...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickClosePopup", "error");
            return;
        }

        String closeButton = "com.twitter.android:id/room_nudge_dismiss_button";
        AccessibilityNodeInfo closePopUp = HelperFunctions.findNodeByResourceId(rootNode, closeButton);
        if (closePopUp != null) {
            Log.d(TAG, "Found close popup button, attempting click...");
            boolean clickSuccess = performClick(closePopUp);
            if (clickSuccess) {
                Log.d(TAG, "Close popup button clicked. Waiting to close popup...");
                handler.postDelayed(this::onWait,30000);
            }
        } else {
            Log.d(TAG, "Close popup button not found");
            handler.postDelayed(this::onWait, 30000);
        }
        rootNode.recycle();
    }


    private boolean performClick(AccessibilityNodeInfo node) {
        if (node == null) {
            Log.e(TAG, "Node is null, cannot perform click");
            return false;
        }
        if (node.isClickable()) {
            Log.d(TAG, "Node is clickable, performing click");
            boolean clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            if (clicked) {
                Log.d(TAG, "Click action performed successfully");
            } else {
                Log.e(TAG, "Click action failed");
            }
            return clicked;
        } else {
            Log.d(TAG, "Node is not clickable, attempting to find clickable parent");
            AccessibilityNodeInfo parent = node.getParent();
            while (parent != null) {
                if (parent.isClickable()) {
                    Log.d(TAG, "Found clickable parent, performing click");
                    boolean clicked = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    if (clicked) {
                        Log.d(TAG, "Click action on parent performed successfully");
                    } else {
                        Log.e(TAG, "Click action on parent failed");
                    }
                    return clicked;
                }
                parent = parent.getParent();
            }
            Log.e(TAG, "No clickable parent found");
            return false;
        }
    }
}
