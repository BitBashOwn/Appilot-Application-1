//package com.example.appilot.automations.InstagramFollowerBot;
//
//import android.content.Context;
//import android.graphics.Rect;
//import android.os.Handler;
//import android.util.Log;
//import android.view.accessibility.AccessibilityNodeInfo;
//
//import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;
//import com.example.appilot.services.MyAccessibilityService;
//import com.example.appilot.utils.HelperFunctions;
//import com.example.appilot.automations.Interfaces.Action;
//
//import java.util.Random;
//
//public class Method2 {
//
//    private static final String TAG = "method1";
////    private final Context context;
////    private final MyAccessibilityService service;
//    private final HelperFunctions helperFunctions;
//    private final InstagramFollowerBotAutomation instagramFollowerBotAutomation;
//    private final Handler handler;
//    private final Random random;
//    private final AccountManager accountManager;
//    private final PopUpHandler popUpHandler;
//
//    public Method2(InstagramFollowerBotAutomation instance, MyAccessibilityService service, Context context, HelperFunctions helperFunctions, Handler handler, Random random, AccountManager manager, PopUpHandler popUpHandler) {
////        this.service = service;
////        this.context = context;
//        this.helperFunctions = helperFunctions;
//        this.instagramFollowerBotAutomation = instance;
//        this.handler = handler;
//        this.random = random;
//        this.accountManager = manager;
//        this.popUpHandler = popUpHandler;
//    }
//
//    public void startFollowersAutomation() {
//        Log.i(TAG, "Entered startFollowersAutomation");
//
//        try {
//            // Check if automation should continue
//            if (this.instagramFollowerBotAutomation.shouldContinueAutomation()) {
//                return;
//            }
//
//            // Check for action blocker popup
//            boolean outerdialogcheck = false;
//            try {
//                outerdialogcheck = this.popUpHandler.checkForActionBlocker(() -> {
//                    accountManager.BlockCurrentAccount();
//                    accountManager.setAccountLimitHit(true);
//                    this.instagramFollowerBotAutomation.getProfileData(() -> {
//                        this.instagramFollowerBotAutomation.ChangeAccount(this.instagramFollowerBotAutomation::callbackAccordingToType);
//                    });
//                });
//            } catch (Exception e) {
//                Log.e(TAG, "Error checking for action blocker: " + e.getMessage(), e);
//            }
//
//            if (outerdialogcheck) {
//                Log.e(TAG, "outerdialogcheck in startProfileFollowing is true");
//                return;
//            }
//
//            if (popUpHandler.handleOtherPopups(this::startFollowersAutomation, null)) return;
//
//            // Get root node with proper error handling
//            AccessibilityNodeInfo rootNode = null;
//            try {
//                rootNode = this.helperFunctions.getRootInActiveWindow();
//                if (rootNode == null) {
//                    Log.e(TAG, "Root node is null - in startFollowersAutomation");
//                    this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
//                    return;
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "Error getting root node: " + e.getMessage(), e);
//                this.helperFunctions.cleanupAndExit("Failed to access screen elements. Please restart the app.", "error");
//                return;
//            }
//
//            // Check for follow button
//            AccessibilityNodeInfo followButton = null;
//            try {
//                followButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/profile_header_follow_button", 40 + random.nextInt(30));
//                if (followButton == null) {
//                    Log.e(TAG, "Profile did not load on current Account. Moving to next Account");
//                    handler.postDelayed(() -> {
//                        this.instagramFollowerBotAutomation.ChangeAccount(() -> this.instagramFollowerBotAutomation.OpenSearchFeed(this.instagramFollowerBotAutomation::ClickAndOpenSearchBar));
//                    }, 1000 + random.nextInt(500));
//                    return;
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "Error finding follow button: " + e.getMessage(), e);
//                this.helperFunctions.cleanupAndExit("Failed to find profile elements. Please try again.", "error");
//                return;
//            }
//
//            // Check for private profile
//            try {
//                if (helperFunctions.InstagramPrivateProfileChecker()) {
//                    accountManager.BlockCurrentAccount();
//                    handler.postDelayed(() -> {
//                        this.instagramFollowerBotAutomation.getProfileData(() -> {
//                            this.instagramFollowerBotAutomation.ChangeAccount(() -> this.instagramFollowerBotAutomation.OpenSearchFeed(this.instagramFollowerBotAutomation::ClickAndOpenSearchBar));
//                        });
//                    }, 500 + random.nextInt(500));
//                    return;
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "Error checking for private profile: " + e.getMessage(), e);
//                this.helperFunctions.cleanupAndExit("Failed to check profile privacy status. Please try again.", "error");
//                return;
//            }
//
//            // Check followers count
//            AccessibilityNodeInfo followersCount = null;
//            try {
//                followersCount = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/row_profile_header_textview_followers_count", 2);
//                if (followersCount == null) {
//                    followersCount = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/profile_header_familiar_followers_value", 2);
//                }
//
//                if (followersCount == null || followersCount.getText() == null ||
//                        helperFunctions.convertPostCount(followersCount.getText().toString()) == 0) {
//                    Log.e(TAG, "Insufficient Followers");
//                    helperFunctions.cleanupAndExit("The profile you provided " + this.instagramFollowerBotAutomation.url +
//                            " does not have enough Followers to perform Method 2 Automation.", "error");
//                    return;
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "Error checking followers count: " + e.getMessage(), e);
//                this.helperFunctions.cleanupAndExit("Failed to read followers count. Please try again.", "error");
//                return;
//            }
//
//            // Find followers button
//            AccessibilityNodeInfo followersButton = null;
//            try {
//                followersButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/row_profile_header_followers_container", 2);
//                if (followersButton == null) {
//                    followersButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/profile_header_followers_stacked_familiar", 2);
//
//                    if (followersButton == null) {
//                        try {
//                            rootNode.refresh();
//                            followersButton = helperFunctions.findNodeByClassAndText(rootNode, "android.widget.TextView", "followers");
//
//                            if (followersButton != null) {
//                                followersButton = followersButton.getParent();
//                            } else {
//                                Log.e(TAG, "Could not find Followers Button");
//                                handler.postDelayed(() -> {
//                                    this.instagramFollowerBotAutomation.ChangeAccount(() ->
//                                            this.instagramFollowerBotAutomation.OpenSearchFeed(this.instagramFollowerBotAutomation::ClickAndOpenSearchBar));
//                                }, 1000 + random.nextInt(500));
//                                return;
//                            }
//                        } catch (Exception e) {
//                            Log.e(TAG, "Error finding followers button by text: " + e.getMessage(), e);
//                            this.helperFunctions.cleanupAndExit("Failed to locate followers button. Please try again.", "error");
//                            return;
//                        }
//                    }
//                }
//
//                if (followersButton == null) {
//                    Log.e(TAG, "Failed to find Followers Button using all methods");
//                    this.helperFunctions.cleanupAndExit("Could not locate followers button. Please try again later.", "error");
//                    return;
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "Error finding followers button: " + e.getMessage(), e);
//                this.helperFunctions.cleanupAndExit("Failed to locate followers button. Please try again.", "error");
//                return;
//            } finally {
//                helperFunctions.safelyRecycleNode(rootNode);
//            }
//
//            // Click on followers button
//            try {
//                if (followersButton.isClickable() &&
//                        followersButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
//                    Log.i(TAG, "Clicked on followers button directly through accessibility");
//                    handler.postDelayed(this.instagramFollowerBotAutomation::startFollowing, 2500 + random.nextInt(1500));
//                } else{
//                    Log.e(TAG, "FollowersButton is not clickable through Accessibility, inside startFollowersAutomation");
//                    Rect bounds = new Rect();
//                    followersButton.getBoundsInScreen(bounds);
//                    this.instagramFollowerBotAutomation.getBoundsAndClick(followersButton,
//                            this.instagramFollowerBotAutomation::startFollowing, "Center", 2500, 4000);
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "Error clicking followers button: " + e.getMessage(), e);
//                this.helperFunctions.cleanupAndExit("Failed to click on followers button. Please try again.", "error");
//            } finally {
//                helperFunctions.safelyRecycleNode(followersButton);
//                helperFunctions.safelyRecycleNode(followersCount);
//                helperFunctions.safelyRecycleNode(followButton);
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Unexpected error in startFollowersAutomation: " + e.getMessage(), e);
//            this.helperFunctions.cleanupAndExit("An unexpected error occurred. Please try again later.", "error");
//        }
//    }
//}


