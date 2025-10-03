package com.example.appilot.automations.LinkedIn;

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

public class Profile {
    private static final String TAG = "LinkedInAutomation";
    private final Context context;
    private final Handler handler;
    private final Random random;
    private final PopUpHandler popUpHandler;
    private final MyAccessibilityService service;
    private HelperFunctions helperFunctions;
    private String Task_id = null;
    private String job_id = null;
    private int scroll = 0;
    private final int MAX_COUNT = 5;

    public Profile(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs) {
        this.context = service;
        this.service = service;
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();
        this.Task_id = taskid;
        this.job_id = jobid;
        this.popUpHandler = new PopUpHandler(this.service, this.handler, this.random, this.helperFunctions);
        this.helperFunctions = new HelperFunctions(context, Task_id, job_id);
    }
    public void findProfileOnHome() {
        Log.d(TAG, "Entering findProfileOnHome method");
        // Verify one more time that we have root access
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available in Profile-Home");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present", "error");
            return;
        }
        Log.d(TAG, "Root node available, proceeding with Profile-Home logic");
        String nodeId = "com.linkedin.android:id/me_launcher_container";
        AccessibilityNodeInfo Button = HelperFunctions.findNodeByResourceId(rootNode, nodeId);
        performProfileClickOnHome(Button);
        rootNode.recycle();
    }
    private void performProfileClickOnHome(AccessibilityNodeInfo Button) {
        handler.postDelayed(() -> {
            boolean isClicked = false;
            try {
                Log.d(TAG, "Found 'Profile-Home' button. Attempting click.");
                isClicked = Button.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            } catch (Exception e) {
                Log.e(TAG, "Error performing click action on Profile-Home button: " + e.getMessage());
            } finally {
                helperFunctions.safelyRecycleNode(Button);
            }
            if (isClicked) {
                Log.d(TAG, "Profile clicked on Home");
                handler.postDelayed(this::findProfileOnTab, 2000);
            } else {
                Log.e(TAG, "Failed to click on 'Profile-Home' button.");
                helperFunctions.cleanupAndExit("Failed to click 'Profile-Home' button", "error");
            }
        }, 2000);
    }
    public void findProfileOnTab() {
        Log.d(TAG, "Entering findProfileOnTab method");
        // Verify one more time that we have root access
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available Profile-Tab");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present", "error");
            return;
        }
        Log.d(TAG, "Root node available, proceeding with Profile-Tab logic");
        String nodeId = "com.linkedin.android:id/identity_mirror_component_profile_image";
        AccessibilityNodeInfo Button = HelperFunctions.findNodeByResourceId(rootNode, nodeId);
        performProfileClickOnTab(Button);
        rootNode.recycle();
    }
    private void performProfileClickOnTab(AccessibilityNodeInfo Button) {
        handler.postDelayed(() -> {
            boolean isClicked = false;
            try {
                Log.d(TAG, "Found 'Profile-Tab' button. Attempting click.");
                isClicked = Button.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            } catch (Exception e) {
                Log.e(TAG, "Error performing click action on Profile-Tab button: " + e.getMessage());
            } finally {
                helperFunctions.safelyRecycleNode(Button);
            }
            if (isClicked) {
                Log.d(TAG, "'Profile-Tab' button clicked successfully. Proceeding to next step...");
                    handler.postDelayed(()->{
                        helperFunctions.performScroll(0.8f,0.3f);
                        handler.postDelayed(this::onScroll, 1000);
                    },2000);
            } else {
                Log.e(TAG, "Failed to click on 'Profile-Tab' button.");
                helperFunctions.cleanupAndExit("Failed to click 'Profile-Tab' button", "error");
            }
        }, 2000);
    }
    private void onScroll() {
        scroll++;
        Log.d(TAG, "Scrolled down! Count: " + scroll + "/" + MAX_COUNT);
        Log.d(TAG, "Maximum number of scrolls : " + MAX_COUNT);
        if (scroll <= MAX_COUNT) {
            handler.postDelayed(() -> {
                Log.d(TAG, "Next Scroll... (" + (scroll + 1) + "/" + MAX_COUNT + ")");
                helperFunctions.performScroll(0.8f,0.3f);
                handler.postDelayed(this::onScroll, 1000);
            }, 2000);
        } else {
            Log.d(TAG, "Reached maximum Scroll (" + MAX_COUNT + ")");
            scroll = 0;
            handler.postDelayed(() -> {
                Log.d(TAG, "Waiting to Exit");
                //helperFunctions.cleanupAndExit("Scroll completed", "final");
                clickBackFromProfile();
            }, 2000);
        }
    }
    private void clickBackFromProfile() {
        Log.d(TAG, "Starting hierarchy navigation to click target element...");

        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickTargetElement", "error");
            return;
        }
        // Find parent node by resource ID
        String parentNodeId = "com.linkedin.android:id/profile_toolbar";
        AccessibilityNodeInfo parentNode = HelperFunctions.findNodeByResourceId(rootNode, parentNodeId);

        if (parentNode != null) {
            Log.d(TAG, "Found parent node with ID: " + parentNodeId);
            Log.d(TAG, "Parent has " + parentNode.getChildCount() + " children");
            // Navigate to target: Parent -> Child(0) -> Child(0)
            AccessibilityNodeInfo targetElement = navigateToBackFromProfile(parentNode);
            if (targetElement != null) {
                Log.d(TAG, "Found target element, attempting click...");
                performClick(targetElement);
            } else {
                Log.e(TAG, "Could not navigate to target element");
                helperFunctions.cleanupAndExit("Could not navigate to target element", "error");
            }
            parentNode.recycle();
        } else {
            Log.e(TAG, "Could not find parent node with ID: " + parentNodeId);
        }
        rootNode.recycle();
    }
    private AccessibilityNodeInfo navigateToBackFromProfile(AccessibilityNodeInfo parent) {
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
            AccessibilityNodeInfo targetElement = parent.getChild(0);
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
    // Perform click on the target element
    private void performClick(AccessibilityNodeInfo targetElement) {
        try {
            boolean clicked = false;
            // Method 1: Try accessibility click
            if (targetElement.isClickable()) {
                clicked = targetElement.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.d(TAG, "Accessibility click result: " + clicked);
            }
            if (clicked) {
                Log.d(TAG, "Target element clicked successfully!");
                handler.postDelayed(() -> {
                    helperFunctions.performScroll(0.8f,0.2f);
                    handler.postDelayed(()->{
                        helperFunctions.cleanupAndExit("On Home Page, scroll down completed.", "final");
                    },2000);
                }, 2000);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clicking target element: " + e.getMessage());
            helperFunctions.cleanupAndExit("Click the invitation tab", "final");
        } finally {
            helperFunctions.safelyRecycleNode(targetElement);
        }
    }
}
