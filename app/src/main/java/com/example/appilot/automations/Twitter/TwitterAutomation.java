package com.example.appilot.automations.Twitter;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;
import com.example.appilot.services.MyAccessibilityService;
import com.example.appilot.utils.HelperFunctions;
import java.util.List;
import java.util.Random;
import com.example.appilot.BuildConfig;
import android.view.accessibility.AccessibilityNodeInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TwitterAutomation {
    private static final String TAG = "TwitterAutomation";
    private static final String TWITTER_PACKAGE = "com.twitter.android";
    private final Context context;
    private final Handler handler;
    private final Random random;
    private final PopUpHandler popUpHandler;
    private final MyAccessibilityService service;
    private HelperFunctions helperFunctions;
    private String Task_id = null;
    private String job_id = null;
    private List<Object> AccountInputs;
    private JSONArray postedArray;
    private JSONArray tweetsArray;
    private int duration;
    private double probability_like;
    private double probability_follow;
    private double probability_comment;
    private int limit_like;
    private int limit_follow;
    private int limit_comment;
    private int limit_user;
    private int numberofProfiles;
    private boolean likeTweet;
    private boolean followTweet;
    private boolean commentsOnTweets;
    private boolean userNameInteract;
    private boolean spaces;
    private boolean newPost;
    private boolean Follow_Unfollow;
    private boolean specificTweets;
    private boolean API;
    private String dateLike;
    private String dateFollow;
    private String dateComment;
    private String spaceLink;
    private String newPostPrompt;
    private String profile;
    private String typeofAction;
    private String API_key;
    private LikeTweets likeTweets;
    private Follow follow;
    private TweetComments tweetComments;
    private InteractionWithUsernames UserNameInteract;
    private JoinSpaces joinspaces;
    private PostTweet postTweet;
    private Follow_Unfollow follow_unfollow;
    private Retweet retweet;
    public TwitterAutomation(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs, int duration) {
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
    }

    public void launchApp() {
        Log.d(TAG, "Launching app: " + TWITTER_PACKAGE);
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(TWITTER_PACKAGE);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
            handler.postDelayed(() -> {
                Log.d(TAG, "Delay before finding 'For You' tab");
                //clickHome();
                startAutomation();
            }, 10000 + random.nextInt(3000));
        } else {
            Log.e(TAG, "Could not launch app: " + TWITTER_PACKAGE);
            launchInstagramExplicitly();
        }
    }
    private void launchInstagramExplicitly() {
        Log.d(TAG, "Entered launchTwitterExplicitly.");
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse("https://twitter.com/"))
                .setPackage(TWITTER_PACKAGE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            handler.postDelayed(() -> {
                Log.d(TAG, "Delay before finding 'For You' tab after explicit launch");
                //clickHome();
                startAutomation();
            }, 10000+ random.nextInt(3000));

        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Twitter", e);
            handler.postDelayed(() -> {
                Log.d(TAG, "Could not launch the Twitter");
                helperFunctions.cleanupAndExit("Couldn't Launch Twitter", "error");
            }, 3000+ random.nextInt(2000));
        }
    }

    private void clickHome() {
        Log.d(TAG, "Starting hierarchy navigation to click target element...");

        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in clickSearchIcon", "error");
            return;
        }
        // Find parent node by resource ID
        String parentNodeId = "com.twitter.android:id/tabs";
        AccessibilityNodeInfo parentNode = HelperFunctions.findNodeByResourceId(rootNode, parentNodeId);

        if (parentNode != null) {
            Log.d(TAG, "Found parent node with ID: " + parentNodeId);
            Log.d(TAG, "Parent has " + parentNode.getChildCount() + " children");
            // Navigate to target: Parent -> Child(0) -> Child(0)
            AccessibilityNodeInfo targetElement = navigateToHome(parentNode);
            if (targetElement != null) {
                Log.d(TAG, "Found target element, attempting click...");
                boolean clickSuccess = performClick(targetElement);
                if (clickSuccess) {
                    Log.d(TAG, "Home Tab clicked successfully. Waiting for home to load...");
                    int randomDelay = 2000 + random.nextInt(3000);
                    handler.postDelayed(this::findForYou, randomDelay);
                }
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
    private void findForYou() {
        Log.d(TAG, "Starting hierarchy navigation to click target element...");
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present in findComments", "error");
            return;
        }
        // Find parent node by resource ID
        String parentNodeId = "com.twitter.android:id/fleetline_app_bar";
        AccessibilityNodeInfo parentNode = HelperFunctions.findNodeByResourceId(rootNode, parentNodeId);

        if (parentNode != null) {
            Log.d(TAG, "Found parent node with ID: " + parentNodeId);
            Log.d(TAG, "Parent has " + parentNode.getChildCount() + " children");
            // Navigate to target: Parent -> Child(0) -> Child(0 -> Child(0))
            AccessibilityNodeInfo targetElement = navigateToForYou(parentNode);
            if (targetElement != null) {
                Log.d(TAG, "Found target element, attempting click...");
                boolean clickSuccess = performClick(targetElement);
                if (clickSuccess) {
                    Log.d(TAG, "For you Tab clicked successfully. Waiting for profile to load...");
                    handler.postDelayed(this::findPosted, 5000);
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
    public void findPosted() {
        Log.d(TAG, "Searching for posted Button");
        // Verify one more time that we have root access
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No root node available in like button");
            helperFunctions.cleanupAndExit("Automation Could not be Completed, because no Root_node present", "error");
            return;
        }
        Log.d(TAG, "Root node available, proceeding with Click logic");
        String nodeId = "com.twitter.android:id/text_banner_layout";
        AccessibilityNodeInfo targetElement = HelperFunctions.findNodeByResourceId(rootNode, nodeId);
        if (targetElement != null) {
            boolean clickSuccess = performClick(targetElement);
            if (clickSuccess) {
                Log.d(TAG, "New posts clicked successfully. Waiting for profile to load...");
                int randomDelay = 2000 + random.nextInt(5000);
                handler.postDelayed(()->{
                    //handler.postDelayed(this::startAutomation, randomDelay);
                    handler.postDelayed(this::checkAutomationCategory, randomDelay);
                }, randomDelay);
            }
        }
        else {
            //startAutomation();
            checkAutomationCategory();
        }
        rootNode.recycle();
    }
    private void startAutomation() {
        Log.d(TAG, "=== Starting TikTok Automation ===");
        Log.d(TAG, "Extracting toggle states from converted JSONArray...");

        try {
            // Extract toggle states from the input data (already converted from JSONArray)
            if (AccountInputs != null && !AccountInputs.isEmpty()) {
                Log.d(TAG, "Processing " + AccountInputs.size() + " input objects from converted JSONArray...");

                for (Object inputObj : AccountInputs) {
                    if (inputObj instanceof JSONObject) {
                        JSONObject inputSection = (JSONObject) inputObj;
                        Log.d(TAG, "Processing input section: " + inputSection.toString());

                        // Check if this JSONObject has "Twitter" key directly
                        if (inputSection.has("Twitter")) {
                            Log.d(TAG, "Found 'Twitter' key, extracting toggle states...");

                            // Extract the "Twitter" array
                            JSONArray connectionsArray = inputSection.optJSONArray("Twitter");
                            if (connectionsArray != null) {
                                Log.d(TAG, "Found " + connectionsArray.length() + " toggles in Twitter array");

                                // Iterate through the Twitter array to get individual toggles
                                for (int i = 0; i < connectionsArray.length(); i++) {
                                    JSONObject connectionObject = connectionsArray.getJSONObject(i);
                                    Log.d(TAG, "Processing Twitter object [" + i + "]: " + connectionObject.toString());

                                    // Extract Twitter specific toggles
                                    if (connectionObject.has("API Key")) {
                                        API = connectionObject.optBoolean("API Key", false);
                                        Log.d(TAG, "✓ API Key: " + API);

                                        if (API && connectionObject.has("api_key")) {
                                            API_key = connectionObject.optString("api_key", "");
                                            Log.d(TAG, "✓ API Key: " + API_key);
                                        }
                                    }

                                    if (connectionObject.has("Like the Tweets")) {
                                        likeTweet = connectionObject.optBoolean("Like the Tweets", false);
                                        Log.d(TAG, "✓ Like the Tweets: " + likeTweet);

                                        if (likeTweet && connectionObject.has("probability")) {
                                            int numberOfConnections = connectionObject.optInt("probability", 0);
                                            probability_like = numberOfConnections / 100.0;
                                            Log.d(TAG, "✓ Probability: " + probability_like);
                                        }
                                        if (likeTweet && connectionObject.has("tweetsPerDay")) {
                                            limit_like = connectionObject.optInt("tweetsPerDay", 0);
                                            Log.d(TAG, "✓ tweetsPerDay: " + limit_like);
                                        }
                                        if (connectionObject.has("date")) {
                                            dateLike = connectionObject.optString("date", "");
                                            Log.d(TAG, "✓ Date for Like the Tweets: " + dateLike);
                                        }
                                    }

                                    if (connectionObject.has("Follow the Users")) {
                                        followTweet = connectionObject.optBoolean("Follow the Users", false);
                                        Log.d(TAG, "✓ Follow the Users: " + followTweet);

                                        if (followTweet && connectionObject.has("probability")) {
                                            int numberOfConnections = connectionObject.optInt("probability", 0);
                                            probability_follow = numberOfConnections / 100.0;
                                            Log.d(TAG, "✓ Probability: " + probability_follow);
                                        }
                                        if (followTweet && connectionObject.has("tweetsPerDay")) {
                                            limit_follow = connectionObject.optInt("tweetsPerDay", 0);
                                            Log.d(TAG, "✓ tweetsPerDay: " + limit_follow);
                                        }
                                        if (connectionObject.has("date")) {
                                            dateFollow = connectionObject.optString("date", "");
                                            Log.d(TAG, "✓ Date for Follow the Users: " + dateFollow);
                                        }
                                    }

                                    if (connectionObject.has("Comment on Tweets")) {
                                        commentsOnTweets = connectionObject.optBoolean("Comment on Tweets", false);
                                        Log.d(TAG, "✓ Comment on Tweets: " + commentsOnTweets);

                                        if (commentsOnTweets && connectionObject.has("probability")) {
                                            int numberOfConnections = connectionObject.optInt("probability", 0);
                                            probability_comment = numberOfConnections / 100.0;
                                            Log.d(TAG, "✓ Probability: " + probability_comment);
                                        }
                                        if (commentsOnTweets && connectionObject.has("tweetsPerDay")) {
                                            limit_comment = connectionObject.optInt("tweetsPerDay", 0);
                                            Log.d(TAG, "✓ tweetsPerDay: " + limit_comment);
                                        }
                                        if (connectionObject.has("date")) {
                                            dateComment = connectionObject.optString("date", "");
                                            Log.d(TAG, "✓ Date for Comments on Tweets: " + dateComment);
                                        }
                                    }

                                    if (connectionObject.has("Profile interaction using Usernames")) {
                                        userNameInteract = connectionObject.optBoolean("Profile interaction using Usernames", false);
                                        Log.d(TAG, "✓ Profile interaction using Usernames: " + userNameInteract);

                                        if (userNameInteract && connectionObject.has("posts")) {
                                            postedArray = connectionObject.optJSONArray("posts");
                                            Log.d(TAG, "✓ Profiles: " + postedArray);
                                        }
                                        if (userNameInteract && connectionObject.has("numberOfPosts")) {
                                            numberofProfiles = connectionObject.optInt("numberOfPosts", 0);
                                            Log.d(TAG, "✓ Number of Profiles: " + numberofProfiles);
                                        }
                                    }

                                    if (connectionObject.has("Join the Twitter Space")) {
                                        spaces = connectionObject.optBoolean("Join the Twitter Space", false);
                                        Log.d(TAG, "✓ Join the Twitter Space: " + spaces);

                                        if (spaces && connectionObject.has("space_link")) {
                                            spaceLink = connectionObject.optString("space_link", "");
                                            Log.d(TAG, "✓ Space Link: " + spaceLink);
                                        }
                                    }

                                    if (connectionObject.has("Post a new Tweet")) {
                                        newPost = connectionObject.optBoolean("Post a new Tweet", false);
                                        Log.d(TAG, "✓ Post a new Tweet: " + newPost);

                                        if (newPost && connectionObject.has("prompt")) {
                                            newPostPrompt = connectionObject.optString("prompt", "");
                                            Log.d(TAG, "✓ Post Prompt: " + newPostPrompt);
                                        }
                                    }

                                    if (connectionObject.has("Follow/Unfollow the Usernames")) {
                                        Follow_Unfollow = connectionObject.optBoolean("Follow/Unfollow the Usernames", false);
                                        Log.d(TAG, "✓ Follow/Unfollow the Usernames: " + Follow_Unfollow);

                                        if (Follow_Unfollow && connectionObject.has("usernames")) {
                                            profile = connectionObject.optString("usernames", "");
                                            Log.d(TAG, "✓ Usernames: " + profile);
                                        }
                                        if (Follow_Unfollow && connectionObject.has("followAction")) {
                                            typeofAction = connectionObject.optString("followAction", "");
                                            Log.d(TAG, "✓ Type of Action: " + typeofAction);
                                        }
                                        if (Follow_Unfollow && connectionObject.has("usersPerDay")) {
                                            limit_user = connectionObject.optInt("usersPerDay", 0);
                                            Log.d(TAG, "✓ Number of users per day: " + limit_user);
                                        }
                                    }

                                    if (connectionObject.has("Interact with specific Tweets")) {
                                        specificTweets = connectionObject.optBoolean("Interact with specific Tweets", false);
                                        Log.d(TAG, "✓ Interact with specific Tweets: " + specificTweets);

                                        if (specificTweets && connectionObject.has("tweetData")) {
                                            tweetsArray = connectionObject.optJSONArray("tweetData");
                                            Log.d(TAG, "✓ Tweet Data: " + tweetsArray);
                                        }
                                    }
                                }
                            } else {
                                Log.e(TAG, "TikTok key found but value is not an array");
                            }
                            break;
                        } else {
                            Log.d(TAG, "No 'TikTok' key found in this section.");
                        }
                    }
                }
            } else {
                helperFunctions.cleanupAndExit("No AccountInputs provided or list is empty", "error");
                Log.d(TAG, "Using default configuration: Like the reel = true");
            }

            // Log the final extracted states
            Log.d(TAG, "=== Final Twitter Toggle States ===");
            Log.d(TAG, "API Key Provided: " + API);
            Log.d(TAG, "OpenAI API Key: " + API_key);
            Log.d(TAG, "Like the Tweets: " + likeTweet);
            Log.d(TAG, "Probability of Liking the Tweets: " + probability_like);
            Log.d(TAG, "Daily Limit of Liking Tweets: " + limit_like);
            Log.d(TAG, "Date for Liking Tweets: " + dateLike);
            Log.d(TAG, "Follow the users: " + followTweet);
            Log.d(TAG, "Probability of Following the Users: " + probability_follow);
            Log.d(TAG, "Daily Limit of Following Users: " + limit_follow);
            Log.d(TAG, "Date for Following Users: " + dateFollow);
            Log.d(TAG, "Comments on Tweets: " + commentsOnTweets);
            Log.d(TAG, "Probability of Commenting on Tweets: " + probability_comment);
            Log.d(TAG, "Daily Limit of Commenting on Tweets: " + limit_comment);
            Log.d(TAG, "Date for Commenting on Tweets: " + dateComment);
            Log.d(TAG, "Interaction with profiles using usernames: " + userNameInteract);
            Log.d(TAG, "Posted Array: " + postedArray);
            Log.d(TAG, "Number of Profiles: " + numberofProfiles);
            Log.d(TAG, "Join the Twitter Space: " + spaces);
            Log.d(TAG, "Space URL: " + spaceLink);
            Log.d(TAG, "Post a new Tweet.: " + newPost);
            Log.d(TAG, "New Post Prompt: " + newPostPrompt);
            Log.d(TAG, "Follow/Unfollow the Usernames: " + Follow_Unfollow);
            Log.d(TAG, "Usernames: " + profile);
            Log.d(TAG, "Type of Action: " + typeofAction);
            Log.d(TAG, "Number of users per day: " + limit_user);
            Log.d(TAG, "Interact with specific Tweets: " + specificTweets);
            Log.d(TAG, "Tweet URLs: " + tweetsArray);
            Log.d(TAG, "Duration: " + duration);
            Log.d(TAG, "=====================================");

            // Create the TikTok automation objects and pass the extracted values
            this.likeTweets = new LikeTweets(service, Task_id, job_id, AccountInputs, duration, probability_like, limit_like, dateLike);
            this.follow = new Follow(service, Task_id, job_id, AccountInputs, duration, probability_follow, limit_follow, dateFollow);
            this.tweetComments = new TweetComments(service, Task_id, job_id, AccountInputs, duration, probability_comment, API_key, limit_comment, dateComment);
            this.UserNameInteract = new InteractionWithUsernames(service, Task_id, job_id, AccountInputs, duration, postedArray, numberofProfiles, API_key);
            this.joinspaces = new JoinSpaces(service, Task_id, job_id, AccountInputs, duration, spaceLink);
            this.postTweet = new PostTweet(service, Task_id, job_id, AccountInputs, duration, newPostPrompt, API_key);
            this.follow_unfollow = new Follow_Unfollow(service, Task_id, job_id, AccountInputs, duration, profile, typeofAction, limit_user);
            this.retweet = new Retweet(service, Task_id, job_id, AccountInputs, duration, tweetsArray, API_key);
//            // Route to the appropriate function based on toggle states
//            if (API && likeTweet) {
//                Log.d(TAG, "🎯 Routing to: Like Tweets Only");
//                likeTweets.startLikeAutomation();
//            } else if (API && followTweet) {
//                Log.d(TAG, "🎯 Routing to: Follow Users Only");
//                follow.startFollowAutomation();
//            } else if (API && commentsOnTweets) {
//                Log.d(TAG, "🎯 Routing to: Like and Reply to comments");
//                tweetComments.startCommentAutomation();
//            } else if (API && userNameInteract) {
//                Log.d(TAG, "🎯 Routing to: Interaction with profiles using usernames");
//                UserNameInteract.startInteractAutomation();
//            } else if (API && spaces) {
//                Log.d(TAG, "🎯 Routing to: Joining Twitter Spaces");
//                joinspaces.startSpaceAutomation();
//            } else {
//                Log.d(TAG, "🎯 No automation selected - Using default: ReelLiker");
//                helperFunctions.cleanupAndExit("Doesn't find any automation", "error");
//            }
            if (API && spaces) {
                Log.d(TAG, "🎯 Routing to: Joining Twitter Spaces");
                joinspaces.startSpaceAutomation();
            } else {
                handler.postDelayed(this::clickHome, 3000 + random.nextInt(3000));
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSONException in startAutomation: " + e.getMessage());
            e.printStackTrace();
            // Default to reel liking on JSON error
            Log.d(TAG, "JSON error occurred, defaulting to ReelLiker");
            helperFunctions.cleanupAndExit("No input found as a JSON", "error");

        } catch (Exception e) {
            Log.e(TAG, "General Exception in startAutomation: " + e.getMessage());
            e.printStackTrace();
            // Default to reel liking on any error
            Log.d(TAG, "General error occurred, defaulting to ReelLiker");
            helperFunctions.cleanupAndExit("No input found in general exception", "error");
        }
    }

    private void checkAutomationCategory() {
        Log.d(TAG, "Checking which automation to run based on toggle states...");
        // Route to the appropriate function based on toggle states
        if (API && likeTweet) {
            Log.d(TAG, "🎯 Routing to: Like Tweets Only");
            likeTweets.startLikeAutomation();
        } else if (API && followTweet) {
            Log.d(TAG, "🎯 Routing to: Follow Users Only");
            follow.startFollowAutomation();
        } else if (API && commentsOnTweets) {
            Log.d(TAG, "🎯 Routing to: Like and Reply to comments");
            tweetComments.startCommentAutomation();
        } else if (API && userNameInteract) {
            Log.d(TAG, "🎯 Routing to: Interaction with profiles using usernames");
            UserNameInteract.startInteractAutomation();
        } else if (API && newPost) {
            Log.d(TAG, "🎯 Routing to: Post a new Tweet.");
            postTweet.startPostingAutomation();
        } else if (API && Follow_Unfollow) {
            Log.d(TAG, "🎯 Routing to: Follow/Unfollow the Usernames");
            follow_unfollow.startFollow_unfollowAutomation();
        } else if (API && specificTweets) {
            Log.d(TAG, "🎯 Routing to: Interact with specific Tweets");
            retweet.startRetweetAutomation();
        } else {
            Log.d(TAG, "🎯 No automation selected - Using default: ReelLiker");
            helperFunctions.cleanupAndExit("Doesn't find any automation", "error");
        }
    }


//    Helper Function
    private AccessibilityNodeInfo navigateToForYou(AccessibilityNodeInfo parent) {
        try {
            Log.d(TAG, "Navigating: Parent -> Child(0) -> Child(0) -> Child(0)");

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
            AccessibilityNodeInfo firstChild = parent.getChild(0);
            if (firstChild == null) {
                Log.e(TAG, "Could not get child(0)");
                return null;
            }
            // Log target element details
            Log.d(TAG, "Found Child(0) of Parent:");
            Log.d(TAG, "Class: " + firstChild.getClassName());
            Log.d(TAG, "Text: " + firstChild.getText());
            Log.d(TAG, "Clickable: " + firstChild.isClickable());

            // Step 2: Get first child (0) of firstChild
            if (firstChild.getChildCount() < 1) {
                Log.e(TAG, "Child(0) has no children");
                return null;
            }
            AccessibilityNodeInfo Child0of0 = firstChild.getChild(0);
            if (Child0of0 == null) {
                Log.e(TAG, "Could not get child(0) of firstChild");
                return null;
            }
            // Log target element details
            Log.d(TAG, "Found Child(0) of Parent -> Child(0):");
            Log.d(TAG, "Class: " + Child0of0.getClassName());
            Log.d(TAG, "Text: " + Child0of0.getText());
            Log.d(TAG, "Clickable: " + Child0of0.isClickable());

            // Step 3: Get first child (0) of Child0of0
            if (Child0of0.getChildCount() < 1) {
                Log.e(TAG, "Child(0) has no children");
                return null;
            }
            AccessibilityNodeInfo targetElement = Child0of0.getChild(0);
            if (targetElement == null) {
                Log.e(TAG, "Could not get child(0) of Child0of0");
                return null;
            }
            // Log target element details
            Log.d(TAG, "Found Child(0) of Parent -> Child(0) -> Child(0):");
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

                performGestureClick(clickX, clickY);
                clicked = true;
                Log.d(TAG, "Gesture click performed at: " + clickX + ", " + clickY);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clicking target element: " + e.getMessage());
            helperFunctions.cleanupAndExit("Error clicking target element", "error");
        } finally {
            helperFunctions.safelyRecycleNode(targetElement);
        }
        return clicked;
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
    private AccessibilityNodeInfo navigateToHome(AccessibilityNodeInfo parent) {
        try {
            Log.d(TAG, "Navigating: Parent -> Child(0) -> Child(0)");

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
            AccessibilityNodeInfo targetElement = Child_0.getChild(0);
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
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to target: " + e.getMessage());
            return null;
        }
    }
}
