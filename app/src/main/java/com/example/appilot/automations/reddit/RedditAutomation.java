
package com.example.appilot.automations.reddit;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;
import com.example.appilot.HomeActivity;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.appilot.MainActivity;
import com.example.appilot.UnsafeHttpClient;
import com.example.appilot.automations.Interfaces.Action;
import com.example.appilot.managers.DeviceRegistrationManager;
import com.example.appilot.services.MyAccessibilityService;
import com.example.appilot.utils.HelperFunctions;
//import com.google.android.ads.mediationtestsuite.activities.HomeActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

import java.io.IOException;

@FunctionalInterface
interface HttpSuccessCallback {
    void onSuccess(String responseBody);
}

@FunctionalInterface
interface HttpFailureCallback {
    void onFailure(IOException e);
}


@FunctionalInterface
interface CommentAction {
    void execute();
}

@FunctionalInterface
interface CommentActionWithParams {
    void execute(String commentingType, String level, GetComment callback);

}

@FunctionalInterface
interface GetComment {
    void execute(String commentingType);
}


public class RedditAutomation {
    private static final String TAG = "RedditRandomScrollUpvote";
    private static final String REDDIT_PACKAGE = "com.reddit.android";

    // Define tokens for different tasks
    private Object nextActionToken = new Object();
    private Object scrollToken = new Object();
    private Object commentToken = new Object();
    private Object votingToken = new Object();
    private Object closeAppToken = new Object();

    // More human-like timing variables
    private static final int READ_AND_VOTE_CHANCE = 50;
    private static final int BASE_SCROLL_DELAY = 1500; // Base delay between scrolls
    private static final int RANDOM_SCROLL_DELAY = 2000; // Additional random delay
    private static final int UPVOTE_CHANCE = 8; // 20% chance to upvote (more natural)
    private static final int COMMENT_CHANCE = 4; // 4% chance to upvote (more natural)
    private static final int SCROLL_UP_CHANCE = 10; // 15% chance to scroll up
    private static final int READ_PAUSE_CHANCE = 50; // 25% chance to pause for "reading"
    private static final int READ_PAUSE_MIN = 1000; // Minimum reading pause
    private static final int READ_PAUSE_MAX = 2500; // Maximum reading pause

    private final Context context;
    private final Handler handler;
    private final Random random;
    private long startTime;
    private final int duration;

    private boolean endlessScrolling;
    private boolean reverseScrolling;
    private boolean microScroll;

    private boolean upvoting;
    private boolean downvoting;
    private boolean readAndUpvote;

    private boolean quickComment;
    private boolean detailedComment;
    private boolean replyComment;
    private String mainComment = null;
    private String level2Comment = null;
    private AccessibilityNodeInfo commentToReplyNode = null;
    private String isCommenting;
    private String userType;

    private static final int COMMENT_OPEN_CHANCE = 15; // 30% chance to open comments
    private static final int COMMENT_SCROLL_CHANCE = 50;
    private static final int COMMENT_SCROLL_DELAY = 3000; // Base delay for scrolling comments
    private static final int COMMENT_SCROLL_RANDOM_DELAY = 4000; // Additional random delay for scrolling comments

    private boolean inCommentsSection = false;
    private boolean isprocessingComments = false;
    private int noOfComments = 0;
    private int noOfCommentScroll = 0;
    private int noOfCommentScrollForReply = 0;


    private int noOfCommentScrollPerformed = 0;
    private boolean isScrolling = true;
    private boolean isProcessingUpvote = false;

    private int scrollDownCount = 0;
    private int scrollsSinceLastComment = 0;
    private int maxUpvote;
    private int maxComment;

    private boolean stopVoting = false;
    private int upvotesDone = 0;
    private int commentsDone = 0;
    private int currentMinuteUpvotes = 0; // Tracks upvotes in the current upvoteDuration
    private int currentMinuteComments = 0; // Tracks comments in the current commentDuration
    private long upvotinglastStartTime = 0;
    private long commentinglastStartTime = 0;
    private long upvoteDuration;
    private long commentDuration;

    private boolean downvotePerformed = false;
    private long downvoteStartTime;
    private long downvoteEndTime;
    private int enterCommentAttempts = 0;
    private int upvotesAttempts = 0;
    private int downVotesAttempts = 0;
    private String postTitle = "";
    private String postDesc;
    private String commentsListStr = "";
    private String commentQuery = "";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();

    private long nextUpvoteTime;
    private long nextCommentTime;
    private boolean upvoteDone = true;
    private boolean commentDone = true;
    private String Task_id;
    private String job_id;
    private HelperFunctions helperFunctions;

    public RedditAutomation(MyAccessibilityService service, int duration, JSONArray scrolling, JSONArray voteInputs, JSONArray commentInputs, JSONArray userType, int maxUpvote, int maxComment, long upvoteDuration, long commentDuration, String Task_id, String job_id) {
        this.Task_id = Task_id;
        this.job_id = job_id;
        this.context = service;
        this.handler = new Handler(Looper.getMainLooper());
        this.helperFunctions = new HelperFunctions(context, Task_id, job_id);
        this.random = new Random();
        this.maxUpvote = maxUpvote;
        this.maxComment = maxComment;
        this.upvoteDuration = upvoteDuration;
        this.commentDuration = commentDuration;
        try {
            this.endlessScrolling = scrolling.getJSONObject(0).has("Endless Downward Scroll") ? scrolling.getJSONObject(0).getBoolean("Endless Downward Scroll") : false;
            this.reverseScrolling = scrolling.getJSONObject(2).has("Reverse Scroll") ? scrolling.getJSONObject(2).getBoolean("Reverse Scroll") : false;
            this.microScroll = scrolling.getJSONObject(3).has("Micro-scroll Up and Down on a Post") ? scrolling.getJSONObject(3).getBoolean("Micro-scroll Up and Down on a Post") : false;
            this.upvoting = voteInputs.getJSONObject(0).has("Quick Upvote") ? voteInputs.getJSONObject(0).getBoolean("Quick Upvote") : false;
            this.downvoting = voteInputs.getJSONObject(1).has("Downvote") ? voteInputs.getJSONObject(1).getBoolean("Downvote") : false;
            this.readAndUpvote = voteInputs.getJSONObject(2).has("Upvote After Reading") ? voteInputs.getJSONObject(2).getBoolean("Upvote After Reading") : false;
            this.quickComment = commentInputs.getJSONObject(0).has("Quick Comment") ? commentInputs.getJSONObject(0).getBoolean("Quick Comment") : false;
            this.detailedComment = commentInputs.getJSONObject(1).has("Detailed Response") ? commentInputs.getJSONObject(1).getBoolean("Detailed Response") : false;
            this.replyComment = commentInputs.getJSONObject(2).has("Reply to Comment") ? commentInputs.getJSONObject(2).getBoolean("Reply to Comment") : false;
            if (userType.getJSONObject(0).has("Normal User") ? userType.getJSONObject(0).getBoolean("Normal User") : false) {
                this.userType = "normal";
            } else {
                this.userType = "extensive";
            }
        } catch (JSONException e) {
            Log.e(TAG, "Inputs are incorrect", e);
        }
        this.duration = duration * 60 * 1000;
        this.inCommentsSection = false;
    }

    public void startScrollingAndUpvoting() {
        Log.d(TAG, "Starting Reddit automation with human-like behavior...");
        Log.d(TAG, "User Type: " + this.userType);
        Log.d(TAG, "Endless Scrolling: " + this.endlessScrolling + " Reverse Scrolling: " + this.reverseScrolling + " Micro Scrolling: " + this.microScroll);
        Log.d(TAG, "upvoting: " + this.upvoting + " down voting: " + this.downvoting + " read and upvote: " + this.readAndUpvote);
        Log.d(TAG, "max Vote: " + this.maxUpvote);
        Log.d(TAG, "max Comments: " + this.maxComment);
        openRedditApp();
    }

    public void openRedditApp() {
        Log.d(TAG, "Launching Reddit via explicit MainActivity intent");

        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.reddit.frontpage", "com.reddit.launch.main.MainActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);

