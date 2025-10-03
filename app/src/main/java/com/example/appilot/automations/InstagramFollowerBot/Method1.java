package com.example.appilot.automations.InstagramFollowerBot;

import android.content.Context;

import com.example.appilot.automations.Interfaces.Action;
import com.example.appilot.services.MyAccessibilityService;

import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.appilot.utils.HelperFunctions;
import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;

import java.util.Random;

public class Method1 {
    private static final String TAG = "method1";
    private final HelperFunctions helperFunctions;
    private final InstagramFollowerBotAutomation instagramFollowerBotAutomation;
    private final Handler handler;
    private final Random random;
    private final PopUpHandler popUpHandler;

    public Method1(InstagramFollowerBotAutomation instance, MyAccessibilityService service, Context context, HelperFunctions helperFunctions, Handler handler, Random random, PopUpHandler popupHandler) {
        this.helperFunctions = helperFunctions;
        this.instagramFollowerBotAutomation = instance;
        this.handler = handler;
        this.random = random;
        this.popUpHandler = popupHandler;
    }

    public void recursivefindButtonandClick(String nodeId, int scrollAttempt, int maxAttempts, Action callback) {
        AccessibilityNodeInfo rootNode = null;
        if(instagramFollowerBotAutomation.shouldContinueAutomation()) return;

        try {
            rootNode = helperFunctions.getRootInActiveWindow();
            if (rootNode == null) {
                Log.e(TAG, "Root node is null - cannot proceed with searching");
                helperFunctions.cleanupAndExit("Automation Could not be Completed Please make sure The Device has Accessibility enabled.", "error");
                return;
            }

            AccessibilityNodeInfo suggestionButton = HelperFunctions.findNodeByResourceId(rootNode, nodeId);
            if (suggestionButton != null) {
                boolean isClicked = false;
                try {
                    isClicked = suggestionButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                } catch (Exception e) {
                    Log.e(TAG, "Error performing click action on suggestionButton: " + e.getMessage());
                }
                this.instagramFollowerBotAutomation.safelyRecycleNode(suggestionButton);

                if (isClicked) {
                    handler.postDelayed(() -> {
                        if (callback != null) {
                            try {
                                callback.execute();
                            } catch (Exception e) {
                                Log.e(TAG, "Error executing callback: " + e.getMessage());
                                helperFunctions.cleanupAndExit("Callback execution failed.", "error");
                            }
                        }
                    }, Math.max(3500, 3500 + random.nextInt(1500)));
                    return;
                }
            }

            if (scrollAttempt < maxAttempts) {
                Log.d(TAG, "Button not found, scrolling and retrying. Attempt: " + (scrollAttempt + 1));
                helperFunctions.performScrollUp(() -> {
                    handler.postDelayed(() -> {
                        this.recursivefindButtonandClick(nodeId, scrollAttempt + 1, maxAttempts, callback);
                    }, Math.max(800, 800 + random.nextInt(700)));
                }, helperFunctions);
            } else {
                Log.e(TAG, "Failed to find button after " + maxAttempts + " scroll attempts, Now Going to enter through ");
                AccessibilityNodeInfo finalRootNode = rootNode;
                handler.postDelayed(() -> {
                    AccessibilityNodeInfo profileTab = HelperFunctions.findNodeByResourceId(finalRootNode, "com.instagram.android:id/profile_tab");
                    if (profileTab != null && profileTab.isClickable()) {
                        try {
                            if (profileTab.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                                handler.postDelayed(this::enterSuggestionsViaProfile, Math.max(800, 800 + random.nextInt(800)));
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error clicking profile tab: " + e.getMessage());
                            helperFunctions.cleanupAndExit("Failed to navigate via profile tab.", "error");
                        }
                    } else {
                        Log.e(TAG, "Profile tab not found or not clickable.");
                        helperFunctions.cleanupAndExit("Profile tab interaction failed.", "error");
                    }
                }, 1000);
            }
        } finally {
            this.instagramFollowerBotAutomation.safelyRecycleNode(rootNode);
        }
    }

    private void enterSuggestionsViaProfile() {
        AccessibilityNodeInfo rootNode = null;
        AccessibilityNodeInfo suggestionListButton = null;

        if (popUpHandler.handleOtherPopups(this::enterSuggestionsViaProfile, null)) return;
        try {
            rootNode = helperFunctions.getRootInActiveWindow();
            if (rootNode == null) {
                Log.e(TAG, "Root node is null while entering suggestions");
                helperFunctions.cleanupAndExit("Automation Could not be Completed Please make sure The Device has Accessibility enabled.", "error");
                return;
            }

            AccessibilityNodeInfo seeAllButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/netego_carousel_cta", 20);
            if (seeAllButton == null) {
                suggestionListButton = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/row_profile_header_button_chaining");
                if (suggestionListButton == null) {
                    AccessibilityNodeInfo profileTab = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/profile_tab");
                    if (profileTab != null && profileTab.isClickable()) {
                        try {
                            if (profileTab.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                                handler.postDelayed(this::enterSuggestionsViaProfile, Math.max(800, 800 + random.nextInt(800)));
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error clicking profile tab: " + e.getMessage());
                            Log.e(TAG, "Error clicking profile tab: " + e.getMessage());
                            helperFunctions.cleanupAndExit("Failed to navigate via profile tab.", "error");
                        }
                    }
                    return;
                }
                if (suggestionListButton.isClickable()) {
                    try {
                        if (suggestionListButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                            handler.postDelayed(this::enterSuggestionsViaProfile, Math.max(800, 800 + random.nextInt(800)));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error clicking suggestion list button: " + e.getMessage());
                        this.instagramFollowerBotAutomation.getBoundsAndClick(suggestionListButton, this::enterSuggestionsViaProfile, "Center", 300, 600);
                    }
                } else {
                    this.instagramFollowerBotAutomation.getBoundsAndClick(suggestionListButton, this::enterSuggestionsViaProfile, "Center", 300, 600);
                }
            } else {
                try {
                    if (seeAllButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        handler.postDelayed(() -> {
                            this.instagramFollowerBotAutomation.Container_id = "com.instagram.android:id/recommended_user_card_one";
                            this.instagramFollowerBotAutomation.Username_Id = "com.instagram.android:id/recommended_user_card_name";
                            this.instagramFollowerBotAutomation.Follow_Button_Id = "com.instagram.android:id/recommended_user_card_follow_button";
                            AccessibilityNodeInfo nodeToLoad = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/recommended_user_card_one", 30);
                            if (nodeToLoad != null) {
                                clickSeeAllButtonAndStartFollowing();
                            } else {
                                this.instagramFollowerBotAutomation.ChangeAccount(this.instagramFollowerBotAutomation::callbackAccordingToType);
                            }
                        }, Math.max(3000, 3000 + random.nextInt(2000)));
                    } else {
                        Log.e(TAG, "Could not click see all button");
                        helperFunctions.cleanupAndExit("Automation Could not be Completed Please make sure The Device has Accessibility enabled.", "error");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error clicking see all button: " + e.getMessage());
                    helperFunctions.cleanupAndExit("Failed to interact with see all button.", "error");
                }
            }
        } finally {
            this.helperFunctions.safelyRecycleNode(rootNode);
            this.helperFunctions.safelyRecycleNode(suggestionListButton);
        }
    }

    public void clickSeeAllButtonAndStartFollowing() {
//        if (this.popUpHandler.allowContactsAccessAlertHandler(this::clickSeeAllButtonAndStartFollowing)) {
//            Log.e(TAG, "Automation stopped due to shouldContinue flag.");
//            return;
//        }
        if (this.popUpHandler.handleOtherPopups(this::clickSeeAllButtonAndStartFollowing, null)) {
            Log.e(TAG, "Automation stopped due to shouldContinue flag.");
            return;
        }
        AccessibilityNodeInfo seeAllButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/see_all_button", 1);
        if (seeAllButton != null) {
            this.recursivefindButtonandClick("com.instagram.android:id/see_all_button", 0, 20, this.instagramFollowerBotAutomation::startFollowing);
        } else {
            helperFunctions.performStaticScrollUp(this::clickSeeAllButtonAndStartFollowing, this.helperFunctions);
        }
    }
}

