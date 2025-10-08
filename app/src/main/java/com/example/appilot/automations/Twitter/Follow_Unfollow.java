package com.example.appilot.automations.Twitter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;
import com.example.appilot.services.MyAccessibilityService;
import com.example.appilot.utils.HelperFunctions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Follow_Unfollow {
    private static final String TAG = "TwitterAutomation";
    private static final String PREFS_NAME = "TwitterAutomationPrefs";
    private static final String PREF_DATE = "follow_unfollow_date";
    private static final String PREF_COUNT = "follow_unfollow_count";
    private final Context context;
    private final Handler handler;
    private final Random random;
    private final PopUpHandler popUpHandler;
    private final MyAccessibilityService service;
    private HelperFunctions helperFunctions;
    private String Task_id = null;
    private String job_id = null;
    private String profile;
    private String typeofAction;
    private List<String> usernames = new java.util.ArrayList<>();
    private List<Object> AccountInputs;
    private int duration;
    private int currentUserIndex = 0;
    private long startTime;
    private int MAX_COUNT;
    private int COUNT = 0;

    public Follow_Unfollow(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs, int duration, String profile, String typeOfAction, int limit_user) {
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
        this.profile = profile;
        this.typeofAction = typeOfAction;
        this.MAX_COUNT = limit_user;
    }

    public void startFollow_unfollowAutomation() {
        // Daily limit check
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String storedDate = prefs.getString(PREF_DATE, null);
        int storedCount = prefs.getInt(PREF_COUNT, 0);
        String todaysDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        if (storedDate == null || !todaysDate.equals(storedDate)) {
            // First run or new day: reset count and store today's date
            prefs.edit().putString(PREF_DATE, todaysDate).putInt(PREF_COUNT, 0).apply();
            COUNT = 0;
            Log.d(TAG, "First run or new day: Resetting count and storing today's date: " + todaysDate);
        } else {
            COUNT = storedCount;
            Log.d(TAG, "Loaded stored date: " + storedDate + ", Today's date: " + todaysDate + ", Count: " + COUNT);
            if (COUNT >= MAX_COUNT) {
                helperFunctions.cleanupAndExit("Daily follow/unfollow limit reached: " + COUNT + "/" + MAX_COUNT, "final");
                return;
            }
        }
        parseUsernames(profile);
        if (!hasCurrentUser()) {
            helperFunctions.cleanupAndExit("No usernames present. Exiting activity.", "final");
            return;
        }
        Log.d(TAG, "Starting Interaction with profiles (multi-user). First: @" + currentUsername());
        handler.postDelayed(()->{
            try{
                clickSearchIcon();
            } catch (Exception e) {
                Log.e(TAG, "Error in clickSearchIcon: " + e.getMessage());
                helperFunctions.cleanupAndExit("Error in clickSearchIcon: " + e.getMessage(), "error");
            }
        }, 1000 + random.nextInt(2000));
    }
    private void clickSearchIcon() {
        Log.d(TAG, "Starting hierarchy navigation to click target element...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in clickSearchIcon", "error");
            return;
        }
        String parentNodeId = "com.twitter.android:id/tabs";
        AccessibilityNodeInfo parentNode = HelperFunctions.findNodeByResourceId(rootNode, parentNodeId);

        if (parentNode != null) {
            Log.d(TAG, "Found parent node with ID: " + parentNodeId);
            Log.d(TAG, "Parent has " + parentNode.getChildCount() + " children");
            // Navigate to target: Parent -> Child(0) -> Child(1)
            AccessibilityNodeInfo targetElement = navigateToSearchTab(parentNode);
            if (targetElement != null) {
                Log.d(TAG, "Found target element, attempting click...");
                boolean clickSuccess = performClick(targetElement);
                if (clickSuccess) {
                    Log.d(TAG, "Search Tab clicked successfully. Waiting for profile to load...");
                    int randomDelay = 3000 + random.nextInt(3000);
                    handler.postDelayed(()->{
                        try {
                            findAndClickSearchBar();
                        } catch (Exception e) {
                            Log.e(TAG, "Error in findAndClickSearchBar: " + e.getMessage());
                            helperFunctions.cleanupAndExit("Error in findAndClickSearchBar: " + e.getMessage(), "error");
                        }
                    },randomDelay);
                }
            } else {
                Log.e(TAG, "Could not navigate to target element");
                helperFunctions.cleanupAndExit("Could not navigate to target element", "error");
            }
            parentNode.recycle();
        } else {
            Log.e(TAG, "Could not find parent node with ID: " + parentNodeId);
            helperFunctions.cleanupAndExit("Could not find parent node with ID: " + parentNodeId, "error");
        }
        rootNode.recycle();
    }
    private void findAndClickSearchBar() {
        Log.d(TAG, "Searching for Search Bar...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickSearchBar", "error");
            return;
        }
        String searchBarId = "com.twitter.android:id/query_view";
        AccessibilityNodeInfo searchBar = HelperFunctions.findNodeByResourceId(rootNode, searchBarId);
        if (searchBar != null) {
            Log.d(TAG, "Found Search Bar, attempting click...");
            boolean clickSuccess = performClick(searchBar);
            if (clickSuccess) {
                Log.d(TAG, "Search Bar clicked. Waiting to type the username...");
                int randomDelay = 3000 + random.nextInt(5000);
                handler.postDelayed(() -> {
                    try {
                        typeProfileNameInSearchBar(currentUsername());
                    } catch (Exception e) {
                        Log.e(TAG, "Error in typeProfileNameInSearchBar: " + e.getMessage());
                        helperFunctions.cleanupAndExit("Error in typeProfileNameInSearchBar: " + e.getMessage(), "error");
                    }
                }, randomDelay);
            }
        } else {
            Log.d(TAG, "Search Bar not found");
            int randomDelay = 2000 + random.nextInt(3000);
            handler.postDelayed(() -> {
                helperFunctions.cleanupAndExit("Could not find the Search Bar, exiting the activity", "error");
            }, randomDelay);
        }
        rootNode.recycle();
    }
    private void typeProfileNameInSearchBar(String username) {
        if (username == null || username.isEmpty()) {
            Log.e(TAG, "Profile name is empty");
            helperFunctions.cleanupAndExit("Profile name is empty", "error");
            return;
        }
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available for typing profile name");
            helperFunctions.cleanupAndExit("No root node available for typing profile name", "error");
            return;
        }
        AccessibilityNodeInfo textField = findEditableTextField(rootNode);
        if (textField == null) {
            Log.e(TAG, "Could not find search bar text field");
            rootNode.recycle();
            helperFunctions.cleanupAndExit("Could not find search bar text field", "error");
            return;
        }
        Log.d(TAG, "TextField class: " + textField.getClassName());
        Log.d(TAG, "TextField editable: " + textField.isEditable());
        Log.d(TAG, "TextField focusable: " + textField.isFocusable());
        Log.d(TAG, "TextField current text: " + textField.getText());
        textField.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
        handler.postDelayed(() -> {
            try {
                Bundle clearBundle = new Bundle();
                clearBundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "");
                boolean clearSuccess = textField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, clearBundle);
                Log.d(TAG, "Clear text success: " + clearSuccess);
                handler.postDelayed(() -> {
                    try {
                        typeCharacterByCharacter(username, 0, new StringBuilder());
                    } catch (Exception e) {
                        Log.e(TAG, "Error while typing profile name: " + e.getMessage());
                        helperFunctions.cleanupAndExit("Error while typing profile name: " + e.getMessage(), "error");
                    }
                }, 1000 + random.nextInt(1000));
            } catch (Exception e) {
                Log.e(TAG, "Error while typing profile name: " + e.getMessage());
                helperFunctions.cleanupAndExit("Error while typing profile name: " + e.getMessage(), "error");
            }
        }, 300);
    }
