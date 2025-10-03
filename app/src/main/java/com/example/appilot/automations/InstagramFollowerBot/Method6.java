package com.example.appilot.automations.InstagramFollowerBot;

import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;
import com.example.appilot.utils.HelperFunctions;
import java.util.List;
import java.util.Random;

public class Method6 {
    private static final String TAG = "method3";
    private final HelperFunctions helperFunctions;
    private final InstagramFollowerBotAutomation instagramFollowerBotAutomation;
    private final Handler handler;
    private final Random random;
    private final AccountManager accountManager;
    private final PopUpHandler popUpHandler;
    public Method6(InstagramFollowerBotAutomation instance, HelperFunctions helperFunctions, Handler handler, Random random, AccountManager manager, PopUpHandler popUpHandler) {
        this.helperFunctions = helperFunctions;
        this.instagramFollowerBotAutomation = instance;
        this.handler = handler;
        this.random = random;
        this.accountManager = manager;
        this.popUpHandler = popUpHandler;
    }

    public void startLikersAutomation() {
        Log.i(TAG, "Entered startLikersAutomation");
        if(instagramFollowerBotAutomation.shouldContinueAutomation()) return;

        if (popUpHandler.handleOtherPopups(()->this.startLikersAutomation(), null)) return;

        boolean outerdialogcheck = popUpHandler.checkForActionBlocker(()->{
            accountManager.BlockCurrentAccount();
            accountManager.setAccountLimitHit(true);
            this.instagramFollowerBotAutomation.handleNavigationByType();
        });

        if (outerdialogcheck) {
            Log.e(TAG, "outerdialogcheck in startLikersAutomation is true");
            return;
        }

        AccessibilityNodeInfo rootNode = this.helperFunctions.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "Root node is null - in startLikersAutomation");
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed Please make sure The Device has Accessibility enabled.", "error");
            return;
        }
        AccessibilityNodeInfo followButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/button_container", 5);
        if (followButton == null) {
            followButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/profile_header_follow_button", 40 + random.nextInt(30));
            if (followButton == null) {
                Log.e(TAG, "Profile did not load, moving to next account");
                handler.postDelayed(() -> instagramFollowerBotAutomation.ChangeAccount(this.instagramFollowerBotAutomation::callbackAccordingToType), 1000 + random.nextInt(500));
                rootNode.recycle();
                return;
            }
        }
        if (helperFunctions.InstagramPrivateProfileChecker()) {
            Log.e(TAG, "Profile is Private, could not perform Automation");
            accountManager.BlockCurrentAccount();
            handler.postDelayed(() -> instagramFollowerBotAutomation.getProfileData(() -> instagramFollowerBotAutomation.ChangeAccount(this.instagramFollowerBotAutomation::callbackAccordingToType)), 1000 + random.nextInt(500));
            rootNode.recycle();
            return;
        }

