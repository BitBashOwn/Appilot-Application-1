package com.example.appilot.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Path;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.appilot.HomeActivity;
import com.example.appilot.automations.Interfaces.Action;
import com.example.appilot.services.MyAccessibilityService;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
@FunctionalInterface
interface ActionWithNoParams {
    void execute();
}
public class HelperFunctions {

    private static final String TAG = "HelperFunctions";
    private final Context context;
    private final Handler handler;

    private String Task_id = null;
    private String job_id = null;
    private static final int STEP_DELAY = 2000;
    private int closeAttempts = 0;

    private final Random random = new Random();

    public HelperFunctions(Context context, String Task_id, String job_id) {
        this.context = context;
        this.Task_id = Task_id;
        this.job_id = job_id;
        this.handler = new Handler(context.getMainLooper());
    }

    public static AccessibilityNodeInfo findNodeByTextAndClass(AccessibilityNodeInfo rootNode, String text, String className) {
        if (rootNode == null) return null;
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo node = rootNode.getChild(i);
            if (node != null && className.contentEquals(node.getClassName())) {
                CharSequence nodeText = node.getText();
                if (text != null && text.contentEquals(nodeText != null ? nodeText : "")) {
                    return node;
                }
            }
            AccessibilityNodeInfo foundNode = findNodeByTextAndClass(node, text, className);
            if (foundNode != null) {
                return foundNode;
            }
        }
        return null;
    }

    public static List<AccessibilityNodeInfo> findNodesByTextAndClass(AccessibilityNodeInfo rootNode, String text, String className) {
        List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        if (rootNode == null) return nodes;
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo node = rootNode.getChild(i);
            if (node != null) {
                if (className.contentEquals(node.getClassName()) && text != null && text.contentEquals(node.getText())) {
                    nodes.add(node);
                }
                nodes.addAll(findNodesByTextAndClass(node, text, className));
            }
        }
        return nodes;
    }
    public static List<AccessibilityNodeInfo> findNodesByClass(AccessibilityNodeInfo rootNode, String className) {
        List<AccessibilityNodeInfo> result = new ArrayList<>();
        if (rootNode == null || className == null) {
            return result;
        }

        int childCount = rootNode.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo childNode = rootNode.getChild(i);
            if (childNode == null) continue;

            if (className.contentEquals(childNode.getClassName())) {
                result.add(childNode);
            }

            result.addAll(findNodesByClass(childNode, className));
        }
        return result;
    }
    public static List<AccessibilityNodeInfo> findNodesByResourceId(AccessibilityNodeInfo rootNode, String resourceId) {
        List<AccessibilityNodeInfo> result = new ArrayList<>();
        if (rootNode == null || resourceId == null) {
            return result;
        }

        int childCount = rootNode.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo childNode = rootNode.getChild(i);
            if (childNode == null) continue;

            if (resourceId.equals(childNode.getViewIdResourceName())) {
                result.add(childNode);
            }

            result.addAll(findNodesByResourceId(childNode, resourceId));
        }
        return result;
    }
    public static AccessibilityNodeInfo findNodeByClass(AccessibilityNodeInfo rootNode, String className) {
        if (rootNode == null) return null;
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo node = rootNode.getChild(i);
            if (node != null && className.contentEquals(node.getClassName())) {
                return node;
            }
            AccessibilityNodeInfo foundNode = findNodeByClass(node, className);
            if (foundNode != null) {
                return foundNode;
            }
        }
        return null;
    }
    public static AccessibilityNodeInfo findNodeByClassAndIndex(AccessibilityNodeInfo rootNode, String className, int index) {
        if (rootNode == null) {
            return null;
        }
        return findNodeByClassAndIndexRecursive(rootNode, className, index, new int[]{0});
    }
    private static AccessibilityNodeInfo findNodeByClassAndIndexRecursive(AccessibilityNodeInfo node, String className, int index, int[] currentIndex) {
        if (node == null) {
            return null;
        }

        if (className.contentEquals(node.getClassName())) {
            if (currentIndex[0] == index) {
                return node;
            } else {
                currentIndex[0]++;
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo result = findNodeByClassAndIndexRecursive(node.getChild(i), className, index, currentIndex);
            if (result != null) {
                return result;
            }
        }

        return null;
    }
    public static AccessibilityNodeInfo findNthClickableNode(AccessibilityNodeInfo root, String className, int n) {
        return findNthClickableNodeHelper(root, className, n, new int[]{0});
    }
    private static AccessibilityNodeInfo findNthClickableNodeHelper(AccessibilityNodeInfo root, String className, int n, int[] count) {
        if (root == null) return null;
        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo child = root.getChild(i);
            if (child != null) {
                if (className.contentEquals(child.getClassName()) && child.isClickable()) {
                    count[0]++;
                    if (count[0] == n) {
                        return child;
                    }
                }
                AccessibilityNodeInfo result = findNthClickableNodeHelper(child, className, n, count);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    public static List<AccessibilityNodeInfo> findEditableNodes(AccessibilityNodeInfo rootNode) {
        List<AccessibilityNodeInfo> editableNodes = new ArrayList<>();
        findEditableNodesRecursively(rootNode, editableNodes);
        return editableNodes;
    }
    private static void findEditableNodesRecursively(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> editableNodes) {
        if (node == null) return;

        if ("android.widget.EditText".contentEquals(node.getClassName()) && node.isEditable()) {
            editableNodes.add(node);
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            findEditableNodesRecursively(node.getChild(i), editableNodes);
        }
    }
    public static void setText(AccessibilityNodeInfo node, String text) {
        if (node != null) {
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        }
    }
    public static AccessibilityNodeInfo findNodeByContentDescriptionAndClass(AccessibilityNodeInfo root, String contentDescription, String className) {
        if (root == null) {
            return null;
        }
        CharSequence rootContentDescription = root.getContentDescription();
        if (rootContentDescription != null && contentDescription.equals(rootContentDescription.toString()) && className.contentEquals(root.getClassName())) {
            return root;
        }
        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo result = findNodeByContentDescriptionAndClass(root.getChild(i), contentDescription, className);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
    public static AccessibilityNodeInfo findNodeByPartialContentDescriptionAndClass(AccessibilityNodeInfo root, String partialContentDescription, String className) {
        if (root == null) {
            return null;
        }
        CharSequence rootContentDescription = root.getContentDescription();
        if (rootContentDescription != null && rootContentDescription.toString().contains(partialContentDescription) && className.contentEquals(root.getClassName())) {
            return root;
        }
        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo result = findNodeByPartialContentDescriptionAndClass(root.getChild(i), partialContentDescription, className);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
    public static AccessibilityNodeInfo findNodeByResourceId(AccessibilityNodeInfo root, String resourceId) {
        if (root == null) {
            Log.d(TAG, "Root is null, cannot search for " + resourceId);
            return null;
        }

        // Log the current node's resource ID being checked
        String currentResourceId = root.getViewIdResourceName();

        // If the current node's resource ID matches the target, return it
        if (resourceId.equals(currentResourceId)) {
            Log.d(TAG, "Found target node with resourceId: " + resourceId);
            return root;
        }

        // Recursively search children
        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo child = root.getChild(i);

            // Ensure the child is not null before recursing
            if (child != null) {
                AccessibilityNodeInfo result = findNodeByResourceId(child, resourceId);
                if (result != null) {
                    return result;  // Found the target node
                }
            }
        }

        return null;
    }
    public static String generateRandomUsername() {
        String firstName = generateRandomItalianFirstName();
        String lastName = generateRandomItalianLastName();
        int randomDigits = new Random().nextInt(9000) + 1000;
        return firstName.toLowerCase() + lastName.toLowerCase() + randomDigits;
    }
    public static String generateRandomPassword() {
        String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder password = getStringBuilder(upperCaseLetters, lowerCaseLetters);

        List<Character> passwordChars = new ArrayList<>();
        for (char c : password.toString().toCharArray()) {
            passwordChars.add(c);
        }
        Collections.shuffle(passwordChars);
        StringBuilder shuffledPassword = new StringBuilder();
        for (char c : passwordChars) {
            shuffledPassword.append(c);
        }

        return shuffledPassword.toString();
    }
    private static StringBuilder getStringBuilder(String upperCaseLetters, String lowerCaseLetters) {
        String digits = "0123456789";
        String symbols = "!@#$%^&*()_-+=<>?";

        String allChars = upperCaseLetters + lowerCaseLetters + digits + symbols;
        Random random = new Random();
        StringBuilder password = new StringBuilder();

        password.append(upperCaseLetters.charAt(random.nextInt(upperCaseLetters.length())));
        password.append(lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(symbols.charAt(random.nextInt(symbols.length())));

        for (int i = 4; i < 9; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        return password;
    }
    public static String generateRandomItalianFirstName() {
        String[] italianFirstNames = {"Giovanni", "Marco", "Luca", "Andrea", "Francesco", "Alessandro", "Lorenzo", "Davide", "Mattia", "Riccardo"};
        Random random = new Random();
        return italianFirstNames[random.nextInt(italianFirstNames.length)];
    }
    public static String generateRandomItalianLastName() {
        String[] italianLastNames = {"Rossi", "Russo", "Ferrari", "Esposito", "Bianchi", "Romano", "Colombo", "Ricci", "Marino", "Greco"};
        Random random = new Random();
        return italianLastNames[random.nextInt(italianLastNames.length)];
    }
    public static String generateRandomDay() {
        Random random = new Random();
        return String.valueOf(random.nextInt(29) + 1);
    }
    public static String generateRandomYear() {
        Random random = new Random();
        return String.valueOf(random.nextInt(2005 - 1995 + 1) + 1995);
    }



    // New methods
    public void openMyApp() {
        AccessibilityService service = (MyAccessibilityService) context;
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
        handler.postDelayed(this::swipeRightAndClickCenter, STEP_DELAY);
    }
    public AccessibilityNodeInfo findLatestNodeForFeedLazyColumn() {
        String resourceId = "com.reddit.frontpage:id/screen_pager";
        MyAccessibilityService service = (MyAccessibilityService) context;
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();

        if (rootNode == null) {
            Log.e(TAG, "Root node is null");
            return null;
        }

        // Find nodes with the specified resource ID
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId(resourceId);

        if (nodes != null && !nodes.isEmpty()) {
            // Return the first node found with the matching resource ID
            AccessibilityNodeInfo latestNode = nodes.get(0);
            Log.d(TAG, "Successfully found node with resource ID: " + resourceId);
            return latestNode;
        } else {
            Log.e(TAG, "No node found with resource ID: " + resourceId);
            return null;
        }
    }
    public void skipChromeAndOpenMyApp() {
        AccessibilityService service = (MyAccessibilityService) context;
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
        handler.postDelayed(this::swipeRightMoreAndClickCenter, STEP_DELAY);
    }
    public void closeAndOpenMyApp() {
        AccessibilityService service = (MyAccessibilityService) context;
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
        handler.postDelayed(this::closeAppAndClickCenter, STEP_DELAY);
    }
    public void closeInstagramAppInRecents() {
        AccessibilityService service = (MyAccessibilityService) context;
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();

        AccessibilityNodeInfo instagramNode = HelperFunctions.findNodeByPartialContentDescriptionAndClass(
                rootNode, "Instagram", "com.instagram.android.activity.MainTabActivity");

        if (instagramNode != null) {
            // Close Instagram using swipe gesture or perform action
            closeAppAndClickCenter();  // This gesture method swipes up and closes the app.
        } else {
            Log.d(TAG, "Instagram app not found in recents.");
        }
    }
    public void closeInstagramAppWithBack() {
        MyAccessibilityService service = (MyAccessibilityService) context;
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
        handler.postDelayed(() -> service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK), 1000);
    }
    private void swipeRightAndClickCenter() {
        Path swipePath = new Path();
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        swipePath.moveTo(screenWidth * 0.2f, screenHeight / 2f);
        swipePath.lineTo(screenWidth * 0.8f, screenHeight / 2f);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 500));

        MyAccessibilityService service = (MyAccessibilityService) context;
        service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                handler.postDelayed(HelperFunctions.this::clickInCenter, STEP_DELAY+2000);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                // Handle cancellation if needed
            }
        }, null);
    }
    private void swipeRightMoreAndClickCenter() {
        Path swipePath = new Path();
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        swipePath.moveTo(screenWidth * 0.1f, screenHeight / 2f);
        swipePath.lineTo(screenWidth * 0.9f, screenHeight / 2f);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 500));

        MyAccessibilityService service = (MyAccessibilityService) context;
        service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                handler.postDelayed(HelperFunctions.this::clickInCenter, STEP_DELAY);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                // Handle cancellation if needed
            }
        }, null);
    }
    private void closeAppAndClickCenter() {
        Log.d(TAG, "closeAppAndClickCenter: entered");

        Path swipePath = new Path();
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        // Start the swipe from 80% of the screen height and swipe up to 5% of the screen height
        swipePath.moveTo(screenWidth / 2f, screenHeight * 0.6f);  // Start lower, at 80% of the screen height
        swipePath.lineTo(screenWidth / 2f, screenHeight * 0.05f); // End near the top, at 5% of the screen height

        // Create the gesture description
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 200, 300));

        MyAccessibilityService service = (MyAccessibilityService) context;
        service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                // After the swipe is completed, click in the center (if needed)
                handler.postDelayed(HelperFunctions.this::clickInCenter, STEP_DELAY);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.e(TAG, "Swipe gesture was cancelled.");
            }
        }, null);
    }

    public void clickInCenter() {
        Log.d(TAG, "going to click in center");
        Path clickPath = new Path();
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        clickPath.moveTo(screenWidth / 2f, screenHeight / 2f);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, 100));

        MyAccessibilityService service = (MyAccessibilityService) context;
        service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
