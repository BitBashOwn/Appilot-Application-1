package com.example.appilot.automations.LinkedIn;

import android.os.Looper;
import android.util.Log;
import android.content.Context;
import android.os.Handler;
import android.view.accessibility.AccessibilityNodeInfo;
import android.graphics.Rect;
import java.util.List;
import java.util.Random;
import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;
import com.example.appilot.services.MyAccessibilityService;
import com.example.appilot.utils.HelperFunctions;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;

public class LikeComment {

    private static final String TAG = "LinkedInAutomation";
    private final Context context;
    private final Handler handler;
    private final Random random;
    private final PopUpHandler popUpHandler;
    private final MyAccessibilityService service;
    private HelperFunctions helperFunctions;
    private String Task_id = null;
    private String job_id = null;
    private int postLiked = 0;
    private static final int Max_posts=3;

    public LikeComment(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs) {
        this.context = service;
        this.service = service;
        this.Task_id = taskid;
        this.job_id = jobid;
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();
        this.popUpHandler = new PopUpHandler(this.service, this.handler, this.random, this.helperFunctions);
        this.helperFunctions = new HelperFunctions(context, Task_id, job_id);
    }

    public void startLiking(){
        handler.postDelayed(()->{helperFunctions.performScroll(0.3f,0.7f);
            handler.postDelayed(this::findResourceId, 1000);
            },1000);
    }
    public void findResourceId() {
        Log.d(TAG, "Entering clickButton function");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available in like post");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present", "error");
            return;
        }
        Log.d(TAG, "Root node available, proceeding with the like click");
        String nodeId = "com.linkedin.android:id/feed_social_actions_like";
        AccessibilityNodeInfo Button = HelperFunctions.findNodeByResourceId(rootNode, nodeId);
        if (Button != null){
            Log.d(TAG, "Found element with resource ID: " + nodeId);
            likeClick(Button);
        } else {
            Log.e(TAG, "Could not find element with resource ID: " + nodeId);
            rootNode.recycle();
            Log.e(TAG,"Remove the rootNodes" + nodeId);
            findId();
        }
    }
    private void likeClick(AccessibilityNodeInfo myNetworkButton) {
        handler.postDelayed(() -> {
            boolean isClicked = false;
            try {
                Log.d(TAG, "Found 'Like' button. Attempting click.");
                isClicked = myNetworkButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            } catch (Exception e) {
                Log.e(TAG, "Error performing click action on My Network button: " + e.getMessage());
            } finally {
                // Always recycle the node after use
                helperFunctions.safelyRecycleNode(myNetworkButton);
            }
            if (isClicked) {
                Log.d(TAG, "'Like' button clicked successfully. Proceeding to next step...");
                postLiked ++;
                // Wait for page to load before next action
                handler.postDelayed(() -> {
                    Log.d(TAG, "Page loaded. Performing scroll down to reveal next button...");
                    onLikeClicked();
                    //helperFunctions.cleanupAndExit("Click the like", "final");
                }, 2000);
            } else {
                Log.e(TAG, "Failed to click on 'Like' button.");
                helperFunctions.cleanupAndExit("Couldn't click like button", "error");
            }
        }, 2000);
    }
    public void findId() {
        Log.d(TAG, "Entering findId function in case of no simple like present");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available in like post");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, becuase no Root_node present", "error");
            return;
        }
        Log.d(TAG, "Root node available, proceeding with the like click");
        String nodeId = "com.linkedin.android:id/feed_item_update_card";
        AccessibilityNodeInfo Button = HelperFunctions.findNodeByResourceId(rootNode, nodeId);
        if (Button != null){
            Log.d(TAG, "Found element with resource ID: " + nodeId);
            performClickByGestures(Button);
        } else {
            Log.e(TAG, "Could not find element with resource ID: " + nodeId);
            rootNode.recycle();
            helperFunctions.cleanupAndExit("Not find nodes","error");
        }
    }
    private void performClickByGestures(AccessibilityNodeInfo element) {
        try {
            Rect bounds = new Rect();
            element.getBoundsInScreen(bounds);

            // Calculate center coordinates
            int centerX = bounds.centerX();
            int centerY = bounds.centerY();

            Log.d(TAG, "Post card bounds: " + bounds.toString());
            Log.d(TAG, "Card width: " + bounds.width() + ", height: " + bounds.height());
            Log.d(TAG, "Center coordinates: (" + centerX + ", " + centerY + ")");
            Log.d(TAG, "Card class: " + element.getClassName());
            Log.d(TAG, "Card resource ID: " + element.getViewIdResourceName());
            Log.d(TAG, "Card clickable: " + element.isClickable());

            handler.postDelayed(()->{performGestureClickOnElement(centerX, centerY);},2000);
        } catch (Exception e) {
            Log.e(TAG, "Error performing gesture click on post card: " + e.getMessage());
            helperFunctions.cleanupAndExit("Card is not clickable", "error");
        } finally {
            helperFunctions.safelyRecycleNode(element);
        }
    }
    private void performGestureClickOnElement(int x, int y) {
        Log.d(TAG, "Performing gesture click on post card at: (" + x + ", " + y + ")");

        Path clickPath = new Path();
        clickPath.moveTo(x, y);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, 200));
        try {
            MyAccessibilityService service = (MyAccessibilityService) context;
            boolean dispatched = service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    Log.d(TAG, "Post card gesture click completed successfully");
                }
                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    Log.e(TAG, "Post card gesture click was cancelled");
                }
            }, null);
            handler.postDelayed(this::clickLike,1000);
            Log.d(TAG, "Post card gesture click dispatch result: " + dispatched);
        } catch (Exception e) {
            Log.e(TAG, "Error in post card gesture click: " + e.getMessage());
        }
    }
    private void clickLike() {
        Log.d(TAG, "Going to click like button");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available for like button");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present", "error");
            return;
        }
        Log.d(TAG, "Root node available, proceeding with like click");

        String firstNodeId = "com.linkedin.android:id/media_viewer_react";
        AccessibilityNodeInfo likeButton = HelperFunctions.findNodeByResourceId(rootNode, firstNodeId);
        if (likeButton != null) {
            Log.d(TAG, "Found like button with first node ID: " + firstNodeId);
            checkLikeStateAndHandle(likeButton, firstNodeId);
        } else {
            String secondNodeId = "com.linkedin.android:id/feed_social_actions_like";
            AccessibilityNodeInfo likeButton2 = HelperFunctions.findNodeByResourceId(rootNode, secondNodeId);
            if (likeButton2 != null) {
                Log.d(TAG, "Found like button with second node ID: " + secondNodeId);
                checkLikeStateAndHandle(likeButton2, secondNodeId);
            } else {
                Log.e(TAG, "Neither node Id found, retrying ...");
                retryFindElement();
            }
        }
        rootNode.recycle();
    }
    private void checkLikeStateAndHandle(AccessibilityNodeInfo likeButton, String nodeId) {
        handler.postDelayed(() -> {
            try {
                // Store the current state before clicking
                String initialText = getButtonText(likeButton);
                String initialContentDesc = getButtonContentDescription(likeButton);

                Log.d(TAG, "Initial button text: " + initialText);
                Log.d(TAG, "Initial button content description: " + initialContentDesc);

                // Check if already liked using more comprehensive detection
                if (isButtonAlreadyLiked(initialText, initialContentDesc)) {
                    Log.d(TAG, "Post is already liked, proceeding to back button");
                    postLiked++; // Count as liked since it's already liked
                    resetRetryCount();
                    helperFunctions.safelyRecycleNode(likeButton);
                    handler.postDelayed(this::backClick, 1000);
                    return;
                }
                // If not liked, perform click and verify the action
                performLikeClickWithVerification(likeButton, nodeId, initialText, initialContentDesc);

            } catch (Exception e) {
                Log.e(TAG, "Error checking like button state: " + e.getMessage());
                helperFunctions.safelyRecycleNode(likeButton);
                retryFindElement();
            }
        }, 1000);
    }
    private boolean isButtonAlreadyLiked(String buttonText, String contentDesc) {
        // Check text content for "liked" indicators
        if (buttonText != null) {
            String text = buttonText.toLowerCase();
            if (text.contains("reacted") || text.contains("liked") ||
                    text.contains("you reacted") || text.contains("you liked") ||
                    text.contains("unlike") || text.contains("remove reaction")) {
                Log.d(TAG, "Button is already liked (detected from text: " + text + ")");
                return true;
            }
        }
        // Check content description for "liked" indicators
        if (contentDesc != null) {
            String desc = contentDesc.toLowerCase();
            if (desc.contains("reacted") || desc.contains("liked") ||
                    desc.contains("you reacted") || desc.contains("you liked") ||
                    desc.contains("unlike") || desc.contains("remove reaction")) {
                Log.d(TAG, "Button is already liked (detected from content description: " + desc + ")");
                return true;
            }
        }
        return false;
    }
    private void performLikeClickWithVerification(AccessibilityNodeInfo Button, String nodeId, String initialText, String initialContentDesc) {
        handler.postDelayed(() -> {
            boolean isClicked = false;
            try {
                Log.d(TAG, "Found button. Attempting click...");
                isClicked = Button.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            } catch (Exception e) {
                Log.e(TAG, "Error performing click action on button: " + e.getMessage());
            }

            if (isClicked) {
                Log.d(TAG, "Click action performed. Verifying if it was a like or unlike...");
                // Wait a bit for the UI to update, then verify the action
                handler.postDelayed(() -> {
                    verifyLikeAction(nodeId, initialText, initialContentDesc);
                }, 1500); // Give time for UI to update
            } else {
                Log.e(TAG, "Failed to click on like button.");
                helperFunctions.safelyRecycleNode(Button);
                retryFindElement();
            }
            helperFunctions.safelyRecycleNode(Button);
        }, 2000);
    }
    private void verifyLikeAction(String nodeId, String initialText, String initialContentDesc) {
        try {
            AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
            if (rootNode == null) {
                Log.e(TAG, "No root node available for verification");
                handler.postDelayed(this::backClick, 1000);
                return;
            }
            AccessibilityNodeInfo updatedButton = HelperFunctions.findNodeByResourceId(rootNode, nodeId);
            if (updatedButton != null) {
                String newText = getButtonText(updatedButton);
                String newContentDesc = getButtonContentDescription(updatedButton);

                Log.d(TAG, "After click - New text: " + newText);
                Log.d(TAG, "After click - New content description: " + newContentDesc);

                // Determine if it was a like or unlike action
                boolean wasAlreadyLiked = isButtonAlreadyLiked(initialText, initialContentDesc);
                boolean isNowLiked = isButtonAlreadyLiked(newText, newContentDesc);

                if (!wasAlreadyLiked && isNowLiked) {
                    Log.d(TAG, "Successfully liked the post");
                    postLiked++;
                } else if (wasAlreadyLiked && !isNowLiked) {
                    Log.d(TAG, "Post was already liked, click resulted in unlike. Re-clicking to like...");
                    // If we accidentally unliked, click again to like
                    handler.postDelayed(() -> {
                        boolean reLiked = updatedButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        if (reLiked) {
                            Log.d(TAG, "Successfully re-liked the post");
                            postLiked++;
                        }
                        helperFunctions.safelyRecycleNode(updatedButton);
                    }, 1000);
                    rootNode.recycle();
                    return;
                } else {
                    Log.d(TAG, "Like state unchanged or unclear, assuming successful");
                    postLiked++;
                }
                helperFunctions.safelyRecycleNode(updatedButton);
            } else {
                Log.w(TAG, "Could not find button for verification, assuming successful");
                postLiked++;
            }
            rootNode.recycle();
            resetRetryCount();
            handler.postDelayed(this::backClick, 1000);
        } catch (Exception e) {
            Log.e(TAG, "Error during verification: " + e.getMessage());
            postLiked++; // Assume successful to continue flow
            resetRetryCount();
            handler.postDelayed(this::backClick, 1000);
        }
    }
    private String getButtonText(AccessibilityNodeInfo button) {
        try {
            CharSequence text = button.getText();
            return text != null ? text.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }
    private String getButtonContentDescription(AccessibilityNodeInfo button) {
        try {
            CharSequence desc = button.getContentDescription();
            return desc != null ? desc.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }
    private void backClick() {
        Log.d(TAG, "Going to click back(<-) button");
        // Verify one more time that we have root access
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available for back button");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, becuase no Root_node present", "error");
            return;
        }
        Log.d(TAG, "Root node available, proceeding with <- click");

        String firstNodeId = "com.linkedin.android:id/media_viewer_close_button";
        AccessibilityNodeInfo backButton = HelperFunctions.findNodeByResourceId(rootNode, firstNodeId);
        if (backButton != null) {
            Log.d(TAG, "Found <- button with first node ID: " + firstNodeId);
            handler.postDelayed(()->{
                performBackClick(backButton);
                resetRetryCount();
                }, 2000);
        } else {
            String secondNodeId = "android.widget.ImageButton";
            AccessibilityNodeInfo backButton2 = HelperFunctions.findNodeByResourceId(rootNode, secondNodeId);
            if (backButton2 != null) {
                Log.d(TAG, "Found <- button with second node ID: " + secondNodeId);
                handler.postDelayed(()->{performBackClick(backButton2);}, 2000);
            } else {
                Log.e(TAG, "Neither node Id found, retrying ...");
                helperFunctions.cleanupAndExit("Not found the back button", "error");
            }
        }
        rootNode.recycle();
    }
    private void performBackClick(AccessibilityNodeInfo Button) {
        handler.postDelayed(() -> {
            boolean isClicked = false;
            try {
                Log.d(TAG, "Found button. Attempting click...");
                isClicked = Button.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            } catch (Exception e) {
                Log.e(TAG, "Error performing click action on button: " + e.getMessage());
            } finally {
                helperFunctions.safelyRecycleNode(Button);
            }
            if (isClicked) {
                Log.d(TAG, "Button clicked successfully. Proceeding to next step...");
                resetRetryCount();
                handler.postDelayed(this::onLikeClicked, 1000);
            } else {
                Log.e(TAG, "Failed to click on 'My Network' button.");
                retryFindElement();
            }
        }, 2000);
    }
    private int retryCount = 0;
    private static final int MAX_RETRIES = 5;
    private void retryFindElement() {
        Log.d(TAG, "Retrying to find post card after scroll...");

        if (retryCount >= MAX_RETRIES) {
            Log.e(TAG, "Max retries reached. Exiting the app.");
            helperFunctions.cleanupAndExit("Failed to find after multiple retries", "error");
            return;
        }
        handler.postDelayed(() -> {helperFunctions.performScroll(0.65f, 0.35f);
            handler.postDelayed(this::findResourceId, 2000);
            retryCount++;
            Log.d(TAG, "Retry attempt " + retryCount);
        }, 1000);
    }
    private void resetRetryCount() {
        retryCount = 0;
        Log.d(TAG, "Retry count reset to 0.");
    }

    private void onLikeClicked() {
        Log.d(TAG, "Connect button successfully clicked! Count: " + postLiked + "/" + Max_posts);
        Log.d(TAG,"Number of connects passed by user : " + Max_posts);
        if (postLiked < Max_posts) {
                handler.postDelayed(this::startLiking, 1000);
        } else {
            Log.d(TAG, "Reached maximum Connect clicks (" + Max_posts + "). Looping back to sendConnection...");
            postLiked = 0;
            handler.postDelayed(() -> {
                Log.d(TAG, "Going back to HomePage...");
                helperFunctions.cleanupAndExit("Have liked the Required Posts", "final");
            }, 2000);
        }
    }

    public void findLoadMore() {
        Log.d(TAG, "Entering loadMore function");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available in like post");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present", "error");
            return;
        }
        Log.d(TAG, "Root node available, proceeding with the loadMore click");
        String nodeId = "com.linkedin.android:id/infra_load_more_footer_button";
        AccessibilityNodeInfo Button = HelperFunctions.findNodeByResourceId(rootNode, nodeId);
        if (Button != null){
            Log.d(TAG, "Found element with resource ID: " + nodeId);
            loadMoreClick(Button);
        } else {
            Log.e(TAG, "Could not find element with resource ID: " + nodeId);
            rootNode.recycle();
            Log.e(TAG,"Remove the rootNodes" + nodeId);
            findResourceId();
        }
    }
    private void loadMoreClick(AccessibilityNodeInfo Button) {
        handler.postDelayed(() -> {
            boolean isClicked = false;
            try {
                Log.d(TAG, "Found 'LoadMore' button. Attempting click.");
                isClicked = Button.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            } catch (Exception e) {
                Log.e(TAG, "Error performing click action on button: " + e.getMessage());
            } finally {
                // Always recycle the node after use
                helperFunctions.safelyRecycleNode(Button);
            }
            if (isClicked) {
                Log.d(TAG, "'Load More' button clicked successfully. Proceeding to next step...");
                handler.postDelayed(() -> {
                    Log.d(TAG, "Page loaded. Performing scroll down to reveal next button...");
                    helperFunctions.performScroll(0.8f,0.2f);
                    handler.postDelayed(this::findResourceId,1000);
                    //helperFunctions.cleanupAndExit("Click the like", "final");
                }, 2000);
            } else {
                Log.e(TAG, "Failed to click on 'Like' button.");
                helperFunctions.cleanupAndExit("Couldn't click like button", "error");
            }
        }, 2000);
    }

}