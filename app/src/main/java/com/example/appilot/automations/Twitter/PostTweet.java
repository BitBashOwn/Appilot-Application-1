package com.example.appilot.automations.Twitter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;
import com.example.appilot.services.MyAccessibilityService;
import com.example.appilot.utils.HelperFunctions;
import com.example.appilot.utils.OpenAIClient;

import java.util.List;
import java.util.Random;

public class PostTweet {
    private static final String TAG = "TwitterAutomation";
    private final Context context;
    private final Handler handler;
    private final Random random;
    private final PopUpHandler popUpHandler;
    private final MyAccessibilityService service;
    private HelperFunctions helperFunctions;
    private String Task_id = null;
    private String job_id = null;
    private String prompt;
    private String openAPIKey;
    private List<Object> AccountInputs;
    private int duration;
    private long startTime;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;

    public PostTweet(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs, int duration, String prompt, String openAPIKey) {
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
        this.prompt = prompt;
        this.openAPIKey = openAPIKey;
    }

    public void startPostingAutomation() {
        Log.d(TAG, "Starting Twitter Post Tweet Automation with prompt: " + prompt);
        handler.postDelayed(this::findAndClickPlus, 2000 + random.nextInt(3000));
    }
    private void findAndClickPlus() {
        Log.d(TAG, "Searching for Plus button...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickPlus", "error");
            return;
        }
        String ButtonId = "com.twitter.android:id/composer_write";
        AccessibilityNodeInfo Button = HelperFunctions.findNodeByResourceId(rootNode, ButtonId);
        if (Button != null) {
            Log.d(TAG, "Found Plus Button, attempting click...");
            boolean clickSuccess = performClick(Button);
            if (clickSuccess) {
                Log.d(TAG, "Plus button clicked. Moving to Add Tweet Button...");
                int randomDelay = 3000 + random.nextInt(5000);
                handler.postDelayed(this::findAndClickPost, randomDelay);
            }
        } else {
            Log.d(TAG, "Plus Button not found");
            int randomDelay = 2000 + random.nextInt(3000);
            handler.postDelayed(() -> {
                helperFunctions.cleanupAndExit("Could not find the Plus Button, exiting the activity", "error");
            }, randomDelay);
        }
        rootNode.recycle();
    }
    private void findAndClickPost() {
        Log.d(TAG, "Searching for Post button...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickPost", "error");
            return;
        }
        String ButtonId = "com.twitter.android:id/composer_write";
        AccessibilityNodeInfo Button = HelperFunctions.findNodeByResourceId(rootNode, ButtonId);
        if (Button != null) {
            Log.d(TAG, "Found Post Button, attempting click...");
            boolean clickSuccess = performClick(Button);
            if (clickSuccess) {
                Log.d(TAG, "Post button clicked. Going to send prompt to OpenAI...");
                sendPromptToOpenAIAndType();
                return;
            }
        } else {
            Log.d(TAG, "Post Button not found");
            int randomDelay = 2000 + random.nextInt(3000);
            handler.postDelayed(() -> {
                helperFunctions.cleanupAndExit("Could not find the Post Button, exiting the activity", "error");
            }, randomDelay);
        }
        rootNode.recycle();
    }
    private void sendPromptToOpenAIAndType() {
        OpenAIClient openAIClient = new OpenAIClient(openAPIKey);
        openAIClient.generateComment(prompt, "tweet", 3, new OpenAIClient.OpenAICallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "OpenAI response received: " + response);
                handler.post(() -> typeTweetLikeHuman(response));
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "OpenAI error: " + error);
                helperFunctions.cleanupAndExit("Failed to get response from OpenAI", "error");
            }
        });
    }
    private void typeTweetLikeHuman(String tweetText) {
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available for typing");
            helperFunctions.cleanupAndExit("No root node for typing tweet", "error");
            return;
        }
        String tweetFieldId = "com.twitter.android:id/tweet_text";
        AccessibilityNodeInfo tweetField = HelperFunctions.findNodeByResourceId(rootNode, tweetFieldId);
        if (tweetField != null && tweetField.isEditable()) {
            typeTextCharByChar(tweetField, tweetText, 0);
        } else {
            Log.e(TAG, "Tweet field not found or not editable");
            helperFunctions.cleanupAndExit("Could not find or edit tweet field", "error");
        }
        rootNode.recycle();
    }
    private void typeTextCharByChar(AccessibilityNodeInfo tweetField, String text, int index) {
        if (index > text.length()) {
            Log.d(TAG, "Finished typing tweet. Posting...");
            int randomDelay = 3000 + random.nextInt(3000);
            handler.postDelayed(this::findAndClickPostTweet, randomDelay);
            return;
        }
        String currentText = text.substring(0, index);
        android.os.Bundle args = new android.os.Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, currentText);
        tweetField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
        handler.postDelayed(() -> typeTextCharByChar(tweetField, text, index + 1), 100 + random.nextInt(150));
    }
    private void findAndClickPostTweet() {
        Log.d(TAG, "Searching for Post Tweet button...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickPostTweet", "error");
            return;
        }
        String ButtonId = "com.twitter.android:id/button_tweet";
        AccessibilityNodeInfo Button = HelperFunctions.findNodeByResourceId(rootNode, ButtonId);
        if (Button != null) {
            Log.d(TAG, "Found Post Tweet Button, attempting click...");
            boolean clickSuccess = performClick(Button);
            if (clickSuccess) {
                Log.d(TAG, "Post Tweet button clicked. Going to exit activity...");
                int randomDelay = 3000 + random.nextInt(5000);
                handler.postDelayed(()->{
                    helperFunctions.cleanupAndExit("Tweet Posted Successfully", "final");
                }, randomDelay);
            }
        } else {
            Log.d(TAG, "Post Tweet Button not found");
            int randomDelay = 2000 + random.nextInt(3000);
            handler.postDelayed(() -> {
                helperFunctions.cleanupAndExit("Could not find the Post Tweet Button, exiting the activity", "error");
            }, randomDelay);
        }
        rootNode.recycle();
    }


// Helper Functions of activity
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