            handler.postDelayed(this::initiateScrollAndUpvote, 3000 + random.nextInt(2000));
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Reddit via explicit MainActivity", e);
            // You can remove fallback or handle accordingly
            launchInstagramExplicitly();
        }
    }

    private void launchInstagramExplicitly() {
        Log.d(TAG, "Entered launchInstagramExplicitly.");
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse("https://www.reddit.com"))
                .setPackage("com.reddit.frontpage")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
            handler.postDelayed(this::initiateScrollAndUpvote, 5000 + random.nextInt(5000));
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Reddit", e);
        }
    }

    private void initiateScrollAndUpvote() {
        this.upvotinglastStartTime = System.currentTimeMillis();
        this.commentinglastStartTime = System.currentTimeMillis();
        this.startTime = System.currentTimeMillis();

        if (this.upvoting) {
            // Correct calculation of time per upvote
            // Correct calculation of time per upvote
            this.upvoteDuration = this.upvoteDuration / this.maxUpvote;
            Log.d(TAG, "maxUpvote: " + this.maxUpvote);
            Log.d(TAG, "timePerUpvote: " + this.upvoteDuration/1000 + " seconds");

            // Random offset within the total upvote duration
            long randomOffset = (long) (random.nextDouble() * this.upvoteDuration);
            this.nextUpvoteTime = this.upvotinglastStartTime + randomOffset;

            long nextUpvoteTimeFromNow = (this.nextUpvoteTime - this.upvotinglastStartTime) / 1000;
            Log.d(TAG, "Next Upvote Time (in seconds from now): " + nextUpvoteTimeFromNow);
        }

        if(this.quickComment || this.detailedComment || this.replyComment){
            // Correct calculation of time per comment
            this.commentDuration = this.commentDuration / this.maxComment;
            Log.d(TAG, "maxComment: " + this.maxComment);
            Log.d(TAG, "timePerComment: " + this.commentDuration/1000 + " seconds");

            // Random offset within the total comment duration
            long randomOffset = (long) (random.nextDouble() * this.commentDuration);
            this.nextCommentTime = this.commentinglastStartTime + randomOffset;

            long nextCommentTimeFromNow = (this.nextCommentTime - this.commentinglastStartTime) / 1000;
            Log.d(TAG, "Next Comment Time (in seconds from now): " + nextCommentTimeFromNow);
        }
        if (this.downvoting) {
            int startPercent = random.nextInt(80);
            this.downvoteStartTime = this.startTime + (duration * startPercent) / 100;
            int endPercent = 1 + random.nextInt(5);
            this.downvoteEndTime = this.downvoteStartTime + (duration * endPercent) / 100;
        }
        handler.postDelayed(this::performNextAction, 1000);
    }

    private void performNextAction() {
        if (System.currentTimeMillis() - startTime >= duration) {
            Log.d(TAG, "Automation duration completed");
            cleanupAndExit();
            return;
        }

        if (inCommentsSection || isProcessingUpvote || isprocessingComments) {
            Log.d(TAG, "Comment automation is active returning");
            return;
        }

        if (random.nextInt(100) < READ_PAUSE_CHANCE) {
            int readingTime = READ_PAUSE_MIN + random.nextInt(READ_PAUSE_MAX - READ_PAUSE_MIN);
            Log.d(TAG, "Pausing to 'read' content for " + readingTime + "ms");
            handler.postDelayed(this::decideNextAction, readingTime);
        } else {
            decideNextAction();
        }
    }

    private void decideNextAction() {
        int action = random.nextInt(100);
        long currentTime = System.currentTimeMillis();

        if (this.downvoting) {
            if ((!this.downvotePerformed && currentTime >= downvoteStartTime && currentTime <= downvoteEndTime) || (!this.downvotePerformed && currentTime > downvoteEndTime)) {
                // Execute the block within the downvoting time frame
                attemptDownVote();
                return;
            }
        }

        if (this.quickComment || this.detailedComment || this.replyComment) {

            placeComment();
            return;
//            if (currentTime >= this.nextCommentTime && this.commentDone) {
//                placeComment();
//                return;
//            }
//
//            // Reset the comment timer if full duration has passed
//            if (currentTime - commentinglastStartTime >= this.commentDuration) {
//                this.commentinglastStartTime = currentTime;
//                long randomOffset = (long) (random.nextDouble() * this.commentDuration);
//                this.nextCommentTime = currentTime + randomOffset; // Use current time
//                Log.e(TAG, "comment timer reset. Ready for next comment.");
//                long nextUpvoteTimeFromNow = (this.nextCommentTime - this.commentinglastStartTime) / 1000;
//                Log.e(TAG, "Next omment Time (in seconds from now): " + nextUpvoteTimeFromNow);
////                Log.d(TAG, "Next Upvote Time (absolute): " + this.nextCommentTime);
//                this.commentDone = true;
//            }
        }


        if (this.upvoting) {
            // Perform upvote if within the random offset time
            if (currentTime >= this.nextUpvoteTime && this.upvoteDone) {
                attemptUpvote(15, "upvote");
                return;
            }

            // Reset the upvote timer if the full duration has passed
            if (currentTime - upvotinglastStartTime >= this.upvoteDuration) {
                this.upvotinglastStartTime = currentTime;
                long randomOffset = (long) (random.nextDouble() * this.upvoteDuration);
                this.nextUpvoteTime = currentTime + randomOffset; // Use current time
                Log.e(TAG, "Upvote timer reset. Ready for next upvote.");
                long nextUpvoteTimeFromNow = (this.nextUpvoteTime - this.upvotinglastStartTime) / 1000;
                Log.d(TAG, "Next Upvote Time (in seconds from now): " + nextUpvoteTimeFromNow);
                this.upvoteDone = true;
            }

        }

        if (this.microScroll && !inCommentsSection && action < COMMENT_OPEN_CHANCE) {
            Log.d(TAG,"if (this.microScroll && !inCommentsSection && action < COMMENT_OPEN_CHANCE");
            if (scrollsSinceLastComment >= 2 && scrollDownCount > 2) {
                Log.d(TAG,"if");
                enterComments(this::scrollComments, 3, "comments");
                scrollsSinceLastComment = 0;
            } else {
                Log.d(TAG,"else");
                ++scrollDownCount;
                ++scrollsSinceLastComment;
                performScrollDown();
            }
        }else if (this.reverseScrolling && action < SCROLL_UP_CHANCE) {
            Log.d(TAG,"else if (this.reverseScrolling && action < SCROLL_UP_CHANCE)");
            if (scrollDownCount <= 4) {
                Log.d(TAG,"if");
                ++scrollDownCount;
                ++scrollsSinceLastComment;
                performScrollDown();
            } else {
                Log.d(TAG,"else");
                scrollDownCount = 0;
                performScrollUp();
            }
        } else {
            Log.d(TAG,"else");
            if (scrollDownCount <= 4) {
                Log.d(TAG,"if");
                ++scrollDownCount;
                ++scrollsSinceLastComment;
                performScrollDown();
            } else {
                Log.d(TAG,"else");
                scrollDownCount = 1;
                ++scrollsSinceLastComment;
                performScrollDown();
            }
        }
    }

    private void performScrollDown() {
        Path swipePath = new Path();
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        float startY = screenHeight * (0.65f + random.nextFloat() * 0.15f);
        float endY = screenHeight * (0.15f + random.nextFloat() * 0.15f);
        float xVariation = screenWidth * (0.1f * random.nextFloat() - 0.05f);

        swipePath.moveTo(screenWidth / 2f + xVariation, startY);
        swipePath.lineTo(screenWidth / 2f + xVariation, endY);

        Log.d(TAG, "Scrolling  Down");
        performScrollGesture(swipePath, "down");
    }
    private void performScrollUp() {
        if (isProcessingUpvote) {
            return;
        }
        Path swipePath = new Path();
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        float startY = screenHeight * (0.2f + random.nextFloat() * 0.1f);
        float endY = screenHeight * (0.7f + random.nextFloat() * 0.1f);
        float xVariation = screenWidth * (0.1f * random.nextFloat() - 0.05f);

        swipePath.moveTo(screenWidth / 2f + xVariation, startY);
        swipePath.lineTo(screenWidth / 2f + xVariation, endY);

        performScrollGesture(swipePath, "up");
        Log.d(TAG, "RETURNED TO UP");
    }
    private void performScrollGesture(Path path, String direction) {
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        int gestureDuration = 150 + random.nextInt(150);
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 0, gestureDuration));

        try {
            MyAccessibilityService service = (MyAccessibilityService) context;
            isScrolling = true;
            service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    int delay = BASE_SCROLL_DELAY + random.nextInt(RANDOM_SCROLL_DELAY);
                    handler.removeCallbacksAndMessages(null);
//                    handler.removeCallbacksAndMessages(scrollToken);
//                    handler.removeCallbacksAndMessages(commentToken);

                    if (inCommentsSection) {
                        handler.postDelayed(() -> scrollComments(), scrollToken, delay);
                    } else {
                        handler.postDelayed(() -> performNextAction(), delay);
                    }
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    handler.postDelayed(() -> performNextAction(), 1000);
                }
            }, null);
        } catch (Exception e) {
            Log.e(TAG, "Error during scroll " + direction, e);
            handler.postDelayed(() -> performNextAction(), nextActionToken, 1000);
        }
    }
    //upvoting funtions
    private void attemptUpvote(int attemptsAllowed, String Check) {
        Log.e(TAG, "Entered to attempt upvote");
        handler.removeCallbacksAndMessages(null);
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "root note not found for upvoting");
            isProcessingUpvote = false;
            handler.postDelayed(() -> {
                performNextAction();
            }, 700 + random.nextInt(600));
            return;
        }
        // Get the height of the header