//                if(closeAttempts < 3){
//                    handler.postDelayed(()->IsAppClosed(),1000+ random.nextInt(3000));
//                }else{
//                    closeAttempts =0;
//                }
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                // Handle cancellation if needed
            }
        }, null);
    }
    public void IsAppClosed(){
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if(rootNode != null){
                AccessibilityNodeInfo allSetSection = findNodeByResourceId(rootNode,"com.example.appilot:id/AllSetContainer");
                if (allSetSection == null) {
                    closeAttempts++;
                    goToHome();
                    handler.postDelayed(this::closeAndOpenMyApp, 1500 + random.nextInt(2000));
                } else {
                    closeAttempts = 0;
                }
            }
    }

    public String cleanComment(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        // Check if input contains colon followed by potential content
        if (input.contains(":")) {
            input = input.split(":", 2)[1].trim();
        }

        // Remove choose/select instructions if present
        int chooseIndex = input.toLowerCase().indexOf(" choose");
        if (chooseIndex != -1) {
            input = input.substring(0, chooseIndex);
        }

        // Handle wrapping quotes
        while (input.startsWith("\"") && input.endsWith("\"")) {
            input = input.substring(1, input.length() - 1).trim();
        }

        // Split content into lines and clean first line
        String[] lines = input.split("\n");
        if (lines.length > 0) {
            String line = lines[0].trim();
            return line.replaceFirst("^\\d+\\.\\s*", "");
        }

        return "";
    }
    public AccessibilityNodeInfo findLatestNodeForFeedLazyColumn(String resourceId) {
        MyAccessibilityService service = (MyAccessibilityService) context;
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();

        if (rootNode == null) {
            Log.e(TAG, "Root node is null");
            return null;
        }

        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId(resourceId);

        if (nodes != null && !nodes.isEmpty()) {
            AccessibilityNodeInfo latestNode = nodes.get(0);
            Log.d(TAG, "Successfully found node with resource ID: " + resourceId);
            return latestNode;
        } else {
            Log.e(TAG, "No node found with resource ID: " + resourceId);
            return null;
        }
    }
    public void logNodeHierarchy(AccessibilityNodeInfo node) {
        if (node == null) return;

        Log.d(TAG, "Node: " + node.getClassName() +
                " | ID: " + node.getViewIdResourceName() +
                " | Clickable: " + node.isClickable());

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                logNodeHierarchy(child);
                child.recycle();
            }
        }
    }
    public boolean evaluatePositiveKeywords(String bioText, List<String> positiveKeywords) {
        if (bioText == null || positiveKeywords == null) {
            Log.e(TAG, "Null argument provided.");
            return false;
        }

        String bioTextLower = bioText.toLowerCase();

        boolean hasPositive = false;

        for (String keyword : positiveKeywords) {
            if (bioTextLower.contains(keyword.toLowerCase())) {
                hasPositive = true;
                Log.d(TAG, "Positive keyword matched: " + keyword);
                break;
            }
        }

        return hasPositive;
    }
    public boolean evaluateNegativeKeywords(String bioText, List<String> negativeKeywords) {
        if (bioText == null || negativeKeywords == null) {
            Log.e(TAG, "Null argument provided.");
            return false;
        }

        String bioTextLower = bioText.toLowerCase();

        boolean hasNegative = false;

        for (String keyword : negativeKeywords) {
            if (bioTextLower.contains(keyword.toLowerCase())) {
                hasNegative = true;
                Log.d(TAG, "Negative keyword matched: " + keyword);
                break; // No need to check further
            }
        }
        return hasNegative;
    }
    public boolean getFollowerCount(String text, int minimumFriends) {
        try {
            if (text == null || text.isEmpty()) {
                return false;
            }

            // Remove "Followed by " prefix if present
            text = text.replace("Followed by ", "");

            int count = 0;

            // Check for "and X others" pattern
            if (text.contains(" and ")) {
                String[] mainParts = text.split(" and ");

                // Count names before "and"
                String namesPart = mainParts[0];
                count += namesPart.split(", ").length;

                // Extract number of others
                if (mainParts[1].contains("others")) {
                    String[] othersParts = mainParts[1].split(" ");
                    try {
                        count += Integer.parseInt(othersParts[0]);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Error parsing others count: " + mainParts[1], e);
                    }
                }
            } else {
                // Handle case without "others" - just count comma-separated names
                count = text.split(", ").length;
            }

            Log.d(TAG, "Mutual friends count for text '" + text + "': " + count);
            return count >= minimumFriends;
        } catch (Exception e) {
            Log.e(TAG, "Error in getFollowerCount method", e);
            return false;
        }
    }
    public int convertPostCount(String postCount) {
        if (postCount == null || postCount.isEmpty()) {
            return 0;
        }

        postCount = postCount.toLowerCase().replace(",", ""); // Remove commas

        try {
            if (postCount.endsWith("k")) {
                double value = Double.parseDouble(postCount.replace("k", ""));
                return (int) (value * 1000);
            } else if (postCount.endsWith("k+")) {
                double value = Double.parseDouble(postCount.replace("k+", ""));
                return (int) (value * 1000);
            } else {
                return Integer.parseInt(postCount); // Parse plain number
            }
        } catch (NumberFormatException e) {
            Log.e("convertPostCount", "Failed to parse post count: " + postCount, e);
            return 0;
        }
    }
    public void navigateBack(){
        Log.i(TAG,"Performed back action");
        MyAccessibilityService service = (MyAccessibilityService) context;
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    private List<AccessibilityNodeInfo> getAllButtonsInWindow(Context context) {
        List<AccessibilityNodeInfo> buttonsList = new ArrayList<>();
        MyAccessibilityService service = (MyAccessibilityService) context;

        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e("MyAccessibilityService", "Root node is null.");
            return buttonsList; // Return an empty list
        }

        try {
            // Find all nodes with the class name "android.widget.Button"
            buttonsList = rootNode.findAccessibilityNodeInfosByViewId("android.widget.Button");
            if (buttonsList.isEmpty()) {
                Log.d("MyAccessibilityService", "No buttons found in the current window.");
            }
        } finally {
            rootNode.recycle(); // Recycle rootNode to avoid memory leaks
        }

        return buttonsList;
    }
    public String CheckInstagramUrlAndReturnUsername(String url) {
        if (url == null ||
                (!url.startsWith("https://www.instagram.com/") && !url.startsWith("https://instagram.com/"))) {
            return null;
        }

        String urlHead = url.startsWith("https://www.instagram.com/")
                ? "https://www.instagram.com/"
                : "https://instagram.com/";

        String username = url.replace(urlHead, "").replace("/", "");

        return username.isEmpty() ? null : username;
    }

