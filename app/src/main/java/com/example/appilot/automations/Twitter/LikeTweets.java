package com.example.appilot.automations.Twitter;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;
import com.example.appilot.services.MyAccessibilityService;
import com.example.appilot.utils.HelperFunctions;

import java.util.List;
import java.util.Random;

public class LikeTweets {
    private static final String TAG = "TwitterAutomation";
    private final Context context;
    private final Handler handler;
    private final Random random;
    private final PopUpHandler popUpHandler;

    private final MyAccessibilityService service;
    private HelperFunctions helperFunctions;
    private String Task_id = null;
    private String job_id = null;
    private List<Object> AccountInputs;
    private int duration;
    private long startTime;
    private double likeProbability;
    private int like_Limit;
    private int likeCount;
    private String todaysDate;
    private static final String PREFS_NAME = "TwitterBotPrefs";
    private static final String PREF_LIKE_COUNT = "likeCount";
    private static final String PREF_DATE = "dateLike";

    public LikeTweets(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs, int duration, double probabilty, int like_Limit, String date) {
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
        this.likeProbability = probabilty;
        this.like_Limit = like_Limit;
        this.todaysDate = date;
        // Load likeCount from SharedPreferences
        likeCount = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(PREF_LIKE_COUNT, 0);
    }

    public void startLikeAutomation() {
        Log.d(TAG, "Starting Like Automation with duration: " + duration + " minutes and like probability: " + likeProbability);
        // Get stored date from SharedPreferences
        String storedDate = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(PREF_DATE, null);
        if (storedDate == null) {
            // First time: store today's date
            likeCount = 0;
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(PREF_DATE, todaysDate)
                    .apply();
            Log.d(TAG, "First run: Storing today's date: " + todaysDate);
        } else {
            Log.d(TAG, "Loaded stored date: " + storedDate + ", Today's date: " + todaysDate);
            if (!todaysDate.equals(storedDate)) {
                // Date changed: reset likeCount and update stored date
                likeCount = 0;
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit()
                        .putInt(PREF_LIKE_COUNT, 0)
                        .putString(PREF_DATE, todaysDate)
                        .apply();
                Log.d(TAG, "Date changed. Reset likeCount to 0 and updated stored date to: " + todaysDate);
            }
        }
        if (likeCount == like_Limit) {
            Log.e(TAG, "You have reached the like limit");
            handler.postDelayed(()->{
                helperFunctions.cleanupAndExit("Like limit reached: " + likeCount + "/" + like_Limit, "final");
            }, 1000 + random.nextInt(3000));
            return;
        }
        findAndClickLikeButton();
    }
    private void findAndClickLikeButton() {
        Log.d(TAG, "Searching for Like Button");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available in like button");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present", "error");
            return;
        }
        Log.d(TAG, "Root node available, proceeding with decision logic");

        double randomValue = random.nextDouble();
        Log.d(TAG,"Random Value: "+randomValue+", Profile view probability: "+likeProbability);
        if (randomValue < likeProbability) {
            Log.d(TAG, "Clicking Like based on probability ("+(likeProbability*100)+ "%)");
            findAndClickLike(rootNode);
        } else {
            Log.d(TAG, "Moving to next tweet");
            int randomDelay = 1000 + random.nextInt(5000);
            handler.postDelayed(this::onScroll, randomDelay);
        }
        rootNode.recycle();
    }
    private void findAndClickLike(AccessibilityNodeInfo rootNode) {
        Log.d(TAG, "Searching for Like Button");

        String profileResourceId = "com.twitter.android:id/inline_like";
        AccessibilityNodeInfo likeButton = HelperFunctions.findNodeByResourceId(rootNode, profileResourceId);
        if (likeButton != null) {
            Log.d(TAG, "Found Like Button, attempting click");
            boolean clickSuccess = performClick(likeButton);
            if (clickSuccess) {
                Log.d(TAG, "Like Button clicked successfully. Waiting for profile to load...");
                // After successfully clicking the like button, increment likeCount
                likeCount++;
                saveLikeCount();
                Log.d(TAG, "Like count incremented: " + likeCount);
                if (likeCount == like_Limit) {
                    Log.e(TAG, "You have reached the like limit");
                    handler.postDelayed(()->{
                        helperFunctions.cleanupAndExit("Like limit reached: " + likeCount + "/" + like_Limit, "error");
                    }, 1000 + random.nextInt(3000));
                    return;
                }
                int randomDelay = 1000 + random.nextInt(5000);
                handler.postDelayed(this::onScroll, randomDelay);
            }
        } else {
            Log.d(TAG, "Like Button is not found");
            handler.postDelayed(this::onScroll, 2000);
        }
    }
    public void onScroll() {
        Log.d(TAG,"Duration : " + duration + " minutes");
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000 / 60;

        Log.d(TAG, "Elapsed Time: " + elapsedTime + " minutes");

        if (elapsedTime < duration) {
            int randomDelay = 2000 + random.nextInt(8000);
            handler.postDelayed(() -> {
                Log.d(TAG, "liking... (" + elapsedTime + "/" + duration + " minutes)");
                float endY = 0.0f + random.nextFloat() * (0.7f);
                float startY = endY + 0.3f;
                helperFunctions.performScroll(startY, endY);

                handler.postDelayed(this::findAndClickLikeButton, 1000);
            }, randomDelay);
        } else {
            Log.d(TAG, "Reached duration of " + duration + " minutes. Exiting app...");
            handler.postDelayed(() -> {
                helperFunctions.cleanupAndExit("Duration Completed for liking the Tweets...", "final");
            }, 2000);
        }
    }
    private boolean performClick(AccessibilityNodeInfo targetButton) {
        boolean isClicked = false;
        try {
            Log.d(TAG, "Found button. Attempting click.");
            isClicked = targetButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } catch (Exception e) {
            Log.e(TAG, "Error performing click action on button: " + e.getMessage());
        } finally {
            helperFunctions.safelyRecycleNode(targetButton);
        }
        if (isClicked) {
            Log.d(TAG, "Button clicked successfully. Proceeding to next step...");
        } else {
            Log.e(TAG, "Failed to click on button.");
        }
        return isClicked;
    }
    // Call this method after incrementing likeCount
    private void saveLikeCount() {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(PREF_LIKE_COUNT, likeCount)
                .apply();
    }
}