//        int headerHeight = getToolbar(rootNode);
//        if (headerHeight == 0) {
//            Log.e(TAG, "could not found header Height!");
//            isProcessingUpvote = false;
//            performNextAction();
//            return;
//        }
//        Log.d(TAG, "got HeaderHeaight: " + headerHeight);
//        // Get the height of the footer
//        int footerHeight = getBottomNav(rootNode);
//        if (footerHeight == 0) {
//            Log.e(TAG, "could not found footer Height!");
//            isProcessingUpvote = false;
//            performNextAction();
//            return;
//        }
//        Log.d(TAG, "got footerHeight: " + footerHeight);
//
////         Calculate the screen height and effective visible area
//        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
//        int visibleTop = headerHeight;
//        int visibleBottom = screenHeight - footerHeight;
//        AccessibilityNodeInfo postNode = getVisibleNode(rootNode, visibleTop, visibleBottom, Check);
        AccessibilityNodeInfo postNode = getVisibleNode(rootNode, Check);
        if (postNode != null) {
            Log.e(TAG, "Found Post to Upvote!");
            AccessibilityNodeInfo footerSection = HelperFunctions.findNodeByResourceId(postNode, "post_footer");
            if (footerSection != null && footerSection.getChildCount() > 0) {
                Log.e(TAG, "Found Post to Upvote footer!");
                AccessibilityNodeInfo upvoteButton = footerSection.getChild(0);

                if (upvoteButton != null) {
                    Log.e(TAG, "Found Post to Upvote button!");
                    android.graphics.Rect bounds = new android.graphics.Rect();
                    upvoteButton.getBoundsInScreen(bounds);
                    upvotesAttempts =0;
                    this.upvoteDone = false;
                    if (this.readAndUpvote && random.nextInt(100) < READ_AND_VOTE_CHANCE) {
                        Log.d(TAG, "Performing Upvote after reading!");
                        handler.postDelayed(() -> {
                            performUpvoteClick(bounds);
                        }, votingToken, 4500 + random.nextInt(2000));
                    } else {
                        handler.postDelayed(() -> {
                            performUpvoteClick(bounds);
                        }, votingToken, 600 + random.nextInt(200));
                    }
                    return;
                }
            }
        }
        if (upvotesAttempts < attemptsAllowed) {
            Log.e(TAG, "post node is null retrying");
            ++upvotesAttempts;
            Log.e(TAG, " retrying time: " + upvotesAttempts);
            scrollToGetVisibleNode(()->{}, Check);
            return;
        }
        Log.e(TAG, "no post found to Upvote");
        isProcessingUpvote = false;
        upvotesAttempts = 0;
        handler.postDelayed(() -> {
            performNextAction();
        }, 800 + random.nextInt(1000));
    }
    private void performUpvoteClick(android.graphics.Rect bounds) {
        Path clickPath = new Path();
        float xOffset = (random.nextFloat() * 10) - 5;
        float yOffset = (random.nextFloat() * 10) - 5;
        clickPath.moveTo(bounds.centerX() + xOffset, bounds.centerY() + yOffset);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, 50 + random.nextInt(50)));

        try {
            MyAccessibilityService service = (MyAccessibilityService) context;
            service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    isProcessingUpvote = false;
                    isScrolling = true;
                    currentMinuteUpvotes++;  // Track upvotes within the minute
                    upvotesDone++; // Track total upvotes performed
                    handler.postDelayed(() -> {
                        scrollToGetVisibleNode(RedditAutomation.this::performNextAction, "done");
                    }, 300 + random.nextInt(600));
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    isProcessingUpvote = false;
                    performNextAction();
                }
            }, null);
        } catch (Exception e) {
            isProcessingUpvote = false;
            performNextAction();
        }
    }
    //upvoting funtions

    //downvoting funtions
    private void attemptDownVote() {
        Log.e(TAG, "Entered to attempt downvote");
        ++downVotesAttempts;
        handler.removeCallbacksAndMessages(null);
        isProcessingUpvote = true;

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            handleFailure("Root node not found for downvoting");
            return;
        }

        // Get the height of the header
//        int headerHeight = getToolbar(rootNode);
//        if (headerHeight == 0) {
//            handleFailure("Header height is 0");
//            isProcessingUpvote = false;
//            performNextAction();
//            return;
//        }
//
//        // Get the height of the footer
//        int footerHeight = getBottomNav(rootNode);
//        if (footerHeight == 0) {
//            handleFailure("Bottom Nav Height is 0");
//            isProcessingUpvote = false;
//            performNextAction();
//            return;
//        }
//
//        // Calculate the screen height and effective visible area
//        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
//        int visibleTop = headerHeight;
//        int visibleBottom = screenHeight - footerHeight;
//
//        AccessibilityNodeInfo postNode = getVisibleNode(rootNode, visibleTop, visibleBottom, "downvote");
        AccessibilityNodeInfo postNode = getVisibleNode(rootNode, "downvote");
        if (postNode != null) {
            processPostNodeToDownvote(postNode);
        } else {
            handleNodeNotFound();
        }
    }
    private void handleFailure(String message) {
        Log.e(TAG, message);
        isProcessingUpvote = false;
        downVotesAttempts = 0;
        handler.postDelayed(this::performNextAction, 800 + random.nextInt(500));
    }
    private void processPostNodeToDownvote(AccessibilityNodeInfo postNode) {
        int childCount = postNode.getChildCount();
        AccessibilityNodeInfo nodeToDownvoteFooter = postNode.getChild(childCount - 1);
        if (nodeToDownvoteFooter != null) {
            AccessibilityNodeInfo downvoteButton = nodeToDownvoteFooter.getChild(0);
            if (downvoteButton != null) {
                android.graphics.Rect bounds = new android.graphics.Rect();
                downvoteButton.getBoundsInScreen(bounds);
                handler.postDelayed(() -> performDownvoteClick(bounds), 350 + random.nextInt(250));
                downvoteButton.recycle();
            } else {
                handleDownvoteButtonNotFound();
            }
        } else {
            handleDownvoteFooterNotFound();
        }
    }
    private void performDownvoteClick(android.graphics.Rect bounds) {
        Path clickPath = new Path();

        // Define x range for the downvote button (last 20% of bounds)
        float downvoteStartX = bounds.left + (bounds.width() * 0.8f); // Start of the downvote area
        float downvoteEndX = bounds.right; // End of the downvote area

        // Generate a random x-position within the downvote button range
        float xPosition = downvoteStartX + (random.nextFloat() * (downvoteEndX - downvoteStartX));
        float yOffset = (random.nextFloat() * 10) - 5; // Small random offset around the vertical center

        // Move to random position within the downvote area
        clickPath.moveTo(xPosition, bounds.centerY() + yOffset);

        // Build and dispatch the gesture
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, 50 + random.nextInt(50)));

        try {
            MyAccessibilityService service = (MyAccessibilityService) context;
            service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    handler.postDelayed(() -> {
                        Log.e(TAG, "performed downvote successfully!");
                        downVotesAttempts = 0;
                        isProcessingUpvote = false;
                        downvotePerformed = true;
                        performNextAction();
                    }, nextActionToken, 2000 + random.nextInt(1000));
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    isProcessingUpvote = false;
                    isScrolling = true;
                    performNextAction();
                }
            }, null);
        } catch (Exception e) {
            isProcessingUpvote = false;
            isScrolling = true;
            performNextAction();
        }
    }
    private void handleDownvoteButtonNotFound() {
        Log.e(TAG, "Post found but downvote button not found");
        if (downVotesAttempts < 3) {
            scrollToGetVisibleNode(this::attemptDownVote, "downvote");
        } else {
            handleFailure("Max downvote attempts reached. Exiting.");
        }
    }
    private void handleDownvoteFooterNotFound() {
        Log.e(TAG, "Post is found but footer is not found");
        if (downVotesAttempts < 3) {
            scrollToGetVisibleNode(this::attemptDownVote, "downvote");
        } else {
            handleFailure("Max downvote attempts reached. Exiting.");
        }
    }
    private void handleNodeNotFound() {
        Log.e(TAG, "Post node is null retrying");
        if (downVotesAttempts < 5) {
            scrollToGetVisibleNode(this::attemptDownVote, "downvote");
        } else {
            handleFailure("Max downvote attempts reached. Exiting.");
        }
    }
    //downvoting funtions

    //Comments related functions
    private void enterComments(CommentAction action, int attemptsAllowed, String Check) {
        Log.d(TAG, "Attempting to enter comments section...");
        isprocessingComments = true;
        handler.removeCallbacksAndMessages(null);
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "Root node is null - cannot proceed with comment section entry");
            handler.postDelayed(() -> {
                performNextAction();
            }, 700 + random.nextInt(400));
            return;
        }
//         Get the height of the header
//        int headerHeight = getToolbar(rootNode);
//        if (headerHeight == 0) {
//            Log.e(TAG, "could not found header Height!");
//            isprocessingComments = false;
//            performNextAction();
//            return;
//        }
//        Log.d(TAG, "got HeaderHeaight: " + headerHeight);
//        // Get the height of the footer
//        int footerHeight = getBottomNav(rootNode);
//        if (footerHeight == 0) {
//            Log.e(TAG, "could not found footer Height!");
//            isprocessingComments = false;
//            performNextAction();
//            return;
//        }
//        Log.d(TAG, "got footerHeight: " + footerHeight);

        // Calculate the screen height and effective visible area