//    public AccessibilityNodeInfo FindAndReturnNodeById(String Id, int attempts) {
//        AccessibilityNodeInfo rootNode = null;
//        AccessibilityNodeInfo targetNode = null;
//
//        try {
//            for (int i = 0; i < attempts; i++) {
//                // Get fresh root node on each attempt
//                rootNode = getRootInActiveWindow();
//
//                if (rootNode == null) {
//                    Log.e(TAG, "rootNode NotFound on attempt " + (i + 1));
//                    continue;
//                }
//
//                // Try to find the target node
//                targetNode = findNodeByResourceId(rootNode, Id);
//
//                if (targetNode != null) {
//                    return targetNode;
//                }
//
//                // Cleanup root node before next attempt
//                if (rootNode != null) {
//                    rootNode.recycle();
//                }
//
//                // Wait 500ms before next attempt
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    Log.e(TAG, "Sleep interrupted: " + e.getMessage());
//                    break;
//                }
//            }
//
//            Log.e(TAG, "Node not found after " + attempts + " attempts");
//            return null;
//
//        } finally {
//            // Final cleanup
//            if (rootNode != null) {
//                rootNode.recycle();
//            }
//            if (targetNode != null && targetNode.getWindowId() == -1) {
//                targetNode.recycle();
//            }
//        }
//    }

    public AccessibilityNodeInfo getRootInActiveWindow() {
        try {
            if (context == null) {
                Log.e(TAG,"Context is Null");
                return null;
            }
            MyAccessibilityService service = (MyAccessibilityService) context;
            return service.getRootInActiveWindow();
        } catch (Exception e) {
            return null;
        }
    }