//    private void selectProfile() {
//        Log.d(TAG, "Starting hierarchy navigation to click target element for profile...");
//        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
//        if (rootNode == null) {
//            Log.e(TAG, "No root node available");
//            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in selectProfile", "error");
//            return;
//        }
//        String parentNodeId = "com.twitter.android:id/search_suggestions_list";
//        AccessibilityNodeInfo parentNode = HelperFunctions.findNodeByResourceId(rootNode, parentNodeId);
//
//        if (parentNode != null) {
//            Log.d(TAG, "Found parent node with ID: " + parentNodeId);
//            Log.d(TAG, "Parent has " + parentNode.getChildCount() + " children");
//            // Navigate to target: Parent -> Child(0)
//            AccessibilityNodeInfo targetElement = navigateToProfile(parentNode, 0);
//            if (targetElement != null) {
//                Log.d(TAG, "Found target element, attempting click...");
//                boolean clickSuccess = performClick(targetElement);
//                if (clickSuccess) {
//                    Log.d(TAG, "Profile clicked successfully. Waiting for profile to load...");
//                    Log.d(TAG, "Action to perform is " + typeofAction + " the users.");
//                    if (typeofAction.equals("Follow")) {
//                        int randomDelay = 5000 + random.nextInt(5000);
//                        handler.postDelayed(this::findAndClickFollowButton,randomDelay);
//                    } else if (typeofAction.equals("Unfollow")) {
//                        handler.postDelayed(this::findAndClickFollowing,5000 + random.nextInt(5000));
//                    }
//                }
//            } else {
//                Log.e(TAG, "Could not navigate to target element");
//                helperFunctions.cleanupAndExit("Could not navigate to target element", "error");
//            }
//            parentNode.recycle();
//        } else {
//            Log.e(TAG, "Could not find parent node with ID: " + parentNodeId);
//            helperFunctions.cleanupAndExit("Could not find parent node with ID: " + parentNodeId, "error");
//        }
//        rootNode.recycle();
//    }
    private void selectProfile() {
        Log.d(TAG, "Starting hierarchy navigation to click target element for profile...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in selectProfile", "error");
            return;
        }
        String parentNodeId = "com.twitter.android:id/search_suggestions_list";
        AccessibilityNodeInfo parentNode = HelperFunctions.findNodeByResourceId(rootNode, parentNodeId);

        if (parentNode != null) {
            Log.d(TAG, "Found parent node with ID: " + parentNodeId);
            Log.d(TAG, "Parent has " + parentNode.getChildCount() + " children");
            // Navigate to target: Parent -> Child(0)
            AccessibilityNodeInfo targetElement = navigateToProfile(parentNode, 0);
            if (targetElement != null) {
                Log.d(TAG, "Found target element, matching Username...");
                AccessibilityNodeInfo screenNameNode = HelperFunctions.findNodeByResourceId(targetElement, "com.twitter.android:id/screenname_item");
                String foundScreenName = (screenNameNode != null && screenNameNode.getText() != null) ? screenNameNode.getText().toString().trim() : "";
                Log.d(TAG, "Found screen name: " + foundScreenName + ", looking for: " + currentUsername());
                if (foundScreenName.equalsIgnoreCase(currentUsername())) {
                    Log.d(TAG, "Screen name matches, attempting click...");
                    boolean clickSuccess = performClick(targetElement);
                    targetElement.recycle();
                    if (clickSuccess) {
                        Log.d(TAG, "Profile clicked successfully. Waiting for profile to load...");
                        Log.d(TAG, "Action to perform is " + typeofAction + " the users.");
                        if (typeofAction.equals("Follow")) {
                            int randomDelay = 5000 + random.nextInt(5000);
                            handler.postDelayed(this::findAndClickFollowButton,randomDelay);
                        } else if (typeofAction.equals("Unfollow")) {
                            handler.postDelayed(this::findAndClickFollowing,5000 + random.nextInt(5000));
                        }
                    }
                } else {
                    Log.e(TAG, "Screen name does not match. Expected: " + currentUsername() + ", Found: " + foundScreenName);
                    targetElement.recycle();
                    handler.postDelayed(()->{
                        launchProfileByIntent(currentUsername());
                    },2000 + random.nextInt(3000));
                }
            } else {
                Log.e(TAG, "Could not find the specific profile in search results");
                handler.postDelayed(()->{
                    launchProfileByIntent(currentUsername());
                },2000 + random.nextInt(3000));
            }
            parentNode.recycle();
        } else {
            Log.e(TAG, "Could not find parent node with ID: " + parentNodeId);
            helperFunctions.cleanupAndExit("Could not find parent node with ID: " + parentNodeId, "error");
        }
        rootNode.recycle();
    }
    private void launchProfileByIntent(String username) {
        Log.d(TAG, "Attempting to launch profile by intent for username: " + username);
        if (username == null || username.isEmpty()) {
            Log.e(TAG, "Username is null or empty. Cannot launch intent.");
            return;
        }
        String cleanUsername = username.startsWith("@") ? username.substring(1) : username;
        String twitterUrl = "twitter://user?screen_name=" + cleanUsername;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(twitterUrl));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            Log.d(TAG, "Trying to open Twitter app with URL: " + twitterUrl);
            context.startActivity(intent);
            Log.d(TAG, "Twitter app intent launched successfully.");
        } catch (android.content.ActivityNotFoundException e) {
            Log.w(TAG, "Twitter app not found. Falling back to browser.");
            String webUrl = "https://twitter.com/" + username;
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(browserIntent);
                Log.d(TAG, "Browser intent launched successfully with URL: " + webUrl);
                int delay = 5000 + random.nextInt(5000);
                Log.d(TAG, "Scheduling findAndClickFollowButton after " + delay + " ms");
                handler.postDelayed(this::findAndClickFollowButton, delay);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to launch browser intent: " + ex.getMessage());
                Log.d(TAG, "Moving to next username if available.");
                handler.postDelayed(this::switchToNextUsername, 2000+ random.nextInt(3000));
            }
        }
    }
    private void findAndClickFollowButton() {
        Log.d(TAG, "Searching for Follow button...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickFollowButton", "error");
            return;
        }
        String searchFollowButton = "com.twitter.android:id/button_bar_follow";
        AccessibilityNodeInfo searchFollow = HelperFunctions.findNodeByResourceId(rootNode, searchFollowButton);
        if (searchFollow != null) {
            Log.d(TAG, "Found Follow Button, attempting click...");
            boolean clickSuccess = performClick(searchFollow);
            if (clickSuccess) {
                Log.d(TAG, "Follow Button clicked...");
                incrementAndStoreCount();
//                int randomDelay = 2000 + random.nextInt(5000);
//                handler.postDelayed(()->{
//                    try {
//                        helperFunctions.performScroll(0.7f, 0.3f);
//                        handler.postDelayed(()->{
//                            Log.d(TAG, "Clicked Follow button, Moving to next user...");
//                            switchToNextUsername();
//                        },randomDelay);
//
//                    } catch (Exception e) {
//                        Log.e(TAG, "Error in findTweetNode1: " + e.getMessage());
//                        helperFunctions.cleanupAndExit("Error in findTweetNode1: " + e.getMessage(), "error");
//                    }
//                },randomDelay);
            }
        } else {
            Log.d(TAG, "Follow button not found");
            int randomDelay = 2000 + random.nextInt(3000);
            handler.postDelayed(()->{
                try {
                    helperFunctions.performScroll(0.7f, 0.3f);
                    handler.postDelayed(()->{
                        try{
                            switchToNextUsername();
                        } catch (Exception e) {
                            Log.e(TAG, "Error in findTweetNode2.2: " + e.getMessage());
                            helperFunctions.cleanupAndExit("Error in findTweetNode1.2: " + e.getMessage(), "error");
                        }
                    },randomDelay);
                } catch (Exception e) {
                    Log.e(TAG, "Error in findTweetNode2: " + e.getMessage());
                    helperFunctions.cleanupAndExit("Error in findTweetNode2: " + e.getMessage(), "error");
                }
            },randomDelay);
        }
        rootNode.recycle();
    }
    private void findAndClickFollowing() {
        Log.d(TAG, "Searching for Following Button...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickFollowing", "error");
            return;
        }
        String searchBarId = "com.twitter.android:id/button_bar_following";
        AccessibilityNodeInfo searchBar = HelperFunctions.findNodeByResourceId(rootNode, searchBarId);
        if (searchBar != null) {
            Log.d(TAG, "Found Following Button, attempting click...");
            boolean clickSuccess = performClick(searchBar);
            if (clickSuccess) {
                Log.d(TAG, "Following Button clicked. Waiting for popup to appear...");
                int randomDelay = 2000 + random.nextInt(3000);
                handler.postDelayed(this::findAndClickUnfollow, randomDelay);
            }
        } else {
            Log.d(TAG, "Following button not found, moving to next profile...");
            int randomDelay = 2000 + random.nextInt(3000);
            handler.postDelayed(this::switchToNextUsername, randomDelay);
        }
        rootNode.recycle();
    }
    private void findAndClickUnfollow() {
        Log.d(TAG, "Searching for Unfollow Button in popup...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickUnfollow", "error");
            return;
        }
        String searchBarId = "android:id/button1";
        AccessibilityNodeInfo searchBar = HelperFunctions.findNodeByResourceId(rootNode, searchBarId);
        if (searchBar != null) {
            Log.d(TAG, "Found Unfollow Button, attempting click...");
            boolean clickSuccess = performClick(searchBar);
            if (clickSuccess) {
                Log.d(TAG, "Unfollow Button clicked. Switcing to next profile...");
                incrementAndStoreCount();
//                int randomDelay = 3000 + random.nextInt(5000);
//                handler.postDelayed(this::switchToNextUsername, randomDelay);
            }
        } else {
            Log.d(TAG, "Unfollow button not found");
            int randomDelay = 2000 + random.nextInt(3000);
            handler.postDelayed(this::switchToNextUsername, randomDelay);
        }
        rootNode.recycle();
    }

    //Helper Methods
    private void typeCharacterByCharacter(String text, int charIndex, StringBuilder typedSoFar) {
        if (charIndex >= text.length()) {
            Log.d(TAG, "Finished typing profile name");
            handler.postDelayed(this::selectProfile, 5000 + random.nextInt(3000));
            return;
        }
        String currentChar = String.valueOf(text.charAt(charIndex));
        typedSoFar.append(currentChar);

        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available during typing");
            helperFunctions.cleanupAndExit("No root node available during typing", "error");
            return;
        }
        AccessibilityNodeInfo textField = findEditableTextField(rootNode);
        if (textField == null) {
            Log.e(TAG, "Could not find text input field during typing");
            rootNode.recycle();
            helperFunctions.cleanupAndExit("Could not find text input field during typing", "error");
            return;
        }
        Log.d(TAG, "Typing char: " + currentChar + " into field: " + textField.getClassName());
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, typedSoFar.toString());
        boolean setTextSuccess = textField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        Log.d(TAG, "Set text success: " + setTextSuccess);
        if (!setTextSuccess) {
            // Fallback: Try paste if available
            if (textField.isEditable() && textField.isFocusable() && textField.isEnabled() && textField.isVisibleToUser()) {
                Log.d(TAG, "Trying fallback paste method");
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("label", typedSoFar.toString());
                clipboard.setPrimaryClip(clip);
                boolean pasteSuccess = textField.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                Log.d(TAG, "Paste action success: " + pasteSuccess);
            }
        }
        textField.recycle();
        rootNode.recycle();

        int delay = 60 + random.nextInt(100); // Human-like delay
        handler.postDelayed(() -> {
            typeCharacterByCharacter(text, charIndex + 1, typedSoFar);
        }, delay);
    }

    private AccessibilityNodeInfo findEditableTextField(AccessibilityNodeInfo root) {
        if (root == null) return null;
        if (root.isEditable() && root.isFocusable()) return root;
        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = root.getChild(i);
            if (child != null) {
                try {
                    AccessibilityNodeInfo result = findEditableTextField(child);
                    if (result != null) return result;
                } finally {
                    child.recycle();
                }
            }
        }
        return null;
    }
    private AccessibilityNodeInfo navigateToSearchTab(AccessibilityNodeInfo parent) {
        try {
            Log.d(TAG, "Navigating: Parent -> Child(0) -> Child(1)");

            if (parent == null) {
                Log.e(TAG, "Parent node is null");
                return null;
            }
            // Log parent details for debugging
            Log.d(TAG, "Parent class: " + parent.getClassName());
            Log.d(TAG, "Parent child count: " + parent.getChildCount());

            // Step 1: Get first child (0)
            if (parent.getChildCount() < 1) {
                Log.e(TAG, "Parent has no children");
                return null;
            }
            AccessibilityNodeInfo Child_0 = parent.getChild(0);
            if (Child_0 == null) {
                Log.e(TAG, "Could not get child(0)");
                return null;
            }
            try {
                // Log target element details
                Log.d(TAG, "Found target element:");
                Log.d(TAG, "Class: " + Child_0.getClassName());
                Log.d(TAG, "Text: " + Child_0.getText());
                Log.d(TAG, "Clickable: " + Child_0.isClickable());

                // Step 1: Get first child (0)
                if (Child_0.getChildCount() < 2) {
                    Log.e(TAG, "Child_0 has no children");
                    return null;
                }
                AccessibilityNodeInfo targetElement = Child_0.getChild(1);
                if (targetElement == null) {
                    Log.e(TAG, "Could not get child(1) of Child_0");
                    return null;
                }
                // Log target element details
                Log.d(TAG, "Found target element:");
                Log.d(TAG, "Class: " + targetElement.getClassName());
                Log.d(TAG, "Text: " + targetElement.getText());
                Log.d(TAG, "Clickable: " + targetElement.isClickable());

                Rect bounds = new Rect();
                targetElement.getBoundsInScreen(bounds);
                Log.d(TAG, "Bounds: " + bounds);
                return targetElement;
            } finally {
                Child_0.recycle();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to target: " + e.getMessage());
            return null;
        }
    }
    private AccessibilityNodeInfo navigateToProfile(AccessibilityNodeInfo parent, int childIndex) {
        try {
            Log.d(TAG, "Navigating: Parent -> Child(0)");

            if (parent == null) {
                Log.e(TAG, "Parent node is null");
                return null;
            }
            // Log parent details for debugging
            Log.d(TAG, "Parent class: " + parent.getClassName());
            Log.d(TAG, "Parent child count: " + parent.getChildCount());

            // Step 1: Get first child (0)
            if (parent.getChildCount() < 1) {
                Log.e(TAG, "Parent has no children");
                return null;
            }
            AccessibilityNodeInfo targetElement = parent.getChild(childIndex);
            if (targetElement == null) {
                Log.e(TAG, "Could not get child(0)");
                return null;
            }
            // Log target element details
            Log.d(TAG, "Found target element:");
            Log.d(TAG, "Class: " + targetElement.getClassName());
            Log.d(TAG, "Text: " + targetElement.getText());
            Log.d(TAG, "Clickable: " + targetElement.isClickable());

            Rect bounds = new Rect();
            targetElement.getBoundsInScreen(bounds);
            Log.d(TAG, "Bounds: " + bounds);
            return targetElement;
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to target: " + e.getMessage());
            return null;
        }
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
    private void switchToNextUsername() {
        Log.d(TAG,"Duration : " + duration + " minutes");
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000 / 60;
        Log.d(TAG, "Elapsed Time: " + elapsedTime + " minutes");
        if (elapsedTime >= duration) {
            Log.d(TAG, "Reached duration. Exiting...");
            handler.postDelayed(() -> helperFunctions.cleanupAndExit("Duration Completed. Exiting activity.", "final"), 1000 + random.nextInt(3000));
            return;
        }
        currentUserIndex++;
        if (!hasCurrentUser()) {
            Log.d(TAG, "No more usernames. Exiting activity.");
            handler.postDelayed(() ->
                            helperFunctions.cleanupAndExit("No more usernames in the list. Exiting activity.", "final"),
                    600
            );
            return;
        }
        Log.d(TAG, "Next username: @" + currentUsername() + " — navigating back and searching again.");
        // Go back once (or twice if needed). Keep it simple with one back, then reopen Search.
        service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK);
        // Small delay, then open Search tab again for next user
        handler.postDelayed(()->{
            try {
                findAndClickSearchBar();
            } catch (Exception e) {
                Log.e(TAG, "Error in findAndClickSearchBar: " + e.getMessage());
                helperFunctions.cleanupAndExit("Error in findAndClickSearchBar: " + e.getMessage(), "error");
            }
        },2000 + random.nextInt(3000));
    }

    private void parseUsernames(String profileCsv) {
        if (profileCsv == null) return;
        for (String raw : profileCsv.split("[,/]")) {
            if (raw == null) continue;
            String u = raw.trim();
            //if (u.startsWith("@")) u = u.substring(1);
            if (!u.isEmpty()) usernames.add(u);
        }
        Log.d(TAG, "Parsed usernames: " + usernames);
    }

    private boolean hasCurrentUser() {
        return currentUserIndex >= 0 && currentUserIndex < usernames.size();
    }

    private String currentUsername() {
        return hasCurrentUser() ? usernames.get(currentUserIndex) : null;
    }
    // Call this after each follow/unfollow action
    private void incrementAndStoreCount() {
        COUNT++;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(PREF_COUNT, COUNT).apply();
        Log.d(TAG, "Incremented and stored count: " + COUNT);
        if (COUNT >= MAX_COUNT) {
            handler.postDelayed(()->{
                helperFunctions.cleanupAndExit("Daily follow/unfollow limit reached: " + COUNT + "/" + MAX_COUNT, "final");
            }, 3000+ random.nextInt(5000));
        }
        else {
            int randomDelay = 3000 + random.nextInt(5000);
            handler.postDelayed(this::switchToNextUsername, randomDelay);
        }
    }
}
