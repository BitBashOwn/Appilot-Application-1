package com.example.appilot.automations.Twitter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;
import com.example.appilot.services.MyAccessibilityService;
import com.example.appilot.utils.HelperFunctions;
import com.example.appilot.utils.OpenAIClient;

import org.json.JSONArray;

import java.util.List;
import java.util.Random;

public class InteractionWithUsernames {
    private static final String TAG = "TwitterAutomation";
    private final Context context;
    private final Handler handler;
    private final Random random;
    private final PopUpHandler popUpHandler;
    private final MyAccessibilityService service;
    private HelperFunctions helperFunctions;
    private String Task_id = null;
    private String job_id = null;
    private String profile;
    private String openAPIKey;
    private String commentType;
    private String tweetText;
    private List<String> usernames = new java.util.ArrayList<>();
    private JSONArray userArray;
    private List<Object> AccountInputs;
    private int duration;
    private int like_count = 0;
    private int comment_count = 0;
    private int repost_count = 0;
    private int quote_count = 0;
    private int post_count = 0;
    private int Max_Like;
    private int Max_Comment;
    private int Max_Repost;
    private int Max_Quote;
    private int Max_Post;
    private int currentUserIndex = 0;
    private int prompt = 0;
    private int Max_Retry = 3;
    private int retryCount = 0;
    private int numberofProfiles;
    private long startTime;

    public InteractionWithUsernames(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs, int duration, JSONArray inputArray, int numOfProfiles, String openAPIKey){
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
        this.startTime = System.currentTimeMillis();
        this.openAPIKey = openAPIKey;
        this.userArray = inputArray;
        this.numberofProfiles = numOfProfiles;
    }

    private void setCurrentProfileData() {
        if (userArray == null || userArray.length() == 0 || currentUserIndex >= userArray.length()) return;
        try {
            org.json.JSONObject profileObj = userArray.getJSONObject(currentUserIndex);
            // Extract and set all fields
            profile = profileObj.optString("username", "");
            Max_Post = profileObj.optInt("numOfPosts", 0);
            Max_Like = profileObj.optInt("numberOfLikes", 0);
            Max_Comment = profileObj.optInt("numberOfComments", 0);
            Max_Repost = profileObj.optInt("numberOfReposts", 0);
            Max_Quote = profileObj.optInt("numberOfQuotes", 0);
            commentType = profileObj.optString("commentType", "Natural");
            // Reset counts for new profile
            post_count = 0;
            like_count = 0;
            comment_count = 0;
            repost_count = 0;
            quote_count = 0;
            // Build usernames list if not already
            if (usernames.size() != userArray.length()) {
                usernames.clear();
                for (int i = 0; i < userArray.length(); i++) {
                    org.json.JSONObject obj = userArray.getJSONObject(i);
                    String uname = obj.optString("username", "");
                    if (!uname.isEmpty()) usernames.add(uname);
                }
            }
            Log.d(TAG, "Set profile data for: " + profile + " | Posts to Interact: " + Max_Post + ", Likes: " + Max_Like + ", Comment: " + Max_Comment + ", Repost: " + Max_Repost + ", Quote: " + Max_Quote + ", Type: " + commentType);
        } catch (Exception e) {
            Log.e(TAG, "Error setting current profile data: " + e.getMessage());
        }
    }
    public void startInteractAutomation() {
        Log.d(TAG, "Input command for App: "+ userArray);
        Log.d(TAG,  "Number of profiles: "+ numberofProfiles);
        setCurrentProfileData();
        if (!hasCurrentUser()) {
            helperFunctions.cleanupAndExit("No usernames present. Exiting activity.", "final");
            return;
        }
        Log.d(TAG, "Starting Interaction with profiles (multi-user). First: @" + currentUsername());
        handler.postDelayed(()->{
            try{
                clickSearchIcon();
            } catch (Exception e) {
                Log.e(TAG, "Error in clickSearchIcon: " + e.getMessage());
                helperFunctions.cleanupAndExit("Error in clickSearchIcon: " + e.getMessage(), "error");
            }
        }, 1000 + random.nextInt(2000));
    }
    private void clickSearchIcon() {
        Log.d(TAG, "Starting hierarchy navigation to click target element...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in clickSearchIcon", "error");
            return;
        }
        String parentNodeId = "com.twitter.android:id/tabs";
        AccessibilityNodeInfo parentNode = HelperFunctions.findNodeByResourceId(rootNode, parentNodeId);

