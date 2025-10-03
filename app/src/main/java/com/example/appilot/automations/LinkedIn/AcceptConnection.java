package com.example.appilot.automations.LinkedIn;

import android.accessibilityservice.AccessibilityService;
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

public class AcceptConnection {
    private static final String TAG = "LinkedInAutomation";
    private final Context context;
    private final Handler handler;
    private final Random random;
    private final PopUpHandler popUpHandler;
    private final MyAccessibilityService service;
    private HelperFunctions helperFunctions;
    private String Task_id = null;
    private String job_id = null;
    private int MAX_CONNECT_ACCEPT;

    public AcceptConnection(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs, int numberOfConnects) {
        this.context = service;
        this.service = service;
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();
        this.Task_id = taskid;
        this.job_id = jobid;
        this.popUpHandler = new PopUpHandler(this.service, this.handler, this.random, this.helperFunctions);
        this.helperFunctions = new HelperFunctions(context, Task_id, job_id);
        this.MAX_CONNECT_ACCEPT = numberOfConnects;
    }

    public void acceptRequest() {
        Log.d(TAG, "Entering sendConnection method");
        // Verify one more time that we have root access
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available in sendConnection");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present", "error");
            return;
        }
        Log.d(TAG, "Root node available, proceeding with connection logic");
        String nodeId = "com.linkedin.android:id/tab_relationships";
        AccessibilityNodeInfo myNetworkButton = HelperFunctions.findNodeByResourceId(rootNode, nodeId);
        performMyNetworkClick(myNetworkButton);
        rootNode.recycle();
    }

    private void performMyNetworkClick(AccessibilityNodeInfo myNetworkButton) {
        handler.postDelayed(() -> {
            boolean isClicked = false;
            try {
                Log.d(TAG, "Found 'My Network' button. Attempting click.");
                isClicked = myNetworkButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            } catch (Exception e) {
                Log.e(TAG, "Error performing click action on My Network button: " + e.getMessage());
            } finally {
                // Always recycle the node after use
                helperFunctions.safelyRecycleNode(myNetworkButton);
            }
            if (isClicked) {
                Log.d(TAG, "'My Network' button clicked successfully. Proceeding to next step...");
                // Wait for page to load before next action
                handler.postDelayed(() -> {
                    Log.d(TAG, "Page loaded. Performing scroll down to reveal next button...");
                    //helperFunctions.cleanupAndExit("Click the 'My Network' button", "final");
                    clickInvitations();
                }, 5000);
            } else {
                Log.e(TAG, "Failed to click on 'My Network' button.");
                helperFunctions.cleanupAndExit("Failed to click My Network button", "error");
            }
        }, 2000);
    }

    private void clickInvitations() {
        Log.d(TAG, "Starting hierarchy navigation to click target element...");

        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickTargetElement", "error");
            return;
        }
        // Find parent node by resource ID
        String parentNodeId = "sdui:lazyColumn";
        AccessibilityNodeInfo parentNode = HelperFunctions.findNodeByResourceId(rootNode, parentNodeId);

        if (parentNode != null) {
            Log.d(TAG, "Found parent node with ID: " + parentNodeId);
            Log.d(TAG, "Parent has " + parentNode.getChildCount() + " children");
            // Navigate to target: Parent -> Child(0) -> Child(0)
            AccessibilityNodeInfo targetElement = navigateToInvitation(parentNode);
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

    // Navigate through the hierarchy: Parent -> Child(0) -> Child(0)
    private AccessibilityNodeInfo navigateToInvitation(AccessibilityNodeInfo parent) {
        try {
            Log.d(TAG, "Navigating: Parent -> Child(0) -> Child(0)");

            // Step 1: Get first child (0)
            if (parent.getChildCount() < 1) {
                Log.e(TAG, "Parent has no children");
                return null;
            }
            AccessibilityNodeInfo child0 = parent.getChild(0);
            if (child0 == null) {
                Log.e(TAG, "Could not get child(0)");
                return null;
            }
            Log.d(TAG, "Got child(0), has " + child0.getChildCount() + " children");

            // Step 2: Get child(0) of child(0)
            if (child0.getChildCount() < 1) {
                Log.e(TAG, "Child(0) has no children");
                child0.recycle();
                return null;
            }
            AccessibilityNodeInfo child0_0 = child0.getChild(0);
            child0.recycle();
            if (child0_0 == null) {
                Log.e(TAG, "Could not get child(0)->child(0)");
                return null;
            }
            Log.d(TAG, "Got child(0)->child(0), has " + child0_0.getChildCount() + " children");

            AccessibilityNodeInfo targetElement = child0_0.getChild(0);
            child0_0.recycle();

            if (targetElement == null) {
                Log.e(TAG, "Could not get target element");
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
            // Method 2: If accessibility click fails, try gesture click
            if (!clicked) {
                Log.d(TAG, "Accessibility click failed, trying gesture click...");
                Rect bounds = new Rect();
                targetElement.getBoundsInScreen(bounds);

                int clickX = bounds.centerX();
                int clickY = bounds.centerY();

                Log.d(TAG, "Gesture click coordinates: (" + clickX + ", " + clickY + ")");
                performGestureClick(clickX, clickY);
                clicked = true;
            }
            if (clicked) {
                Log.d(TAG, "Target element clicked successfully!");
                handler.postDelayed(() -> {
                    clickAccept();
                }, 2000);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clicking target element: " + e.getMessage());
            helperFunctions.cleanupAndExit("Click the invitation tab", "error");
        } finally {
            helperFunctions.safelyRecycleNode(targetElement);
        }
    }

    private void performGestureClick(int x, int y) {
        Log.d(TAG, "Performing gesture click at: " + x + ", " + y);
        Path clickPath = new Path();
        clickPath.moveTo(x, y);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, 100));
        try {
            MyAccessibilityService service = (MyAccessibilityService) context;
            boolean dispatched = service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    Log.d(TAG, "Gesture click completed");
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    Log.e(TAG, "Gesture click cancelled");
                }
            }, null);
            Log.d(TAG, "Gesture click dispatch result: " + dispatched);
        } catch (Exception e) {
            Log.e(TAG, "Error in gesture click: " + e.getMessage());
        }
    }

    private void clickAccept() {
        Log.d(TAG, "Starting hierarchy navigation to click target element...");

        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickTargetElement", "error");
            return;
        }
        // Find parent node by resource ID
        String parentNodeId = "sdui:lazyColumn";
        AccessibilityNodeInfo parentNode = HelperFunctions.findNodeByResourceId(rootNode, parentNodeId);

        if (parentNode != null) {
            Log.d(TAG, "Found parent node with ID: " + parentNodeId);
            Log.d(TAG, "Parent has " + parentNode.getChildCount() + " children");
            // Navigate to target: Parent -> Child(0) -> Child(0)
            AccessibilityNodeInfo targetElement = navigateToAccept(parentNode);
            if (targetElement != null) {
                Log.d(TAG, "Found target element, attempting click...");
                performAcceptClick(targetElement);
            } else {
                Log.e(TAG, "Could not navigate to target element");
                helperFunctions.cleanupAndExit("Could not navigate to target element for accept", "error");
            }
            parentNode.recycle();
        } else {
            Log.e(TAG, "Could not find parent node with ID: " + parentNodeId);
        }
        rootNode.recycle();
    }

    private AccessibilityNodeInfo navigateToAccept(AccessibilityNodeInfo parent) {
        try {
            Log.d(TAG, "Attempting to navigate to accept button");
            if (parent == null) {
                Log.e(TAG, "Parent is null");
                return null;
            }
            // Try Case 1 first: Parent -> Child(1) -> Child(0) -> Child(6/7)
            AccessibilityNodeInfo case1Result = tryCase1Navigation(parent);
            if (case1Result != null) {
                Log.d(TAG, "Successfully found target using Case 1 navigation");
                return case1Result;
            }
            // If Case 1 fails, try Case 2: Parent -> Child(0) -> Child(1) -> Child(0) -> Child(5/6)
            Log.d(TAG, "Case 1 failed, trying Case 2 navigation");
            AccessibilityNodeInfo case2Result = tryCase2Navigation(parent);
            if (case2Result != null) {
                Log.d(TAG, "Successfully found target using Case 2 navigation");
                return case2Result;
            }

            Log.e(TAG, "Both navigation cases failed");
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to target: " + e.getMessage());
            return null;
        }
    }

    private AccessibilityNodeInfo tryCase1Navigation(AccessibilityNodeInfo parent) {
        try {
            Log.d(TAG, "Case 1: Navigating Parent -> Child(1) -> Child(0) -> Child(6/7)");

            if (parent.getChildCount() <= 1) {
                Log.d(TAG, "Case 1: Parent does not have enough children");
                return null;
            }

            AccessibilityNodeInfo child1 = parent.getChild(1);
            if (child1 == null || child1.getChildCount() < 1) {
                Log.d(TAG, "Case 1: child(1) is null or has no children");
                return null;
            }

            AccessibilityNodeInfo child1_0 = child1.getChild(0);
            child1.recycle();
            if (child1_0 == null) {
                Log.d(TAG, "Case 1: child(1)->child(0) is null");
                return null;
            }

            int subChildCount = child1_0.getChildCount();
            Log.d(TAG, "Case 1: child(1)->child(0) has " + subChildCount + " sub-children");

            if (subChildCount < 6) {
                Log.d(TAG, "Case 1: child(1)->child(0) does not have enough children. Has: " + subChildCount + " (need at least 6)");
                child1_0.recycle();
                return null;
            }

            int targetIndex;
            if (subChildCount == 7) {
                targetIndex = 6; // 7th child (index 6)
                Log.d(TAG, "Case 1: Sub-child count is 7, selecting 7th child (index 6)");
            } else if (subChildCount >= 8) {
                targetIndex = 7; // 8th child (index 7)
                Log.d(TAG, "Case 1: Sub-child count is " + subChildCount + " (>=8), selecting 8th child (index 7)");
            } else {
                Log.d(TAG, "Case 1: Unexpected sub-child count: " + subChildCount);
                child1_0.recycle();
                return null;
            }

            AccessibilityNodeInfo targetButton = child1_0.getChild(targetIndex);
            child1_0.recycle();
            if (targetButton == null) {
                Log.d(TAG, "Case 1: child(1)->child(0)->child(" + targetIndex + ") is null");
                return null;
            }
            // Log target element details
            Log.d(TAG, "Case 1: Found target element:");
            Log.d(TAG, "Class: " + targetButton.getClassName());
            Log.d(TAG, "Text: " + targetButton.getText());
            Log.d(TAG, "Clickable: " + targetButton.isClickable());

            Rect bounds = new Rect();
            targetButton.getBoundsInScreen(bounds);
            Log.d(TAG, "Bounds: " + bounds);

            return targetButton;

        } catch (Exception e) {
            Log.d(TAG, "Case 1: Exception occurred: " + e.getMessage());
            return null;
        }
    }

    private AccessibilityNodeInfo tryCase2Navigation(AccessibilityNodeInfo parent) {
        try {
            Log.d(TAG, "Case 2: Navigating Parent -> Child(0) -> Child(1) -> Child(0) -> Child(5/6)");

            if (parent.getChildCount() < 1) {
                Log.d(TAG, "Case 2: Parent has no children");
                return null;
            }

            AccessibilityNodeInfo child0 = parent.getChild(0);
            if (child0 == null || child0.getChildCount() <= 1) {
                Log.d(TAG, "Case 2: child(0) is null or does not have enough children");
                return null;
            }

            AccessibilityNodeInfo child0_1 = child0.getChild(1);
            if (child0_1 == null || child0_1.getChildCount() < 1) {
                Log.d(TAG, "Case 2: child(0)->child(1) is null or has no children");
                child0.recycle();
                return null;
            }

            AccessibilityNodeInfo child0_1_0 = child0_1.getChild(0);
            child0.recycle();
            child0_1.recycle();
            if (child0_1_0 == null) {
                Log.d(TAG, "Case 2: child(0)->child(1)->child(0) is null");
                return null;
            }

            int subChildCount = child0_1_0.getChildCount();
            Log.d(TAG, "Case 2: child(0)->child(1)->child(0) has " + subChildCount + " sub-children");

            if (subChildCount < 5) {
                Log.d(TAG, "Case 2: child(0)->child(1)->child(0) does not have enough children. Has: " + subChildCount + " (need at least 5)");
                child0_1_0.recycle();
                return null;
            }

            int targetIndex;
            if (subChildCount == 6) {
                targetIndex = 5; // 6th child (index 5)
                Log.d(TAG, "Case 2: Sub-child count is 6, selecting 6th child (index 5)");
            } else if (subChildCount >= 7) {
                targetIndex = 6; // 7th child (index 6)
                Log.d(TAG, "Case 2: Sub-child count is " + subChildCount + " (>=7), selecting 7th child (index 6)");
            } else {
                Log.d(TAG, "Case 2: Unexpected sub-child count: " + subChildCount);
                child0_1_0.recycle();
                return null;
            }
            AccessibilityNodeInfo targetButton = child0_1_0.getChild(targetIndex);
            child0_1_0.recycle();
            if (targetButton == null) {
                Log.d(TAG, "Case 2: child(0)->child(1)->child(0)->child(" + targetIndex + ") is null");
                return null;
            }
            // Log target element details
            Log.d(TAG, "Case 2: Found target element:");
            Log.d(TAG, "Class: " + targetButton.getClassName());
            Log.d(TAG, "Text: " + targetButton.getText());
            Log.d(TAG, "Clickable: " + targetButton.isClickable());

            Rect bounds = new Rect();
            targetButton.getBoundsInScreen(bounds);
            Log.d(TAG, "Bounds: " + bounds);

            return targetButton;
        } catch (Exception e) {
            Log.d(TAG, "Case 2: Exception occurred: " + e.getMessage());
            return null;
        }
    }

    private void performAcceptClick(AccessibilityNodeInfo targetElement) {
        try {
            boolean clicked = false;
            // Method 1: Try accessibility click
            if (targetElement.isClickable()) {
                clicked = targetElement.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.d(TAG, "Accessibility click result: " + clicked);
            }
            // Method 2: If accessibility click fails, try gesture click
            if (!clicked) {
                Log.d(TAG, "Accessibility click failed, trying gesture click...");
                Rect bounds = new Rect();
                targetElement.getBoundsInScreen(bounds);

                int clickX = bounds.centerX();
                int clickY = bounds.centerY();

                Log.d(TAG, "Gesture click coordinates: (" + clickX + ", " + clickY + ")");
                performGestureClick(clickX, clickY);
                clicked = true;
            }
            if (clicked) {
                Log.d(TAG, "Target element clicked successfully!");
                handler.postDelayed(() -> {
                    clickCross();
                }, 2000);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clicking target element: " + e.getMessage());
            helperFunctions.cleanupAndExit("Click the invitation tab", "final");
        } finally {
            helperFunctions.safelyRecycleNode(targetElement);
        }
    }

    private void clickCross() {
        Log.d(TAG, "Starting hierarchy navigation to click target element...");

        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickTargetElement", "error");
            return;
        }
        // Find parent node by resource ID
        String parentNodeId = "sdui:lazyColumn";
        AccessibilityNodeInfo parentNode = HelperFunctions.findNodeByResourceId(rootNode, parentNodeId);

        if (parentNode != null) {
            Log.d(TAG, "Found parent node with ID: " + parentNodeId);
            Log.d(TAG, "Parent has " + parentNode.getChildCount() + " children");
            // Navigate to target: Parent -> Child(0) -> Child(0)
            AccessibilityNodeInfo targetElement = navigateToCross(parentNode);
            if (targetElement != null) {
                Log.d(TAG, "Found target element, attempting click...");
                performCrossClick(targetElement);
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

    private AccessibilityNodeInfo navigateToCross(AccessibilityNodeInfo parent) {
        try {
            Log.d(TAG, "Navigating: Parent -> Child(1) -> Child(0) -> Child(3)");
            Log.d(TAG, "Parent has " + parent.getChildCount() + " children");

            // Step 1: Get Child(1)
            if (parent == null || parent.getChildCount() <= 1) {
                Log.e(TAG, "Parent is null or does not have enough children. Has: " + (parent != null ? parent.getChildCount() : "null"));
                return null;
            }

            AccessibilityNodeInfo child1 = parent.getChild(1);
            if (child1 == null) {
                Log.e(TAG, "Child(1) is null");
                return null;
            }
            Log.d(TAG, "Got Child(1), has " + child1.getChildCount() + " children");

            // Step 2: Get Child(0) of Child(1)
            if (child1.getChildCount() < 1) {
                Log.e(TAG, "Child(1) has no children. Has: " + child1.getChildCount());
                child1.recycle();
                return null;
            }

            AccessibilityNodeInfo child1_0 = child1.getChild(0);
            child1.recycle();
            if (child1_0 == null) {
                Log.e(TAG, "Child(1)->Child(0) is null");
                return null;
            }
            Log.d(TAG, "Got Child(1)->Child(0), has " + child1_0.getChildCount() + " children");

            // Step 3: Get target child dynamically based on child count
            int subChildCount = child1_0.getChildCount();
            Log.d(TAG, "child1_0 has " + subChildCount + " sub-children");

            if (subChildCount < 4) {
                Log.e(TAG, "Child(1)->Child(0) doesn't have enough children. Has: " + subChildCount + " (need at least 4)");
                child1_0.recycle();
                return null;
            }
            // Determine which child to select based on count
            int targetIndex;
            if (subChildCount == 4) {
                targetIndex = 3; // 4th child (index 3)
                Log.d(TAG, "Sub-child count is 4, selecting child at index 3");
            } else if (subChildCount == 5) {
                targetIndex = 4; // 5th child (index 4)
                Log.d(TAG, "Sub-child count is 5, selecting child at index 4");
            } else {
                // For any other count (>5), default to index 4
                targetIndex = 4;
                Log.d(TAG, "Sub-child count is " + subChildCount + " (>5), defaulting to child at index 4");
            }

            AccessibilityNodeInfo targetButton = child1_0.getChild(targetIndex);
            child1_0.recycle();

            if (targetButton == null) {
                Log.e(TAG, "Target button at index " + targetIndex + " is null");
                return null;
            }
            // Log target element details
            Log.d(TAG, "Found target element:");
            Log.d(TAG, "Class: " + targetButton.getClassName());
            Log.d(TAG, "Text: " + targetButton.getText());
            Log.d(TAG, "Clickable: " + targetButton.isClickable());

            Rect bounds = new Rect();
            targetButton.getBoundsInScreen(bounds);
            Log.d(TAG, "Bounds: " + bounds);
            return targetButton;
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to target: " + e.getMessage());
            return null;
        }
    }

    private void performCrossClick(AccessibilityNodeInfo targetElement) {
        try {
            boolean clicked = false;
            // Method 1: Try accessibility click
            if (targetElement.isClickable()) {
                clicked = targetElement.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.d(TAG, "Accessibility click result: " + clicked);
            }
            // Method 2: If accessibility click fails, try gesture click
            if (!clicked) {
                Log.d(TAG, "Accessibility click failed, trying gesture click...");
                Rect bounds = new Rect();
                targetElement.getBoundsInScreen(bounds);

                int clickX = bounds.centerX();
                int clickY = bounds.centerY();

                Log.d(TAG, "Gesture click coordinates: (" + clickX + ", " + clickY + ")");
                performGestureClick(clickX, clickY);
                clicked = true;
            }
            if (clicked) {
                Log.d(TAG, "Target element clicked successfully!");
                handler.postDelayed(() -> {
                    onConnectClicked();
                }, 2000);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clicking target element: " + e.getMessage());
            helperFunctions.cleanupAndExit("Click the invitation tab", "final");
        } finally {
            helperFunctions.safelyRecycleNode(targetElement);
        }
    }

    private int connectClickCount = 0;

    private void onConnectClicked() {
        connectClickCount++;
        Log.d(TAG, "Connect button successfully clicked! Count: " + connectClickCount + "/" + MAX_CONNECT_ACCEPT);
        Log.d(TAG, "Number of connects passed by user : " + MAX_CONNECT_ACCEPT);
        if (connectClickCount < MAX_CONNECT_ACCEPT) {
            handler.postDelayed(() -> {
                Log.d(TAG, "Looking for next Accept button... (" + (connectClickCount + 1) + "/" + MAX_CONNECT_ACCEPT + ")");
                handler.postDelayed(this::clickAccept, 1000);
                helperFunctions.performScroll(0.3f, 0.7f);
            }, 2000);
        } else {
            Log.d(TAG, "Reached maximum Connect clicks (" + MAX_CONNECT_ACCEPT + "). Looping back to sendConnection...");
            connectClickCount = 0;
            handler.postDelayed(() -> {
                Log.d(TAG, "Waiting to Exit");
                helperFunctions.cleanupAndExit("Click the invitation tab", "final");
            }, 2000);
        }
    }
}