//        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
//        int visibleTop = headerHeight;
//        int visibleBottom = screenHeight - footerHeight;
//        AccessibilityNodeInfo postNode = getVisibleNode(rootNode, visibleTop, visibleBottom, Check);
        AccessibilityNodeInfo postNode = getVisibleNode(rootNode, Check);
        if (postNode != null) {
            AccessibilityNodeInfo commentButton = HelperFunctions.findNodeByResourceId(postNode, "post_comment_button");
            android.graphics.Rect bounds = new android.graphics.Rect();
//            commentButton.getBoundsInScreen(bounds);
//            commentButton.recycle();
            if (commentButton != null) {
                Log.d(TAG, "going to press Comment button");
                commentButton.getBoundsInScreen(bounds);
                enterCommentAttempts = 0;
                handler.postDelayed(() -> {
                    performCommentClick(bounds, action);
                }, 1200 + random.nextInt(800));
                return;
            } else {
                Log.e(TAG, "post found but not comment button!");
                isprocessingComments = false;
                inCommentsSection = false;
                enterCommentAttempts = 0;
                handler.postDelayed(() -> {
                    performNextAction();
                }, 800 + random.nextInt(1000));
                return;
            }
        }
        if (enterCommentAttempts < attemptsAllowed) {
            Log.e(TAG, "post node is null retrying");
            ++enterCommentAttempts;
            Log.e(TAG, " retrying time: " + enterCommentAttempts);

            scrollToGetVisibleNode(action, Check);
            return;
        }
        Log.e(TAG, "no post found");
        isprocessingComments = false;
        enterCommentAttempts = 0;
        handler.postDelayed(() -> {
            performNextAction();
        }, 800 + random.nextInt(1000));
        return;
    }
    private void placeComment() {
        int choose = random.nextInt(100);
        if (choose <= 45) {
            // Case 1: 0-35% chance
            if (quickComment) {
                quickReply();
            } else {
                handleReplyChoice(detailedComment);
            }
        } else if (choose <= 85) {
            // Case 2: 36-85% chance
            if (detailedComment) {
                detailedReply();
            } else {
                handleQuickReplyChoice(quickComment);
            }
        } else {
            // Case 3: 86-99% chance
            if (replyComment) {
                replyComment();
            } else {
                handleReplyCommentChoice(quickComment);
            }
        }
    }
    private void handleReplyChoice(boolean check){
        if(check){
            detailedReply();
        }else{
            replyComment();
        }
    }
    private void handleQuickReplyChoice(boolean check){
        if(check){
            quickReply();
        }else{
            replyComment();
        }
    }
    private void handleReplyCommentChoice(boolean check){
        if(check){
            quickReply();
        }else{
            detailedReply();
        }
    }
    private void quickReply() {
        if (!inCommentsSection) {
            Log.w(TAG, "Entered to Quick Reply a comment");
            postTitle = "";
            postDesc = "";
            commentQuery = "";
            isprocessingComments = true;
            handler.removeCallbacksAndMessages(null);
            AccessibilityNodeInfo rootnode = getRootInActiveWindow();
            if (rootnode == null) {
                Log.e(TAG, "root node not found");
                isprocessingComments = true;
                handler.postDelayed(this::performNextAction, 700 + random.nextInt(400));
                return;
            }
            enterComments(this::quickReply, 10, "commenting");
        } else {
            Log.w(TAG, "Entered a post where comments are greater than 10 and returned to Quickreply");
            handler.postDelayed(() -> {
                ScrollUpToGetPostData(() -> {
                    handler.postDelayed(() -> {
                        this.getCommentsData("quick", "Level 1 ", this::createPayloadData);
                    }, 2000 + random.nextInt(2000));
                });
            }, 1500 + random.nextInt(1000));
        }
    }
    private void createPayloadData(String check) {
        Log.e(TAG, "Check: " + check);
        // Check if postTitle or commentQuery is empty

        if(check == "replyComment"){
            if(postTitle.isEmpty() || mainComment.isEmpty() || level2Comment.isEmpty() || commentToReplyNode == null || commentQuery == null){
                Log.e(TAG, "Post title or mainComment or level2comment or commentToReplyNode are not available for reply a comment");
                exitCommentsAndContinue();
                return;
            }
            StringBuilder queryBuilder = new StringBuilder();

            queryBuilder.append("Task: Generate a short reply to the comment of the Reddit post below. Use the materials provided to help craft your comment.\n");
            queryBuilder.append("Original Post:\nTitle: ").append(postTitle).append("\n\n");

            // Add post description if available
            if (!postDesc.isEmpty()) {
                queryBuilder.append("Description: ").append(postDesc).append("\n");
            }
            queryBuilder.append("Top 10 Comments from Other Reddit Users:\n");
            queryBuilder.append(commentQuery).append("\n");
            queryBuilder.append("Targeted Comment for which to genrate a reply: \n").append("\n");
            queryBuilder.append("{ \n").append("\n");
            queryBuilder.append("main comment: ");
            queryBuilder.append(mainComment).append("\n");
            queryBuilder.append("Other Reddit user Replied to thsi main comment: ");
            queryBuilder.append(level2Comment).append("\n");
            queryBuilder.append("}\n").append("\n");
            queryBuilder.append("Guidelines for Reply Generation:\n" +
                    "\n" +
                    "\t1.\tUnderstand the Topic: Analyze the original post, its description, and top comments to understand the subject and tone and targeted comment to which you will give reply to. \n" +
                    "\n" +
                    "\t2.\tReply Requirements:\n" +
                    "\t•\tYour Reply must be within 3-15 words it can be short or long depend upon your understanding and feasibilty. \n" +
                    "\t•\tIt should provide a egnaging and relevant opinion.\n" +
                    "\t•\tThe tone and style should be the mixture of provided top comments for authenticity. \n" +
                    "\t3.\tOutput Format: \n Provide only the Reply itself, with no additional text or quotes.");
            // Convert StringBuilder to String
            String query = queryBuilder.toString();

            // Log the constructed query
            Log.d(TAG, "------------------------------- Comment Query -----------------------------");
            Log.d(TAG, query);
            Log.d(TAG, "-------------------------------------------------------------------------");
            handler.postDelayed(() -> {
                createComment(this::replyComment, this::creatingCommentFailed, query);
            }, 2000 + random.nextInt(2000));
            return;
        }

        if (postTitle.isEmpty() || commentQuery.isEmpty()) {
            Log.e(TAG, "Post title or comments are not available");
            exitCommentsAndContinue();
            return; // Exit if either title or comments are not available
        }

        // Build the query string using StringBuilder for efficiency
        StringBuilder queryBuilder = new StringBuilder();

        if ("quick".equals(check)) {
            queryBuilder.append("Task: Generate a quick response to the Reddit post below. Use the materials provided to help craft your comment.\n");
            queryBuilder.append("Original Post:\nTitle: ").append(postTitle).append("\n\n");
            if (!postDesc.isEmpty()) {
                queryBuilder.append("Description: ").append(postDesc).append("\n");
            }
            queryBuilder.append("Top 10 Comments from Other Reddit Users:\n");
            queryBuilder.append(commentQuery).append("\n");
            queryBuilder.append("Guidelines for Comment Generation:\n" +
                    "\n" +
                    "\t1.\tUnderstand the Topic: Analyze the original post, its description, and top comments to understand the subject and tone.\n" +
                    "\t2.\tComment Requirements:\n" +
                    "\t•\tYour comment must be 3-5 words.\n" +
                    "\t•\tIt should provide a concise opinion or reaction.\n" +
                    "\t•\tThe tone and style should be the mixture of provided top comments for authenticity. \n" +
                    "\t3.\tOutput Format: Provide only the comment itself as plain text. Do not include any prefixes, headers, or quotation marks.");
        } else if ("detailed".equals(check)) {
            queryBuilder.append("Task: Generate a detailed response to the Reddit post below. Use the materials provided to help craft your comment.\n");
            queryBuilder.append("Original Post:\nTitle: ").append(postTitle).append("\n\n");
            if (!postDesc.isEmpty()) {
                queryBuilder.append("Description: ").append(postDesc).append("\n");
            }
            queryBuilder.append("Top 10 Comments from Other Reddit Users:\n");
            queryBuilder.append(commentQuery).append("\n");
            queryBuilder.append("Guidelines for Comment Generation:\n" +
                    "\n" +
                    "\t1.\tUnderstand the Topic: Analyze the original post, its description, and top comments to understand the subject and tone.\n" +
                    "\n" +
                    "\t2.\tComment Requirements:\n" +
                    "\t•\tYour comment must be 15-20 words.\n" +
                    "\t•\tIt should provide a detailed, thoughtful insight and relevant opinion.\n" +
                    "\t•\tThe tone and style should be the mixture of provided top comments for authenticity. \n" +
                    "\t3.\tOutput Format: Provide only the comment itself as plain text. Do not include any prefixes, headers, or quotation marks.");
        }

        // Convert StringBuilder to String
        String query = queryBuilder.toString();

        // Log the constructed query
        Log.d(TAG, "------------------------------- Comment Query -----------------------------");
        Log.d(TAG, query);
        Log.d(TAG, "-------------------------------------------------------------------------");

        // Delay the exit and continue action
        handler.postDelayed(() -> {
            createComment(this::writeComment, this::creatingCommentFailed, query);
//            createComment(query);
        }, 1000 + random.nextInt(1000));
    }
    private void writeComment(String modelResponse) {
        Log.e(TAG, " Entered writeComment function");
        Log.d(TAG, "Generated Comment: " + modelResponse);
//        AccessibilityNodeInfo commentInput = getNodeInActivePage("com.reddit.frontpage:id/reply_text_view");
        AccessibilityNodeInfo commentInput = getNodeInActivePageByDirectId("com.reddit.frontpage:id/reply_text_view");
        if (commentInput == null) {
            Log.e(TAG, "commentInput node is null");
            exitCommentsAndContinue();
            return;
        }

        commentInput.performAction(AccessibilityNodeInfo.ACTION_CLICK);

        handler.postDelayed(() -> {
            AccessibilityNodeInfo inputField = getNodeInActivePageByDirectId("com.reddit.frontpage:id/reply_text");
            if (inputField == null) {
                Log.e(TAG, "inputField node is null");
                exitCommentsAndContinue();
                return;
            }
            Log.e(TAG,"recieved comment: " + modelResponse);
//            HelperFunctions helperFunctions = new HelperFunctions(context);
            String cleanedComment = helperFunctions.cleanComment(modelResponse);
            Log.e(TAG,"recieved comment after cleaning: " + cleanedComment);
            typeCommentHumanLike(inputField, cleanedComment, () -> {
                // After typing, find the post button
                handler.postDelayed(() -> {
                    AccessibilityNodeInfo postButton = getNodeInActivePage("com.reddit.frontpage:id/menu_item_text");
                    if (postButton == null) {
                        Log.e(TAG, "postButton node is null");
                        closeCommentInputFielsAndExitComments();
                        return;
                    }

                    // Perform the post action
                    postButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.d(TAG, "Pressed Post Button!");
                    currentMinuteComments++;  // Track upvotes within the minute
                    commentsDone++; // Track total upvotes performed
                    Log.d(TAG, "Comments Done: " + commentsDone);
                    this.commentDone = false; // Ensure no repeated actions
                    handler.postDelayed(this::ExitCommentsAndscrollAndContinue, 7000 + random.nextInt(5000));
                }, 2000);
            });
        }, 3000 + random.nextInt(2000));

    }

    private void typeCommentHumanLike(AccessibilityNodeInfo inputField, String comment, Runnable onComplete) {
        // A separate handler for typing character by character
        Handler typingHandler = new Handler(Looper.getMainLooper());
        char[] chars = comment.toCharArray();
        long typingDelay = 50 + random.nextInt(150); // Random delay between 50-200ms

        typeCharacter(typingHandler, inputField, chars, 0, typingDelay, onComplete);
    }

    private void typeCharacter(Handler typingHandler, AccessibilityNodeInfo inputField, char[] chars, int index, long typingDelay, Runnable onComplete) {
        if (index >= chars.length) {
            // Finished typing, call the onComplete action
            onComplete.run();
            return;
        }

        // Set the current text in the input field
        String typedSoFar = new String(chars, 0, index + 1);
        Bundle args = new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, typedSoFar);
        inputField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);

        // Schedule the next character typing
        typingHandler.postDelayed(() -> typeCharacter(typingHandler, inputField, chars, index + 1, 50 + random.nextInt(150), onComplete), typingDelay);
    }

    private void replyComment(String modelResponse){
        AccessibilityNodeInfo replyButton = HelperFunctions.findNodeByResourceId(commentToReplyNode,"com.reddit.frontpage:id/reply_to_comment");
        if(replyButton == null){
            Log.d(TAG,"Reply button not found in Comment");
            ExitCommentsAndscrollAndContinue();
            return;
        }

        replyButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);

        handler.postDelayed(()->{
            AccessibilityNodeInfo inputField = getNodeInActivePageByDirectId("com.reddit.frontpage:id/reply_text");
            if (inputField == null) {
                Log.e(TAG, "inputField node is null");
                exitCommentsAndContinue();
                return;
            }
//            Bundle args = new Bundle();
//            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, modelResponse);
//
//            // Set the text
//            boolean success = inputField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
//            if(success){
            Log.e(TAG,"Recieved comment: " + modelResponse);
//            HelperFunctions helperFunctions = new HelperFunctions(context);
            String cleanedComment = helperFunctions.cleanComment(modelResponse);
            Log.e(TAG,"Recieved comment after cleaning: " + cleanedComment);
            typeCommentHumanLike(inputField, cleanedComment, () -> {
                handler.postDelayed(()->{
                    AccessibilityNodeInfo postButton = getNodeInActivePage("com.reddit.frontpage:id/menu_item_text");
                    if (postButton == null) {
                        Log.e(TAG, "postButton node is null");
                        closeCommentInputFielsAndExitComments();
                        return;
                    }
                    postButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.d(TAG,"Pressed Post Button!");
                    currentMinuteComments++;  // Track upvotes within the minute
                    commentsDone++; // Track total upvotes performed
                    Log.d(TAG,"Comments Done: "+commentsDone);
                    this.commentDone = false; // Ensure no repeated actions
                    handler.postDelayed(
//                            this::closeCommentInputFielsAndExitComments
                            this::ExitCommentsAndscrollAndContinue
                            , 7000+ random.nextInt(5000));

                },2000);
//            }else{
//                Log.e(TAG, "toolbar node is null");
//                closeCommentInputFielsAndExitComments();
//            }
        });
        }, 3000+ random.nextInt(2000));
    }
    private void closeCommentInputFielsAndExitComments(){
        Log.d(TAG," Exiting comments through close button");
        AccessibilityNodeInfo toolbar = getNodeInActivePage("com.reddit.frontpage:id/toolbar");
        if(toolbar == null || toolbar.getChildCount() <=0){
            Log.e(TAG,"Could not found toolbar");
            closeCommentInputFielsAndExitCommentsManually();
            return;
        }

        AccessibilityNodeInfo closeButton = toolbar.getChild(0);
        if(closeButton == null){
            Log.e(TAG,"Could not found close button");
            closeCommentInputFielsAndExitCommentsManually();
            return;
        }
        closeButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        handler.postDelayed(this::ExitCommentsAndscrollAndContinue,4000+random.nextInt(2000));
    }
    private void closeCommentInputFielsAndExitCommentsManually(){
        Log.d(TAG," Exiting comments manually");
        moveBack();
        handler.postDelayed(()->{
            moveBack();
            handler.postDelayed(()->{
                ExitCommentsAndscrollAndContinue();
            },1500 + random.nextInt(2000));
        },1500 + random.nextInt(2000));
    }
    private void typeComment(String response) {
        Log.d(TAG, "Entered typeComment function");
        ExitCommentsAndscrollAndContinue();
    }
    private void creatingCommentFailed(IOException e) {
        Log.e(TAG, "Error while getting response from model to generate comment: "+e);
        ExitCommentsAndscrollAndContinue();
    }
    private void detailedReply() {
        if (!inCommentsSection) {
            Log.w(TAG, "Entered to detailed Reply a comment");
            postTitle = "";
            postDesc = "";
            commentQuery = "";
            isprocessingComments = true;
            handler.removeCallbacksAndMessages(null);
            AccessibilityNodeInfo rootnode = getRootInActiveWindow();
            if (rootnode == null) {
                Log.e(TAG, "root node not found");
                isprocessingComments = true;
                handler.postDelayed(this::performNextAction, 700 + random.nextInt(400));
                return;
            }
            enterComments(this::detailedReply, 10, "commenting");
        } else {
            Log.w(TAG, "Entered a post where comments are greater than 10 and returned to detailedReply");
            handler.postDelayed(() -> {
                ScrollUpToGetPostData(() -> {
                    handler.postDelayed(() -> {
                        this.getCommentsData("detailed", "Level 1 ", this::createPayloadData);
                    }, 2000 + random.nextInt(1000));
                });
            }, 1500 + random.nextInt(1000));
        }
    }
    private void replyComment() {
        if (!inCommentsSection) {
            Log.w(TAG, "Entered to Reply a comment");
            postTitle = "";
            postDesc = "";
            commentQuery = "";
            isprocessingComments = true;
            handler.removeCallbacksAndMessages(null);
            AccessibilityNodeInfo rootnode = getRootInActiveWindow();
            if (rootnode == null) {
                Log.e(TAG, "root node not found");
                isprocessingComments = true;
                handler.postDelayed(this::performNextAction, 700 + random.nextInt(400));
                return;
            }
            enterComments(this::replyComment, 10, "commenting");
        } else {
            Log.w(TAG, "Entered a post where comments are greater than 10 and returned to replyComment");
            handler.postDelayed(() -> {
                ScrollUpToGetPostData(() -> {
                    handler.postDelayed(() -> {
                        this.getCommentsData("replyComment", "Level 1 ", this::moveToTopOfPost);
                    }, 2000 + random.nextInt(1000));
                });
            }, 1500 + random.nextInt(1000));
        }
    }
    public void moveToTopOfPost(String commentingType){
        AccessibilityNodeInfo miniContextBar = getNodeInActivePage("mini_context_bar");
        if(miniContextBar == null){
            Log.d(TAG, "could not move to the top of post again");
            ExitCommentsAndscrollAndContinue();
        }
        handler.postDelayed(()->{
            miniContextBar.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            handler.postDelayed(()->{
                this.getCommentDataToReply(commentingType, "Level 1 ", this::createPayloadData);
            }, 2000 + random.nextInt(2000));
        }, 1000+ random.nextInt(2000));
    }
    public void createComment(HttpSuccessCallback callback, HttpFailureCallback failCallBack, String payload) {
        Log.e(TAG, "Entered createComment function");
//        callback.onSuccess("");
//        return;
        new Thread(() -> {
            try {
                // Use the unsafe OkHttpClient
                OkHttpClient client = UnsafeHttpClient.getUnsafeOkHttpClient();

                // Create the JSON object with the text
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("text", payload);

                // Prepare the request body
                RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url("https://legaitech.xyz/api/") // Ensure you're using the correct URL
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .build();

                // Execute the request
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    // Handle success
                    if (callback != null) {
                        String responseBody = response.body().string();
                        JSONObject res = new JSONObject(responseBody);
                        String modelResponse = res.getString("model_response");
                        if (modelResponse.startsWith("\"") && modelResponse.endsWith("\"")) {
                            modelResponse = modelResponse.substring(1, modelResponse.length() - 1);
                        }
                        callback.onSuccess(modelResponse);
                    }else{
                        Log.e(TAG, "No success callback provided");
                        closeCommentInputFielsAndExitComments();
                    }
                } else {
                    // Handle error response
                    if (failCallBack != null) {
                        failCallBack.onFailure(new IOException("Request failed with code: " + response.code()));
                    }else{
                        Log.e(TAG, "No failed callback provided");
                        closeCommentInputFielsAndExitComments();
                    }
                }
            } catch (Exception e) {
                if (failCallBack != null) {
                    failCallBack.onFailure(new IOException("Unexpected error: " + e.getMessage(), e));
                }else{
                    Log.e(TAG, "No failed callback provided");
                    closeCommentInputFielsAndExitComments();
                }
            }
        }).start();
    }
    private void getCommentsData(String commentingType, String level, GetComment callback) {
//        AccessibilityNodeInfo commentsList = getLatestNode("com.reddit.frontpage:id/detail_list");
        AccessibilityNodeInfo commentsList = findLatestNodeForFeedscreenPager();
        if (commentsList == null) {
            Log.e(TAG,"detail_list was not found");
            exitCommentsAndContinue();
            return;
        }
        Log.w(TAG, "detail list: " + commentsList);
        Log.w(TAG, "detail list childrens count: " + commentsList.getChildCount());
        // Check if post title and description have been captured correctly
        Log.d(TAG, "Attempting to capture postTitle and postDesc...");


        // Update postTitle and postDesc only if empty
        if (postTitle.isEmpty()) {
//            AccessibilityNodeInfo postThumbNail = HelperFunctions.findNodeByResourceId(commentsList, "com.reddit.frontpage:id/thumbnail_container");
//            AccessibilityNodeInfo postThumbNail = HelperFunctions.findNodeByResourceId(commentsList, "post_title");
//            if (postThumbNail != null) {
                AccessibilityNodeInfo postTitleNode = HelperFunctions.findNodeByResourceId(commentsList, "post_title");
                if (postTitleNode != null) {
                    postTitle = postTitleNode.getText().toString();
                    Log.d(TAG, "Captured postTitle: " + postTitle);
                }
//            }
        }

        if (postDesc.isEmpty()) {
            AccessibilityNodeInfo postRecyclerNode = HelperFunctions.findNodeByResourceId(commentsList, "post_self_content");
            if (postRecyclerNode != null && postRecyclerNode.getChildCount() > 0) {
                StringBuilder descBuilder = new StringBuilder();
                for (int i = 0; i < postRecyclerNode.getChildCount(); i++) {
                    AccessibilityNodeInfo descNode = postRecyclerNode.getChild(i);
                    if (descNode != null && descNode.getText() != null) {
                        descBuilder.append("\n").append(descNode.getText().toString());
                    }
                }
                postDesc = descBuilder.toString();
                Log.d(TAG, "Captured postDesc: " + postDesc);
            } else {
                Log.d(TAG, "Captured postDesc: " + "null");
            }
        }

        Log.i(TAG,"Post Title: "+postTitle);
        Log.i(TAG,"Post description: "+postDesc);

        // Continue with processing comments
        Log.e(TAG,"commentsList.getChildCount(): " + commentsList.getChildCount());
        for (int i = 0; i < commentsList.getChildCount(); i++) {
            AccessibilityNodeInfo child = commentsList.getChild(i);
            if (child == null) {
                Log.e(TAG, "Child node is null at index " + i);
                continue;
            }

            CharSequence contentDescription = child.getContentDescription();
            if (contentDescription == null || contentDescription.length() <= 0) continue;

            String LevelData = contentDescription.toString();

            if (!LevelData.contains(level)) continue;

            // Find the data node with the resource ID
            AccessibilityNodeInfo dataNode = HelperFunctions.findNodeByResourceId(child, "com.reddit.frontpage:id/comment_richtext");
            if (dataNode == null || dataNode.getChildCount() == 0) {
                Log.e(TAG, "Data node has no children or it is null at index " + i);
                continue; // Skip to the next iteration if the data node is null
            }

            dataNode = dataNode.getChild(0); // Get the first child
            StringBuilder commentBuilder = new StringBuilder(); // Use StringBuilder for efficiency

            for (int j = 0; j < dataNode.getChildCount(); j++) {
                AccessibilityNodeInfo textNode = dataNode.getChild(j);
                if (textNode == null) {
                    Log.e(TAG, "Text node is null at index " + j + " for comment index " + i);
                    continue; // Skip to the next iteration if the text node is null
                }

                CharSequence charPara = textNode.getText();
                if (charPara == null || charPara.length() <= 0) {
                    Log.e(TAG, "Text is empty or null at index " + j + " for comment index " + i);
                    continue; // Skip to the next iteration if text is empty or null
                }

                commentBuilder.append(charPara); // Append the non-empty text
            }

            String comment = commentBuilder.toString(); // Convert StringBuilder to String
            if (comment.isEmpty()) {
                Log.e(TAG, "No data found for comment index " + i);
            } else {
                if (!commentQuery.contains(comment)) {
                    Log.i(TAG, "Comment for index " + i + " is: " + comment);
                    commentQuery = commentQuery + "\n" + "- " + comment;
                }
            }
        }
        if (noOfCommentScroll > 0) {
            --noOfCommentScroll;
//            scrollToGetVisibleNode(callback, commentingType, level, this::getCommentsData);
            performClickCommentReadCapButton(callback, commentingType, level, this::getCommentsData);
        } else {
            callback.execute(commentingType);
        }
    }
    private void getCommentDataToReply(String commentingType, String level, GetComment callback) {

        AccessibilityNodeInfo commentsList = getLatestNode("com.reddit.frontpage:id/detail_list");
        if (commentsList == null) {
            exitCommentsAndContinue();
            return;
        }
        Log.w(TAG, "detail list: " + commentsList);
        Log.w(TAG, "detail list childrens count: " + commentsList.getChildCount());
        // Check if post title and description have been captured correctly
        Log.d(TAG, "Attempting to capture postTitle and postDesc...");

        // Update postTitle and postDesc only if empty
        if (postTitle.isEmpty()) {
            AccessibilityNodeInfo postThumbNail = HelperFunctions.findNodeByResourceId(commentsList, "com.reddit.frontpage:id/thumbnail_container");
            if (postThumbNail != null) {
                AccessibilityNodeInfo postTitleNode = HelperFunctions.findNodeByResourceId(postThumbNail, "com.reddit.frontpage:id/link_title");
                if (postTitleNode != null) {
                    postTitle = postTitleNode.getText().toString();
                    Log.d(TAG, "Captured postTitle: " + postTitle);
                }
            }
            if (postDesc.isEmpty()) {
                AccessibilityNodeInfo postRecyclerNode = HelperFunctions.findNodeByResourceId(commentsList, "com.reddit.frontpage:id/richtext_recyclerview");
                if (postRecyclerNode != null && postRecyclerNode.getChildCount() > 0) {
                    StringBuilder descBuilder = new StringBuilder();
                    for (int i = 0; i < postRecyclerNode.getChildCount(); i++) {
                        AccessibilityNodeInfo descNode = postRecyclerNode.getChild(i);
                        if (descNode != null && descNode.getText() != null) {
                            descBuilder.append("\n").append(descNode.getText().toString());
                        }
                    }
                    postDesc = descBuilder.toString();
                    Log.d(TAG, "Captured postDesc: " + postDesc);
                } else {
                    Log.d(TAG, "Captured postDesc: " + "null");
                }
            }
            performClickCommentReadCapButton(callback, commentingType, level, this::getCommentDataToReply);
        }

        mainComment = null;
        level2Comment = null;
        commentToReplyNode = null;
        StringBuilder commentListBuilder = new StringBuilder();
        // Continue with processing comments
        for (int i = 0; i < commentsList.getChildCount(); i++) {
            AccessibilityNodeInfo selectedComment = commentsList.getChild(i);
            if (selectedComment == null) {
                Log.e(TAG, "selectedComment node is null at index " + i);
                continue;
            }

            CharSequence contentDescription = selectedComment.getContentDescription();
            if (contentDescription == null || contentDescription.length() <= 0) continue;

            String LevelData = contentDescription.toString();

            if (LevelData.contains(level)) {
                AccessibilityNodeInfo selectedCommentNode = selectedComment;
                AccessibilityNodeInfo dataNode = HelperFunctions.findNodeByResourceId(selectedComment, "com.reddit.frontpage:id/comment_richtext");
                if (dataNode == null || dataNode.getChildCount() == 0) {
                    Log.e(TAG, "Data node has no children or it is null at index " + i);
                    continue; // Skip to the next iteration if the data node is null
                }
                dataNode = dataNode.getChild(0); // Get the first child
                StringBuilder commentBuilder = new StringBuilder(); // Use StringBuilder for efficiency

                for (int j = 0; j < dataNode.getChildCount(); j++) {
                    AccessibilityNodeInfo textNode = dataNode.getChild(j);
                    if (textNode == null) {
                        Log.e(TAG, "Text node is null at index " + j + " for comment index " + i);
                        continue; // Skip to the next iteration if the text node is null
                    }

                    CharSequence charPara = textNode.getText();
                    if (charPara == null || charPara.length() <= 0) {
                        Log.e(TAG, "Text is empty or null at index " + j + " for comment index " + i);
                        continue; // Skip to the next iteration if text is empty or null
                    }

                    commentBuilder.append(charPara); // Append the non-empty text
                }
                mainComment = commentBuilder.toString();
//                commentListBuilder.append(main)
                commentToReplyNode = selectedCommentNode;

                Log.d(TAG, "Got main Comment: "+ mainComment);
                for(int j = i+1; j<commentsList.getChildCount(); j++){
                    AccessibilityNodeInfo replyNode = commentsList.getChild(j);
                    if (replyNode == null) continue; // Skip to the next iteration if the child node is null

                    String replyDescription = replyNode.getContentDescription().toString();
                    if (replyDescription == null || replyDescription.length() <= 0) continue;
                    if (replyDescription.contains("Level 2 ")){

                        AccessibilityNodeInfo replyDataNode = HelperFunctions.findNodeByResourceId(replyNode, "com.reddit.frontpage:id/comment_richtext");
                        if (replyDataNode == null || replyDataNode.getChildCount() == 0) {
                            Log.e(TAG, "Data node has no children or it is null at index " + i);
                            continue; // Skip to the next iteration if the data node is null
                        }
                        replyDataNode = replyDataNode.getChild(0); // Get the first child
                        StringBuilder replyCommentBuilder = new StringBuilder(); // Use StringBuilder for efficiency

                        for (int k = 0; k < replyDataNode.getChildCount(); k++) {
                            AccessibilityNodeInfo textNode = replyDataNode.getChild(k);
                            if (textNode == null) {
                                Log.e(TAG, "Text node is null at index " + k + " for comment index " + i);
                                continue; // Skip to the next iteration if the text node is null
                            }

                            CharSequence charPara = textNode.getText();
                            if (charPara == null || charPara.length() <= 0) {
                                Log.e(TAG, "Text is empty or null at index " + j + " for comment index " + i);
                                continue; // Skip to the next iteration if text is empty or null
                            }

                            replyCommentBuilder.append(charPara); // Append the non-empty text
                        }

                        level2Comment = replyCommentBuilder.toString();
                        Log.d(TAG, "Got main Comment reply: "+ level2Comment);
                        break;
                    }else break;
                }

                if(level2Comment != null && mainComment != null && commentToReplyNode != null){
                    break;
                }
            }
        }

        if(level2Comment != null && mainComment != null && commentToReplyNode != null){
            noOfCommentScroll = 0;
            callback.execute(commentingType);
            return;
        }
        if (noOfCommentScrollForReply > 0) {
            --noOfCommentScrollForReply;
            performClickCommentReadCapButton(callback, commentingType, level, this::getCommentDataToReply);
            return;
        }
//        else if (level2Comment != null && mainComment != null && commentToReplyNode != null) {
//            noOfCommentScroll = 0;
//            commentQuery = commentListBuilder.toString();
//            callback.execute(commentingType);
//            return;
//        }
        Log.d(TAG, "no comments found on level 1 with replies");
        noOfCommentScroll = 0;
        handler.postDelayed(()->{
            ExitCommentsAndscrollAndContinue();
        },1000+random.nextInt(2000));
    }
    private AccessibilityNodeInfo getLatestNode(String id) {
        // Get the current AccessibilityService instance
        Log.i(TAG,"id: "+id);
        MyAccessibilityService service = (MyAccessibilityService) context;

        // Find the nodes by resource ID directly
        List<AccessibilityNodeInfo> commentsListNodes = service.getRootInActiveWindow()
                .findAccessibilityNodeInfosByViewId(id);

        if (commentsListNodes != null && !commentsListNodes.isEmpty()) {
            // Assuming there is at least one node with this ID, use the first one
            AccessibilityNodeInfo commentsListNode = commentsListNodes.get(0);

            Log.d(TAG, "Successfully found the comments list node.");
            // Now you can work with commentsListNode, for example, get its children
            return commentsListNode;
        } else {
            Log.e(TAG, "Could not find the comments list node.");
            return null;
        }
    }

    public AccessibilityNodeInfo findLatestNodeForFeedscreenPager() {
        String resourceId = "com.reddit.frontpage:id/fragment_pager";
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
    public AccessibilityNodeInfo getNodeInActivePageByDirectId(String id) {
        MyAccessibilityService service = (MyAccessibilityService) context;
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();

        if (rootNode == null) {
            Log.e(TAG, "Root node is null");
            return null;
        }

        // Find nodes with the specified resource ID
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId(id);

        if (nodes != null && !nodes.isEmpty()) {
            // Return the first node found with the matching resource ID
            AccessibilityNodeInfo latestNode = nodes.get(0);
            Log.d(TAG, "Successfully found node with resource ID: " + id);
            return latestNode;
        } else {
            Log.e(TAG, "No node found with resource ID: " + id);
            return null;
        }
    }
    public AccessibilityNodeInfo getNodeInActivePage(String id) {
        MyAccessibilityService service = (MyAccessibilityService) context;
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();

        if (rootNode == null) {
            Log.e(TAG, "Root node is null");
            return null;
        }

        // Find nodes with the specified resource ID
        AccessibilityNodeInfo nodes = HelperFunctions.findNodeByResourceId(rootNode, id);

        if (nodes != null) {
            // Return the first node found with the matching resource ID
            Log.d(TAG, "Successfully found node with resource ID: " + id);
            rootNode.recycle();
            return nodes;
        } else {
            Log.e(TAG, "No node found with resource ID: " + id);
            return null;
        }
    }
    private void exitCommentsAndContinue() {
        isprocessingComments = false;
        inCommentsSection = false;
        moveBack();
        handler.postDelayed(this::performNextAction, 1000 + random.nextInt(1000));
    }
    private void ExitCommentsAndscrollAndContinue() {
        isprocessingComments = false;
        inCommentsSection = false;
        moveBack();
        handler.postDelayed(() -> {
            scrollToGetVisibleNode(this::performNextAction, "done");
        }, 1000 + random.nextInt(1000));
    }
    private void performCommentClick(android.graphics.Rect bounds, CommentAction action) {
        Path clickPath = new Path();
        float x = bounds.left + random.nextFloat() * (bounds.right - bounds.left);
        float y = bounds.top + random.nextFloat() * (bounds.bottom - bounds.top);
        clickPath.moveTo(x, y);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, 50 + random.nextInt(50)));

        try {
            MyAccessibilityService service = (MyAccessibilityService) context;
            service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    inCommentsSection = true;
                    Log.d(TAG, "pressed comment button inside comments of post");
                    handler.postDelayed(() -> {
                        action.execute();
                    }, 2000 + random.nextInt(1000));
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    performNextAction();
                }
            }, null);
        } catch (Exception e) {
            performNextAction();
        }
    }
    private void performCommentInputClick(android.graphics.Rect bounds, String response, GetComment action) {
        Path clickPath = new Path();
        float x = bounds.left + random.nextFloat() * (bounds.right - bounds.left);
        float y = bounds.top + random.nextFloat() * (bounds.bottom - bounds.top);
        clickPath.moveTo(x, y);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, 50 + random.nextInt(50)));

        try {
            MyAccessibilityService service = (MyAccessibilityService) context;
            service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    inCommentsSection = true;
                    Log.d(TAG, "pressed comment button inside comments of post");
                    handler.postDelayed(() -> {
                        action.execute(response);
                    }, 2000 + random.nextInt(1000));
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    performNextAction();
                }
            }, null);
        } catch (Exception e) {
            performNextAction();
        }
    }
    private void scrollComments() {
        Path swipePath = new Path();
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        float startY = screenHeight * (0.7f + random.nextFloat() * 0.15f);
        float endY = screenHeight * (0.2f + random.nextFloat() * 0.15f);
        swipePath.moveTo(screenWidth / 2f, startY);
        swipePath.lineTo(screenWidth / 2f, endY);
        if (noOfCommentScrollPerformed < noOfCommentScroll && random.nextInt(100) < COMMENT_SCROLL_CHANCE) {
            Log.d(TAG, "choosed to Scroll in comments");
            ++noOfCommentScrollPerformed;
            handler.postDelayed(() -> {
                performScrollGesture(swipePath, "down_comments");
            }, scrollToken, COMMENT_SCROLL_DELAY + random.nextInt(COMMENT_SCROLL_RANDOM_DELAY));
            return;
        } else {
            Log.d(TAG, "choosed not to Scroll in comments");
            noOfCommentScrollPerformed = 0;
            noOfCommentScroll = 0;
            handler.postDelayed(this::exitCommentsAndContinue, COMMENT_SCROLL_DELAY + random.nextInt(COMMENT_SCROLL_RANDOM_DELAY));
            return;
        }
    }
    //Comments related functions
    private int getBottomNav(AccessibilityNodeInfo rootNode) {
        Log.d(TAG, "enetered to get footer height");
        int footerHeight = 0;
        AccessibilityNodeInfo footerNode = HelperFunctions.findNodeByResourceId(rootNode, "com.reddit.frontpage:id/bottom_nav_compose");
        if (footerNode != null) {
            android.graphics.Rect footerBounds = new android.graphics.Rect();
            footerNode.getBoundsInScreen(footerBounds);
            footerHeight = footerBounds.height();
            footerNode.recycle();
            return footerHeight;
        } else {
            return 0;
        }
    }
    private int getToolbar(AccessibilityNodeInfo rootNode) {
        Log.d(TAG, "enetered to get headerNode height");
        int headerHeight = 0;
        AccessibilityNodeInfo headerNode = HelperFunctions.findNodeByResourceId(rootNode, "main_top_app_bar");
        if (headerNode != null) {
            android.graphics.Rect headerBounds = new android.graphics.Rect();
            headerNode.getBoundsInScreen(headerBounds);
            headerHeight = headerBounds.height();
            headerNode.recycle();
            return headerHeight;
        } else {
            return 0;
        }
    }