package com.example.appilot.automations.InstagramFollowerBot;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;
import com.example.appilot.services.MyAccessibilityService;
import com.example.appilot.utils.HelperFunctions;
import com.example.appilot.automations.Interfaces.Action;

import java.util.Random;

public class Method2 {

    private static final String TAG = "method2";
    //    private final Context context;
//    private final MyAccessibilityService service;
    private final HelperFunctions helperFunctions;
    private final InstagramFollowerBotAutomation instagramFollowerBotAutomation;
    private final Handler handler;
    private final Random random;
    private final AccountManager accountManager;
    private final PopUpHandler popUpHandler;

    public Method2(InstagramFollowerBotAutomation instance, MyAccessibilityService service, Context context, HelperFunctions helperFunctions, Handler handler, Random random, AccountManager manager, PopUpHandler popUpHandler) {
//        this.service = service;
//        this.context = context;
        this.helperFunctions = helperFunctions;
        this.instagramFollowerBotAutomation = instance;
        this.handler = handler;
        this.random = random;
        this.accountManager = manager;
        this.popUpHandler = popUpHandler;
    }

    public void startFollowersAutomation() {
        Log.i(TAG, "Entered startFollowersAutomation");

        try {
            // Check if automation should continue
            if (this.instagramFollowerBotAutomation.shouldContinueAutomation()) {
                return;
            }

            // Check for action blocker popup
            boolean outerdialogcheck = false;
            try {
                outerdialogcheck = this.popUpHandler.checkForActionBlocker(() -> {
                    accountManager.BlockCurrentAccount();
                    accountManager.setAccountLimitHit(true);
                    this.instagramFollowerBotAutomation.getProfileData(() -> {
                        this.instagramFollowerBotAutomation.ChangeAccount(this.instagramFollowerBotAutomation::callbackAccordingToType);
                    });
                });
            } catch (Exception e) {
                Log.e(TAG, "Error checking for action blocker: " + e.getMessage(), e);
            }

            if (outerdialogcheck) {
                Log.e(TAG, "outerdialogcheck in startProfileFollowing is true");
                return;
            }

            if (popUpHandler.handleOtherPopups(this::startFollowersAutomation, null)) return;

            // Get root node with proper error handling
            AccessibilityNodeInfo rootNode = null;
            try {
                rootNode = this.helperFunctions.getRootInActiveWindow();
                if (rootNode == null) {
                    Log.e(TAG, "Root node is null - in startFollowersAutomation");
                    this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting root node: " + e.getMessage(), e);
                this.helperFunctions.cleanupAndExit("Failed to access screen elements. Please restart the app.", "error");
                return;
            }

            // Check for follow button
            AccessibilityNodeInfo followButton = null;
            try {
                followButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/profile_header_follow_button", 40 + random.nextInt(30));
                if (followButton == null) {
                    Log.e(TAG, "Profile did not load on current Account. Moving to next Account");
                    handler.postDelayed(() -> {
                        this.instagramFollowerBotAutomation.ChangeAccount(this.instagramFollowerBotAutomation::callbackAccordingToType);
                    }, 1000 + random.nextInt(500));
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error finding follow button: " + e.getMessage(), e);
                this.helperFunctions.cleanupAndExit("Failed to find profile elements. Please try again.", "error");
                return;
            }

            // Check for private profile
            try {
                if (helperFunctions.InstagramPrivateProfileChecker()) {
                    accountManager.BlockCurrentAccount();
                    handler.postDelayed(() -> {
                        this.instagramFollowerBotAutomation.getProfileData(() -> {
                            this.instagramFollowerBotAutomation.ChangeAccount(this.instagramFollowerBotAutomation::callbackAccordingToType);
                        });
                    }, 500 + random.nextInt(500));
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking for private profile: " + e.getMessage(), e);
                this.helperFunctions.cleanupAndExit("Failed to check profile privacy status. Please try again.", "error");
                return;
            }

            // Check followers count
            AccessibilityNodeInfo followersCount = null;
            try {
                followersCount = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/row_profile_header_textview_followers_count", 2);
                if (followersCount == null) {
                    followersCount = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/profile_header_familiar_followers_value", 2);
                }

                if (followersCount == null || followersCount.getText() == null ||
                        helperFunctions.convertPostCount(followersCount.getText().toString()) == 0) {
                    Log.e(TAG, "Insufficient Followers");
                    helperFunctions.cleanupAndExit("The profile you provided " + this.accountManager.getCurrentAccountUrl() +
                            " does not have enough Followers to perform Method 2 Automation.", "error");
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking followers count: " + e.getMessage(), e);
                this.helperFunctions.cleanupAndExit("Failed to read followers count. Please try again.", "error");
                return;
            }

            // Find followers button
            AccessibilityNodeInfo followersButton = null;
            try {
                followersButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/row_profile_header_followers_container", 2);
                if (followersButton == null) {
                    followersButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/profile_header_followers_stacked_familiar", 2);

                    if (followersButton == null) {
                        try {
                            rootNode.refresh();
                            followersButton = helperFunctions.findNodeByClassAndText(rootNode, "android.widget.TextView", "followers");

                            if (followersButton != null) {
                                followersButton = followersButton.getParent();
                            } else {
                                Log.e(TAG, "Could not find Followers Button");
                                handler.postDelayed(() -> {
                                    this.instagramFollowerBotAutomation.ChangeAccount(this.instagramFollowerBotAutomation::callbackAccordingToType);
                                }, 1000 + random.nextInt(500));
                                return;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error finding followers button by text: " + e.getMessage(), e);
                            this.helperFunctions.cleanupAndExit("Failed to locate followers button. Please try again.", "error");
                            return;
                        }
                    }
                }

                if (followersButton == null) {
                    Log.e(TAG, "Failed to find Followers Button using all methods");
                    this.helperFunctions.cleanupAndExit("Could not locate followers button. Please try again later.", "error");
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error finding followers button: " + e.getMessage(), e);
                this.helperFunctions.cleanupAndExit("Failed to locate followers button. Please try again.", "error");
                return;
            } finally {
                helperFunctions.safelyRecycleNode(rootNode);
            }

            // Click on followers button
            try {
                if (followersButton.isClickable() &&
                        followersButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    Log.i(TAG, "Clicked on followers button directly through accessibility");
                    handler.postDelayed(this.instagramFollowerBotAutomation::startFollowing, 2500 + random.nextInt(1500));
                } else{
                    Log.e(TAG, "FollowersButton is not clickable through Accessibility, inside startFollowersAutomation");
                    Rect bounds = new Rect();
                    followersButton.getBoundsInScreen(bounds);
                    this.instagramFollowerBotAutomation.getBoundsAndClick(followersButton,
                            this.instagramFollowerBotAutomation::startFollowing, "Center", 2500, 4000);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error clicking followers button: " + e.getMessage(), e);
                this.helperFunctions.cleanupAndExit("Failed to click on followers button. Please try again.", "error");
            } finally {
                helperFunctions.safelyRecycleNode(followersButton);
                helperFunctions.safelyRecycleNode(followersCount);
                helperFunctions.safelyRecycleNode(followButton);
            }
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in startFollowersAutomation: " + e.getMessage(), e);
            this.helperFunctions.cleanupAndExit("An unexpected error occurred. Please try again later.", "error");
        }
    }
}
