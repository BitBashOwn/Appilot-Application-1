package com.example.appilot.automations.Twitter;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.WindowManager;

import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;
import com.example.appilot.services.MyAccessibilityService;
import com.example.appilot.utils.HelperFunctions;
import com.example.appilot.utils.OpenAIClient;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TweetComments {
    private static final String TAG = "TwitterAutomation";
    private final Context context;
    private final Handler handler;
    private final Random random;
    private final PopUpHandler popUpHandler;
    private final MyAccessibilityService service;
    private HelperFunctions helperFunctions;
    private String Task_id = null;
    private String job_id = null;
    private String openAPIKey;
    private String commentType = "natural";
    private List<Object> AccountInputs;
    private int duration;
    private long startTime;
    private double commentProbability;
    private int processCount = 0;
    private OpenAIClient openAIClient;
    private String currentCommentText = null;
    private int comment_limit;
    private int commentCount = 0;
    private int prompt = 1;
    private String todaysDate;
    private static final String PREFS_NAME = "TwitterBotPrefs";
    private static final String PREF_COMMENT_COUNT = "commentCount";
    private static final String PREF_DATE = "dateComments";

    public TweetComments(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs, int duration, double commentProbability, String openAPIKey, int comment_limit, String date) {
        this.context = service;
        this.service = service;
        this.Task_id = taskid;
        this.job_id = jobid;
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();
        this.popUpHandler = new PopUpHandler(this.service, this.handler, this.random, this.helperFunctions);
        this.helperFunctions = new HelperFunctions(context, Task_id, job_id);
        this.AccountInputs = AccountInputs;
        this.duration = duration;
        this.commentProbability = commentProbability;
        this.startTime = System.currentTimeMillis();
        this.openAPIKey = openAPIKey;
        this.openAIClient = new OpenAIClient(openAPIKey);
        this.comment_limit = comment_limit;
        this.todaysDate = date;
        // Load commentCount from SharedPreferences
        commentCount = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(PREF_COMMENT_COUNT, 0);
    }
    public void startCommentAutomation() {
        Log.d(TAG, "Starting Comment Automation with duration: " + duration + " minutes and comment probability: " + commentProbability);
        // Get stored date from SharedPreferences
        String storedDate = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(PREF_DATE, null);
        if (storedDate == null) {
            // First time: store today's date
            commentCount = 0;
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(PREF_DATE, todaysDate)
                    .apply();
            Log.d(TAG, "First run: Storing today's date: " + todaysDate);
        } else {
            Log.d(TAG, "Loaded stored date: " + storedDate + ", Today's date: " + todaysDate);
            if (!todaysDate.equals(storedDate)) {
                // Date changed: reset likeCount and update stored date
                commentCount = 0;
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit()
                        .putInt(PREF_COMMENT_COUNT, 0)
                        .putString(PREF_DATE, todaysDate)
                        .apply();
                Log.d(TAG, "Date changed. Reset likeCount to 0 and updated stored date to: " + todaysDate);
            }
        }
        if (commentCount >= comment_limit) {
            Log.d(TAG, "Comment limit reached: " + commentCount + "/" + comment_limit);
            handler.postDelayed(()->{
                helperFunctions.cleanupAndExit("Comment limit reached: " + commentCount + "/" + comment_limit, "final");
            }, 1000 + random.nextInt(3000));
            return;
        }
        findComments();
    }
    private void findComments() {
        Log.d(TAG, "Starting hierarchy navigation to click target element...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findComments", "error");
            return;
        }
        // Find parent node by resource ID
        String parentNodeId = "android:id/list";
        AccessibilityNodeInfo parentNode = HelperFunctions.findNodeByResourceId(rootNode, parentNodeId);
        if (parentNode != null) {
            Log.d(TAG, "Found parent node with ID: " + parentNodeId);
            // Find the first ViewGroup child under the parent node
            AccessibilityNodeInfo viewGroupChild = null;
            for (int i = 0; i < parentNode.getChildCount(); i++) {
                AccessibilityNodeInfo child = parentNode.getChild(i);
                if (child != null && "android.view.ViewGroup".contentEquals(child.getClassName())) {
                    viewGroupChild = child;
                    break;
                }
            }
            if (viewGroupChild != null) {
                Log.d(TAG, "Found ViewGroup child under parent node, attempting click...");
                double randomValue = random.nextDouble();
                Log.d(TAG, "Random Value: " + randomValue + ", Comment probability: " + commentProbability);
                if (randomValue < commentProbability) {
                    Log.d(TAG, "Clicking Tweet based on probability (" + (commentProbability * 100) + "%)");
                    //findComments();
                    boolean clickSuccess = performClick(viewGroupChild);
                    if (clickSuccess) {
                        Log.d(TAG, "Successfully clicked ViewGroup child, now navigating to comments...");
                        handler.postDelayed(() -> {
                            helperFunctions.performScroll(0.8f, 0.3f);
                            handler.postDelayed(this::findAndLikeComments, 1000 + random.nextInt(3000));
                        }, 1000 + random.nextInt(3000));
                    }
                } else {
                    Log.d(TAG, "Moving to next tweet");
                    // Perform a scroll action to move to the next tweet
                    handler.postDelayed(this::onScroll, 1000 + random.nextInt(3000));
                }
                viewGroupChild.recycle();
            } else {
                Log.d(TAG, "No ViewGroup child found under parent node");
                handler.postDelayed(() -> {
                    helperFunctions.cleanupAndExit("No ViewGroup child found under parent node", "error");
                }, 2000);
            }
            parentNode.recycle();
        } else {
            Log.e(TAG, "Could not find parent node with ID: " + parentNodeId);
            handler.postDelayed(() -> {
                helperFunctions.cleanupAndExit("Could not find parent node in findComments", "error");
            }, 2000);
        }
        rootNode.recycle();
    }
    private void findAndLikeComments() {
        Log.d(TAG, "Starting to find and like comments...");

        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available for comment liking");
            helperFunctions.cleanupAndExit("No root node available for comment liking", "error");
            return;
        }
        // Find the parent node containing comments
        AccessibilityNodeInfo parentNode = HelperFunctions.findNodeByResourceId(rootNode, "android:id/list");

        if (parentNode != null) {
            Log.d(TAG, "Found parent node for comments");
            // Collect all ViewGroup children
            List<AccessibilityNodeInfo> viewGroupChildren = collectViewGroupChildren(parentNode);
            if (!viewGroupChildren.isEmpty()) {
                Log.d(TAG, "Found " + viewGroupChildren.size() + " ViewGroup children");
                processViewGroupsWithProbability(viewGroupChildren, 1);
            } else {
                Log.d(TAG, "No ViewGroup children found");
                helperFunctions.cleanupAndExit("No ViewGroup Children are found for comments", "final");
            }
            parentNode.recycle();
        } else {
            Log.e(TAG, "Could not find parent node for comment liking");
            helperFunctions.cleanupAndExit("Could not find parent node for comment liking", "error");
        }
        rootNode.recycle();
    }
    // Method to collect all ViewGroup children from a parent node
    private List<AccessibilityNodeInfo> collectViewGroupChildren(AccessibilityNodeInfo parentNode) {
        List<AccessibilityNodeInfo> viewGroupChildren = new ArrayList<>();
        if (parentNode == null) {
            return viewGroupChildren;
        }
        try {
            collectViewGroupChildrenRecursive(parentNode, viewGroupChildren);
            Log.d(TAG, "Collected " + viewGroupChildren.size() + " ViewGroup children");
        } catch (Exception e) {
            Log.e(TAG, "Error collecting ViewGroup children: " + e.getMessage());
        }
        return viewGroupChildren;
    }
    // Recursive method to find all ViewGroup children
    private void collectViewGroupChildrenRecursive(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> viewGroupChildren) {
        if (node == null) {
            return;
        }
        // Check if current node has the specific resource ID
        String resourceId = node.getViewIdResourceName();
        if ("com.twitter.android:id/outer_layout_row_view_tweet".equals(resourceId)) {
            viewGroupChildren.add(node);
            Log.d(TAG, "Added node with outer_layout_row_view_tweet ID to collection");
        }
        // Recursively check all children
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                collectViewGroupChildrenRecursive(child, viewGroupChildren);
            }
        }
    }
    // Method to process ViewGroups sequentially with probability check
    private void processViewGroupsWithProbability(List<AccessibilityNodeInfo> viewGroupChildren, int currentIndex) {
        // Check if duration time has been exceeded
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;
        long durationInMillis = duration * 60 * 1000; // Convert duration from minutes to milliseconds

        if (elapsedTime >= durationInMillis) {
            Log.d(TAG, "Duration time exceeded. Elapsed: " + (elapsedTime / 1000) + "s, Duration: " + (durationInMillis / 1000) + "s");
            helperFunctions.cleanupAndExit("Automation completed - Duration time limit reached", "final");
            return;
        }
        // Check if we've processed all ViewGroups
        if (currentIndex >= viewGroupChildren.size()) {
            Log.d(TAG, "All ViewGroups have been checked based on probability.");
            processCount++;
            int maxProcessCount = 1 + random.nextInt(1); // Randomly choose between 1 and 3
            //int maxProcessCount =1;
            if (processCount < maxProcessCount) {
                int randomDelay = 2000 + random.nextInt(3000);
                handler.postDelayed(()->{
                    helperFunctions.performScroll(0.7f,0.3f);
                    handler.postDelayed(()->{
                        // Restart the process after scroll
                        findAndLikeComments();
                    }, randomDelay);
                }, randomDelay);
            } else {
                // After second completion, click back to feed and exit
                int randomDelay = 2000 + random.nextInt(3000);
                handler.postDelayed(()->{
                    clickBackToFeed();
                }, randomDelay);
            }
            return;
        }
        // Get current ViewGroup
        AccessibilityNodeInfo currentViewGroup = viewGroupChildren.get(currentIndex);
        Log.d(TAG, "Processing ViewGroup at index: " + currentIndex);
        // Apply probability check
        double randomValue = random.nextDouble();
        Log.d(TAG, "Random Value: " + randomValue + ", Comment probability: " + commentProbability);
        if (randomValue < commentProbability) {
            Log.d(TAG, "Clicking Like based on probability (" + (commentProbability * 100) + "%)");
            // Find and click the like button in the current ViewGroup
            boolean likeSuccess = clickLikeButtonInViewGroup(currentViewGroup);
            if (likeSuccess) {
                // Extract and log the comment text before clicking reply
                AccessibilityNodeInfo commentTextNode = findNodeByResourceIdInSubtree(currentViewGroup, "com.twitter.android:id/tweet_content_text");
                String extractedText = null;
                if (commentTextNode != null) {
                    CharSequence commentText = commentTextNode.getText();
                    if (commentText != null && commentText.length() > 0) {
                        extractedText = commentText.toString();
                    } else {
                        // Check subchild at index 0 for text
                        if (commentTextNode.getChildCount() > 0) {
                            AccessibilityNodeInfo subChild = commentTextNode.getChild(0);
                            if (subChild != null) {
                                CharSequence subChildText = subChild.getText();
                                if (subChildText != null && subChildText.length() > 0) {
                                    extractedText = subChildText.toString();
                                } else {
                                    CharSequence subChildDesc = subChild.getContentDescription();
                                    if (subChildDesc != null && subChildDesc.length() > 0) {
                                        extractedText = subChildDesc.toString();
                                    }
                                }
                                subChild.recycle();
                            }
                        }
                        if (extractedText == null) {
                            CharSequence desc = commentTextNode.getContentDescription();
                            if (desc != null && desc.length() > 0) {
                                extractedText = desc.toString();
                            } else {
                                for (int i = 0; i < commentTextNode.getChildCount(); i++) {
                                    AccessibilityNodeInfo child = commentTextNode.getChild(i);
                                    if (child != null) {
                                        CharSequence childText = child.getText();
                                        if (childText != null && childText.length() > 0) {
                                            extractedText = childText.toString();
                                            child.recycle();
                                            break;
                                        }
                                        CharSequence childDesc = child.getContentDescription();
                                        if (childDesc != null && childDesc.length() > 0) {
                                            extractedText = childDesc.toString();
                                            child.recycle();
                                            break;
                                        }
                                        child.recycle();
                                    }
                                }
                            }
                        }
                    }
                    commentTextNode.recycle();
                }
                // Try the fallback node if first extraction failed
                if (extractedText == null || extractedText.isEmpty()) {
                    AccessibilityNodeInfo fallbackTextNode = findNodeByResourceIdInSubtree(currentViewGroup, "com.twitter.android:id/text_content_container");
                    if (fallbackTextNode != null) {
                        CharSequence fallbackText = fallbackTextNode.getText();
                        if (fallbackText != null && fallbackText.length() > 0) {
                            extractedText = fallbackText.toString();
                        } else {
                            CharSequence fallbackDesc = fallbackTextNode.getContentDescription();
                            if (fallbackDesc != null && fallbackDesc.length() > 0) {
                                extractedText = fallbackDesc.toString();
                            } else {
                                for (int i = 0; i < fallbackTextNode.getChildCount(); i++) {
                                    AccessibilityNodeInfo child = fallbackTextNode.getChild(i);
                                    if (child != null) {
                                        CharSequence childText = child.getText();
                                        if (childText != null && childText.length() > 0) {
                                            extractedText = childText.toString();
                                            child.recycle();
                                            break;
                                        }
                                        CharSequence childDesc = child.getContentDescription();
                                        if (childDesc != null && childDesc.length() > 0) {
                                            extractedText = childDesc.toString();
                                            child.recycle();
                                            break;
                                        }
                                        child.recycle();
                                    }
                                }
                            }
                        }
                        fallbackTextNode.recycle();
                    }
                }
                Log.d(TAG, "Extracted comment text: " + (extractedText != null ? extractedText : "<empty>"));
                // Store the extracted text for OpenAI processing
                this.currentCommentText = extractedText;
                Log.d(TAG, "Successfully liked comment in node " + currentIndex + ". Now clicking reply button...");
                // After successful like, always click reply button
                int replyDelay = 1000 + random.nextInt(2000);
                handler.postDelayed(() -> {
                    clickReplyButton(currentViewGroup, currentIndex, viewGroupChildren);
                }, replyDelay);
                return;
            } else {
                Log.d(TAG, "Failed to like comment in node " + currentIndex + ". Moving to next node.");
                // Continue to next node after a delay
                int randomDelay = 1000 + random.nextInt(3000);
                handler.postDelayed(() -> {
                    processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
                }, randomDelay);
            }
        } else {
            Log.d(TAG, "Skipping Node "+currentIndex+" based on probability. Moving to next Node.");
            // Move to next ViewGroup after a short delay
            int randomDelay = 500 + random.nextInt(1500);
            handler.postDelayed(() -> {
                processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
            }, randomDelay);
        }
    }
    // Method to find and click the like button within a ViewGroup
    private boolean clickLikeButtonInViewGroup(AccessibilityNodeInfo viewGroup) {
        if (viewGroup == null) {
            Log.e(TAG, "ViewGroup is null, cannot find like button");
            return false;
        }
        Log.d(TAG, "Searching for like button in ViewGroup...");
        // Find the like button by resource ID within this ViewGroup
        AccessibilityNodeInfo likeButton = findNodeByResourceIdInSubtree(viewGroup, "com.twitter.android:id/inline_like");
        if (likeButton != null) {
            Log.d(TAG, "Found like button, attempting to click...");
            boolean clickSuccess = performClick(likeButton);
            if (clickSuccess) {
                Log.d(TAG, "Successfully clicked like button on comment");
                likeButton.recycle();
                return true;
            } else {
                Log.e(TAG, "Failed to click like button");
                likeButton.recycle();
                return false;
            }
        } else {
            Log.e(TAG, "No like button found in this ViewGroup");
            return false;
        }
    }
    // Method to click reply button after first successful like
    private void clickReplyButton(AccessibilityNodeInfo viewGroup, int currentIndex, List<AccessibilityNodeInfo> viewGroupChildren) {
        Log.d(TAG, "Searching for reply button in ViewGroup...");
        // Find the reply button by resource ID within this ViewGroup
        AccessibilityNodeInfo replyButton = findNodeByResourceIdInSubtree(viewGroup, "com.twitter.android:id/inline_reply");
        if (replyButton != null) {
            Log.d(TAG, "Found reply button, attempting to click...");
            boolean clickSuccess = performClick(replyButton);
            if (clickSuccess) {
                Log.d(TAG, "Successfully clicked reply button");
                // Wait for 2 seconds, then check for compose_content node before typing
                handler.postDelayed(() -> {
                    Log.d(TAG, "Now checking for compose_content node before typing comment");
                    AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
                    if (rootNode != null) {
                        AccessibilityNodeInfo composeNode = findNodeByResourceIdInSubtree(rootNode, "com.twitter.android:id/compose_content");
                        if (composeNode != null) {
                            Log.d(TAG, "compose_content node found, jumping to Reply pop up");
                            replyPopUp(composeNode, currentIndex, viewGroupChildren);
                        } else {
                            Log.d(TAG, "compose_content node not found, proceeding to type comment");
                            typeCommentAndSubmit(currentIndex, viewGroupChildren);
                        }
                        rootNode.recycle();
                    } else {
                        Log.e(TAG, "No root node available when checking for compose_content, proceeding to type comment");
                        typeCommentAndSubmit(currentIndex, viewGroupChildren);
                    }
                }, 2000);
            } else {
                Log.e(TAG, "Failed to click reply button, continuing to next node");
                // Continue to next node after a delay
                int randomDelay = 1000 + random.nextInt(3000);
                handler.postDelayed(() -> {
                    processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
                }, randomDelay);
            }
            replyButton.recycle();
        } else {
            Log.d(TAG, "No reply button found in this ViewGroup, continuing to next node");
            // Continue to next node after a delay
            int randomDelay = 1000 + random.nextInt(3000);
            handler.postDelayed(() -> {
                processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
            }, randomDelay);
        }
    }
    private void replyPopUp(AccessibilityNodeInfo composeNode, int currentIndex, List<AccessibilityNodeInfo> viewGroupChildren) {
        if (composeNode != null) {
            Log.d(TAG, "Found parent node with ID: " + composeNode);
            Log.d(TAG, "Parent has " + composeNode.getChildCount() + " children");
            // Navigate to target: Parent -> Child(0)
            AccessibilityNodeInfo targetElement = navigateToPopUp(composeNode);
            if (targetElement != null) {
                Log.d(TAG, "Found target element, attempting click...");
                boolean clickSuccess = performClick(targetElement);
                if (clickSuccess) {
                    // Move to next element in the list after successful click
                    int randomDelay = 1000 + random.nextInt(2000);
                    handler.postDelayed(() -> {
                        processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
                    }, randomDelay);
                }
            } else {
                Log.e(TAG, "Could not navigate to target element");
                helperFunctions.cleanupAndExit("Could not navigate to target element", "error");
            }
            composeNode.recycle();
        } else {
            Log.e(TAG, "Could not find parent node with ID: " + composeNode);
            helperFunctions.cleanupAndExit("Could not find parent node in replyPopUp", "error");
        }
    }
    // Method to navigate to the reply pop-up element
    private AccessibilityNodeInfo navigateToPopUp(AccessibilityNodeInfo parent) {
        try {
            Log.d(TAG, "Navigating: Parent -> Child(0) -> Child(0) -> Child(1) -> Child(0)");

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
            AccessibilityNodeInfo firstView = parent.getChild(0);
            if (firstView == null) {
                Log.e(TAG, "Could not get child(0)");
                return null;
            }
            // Log target element details
            Log.d(TAG, "Found Child(0) of Parent:");
            Log.d(TAG, "Class: " + firstView.getClassName());
            Log.d(TAG, "Text: " + firstView.getText());
            Log.d(TAG, "Clickable: " + firstView.isClickable());

            // Step 2: Get first child (0) of firstChild
            if (firstView.getChildCount() < 1) {
                Log.e(TAG, "Child(0) has no children");
                return null;
            }
            AccessibilityNodeInfo secondView = firstView.getChild(0);
            if (secondView == null) {
                Log.e(TAG, "Could not get child(0) of firstChild");
                return null;
            }
            // Log target element details
            Log.d(TAG, "Found Child(0) of Parent -> Child(0):");
            Log.d(TAG, "Class: " + secondView.getClassName());
            Log.d(TAG, "Text: " + secondView.getText());
            Log.d(TAG, "Clickable: " + secondView.isClickable());
            // Step 3: Get second child (1) of firstChild
            if (secondView.getChildCount() < 2) {
                Log.e(TAG, "Child(0) has no second child");
                return null;
            }
            AccessibilityNodeInfo scrollView = secondView.getChild(1);
            if (scrollView == null) {
                Log.e(TAG, "Could not get child(1) of firstChild");
                return null;
            }
            // Log target element details
            Log.d(TAG, "Found Child(1) of Parent -> Child(0):");
            Log.d(TAG, "Class: " + scrollView.getClassName());
            Log.d(TAG, "Text: " + scrollView.getText());
            Log.d(TAG, "Clickable: " + scrollView.isClickable());
            // Step 4: Get first child (0) of thirdChild
            if (scrollView.getChildCount() < 2) {
                Log.e(TAG, "Child(1) has no children");
                return null;
            }
            AccessibilityNodeInfo targetElement = scrollView.getChild(3);
            if (targetElement == null) {
                Log.e(TAG, "Could not get child(0) of thirdChild");
                return null;
            }
            // Log target element details
            Log.d(TAG, "Found Child(0) of Parent -> Child(0) -> Child(1):");
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
    private void typeCommentAndSubmit(int currentIndex, List<AccessibilityNodeInfo> viewGroupChildren) {
        Log.d(TAG, "Starting to type comment...");

        // Check if we have extracted comment text to send to OpenAI
        if (currentCommentText != null && !currentCommentText.isEmpty()) {
            Log.d(TAG, "Generating dynamic comment using OpenAI for text: " + currentCommentText);
            // Use OpenAI to generate a dynamic comment
            openAIClient.generateComment(currentCommentText, commentType, prompt, new OpenAIClient.OpenAICallback() {
                @Override
                public void onSuccess(String generatedComment) {
                    Log.d(TAG, "OpenAI generated comment: " + generatedComment);
                    // Type the AI-generated comment
                    handler.post(() -> {
                        boolean typingSuccess = typeTextLikeHuman(generatedComment, currentIndex, viewGroupChildren);
                        if (typingSuccess) {
                            Log.d(TAG, "Successfully started typing AI-generated comment: " + generatedComment);
                        } else {
                            Log.e(TAG, "Failed to start typing AI-generated comment, moving to next node");
                            handler.postDelayed(() -> {
                                processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
                            }, 1000 + random.nextInt(2000));
                        }
                    });
                }
                @Override
                public void onError(String error) {
                    Log.e(TAG, "OpenAI error: " + error);
                    // Fallback to default comment if OpenAI fails
                    String fallbackComment = "Great!";
                    Log.d(TAG, "Using fallback comment: " + fallbackComment);
                    handler.post(() -> {
                        boolean typingSuccess = typeTextLikeHuman(fallbackComment, currentIndex, viewGroupChildren);
                        if (typingSuccess) {
                            Log.d(TAG, "Successfully started typing fallback comment: " + fallbackComment);
                        } else {
                            Log.e(TAG, "Failed to start typing fallback comment, moving to next node");
                            handler.postDelayed(() -> {
                                processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
                            }, 1000 + random.nextInt(2000));
                        }
                    });
                }
            });
        } else {
            // If no comment text was extracted, use a default comment
            Log.d(TAG, "No comment text extracted, using default comment");
            String defaultComment = "Interesting!";
            boolean typingSuccess = typeTextLikeHuman(defaultComment, currentIndex, viewGroupChildren);
            if (typingSuccess) {
                Log.d(TAG, "Successfully started typing default comment: " + defaultComment);
            } else {
                Log.e(TAG, "Failed to start typing default comment, moving to next node");
                handler.postDelayed(() -> {
                    processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
                }, 1000 + random.nextInt(2000));
            }
        }
    }
    // Method to type text like a human with realistic delays
    private boolean typeTextLikeHuman(String text, int currentIndex, List<AccessibilityNodeInfo> viewGroupChildren) {
        if (text == null || text.isEmpty()) {
            Log.e(TAG, "No text to type");
            return false;
        }
        try {
            // Find the text input field first
            AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
            if (rootNode == null) {
                Log.e(TAG, "No root node available for typing");
                return false;
            }
            // Look for editable text field (usually EditText)
            AccessibilityNodeInfo textField = findEditableTextField(rootNode);
            if (textField == null) {
                Log.e(TAG, "Could not find text input field");
                rootNode.recycle();
                return false;
            }
            // Focus on the text field
            textField.performAction(AccessibilityNodeInfo.ACTION_FOCUS);

            Log.d(TAG, "Clearing existing text in the field...");
            // Create bundle for text input
            Bundle clearBundle = new Bundle();
            clearBundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "");
            boolean clearSuccess = textField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, clearBundle);

            textField.recycle();
            rootNode.recycle();

            if (clearSuccess) {
                Log.d(TAG, "Successfully cleared existing text");
                handler.postDelayed(()->{
                    typeCharacterByCharacter(text, 0, new StringBuilder(), currentIndex, viewGroupChildren);
                }, 300 + random.nextInt(200));
                return  true;
            } else {
                Log.d(TAG,"Failed to clear existing text, but continuing..");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while typing text: " + e.getMessage());
            return false;
        }
    }
    private void typeCharacterByCharacter(String text, int charIndex, StringBuilder typedSoFar,
                                          int currentIndex, List<AccessibilityNodeInfo> viewGroupChildren) {
        if (charIndex >= text.length()) {
            Log.d(TAG, "Finished typing all characters");
            handler.postDelayed(() -> {
                Log.d(TAG, "Typing completed, now clicking tweet button to submit comment");
                clickTweetButtonAndContinue(currentIndex, viewGroupChildren);
            }, 2000 + random.nextInt(2000));
            return;
        }
        try {
            String currentChar = String.valueOf(text.charAt(charIndex));
            typedSoFar.append(currentChar);  // Maintain our own internal typed state

            AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
            if (rootNode == null) {
                Log.e(TAG, "No root node available during typing");
                handler.postDelayed(()->{
                    processViewGroupsWithProbability(viewGroupChildren, currentIndex+1);
                }, 1000);
                return;
            }
            AccessibilityNodeInfo textField = findEditableTextField(rootNode);
            if (textField == null) {
                Log.e(TAG, "Could not find text input field during typing");
                rootNode.recycle();
                handler.postDelayed(()->{
                    processViewGroupsWithProbability(viewGroupChildren, currentIndex+1);
                }, 1000);
                return;
            }
            // Set the full typed text so far
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, typedSoFar.toString());
            boolean success = textField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);

            textField.recycle();
            rootNode.recycle();

            int delay = calculateTypingDelay(currentChar, charIndex);
            if (success) {
                Log.d(TAG, "Typed character: '" + currentChar + "' (index " + charIndex + ")");
            } else {
                Log.e(TAG, "Failed to type character: '" + currentChar + "', retrying...");
            }
            // Schedule next character regardless of success (soft retry logic)
            handler.postDelayed(() -> {
                typeCharacterByCharacter(text, charIndex + 1, typedSoFar, currentIndex, viewGroupChildren);
            }, delay);
        } catch (Exception e) {
            Log.e(TAG, "Error typing character at index " + charIndex + ": " + e.getMessage());
            handler.postDelayed(() -> {
                typeCharacterByCharacter(text, charIndex + 1, typedSoFar, currentIndex, viewGroupChildren);
            }, 300 + random.nextInt(400));
        }
    }
    // Method to calculate realistic typing delays based on character and position
    private int calculateTypingDelay(String character, int position) {
        int baseDelay = 60; // Base typing speed (80-120ms per character)
        int randomVariation = random.nextInt(20); // Add 0-40ms random variation
        // Add extra delays for certain characters
        if (character.equals(" ")) {
            // Slight pause after spaces
            baseDelay += 20 + random.nextInt(30);
        } else if (character.equals(".") || character.equals("!") || character.equals("?")) {
            // Longer pause after punctuation
            baseDelay += 50 + random.nextInt(100);
        } else if (character.equals(",")) {
            // Medium pause after commas
            baseDelay += 30 + random.nextInt(50);
        }
        // Occasional longer pauses (simulating thinking)
        if (random.nextInt(10) == 0) { // 10% chance
            baseDelay += 200 + random.nextInt(300); // 200-500ms thinking pause
            Log.d(TAG, "Added thinking pause at position " + position);
        }
        // Very occasionally add a correction pause (simulating backspace/retype)
        if (random.nextInt(20) == 0) { // 5% chance
            baseDelay += 400 + random.nextInt(600); // 400-1000ms correction pause
            Log.d(TAG, "Added correction pause at position " + position);
        }
        return baseDelay + randomVariation;
    }
    // Method to click tweet button and continue to next element
    private void clickTweetButtonAndContinue(int currentIndex, List<AccessibilityNodeInfo> viewGroupChildren) {
        Log.d(TAG, "Searching for tweet button...");

        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available for tweet button");
            // Continue to next node even if tweet button fails
            int randomDelay = 1000 + random.nextInt(3000);
            handler.postDelayed(() -> {
                processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
            }, randomDelay);
            return;
        }
        // Find the tweet button by resource ID
        AccessibilityNodeInfo tweetButton = HelperFunctions.findNodeByResourceId(rootNode, "com.twitter.android:id/button_tweet");
        if (tweetButton != null) {
            Log.d(TAG, "Found tweet button, attempting to click...");
            boolean clickSuccess = performClick(tweetButton);
            if (clickSuccess) {
                commentCount++;
                saveCommentCount();
                Log.d(TAG, "Comment count incremented: " + commentCount);
                if (commentCount >= comment_limit) {
                    Log.e(TAG, "You have reached the comment limit");
                    handler.postDelayed(()->{
                        helperFunctions.cleanupAndExit("Comment limit reached: " + commentCount + "/" + comment_limit, "error");
                    }, 1000 + random.nextInt(3000));
                    tweetButton.recycle();
                    rootNode.recycle();
                    return;
                }
                Log.d(TAG, "Tweet button clicked successfully. Waiting before continuing to next node...");
                // Wait for 3 seconds, then continue to next node
                handler.postDelayed(() -> {
                    Log.d(TAG, "Comment submitted successfully. Moving to next node...");
                    processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
                }, 2000 + random.nextInt(3000));
            } else {
                Log.e(TAG, "Failed to click tweet button, but continuing to next node");
                handler.postDelayed(() -> {
                    processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
                }, 2000 + random.nextInt(3000));
            }
            tweetButton.recycle();
        } else {
            Log.e(TAG, "Could not find tweet button, continuing to next node");
            handler.postDelayed(() -> {
                processViewGroupsWithProbability(viewGroupChildren, currentIndex + 1);
            }, 1000 + random.nextInt(3000));
        }
        rootNode.recycle();
    }
    // Helper method to find a node by resource ID within a specific subtree
    private AccessibilityNodeInfo findNodeByResourceIdInSubtree(AccessibilityNodeInfo root, String resourceId) {
        if (root == null || resourceId == null) {
            return null;
        }
        // Check if current node matches
        if (resourceId.equals(root.getViewIdResourceName())) {
            return root;
        }
        // Recursively search children
        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = root.getChild(i);
            if (child != null) {
                AccessibilityNodeInfo result = findNodeByResourceIdInSubtree(child, resourceId);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    private void clickBackToFeed() {
        Log.d(TAG, "Starting hierarchy navigation to click target element...");

        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in clickBackToFeed", "error");
            return;
        }
        // Find parent node by resource ID
        String parentNodeId = "com.twitter.android:id/toolbar";
        AccessibilityNodeInfo parentNode = HelperFunctions.findNodeByResourceId(rootNode, parentNodeId);
        if (parentNode != null) {
            Log.d(TAG, "Found parent node with ID: " + parentNodeId);
            Log.d(TAG, "Parent has " + parentNode.getChildCount() + " children");
            // Navigate to target: Parent -> Child(0)
            AccessibilityNodeInfo targetElement = navigateBackToFeed(parentNode);
            if (targetElement != null) {
                Log.d(TAG, "Found target element, attempting click...");
                boolean clickSuccess = performClick(targetElement);
                if (clickSuccess) {
                    Log.d(TAG, "Back button clicked successfully. Waiting for profile to load...");
//                    int randomDelay = 1000 + random.nextInt(5000);
//                    handler.postDelayed(this::onScroll, randomDelay);
                    int randomDelay = 2000 + random.nextInt(3000);
                    handler.postDelayed(()->{
                        helperFunctions.performScroll(0.7f,0.3f);
                        handler.postDelayed(this::findNewPosted, randomDelay);
                    }, randomDelay);
                }
            } else {
                Log.e(TAG, "Could not navigate to target element");
                helperFunctions.cleanupAndExit("Could not navigate to target element", "error");
            }
            parentNode.recycle();
        } else {
            Log.e(TAG, "Could not find parent node with ID: " + parentNodeId);
            helperFunctions.cleanupAndExit("Could not find parent node", "error");
        }
        rootNode.recycle();
    }
    public void findNewPosted() {
        Log.d(TAG, "Searching for posted Button");
        // Verify one more time that we have root access
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available in like button");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findPosted", "error");
            return;
        }
        Log.d(TAG, "Root node available, proceeding with Click logic");
        String nodeId = "com.twitter.android:id/text_banner_layout";
        AccessibilityNodeInfo targetElement = HelperFunctions.findNodeByResourceId(rootNode, nodeId);
        if (targetElement != null) {
            boolean clickSuccess = performClick(targetElement);
            if (clickSuccess) {
                Log.d(TAG, "Image Container clicked successfully. Waiting for profile to load...");
                int randomDelay = 2000 + random.nextInt(5000);
                handler.postDelayed(()->{
                    handler.postDelayed(this::findComments, randomDelay);
                }, randomDelay);
            }
        } else {
            findComments();
        }
        rootNode.recycle();
    }
    private AccessibilityNodeInfo navigateBackToFeed(AccessibilityNodeInfo parent) {
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
    private boolean performClick(AccessibilityNodeInfo targetElement) {
        boolean clicked = false;
        try {
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

                // Optional: clamp to screen to avoid status/nav bars
                DisplayMetrics dm = context.getResources().getDisplayMetrics();
                clickX = Math.max(2, Math.min(clickX, dm.widthPixels - 2));
                clickY = Math.max(2, Math.min(clickY, dm.heightPixels - 2));

                Log.d(TAG, "Gesture click coordinates: (" + clickX + ", " + clickY + ")");
                clicked = performGestureClick(clickX, clickY); // <-- now real success/failure
                Log.d(TAG, "Gesture concluded with success=" + clicked);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clicking target element: " + e.getMessage());
            helperFunctions.cleanupAndExit("Error clicking target element", "error");
        } finally {
            helperFunctions.safelyRecycleNode(targetElement);
        }
        return clicked;
    }
    private boolean performGestureClick(int x, int y) {
        Log.d(TAG, "Performing gesture click at: " + x + ", " + y);

        Path clickPath = new Path();
        clickPath.moveTo(x, y);
        clickPath.lineTo(x + 1, y + 1);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, 90));

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(false);

        try {
            MyAccessibilityService service = (MyAccessibilityService) context;
            boolean dispatched = service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    Log.d(TAG, "Gesture click completed");
                    success.set(true);
                    latch.countDown();
                }
                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    Log.e(TAG, "Gesture click cancelled");
                    success.set(false);
                    latch.countDown();
                }
            }, null);
            Log.d(TAG, "Gesture click dispatch result: " + dispatched);
            if (!dispatched) return false;
            // 🔑 If we're on the main thread, don't block the callback.
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Log.d(TAG, "Main thread: skipping await, returning dispatch result");
                return true; // or 'dispatched'
            }
            // Wait (briefly) for the callback to know if it really completed
            latch.await(2000, TimeUnit.MILLISECONDS); // <= keep short
            return success.get();
        } catch (Exception e) {
            Log.e(TAG, "Error in gesture click: " + e.getMessage());
            return false;
        }
    }
    public void onScroll() {
        // Check if duration time has been exceeded
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;
        long durationInMillis = duration * 60 * 1000; // Convert duration from minutes to milliseconds

        if (elapsedTime >= durationInMillis) {
            Log.d(TAG, "Duration time exceeded in onScroll. Elapsed: " + (elapsedTime / 1000) + "s, Duration: " + (durationInMillis / 1000) + "s");
            helperFunctions.cleanupAndExit("Automation completed - Duration time limit reached for Comments Like and Reply", "final");
            return;
        }
        handler.postDelayed(() -> {
            Log.d(TAG, "liking... (scrolling)");
            float endY = 0.0f + random.nextFloat() * (0.7f);
            float startY = endY + 0.3f;
            helperFunctions.performScroll(startY, endY);
            handler.postDelayed(this::findComments, 1000 + random.nextInt(2000));
        }, 1000 + random.nextInt(2000));
    }
    private AccessibilityNodeInfo findEditableTextField(AccessibilityNodeInfo root) {
        if (root == null) {
            return null;
        }
        // Check if current node is editable
        if (root.isEditable() && root.isFocusable()) {
            Log.d(TAG, "Found editable text field: " + root.getClassName());
            return root;
        }
        // Recursively search children
        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = root.getChild(i);
            if (child != null) {
                AccessibilityNodeInfo result = findEditableTextField(child);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    // Helper method to save commentCount
    private void saveCommentCount() {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(PREF_COMMENT_COUNT, commentCount)
                .apply();
    }
}
