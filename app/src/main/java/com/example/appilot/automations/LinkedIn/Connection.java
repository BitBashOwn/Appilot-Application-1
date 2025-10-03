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

public class Connection {
    private static final String TAG = "LinkedInAutomation";
    private final Context context;
    private final Handler handler;
    private final Random random;
    private final PopUpHandler popUpHandler;
    private final MyAccessibilityService service;
    private HelperFunctions helperFunctions;
    private String Task_id = null;
    private String job_id = null;
    private boolean sendConnectionRequests;
    private final boolean likePosts;
    private final LikeComment likeComment;
    private final int MAX_CONNECT_CLICKS;

    public Connection(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs,boolean sendConnectionRequests, boolean likePosts, int numberOfConnects) {
        this.context = service;
        this.service = service;
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();
        this.Task_id = taskid;
        this.job_id = jobid;
        this.popUpHandler = new PopUpHandler(this.service, this.handler, this.random, this.helperFunctions);
        this.helperFunctions = new HelperFunctions(context, Task_id, job_id);
        this.sendConnectionRequests = sendConnectionRequests;
        this.likeComment = new LikeComment(service, Task_id, job_id, AccountInputs);
        this.likePosts = likePosts;
        this.MAX_CONNECT_CLICKS = numberOfConnects;
    }
    // Clicking the "My Network" button on the home page
    public void sendConnection() {
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
                    helperFunctions.performScroll(0.65f,0.30f);
                    // After scroll, wait a moment then loop back to sendConnection
                    handler.postDelayed(() -> {
                        Log.d(TAG, "Scroll completed.");
                        findAndClickSeeAllButton();
                    }, 5000);
                }, 5000);
            } else {
                Log.e(TAG, "Failed to click on 'My Network' button.");
                helperFunctions.cleanupAndExit("Failed to click My Network button", "error");
            }
        }, 2000);
    }
    // Clicking the "Show all" button on the "My Network Screen"
    private void findAndClickSeeAllButton() {
        Log.d(TAG, "Searching for 'Show All' button...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available for finding See All button");
            helperFunctions.cleanupAndExit("No root node available", "error");
            return;
        }
        AccessibilityNodeInfo seeAllButton = findNodeByText(rootNode, "Show all");
        if (seeAllButton != null) {
            Log.d(TAG, "Found 'See All' button, attempting to click...");
            performGestureClickOnButton(seeAllButton);
        } else {
            Log.e(TAG, "Could not find 'See All' button");
            rootNode.recycle();
            // Try scrolling a bit more and search again
            retryFindSeeAllButton();
        }
        rootNode.recycle();
    }
    private AccessibilityNodeInfo findNodeByText(AccessibilityNodeInfo node, String text) {
        if (node == null) return null;
        CharSequence nodeText = node.getText();
        if (nodeText != null && nodeText.toString().toLowerCase().contains(text.toLowerCase())) {
            Log.d(TAG, "Found exact text match: '" + nodeText + "'");
            return node;
        }
        // Search children
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                AccessibilityNodeInfo result = findNodeByText(child, text);
                if (result != null) {
                    child.recycle();
                    return result;
                }
                child.recycle();
            }
        }
        return null;
    }
    private void performGestureClickOnButton(AccessibilityNodeInfo button) {
        try {
            // Get button bounds
            Rect bounds = new Rect();
            button.getBoundsInScreen(bounds);
            int clickX = bounds.centerX();
            int clickY = bounds.centerY();
            Log.d(TAG, "Button bounds: " + bounds.toString());
            Log.d(TAG, "Gesture click coordinates: (" + clickX + ", " + clickY + ")");
            Log.d(TAG, "Button text: " + button.getText());
            Log.d(TAG, "Button class: " + button.getClassName());
            // Perform gesture click
            performGestureClick(clickX, clickY);
            // Wait and then proceed to next step
            handler.postDelayed(() -> {
                Log.d(TAG, "See All button gesture click completed. Proceeding to next step...");
                onSeeAllClicked();
            }, 2000);
        } catch (Exception e) {
            Log.e(TAG, "Error performing gesture click on See All button: " + e.getMessage());
            retryFindSeeAllButton();
        } finally {
            helperFunctions.safelyRecycleNode(button);
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
    private void retryFindSeeAllButton() {
        Log.d(TAG, "Retrying to find See All button...");
        // Scroll down a little more to reveal the button
        handler.postDelayed(() -> {
            helperFunctions.performScroll(0.65f,0.30f);
            // Try finding the button again after scroll
            handler.postDelayed(this::findAndClickSeeAllButton, 1000);
        }, 1000);
    }
    private void onSeeAllClicked() {
        Log.d(TAG, "See All button successfully clicked!");
        handler.postDelayed(() -> {
            Log.d(TAG, "Looping back to sendConnection after See All click...");
            findAndClickConnectButton();
        }, 2000);
    }
    // Starting clicking the connect and cross button
    private void findAndClickConnectButton() {
        Log.d(TAG, "Attempting hierarchy-based Connect button search...");
        handler.postDelayed(this::findAndClickConnectButtonByHierarchy, 1000);
        //findAndClickConnectButtonByHierarchy();
    }
    // Navigate through hierarchy to find the Connect button
    private void findAndClickConnectButtonByHierarchy() {
        Log.d(TAG, "Searching for Connect button using hierarchy navigation...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            return;
        }
        // Find the clickable parent element (the card container)
        AccessibilityNodeInfo clickableParent = findClickableParentCard(rootNode);
        if (clickableParent != null) {
            Log.d(TAG, "Found clickable parent card, using dynamic navigation...");

            debugDynamicNavigation(clickableParent);
            // Navigate to 7th child -> 3rd sub-child for Connect button
            AccessibilityNodeInfo connectButton = navigateToConnectButton(clickableParent);
            if (connectButton != null) {
                Log.d(TAG, "Found Connect button via hierarchy, attempting click...");
                performHierarchyClick(connectButton, "Connect");
                // Store the parent reference for later cross button click
                storeParentForCrossButton(clickableParent);
                resetRetryCount();
            } else {
                Log.e(TAG, "Could not navigate to Connect button in hierarchy");
                clickableParent.recycle();
                retryFindConnectButton();
            }
        } else {
            Log.e(TAG, "Could not find clickable parent card");
            rootNode.recycle();
            retryFindConnectButton();
        }
        rootNode.recycle();
    }
    // Find the clickable parent element that contains Connect buttons
    private AccessibilityNodeInfo findClickableParentCard(AccessibilityNodeInfo node) {
        if (node == null) return null;
        // Check if current node is clickable and has Connect-related children
        if (node.isClickable()) {
            if (hasConnectButtonInHierarchy(node)) {
                Log.d(TAG, "Found clickable parent with Connect children:");
                Log.d(TAG, "Parent class: " + node.getClassName());
                Log.d(TAG, "Parent clickable: " + node.isClickable());
                Log.d(TAG, "Parent child count: " + node.getChildCount());
                return node;
            }
        }
        // Search children recursively
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                AccessibilityNodeInfo result = findClickableParentCard(child);
                if (result != null) {
                    child.recycle();
                    return result;
                }
                child.recycle();
            }
        }
        return null;
    }
    // Check if node has Connect text in its children/descendants
    private boolean hasConnectButtonInHierarchy(AccessibilityNodeInfo parent) {
        if (parent == null) return false;
        // Look through children for Connect text
        return searchForTextInChildren(parent, "Connect", 0, 3); // Search up to 3 levels deep
    }
    // Recursively search for specific text in children
    private boolean searchForTextInChildren(AccessibilityNodeInfo node, String targetText, int currentDepth, int maxDepth) {
        if (node == null || currentDepth > maxDepth) return false;
        // Check current node
        CharSequence text = node.getText();
        if (text != null && targetText.equals(text.toString())) {
            return true;
        }
        // Search children
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                boolean found = searchForTextInChildren(child, targetText, currentDepth + 1, maxDepth);
                child.recycle();
                if (found) return true;
            }
        }
        return false;
    }
    // Navigate: Parent -> 7th child -> 3rd sub-child
    private AccessibilityNodeInfo navigateToConnectButton(AccessibilityNodeInfo parent) {
        try {
            int childCount = parent.getChildCount();
            Log.d(TAG, "Navigating to Connect button dynamically...");
            Log.d(TAG, "Parent has " + childCount + " children");
            // Check if we have enough children (minimum 2 for second-last)
            if (childCount < 2) {
                Log.e(TAG, "Parent doesn't have enough children. Has: " + childCount + " (need at least 2)");
                return null;
            }
            // Calculate second-last child index
            int secondLastIndex = childCount - 2;
            Log.d(TAG, "Second-last child index: " + secondLastIndex + " (child " + (secondLastIndex + 1) + ")");
            // Get the second-last child
            AccessibilityNodeInfo secondLastChild = parent.getChild(secondLastIndex);
            if (secondLastChild == null) {
                Log.e(TAG, "Could not get second-last child at index " + secondLastIndex);
                return null;
            }
            Log.d(TAG, "Got second-last child. Class: " + secondLastChild.getClassName());
            Log.d(TAG, "Second-last child has " + secondLastChild.getChildCount() + " sub-children");
            // Get sub-child 2 (index 1) for Connect button
            if (secondLastChild.getChildCount() < 2) {
                Log.e(TAG, "Second-last child doesn't have enough sub-children. Has: " + secondLastChild.getChildCount() + " (need at least 2)");
                secondLastChild.recycle();
                return null;
            }
            AccessibilityNodeInfo connectButton = secondLastChild.getChild(1);
            secondLastChild.recycle();

            if (connectButton == null) {
                Log.e(TAG, "Could not get Connect button at sub-child 2 (index 1)");
                return null;
            }
            Log.d(TAG, "Got Connect button - sub-child 2. Class: " + connectButton.getClassName());
            Log.d(TAG, "Connect button text: " + connectButton.getText());
            Log.d(TAG, "Connect button clickable: " + connectButton.isClickable());

            return connectButton;
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to Connect button: " + e.getMessage());
            return null;
        }
    }
    private AccessibilityNodeInfo navigateToCrossButton(AccessibilityNodeInfo parent) {
        try {
            int childCount = parent.getChildCount();
            Log.d(TAG, "Navigating to Cross button dynamically...");
            Log.d(TAG, "Parent has " + childCount + " children");
            if (childCount < 1) {
                Log.e(TAG, "Parent doesn't have any children. Has: " + childCount);
                return null;
            }
            // Calculate last child index
            int lastIndex = childCount - 1;
            Log.d(TAG, "Last child index: " + lastIndex + " (child " + (lastIndex + 1) + ")");
            // Get the last child
            AccessibilityNodeInfo lastChild = parent.getChild(lastIndex);
            if (lastChild == null) {
                Log.e(TAG, "Could not get last child at index " + lastIndex);
                return null;
            }
            Log.d(TAG, "Got last child. Class: " + lastChild.getClassName());
            Log.d(TAG, "Last child has " + lastChild.getChildCount() + " sub-children");
            // Get sub-child 3 (index 2) for Cross button
            if (lastChild.getChildCount() < 3) {
                Log.e(TAG, "Last child doesn't have enough sub-children. Has: " + lastChild.getChildCount() + " (need at least 3)");
                lastChild.recycle();
                return null;
            }
            AccessibilityNodeInfo crossButton = lastChild.getChild(2);
            lastChild.recycle();

            if (crossButton == null) {
                Log.e(TAG, "Could not get Cross button at sub-child 3 (index 2)");
                return null;
            }
            Log.d(TAG, "Got Cross button - sub-child 3. Class: " + crossButton.getClassName());
            Log.d(TAG, "Cross button text: " + crossButton.getText());
            Log.d(TAG, "Cross button clickable: " + crossButton.isClickable());

            return crossButton;
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "IndexOutOfBoundsException in Cross button navigation: " + e.getMessage());
            Log.e(TAG, "Parent child count: " + (parent != null ? parent.getChildCount() : "null"));
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to Cross button: " + e.getMessage());
            return null;
        }
    }
    private void debugDynamicNavigation(AccessibilityNodeInfo parent) {
        if (parent == null) {
            Log.d(TAG, "=== Debug: Parent is null ===");
            return;
        }
        int childCount = parent.getChildCount();
        Log.d(TAG, "=== Dynamic Navigation Debug ===");
        Log.d(TAG, "Total children: " + childCount);
        if (childCount >= 2) {
            int secondLastIndex = childCount - 2;
            Log.d(TAG, "Second-last child index: " + secondLastIndex + " (for Connect button)");
            Log.d(TAG, "Connect button path: Child(" + secondLastIndex + ") -> Sub-child(1)");
        } else {
            Log.d(TAG, "Not enough children for Connect button (need at least 2)");
        }
        if (childCount >= 1) {
            int lastIndex = childCount - 1;
            Log.d(TAG, "Last child index: " + lastIndex + " (for Cross button)");
            Log.d(TAG, "Cross button path: Child(" + lastIndex + ") -> Sub-child(2)");
        } else {
            Log.d(TAG, "Not enough children for Cross button (need at least 1)");
        }
        Log.d(TAG, "===============================");
    }
    // Perform click using hierarchy method
    private void performHierarchyClick(AccessibilityNodeInfo button, String buttonType) {
        try {
            Log.d(TAG, "Performing hierarchy click on " + buttonType + " button...");
            boolean clicked = false;
            // Method 1: Try accessibility click
            if (button.isClickable()) {
                clicked = button.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.d(TAG, buttonType + " accessibility click result: " + clicked);
            }
            // Method 2: If accessibility click fails, try gesture click
            if (!clicked) {
                Log.d(TAG, "Accessibility click failed, trying gesture click...");
                Rect bounds = new Rect();
                button.getBoundsInScreen(bounds);
                int clickX = bounds.centerX();
                int clickY = bounds.centerY();
                Log.d(TAG, buttonType + " button bounds: " + bounds);
                Log.d(TAG, buttonType + " gesture click coordinates: (" + clickX + ", " + clickY + ")");
                performGestureClick(clickX, clickY);
                clicked = true;
            }
            if (clicked) {
                Log.d(TAG, buttonType + " button clicked successfully!");
                if ("Connect".equals(buttonType)) {
                    handler.postDelayed(() -> {
                        clickCrossButton();
                    }, 2000);
                    resetRetryCount();
                } else if ("Cross".equals(buttonType)) {
                    handler.postDelayed(() -> {
                        onConnectClicked();
                    }, 1000);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in hierarchy click: " + e.getMessage());
            if ("Connect".equals(buttonType)) {
                retryFindConnectButton();
            }
        } finally {
            helperFunctions.safelyRecycleNode(button);
        }
    }
    private AccessibilityNodeInfo storedParent = null;
    private void storeParentForCrossButton(AccessibilityNodeInfo parent) {
        if (storedParent != null) {
            storedParent.recycle();
        }
        storedParent = parent;
        Log.d(TAG, "Stored parent reference for cross button");
    }
    // Click the cross button using stored parent
    private void clickCrossButton() {
        Log.d(TAG, "Attempting to click Cross button...");
        if (storedParent == null) {
            Log.e(TAG, "No stored parent for cross button");
            helperFunctions.cleanupAndExit("Cross button not found", "error");
            return;
        }
        debugDynamicNavigation(storedParent);
        AccessibilityNodeInfo crossButton = navigateToCrossButton(storedParent);
        if (crossButton != null) {
            Log.d(TAG, "Found Cross button, attempting click...");
            performHierarchyClick(crossButton, "Cross");
        } else {
            Log.e(TAG, "Could not find Cross button");
            helperFunctions.cleanupAndExit("Cross button not found", "error");
        }
        storedParent.recycle();
        storedParent = null;
    }
    private int retryCount = 0;
    private static final int MAX_RETRIES = 5;
    private void retryFindConnectButton() {
        Log.d(TAG, "Retrying to find Connect button...");
        if (retryCount >= MAX_RETRIES) {
            Log.e(TAG, "Max retries reached. Exiting the app.");
            helperFunctions.cleanupAndExit("Failed to find after multiple retries", "error");
            return;
        }
        handler.postDelayed(() -> {
            helperFunctions.performScroll(0.65f,0.30f);
            handler.postDelayed(this::findAndClickConnectButton, 1000);
            retryCount++;
            Log.d(TAG, "Retry attempt " + retryCount);
        }, 1000);
    }
    private void resetRetryCount() {
        retryCount = 0;
        Log.d(TAG, "Retry count reset to 0.");
    }
    private int connectClickCount = 0;
    private void onConnectClicked() {
        connectClickCount++;
        Log.d(TAG, "Connect button successfully clicked! Count: " + connectClickCount + "/" + MAX_CONNECT_CLICKS);
        Log.d(TAG,"Number of connects passed by user : " + MAX_CONNECT_CLICKS);
        if (connectClickCount < MAX_CONNECT_CLICKS) {
            handler.postDelayed(() -> {
                Log.d(TAG, "Looking for next Connect button... (" + (connectClickCount + 1) + "/" + MAX_CONNECT_CLICKS + ")");
                findAndClickConnectButton();
            }, 3000);
        } else {
            Log.d(TAG, "Reached maximum Connect clicks (" + MAX_CONNECT_CLICKS + "). Looping back to sendConnection...");
            connectClickCount = 0;
            handler.postDelayed(() -> {
                Log.d(TAG, "Going back to HomePage...");
                findAndClickTargetElement();
            }, 2000);
        }
    }
    private void findAndClickTargetElement() {
        Log.d(TAG, "Starting hierarchy navigation to click target element...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickTargetElement", "error");
            return;
        }
        // Find parent node by resource ID
        String parentNodeId = "com.linkedin.android:id/sdui_compose_view";
        AccessibilityNodeInfo parentNode = HelperFunctions.findNodeByResourceId(rootNode, parentNodeId);

        if (parentNode != null) {
            Log.d(TAG, "Found parent node with ID: " + parentNodeId);
            Log.d(TAG, "Parent has " + parentNode.getChildCount() + " children");
            // Navigate to target: Parent -> Child(0) -> Child(0) -> Child(2) -> Child(0)
            AccessibilityNodeInfo targetElement = navigateToTarget(parentNode);
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
    // Navigate through the hierarchy: Parent -> Child(0) -> Child(0) -> Child(2) -> Child(0)
    private AccessibilityNodeInfo navigateToTarget(AccessibilityNodeInfo parent) {
        try {
            Log.d(TAG, "Navigating: Parent -> Child(0) -> Child(0) -> Child(2) -> Child(0)");
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
            // Step 3: Get child(2) of child(0)->child(0)
            if (child0_0.getChildCount() < 3) {
                Log.e(TAG, "child(0)->child(0) has only " + child0_0.getChildCount() + " children, need at least 3");
                child0_0.recycle();
                return null;
            }
            AccessibilityNodeInfo child0_0_2 = child0_0.getChild(2);
            child0_0.recycle();
            if (child0_0_2 == null) {
                Log.e(TAG, "Could not get child(0)->child(0)->child(2)");
                return null;
            }
            Log.d(TAG, "Got child(0)->child(0)->child(2), has " + child0_0_2.getChildCount() + " children");
            // Step 4: Get child(0) of child(0)->child(0)->child(2)
            if (child0_0_2.getChildCount() < 1) {
                Log.e(TAG, "child(0)->child(0)->child(2) has no children");
                child0_0_2.recycle();
                return null;
            }
            AccessibilityNodeInfo targetElement = child0_0_2.getChild(0);
            child0_0_2.recycle();

            if (targetElement == null) {
                Log.e(TAG, "Could not get target element");
                return null;
            }
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
                handler.postDelayed(this::clickHome,2000);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clicking target element: " + e.getMessage());
        } finally {
            helperFunctions.safelyRecycleNode(targetElement);
        }
    }
    public void clickHome() {
        Log.d(TAG, "Entering sendConnection method");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available in sendConnection");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in clickHome", "error");
            return;
        }
        Log.d(TAG, "Root node available, proceeding with connection logic");
        String nodeId = "com.linkedin.android:id/tab_feed";
        AccessibilityNodeInfo homeButton = HelperFunctions.findNodeByResourceId(rootNode, nodeId);
        performHomeClick(homeButton);
        rootNode.recycle();
    }
    private void performHomeClick(AccessibilityNodeInfo Button) {
        handler.postDelayed(() -> {
            boolean isClicked = false;
            try {
                Log.d(TAG, "Found 'Home' button. Attempting click.");
                isClicked = Button.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            } catch (Exception e) {
                Log.e(TAG, "Error performing click action on Home button: " + e.getMessage());
            } finally {
                helperFunctions.safelyRecycleNode(Button);
            }
            if (isClicked) {
                Log.d(TAG, "'Home' button clicked successfully. Proceeding to next step...");
                sendConnectionRequests = true;

                if (likePosts) {
                    Log.d(TAG, "🔄 Both toggles were ON - Switching to Like Posts process...");
                    handler.postDelayed(() -> {
                        Log.d(TAG, "Starting Like Posts automation...");
                        likeComment.startLiking();
                    }, 3000);
                } else {
                    Log.d(TAG, "Only Connection Requests was enabled - Completing automation");
                    handler.postDelayed(() -> {
                        Log.d(TAG, "Page loaded. Performing scroll down to reveal next button...");
                        helperFunctions.performScroll(0.65f,0.30f);
                        handler.postDelayed(() -> {
                            Log.d(TAG, "Scroll completed. Automation finished.");
                            helperFunctions.cleanupAndExit("Connection automation completed", "final");
                        }, 5000);
                    }, 5000);
                }
            } else {
                Log.e(TAG, "Failed to click on 'My Network' button.");
                helperFunctions.cleanupAndExit("Failed to click My Network button", "error");
            }
        }, 2000);
    }
}