//        AccessibilityNodeInfo postCount = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/row_profile_header_textview_post_count", 1);
        rootNode.refresh();
        AccessibilityNodeInfo postCount = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/row_profile_header_textview_post_count");
        if (postCount == null) {
//            postCount = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/profile_header_familiar_post_count_value", 1);
            postCount = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/profile_header_familiar_post_count_value");
            if (postCount == null) {
                rootNode.refresh();
                postCount = helperFunctions.findNodeByClassAndText(rootNode, "android.widget.TextView", "posts");
                if (postCount == null) {
                    rootNode.recycle();
                    Log.e(TAG, "Cannot find posts count");
                    this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please ensure Accessibility service is enabled.", "error");
                    return;
                }
                AccessibilityNodeInfo parentNode = postCount.getParent();
                postCount = parentNode != null ? parentNode.getChild(0) : null;
            }
        }

        if (postCount != null) {
            Log.e(TAG, "Posts in text = " + postCount.getText().toString());
            int totalPosts = helperFunctions.convertPostCount(postCount.getText().toString());
            accountManager.setCurrentAccountTotalPosts(totalPosts);
            postCount.recycle();

            if (totalPosts == 0) {
                Log.e(TAG, "Insufficient posts on profile");
                rootNode.recycle();
                helperFunctions.cleanupAndExit("The profile does not have enough posts to perform Method 6 Automation.", "error");
                return;
            }
            Log.i(TAG, "totalPosts: " + totalPosts);
            AccessibilityNodeInfo postButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/row_profile_header_post_count_container", 3);
            if (postButton == null) {
                postButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/profile_header_post_count_front_familiar", 3);
                if (postButton == null) {
                    rootNode.refresh();
                    postButton = helperFunctions.findNodeByClassAndText(rootNode, "android.widget.TextView", "posts");
                    if (postButton == null) {
                        rootNode.recycle();
                        Log.e(TAG, "Could not find post button");
                        this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please ensure Accessibility is enabled and network connection is strong.", "error");
                        return;
                    }
                    postButton = postButton.getParent();
                    Log.i(TAG, "postButton Found");
                }
            }

            if (postButton != null && postButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                Log.e(TAG, "Found Post Count Button and Clicked");
                handler.postDelayed(this::getNextPost, 1500 + random.nextInt(500));
            } else {
                Log.e(TAG, "Could not click post button");
                this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please ensure Accessibility is enabled.", "error");
            }

            if (postButton != null) postButton.recycle();
        } else {
            Log.e(TAG, "Cannot find posts count");
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please ensure Accessibility service is enabled.", "error");
        }

        rootNode.recycle();
    }


    public void getNextPost() {
        Log.i(TAG, "Entered getNextPost");
        if (instagramFollowerBotAutomation.shouldContinueAutomation()) return;

        if (popUpHandler.handleOtherPopups(() -> this.getNextPost(), null)) return;

        boolean outerdialogcheck = popUpHandler.checkForActionBlocker(() -> {
            accountManager.BlockCurrentAccount();
            accountManager.setAccountLimitHit(true);
            this.instagramFollowerBotAutomation.handleNavigationByType();
        });

        if (outerdialogcheck) {
            Log.e(TAG, "outerdialogcheck in getNextPost is true");
            return;
        }
        if (accountManager.getCurrentAccountPostsDone() >= accountManager.getCurrentAccountTotalPosts()) {
            Log.e(TAG, "All posts are done");
            accountManager.BlockCurrentAccount();
            instagramFollowerBotAutomation.handleNavigationByType();
            return;
        }

        AccessibilityNodeInfo rootNode = helperFunctions.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "Root node is null. Can't proceed.");
            helperFunctions.cleanupAndExit("Automation Could not be Completed Please make sure The Device has Accessibility enabled.", "error");
            return;
        }

        helperFunctions.saveNodeTreeToDownloads( rootNode, "post_node_hierarchy.txt");
        List<AccessibilityNodeInfo> rows = helperFunctions.FindAndReturnAllNodesById("com.instagram.android:id/media_set_row_content_identifier", 15 + random.nextInt(10));
        if (rows == null || rows.isEmpty()) {
            Log.e(TAG, "No rows found. Attempting to scroll up.");
            helperFunctions.cleanupAndExit("Could not found post nodes for the provided profile url.", "error");
//            helperFunctions.performStaticScrollUp(this::getNextPost, this.helperFunctions);
            return;
        }

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            AccessibilityNodeInfo row = rows.get(rowIndex);
            List<AccessibilityNodeInfo> buttons = HelperFunctions.findNodesByClass(row, "android.widget.Button");
            if (buttons.isEmpty()) continue;

            for (AccessibilityNodeInfo button : buttons) {
                String desc = button.getContentDescription().toString().toLowerCase();
                if (desc.contains("row " + accountManager.getCurrentAccountCurrentRow() + ", column " + accountManager.getCurrentAccountCurrentColumn())) {
                    button.recycle();
                    row.recycle();
//                    accountManager.incrementCurrentAccountPostsDone();
                    if (button.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        Log.e(TAG, "Found Post and Opened");
                        handler.postDelayed(this::StartLikesFollowing, 1500 + random.nextInt(1000));
                        return;
                    } else {
                        continue;
                    }
                }
                button.recycle();
            }
            row.recycle();
        }

        if (accountManager.getCurrentAccountPostsDone() < accountManager.getCurrentAccountTotalPosts()) {
            helperFunctions.performStaticScrollUp(this::getNextPost, this.helperFunctions);
        }
    }

    public void StartLikesFollowing() {
        Log.i(TAG, "Entered StartLikesFollowing");
        if (this.instagramFollowerBotAutomation.shouldContinueAutomation()) return;

        if (popUpHandler.handleOtherPopups(()->this.StartLikesFollowing(), null)) return;

        AccessibilityNodeInfo rootNode = null;
        try {
            rootNode = this.helperFunctions.getRootInActiveWindow();
            if (rootNode == null) {
                Log.e(TAG, "Root node is null - in StartLikesFollowing");
                accountManager.BlockCurrentAccount();
                instagramFollowerBotAutomation.getProfileData(() -> instagramFollowerBotAutomation.ChangeAccount(this.instagramFollowerBotAutomation::callbackAccordingToType));
                return;
            }
            Log.d(TAG, "Found RootNode inside StartLikesFollowing");

            // Try to find and click the like count button first
            AccessibilityNodeInfo likersBtn = null;
            try {
                likersBtn = HelperFunctions.findNodeByResourceId(rootNode,
                        "com.instagram.android:id/row_feed_like_count");

                if (likersBtn != null) {
                    Log.d(TAG, "Found liker Button Directly and going to click");
                    handleLikerButtonClick(rootNode, likersBtn);
                    return;
                }
                Log.e(TAG, "Could not Found liker Button Directly");
                handlePostFooterLikes(rootNode);
            } catch (Exception e) {
                Log.e(TAG, "Exception in finding liker button: " + e.getMessage(), e);
                handlePostFooterLikes(rootNode);
            } finally {
                if (likersBtn != null) {
                    likersBtn.recycle();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in StartLikesFollowing: " + e.getMessage(), e);
            accountManager.BlockCurrentAccount();
            this.instagramFollowerBotAutomation.handleNavigationByType();
        } finally {
            if (rootNode != null) {
                rootNode.recycle();
            }
        }
    }

    private void handleLikerButtonClick(AccessibilityNodeInfo rootNode, AccessibilityNodeInfo likersBtn) {
        Log.i(TAG, "Entered handleLikerButtonClick");
        if(instagramFollowerBotAutomation.shouldContinueAutomation()) return;

        if (likersBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            Log.i(TAG, "Clicked likersBtn Directly");
            scheduleLikersSectionCheck(rootNode);
        } else {
            Log.i(TAG, "Could not Click likersBtn Directly, going to click through bounds");
            instagramFollowerBotAutomation.getBoundsAndClick(likersBtn, () -> {
                Log.d(TAG, "Clicked successfully on likers Button bounds");
                scheduleLikersSectionCheck(rootNode);
            }, "Center", 2000, 3000);
        }
    }

    private void handlePostFooterLikes(AccessibilityNodeInfo rootNode) {
        Log.i(TAG, "Entered handlePostFooterLikes");
        if (this.instagramFollowerBotAutomation.shouldContinueAutomation()) {
            return;
        }

        List<AccessibilityNodeInfo> postFooter = null;
        AccessibilityNodeInfo footer = null;
        AccessibilityNodeInfo userAvatars = null;
        List<AccessibilityNodeInfo> likersButtons = null;
        AccessibilityNodeInfo likersButton = null;

        try {
            rootNode.refresh();
            postFooter = rootNode.findAccessibilityNodeInfosByViewId(
                    "com.instagram.android:id/row_feed_view_group_buttons");

            if (postFooter == null || postFooter.isEmpty()) {
                Log.e(TAG, "PostFooter not found in StartLikesFollowing");
                this.instagramFollowerBotAutomation.performStaticScrollUp(this::StartLikesFollowing);
                return;
            }

            userAvatars = helperFunctions.findNodeByClassAndText(rootNode, "android.widget.TextView", "Liked by");
            if (userAvatars != null) {
                Log.v(TAG, "Found userAvatars going to enter to schedule through it");
                try {
                    if (userAvatars.isClickable() && userAvatars.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        handler.postDelayed(() -> {
                            scheduleLikersSectionCheck(rootNode);
                        }, 1500 + random.nextInt(1000));
                    } else {
                        this.instagramFollowerBotAutomation.getBoundsAndClick(userAvatars, () -> {
                            Log.d(TAG, "Clicked successfully on likersButtons Button bounds");
                            scheduleLikersSectionCheck(rootNode);
                        }, "Last", 1500, 2500);
                    }
                    return;
                } catch (Exception e) {
                    Log.e(TAG, "Exception while clicking userAvatars: " + e.getMessage(), e);
                }
            }

            Log.i(TAG, "Found PostFooter");
            footer = postFooter.get(postFooter.size() - 1);
            try {
                likersButtons = HelperFunctions.findNodesByClass(footer, "android.widget.Button");

                if (likersButtons == null || likersButtons.isEmpty()) {
                    Log.e(TAG, "likersButtons not found in PostFooter");
                    accountManager.BlockCurrentAccount();
                    this.instagramFollowerBotAutomation.handleNavigationByType();
                    return;
                }

                Log.i(TAG, "Found likersButtons");
                for (AccessibilityNodeInfo button : likersButtons) {
                    try {
                        CharSequence buttonText = button.getText();
                        if (buttonText != null && !buttonText.toString().isEmpty()) {
                            likersButton = button;
                            break;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error checking button text: " + e.getMessage(), e);
                    }
                }

                if (likersButton != null) {
                    Log.i(TAG, "Found first likersButtons with text");
                    try {
                        if (likersButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                            Log.i(TAG, "clicked first likersButtons with text directly");
                            handler.postDelayed(() -> {
                                scheduleLikersSectionCheck(rootNode);
                            }, 1500 + random.nextInt(1000));
                        } else {
                            Log.i(TAG, "Could Not click first likersButtons with text directly, going to click through bounds");
                            Rect bounds = new Rect();
                            likersButton.getBoundsInScreen(bounds);
                            this.instagramFollowerBotAutomation.clickOnBounds(bounds, () -> {
                                Log.d(TAG, "Clicked successfully on likersButtons Button bounds");
                                scheduleLikersSectionCheck(rootNode);
                            }, "Center", 2000, 1000);
                            return;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Exception clicking likersButton: " + e.getMessage(), e);
                    }
                } else {
                    Log.e(TAG, "likersButton is null in StartLikesFollowing");
                    accountManager.BlockCurrentAccount();
                    this.instagramFollowerBotAutomation.handleNavigationByType();
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception processing footer buttons: " + e.getMessage(), e);
                this.instagramFollowerBotAutomation.handleNavigationByType();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in handlePostFooterLikes: " + e.getMessage(), e);
            accountManager.BlockCurrentAccount();
            this.instagramFollowerBotAutomation.handleNavigationByType();
        } finally {
            // Recycle all nodes
            if (userAvatars != null) {
                userAvatars.recycle();
            }
            if (likersButton != null) {
                likersButton.recycle();
            }
            if (likersButtons != null) {
                for (AccessibilityNodeInfo button : likersButtons) {
                    if (button != null) {
                        button.recycle();
                    }
                }
            }
            if (footer != null) {
                footer.recycle();
            }
            if (postFooter != null) {
                for (AccessibilityNodeInfo node : postFooter) {
                    if (node != null) {
                        node.recycle();
                    }
                }
            }
        }
    }

    private void scheduleLikersSectionCheck(AccessibilityNodeInfo rootNode) {
        Log.i(TAG, "Entered scheduleLikersSectionCheck");
        if(instagramFollowerBotAutomation.shouldContinueAutomation()) return;

//        if (popUpHandler.handleOtherPopups(()->this.scheduleLikersSectionCheck(), null)) return;

        boolean outerdialogcheck = popUpHandler.checkForActionBlocker(()->{
            accountManager.BlockCurrentAccount();
            accountManager.setAccountLimitHit(true);
            this.instagramFollowerBotAutomation.handleNavigationByType();
        });

        if (outerdialogcheck) {
            Log.e(TAG, "outerdialogcheck in startProfileFollowing is true");
            return;
        }
        rootNode.refresh();
        handler.postDelayed(() -> {
            AccessibilityNodeInfo likesSectionHead = HelperFunctions.findNodeByResourceId(rootNode,
                    "com.instagram.android:id/bottom_sheet_drag_handle_frame");

            if (likesSectionHead == null) {
                Log.e(TAG, "likesSectionHead not found");
                navigateBackAndGetNextPost();
                return;
            }
            Log.i(TAG, "Found likesSectionHead");
            AccessibilityNodeInfo title = HelperFunctions.findNodeByResourceId(rootNode,
                    "com.instagram.android:id/title_text_view");

            if (title == null || title.getText().toString().contains("Comments")) {
                Log.e(TAG, "Post has no likes");
                helperFunctions.navigateBack();
                changePostIndex();
                return;
            }
            this.helperFunctions.dragSliderSection(likesSectionHead, this.instagramFollowerBotAutomation::startFollowing, this.helperFunctions);
        }, 1000 + random.nextInt(800));
    }
    public void changePostIndex() {
        accountManager.incrementCurrentAccountPostsDone();
        if (accountManager.getCurrentAccountCurrentColumn() == 3) {
            accountManager.setCurrentAccountCurrentColumn(1);
            accountManager.incrementCurrentAccountCurrentRow();
        } else {
            accountManager.incrementCurrentAccountCurrentColumn();
        }
    }
    public void navigateBackAndGetNextPost() {
        Log.i(TAG, "Entered navigateBackAndGetNextPost");
        if(instagramFollowerBotAutomation.shouldContinueAutomation()) return;
        changePostIndex();
        handler.postDelayed(() -> {
            helperFunctions.navigateBack();
            handler.postDelayed(() -> {
                helperFunctions.navigateBack();
                handler.postDelayed(this::getNextPost, 800 + random.nextInt(800));
            }, 800 + random.nextInt(800));
        }, 800 + random.nextInt(800));
    }
}
