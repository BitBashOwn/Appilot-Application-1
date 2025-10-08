package com.example.appilot.automations.Twitter;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;
import com.example.appilot.services.MyAccessibilityService;
import com.example.appilot.utils.HelperFunctions;
import com.example.appilot.utils.OpenAIClient;

import org.json.JSONArray;

import java.util.List;
import java.util.Random;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

public class Retweet {
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
    private String commentType = "Natural reply";
    private String tweetText;
    private JSONArray userArray;
    private List<Object> AccountInputs;
    private int duration;
    private int Max_Retry = 3;
    private int retryCount = 0;
    private int prompt = 0;
    private long startTime;
    private int currentUrlIndex = 0;
    private boolean like;
    private boolean repost;
    private boolean comment;
    private boolean quote;

    public Retweet(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs, int duration, JSONArray inputArray, String openAPIKey){
        this.context = service;
        this.service = service;
        this.Task_id = taskid;
        this.job_id = jobid;
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();
        this.popUpHandler = new PopUpHandler(this.service, this.handler, this.random, this.helperFunctions);
        this.helperFunctions = new HelperFunctions(context, Task_id, job_id);
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
        this.openAPIKey = openAPIKey;
        this.userArray = inputArray;
    }
    public void startRetweetAutomation(){
        Log.d(TAG, "Starting Twitter Retweet Automation");
        Log.d(TAG, "Input Array: " + userArray.toString());
        processNextUrl();
    }
    private void processNextUrl() {
        Log.d(TAG,"Duration : " + duration + " minutes");
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000 / 60;
        Log.d(TAG, "Elapsed Time: " + elapsedTime + " minutes");
        if (elapsedTime >= duration) {
            Log.d(TAG, "Reached duration. Exiting...");
            handler.postDelayed(() -> helperFunctions.cleanupAndExit("Duration Completed. Exiting activity.", "final"), 1000 + random.nextInt(3000));
            return;
        }
        if (userArray == null || userArray.length() == 0) {
            Log.d(TAG, "No URLs present in userArray");
            helperFunctions.cleanupAndExit("no new URL present", "final");
            return;
        }
        if (currentUrlIndex < userArray.length()) {
            org.json.JSONObject obj = userArray.optJSONObject(currentUrlIndex);
            if (obj != null) {
                String url = obj.optString("url", null);
                like = obj.optBoolean("like", false);
                repost = obj.optBoolean("repost", false);
                comment = obj.optBoolean("comment", false);
                quote = obj.optBoolean("quote", false);
                if (url != null && !url.isEmpty()) {
                    Log.d(TAG, "Launching URL at index " + currentUrlIndex + ": " + url);
                    launchIntent(url);
                } else {
                    Log.e(TAG, "URL at index " + currentUrlIndex + " is null or empty, skipping.");
                    currentUrlIndex++;
                    processNextUrl();
                }
            } else {
                Log.e(TAG, "JSONObject at index " + currentUrlIndex + " is null, skipping.");
                currentUrlIndex++;
                processNextUrl();
            }
        } else {
            Log.d(TAG, "No new URL present");
            handler.postDelayed(()->{
                helperFunctions.cleanupAndExit("no new URL present", "final");
            }, 2000+ random.nextInt(3000));
        }
    }
    private void launchIntent(String url) {
        try {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url));
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            Log.d(TAG, "Launched URL: " + url);
            handler.postDelayed(this::findTweetNode, 5000+ random.nextInt(5000));
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch URL: " + url + ", error: " + e.getMessage());
        }
    }
    private void findTweetNode() {
        Log.d(TAG, "Searching for Whole Tweet node...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findTweetNode", "error");
            return;
        }
        String searchFollowButton = "com.twitter.android:id/tweet_auto_playable_content_parent";
        AccessibilityNodeInfo searchFollow = HelperFunctions.findNodeByResourceId(rootNode, searchFollowButton);
        if (searchFollow != null) {
            tweetText=extractTweetText();
            Log.d(TAG, "Extracted Tweet Text: "+tweetText);
            Log.d(TAG, "Find the Tweet Node, proceeding to click like...");
            handler.postDelayed(()->{
                try{
                    findAndClickLike();
                } catch (Exception e) {
                    Log.e(TAG, "Error in findAndClickLike: " + e.getMessage());
                    helperFunctions.cleanupAndExit("Error in findAndClickLike: " + e.getMessage(), "error");
                }
            },1000+random.nextInt(1000));
        } else {
            Log.d(TAG, "Tweet node not found");
            if (retryCount < Max_Retry) {
                retryCount ++;
                int randomDelay = 1000 + random.nextInt(1000);
                handler.postDelayed(()->{
                    helperFunctions.performScroll(0.5f,0.3f);
                    handler.postDelayed(()->{
                        try{
                            findTweetNode();
                        } catch (Exception e) {
                            Log.e(TAG, "Error in findTweetNode3.2: " + e.getMessage());
                            helperFunctions.cleanupAndExit("Error in findTweetNode3.2: " + e.getMessage(), "error");
                        }
                    },1000+random.nextInt(1000));
                },randomDelay);
            } else {
                Log.e(TAG," Max retries reached for finding tweet node. Moving to next user if available.");
                currentUrlIndex ++;
                handler.postDelayed(this::processNextUrl, 5000+ random.nextInt(5000));
            }
        }
        rootNode.recycle();
    }
    private void findAndClickLike() {
        if (!like) {
            Log.d(TAG, "Like action not required, moving to Repost if needed");
            handler.postDelayed(()->{
                try{
                    findAndClickRepost();
                } catch (Exception e) {
                    Log.e(TAG, "Error in findAndClickRepost: " + e.getMessage());
                    helperFunctions.cleanupAndExit("Error in findAndClickRepost: " + e.getMessage(), "error");
                }
            },2000+random.nextInt(3000));
            return;
        }
        Log.d(TAG, "Searching for Like Button");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickLike", "error");
            return;
        }
        String profileResourceId = "com.twitter.android:id/inline_like";
        AccessibilityNodeInfo likeButton = HelperFunctions.findNodeByResourceId(rootNode, profileResourceId);
        if (likeButton != null) {
            Log.d(TAG, "Found Like Button, attempting click");
            boolean clickSuccess = performClick(likeButton);
            if (clickSuccess) {
                handler.postDelayed(()->{
                    try{
                        findAndClickRepost();
                    } catch (Exception e) {
                        Log.e(TAG, "Error in findAndClickRepost: " + e.getMessage());
                        helperFunctions.cleanupAndExit("Error in findAndClickRepost: " + e.getMessage(), "error");
                    }
                },2000+random.nextInt(3000));
            }
        } else {
            Log.d(TAG, "Like Button is not found");
            handler.postDelayed(()->{
                try{
                    findAndClickRepost();
                } catch (Exception e) {
                    Log.e(TAG, "Error in findAndClickRepost: " + e.getMessage());
                    helperFunctions.cleanupAndExit("Error in findAndClickRepost: " + e.getMessage(), "error");
                }
            }, 2000);
        }
        rootNode.recycle();
    }
    private void findAndClickRepost() {
        if (!repost && !quote) {
            Log.d(TAG, "Repost/Quote action not required, moving to Comment if needed");
            if (comment && tweetText != null) {
                handler.postDelayed(() -> {
                    findAndClickComment(tweetText);
                }, 2000 + random.nextInt(3000));
            } else {
                currentUrlIndex ++;
                handler.postDelayed(this::processNextUrl, 5000+ random.nextInt(5000));
            }
            return;
        }
        Log.d(TAG, "Searching for Repost Button");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickRepost", "error");
            return;
        }
        String profileResourceId = "com.twitter.android:id/inline_retweet";
        AccessibilityNodeInfo repostButton = HelperFunctions.findNodeByResourceId(rootNode, profileResourceId);
        if (repostButton != null) {
            Log.d(TAG, "Found Repost Button, attempting click");
            boolean clickSuccess = performClick(repostButton);
            if (clickSuccess) {
                int randomDelay = 1000 + random.nextInt(3000);
                handler.postDelayed(()->{
                    try{
                        handleRepostPopup();
                    } catch (Exception e) {
                        Log.e(TAG, "Error in clickRepostPopup: " + e.getMessage());
                        helperFunctions.cleanupAndExit("Error in clickRepostPopup: " + e.getMessage(), "error");
                    }
                }, randomDelay);
            }
        } else {
            Log.d(TAG, "Repost Button is not found");
            if (tweetText != null) {
                handler.postDelayed(() -> {
                    findAndClickComment(tweetText);
                }, 2000 + random.nextInt(3000));
            } else {
                currentUrlIndex ++;
                handler.postDelayed(this::processNextUrl, 5000+ random.nextInt(5000));
            }
        }
        rootNode.recycle();
    }
    private void handleRepostPopup(){
        Log.d(TAG, "Starting hierarchy navigation to Repost element for popup...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in clickRepostPopUp", "error");
            return;
        }
        String parentNodeId = "com.twitter.android:id/action_sheet_recycler_view";
        AccessibilityNodeInfo parentNode = HelperFunctions.findNodeByResourceId(rootNode, parentNodeId);
        if (parentNode != null){
            if (quote) {
                int childIndex = 1;
                Log.d(TAG, "Attempting to click Quote");
                handler.postDelayed(()->{
                    clickQuoteInPopup(parentNode, childIndex);
                }, 2000+ random.nextInt(3000));
            } else if (repost) {
                int childIndex = 0;
                Log.d(TAG, "Attempting to click Repost");
                handler.postDelayed(()->{
                    clickRepostInPopup(parentNode, childIndex);
                }, 2000+ random.nextInt(3000));
            }
        }
    }
    private void clickRepostInPopup(AccessibilityNodeInfo parentNode, int childIndex){
        AccessibilityNodeInfo targetElement = navigateToProfile(parentNode, childIndex);
        if (targetElement != null) {
            Log.d(TAG, "Found target element, attempting click...");
            boolean clickSuccess = performClick(targetElement);
            targetElement.recycle();
            if (clickSuccess) {
                Log.d(TAG, "Repost clicked successfully. Extracting tweet content and calling comment...");
                int randomDelay = 2000 + random.nextInt(3000);
                if (tweetText != null) {
                    handler.postDelayed(() -> {
                        findAndClickComment(tweetText);
                    }, randomDelay);
                } else {
                    currentUrlIndex ++;
                    handler.postDelayed(this::processNextUrl, 5000+ random.nextInt(5000));
                }
            }
        } else {
            Log.e(TAG, "Click on target element failed");
            helperFunctions.cleanupAndExit("Click on target element failed", "error");
        }
        parentNode.recycle();
    }
    private void clickQuoteInPopup(AccessibilityNodeInfo parentNode, int childIndex){
        AccessibilityNodeInfo targetElement = navigateToProfile(parentNode, childIndex);
        if (targetElement != null) {
            Log.d(TAG, "Found target element, attempting click...");
            boolean clickSuccess = performClick(targetElement);
            targetElement.recycle();
            if (clickSuccess) {
                Log.d(TAG, "Quote clicked successfully. Moving to type Quote...");
                prompt = 2;
                int randomDelay = 2000 + random.nextInt(3000);
                handler.postDelayed(()->{
                    sendTextToOpenAIAndType(tweetText);
                },randomDelay);
            }
        } else {
            Log.e(TAG, "Click on target element failed");
            helperFunctions.cleanupAndExit("Click on target element failed", "error");
        }
        parentNode.recycle();
    }
    private void findAndClickRepostInQuote() {
        Log.d(TAG, "Searching for Repost Button in Quote Tab");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickRepostInQuote", "error");
            return;
        }
        String repost2ResourceId = "com.twitter.android:id/button_tweet";
        AccessibilityNodeInfo repost2Button = HelperFunctions.findNodeByResourceId(rootNode, repost2ResourceId);
        if (repost2Button != null) {
            Log.d(TAG, "Found Repost Button in Quote, attempting click");
            boolean clickSuccess = performClick(repost2Button);
            if (clickSuccess) {
                if (tweetText != null) {
                    int randomDelay = 2000 + random.nextInt(3000);
                    handler.postDelayed(() -> {
                        findAndClickComment(tweetText);
                    }, randomDelay);
                } else {
                    currentUrlIndex ++;
                    handler.postDelayed(this::processNextUrl, 5000+ random.nextInt(5000));
                }
            }
        } else {
            Log.d(TAG, "Repost Button in Quote is not found");
            handler.postDelayed(()->{
                helperFunctions.cleanupAndExit("Repost button in quote not found, exiting...", "error");
            }, 2000);
        }
        rootNode.recycle();
    }
    private void findAndClickComment(String text) {
        if (!comment) {
            Log.d(TAG, "Comment action not required, moving to next URL if available");
            currentUrlIndex ++;
            handler.postDelayed(this::processNextUrl, 5000+random.nextInt(5000));
            return;
        }
        Log.d(TAG, "Searching for Comment Button");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickComment", "error");
            return;
        }
        String profileResourceId = "com.twitter.android:id/inline_reply";
        AccessibilityNodeInfo replyButton = HelperFunctions.findNodeByResourceId(rootNode, profileResourceId);
        if (replyButton != null) {
            Log.d(TAG, "Found Comment Button, attempting click");
            boolean clickSuccess = performClick(replyButton);
            if (clickSuccess) {
                Log.d(TAG, "Successfully clicked reply button");
                prompt = 1;
                // Wait for 2 seconds, then check for compose_content node before typing
                handler.postDelayed(() -> {
                    try {
                        Log.d(TAG, "Now checking for compose_content node before typing comment");
                        AccessibilityNodeInfo rootNode1 = service.getRootInActiveWindow();
                        if (rootNode1 != null) {
                            AccessibilityNodeInfo composeNode1 = findNodeByResourceIdInSubtree(rootNode1, "com.twitter.android:id/compose_content");
                            if (composeNode1 != null) {
                                Log.d(TAG, "compose_content node found, jumping to Reply pop up");
                                replyPopUp(composeNode1);
                            } else {
                                Log.d(TAG, "compose_content node not found, proceeding to type comment");
                                sendTextToOpenAIAndType(text);
                            }
                            rootNode1.recycle();
                        } else {
                            Log.e(TAG, "No root node available when checking for compose_content, proceeding to type comment");
                            sendTextToOpenAIAndType(text);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in private comments: " + e.getMessage());
                        helperFunctions.cleanupAndExit("Error in private comments: " + e.getMessage(), "error");
                    }
                }, 2000);
            }
        } else {
            Log.d(TAG, "Comment Button is not found");
            currentUrlIndex ++;
            handler.postDelayed(this::processNextUrl, 5000+random.nextInt(5000));
        }
        rootNode.recycle();
    }
    private void replyPopUp(AccessibilityNodeInfo composeNode) {
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
                    int randomDelay = 5000 + random.nextInt(5000);
                    currentUrlIndex ++;
                    handler.postDelayed(this::processNextUrl,randomDelay);
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
    private void sendTextToOpenAIAndType(String tweetText) {
        if (tweetText == null || tweetText.isEmpty()) {
            Log.d(TAG, "No tweet text to send to OpenAI, using default comment");
            typeTextLikeHuman("Interesting tweet!");
            return;
        }
        // Use OpenAIClient if available, else fallback
        if (openAPIKey != null && !openAPIKey.isEmpty()) {
            OpenAIClient openAIClient = new OpenAIClient(openAPIKey);
            openAIClient.generateComment(tweetText, commentType, prompt, new OpenAIClient.OpenAICallback() {
                @Override
                public void onSuccess(String generatedComment) {
                    Log.d(TAG, "OpenAI generated comment: " + generatedComment);
                    handler.post(() -> typeTextLikeHuman(generatedComment));
                }
                @Override
                public void onError(String error) {
                    Log.e(TAG, "OpenAI error: " + error);
                    helperFunctions.cleanupAndExit("Failed to get response from OpenAI", "error");
                }
            });
        } else {
            Log.d(TAG, "No OpenAI key, using fallback comment");
            helperFunctions.cleanupAndExit("No OpenAI key provided, cannot generate comment", "error");
        }
    }
    // Types the given text in the comment field, character by character
    private void typeTextLikeHuman(String text) {
        if (text == null || text.isEmpty()) return;
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) return;
        AccessibilityNodeInfo textField = findEditableTextField(rootNode);
        if (textField == null) {
            rootNode.recycle();
            return;
        }
        textField.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
        Bundle clearBundle = new Bundle();
        clearBundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "");
        textField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, clearBundle);
        textField.recycle();
        rootNode.recycle();
        handler.postDelayed(() -> typeCommentCharacterByCharacter(text, 0, new StringBuilder()), 300 + random.nextInt(200));
    }
    private void typeCommentCharacterByCharacter(String text, int charIndex, StringBuilder typedSoFar) {
        if (charIndex >= text.length()) {
            Log.d(TAG, "Finished typing comment. Going to click reply button...");
            //handler.postDelayed(this::findAndClickReplyButton, 2000 + random.nextInt(5000));
            if (prompt == 1) {
                handler.postDelayed(()->{
                    Log.d(TAG, "Inside handler.postDelayed lambda for findAndClickReplyButton");
                    try{
                        findAndClickReplyButton();
                    } catch (Exception e) {
                        Log.e(TAG, "Error in findAndClickReplyButton: " + e.getMessage());
                        helperFunctions.cleanupAndExit("Error in findAndClickReplyButton: " + e.getMessage(), "error");
                    }
                }, 2000 + random.nextInt(5000));
            } else if (prompt == 2) {
                handler.postDelayed(()->{
                    Log.d(TAG, "Inside handler.postDelayed lambda for findAndClickReplyButton in quote");
                    try{
                        findAndClickRepostInQuote();
                    } catch (Exception e) {
                        Log.e(TAG, "Error in findAndClickRepostInQuote: " + e.getMessage());
                        helperFunctions.cleanupAndExit("Error in findAndClickRepostInQuote: " + e.getMessage(), "error");
                    }
                }, 2000 + random.nextInt(5000));
            }
            return;
        }
        String currentChar = String.valueOf(text.charAt(charIndex));
        typedSoFar.append(currentChar);
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) return;
        AccessibilityNodeInfo textField = findEditableTextField(rootNode);
        if (textField == null) {
            rootNode.recycle();
            return;
        }
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, typedSoFar.toString());
        textField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        textField.recycle();
        rootNode.recycle();
        int delay = 150 + random.nextInt(200);
        handler.postDelayed(() -> typeCommentCharacterByCharacter(text, charIndex + 1, typedSoFar), delay);
    }
    private void findAndClickReplyButton() {
        Log.d(TAG, "Searching for Reply Button...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickReplyButton", "error");
            return;
        }

        String replyButtonId = "com.twitter.android:id/button_tweet";
        AccessibilityNodeInfo replyButton = HelperFunctions.findNodeByResourceId(rootNode, replyButtonId);
        if (replyButton != null) {
            Log.d(TAG, "Found Reply Button, attempting click...");
            boolean clickSuccess = performClick(replyButton);
            if (clickSuccess) {
                Log.d(TAG, "Reply Button clicked. Waiting for the reply to load...");
                int randomDelay = 5000 + random.nextInt(5000);
                currentUrlIndex ++;
                handler.postDelayed(this::processNextUrl,randomDelay);
            }
        } else {
            Log.d(TAG, "Reply Button not found");
            int randomDelay = 2000 + random.nextInt(3000);
            handler.postDelayed(() -> {
                helperFunctions.cleanupAndExit("Could not find the Reply Button, exiting the activity", "error");
            }, randomDelay);
        }
        rootNode.recycle();
    }


    //    Helper Functions
    private AccessibilityNodeInfo findEditableTextField(AccessibilityNodeInfo root) {
        if (root == null) return null;
        if (root.isEditable() && root.isFocusable()) return root;
        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = root.getChild(i);
            if (child != null) {
                try {
                    AccessibilityNodeInfo result = findEditableTextField(child);
                    if (result != null) return result;
                } finally {
                    child.recycle();
                }
            }
        }
        return null;
    }
    private AccessibilityNodeInfo navigateToProfile(AccessibilityNodeInfo parent, int childIndex) {
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
            AccessibilityNodeInfo targetElement = parent.getChild(childIndex);
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
    private String navigateToTextView(AccessibilityNodeInfo parent) {
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
            // Extract text
            CharSequence text = targetElement.getText();
            String childText = (text != null) ? text.toString() : null;

            // Log target element details
            Log.d(TAG, "Found target element:");
            Log.d(TAG, "Class: " + targetElement.getClassName());
            Log.d(TAG, "Text: " + targetElement.getText());
            Log.d(TAG, "Clickable: " + targetElement.isClickable());

            Rect bounds = new Rect();
            targetElement.getBoundsInScreen(bounds);
            Log.d(TAG, "Bounds: " + bounds);
            targetElement.recycle();
            return childText;
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to target: " + e.getMessage());
            return null;
        }
    }
    private AccessibilityNodeInfo navigateToPopUp(AccessibilityNodeInfo parent) {
        try {
            Log.d(TAG, "Navigating: Parent -> Child(0) -> Child(0) -> Child(1) -> Child(3)");

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

            try {
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
                try {
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
                    try {
                        if (scrollView.getChildCount() < 4) {
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
                    } finally {
                        scrollView.recycle();
                    }
                } finally  {
                    secondView.recycle();
                }
            } finally {
                firstView.recycle();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to target: " + e.getMessage());
            return null;
        }
    }
    private boolean performClick(AccessibilityNodeInfo node) {
        if (node == null) {
            Log.e(TAG, "Node is null, cannot perform click");
            return false;
        }
        if (node.isClickable()) {
            Log.d(TAG, "Node is clickable, performing click");
            boolean clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            if (clicked) {
                Log.d(TAG, "Click action performed successfully");
            } else {
                Log.e(TAG, "Click action failed");
            }
            return clicked;
        } else {
            Log.d(TAG, "Node is not clickable, attempting to find clickable parent");
            AccessibilityNodeInfo parent = node.getParent();
            while (parent != null) {
                if (parent.isClickable()) {
                    Log.d(TAG, "Found clickable parent, performing click");
                    boolean clicked = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    if (clicked) {
                        Log.d(TAG, "Click action on parent performed successfully");
                    } else {
                        Log.e(TAG, "Click action on parent failed");
                    }
                    return clicked;
                }
                parent = parent.getParent();
            }
            Log.e(TAG, "No clickable parent found");
            return false;
        }
    }
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
                try {
                    AccessibilityNodeInfo result = findNodeByResourceIdInSubtree(child, resourceId);
                    if (result != null) {
                        return result;
                    }
                } finally {
                    child.recycle();
                }
            }
        }
        return null;
    }
    private String extractTweetText() {
        Log.d(TAG, "Starting hierarchy navigation to extract text from tweet...");

        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            return null;
        }
        String parentNodeId = "com.twitter.android:id/tweet_content_text";
        AccessibilityNodeInfo parentNode = HelperFunctions.findNodeByResourceId(rootNode, parentNodeId);

        String tweet_Text = null;
        if (parentNode != null) {
            Log.d(TAG, "Found parent node with ID: " + parentNodeId);
            Log.d(TAG, "Parent has " + parentNode.getChildCount() + " children");
            tweet_Text = navigateToTextView(parentNode);
            parentNode.recycle();
        } else {
            Log.e(TAG, "Could not find parent node with ID: " + parentNodeId);
        }
        rootNode.recycle();
        return tweet_Text;
    }
}