//    private AccessibilityNodeInfo getVisibleNode(AccessibilityNodeInfo rootNode, int visibleTop, int visibleBottom, String check) {
private AccessibilityNodeInfo getVisibleNode(AccessibilityNodeInfo rootNode, String check) {
        Log.d(TAG, "Entered to get visible Node!");
//        AccessibilityNodeInfo lazyColumn = HelperFunctions.findNodeByResourceId(rootNode, "feed_lazy_column");
        AccessibilityNodeInfo screenPager = findLatestNodeForFeedLazyColumn();
        AccessibilityNodeInfo lazyColumn = HelperFunctions.findNodeByResourceId(screenPager, "feed_lazy_column");
//        AccessibilityNodeInfo lazyColumn = getLatestNode("feed_lazy_column");
//        MyAccessibilityService service = (MyAccessibilityService) context;
//        AccessibilityNodeInfo lazyColumn = service.getRootInActiveWindow()
//                .findAccessibilityNodeInfosByViewId("feed_lazy_column");
        if (lazyColumn != null && lazyColumn.getChildCount() > 0) {
            Log.d(TAG, "Found Lazy column!");
            int foundChildIndex = -1;

            for (int i = 0; i < lazyColumn.getChildCount(); i++) {
                AccessibilityNodeInfo child = lazyColumn.getChild(i);
                if (child == null) continue;

                android.graphics.Rect bounds = new android.graphics.Rect();
                child.getBoundsInScreen(bounds);

                // Check if child is fully within the effective visible area
//                if (bounds.top >= visibleTop && bounds.bottom <= visibleBottom && child.getChildCount() > 5) {
                if (child.getChildCount() > 5) {
                    foundChildIndex = i;
                    Log.e(TAG, "Child node : " + child);
                    child.recycle();
                    break;
                }
                child.recycle();
            }
            if (foundChildIndex < 0) {
                lazyColumn.recycle();
                return null;
            }

            AccessibilityNodeInfo postNode = lazyColumn.getChild(foundChildIndex);
            lazyColumn.recycle();
            if (postNode == null) {
                return null;
            }
            Log.d(TAG, "Found vivble Node!");
            AccessibilityNodeInfo captionNode = postNode.getChild(0);
            CharSequence caption = (captionNode != null) ? captionNode.getContentDescription() : null;
            captionNode.recycle();
            if (caption == null) {
                Log.e(TAG, "No caption found in post to downvote");
                return null;
            }
            String captionInString = caption.toString();
            Log.d(TAG, "Post Caption: " + caption);
            int upvotes = extractInteger(captionInString, "(\\d+) upvotes");
            int comments = extractInteger(captionInString, "(\\d+) comment");
            Log.e(TAG,"upvotes : "+upvotes);
            Log.e(TAG,"comments : "+comments);
            if (check == "comments") {
                if (comments > 0) {
                    Log.d(TAG, "comments are greater than 0!");
                    noOfComments = comments;
                    noOfCommentScroll = comments / 7;
                    Log.d(TAG, "no of comment scroll: " + noOfCommentScroll);
                    return postNode;
                }
            } else if (check == "upvote") {
                if (upvotes > 20) {
                    return postNode;
                }
            } else if (check == "downvote") {
                if (upvotes < 100) {
                    return postNode;
                }
            } else if (check == "commenting") {
                Log.e(TAG, "comments for commenting: " + comments);
                if (comments >= 25) {
                    Log.d(TAG, "comments are greater than 10!");
                    noOfComments = comments;
                    noOfCommentScroll = comments / 5;
                    noOfCommentScrollForReply = noOfCommentScroll;
                    if (noOfCommentScroll > 10) {
                        noOfCommentScroll = 10;
                        noOfCommentScrollForReply = noOfCommentScroll;
                    }
                    return postNode;
                }
            }
            postNode.recycle();
            Log.e(TAG, "postNode not matched conditions!");
            return null;

        } else {
            Log.e(TAG, "No lazy column found!");
            return null;
        }
    }
    private void scrollToGetVisibleNode(CommentAction action, String check) {
        Path swipePath = new Path();
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        float startY = screenHeight * (0.65f + random.nextFloat() * 0.15f);
        float endY = screenHeight * (0.15f + random.nextFloat() * 0.15f);
        float xVariation = screenWidth * (0.1f * random.nextFloat() - 0.05f);

        swipePath.moveTo(screenWidth / 2f + xVariation, startY);
        swipePath.lineTo(screenWidth / 2f + xVariation, endY);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        int gestureDuration = 150 + random.nextInt(100);
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, gestureDuration));

        try {
            MyAccessibilityService service = (MyAccessibilityService) context;
            isScrolling = true;
            service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    int delay = 2000 + random.nextInt(2000);
                    handler.removeCallbacksAndMessages(null);
                    if (check == "done") {
                        handler.postDelayed(
                                action::execute
                                , delay);
                    } else if (check == "comments") {
                        handler.postDelayed(() -> {
                            enterComments(action::execute, 3, check);
                        }, delay);
                    } else if (check == "downvote") {
                        handler.postDelayed(action::execute, delay);
                    } else if (check == "commenting") {
                        handler.postDelayed(() -> enterComments(action, 10, check), delay);
                    }else if(check == "upvote"){
                        handler.postDelayed(()->attemptUpvote(15,check), delay);
                    }
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    isScrolling = false;
                    handler.postDelayed(() -> performNextAction(), 1000);
                }
            }, null);
        } catch (Exception e) {
            Log.e(TAG, "Error during scrolling to get vivble node: " + e);
            handler.postDelayed(() -> performNextAction(), nextActionToken, 1000);
        }
    }
    private AccessibilityNodeInfo getRootInActiveWindow() {
        try {
            if (context == null) {
                return null;
            }
            MyAccessibilityService service = (MyAccessibilityService) context;
            return service.getRootInActiveWindow();
        } catch (Exception e) {
            return null;
        }
    }
    private void moveBack() {
        try {
            MyAccessibilityService service = (MyAccessibilityService) context;
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
        } catch (Exception e) {
            Log.e(TAG, "Failed to perform back action", e);
        }
    }
    private void goToHome() {
        try {
            MyAccessibilityService service = (MyAccessibilityService) context;
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
        } catch (Exception e) {
            Log.e(TAG, "Failed to perform back action", e);
        }
    }
    private static int extractInteger(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0; // default if pattern not found
    }
    private void cleanupAndExit() {
        handler.postDelayed(()->{
            goToHome();
            HomeActivity home = new HomeActivity();
            home.sendMessage("Automation Completed",this.Task_id,this.job_id, "final");
            handler.postDelayed(helperFunctions::closeAndOpenMyApp, closeAppToken, 1500 + random.nextInt(2000));
        },1000+ random.nextInt(500));
    }
    private void ScrollUpToGetPostData(CommentAction action) {
        Path swipePath = new Path();
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        float startY = screenHeight * (0.2f + random.nextFloat() * 0.1f);
        float endY = screenHeight * (0.7f + random.nextFloat() * 0.1f);
        float xVariation = screenWidth * (0.1f * random.nextFloat() - 0.05f);

        swipePath.moveTo(screenWidth / 2f + xVariation, startY);
        swipePath.lineTo(screenWidth / 2f + xVariation, endY);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        int gestureDuration = 100 + random.nextInt(100);
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, gestureDuration));

        try {
            MyAccessibilityService service = (MyAccessibilityService) context;
            service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    action.execute();
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    handler.postDelayed(() -> exitCommentsAndContinue(), 1000);
                }
            }, null);
        } catch (Exception e) {
            Log.e(TAG, "Error during scrolling to get post data: " + e);
            handler.postDelayed(() -> exitCommentsAndContinue(), 1000);
        }

    }
    private void scrollToGetVisibleNode(GetComment action, String Check, String Level, CommentActionWithParams returnAction) {
        Path swipePath = new Path();
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        float startY = screenHeight * (0.65f + random.nextFloat() * 0.15f);
        float endY = screenHeight * (0.15f + random.nextFloat() * 0.15f);
        float xVariation = screenWidth * (0.1f * random.nextFloat() - 0.05f);

        swipePath.moveTo(screenWidth / 2f + xVariation, startY);
        swipePath.lineTo(screenWidth / 2f + xVariation, endY);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        int gestureDuration = 350 + random.nextInt(100);
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, gestureDuration));

        try {
            MyAccessibilityService service = (MyAccessibilityService) context;
            isScrolling = true;
            service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    int delay = 2000 + random.nextInt(1000);
                    handler.removeCallbacksAndMessages(null);
                    handler.postDelayed(() -> {
                        returnAction.execute(Check, Level, action);
                    }, delay);
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    isScrolling = false;
                    handler.postDelayed(() -> {
                        returnAction.execute(Check, Level, action);
                    }, 500 + random.nextInt(500));
                }
            }, null);
        } catch (Exception e) {
            Log.e(TAG, "Error during scrolling to get vivble node: " + e);
            handler.postDelayed(() -> {
                returnAction.execute(Check, Level, action);
            }, 500 + random.nextInt(500));
        }
    }
    private void performClickCommentReadCapButton(GetComment action, String Check, String Level, CommentActionWithParams returnAction) {
        AccessibilityNodeInfo capButton = getNodeInActivePage("com.reddit.frontpage:id/speed_read_button_cab");
        if (capButton == null) {
            Log.d(TAG, "comment scroll button not found");
            ExitCommentsAndscrollAndContinue();
            return;
        }
        android.graphics.Rect bounds = new android.graphics.Rect();
        capButton.getBoundsInScreen(bounds); // Get the bounds on the screen

        // Ensure bounds are valid
        if (bounds.isEmpty()) {
            Log.e(TAG, "Bounds are empty. Cannot perform click on capButton.");
            return;
        }

        float x = bounds.left + random.nextFloat() * (bounds.width());
        float y = bounds.top + random.nextFloat() * (bounds.height());

        // Create a path for the click gesture
        Path clickPath = new Path();
        clickPath.moveTo(x, y); // Move to the random click point

        // Build the gesture description
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, 100 + random.nextInt(50))); // Random duration

        try {
            MyAccessibilityService service = (MyAccessibilityService) context;
            isScrolling = true;
            service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    int delay = 2000 + random.nextInt(1000);
                    handler.removeCallbacksAndMessages(null);
                    handler.postDelayed(() -> {
                        returnAction.execute(Check, Level, action);
                    }, delay);
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    isScrolling = false;
                    handler.postDelayed(() -> {
                        returnAction.execute(Check, Level, action);
                    }, 500 + random.nextInt(500));
                }
            }, null);
        } catch (Exception e) {
            Log.e(TAG, "Error during scrolling to get vivble node: " + e);
            handler.postDelayed(() -> {
                returnAction.execute(Check, Level, action);
            }, 500 + random.nextInt(500));
        }

    }

}