public AccessibilityNodeInfo FindAndReturnNodeById(String Id, int attempts) {
    AccessibilityNodeInfo rootNode = null;
    AccessibilityNodeInfo targetNode = null;

    try {
        for (int i = 0; i < attempts; i++) {
            // Get fresh root node on each attempt
            rootNode = getRootInActiveWindow();

            if (rootNode == null) {
                Log.e(TAG, "Root node not found on attempt " + (i + 1));
                continue;
            }

            // Try to find the target node
            targetNode = findNodeByResourceId(rootNode, Id);

            if (targetNode != null) {
                return targetNode; // Return immediately if the target node is found
            }

            // Cleanup root node before next attempt
            if (rootNode != null) {
                rootNode.recycle();
            }

            // Wait 500ms before the next attempt
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.e(TAG, "Sleep interrupted: " + e.getMessage());
                break; // Exit loop if interrupted
            }
        }

        Log.e(TAG, "Node with ID '" + Id + "' not found after " + attempts + " attempts");
        return null;

    } finally {
        // Final cleanup
        if (rootNode != null) {
            rootNode.recycle();
        }
        if (targetNode != null && targetNode.getWindowId() == -1) {
            targetNode.recycle();
        }
    }
}

    public List<AccessibilityNodeInfo> FindAndReturnNodesById(String Id, int attempts) {
        AccessibilityNodeInfo rootNode = null;
        List<AccessibilityNodeInfo> nodesList = new ArrayList<>();

        try {
            for (int i = 0; i < attempts; i++) {
                // Get fresh root node on each attempt
                rootNode = getRootInActiveWindow();

                if (rootNode == null) {
                    Log.e(TAG, "rootNode Not Found on attempt " + (i + 1));
                    continue;
                }

                // Try to find the nodes
                nodesList = rootNode.findAccessibilityNodeInfosByViewId(Id);

                if (nodesList != null && !nodesList.isEmpty()) {
                    return nodesList;  // Return immediately if nodes are found
                }

                // Cleanup root node before next attempt
                if (rootNode != null) {
                    rootNode.recycle();
                }

                // Wait 500ms before next attempt
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Sleep interrupted: " + e.getMessage());
                    break;
                }
            }

            Log.e(TAG, "Nodes not found after " + attempts + " attempts");
            return nodesList;  // Returns empty list if nodes not found

        } finally {
            // Final cleanup
            if (rootNode != null) {
                rootNode.recycle();
            }
        }
    }

    public List<AccessibilityNodeInfo> FindAndReturnAllNodesById(String id, int attempts) {
        AccessibilityNodeInfo rootNode = null;
        List<AccessibilityNodeInfo> foundNodes = new ArrayList<>();

        try {
            for (int i = 0; i < attempts; i++) {
                // Get a fresh root node on each attempt
                rootNode = getRootInActiveWindow();

                if (rootNode == null) {
                    Log.e(TAG, "rootNode not found on attempt " + (i + 1));
                    continue;
                }

                // Find all nodes with the specified resource ID
                List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId(id);

                if (nodes != null && !nodes.isEmpty()) {
                    foundNodes.addAll(nodes);
                }

                // Cleanup root node before the next attempt
                if (rootNode != null) {
                    rootNode.recycle();
                }

                // If nodes are found, return them immediately
                if (!foundNodes.isEmpty()) {
                    return foundNodes;
                }

                // Wait 500ms before the next attempt
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Sleep interrupted: " + e.getMessage());
                    break;
                }
            }

            Log.e(TAG, "Nodes not found after " + attempts + " attempts");
            return foundNodes; // Returns an empty list if no nodes are found

        } finally {
            // Final cleanup
            if (rootNode != null) {
                rootNode.recycle();
            }
        }
    }
    public List<AccessibilityNodeInfo> FindNodesByClassAndIndexUntilText(String targetText, int targetIndex) {
        List<AccessibilityNodeInfo> resultNodes = new ArrayList<>();
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();

        try {
            // Get the root node
            rootNode = getRootInActiveWindow();

            if (rootNode == null) {
                Log.e(TAG, "Root node is null");
                return resultNodes;
            }

            // Recursively gather nodes by class and index until the target text is found
            gatherNodesByClassAndIndexUntilText(rootNode, "android.view.View", targetText, targetIndex, resultNodes);

            return resultNodes;

        } finally {
            if (rootNode != null) {
                rootNode.recycle();
            }
        }
    }
    private boolean gatherNodesByClassAndIndexUntilText(AccessibilityNodeInfo node, String targetClass, String targetText, int targetIndex, List<AccessibilityNodeInfo> resultNodes) {
        if (node == null) {
            return false;
        }

        // Check if the node matches the class and has the specified index
        if (targetClass.equals(node.getClassName()) && node.getParent() != null) {
            int index = getIndexInParent(node);
            if (index == targetIndex) {
                resultNodes.add(node);
            }
        }

        // Check if the node contains the target text
        if (node.getText() != null && targetText.equals(node.getText().toString())) {
            resultNodes.remove(resultNodes.size() - 1); // Remove the last added node with the target text
            return true; // Stop further processing
        }

        // Recursively process child nodes
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = node.getChild(i);
            if (gatherNodesByClassAndIndexUntilText(childNode, targetClass, targetText, targetIndex, resultNodes)) {
                return true; // Stop further processing once target is found
            }

            if (childNode != null) {
                childNode.recycle();
            }
        }

        return false;
    }
    public AccessibilityNodeInfo findButtonByContentDesc(AccessibilityNodeInfo rootNode, String contentDesc) {
        if (rootNode == null || contentDesc == null) {
            Log.d(TAG, "Root node or content description is null");
            return null;
        }

        // Check the current node first
        CharSequence nodeContentDesc = rootNode.getContentDescription();
        if (nodeContentDesc != null &&
                contentDesc.equals(nodeContentDesc.toString()) &&
                (rootNode.isClickable() || "android.widget.Button".equals(rootNode.getClassName()))) {
            Log.d(TAG, "Found button with content description: " + contentDesc);
            return rootNode;
        }

        // Search through all children recursively
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo child = rootNode.getChild(i);
            if (child != null) {
                AccessibilityNodeInfo result = findButtonByContentDesc(child, contentDesc);
                if (result != null) {
                    // Don't recycle the result node as it will be used by caller
                    child.recycle();
                    return result;
                }
                child.recycle();
            }
        }

        return null;
    }

    public AccessibilityNodeInfo findNodeByClassAndContentDesc(AccessibilityNodeInfo rootNode, String Class, String contentDesc) {
        if (rootNode == null || contentDesc == null) {
            Log.d(TAG, "Root node or content description is null");
            return null;
        }

        // Check the current node first
        CharSequence nodeContentDesc = rootNode.getContentDescription();
        if (nodeContentDesc != null &&
                contentDesc.equals(nodeContentDesc.toString()) &&
                (rootNode.isClickable() || Class.equals(rootNode.getClassName()))) {
            Log.d(TAG, "Found button with content description: " + contentDesc);
            return rootNode;
        }

        // Search through all children recursively
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo child = rootNode.getChild(i);
            if (child != null) {
                AccessibilityNodeInfo result = findButtonByContentDesc(child, contentDesc);
                if (result != null) {
                    // Don't recycle the result node as it will be used by caller
                    child.recycle();
                    return result;
                }
                child.recycle();
            }
        }

        return null;
    }
    public AccessibilityNodeInfo findNodeByClassAndText(AccessibilityNodeInfo rootNode, String Class, String TextContent) {
        rootNode.refresh();
        if (rootNode == null || TextContent == null || Class == null) {
            Log.d(TAG, "Root node or content description is null");
            return null;
        }

        // Check the current node first
        CharSequence nodeContentDesc = rootNode.getText();
        if (nodeContentDesc != null &&
                nodeContentDesc.toString().contains(TextContent) &&
                (rootNode.isClickable() || Class.equals(rootNode.getClassName()))) {
            Log.d(TAG, "Found button with content description: " + TextContent);
            return rootNode;
        }

        // Search through all children recursively
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo child = rootNode.getChild(i);
            if (child != null) {
                AccessibilityNodeInfo result = findNodeByClassAndText(child, Class, TextContent);
                if (result != null) {
                    child.recycle();
                    return result;
                }
                child.recycle();
            }
        }

        return null;
    }

    private int getIndexInParent(AccessibilityNodeInfo node) {
        if (node == null || node.getParent() == null) {
            return -1;
        }

        AccessibilityNodeInfo parent = node.getParent();
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (node.equals(parent.getChild(i))) {
                return i;
            }
        }
        return -1;
    }
    public Boolean InstagramPrivateProfileChecker(AccessibilityNodeInfo rootNode){
        Log.i(TAG,"Entered InstagramPrivateProfileChecker");
        rootNode.refresh();
        AccessibilityNodeInfo privateCheck = findNodeByResourceId(rootNode, "com.instagram.android:id/row_profile_header_empty_profile_notice_title");
        if (privateCheck != null) {
            CharSequence title = privateCheck.getText();
            if ("This account is private".equals(title.toString())) {
                Log.e(TAG, "Account is Private, inside InstagramPrivateProfileChecker");
                return true;
            }else{
                Log.e(TAG, "Account is not Private, inside InstagramPrivateProfileChecker");
                return false;
            }
        }else{
            AccessibilityNodeInfo privateCheck2 = findNodeByResourceId(rootNode, "com.instagram.android:id/igds_headline_emphasized_headline");
            if(privateCheck2 != null && privateCheck2.getText() != null){
                CharSequence title = privateCheck2.getText();
                if ("This account is private".equals(title.toString())) {
                    Log.e(TAG, "Account is Private, inside InstagramPrivateProfileChecker");
                    return true;
                }else{
                    Log.e(TAG, "Account is not Private, inside InstagramPrivateProfileChecker");
                    return false;
                }
            }
        }
        return false;
    }

    public void performScrollUp(Action callback, HelperFunctions instance) {
        Path swipePath = new Path();
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        float startY = screenHeight * (0.7f + random.nextFloat() * 0.1f);
        float endY = screenHeight * (0.3f + random.nextFloat() * 0.1f);
        float xVariation = screenWidth * (0.1f * random.nextFloat() - 0.05f);

        swipePath.moveTo(screenWidth / 2f + xVariation, startY);
        swipePath.lineTo(screenWidth / 2f + xVariation, endY);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        int gestureDuration = 150 + random.nextInt(150);
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, gestureDuration));

        try {
            MyAccessibilityService service = (MyAccessibilityService) context;
            service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    handler.postDelayed(
                            callback::execute
                            , 1000 + random.nextInt(2000));
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    handler.postDelayed(() -> {
                        instance.cleanupAndExit("Automation Could not be Completed Please make sure The Device has Accessibility enabled.", "error");
                    }, 1000 + random.nextInt(2000));
                }
            }, null);
        } catch (Exception e) {
            Log.e(TAG, "Error during scroll up", e);
            handler.postDelayed(() -> {
                instance.cleanupAndExit("Automation Could not be Completed Please make sure The Device has Accessibility enabled.", "error");
            }, 1000 + random.nextInt(2000));
        }
    }

    public void performStaticScrollUp(Action callback, HelperFunctions instance) {
        Path swipePath = new Path();
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        float startY = screenHeight * (0.7f + random.nextFloat() * 0.1f);
        float endY = screenHeight * (0.3f + random.nextFloat() * 0.1f);
        float xVariation = screenWidth * (0.1f * random.nextFloat() - 0.05f);

        swipePath.moveTo(screenWidth / 2f + xVariation, startY);
        swipePath.lineTo(screenWidth / 2f + xVariation, endY);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();

        int gestureDuration = 500 + random.nextInt(500);
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, gestureDuration));

        try {
            MyAccessibilityService service = (MyAccessibilityService) context;
            service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    handler.postDelayed(callback::execute, 3000 + random.nextInt(1500));
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    // Fixed delay in case of cancellation
                    handler.postDelayed(() -> {
                        instance.cleanupAndExit("Automation Could not be Completed Please make sure The Device has Accessibility enabled.", "error");
                    }, 1000 + random.nextInt(2000));
                }
            }, null);
        } catch (Exception e) {
            Log.e(TAG, "Error during scroll up", e);
            // Fixed delay in case of exception
            handler.postDelayed(() -> {
                instance.cleanupAndExit("Automation Could not be Completed Please make sure The Device has Accessibility enabled.", "error");
            }, 1000 + random.nextInt(2000));
        }
    }
    public void safelyRecycleNode(AccessibilityNodeInfo node) {
        if (node != null) {
            try {
                node.recycle();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error recycling node: " + e.getMessage());
            }
        }
    }
    public Boolean InstagramPrivateProfileChecker(){
        Log.i(TAG,"Entered InstagramPrivateProfileChecker");
        AccessibilityNodeInfo privateCheck = FindAndReturnNodeById("com.instagram.android:id/row_profile_header_empty_profile_notice_title", 2);
        if (privateCheck != null) {
            CharSequence title = privateCheck.getText();
            if ("This account is private".equals(title.toString())) {
                Log.e(TAG, "Account is Private, inside InstagramPrivateProfileChecker");
                return true;
            }else{
                Log.e(TAG, "Account is not Private, inside InstagramPrivateProfileChecker");
                return false;
            }
        }else{
            AccessibilityNodeInfo privateCheck2 = FindAndReturnNodeById("com.instagram.android:id/igds_headline_emphasized_headline", 2);
            if(privateCheck2 != null && privateCheck2.getText() != null){
                CharSequence title = privateCheck2.getText();
                if ("This account is private".equals(title.toString())) {
                    Log.e(TAG, "Account is Private, inside InstagramPrivateProfileChecker");
                    return true;
                }else{
                    Log.e(TAG, "Account is not Private, inside InstagramPrivateProfileChecker");
                    return false;
                }
            }
        }
        return false;
    }

    public void dragSliderSection(AccessibilityNodeInfo SectionHead, Action callback,HelperFunctions helperFunctions) {
        Log.i(TAG, "Entered dragLikersSection");
        Rect bounds = new Rect();
        SectionHead.getBoundsInScreen(bounds);

        Path dragPath = new Path();
        float startY = bounds.centerY();
        float endY = bounds.centerY() - (bounds.height() * 8);

        dragPath.moveTo(bounds.centerX(), startY);
        dragPath.lineTo(bounds.centerX(), endY);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(dragPath, 0, 300 + random.nextInt(200)));

        try {
            MyAccessibilityService service = (MyAccessibilityService) context;
            service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    handler.postDelayed(callback::execute, 1500 + random.nextInt(1000));
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    Log.e(TAG, "Drag gesture cancelled");
                    helperFunctions.cleanupAndExit("Automation Could not be Completed Please make sure The Device has Accessibility enabled.", "error");
                }
            }, null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to dispatch drag gesture", e);
            helperFunctions.cleanupAndExit("Automation Could not be Completed Please make sure The Device has Accessibility enabled.", "error");
        }
    }

    public void clickOnBounds(android.graphics.Rect bounds, Action callback, String Type, int bastime, int maxTime, HelperFunctions instance) {
        if (bounds.isEmpty()) {
            Log.e(TAG, "Node bounds are empty, cannot click.");
            return;
        }

        float X = 0;
        float Y = 0;

        float width = Math.max(1, bounds.width());
        float height = Math.max(1, bounds.height());

        float centerX = bounds.exactCenterX();
        float centerY = bounds.exactCenterY();

        float marginX = width * 0.15f;
        float marginY = height * 0.15f;

        switch (Type) {
            case "Center":
                X = centerX + random.nextFloat() * (2 * marginX) - marginX;
                Y = centerY + random.nextFloat() * (2 * marginY) - marginY;
                break;

            case "Last":
                float LastX = width * 0.85f;
                float LastY = height * 0.85f;
                X = LastX + random.nextFloat() * Math.max(1, (width - LastX));
                Y = LastY + random.nextFloat() * Math.max(1, (height - LastY));
                break;

            case "Start":
                float startMarginX = width * 0.2f;
                float startMarginY = height * 0.2f;
                X = bounds.left + random.nextFloat() * startMarginX;
                Y = bounds.top + random.nextFloat() * startMarginY;
                break;
        }

        Path clickPath = new Path();
        clickPath.moveTo(X, Y);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        int clickDuration = 100;
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, clickDuration));

        try {
            MyAccessibilityService service = (MyAccessibilityService) context;
            service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    Log.d(TAG, "Click action completed successfully");
                    int gap = bastime + (maxTime > bastime ? random.nextInt(maxTime - bastime) : 0);
                    handler.postDelayed(callback::execute, gap);
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    Log.e(TAG, "Click action was cancelled");
                    handler.post(() -> instance.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.","error"));
                }
            }, null);
        } catch (Exception e) {
            Log.e(TAG, "Error while clicking in the center of bounds", e);
            handler.post(() -> instance.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.","error"));
        }
    }


    public void sleep(int time){
        try {
            Thread.sleep(time );
        } catch (InterruptedException e) {
            Log.e(TAG, "Sleep interrupted: " + e.getMessage());
        }
    }
    public void goToHome() {
        try {
            MyAccessibilityService service = (MyAccessibilityService) context;
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
        } catch (Exception e) {
            Log.e(TAG, "Failed to perform back action", e);
        }
    }

    public void cleanupAndExit(String returnMessage, String type) {
        handler.postDelayed(()->{
            goToHome();
            HomeActivity home = new HomeActivity();
            home.sendMessage(returnMessage,this.Task_id,this.job_id,type);
            handler.postDelayed(this::closeAndOpenMyApp, 1500 + random.nextInt(2000));
            },1000+ random.nextInt(500));

    }
    public void sendUpdateMessage(String returnMessage, String type){
        HomeActivity home = new HomeActivity();
        home.sendMessage(returnMessage,this.Task_id,this.job_id, type);
    }


    public void saveNodeTreeToDownloads(AccessibilityNodeInfo node, String fileName) {
        if (node == null) return;

        StringBuilder builder = new StringBuilder();
        buildNodeTreeString(node, builder, 0);
        String content = builder.toString();

        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
        values.put(MediaStore.Downloads.IS_PENDING, 1);

        ContentResolver resolver = context.getContentResolver();
        Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri fileUri = resolver.insert(collection, values);

        if (fileUri != null) {
            try (OutputStream out = resolver.openOutputStream(fileUri)) {
                if (out != null) {
                    out.write(content.getBytes());
                    out.flush();
                }
                values.clear();
                values.put(MediaStore.Downloads.IS_PENDING, 0);
                resolver.update(fileUri, values, null, null);

                Log.i(TAG, "File saved to Downloads: " + fileUri.toString());
            } catch (IOException e) {
                Log.e(TAG, "Failed to save file to Downloads", e);
            }
        } else {
            Log.e(TAG, "Unable to create file in Downloads folder");
        }
    }

    private void buildNodeTreeString(AccessibilityNodeInfo node, StringBuilder builder, int depth) {
        if (node == null) return;

        for (int i = 0; i < depth; i++) builder.append("  "); // Indentation
        builder.append("Class: ").append(node.getClassName())
                .append(", Text: ").append(node.getText())
                .append(", ContentDesc: ").append(node.getContentDescription())
                .append(", ID: ").append(node.getViewIdResourceName())
                .append("\n");

        for (int i = 0; i < node.getChildCount(); i++) {
            buildNodeTreeString(node.getChild(i), builder, depth + 1);
        }
    }
    public void performScroll(float xf, float yf) {
        Path swipePath = new Path();
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        float startY = screenHeight * xf; // Bottom
        float endY = screenHeight * yf;   // Top
        float centerX = screenWidth / 2f;
        Log.d(TAG, "Test scroll: " + centerX + "," + startY + " → " + centerX + "," + endY);

        swipePath.moveTo(centerX, startY);
        swipePath.lineTo(centerX, endY);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        int gestureDuration = 150 + random.nextInt(150); // Random gesture duration
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, gestureDuration));

        try {
            MyAccessibilityService service = (MyAccessibilityService) context;
            boolean dispatched = service.dispatchGesture(gestureBuilder.build(), null, null);
            Log.d(TAG, "Simple test dispatch: " + dispatched);
        } catch (Exception e) {
            Log.e(TAG, "Simple test error: " + e.getMessage());
            cleanupAndExit("Scroll Done. Exiting Now", "Success");
        }
    }
}
