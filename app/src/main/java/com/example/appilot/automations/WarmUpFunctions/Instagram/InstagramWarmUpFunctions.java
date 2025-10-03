package com.example.appilot.automations.WarmUpFunctions.Instagram;

import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.appilot.automations.Interfaces.Action;
import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;
import com.example.appilot.services.MyAccessibilityService;
import com.example.appilot.utils.HelperFunctions;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class InstagramWarmUpFunctions {
    private static final String TAG = "Instagram Profile Warmup Functions";
    private final MyAccessibilityService service;
    private final Handler handler;
    private final Random random;
    private final HelperFunctions helperFunctions;
    private final PopUpHandler popUpHandler;
    private int scrollCount = 0;

    public InstagramWarmUpFunctions(MyAccessibilityService service, Handler handler, Random random, HelperFunctions helperfunctions, PopUpHandler popupHandler) {
        this.service = service;
        this.handler = handler;
        this.random = random;
        this.helperFunctions = helperfunctions;
        this.popUpHandler = popupHandler;
    }


    public void viewProfile(AccessibilityNodeInfo rootNode, Action Callback) {
        Log.d(TAG, "entered to perform warmUp function: viewProfile");

        AccessibilityNodeInfo profileNode = null;
        try {
            profileNode = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/row_profile_header_imageview_frame_layout");
            if (profileNode == null) {
                profileNode = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/avatar_on_profile_header_view");
                if (profileNode == null) {
                    handler.postDelayed(Callback::execute, 1000 + random.nextInt(500));
                    return;
                }
            }
            if (profileNode.isClickable() && profileNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                handler.postDelayed(() -> {
                    if (helperFunctions.FindAndReturnNodeById("com.instagram.android:id/expanded_profile_pic", 1) != null || helperFunctions.FindAndReturnNodeById("com.instagram.android:id/reel_viewer_front_avatar", 1) != null) {
                        helperFunctions.navigateBack();
                    }
                    handler.postDelayed(Callback::execute, 500 + random.nextInt(500));
                }, 2230 + random.nextInt(734));
            } else {
                handler.postDelayed(Callback::execute, 1000 + random.nextInt(500));
            }
        } finally {
            helperFunctions.safelyRecycleNode(rootNode);
        }
    }

    public void viewPosts(AccessibilityNodeInfo rootNode, Action Callback) {
        Log.d(TAG, "entered to perform warmUp function: viewPosts");

        try {
            if (helperFunctions.InstagramPrivateProfileChecker(rootNode)) {
                Log.e(TAG, "profile is private");
                Callback.execute();
                return;
            }

            AccessibilityNodeInfo postCount = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/row_profile_header_textview_post_count");
            if (postCount == null) {
                postCount = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/profile_header_familiar_post_count_value");
            }
            if (postCount != null) {
                int posts = helperFunctions.convertPostCount(postCount.getText().toString());
                if (posts == 0) {
                    Log.e(TAG, "profile has no posts");
                    Callback.execute();
                } else {
                    handler.postDelayed(() -> {
//                        performScrollUp(()->{openPostHandler(rootNode,posts);});
                        openPostHandler(rootNode, posts, Callback);
                    }, 1500 + random.nextInt(1000));
                }
            } else {
                handler.postDelayed(Callback::execute, 1000 + random.nextInt(1000));
            }

        } finally {
            helperFunctions.safelyRecycleNode(rootNode);
        }
    }

    public void openPostHandler(AccessibilityNodeInfo rootNode, int postCount, Action Callback) {

        rootNode.refresh();
        List<AccessibilityNodeInfo> postsSection = rootNode.findAccessibilityNodeInfosByViewId("com.instagram.android:id/media_set_row_content_identifier");
        Log.e(TAG, "postsSection size = " + postsSection.size());
//        if (postsSection != null && postsSection.size() > 0) {
        if (!postsSection.isEmpty()) {
            Log.e(TAG, "Found list inside openPostHandler");

            int collectionId = random.nextInt(postsSection.size());
            Log.d(TAG, "Selected collectionId: " + collectionId);
            if (postCount > 20) {
                postCount = 20;
            }
            scrollCount = postCount - ((collectionId - 1) * 3);
            AccessibilityNodeInfo collection = postsSection.get(collectionId);

            if (collection != null && collection.getChildCount() > 0) {
                int postId = random.nextInt(collection.getChildCount());
                scrollCount -= postId;
                AccessibilityNodeInfo post = collection.getChild(postId);
                Log.d(TAG, "postId Number: " + postId);
                if (post != null && post.isClickable()) {
                    post.performAction(AccessibilityNodeInfo.ACTION_CLICK);

//                    if (false) {
//                        handler.postDelayed(() -> {
//                            helperFunctions.navigateBack();
//                            handler.postDelayed(Callback::execute, 1000 + random.nextInt(1000));
//                        }, 3000 + random.nextInt(3000));
//                    } else {
                        Log.i(TAG, "postCount: " + postCount);
                        Log.i(TAG, "Max scrollCount: " + scrollCount);
                        float percent = 0.1f + random.nextFloat() * 0.3f;
                        int reducedScrollCount = (int) Math.ceil(postCount * percent);
                        if (reducedScrollCount < 1) {
                            reducedScrollCount = 1;
                        }
                        Log.i(TAG, "Calculated scrollCount: " + reducedScrollCount);
                        scrollPosts(reducedScrollCount, Callback);
//                    }
                } else {
                    Log.e(TAG, "Post is null or not clickable");
                    handler.postDelayed(Callback::execute, 1000 + random.nextInt(1000));
                }
            } else {
                Log.e(TAG, "Collection is null or has no children");
                Callback.execute();
            }
        } else {
            Log.e(TAG, "Posts section is null or has no valid collections (excluding index 0)");
            handler.postDelayed(Callback::execute, 1000 + random.nextInt(1000));
        }
    }

    public void scrollPosts(int scrollCount, Action Callback) {

        Log.e(TAG, "Entered SctollPosts");
        if (scrollCount > 0) {
            handler.postDelayed(() -> {
                helperFunctions.performScrollUp(() -> {
                    scrollPosts(scrollCount - 1, Callback);
                },helperFunctions);
            }, 1500 + random.nextInt(1500));
        } else {
            helperFunctions.navigateBack();
//            handler.postDelayed(this::handleFirstProfileVisit, 1000 + random.nextInt(1000));
            handler.postDelayed(Callback::execute, 1000 + random.nextInt(1000));
        }

    }

    public void viewFollowingandFollowers(AccessibilityNodeInfo rootNode, String countId, String buttonId, String countId2, String buttonId2, Action Callback) {

        Log.d(TAG, "entered to perform warmUp function: viewFollowingandFollowers");
        try {
            if (helperFunctions.InstagramPrivateProfileChecker(rootNode)) {
                Log.e(TAG, "profile is private");
                Callback.execute();
                return;
            }

            AccessibilityNodeInfo FollowersCount = HelperFunctions.findNodeByResourceId(rootNode, countId);
            if (FollowersCount == null) {
                FollowersCount = HelperFunctions.findNodeByResourceId(rootNode, countId2);
            }
            if (FollowersCount != null) {
                int followers = helperFunctions.convertPostCount(FollowersCount.getText().toString());
                if (followers == 0) {
                    Log.e(TAG, "profile has no viewFollowingandFollowers");
                    Callback.execute();
                } else {
                    Log.e(TAG, "Followers: " + followers);
                    AccessibilityNodeInfo FollowersButton = HelperFunctions.findNodeByResourceId(rootNode, buttonId);
                    if (FollowersButton == null) {
                        FollowersButton = HelperFunctions.findNodeByResourceId(rootNode, buttonId2);
                    }
                    if (FollowersButton != null && FollowersButton.isClickable() && FollowersButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        Log.e(TAG, "Clicked on FollowersButton");

                        handler.postDelayed(() -> {
                            int scrolls = followers / 20;
                            if (scrolls > 10) {
                                scrolls = random.nextInt(10);
                            }
                            scrollPosts(scrolls, Callback);
                        }, 2500 + random.nextInt(2000));
                        return;
                    } else {
                        Callback.execute();
                        return;
                    }
                }
            } else {
                handler.postDelayed(Callback::execute, 1000 + random.nextInt(1000));
            }
        } finally {
            helperFunctions.safelyRecycleNode(rootNode);
        }
    }

//    public void enterDM(Action callback) {
//        Log.i(TAG, "Entered to perform warmup function enterDM");
//        AccessibilityNodeInfo rootNode = helperFunctions.getRootInActiveWindow();
//        if (rootNode == null) {
//            Log.e(TAG, "RootNode not found in enterDM");
//            callback.execute();
//            return;
//        }
//
//        AccessibilityNodeInfo inboxButton = HelperFunctions.findNodeByResourceId(rootNode,
//                "com.instagram.android:id/action_bar_inbox_button");
//        if (inboxButton == null || !inboxButton.isClickable()) {
//            Log.e(TAG, inboxButton == null ? "inboxButton is null" : "InboxButton is not clickable");
//            callback.execute();
//            return;
//        }
//
//        if (!inboxButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
//            Log.e(TAG, "Could not click InboxButton");
//            callback.execute();
//            return;
//        }
//
//        // Wait for DMs to load
//        handler.postDelayed(() -> {
//            // Get fresh root node after navigation
//            AccessibilityNodeInfo newRootNode = helperFunctions.getRootInActiveWindow();
//            if (newRootNode == null) {
//                Log.e(TAG, "New root node is null after entering inbox");
//                helperFunctions.navigateBack();
//                handler.postDelayed(callback::execute, 1000 + random.nextInt(1000));
//                return;
//            }
//
//            List<AccessibilityNodeInfo> dms = newRootNode.findAccessibilityNodeInfosByViewId(
//                    "com.instagram.android:id/row_inbox_container");
//
//            if (dms == null || dms.isEmpty()) {
//                Log.e(TAG, "No DMs found");
//                helperFunctions.navigateBack();
//                handler.postDelayed(callback::execute, 1000 + random.nextInt(1000));
//                return;
//            }
//
//            // 50% chance to scroll if there are enough DMs
//            if (random.nextInt(100) < 50 && dms.size() > 8) {
//                Log.i(TAG, "Going to just scroll and return from DM warmup function");
//                helperFunctions.performScrollUp(() -> {
//                    helperFunctions.navigateBack();
//                    handler.postDelayed(callback::execute, 2000 + random.nextInt(1000));
//                },helperFunctions);
//                return;
//            }
//
//            // Enter random DM
//            Log.i(TAG, "Going to enter DM and return from DM warmup function");
//            try {
//                int randomIndex = random.nextInt(Math.max(1, dms.size() - 1));
//                AccessibilityNodeInfo dmNode = dms.get(randomIndex);
//
//                if (dmNode != null && dmNode.isClickable()) {
//                    if (dmNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
//                        Log.i(TAG, "Entered DM");
//                        // Wait random time in DM then exit
//                        handler.postDelayed(() -> {
//                            helperFunctions.navigateBack();
//                            handler.postDelayed(() -> {
//                                helperFunctions.navigateBack();
//                                handler.postDelayed(callback::execute, 2000 + random.nextInt(1000));
//                            }, 800 + random.nextInt(800));
//                        }, 5000 + random.nextInt(3000));
//                        return;
//                    }
//                }
//                Log.e(TAG, "Failed to click DM node");
//            } catch (Exception e) {
//                Log.e(TAG, "Error processing DM node: " + e.getMessage());
//            }
//
//            // If we reach here, something went wrong with DM entry
//            helperFunctions.navigateBack();
//            handler.postDelayed(callback::execute, 2000 + random.nextInt(1000));
//
//        }, 2000 + random.nextInt(2000));
//    }


    public void enterDM(Action callback) {
        Log.i(TAG, "Entered to perform warmup function enterDM");
        AccessibilityNodeInfo rootNode = helperFunctions.getRootInActiveWindow();

        if (rootNode == null) {
            logErrorAndExecuteCallback("RootNode not found in enterDM", callback);
            return;
        }

        openInbox(rootNode, callback);
    }

    /**
     * Attempts to open the Instagram inbox
     */
    private void openInbox(AccessibilityNodeInfo rootNode, Action callback) {
        AccessibilityNodeInfo inboxButton = HelperFunctions.findNodeByResourceId(rootNode,
                "com.instagram.android:id/action_bar_inbox_button");

        if (inboxButton == null || !inboxButton.isClickable()) {
            logErrorAndExecuteCallback(inboxButton == null ? "inboxButton is null" : "InboxButton is not clickable", callback);
            return;
        }

        if (!inboxButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            logErrorAndExecuteCallback("Could not click InboxButton", callback);
            return;
        }

        // Wait for DMs to load then process the inbox
        int delayTime = 2000 + random.nextInt(2000);
        handler.postDelayed(() -> processInbox(callback), delayTime);
    }

    /**
     * Processes the inbox after it has loaded
     */
    private void processInbox(Action callback) {

        if (popUpHandler.handleOtherPopups(()->this.processInbox(callback), null)) return;
        // Get fresh root node after navigation
        AccessibilityNodeInfo newRootNode = helperFunctions.getRootInActiveWindow();



        if (newRootNode == null) {
            Log.e(TAG, "New root node is null after entering inbox");
            exitAndExecuteCallback(callback, 1000 + random.nextInt(1000));
            return;
        }

        List<AccessibilityNodeInfo> dms = findDmContainers(newRootNode);

        if (dms == null || dms.isEmpty()) {
            Log.e(TAG, "No DMs found");
            exitAndExecuteCallback(callback, 1000 + random.nextInt(1000));
            return;
        }

        // 50% chance to scroll if there are enough DMs
        if (shouldJustScroll(dms)) {
            Log.i(TAG, "Going to just scroll and return from DM warmup function");
            scrollInboxAndExit(callback);
            return;
        }

        // Enter random DM
        Log.i(TAG, "Going to enter DM and return from DM warmup function");
        enterRandomDm(dms, callback);
    }

    /**
     * Finds DM container nodes in the inbox
     */
    private List<AccessibilityNodeInfo> findDmContainers(AccessibilityNodeInfo rootNode) {
        try {
            return rootNode.findAccessibilityNodeInfosByViewId(
                    "com.instagram.android:id/row_inbox_container");
        } catch (Exception e) {
            Log.e(TAG, "Error finding DM containers: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Determines if we should just scroll through the inbox
     */
    private boolean shouldJustScroll(List<AccessibilityNodeInfo> dms) {
        return random.nextInt(100) < 50 && dms.size() > 8;
    }

    /**
     * Scrolls through the inbox and then exits
     */
    private void scrollInboxAndExit(Action callback) {
        helperFunctions.performScrollUp(() -> {
            helperFunctions.navigateBack();
            int delayTime = 2000 + random.nextInt(1000);
            handler.postDelayed(callback::execute, delayTime);
        }, helperFunctions);
    }

    /**
     * Enters a random DM conversation
     */
    private void enterRandomDm(List<AccessibilityNodeInfo> dms, Action callback) {
        try {

            int randomIndex = random.nextInt(Math.max(1, dms.size() - 1));
            AccessibilityNodeInfo dmNode = dms.get(randomIndex);

            if (dmNode != null && dmNode.isClickable() && dmNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                Log.i(TAG, "Entered DM");
                handler.postDelayed(()->this.stayInDmThenExit(callback), 2000+random.nextInt(2000));
                return;
            }

            Log.e(TAG, "Failed to click DM node");
        } catch (Exception e) {
            Log.e(TAG, "Error processing DM node: " + e.getMessage());
        }

        // If we reach here, something went wrong with DM entry
        exitAndExecuteCallback(callback, 2000 + random.nextInt(1000));
    }

    /**
     * Stays in a DM conversation for a random time, then exits
     */
    private void stayInDmThenExit(Action callback) {
        if (popUpHandler.handleOtherPopups(()->this.stayInDmThenExit(callback), null)) return;
        // Wait random time in DM then exit
        int stayTime = 5000 + random.nextInt(3000);
        handler.postDelayed(() -> {
            helperFunctions.navigateBack();
            int firstExitDelay = 800 + random.nextInt(800);
            handler.postDelayed(() -> {
                helperFunctions.navigateBack();
                int finalDelay = 2000 + random.nextInt(1000);
                handler.postDelayed(callback::execute, finalDelay);
            }, firstExitDelay);
        }, stayTime);
    }

    /**
     * Navigates back and executes the callback after a delay
     */
    private void exitAndExecuteCallback(Action callback, int delayMs) {
        helperFunctions.navigateBack();
        handler.postDelayed(callback::execute, delayMs);
    }

    /**
     * Logs an error and executes the callback immediately
     */
    private void logErrorAndExecuteCallback(String errorMessage, Action callback) {
        Log.e(TAG, errorMessage);
        callback.execute();
    }



}
