package com.example.appilot.automations.LinkedIn;

import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;
import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;
import com.example.appilot.services.MyAccessibilityService;
import com.example.appilot.utils.HelperFunctions;
import java.util.List;
import java.util.Random;

public class Notification {
    private static final String TAG = "LinkedInAutomation";
    private final Context context;
    private final Handler handler;
    private final Random random;
    private final PopUpHandler popUpHandler;
    private final MyAccessibilityService service;
    private HelperFunctions helperFunctions;
    private String Task_id = null;
    private String job_id = null;
    private int count = 0;
    private int scroll = 0;
    private final int MAX_COUNT = 5;

    public Notification(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs) {
        this.context = service;
        this.service = service;
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();
        this.Task_id = taskid;
        this.job_id = jobid;
        this.popUpHandler = new PopUpHandler(this.service, this.handler, this.random, this.helperFunctions);
        this.helperFunctions = new HelperFunctions(context, Task_id, job_id);
    }
    public void seeNotification() {
        Log.d(TAG, "Entering seeNotification method");
        // Verify one more time that we have root access
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available in Notification");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present", "error");
            return;
        }
        Log.d(TAG, "Root node available, proceeding with connection logic");
        String nodeId = "com.linkedin.android:id/tab_notifications";
        AccessibilityNodeInfo Button = HelperFunctions.findNodeByResourceId(rootNode, nodeId);
        performNotificationClick(Button);
        rootNode.recycle();
    }
    private void performNotificationClick(AccessibilityNodeInfo Button) {
        handler.postDelayed(() -> {
            boolean isClicked = false;
            try {
                Log.d(TAG, "Found 'Notification' button. Attempting click.");
                isClicked = Button.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            } catch (Exception e) {
                Log.e(TAG, "Error performing click action on Notification button: " + e.getMessage());
            } finally {
                helperFunctions.safelyRecycleNode(Button);
            }
            if (isClicked) {
                Log.d(TAG, "'Load More' button clicked successfully. Proceeding to next step...");
                handler.postDelayed(this::loadMore, 1000);
            } else {
                Log.e(TAG, "Failed to click on 'Load More' button.");
                helperFunctions.cleanupAndExit("Failed to click Load More button", "error");
            }
        }, 2000);
    }
    public void loadMore() {
        Log.d(TAG, "Entering loadMore method");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available in LoadMore");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present", "error");
            return;
        }
        Log.d(TAG, "Root node available, proceeding with connection logic");
        String nodeId = "com.linkedin.android:id/infra_load_more_footer_button";
        AccessibilityNodeInfo Button = HelperFunctions.findNodeByResourceId(rootNode, nodeId);
        // Check if scroll limit reached first
        if (scroll >= 10) {
            Log.d(TAG, "Maximum scroll attempts (10) reached, exiting process");
            rootNode.recycle();
            handler.postDelayed(() -> {
                helperFunctions.cleanupAndExit("Scroll attempts are completed - Max limit reached", "final");
            }, 1000);
            return;
        }
        // If button not found and scroll limit not reached, continue scrolling
        if (Button == null) {
            Log.d(TAG, "Load more button not found, scrolling down. Attempt: " + (scroll + 1) + "/10");
            handler.postDelayed(() -> {
                helperFunctions.performScroll(0.8f, 0.3f);
                scroll++;
                handler.postDelayed(this::loadMore, 1000);
            }, 2000);
            rootNode.recycle();
        }
        // If button found, click it
        else {
            Log.d(TAG, "Load more button found, performing click");
            performLoadMoreClick(Button);
            rootNode.recycle();
        }
    }
    private void performLoadMoreClick(AccessibilityNodeInfo Button) {
        handler.postDelayed(() -> {
            boolean isClicked = false;
            try {
                Log.d(TAG, "Found 'Load More' button. Attempting click.");
                isClicked = Button.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            } catch (Exception e) {
                Log.e(TAG, "Error performing click action on Load More button: " + e.getMessage());
            } finally {
                helperFunctions.safelyRecycleNode(Button);
            }
            if (isClicked) {
                Log.d(TAG, "'Load More' button clicked successfully. Proceeding to next step...");
                handler.postDelayed(this::onLoadMoreClicked, 1000);
            } else {
                Log.e(TAG, "Failed to click on 'Notification' button.");
                helperFunctions.cleanupAndExit("Failed to click Notification button", "error");
            }
        }, 2000);
    }
    private void onLoadMoreClicked() {
        count++;
        Log.d(TAG, "Connect button successfully clicked! Count: " + count + "/" + MAX_COUNT);
        Log.d(TAG, "Number of connects passed by user : " + MAX_COUNT);
        if (count <= MAX_COUNT) {
            handler.postDelayed(() -> {
                Log.d(TAG, "Looking for next Accept button... (" + (count + 1) + "/" + MAX_COUNT + ")");
                helperFunctions.performScroll(0.8f,0.3f);
                handler.postDelayed(this::loadMore, 1000);
            }, 2000);
        } else {
            Log.d(TAG, "Reached maximum Connect clicks (" + MAX_COUNT + "). Looping back to sendConnection...");
            count = 0;
            handler.postDelayed(() -> {
                Log.d(TAG, "Waiting to Exit");
                helperFunctions.cleanupAndExit("Click the invitation tab", "final");
            }, 2000);
        }
    }
}
