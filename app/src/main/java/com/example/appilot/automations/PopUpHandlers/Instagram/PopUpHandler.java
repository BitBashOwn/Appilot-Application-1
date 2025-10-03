package com.example.appilot.automations.PopUpHandlers.Instagram;

import android.graphics.Rect;
import android.os.Handler;

import com.example.appilot.automations.Interfaces.Action;
import com.example.appilot.services.MyAccessibilityService;
import com.example.appilot.utils.HelperFunctions;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.appilot.utils.HelperFunctions;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class PopUpHandler {
    private static final String TAG = "PopUpHandler";
    private static final int MAX_RECURSION_DEPTH = 10;
    private final int MAX_POPUP_ATTEMPTS = 3;
    private final MyAccessibilityService service;
    private final Handler handler;
    private final Random random;
    private final HelperFunctions helperFunctions;
//    private static final int MAX_RECURSION_DEPTH = 30;
    private final String blockActionDialogTitle = "Try again later";
    private final String AllowContactsAccessPopUpHandler = "Allow Instagram to access your contacts?";
    private final String blockActionDialogDesc = "We limit how often you can do certain things on Instagram, such as following people, to protect our community. Let us know if you think that we've made a mistake.";
    private final String blockActionDialogDesc2 = "We limit how often you can do certain things on Instagram to protect our community. Tell us if you think that we've made a mistake.";
    private final String RequestPendingPopUpTitle = "Your request is pending";
    private final String RequestPendingPopUpDesc = "Some accounts prefer to manually review followers even when they're public. Let us know if you think that we've made a mistake.";
    private final String dialogId = "com.instagram.android:id/dialog_container";

    public PopUpHandler(MyAccessibilityService service, Handler handler, Random random, HelperFunctions helperfunctions){
        this.service = service;
        this.handler = handler;
        this.random = random;
        this.helperFunctions = helperfunctions;
    }


//    public boolean handleOtherPopups(Action callback) {
//        Log.i(TAG, "Entered generic popup handler");
//        AccessibilityNodeInfo rootNode = null;
//
//        try {
//            // Step 1: Get the root node
//            rootNode = helperFunctions.getRootInActiveWindow();
//            if (rootNode == null) {
//                Log.e(TAG, "Root node not found");
//                return false;
//            }
//
//            // Step 2: Find dialog (try multiple possible dialog containers)
//            AccessibilityNodeInfo dialog = HelperFunctions.findNodeByResourceId(rootNode, dialogId);
//            if (dialog == null) {
//                dialog = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/bottom_sheet_container_view");
//            }
//
//            if (dialog == null) {
//                Log.d(TAG, "No dialog found");
//                return false;
//            }
//
//            // Step 3: Try to find buttons by their possible resource IDs
//            String[] buttonIds = {
//                    "com.instagram.android:id/primary_button_row",
//                    "com.instagram.android:id/igds_alert_dialog_cancel_button",
//                    "com.instagram.android:id/negative_button_row",
//                    "com.instagram.android:id/igds_alert_dialog_dismiss_button",
//                    "com.instagram.android:id/igds_alert_dialog_primary_button"
//            };
//
//            AccessibilityNodeInfo button = null;
//
//            // First try to find buttons by resource ID
//            for (String buttonId : buttonIds) {
//                button = HelperFunctions.findNodeByResourceId(dialog, buttonId);
//
//                // Ensure the button is not null before proceeding
//                if (button != null) {
//                    // Check if the button has children
//                    if (button.getChildCount() > 0) {
//                        AccessibilityNodeInfo child = button.getChild(0);
//
//                        // Ensure the child is not null and its text is not null
//                        if (child != null && child.getText() != null && child.getText().toString().equals("Review Followers")) {
//                            // Skip this button if it matches "Review Followers"
//                            continue;
//                        }
//                    }
//
//                    // If we reach here, we've found a valid button
//                    break;
//                }
//            }
//
//            // If no button found by ID, try to find by content description
//            if (button == null) {
//                button = helperFunctions.findButtonByContentDesc(dialog, "Follow");
//            }
//
//            // If still no button found, try to get any clickable child
//            if (button == null) {
//                for (int i = 0; i < dialog.getChildCount(); i++) {
//                    AccessibilityNodeInfo child = dialog.getChild(i);
//                    if (child != null && child.isClickable()) {
//                        button = child;
//                        break;
//                    }
//                    if (child != null) {
//                        child.recycle();
//                    }
//                }
//            }
//
//            if (button == null) {
//                Log.e(TAG, "No clickable button found in dialog");
//                return false;
//            }
//
//            // Step 4: Perform the click action on the found button
//            if (button.isClickable() && button.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
//                Log.i(TAG, "Clicked on button through Accessibility");
//                try {
//                    Thread.sleep(800 + random.nextInt(200));
//                } catch (InterruptedException e) {
//                    Log.e(TAG, "Typing interrupted", e);
//                }
//                return false; // Allow automation to continue
//            } else {
//                // Fallback to gesture-based click
//                Rect bounds = new Rect();
//                button.getBoundsInScreen(bounds);
//
//                String clickType = determineClickType();
//
//                helperFunctions.clickOnBounds(bounds, () -> {
//                    Log.i(TAG, "Clicked on button via gestures");
//                    callback.execute(); // Execute callback after click
//                }, clickType, 800, 1600, helperFunctions);
//
//                return true;
//            }
//
//        } catch (Exception e) {
//            Log.e(TAG, "Unexpected error in handleAllPopups: " + e.getMessage(), e);
//            return false;
//        } finally {
//            // Safely recycle the root node
//            if (rootNode != null) {
//                rootNode.recycle();
//            }
//        }
//    }


    public boolean handleOtherPopups(Action callback, String[] prioritizedButtonIds) {
        Log.i(TAG, "Entered generic popup handler");
        AccessibilityNodeInfo rootNode = null;

        try {
            // Step 1: Get the root node
            rootNode = helperFunctions.getRootInActiveWindow();
            if (rootNode == null) {
                Log.e(TAG, "Root node not found");
                return false;
            }

            // Step 2: Find dialog (try multiple possible dialog containers)
            AccessibilityNodeInfo dialog = HelperFunctions.findNodeByResourceId(rootNode, dialogId);
            if (dialog == null) {
                dialog = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/bottom_sheet_container_view");
            }

            if (dialog == null) {
                Log.d(TAG, "No dialog found");
                return false;
            }

            // Step 3: Try to find buttons - first check prioritized IDs, then fall back to defaults
            AccessibilityNodeInfo button = null;

            // First try the prioritized button IDs if provided
            if (prioritizedButtonIds != null && prioritizedButtonIds.length > 0) {
                for (String buttonId : prioritizedButtonIds) {
                    button = HelperFunctions.findNodeByResourceId(dialog, buttonId);

                    // Check if we found a valid button
                    if (button != null) {
                        // Verify it's not the "Review Followers" button to skip
                        if (button.getChildCount() > 0) {
                            AccessibilityNodeInfo child = button.getChild(0);
                            if (child != null && child.getText() != null &&
                                    child.getText().toString().equals("Review Followers")) {
                                button = null; // Reset button to null to continue searching
                                continue;
                            }
                        }

                        // If we reach here, we've found a valid button from prioritized list
                        Log.i(TAG, "Found prioritized button with ID: " + buttonId);
                        break;
                    }
                }
            }

            // If no prioritized button found, try the default button IDs
            if (button == null) {
                String[] defaultButtonIds = {
                        "com.instagram.android:id/primary_button_row",
                        "com.instagram.android:id/igds_alert_dialog_cancel_button",
                        "com.instagram.android:id/negative_button_row",
                        "com.instagram.android:id/igds_alert_dialog_dismiss_button",
                        "com.instagram.android:id/igds_alert_dialog_primary_button"
                };

                for (String buttonId : defaultButtonIds) {
                    button = HelperFunctions.findNodeByResourceId(dialog, buttonId);

                    // Ensure the button is not null before proceeding
                    if (button != null) {
                        // Check if the button has children
                        if (button.getChildCount() > 0) {
                            AccessibilityNodeInfo child = button.getChild(0);

                            // Ensure the child is not null and its text is not null
                            if (child != null && child.getText() != null &&
                                    child.getText().toString().equals("Review Followers")) {
                                // Skip this button if it matches "Review Followers"
                                button = null; // Reset to continue search
                                continue;
                            }
                        }

                        // If we reach here, we've found a valid default button
                        Log.i(TAG, "Found default button with ID: " + buttonId);
                        break;
                    }
                }
            }

            // If no button found by ID, try to find by content description
            if (button == null) {
                button = helperFunctions.findButtonByContentDesc(dialog, "Follow");
            }

            if (button == null) {
                String[] priorityTextList = {"Cancel", "Don't Allow Access", "Ok"};

                for (String buttonText : priorityTextList) {
                    button = helperFunctions.findNodeByClassAndText(dialog, "android.widget.Button", buttonText);

                    if (button != null) {
                        Log.i(TAG, "Found button with text: " + buttonText);
                        break;
                    }
                }
            }

            // If still no button found, try to get any clickable child
            if (button == null) {
                for (int i = 0; i < dialog.getChildCount(); i++) {
                    AccessibilityNodeInfo child = dialog.getChild(i);
                    if (child != null && child.isClickable()) {
                        button = child;
                        break;
                    }
                    if (child != null) {
                        child.recycle();
                    }
                }
            }

            if (button == null) {
                Log.e(TAG, "No clickable button found in dialog");
                return false;
            }

            // Step 4: Perform the click action on the found button
            if (button.isClickable() && button.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                Log.i(TAG, "Clicked on button through Accessibility");
                try {
                    Thread.sleep(800 + random.nextInt(200));
                } catch (InterruptedException e) {
                    Log.e(TAG, "Typing interrupted", e);
                }
                return false; // Allow automation to continue
            } else {
                // Fallback to gesture-based click
                Rect bounds = new Rect();
                button.getBoundsInScreen(bounds);

                String clickType = determineClickType();

                helperFunctions.clickOnBounds(bounds, () -> {
                    Log.i(TAG, "Clicked on button via gestures");
                    callback.execute(); // Execute callback after click
                }, clickType, 800, 1600, helperFunctions);


                return true;
            }

        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in handleAllPopups: " + e.getMessage(), e);
            return false;
        } finally {
            // Safely recycle the root node
            if (rootNode != null) {
                rootNode.recycle();
            }
        }
    }

    private String determineClickType() {
        int check = random.nextInt(100);
        if (check < 30) {
            return "Start";
        } else if (check < 65) {
            return "Center";
        } else {
            return "Last";
        }
    }


//    public boolean allowContactsAccessAlertHandler(Action Callback) {
//        Log.e(TAG, "Entered allowContactsAccessAlertHandler");
//        try {
//            // Step 1: Get the root node
//            AccessibilityNodeInfo rootNode = helperFunctions.getRootInActiveWindow();
//            if (rootNode == null) {
//                Log.e(TAG, "Root node not found");
//                return false;
//            }
//
//            // Step 2: Find the dialog
//            AccessibilityNodeInfo dialog = HelperFunctions.findNodeByResourceId(rootNode, dialogId);
//            if (dialog == null) {
//                Log.d(TAG, "No dialog found");
//                return false;
//            }
//
//            // Step 3: Find the title of the dialog
//            AccessibilityNodeInfo title = null;
//            AccessibilityNodeInfo dontAllowButton = null;
//
//            try {
//                title = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/igds_alert_dialog_headline");
//                if (title == null) {
//                    title = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/igds_headline_headline");
//                }
//
//                if (title == null) {
//                    Log.d(TAG, "Dialog title not found");
//                    return false;
//                }
//
//                // Step 4: Check if the title matches the expected text
//                CharSequence titleText = title.getText();
//                if (titleText == null || !titleText.toString().equals(AllowContactsAccessPopUpHandler)) {
//                    Log.d(TAG, "Dialog title does not match expected text");
//                    return false;
//                }
//
//                // Step 5: Find the "Don't Allow" button
//                dontAllowButton = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/igds_alert_dialog_cancel_button");
//                if (dontAllowButton == null) {
//                    dontAllowButton = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/negative_button_row");
//                }
//
//                if (dontAllowButton == null) {
//                    Log.e(TAG, "Don't Allow button not found");
//                    return false;
//                }
//
//                // Step 6: Perform the click action on the "Don't Allow" button
//                if (dontAllowButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
//                    Log.i(TAG, "Clicked on Don't Allow Access button");
//                    return false;
//                } else {
//                    Log.e(TAG, "Failed to perform click action on Don't Allow button");
//
//                    Rect bounds = new Rect();
//                    dontAllowButton.getBoundsInScreen(bounds);
//
//                    helperFunctions.clickOnBounds(bounds, () -> {
//                        Log.i(TAG, "Clicked on Don't Allow Access button via gestures");
//                        Callback.execute();
//                    }, "Center", 800, 1600, helperFunctions);
//
//                    // Wait for the gesture click to complete
//                    return true;
//                }
//
//            } finally {
//                // Safely recycle nodes to avoid memory leaks
//                if (title != null) helperFunctions.safelyRecycleNode(title);
//                if (dontAllowButton != null) helperFunctions.safelyRecycleNode(dontAllowButton);
//            }
//
//        } catch (Exception e) {
//            Log.e(TAG, "Unexpected error in allowContactsAccessAlertHandler: " + e.getMessage());
//            return false;
//        }
//    }
//
//
//    public boolean RequestPendingPopUpHandler(Action Callback) {
//        Log.i(TAG, "Entered to check request pending pop-up");
//        AccessibilityNodeInfo rootNode = null;
//        AccessibilityNodeInfo title = null;
//        AccessibilityNodeInfo desc = null;
//        AccessibilityNodeInfo dontAllowButton = null;
//
//        try {
//            // Step 1: Get the root node
//            rootNode = helperFunctions.getRootInActiveWindow();
//            if (rootNode == null) {
//                Log.e(TAG, "Root node not found");
//                return false;
//            }
//
//            // Step 2: Find the dialog
//            AccessibilityNodeInfo dialog = HelperFunctions.findNodeByResourceId(rootNode, dialogId);
//            if (dialog == null) {
//                Log.d(TAG, "No dialog found");
//                return false;
//            }
//
//            // Step 3: Find the title and description of the dialog
//            try {
//                title = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/igds_headline_headline");
//                desc = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/igds_headline_body");
//
//                if (title == null || desc == null) {
//                    Log.d(TAG, "Dialog title or description not found");
//                    return false;
//                }
//
//                // Step 4: Check if the title and description match the expected text
//                CharSequence titleText = title.getText();
//                CharSequence descText = desc.getText();
//
//                if (titleText == null || descText == null ||
//                        !RequestPendingPopUpTitle.equals(titleText.toString()) ||
//                        !RequestPendingPopUpDesc.equals(descText.toString())) {
//                    Log.d(TAG, "Dialog title or description does not match expected text");
//                    return false;
//                }
//
//                // Step 5: Find the "Don't Allow" button
//                dontAllowButton = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/primary_button_row");
//                if (dontAllowButton == null) {
//                    Log.e(TAG, "Don't Allow button not found");
//                    return false;
//                }
//
//                // Step 6: Perform the click action on the "Don't Allow" button
//                if (dontAllowButton.isClickable() && dontAllowButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
//                    Log.i(TAG, "Clicked on Don't Allow Access button");
//                    return false;
//                } else {
//                    Log.e(TAG, "Failed to perform click action on Don't Allow button");
//                    Rect bounds = new Rect();
//                    dontAllowButton.getBoundsInScreen(bounds);
//
//                    // Fallback to gesture-based click
//                    helperFunctions.clickOnBounds(bounds, () -> {
//                        Log.i(TAG, "Clicked on Don't Allow Access button via gestures");
//                        Callback.execute();
//                    }, "Center", 800, 1600, helperFunctions);
//                    return true;
//                }
//
//            } finally {
//                // Safely recycle nodes
//                helperFunctions.safelyRecycleNode(title);
//                helperFunctions.safelyRecycleNode(desc);
//                helperFunctions.safelyRecycleNode(dontAllowButton);
//            }
//
//        } catch (Exception e) {
//            Log.e(TAG, "Unexpected error in RequestPendingPopUpHandler: " + e.getMessage());
//            return false;
//
//        } finally {
//            if (rootNode != null) {
//                rootNode.recycle();
//            }
//        }
//    }
//
//
//    public boolean handleReviewProfileBeforeFollowingPopup(Action callback) {
//        Log.i(TAG, "Entered to check review profile before following popup");
//        AccessibilityNodeInfo rootNode = null;
//        AccessibilityNodeInfo dialog = null;
//        AccessibilityNodeInfo button = null;
//
//        try {
//            // Step 1: Get the root node
//            rootNode = helperFunctions.getRootInActiveWindow();
//            if (rootNode == null) {
//                Log.e(TAG, "Root node not found");
//                return false;
//            }
//
//            // Step 2: Find the dialog
//            dialog = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/bottom_sheet_container_view");
//            if (dialog == null) {
//                Log.d(TAG, "No profile review container found");
//                return false;
//            }
//
//            // Step 3: Find the "Follow" button
//            try {
//                button = helperFunctions.findButtonByContentDesc(dialog, "Follow");
//                if (button == null) {
//                    Log.d(TAG, "Follow button not found in dialog");
//                    return false;
//                }
//
//                // Step 4: Perform the click action on the "Follow" button
//                if (button.isClickable() && button.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
//                    Log.i(TAG, "Clicked on Follow button in review profile dialog");
//                    return false; // Allow automation to continue
//                } else {
//                    Log.e(TAG, "Failed to perform click action on Follow button");
//                    Rect bounds = new Rect();
//                    button.getBoundsInScreen(bounds);
//
//                    // Fallback to gesture-based click
//                    helperFunctions.clickOnBounds(bounds, () -> {
//                        Log.i(TAG, "Clicked on Follow button via gestures");
//                        callback.execute(); // Execute callback after click
//                    }, "Center", 800, 1600, helperFunctions);
//                    return true; // Return false to allow automation to continue
//                }
//
//            } finally {
//                // Safely recycle nodes
//                helperFunctions.safelyRecycleNode(dialog);
//                helperFunctions.safelyRecycleNode(button);
//            }
//
//        } catch (Exception e) {
//            Log.e(TAG, "Unexpected error in handleReviewProfileBeforeFollowingPopup: " + e.getMessage(), e);
//            return false; // Allow automation to retry
//
//        } finally {
//            // Safely recycle the root node
//            if (rootNode != null) {
//                rootNode.recycle();
//            }
//        }
//    }
//
//
//    public boolean handleUnfollowConfirmationPopup(Action callback) {
//        Log.i(TAG, "Entered handleUnfollowConfirmationPopup to check unfollow confirmation popup");
//        AccessibilityNodeInfo dialog = null;
//
//        try {
//            // Step 1: Find the dialog
//            dialog = helperFunctions.FindAndReturnNodeById(dialogId, 3);
//            if (dialog == null) {
//                Log.d(TAG, "No dialog found");
//                return false;
//            }
//
//            AccessibilityNodeInfo desc = null;
//            AccessibilityNodeInfo unfollowButton = null;
//
//            try {
//                // Step 2: Find the description node
//                desc = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/igds_headline_body");
//                if (desc == null || desc.getText() == null) {
//                    Log.e(TAG, "Popup description not found");
//                    return false;
//                }
//
//                String text = desc.getText().toString();
//                if (text.contains("If you change your mind,") || text.contains("You'll be removed from their followers.")) {
//                    // Step 3: Find the "Unfollow" button
//                    unfollowButton = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/primary_button_row");
//                    if (unfollowButton == null) {
//                        Log.e(TAG, "Unfollow button not found");
//                        return false;
//                    }
//
//                    // Step 4: Perform the click action on the "Unfollow" button
//                    if (unfollowButton.isClickable() && unfollowButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
//                        Log.i(TAG, "Clicked on Unfollow button through Accessibility");
//                        return false; // Allow automation to continue
//                    } else {
//                        // Fallback to gesture-based click
//                        String clickType = determineClickType(); // Use a deterministic approach
//                        Rect bounds = new Rect();
//                        unfollowButton.getBoundsInScreen(bounds);
//
//                        Log.d(TAG, "Falling back to bounds click with type: " + clickType);
//                        helperFunctions.clickOnBounds(bounds, () -> {
//                            Log.i(TAG, "Clicked on Unfollow button via gestures");
//                            callback.execute(); // Execute callback after click
//                        }, clickType, 800, 1600, helperFunctions);
//
//                        return true;
//                    }
//                } else {
//                    Log.d(TAG, "Popup description does not match expected text");
//                    return false;
//                }
//
//            } finally {
//                // Safely recycle nodes
//                helperFunctions.safelyRecycleNode(desc);
//                helperFunctions.safelyRecycleNode(unfollowButton);
//            }
//
//        } catch (Exception e) {
//            Log.e(TAG, "Unexpected error in handleUnfollowConfirmationPopup: " + e.getMessage(), e);
//            return false; // Allow automation to retry
//
//        } finally {
//            // Safely recycle the dialog node
//            if (dialog != null) {
//                dialog.recycle();
//            }
//        }
//    }
//
//    private String determineClickType() {
//        int check = random.nextInt(100);
//        if (check < 30) {
//            return "Start";
//        } else if (check < 65) {
//            return "Center";
//        } else {
//            return "Last";
//        }
//    }





    public boolean checkForActionBlocker(Action callback) {
        Log.i(TAG,"Entered checkForActionBlocker");
        return checkForActionBlockerWithRetry(0, 0, callback);
    }

    private boolean checkForActionBlockerWithRetry(int depth, int attempts, Action callback) {
        // Prevent infinite recursion
        if (depth >= MAX_RECURSION_DEPTH || attempts >= MAX_POPUP_ATTEMPTS) {
            Log.e(TAG, "Max recursion depth or attempts reached");
            return false;
        }

        try {
            // Small delay to allow for potential popup rendering
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Log.e(TAG, "Sleep interrupted", e);
        }

        AccessibilityNodeInfo rootNode = null;
        try {
            rootNode = helperFunctions.getRootInActiveWindow();
            if (rootNode == null) {
                Log.e(TAG, "Root node not found");
                return false;
            }

            AccessibilityNodeInfo dialog = HelperFunctions.findNodeByResourceId(rootNode, dialogId);
            if (dialog == null) {
                Log.d(TAG, "No dialog found");
                return false;
            }

            return handleDialogWithSafetyChecks(dialog, depth, attempts, callback);

        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in checkForActionBlockerWithRetry", e);
            return false;
        } finally {
            if (rootNode != null) {
                rootNode.recycle();
            }
        }
    }

    private boolean handleDialogWithSafetyChecks(AccessibilityNodeInfo dialog, int depth, int attempts, Action callback) {
        AccessibilityNodeInfo title = null;
        AccessibilityNodeInfo description = null;
        AccessibilityNodeInfo okButton = null;

        try {
            // Find title with multiple fallback options
            title = findTitleNode(dialog);
            // Find description with multiple fallback options
            description = findDescriptionNode(dialog);

            // Validate nodes before processing
            if (title == null || description == null) {
                Log.e(TAG, "Title or description is null");
                return false;
            }

            // Check if this is the target dialog
            if (isTargetDialog(title, description)) {
                okButton = findOkButton(dialog);
                if (okButton == null) {
                    Log.e(TAG, "OK button not found");
                    return false;
                }

                // Handle button click with synchronization
                return handleOkButtonClick(okButton, depth, attempts, callback);
            }

            return false;

        } catch (Exception e) {
            Log.e(TAG, "Error processing dialog", e);
            return false;
        } finally {
            // Safely recycle nodes
            helperFunctions.safelyRecycleNode(title);
            helperFunctions.safelyRecycleNode(description);
            helperFunctions.safelyRecycleNode(okButton);
        }
    }

//    private boolean handleOkButtonClick(AccessibilityNodeInfo okButton, int depth, int attempts, Action callback) {
//        try {
//            // Attempt accessibility click first
//            if (okButton.isClickable() && okButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
//                Log.i(TAG, "Clicked Ok button via accessibility");
//                // Recursive check for next potential popup
////                return checkForActionBlockerWithRetry(depth + 1, attempts + 1, callback);
//                callback.execute();
//                return true;
//            }
//            // Fallback to bounds clicking if accessibility click fails
//            else {
//                Rect bounds = new Rect();
//                okButton.getBoundsInScreen(bounds);
//
//                // Create a synchronized callback mechanism
////                CompletableFuture<Boolean> clickFuture = new CompletableFuture<>();
//
//                helperFunctions.clickOnBounds(bounds, () -> {
//                    // Signal successful click
//                    callback.execute();
//                }, "Center", 800, 2000, helperFunctions);
//
//                // Wait for click to complete and then check for next popup
////                boolean clickResult = clickFuture.get(5, TimeUnit.SECONDS);
////                return clickResult && checkForActionBlockerWithRetry(depth + 1, attempts + 1, callback);
//                return true;
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Error clicking OK button", e);
//            return false;
//        }
//    }


    private boolean handleOkButtonClick(AccessibilityNodeInfo okButton, int depth, int attempts, Action callback) {
        try {
            // Attempt accessibility click first
            if (okButton.isClickable() && okButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                Log.i(TAG, "Clicked Ok button via accessibility");

                // Recursive check for next potential popup
                // Only execute callback if no more popups are found
                boolean moreDialogsFound = checkForActionBlockerWithRetry(depth + 1, attempts + 1, null);

                if (!moreDialogsFound && callback != null) {
                    callback.execute();
                }

                return true; // We handled at least this popup
            }
            // Fallback to bounds clicking if accessibility click fails
            else {
                Rect bounds = new Rect();
                okButton.getBoundsInScreen(bounds);

                // Use CountDownLatch for synchronization
                CountDownLatch clickLatch = new CountDownLatch(1);
                final boolean[] result = {false};

                helperFunctions.clickOnBounds(bounds, () -> {
                    // Signal click completed
                    clickLatch.countDown();
                }, "Center", 800, 2000, helperFunctions);

                // Wait for click to complete
                try {
                    clickLatch.await(5, TimeUnit.SECONDS);

                    // Now check for next popup
                    boolean moreDialogsFound = checkForActionBlockerWithRetry(depth + 1, attempts + 1, null);

                    if (!moreDialogsFound && callback != null) {
                        callback.execute();
                    }

                    return true; // We handled at least this popup

                } catch (InterruptedException e) {
                    Log.e(TAG, "Click wait interrupted", e);
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clicking OK button", e);
            return false;
        }
    }

    private AccessibilityNodeInfo findTitleNode(AccessibilityNodeInfo dialog) {
        AccessibilityNodeInfo title = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/igds_headline_headline");
        if (title == null) {
            title = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/igds_alert_dialog_headline");
        }
        return title;
    }

    private AccessibilityNodeInfo findDescriptionNode(AccessibilityNodeInfo dialog) {
        AccessibilityNodeInfo description = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/igds_headline_body");
        if (description == null) {
            description = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/igds_alert_dialog_subtext");
        }
        return description;
    }

    private AccessibilityNodeInfo findOkButton(AccessibilityNodeInfo dialog) {
        AccessibilityNodeInfo okButton = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/primary_button_row");
        if (okButton == null) {
            okButton = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/igds_alert_dialog_primary_button");
        }
        return okButton;
    }

    // Existing method for checking target dialog
    private boolean isTargetDialog(AccessibilityNodeInfo title, AccessibilityNodeInfo description) {
        try {
            title.refresh();
            description.refresh();

            if (title == null || description == null) {
                return false;
            }

            String actualTitle = title.getText().toString();
            String actualDesc = description.getText().toString();

            Log.d(TAG, "Dialog texts - Title: " + actualTitle + ", Description: " + actualDesc);
            Log.d(TAG, "Expected - Title: " + blockActionDialogTitle + ", Description: " + blockActionDialogDesc);

            return blockActionDialogTitle.equals(actualTitle) &&
                    (blockActionDialogDesc.equals(actualDesc) ||
                            blockActionDialogDesc2.equals(actualDesc) ||
                            actualDesc.contains("We limit how often you can do certain things"));
        } catch (Exception e) {
            Log.e(TAG, "Error checking target dialog", e);
            return false;
        }
    }







//    public boolean checkForActionBlocker(Action callback) {
//        return checkForActionBlockerRecursive(0, callback);
//    }
//    private boolean checkForActionBlockerRecursive(int depth, Action callback) {
//        if (depth >= MAX_RECURSION_DEPTH) {
//            Log.e(TAG, "Max recursion depth reached");
//            helperFunctions.cleanupAndExit("More Than 10 Action Blocker popUps appeared on a profile exited Automation.", "error");
//            return false;
//        }
//
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            Log.e(TAG, "Sleep interrupted", e);
//        }
//        AccessibilityNodeInfo rootNode = null;
//        try {
//            rootNode = helperFunctions.getRootInActiveWindow();
//            if (rootNode == null) {
//                Log.e(TAG, "Root node not found");
//                helperFunctions.cleanupAndExit("Automation Could not be Completed Please make sure The Device has Accessibility enabled.", "error");
//                return false;
//            }
//
//            AccessibilityNodeInfo dialog = HelperFunctions.findNodeByResourceId(rootNode, dialogId);
////            AccessibilityNodeInfo dialog = helperFunctions.FindAndReturnNodeById(dialogId,4);
//            if (dialog == null) {
//                Log.d(TAG, "No dialog found");
//                return false;
//            }
//
//            try {
//                return handleDialog(dialog, depth, callback);
//
//            } finally {
//                dialog.recycle();
//            }
//        } finally {
//            if (rootNode != null) {
//                rootNode.recycle();
//            }
//        }
//    }
//
//    private boolean handleDialog(AccessibilityNodeInfo dialog, int depth, Action callback) {
//        AccessibilityNodeInfo title = null;
//        AccessibilityNodeInfo description = null;
//        AccessibilityNodeInfo okButton = null;
//
//        try {
//            title = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/igds_headline_headline");
//            if (title == null) {
//                title = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/igds_alert_dialog_headline");
//            }
//            description = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/igds_headline_body");
//            if (description == null) {
//                description = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/igds_alert_dialog_subtext");
//            }
//
//            if (title == null || description == null) {
//                Log.e(TAG, "Title or description is null");
//                return false;
//            }
//
//            if (isTargetDialog(title, description)) {
//                okButton = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/primary_button_row");
//                if (okButton == null) {
//                    okButton = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/igds_alert_dialog_primary_button");
//                    if (okButton == null) {
//                        Log.e(TAG, "OK button not found");
//                        helperFunctions.cleanupAndExit("Automation Could not be Completed Please make sure The Device has Accessibility enabled.", "error");
//                        return true;
//                    }
//                }
//
////                accountManager.setAccountLimitHit(true);
//                return !handleOkButton(okButton, depth, callback);
//            }
//            Log.e(TAG, "isTargetDialog returned false");
//            return false;
//
//        } finally {
//            helperFunctions.safelyRecycleNode(title);
//            helperFunctions.safelyRecycleNode(description);
//            helperFunctions.safelyRecycleNode(okButton);
//        }
//    }
//
//    private boolean isTargetDialog(AccessibilityNodeInfo title, AccessibilityNodeInfo description) {
//        title.refresh();
//        description.refresh();
//        if (title != null && description != null) {
//            String actualTitle = title.getText().toString();
//            String actualDesc = description.getText().toString();
//            Log.d(TAG, "Dialog texts - Title: " + actualTitle + ", Description: " + actualDesc);
//            Log.d(TAG, "Expected - Title: " + blockActionDialogTitle + ", Description: " + blockActionDialogDesc);
//            return blockActionDialogTitle.equals(actualTitle) &&
//                    (blockActionDialogDesc.equals(actualDesc) || blockActionDialogDesc2.equals(actualDesc) || actualDesc.contains("We limit how often you can do certain things"));
//        }
//        Log.e(TAG, "title and description nodes are null");
//        return false;
//    }
//
//    private boolean handleOkButton(AccessibilityNodeInfo okButton, int depth, Action Callback) {
//        if (okButton != null) {
//
//            if (okButton.isClickable() && okButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
//                Log.i(TAG, "Clicked Ok button");
//                return checkForActionBlockerRecursive(depth + 1, Callback);
//            } else {
//                Log.d(TAG, "Falling back to bounds click");
//                Rect bounds = new Rect();
//                okButton.getBoundsInScreen(bounds);
//                Log.d(TAG, "Falling back to bounds click");
//                helperFunctions.clickOnBounds(bounds, ()-> {
//                    return;
//                }, "Center", 800, 2000,helperFunctions);
//                return true;
//            }
//        }
//        Log.e(TAG, "OK button is null");
//        return false;
//    }





//    public boolean handleAnotherPopup(Action Callback) {
//        Log.e(TAG, "Entered handleAnotherPopup");
//        try {
//            // Step 1: Get the root node
//            AccessibilityNodeInfo rootNode = helperFunctions.getRootInActiveWindow();
//            if (rootNode == null) {
//                Log.e(TAG, "Root node not found");
//                return false;
//            }
//
//            // Step 2: Find the dialog
//            AccessibilityNodeInfo dialog = HelperFunctions.findNodeByResourceId(rootNode, dialogId);
//            if (dialog == null) {
//                Log.d(TAG, "No dialog found");
//                return false;
//            }
//
//            AccessibilityNodeInfo button = null;
//
//            try {
//                button = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/igds_alert_dialog_cancel_button");
//                if (button == null) {
//                    button = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/negative_button_row");
//                    if(button == null){
//                        button = HelperFunctions.findNodeByResourceId(dialog, "com.instagram.android:id/primary_button_row");
//                    }
//                }
//
//                if (button == null) {
//                    Log.e(TAG, "Don't Allow button not found");
//                    return false;
//                }
//
//                // Step 6: Perform the click action on the "Don't Allow" button
//                if (button.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
//                    Log.i(TAG, "Clicked on Don't Allow Access button");
//                    return false;
//                } else {
//                    Log.e(TAG, "Failed to perform click action on Don't Allow button");
//
//                    Rect bounds = new Rect();
//                    button.getBoundsInScreen(bounds);
//
//                    helperFunctions.clickOnBounds(bounds, () -> {
//                        Log.i(TAG, "Clicked on Don't Allow Access button via gestures");
//                        Callback.execute();
//                    }, "Center", 800, 1600, helperFunctions);
//
//                    // Wait for the gesture click to complete
//                    return true;
//                }
//
//            } finally {
//                // Safely recycle nodes to avoid memory leaks
//                if (button != null) helperFunctions.safelyRecycleNode(button);
//            }
//
//        } catch (Exception e) {
//            Log.e(TAG, "Unexpected error in handleAnotherPopup: " + e.getMessage());
//            return false;
//        }
//    }
}
