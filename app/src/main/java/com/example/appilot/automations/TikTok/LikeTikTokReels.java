package com.example.appilot.automations.TikTok;

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

public class LikeTikTokReels {
    private static final String TAG = "LinkedInAutomation";
    private final Context context;
    private final Handler handler;
    private final Random random;
    private final PopUpHandler popUpHandler;
    private final MyAccessibilityService service;
    private HelperFunctions helperFunctions;
    private String Task_id = null;
    private String job_id = null;
    private int duration;
    private long startTime;

    public LikeTikTokReels(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs, int duration) {
        this.context = service;
        this.service = service;
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();
        this.Task_id = taskid;
        this.job_id = jobid;
        this.popUpHandler = new PopUpHandler(this.service, this.handler, this.random, this.helperFunctions);
        this.helperFunctions = new HelperFunctions(context, Task_id, job_id);
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }
    public void findLikeButton() {
        Log.d(TAG, "Entering findLikeButton method");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present", "error");
            return;
        }
        Log.d(TAG, "Root node available, proceeding...");
        String nodeId = "com.zhiliaoapp.musically:id/edq";
        AccessibilityNodeInfo Button = HelperFunctions.findNodeByResourceId(rootNode, nodeId);
        if (Button != null ) {
            performClickOnLike(Button);
        } else {
            Log.e(TAG, "Like button not found!");
        }
        rootNode.recycle();
    }
    private void performClickOnLike(AccessibilityNodeInfo Button) {
        handler.postDelayed(() -> {
            boolean isClicked = false;
            try {
                Log.d(TAG, "Found 'Like' button. Attempting click...");
                isClicked = Button.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            } catch (Exception e) {
                Log.e(TAG, "Error performing click action on button: " + e.getMessage());
            } finally {
                helperFunctions.safelyRecycleNode(Button);
            }
            if (isClicked) {
                Log.d(TAG, "Button clicked successfully. Proceeding to next step...");
                int randomDelay = 2000 + random.nextInt(8000);
                handler.postDelayed(this::onScroll,randomDelay);
            } else {
                Log.e(TAG, "Failed to click on button.");
                helperFunctions.cleanupAndExit("Failed to click My Network button", "error");
            }
        }, 2000);
    }
    public void onScroll() {
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000 / 60;
        Log.d(TAG,"Elapsed Time: "+ elapsedTime +" minutes");
        if (elapsedTime < duration) {
            handler.postDelayed(() -> {
                Log.d(TAG, "Scrolling... (" + elapsedTime + "/" + duration + " minutes)");
                helperFunctions.performScroll(0.8f,0.3f);
                handler.postDelayed(this::findLikeButton, 3000);
            }, 3000);
        } else {
            Log.d(TAG, "Reached duration of (" + duration + "). Looping back to Scroll...");
            handler.postDelayed(() -> {
                helperFunctions.cleanupAndExit("Duration Completed, Exiting..", "final");
            }, 2000);
        }
    }
}