        if (parentNode != null) {
            Log.d(TAG, "Found parent node with ID: " + parentNodeId);
            Log.d(TAG, "Parent has " + parentNode.getChildCount() + " children");
            // Navigate to target: Parent -> Child(0) -> Child(1)
            AccessibilityNodeInfo targetElement = navigateToSearchTab(parentNode);
            if (targetElement != null) {
                Log.d(TAG, "Found target element, attempting click...");
                boolean clickSuccess = performClick(targetElement);
                if (clickSuccess) {
                    Log.d(TAG, "Search Tab clicked successfully. Waiting for profile to load...");
                    int randomDelay = 3000 + random.nextInt(3000);
                    handler.postDelayed(()->{
                        try {
                            findAndClickSearchBar();
                        } catch (Exception e) {
                            Log.e(TAG, "Error in findAndClickSearchBar: " + e.getMessage());
                            helperFunctions.cleanupAndExit("Error in findAndClickSearchBar: " + e.getMessage(), "error");
                        }
                    },randomDelay);
                }
            } else {
                Log.e(TAG, "Could not navigate to target element");
                helperFunctions.cleanupAndExit("Could not navigate to target element", "error");
            }
            parentNode.recycle();
        } else {
            Log.e(TAG, "Could not find parent node with ID: " + parentNodeId);
            helperFunctions.cleanupAndExit("Could not find parent node with ID: " + parentNodeId, "error");
        }
        rootNode.recycle();
    }
    private void findAndClickSearchBar() {
        Log.d(TAG, "Searching for Search Bar...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickSearchBar", "error");
            return;
        }
        String searchBarId = "com.twitter.android:id/query_view";
        AccessibilityNodeInfo searchBar = HelperFunctions.findNodeByResourceId(rootNode, searchBarId);
        if (searchBar != null) {
            Log.d(TAG, "Found Search Bar, attempting click...");
            boolean clickSuccess = performClick(searchBar);
            if (clickSuccess) {
                Log.d(TAG, "Search Bar clicked. Waiting to type the username...");
                int randomDelay = 3000 + random.nextInt(5000);
                handler.postDelayed(() -> {
                    try {
                        typeProfileNameInSearchBar(currentUsername());
                    } catch (Exception e) {
                        Log.e(TAG, "Error in typeProfileNameInSearchBar: " + e.getMessage());
                        helperFunctions.cleanupAndExit("Error in typeProfileNameInSearchBar: " + e.getMessage(), "error");
                    }
                }, randomDelay);
            }
        } else {
            Log.d(TAG, "Search Bar not found");
            int randomDelay = 2000 + random.nextInt(3000);
            handler.postDelayed(() -> {
                helperFunctions.cleanupAndExit("Could not find the Search Bar, exiting the activity", "error");
            }, randomDelay);
        }
        rootNode.recycle();
    }
    private void typeProfileNameInSearchBar(String username) {
        if (username == null || username.isEmpty()) {
            Log.e(TAG, "Profile name is empty");
            helperFunctions.cleanupAndExit("Profile name is empty", "error");
            return;
        }
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available for typing profile name");
            helperFunctions.cleanupAndExit("No root node available for typing profile name", "error");
            return;
        }
        AccessibilityNodeInfo textField = findEditableTextField(rootNode);
        if (textField == null) {
            Log.e(TAG, "Could not find search bar text field");
            rootNode.recycle();
            helperFunctions.cleanupAndExit("Could not find search bar text field", "error");
            return;
        }
        Log.d(TAG, "TextField class: " + textField.getClassName());
        Log.d(TAG, "TextField editable: " + textField.isEditable());
        Log.d(TAG, "TextField focusable: " + textField.isFocusable());
        Log.d(TAG, "TextField current text: " + textField.getText());
        textField.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
        handler.postDelayed(() -> {
            try {
                Bundle clearBundle = new Bundle();
                clearBundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "");
                boolean clearSuccess = textField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, clearBundle);
                Log.d(TAG, "Clear text success: " + clearSuccess);
                handler.postDelayed(() -> {
                    try {
                        typeCharacterByCharacter(username, 0, new StringBuilder());
                    } catch (Exception e) {
                        Log.e(TAG, "Error while typing profile name: " + e.getMessage());
                        helperFunctions.cleanupAndExit("Error while typing profile name: " + e.getMessage(), "error");
                    }
                }, 500 + random.nextInt(500));
            } catch (Exception e) {
                Log.e(TAG, "Error while typing profile name: " + e.getMessage());
                helperFunctions.cleanupAndExit("Error while typing profile name: " + e.getMessage(), "error");
            }
        }, 300);
    }
    private void selectProfile() {
        Log.d(TAG, "Starting hierarchy navigation to click target element for profile...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in selectProfile", "error");
            return;
        }
        String parentNodeId = "com.twitter.android:id/search_suggestions_list";
        AccessibilityNodeInfo parentNode = HelperFunctions.findNodeByResourceId(rootNode, parentNodeId);

        if (parentNode != null) {
            Log.d(TAG, "Found parent node with ID: " + parentNodeId);
            Log.d(TAG, "Parent has " + parentNode.getChildCount() + " children");
            // Navigate to target: Parent -> Child(0)
            AccessibilityNodeInfo targetElement = navigateToProfile(parentNode, 0);
            if (targetElement != null) {
                Log.d(TAG, "Found target element, matching Username...");
                AccessibilityNodeInfo screenNameNode = HelperFunctions.findNodeByResourceId(targetElement, "com.twitter.android:id/screenname_item");
                String foundScreenName = (screenNameNode != null && screenNameNode.getText() != null) ? screenNameNode.getText().toString().trim() : "";
                Log.d(TAG, "Found screen name: " + foundScreenName + ", looking for: " + currentUsername());
                if (foundScreenName.equalsIgnoreCase(currentUsername())) {
                    Log.d(TAG, "Screen name matches, attempting click...");
                    boolean clickSuccess = performClick(targetElement);
                    targetElement.recycle();
                    if (clickSuccess) {
                        Log.d(TAG, "Profile clicked successfully. Waiting for profile to load...");
                        int randomDelay = 2000 + random.nextInt(3000);
                        handler.postDelayed(()->{
                            try {
                                findAndClickFollowButton();
                            } catch (Exception e) {
                                Log.e(TAG, "Error in findAndClickFollowButton: " + e.getMessage());
                                helperFunctions.cleanupAndExit("Error in findAndClickFollowButton: " + e.getMessage(), "error");
                            }
                        },randomDelay);
                    }
                } else {
                    Log.e(TAG, "Screen name does not match. Expected: " + currentUsername() + ", Found: " + foundScreenName);
                    targetElement.recycle();
                    handler.postDelayed(()->{
                        launchProfileByIntent(currentUsername());
                    },2000 + random.nextInt(3000));
                }
            } else {
                Log.e(TAG, "Could not find the specific profile in search results");
                handler.postDelayed(()->{
                    launchProfileByIntent(currentUsername());
                },2000 + random.nextInt(3000));
            }
            parentNode.recycle();
        } else {
            Log.e(TAG, "Could not find parent node with ID: " + parentNodeId);
            helperFunctions.cleanupAndExit("Could not find parent node with ID: " + parentNodeId, "error");
        }
        rootNode.recycle();
    }
    private void launchProfileByIntent(String username) {
        Log.d(TAG, "Attempting to launch profile by intent for username: " + username);
        if (username == null || username.isEmpty()) {
            Log.e(TAG, "Username is null or empty. Cannot launch intent.");
            return;
        }
        String cleanUsername = username.startsWith("@") ? username.substring(1) : username;
        String twitterUrl = "twitter://user?screen_name=" + cleanUsername;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(twitterUrl));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            Log.d(TAG, "Trying to open Twitter app with URL: " + twitterUrl);
            context.startActivity(intent);
            Log.d(TAG, "Twitter app intent launched successfully.");
        } catch (android.content.ActivityNotFoundException e) {
            Log.w(TAG, "Twitter app not found. Falling back to browser.");
            String webUrl = "https://twitter.com/" + username;
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(browserIntent);
                Log.d(TAG, "Browser intent launched successfully with URL: " + webUrl);
                int delay = 5000 + random.nextInt(5000);
                Log.d(TAG, "Scheduling findAndClickFollowButton after " + delay + " ms");
                handler.postDelayed(this::findAndClickFollowButton, delay);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to launch browser intent: " + ex.getMessage());
                Log.d(TAG, "Moving to next username if available.");
                handler.postDelayed(this::switchToNextUsername, 2000+ random.nextInt(3000));
            }
        }
    }
    private void findAndClickFollowButton() {
        Log.d(TAG, "Searching for Follow button...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findAndClickFollowButton", "error");
            return;
        }
        String searchFollowButton = "com.twitter.android:id/button_bar_follow";
        AccessibilityNodeInfo searchFollow = HelperFunctions.findNodeByResourceId(rootNode, searchFollowButton);
        if (searchFollow != null) {
            Log.d(TAG, "Found Follow Button, attempting click...");
            boolean clickSuccess = performClick(searchFollow);
            if (clickSuccess) {
                Log.d(TAG, "Follow Button clicked...");
                int randomDelay = 2000 + random.nextInt(5000);
                handler.postDelayed(()->{
                    try {
                        helperFunctions.performScroll(0.7f, 0.3f);
                        //handler.postDelayed(this::findTweetNode, randomDelay);
                        handler.postDelayed(()->{
                            try{
                                findTweetNode();
                            } catch (Exception e) {
                                Log.e(TAG, "Error in findTweetNode1.2: " + e.getMessage());
                                helperFunctions.cleanupAndExit("Error in findTweetNode1.2: " + e.getMessage(), "error");
                            }
                        },randomDelay);

                    } catch (Exception e) {
                        Log.e(TAG, "Error in findTweetNode1: " + e.getMessage());
                        helperFunctions.cleanupAndExit("Error in findTweetNode1: " + e.getMessage(), "error");
                    }
                },randomDelay);
            }
        } else {
            Log.d(TAG, "Follow button not found");
            int randomDelay = 2000 + random.nextInt(3000);
            handler.postDelayed(()->{
                try {
                    helperFunctions.performScroll(0.7f, 0.3f);
                    //handler.postDelayed(this::findTweetNode, randomDelay);
                    handler.postDelayed(()->{
                        try{
                            findTweetNode();
                        } catch (Exception e) {
                            Log.e(TAG, "Error in findTweetNode2.2: " + e.getMessage());
                            helperFunctions.cleanupAndExit("Error in findTweetNode1.2: " + e.getMessage(), "error");
                        }
                    },randomDelay);
                } catch (Exception e) {
                    Log.e(TAG, "Error in findTweetNode2: " + e.getMessage());
                    helperFunctions.cleanupAndExit("Error in findTweetNode2: " + e.getMessage(), "error");
                }
            },randomDelay);
        }
        rootNode.recycle();
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
                    helperFunctions.performScroll(0.7f,0.3f);
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
                switchToNextUsername();
            }
        }
        rootNode.recycle();
    }
    private void findAndClickLike() {
        if (like_count == Max_Like) {
            Log.e(TAG, "You have reached the like limit");
            handler.postDelayed(()->{
                Log.d(TAG," Reached max likes for this user, moving to next user if available.");
                findAndClickRepost();
            }, 1000 + random.nextInt(3000));
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
                like_count ++;
                handler.postDelayed(this::findAndClickRepost,2000+random.nextInt(3000));
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
        if (repost_count == Max_Repost && quote_count == Max_Quote) {
            Log.e(TAG, "You have reached the repost limit");
            if (tweetText != null) {
                handler.postDelayed(() -> {
                    Log.d(TAG, " Reached max reposts for this user, moving to next user if available.");
                    findAndClickComment(tweetText);
                }, 1000 + random.nextInt(3000));
                return;
            } else {
                handler.postDelayed(this::onScroll,2000+random.nextInt(3000));
                return;
            }
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
                handler.postDelayed(this::onScroll, 2000);
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
        int randomNum = random.nextInt(100) + 1; // 1 to 100
        int childIndex = (randomNum > 50) ? 1 : 0;
        Log.d(TAG, "Random number: " + randomNum + ", using child index: " + childIndex);
        if (parentNode != null){
            if (childIndex == 1 && quote_count < Max_Quote) {
                Log.d(TAG, "Attempting to click Quote");
                handler.postDelayed(()->{
                    clickQuoteInPopup(parentNode, childIndex);
                }, 2000+ random.nextInt(3000));
            } else if (childIndex == 0 && repost_count < Max_Repost) {
                Log.d(TAG, "Attempting to click Repost");
                handler.postDelayed(()->{
                    clickRepostInPopup(parentNode, childIndex);
                }, 2000+ random.nextInt(3000));
            } else if (childIndex == 1 && quote_count >= Max_Quote && repost_count < Max_Repost) {
                Log.d(TAG, "Quote limit reached, clicking Repost instead");
                handler.postDelayed(()->{
                    clickRepostInPopup(parentNode, 0);
                }, 2000+ random.nextInt(3000));
            } else if (childIndex == 0 && repost_count >= Max_Repost && quote_count < Max_Quote) {
                Log.d(TAG, "Repost limit reached, clicking Quote instead");
                handler.postDelayed(()->{
                    clickQuoteInPopup(parentNode, 1);
                }, 2000+ random.nextInt(3000));
            } else {
                Log.d(TAG, "Both Repost and Quote limits reached, moving to comment or next tweet");
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
                repost_count++;
                Log.d(TAG, "Repost clicked successfully. Extracting tweet content and calling comment...");
                int randomDelay = 2000 + random.nextInt(3000);
                if (tweetText != null) {
                    handler.postDelayed(() -> {
                        findAndClickComment(tweetText);
                    }, randomDelay);
                } else {
                    handler.postDelayed(this::onScroll, 2000 + random.nextInt(3000));
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
                quote_count++;
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
                    handler.postDelayed(this::onScroll,2000+random.nextInt(3000));
                }
            }
        } else {
            Log.d(TAG, "Like Button is not found");
            handler.postDelayed(()->{
                helperFunctions.cleanupAndExit("Repost button in quote not found, exiting...", "error");
            }, 2000);
        }
        rootNode.recycle();
    }
    private void findAndClickComment(String text) {
        if (comment_count == Max_Comment) {
            Log.e(TAG, "You have reached the comment limit");
            handler.postDelayed(()->{
                Log.d(TAG," Reached max comments for this user, moving to next user if available.");
                onScroll();
            }, 1000 + random.nextInt(3000));
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
            handler.postDelayed(this::onScroll, 2000+random.nextInt(3000));
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
                    int randomDelay = 1000 + random.nextInt(2000);
                    handler.postDelayed(this::onScroll,randomDelay);
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
            helperFunctions.cleanupAndExit("OpenAI API key is missing. Cannot generate comment.", "error");
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
                Log.d(TAG, "Reply Button clicked. Waiting for the page to load...");
                comment_count ++;
                int randomDelay = 2000 + random.nextInt(5000);
                handler.postDelayed(this::onScroll,randomDelay);
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
    private void typeCharacterByCharacter(String text, int charIndex, StringBuilder typedSoFar) {
        if (charIndex >= text.length()) {
            Log.d(TAG, "Finished typing profile name");
            handler.postDelayed(this::selectProfile, 5000 + random.nextInt(5000));
            return;
        }
        String currentChar = String.valueOf(text.charAt(charIndex));
        typedSoFar.append(currentChar);

        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available during typing");
            helperFunctions.cleanupAndExit("No root node available during typing", "error");
            return;
        }
        AccessibilityNodeInfo textField = findEditableTextField(rootNode);
        if (textField == null) {
            Log.e(TAG, "Could not find text input field during typing");
            rootNode.recycle();
            helperFunctions.cleanupAndExit("Could not find text input field during typing", "error");
            return;
        }
        Log.d(TAG, "Typing char: " + currentChar + " into field: " + textField.getClassName());
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, typedSoFar.toString());
        boolean setTextSuccess = textField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        Log.d(TAG, "Set text success: " + setTextSuccess);
        if (!setTextSuccess) {
            // Fallback: Try paste if available
            if (textField.isEditable() && textField.isFocusable() && textField.isEnabled() && textField.isVisibleToUser()) {
                Log.d(TAG, "Trying fallback paste method");
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("label", typedSoFar.toString());
                clipboard.setPrimaryClip(clip);
                boolean pasteSuccess = textField.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                Log.d(TAG, "Paste action success: " + pasteSuccess);
            }
        }
        textField.recycle();
        rootNode.recycle();

        int delay = 60 + random.nextInt(100); // Human-like delay
        handler.postDelayed(() -> {
            typeCharacterByCharacter(text, charIndex + 1, typedSoFar);
        }, delay);
    }

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
    private AccessibilityNodeInfo navigateToSearchTab(AccessibilityNodeInfo parent) {
        try {
            Log.d(TAG, "Navigating: Parent -> Child(0) -> Child(1)");

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
            AccessibilityNodeInfo Child_0 = parent.getChild(0);
            if (Child_0 == null) {
                Log.e(TAG, "Could not get child(0)");
                return null;
            }
            try {
                // Log target element details
                Log.d(TAG, "Found target element:");
                Log.d(TAG, "Class: " + Child_0.getClassName());
                Log.d(TAG, "Text: " + Child_0.getText());
                Log.d(TAG, "Clickable: " + Child_0.isClickable());

                // Step 1: Get first child (0)
                if (Child_0.getChildCount() < 2) {
                    Log.e(TAG, "Child_0 has no children");
                    return null;
                }
                AccessibilityNodeInfo targetElement = Child_0.getChild(1);
                if (targetElement == null) {
                    Log.e(TAG, "Could not get child(1) of Child_0");
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
            } finally {
                Child_0.recycle();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to target: " + e.getMessage());
            return null;
        }
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
    public void onScroll() {
        Log.d(TAG,"Duration : " + duration + " minutes");
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000 / 60;
        Log.d(TAG, "Elapsed Time: " + elapsedTime + " minutes");
        if (elapsedTime >= duration) {
            Log.d(TAG, "Reached duration. Exiting...");
            handler.postDelayed(() -> helperFunctions.cleanupAndExit("Duration Completed. Exiting activity.", "final"), 1000 + random.nextInt(3000));
            return;
        }
        // Perform a scroll
        int randomDelay = 1200 + random.nextInt(4000);
        handler.postDelayed(() -> {
            float endY = 0.0f + random.nextFloat() * 0.7f;
            float startY = endY + 0.3f;
            helperFunctions.performScroll(startY, endY);
            post_count ++;

            if (post_count >= Max_Post) {
                Log.d(TAG, "Reached max posts for this user, moving to next user if available.");
                switchToNextUsername();
                return;
            }
            if (like_count == Max_Like && comment_count == Max_Comment && repost_count == Max_Repost && quote_count == Max_Quote) {
                Log.d(TAG, "like, comment and repost reached to limit. Switching to next username...");
                switchToNextUsername();
            } else {
                handler.postDelayed(this::findTweetNode, 1000);
            }
        }, randomDelay);
    }
    private void switchToNextUsername() {
        currentUserIndex++;
        setCurrentProfileData(); // <-- Set next profile's data
        if (!hasCurrentUser()) {
            Log.d(TAG, "No more usernames. Exiting activity.");
            handler.postDelayed(() -> helperFunctions.cleanupAndExit("No more usernames in the list. Exiting activity.", "final"), 1000 + random.nextInt(3000));
            return;
        }
        Log.d(TAG, "Next username: @" + currentUsername() + " — navigating back and searching again.");
        // Go back once (or twice if needed). Keep it simple with one back, then reopen Search.
        service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK);
        handler.postDelayed(()->{
            try {
                findAndClickSearchBar();
            } catch (Exception e) {
                Log.e(TAG, "Error in findAndClickSearchBar: " + e.getMessage());
                helperFunctions.cleanupAndExit("Error in findAndClickSearchBar: " + e.getMessage(), "error");
            }
        },2000 + random.nextInt(3000));
    }
    private boolean hasCurrentUser() {
        return currentUserIndex >= 0 && currentUserIndex < usernames.size();
    }
    private String currentUsername() {
        return hasCurrentUser() ? usernames.get(currentUserIndex) : null;
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
