package com.example.appilot.automations.InstagramFollowerBot;

import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;
import com.example.appilot.utils.HelperFunctions;

import java.util.List;
import java.util.Random;

public class Method3 {
    private static final String TAG = "method3";
    private final HelperFunctions helperFunctions;
    private final InstagramFollowerBotAutomation instagramFollowerBotAutomation;
    private final Handler handler;
    private final Random random;
    private final AccountManager accountManager;
    private final PopUpHandler popUpHandler;

    public Method3(InstagramFollowerBotAutomation instance, HelperFunctions helperFunctions, Handler handler, Random random, AccountManager manager, PopUpHandler popUpHandler) {
        this.helperFunctions = helperFunctions;
        this.instagramFollowerBotAutomation = instance;
        this.handler = handler;
        this.random = random;
        this.accountManager = manager;
        this.popUpHandler = popUpHandler;
    }

    public void StartLikesFollowing() {
        Log.i(TAG, "Entered StartLikesFollowing");
        if (this.instagramFollowerBotAutomation.shouldContinueAutomation()) {
            return;
        }

        if (popUpHandler.handleOtherPopups(()->this.StartLikesFollowing(), null)) return;

        AccessibilityNodeInfo rootNode = null;
        try {
            rootNode = this.helperFunctions.getRootInActiveWindow();
            if (rootNode == null) {
                Log.e(TAG, "Root node is null - in StartLikesFollowing");
                this.instagramFollowerBotAutomation.closeMyApp();
                handler.postDelayed(()->{
                    this.instagramFollowerBotAutomation.launchApp(()->this.instagramFollowerBotAutomation.ChangeAccount(this.instagramFollowerBotAutomation::callbackAccordingToType));
                }, 30000 + random.nextInt(20000));
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
            this.instagramFollowerBotAutomation.closeMyApp();
            handler.postDelayed(()->{
                this.instagramFollowerBotAutomation.launchApp(()->this.instagramFollowerBotAutomation.ChangeAccount(this.instagramFollowerBotAutomation::callbackAccordingToType));
            }, 30000 + random.nextInt(20000));
        } finally {
            if (rootNode != null) {
                rootNode.recycle();
            }
        }
    }
    private void handleLikerButtonClick(AccessibilityNodeInfo rootNode, AccessibilityNodeInfo likersBtn) {
        Log.i(TAG, "Entered handleLikerButtonClick");
        if (this.instagramFollowerBotAutomation.shouldContinueAutomation()) {
            return;
        }

        try {
            if (likersBtn.isClickable() && likersBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                Log.i(TAG, "Clicked likersBtn Directly");
                scheduleLikersSectionCheck(rootNode);
            } else {
                Log.i(TAG, "Could not Click likersBtn Directly, going to click through bounds");
                this.instagramFollowerBotAutomation.getBoundsAndClick(likersBtn, () -> {
                    Log.d(TAG, "Clicked successfully on likers Button bounds");
                    scheduleLikersSectionCheck(rootNode);
                }, "Center", 2000, 3000);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in handleLikerButtonClick: " + e.getMessage(), e);
            accountManager.BlockCurrentAccount();
            this.instagramFollowerBotAutomation.handleNavigationByType();
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
        if (this.instagramFollowerBotAutomation.shouldContinueAutomation()) {
            return;
        }

        try {
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
                AccessibilityNodeInfo likesSectionHead = null;
                AccessibilityNodeInfo title = null;

                try {
                    likesSectionHead = HelperFunctions.findNodeByResourceId(rootNode,
                            "com.instagram.android:id/bottom_sheet_drag_handle_frame");

                    if (likesSectionHead == null) {
                        Log.e(TAG, "likesSectionHead not found");
                        accountManager.BlockCurrentAccount();
                        this.instagramFollowerBotAutomation.handleNavigationByType();
                        return;
                    }

                    Log.i(TAG, "Found likesSectionHead");
                    title = HelperFunctions.findNodeByResourceId(rootNode,
                            "com.instagram.android:id/title_text_view");

                    if (title == null || title.getText().toString().contains("Comments")) {
                        Log.e(TAG, "Post has no likes");
                        accountManager.BlockCurrentAccount();
                        this.instagramFollowerBotAutomation.handleNavigationByType();
                        return;
                    }

                    this.helperFunctions.dragSliderSection(likesSectionHead, this.instagramFollowerBotAutomation::startFollowing, this.helperFunctions);
                } catch (Exception e) {
                    Log.e(TAG, "Exception in delayed section of scheduleLikersSectionCheck: " + e.getMessage(), e);
                    accountManager.BlockCurrentAccount();
                    this.instagramFollowerBotAutomation.handleNavigationByType();
                } finally {
                    if (likesSectionHead != null) {
                        likesSectionHead.recycle();
                    }
                    if (title != null) {
                        title.recycle();
                    }
                }
            }, 1000 + random.nextInt(800));
        } catch (Exception e) {
            Log.e(TAG, "Exception in scheduleLikersSectionCheck: " + e.getMessage(), e);
            accountManager.BlockCurrentAccount();
            this.instagramFollowerBotAutomation.handleNavigationByType();
        }
    }

}
