package com.example.appilot.automations.InstagramFollowerBot;

import android.content.Context;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.graphics.Rect;
import android.content.Intent;
import android.os.Bundle;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.example.appilot.automations.Interfaces.Action;
import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;

import com.example.appilot.automations.WarmUpFunctions.Instagram.InstagramWarmUpFunctions;
import com.example.appilot.services.MyAccessibilityService;
import com.example.appilot.utils.HelperFunctions;

import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class InstagramFollowerBotAutomation {

    private static final String TAG = "InstagramFollowerBotAutomation";

    private static final int BASE_DELAY = 1000;
    private static final int RANDOM_DELAY = 1000;
    private static final String FOLLOW_BUTTON_ID = "com.instagram.android:id/profile_header_follow_button";
    private static final String CAROUSEL_VIEW_ID = "com.instagram.android:id/similar_accounts_carousel_view";
    private static final String DISMISS_BUTTON_ID = "com.instagram.android:id/dismiss_button";
    private static final String CARD_CONTAINER_ID = "com.instagram.android:id/suggested_entity_card_container";
    private static final String CARD_CONTAINER_username = "com.instagram.android:id/suggested_entity_card_name";
    private static final String Dialog_Id = "com.instagram.android:id/dialog_container";
    private String List_Id;
    public String Username_Id;
    public String Container_id;
    public String Follow_Button_Id;
    private static final String Instagram_Package = "com.instagram.android";
    private final Context context;
    private final MyAccessibilityService service;
    private final Handler handler;
    private final Random random;
    private SharedPreferences sharedPreferences = null;
    private String Task_id = null;
    private PopUpHandler popUpHandler;
    private String job_id = null;
    private HelperFunctions helperFunctions;
    private AccountManager accountManager;
    private int tracker = 0;
    private static final int MAX_BIO_REJECTIONS = 3;
    private final int MAX_profileTab_found_rejections = 2;
    private int MAX_profileTab_found_try = 0;
    private int bioRejectionCounter;
    private String lastChildname = null;
    //    private List<String> DoneUsers = new ArrayList<>();
    private Stack<String> lastProfileNodes = new Stack<>();
    private int retryCount = 0;
    private final int MAX_RETRIES = 10;
    //    private List<String> DoneAccounts = new ArrayList<>();
    private String Username = null;
    private int userListEmptyScrollUpCount = 0;
    private Boolean UserListFound = false;
    private static final int MAX_RECURSION_DEPTH = 30;
    private final String dialogId = "com.instagram.android:id/dialog_container";
    private final String blockActionDialogTitle = "Try again later";
    public String FollowRequests = "-";
    private String noOfFollowers = "";
    private String noOfFollowings = "";
    //    public Boolean shouldContinue = true;
    private Set<String> viewedUsers = new HashSet<String>();
    private String startTime;
    private String endTime;
    private static final int MIN_STORIES_TO_VIEW = 1;
    private StringBuilder returnMessageBuilder = new StringBuilder();
    private StringBuilder updateMessageBuilder = new StringBuilder();
    private String thisRunStartTime;
    private String thisRunEndTime;
    private Boolean isStart = true;
    private int unfollowingListFindAttempts = 0;
    private Boolean isContainerWithMessageButton = false;
    private String ChatData = "-";
    private String NotificationData = "-";
    private String usernameToUnfollowFrom;
    private Boolean isCheckedThroughFollowersList = false;
    private Boolean isCheckedThroughProfile = false;
    public Boolean isRequestAccepted = true;
    private boolean istriedUnFollowFromProfile = false;
    private boolean istriedUnFollowFromList = false;
    private String mutualFriendsString = null;
    private Method1 method1;
    private Method2 method2;
    private Method3 method3;
    private Method5 method5;
    private Method6 method6;
    private Method7 method7;
    private Method8 method8;
    private InstagramWarmUpFunctions instagramWarmUpFunctions;

    public boolean shouldStop = false;

    public InstagramFollowerBotAutomation(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs) {
        this.context = service;
        this.service = service;
        this.Task_id = taskid;
        this.job_id = jobid;
        this.accountManager = new AccountManager(AccountInputs);
        this.helperFunctions = new HelperFunctions(context, Task_id, job_id);
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();
        this.popUpHandler = new PopUpHandler(this.service, this.handler, this.random, this.helperFunctions);
        this.tracker = 0;
        this.bioRejectionCounter = 0;
        this.sharedPreferences = this.context.getSharedPreferences("InstaGramFollowersBotPrefs", this.context.MODE_PRIVATE);
        this.method1 = new Method1(this, this.service,this.context, this.helperFunctions, this.handler, this.random, this.popUpHandler);
        this.method2 = new Method2(this, this.service,this.context, this.helperFunctions, this.handler, this.random, this.accountManager,this.popUpHandler);
        this.method3 = new Method3(this, this.helperFunctions, this.handler, this.random, this.accountManager,this.popUpHandler);
        this.method5 = new Method5(this, this.helperFunctions, this.handler, this.random, this.accountManager,this.popUpHandler);
        this.method6 = new Method6(this, this.helperFunctions, this.handler, this.random, this.accountManager,this.popUpHandler);
        this.method7 = new Method7(this, this.helperFunctions, this.handler, this.random, this.accountManager,this.popUpHandler);
        this.method8 = new Method8(this, this.helperFunctions, this.handler, this.random, this.accountManager,this.popUpHandler);
        this.instagramWarmUpFunctions = new InstagramWarmUpFunctions(this.service, this.handler, this.random, this.helperFunctions, this.popUpHandler);
    }

    public void checkToperformWarmUpAndThenStartAutomation() {
        Log.e(TAG, "Automation Started");
        Log.i(TAG, "Entered checkToperformWarmUpAndThenStartAutomation");
        if (shouldContinueAutomation()) {
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        this.startTime = dateFormat.format(new Date());
        SimpleDateFormat startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        this.thisRunStartTime = startTime.format(new Date());

        returnMessageBuilder.append("Start Time:  ").append(this.startTime).append("\n");

        int check = random.nextInt(100);
        if (check < 50) {
            int whichWarmUpFunction = random.nextInt(100);

            if (whichWarmUpFunction < 10) {
                helperFunctions.sendUpdateMessage(
                        "Going to enter DM to perform Warmup",
                        "update"
                );
                this.launchApp(() -> {
                    this.instagramWarmUpFunctions.enterDM(()->getAccountsData(this::startAutomation));
                });
            } else {
                long warmUpTime = 60000+ random.nextInt(180000);
                helperFunctions.sendUpdateMessage(
                        "Going to perform Scroll warmup for " +
                                (warmUpTime / (1000.0 * 60)) +
                                " minutes.",
                        "update"
                );
                this.launchApp(() -> performTimedScrollUp(warmUpTime, ()->getAccountsData(this::startAutomation)));
            }
        } else {
            this.launchApp(()->getAccountsData(this::startAutomation));
        }
    }

    public void launchApp(Action callback) {
        Log.d(TAG, "Launching app: " + Instagram_Package);
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(Instagram_Package);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);

            // Delay to check if the app launched properly
            handler.postDelayed(() -> {
                // Handle popups or execute the callback
                if (popUpHandler.handleOtherPopups(callback,null)) {
                    Log.i(TAG, "After Launching Instagram Found a PopUp handling it through Gesture");
                    return;
                }
                callback.execute();
            }, 5000 + random.nextInt(5000));
        } else {
            Log.e(TAG, "Could not launch app: " + Instagram_Package);
            // Fallback to launchInstagramExplicitly if no launch intent is found
            launchInstagramExplicitly(callback);
        }
    }

    private void launchInstagramExplicitly(Action callback) {
        Log.d(TAG, "Entered launchInstagramExplicitly.");
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse("https://www.instagram.com/"))
                .setPackage(Instagram_Package)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
            handler.postDelayed(() -> {
                if (popUpHandler.handleOtherPopups(callback, null)) {
                    Log.i(TAG, "After Launching Instagram Found a PopUp handling it through Gesture");
                    return;
                }
                callback.execute();
            }, 5000 + random.nextInt(5000));
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Instagram", e);
        }
    }

    private void launchInstagramPost(Action callback, String postUrl) {
        Log.d(TAG, "Attempting to launch Instagram post: " + postUrl);

        // Create an intent with ACTION_VIEW and the post URL
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse(postUrl))
                .setPackage("com.instagram.android") // Ensure it opens in the Instagram app
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        try {
            // Check if the Instagram app is installed
            context.getPackageManager().getPackageInfo("com.instagram.android", 0);
            context.startActivity(intent);

            // Delay to handle popups or execute the callback
            handler.postDelayed(() -> {
                if (popUpHandler.handleOtherPopups(callback, null)) {
                    Log.i(TAG, "Handled popup after launching Instagram post.");
                    return;
                }
                callback.execute();
            }, 5000 + random.nextInt(5000)); // Adjust delay as needed

        } catch (PackageManager.NameNotFoundException e) {
            // Fallback to opening the URL in a browser if Instagram app is not installed
            Log.e(TAG, "Instagram app not installed. Falling back to browser.");
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(postUrl))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(browserIntent);
            callback.execute();
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Instagram post.", e);
        }
    }

    private void getAccountsData(Action Callback) {
        Log.i(TAG,"Entered getAccountsData");
        List<AccessibilityNodeInfo> allProfileNodes = null;
        if (shouldContinueAutomation()) {
            return;
        }

        if (popUpHandler.handleOtherPopups(()->this.getAccountsData(Callback), null)) return;

        AccessibilityNodeInfo profileTab = null;

        try {

            // Step 1: Find the profile tab
            profileTab = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/profile_tab", 20);
            if (profileTab == null) {
                Log.e(TAG, "Profile tab not found.");
                helperFunctions.cleanupAndExit("Please make sure your Accessibility Service is Enabled on device, (Profile Tab Button Did Not Found)", "error");
                return;
            }

            // Step 2: Perform a long click on the profile tab
            boolean isClicked = profileTab.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
            if (!isClicked) {
                Log.e(TAG, "Failed to perform long click on profile tab.");
                helperFunctions.cleanupAndExit("Failed to interact with profile tab.", "error");
                return;
            }

            // Step 3: Delay and retrieve account nodes
            handler.postDelayed(() -> {
                try {
                    // Create a local copy of the list to ensure it's effectively final
                    AccessibilityNodeInfo rootNode = helperFunctions.getRootInActiveWindow();
                    if(rootNode != null){
                        helperFunctions.saveNodeTreeToDownloads( rootNode, "accounts_node_hierarchy.txt");
                    }
                    List<AccessibilityNodeInfo> localAllProfileNodes = helperFunctions.FindNodesByClassAndIndexUntilText("Add Instagram account", 1);
                    if (localAllProfileNodes == null || localAllProfileNodes.isEmpty()) {
                        Log.e(TAG, "No Instagram accounts found.");
                        helperFunctions.cleanupAndExit("No Instagram Accounts found on device", "error");
                        return;
                    }

                    // Initialize accounts
                    accountManager.initializeAccounts(localAllProfileNodes);

                    if(!accountManager.getIsAnyAccount()){
                        Log.e(TAG,"Please provide correct account Usernames");
                        helperFunctions.cleanupAndExit("Please provide correct account Usernames", "error");
                        return;
                    }

                    // Step 4: Check if the first account is excluded
                    AccessibilityNodeInfo firstNode = localAllProfileNodes.get(0);
                    CharSequence username = firstNode.getText();
                    firstNode.recycle(); // Recycle the first node

                    if (username != null && !accountManager.isAccountToAutomate(username.toString())) {

                        for (int i = 1; i < localAllProfileNodes.size(); i++) {
                            AccessibilityNodeInfo nextUsernameNode = localAllProfileNodes.get(i);
                            CharSequence userName = nextUsernameNode.getText();

                            if (userName != null && accountManager.isAccountToAutomate(userName.toString())) {
                                if (nextUsernameNode.isClickable() && nextUsernameNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                                    handler.postDelayed(() -> {
                                        Log.i(TAG, "Changed the Account");
                                        Callback.execute();
                                    }, 3000 + random.nextInt(3000));
                                } else {
                                    getBoundsAndClick(nextUsernameNode, Callback, "Center", 3000, 6000);
                                }
                                return;
                            }
                        }

                        // If no valid account is found
                        Log.e(TAG, "All accounts are excluded.");
                        helperFunctions.cleanupAndExit("All of the active accounts are excluded, no account available to automate", "error");
                    } else {
                        // Navigate back if the first account is valid
                        handler.postDelayed(() -> {
                            helperFunctions.navigateBack();
                            handler.postDelayed(Callback::execute, 300 + random.nextInt(200));
                        }, 300 + random.nextInt(200));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in getAccountsData: " + e.getMessage());
                    helperFunctions.cleanupAndExit("An unexpected error occurred while retrieving account data.", "error");
                }
            }, 1500 + random.nextInt(500));

        } finally {
            // Cleanup resources
            if (profileTab != null) {
                profileTab.recycle();
            }
            if (allProfileNodes != null) {
                for (AccessibilityNodeInfo node : allProfileNodes) {
                    if (node != null) {
                        node.recycle();
                    }
                }
            }
        }
    }

    public void startAutomation() {
        Log.i(TAG, "Entered startAutomation");
        if (shouldContinueAutomation()) {
            return;
        }

        if (popUpHandler.handleOtherPopups(()->this.startAutomation(), null)) return;

        AccessibilityNodeInfo InboxTab = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/action_bar_inbox_button", 10);
        if (InboxTab != null && InboxTab.getContentDescription() != null) {
            this.ChatData = InboxTab.getContentDescription().toString();
        }
        setNodeIds();
        if ("NotificationSuggestion".equals(accountManager.getCurrentAccountAutomationType())) {
            this.enterNotificationSection(() -> {
                getfollowRequestsCount(() -> {
                    this.method1.recursivefindButtonandClick("activity_feed_see_all_row", 0, 20, this::startFollowing);
                });
            });
        } else if ("ProfileSuggestion".equals(accountManager.getCurrentAccountAutomationType())) {
            this.enterNotificationSection(() -> {
                getfollowRequestsCount(() -> OpenSearchFeed(()->{this.ClickAndOpenSearchBar(this.method2::startFollowersAutomation);}));
            });
        } else if ("ProfileLikersFollow".equals(accountManager.getCurrentAccountAutomationType())) {
            this.enterNotificationSection(() -> {
                getfollowRequestsCount(() -> {
                    closeMyApp();
                    handler.postDelayed(()->{
                        launchInstagramPost(
                                this.method3::StartLikesFollowing, accountManager.getCurrentAccountUrl());
                    }, 30000+random.nextInt(20000));

                });
            });
        } else if ("unFollow".equals(accountManager.getCurrentAccountAutomationType())) {
            Log.e(TAG, "Method 4 is not Currently Available, Its In Development phases");
            this.enterNotificationSection(() -> {
                enterProfile(this::startUnFollowingAutomation);
            });
        } else if ("FollowAllRequests".equals(accountManager.getCurrentAccountAutomationType())) {
            this.enterNotificationSection(() -> {
                getfollowRequestsCount(() -> {
                    enterProfile(this.method5::AccesptAllRequests);
                });
            });
        } else if ("FollowProfilePostsLikers".equals(accountManager.getCurrentAccountAutomationType())) {
            this.enterNotificationSection(() -> {
                getfollowRequestsCount(() -> OpenSearchFeed(()->{this.ClickAndOpenSearchBar(this.method6::startLikersAutomation);}));
            });
        } else if ("PublicToPrivate".equals(accountManager.getCurrentAccountAutomationType())) {
            this.enterNotificationSection(() -> {
                getfollowRequestsCount(() -> {
                    enterProfile(this.method7::switchPublicToPrivate);
                });
            });
        } else if ("PrivateToPublic".equals(accountManager.getCurrentAccountAutomationType())) {
            this.enterNotificationSection(() -> {
                getfollowRequestsCount(() -> {
                    enterProfile(this.method8::switchPrivateToPublic);
                });
            });
        }
    }

    private void setNodeIds(){
        if ("NotificationSuggestion".equals(accountManager.getCurrentAccountAutomationType())) {
            List_Id = "com.instagram.android:id/recycler_view";
            Username_Id = "com.instagram.android:id/row_recommended_user_username";
            Follow_Button_Id = "com.instagram.android:id/row_recommended_user_follow_button";
            Container_id = "com.instagram.android:id/recommended_user_row_content_identifier";
        } else if ("ProfileSuggestion".equals(accountManager.getCurrentAccountAutomationType())) {
            List_Id = "android:id/list";
            Username_Id = "com.instagram.android:id/follow_list_username";
            Follow_Button_Id = "com.instagram.android:id/follow_list_row_large_follow_button";
            Container_id = "com.instagram.android:id/follow_list_container";
        } else if ("ProfileLikersFollow".equals(accountManager.getCurrentAccountAutomationType()) || "FollowProfilePostsLikers".equals(accountManager.getCurrentAccountAutomationType())) {
            List_Id = "android:id/list";
            Username_Id = "com.instagram.android:id/row_user_primary_name";
            Follow_Button_Id = "com.instagram.android:id/row_follow_button";
            Container_id = "com.instagram.android:id/row_user_container_base";
        } else if ("unFollow".equals(accountManager.getCurrentAccountAutomationType())) {
            Log.e(TAG, "Method 4 is not Currently Available, Its In Development phases");
            this.List_Id = "android:id/list";
            this.Username_Id = "com.instagram.android:id/follow_list_username";
            this.Follow_Button_Id = "com.instagram.android:id/follow_list_row_large_follow_button";
            this.Container_id = "com.instagram.android:id/follow_list_container";
        }
    }

    private void getfollowRequestsCount(Action Callback) {
        Log.i(TAG, "Entered getfollowRequestsCount");

        // Check if automation should continue
        if (shouldContinueAutomation()) {
            return;
        }

        if (popUpHandler.handleOtherPopups(()->this.getfollowRequestsCount(Callback), null)) return;

        AccessibilityNodeInfo rootNode = null;
        AccessibilityNodeInfo followRequestsNode = null;
        AccessibilityNodeInfo parentNode = null;
        AccessibilityNodeInfo requestsCountNode = null;

        try {
            // Get the root node in the active window
            rootNode = helperFunctions.getRootInActiveWindow();
            if (rootNode == null) {
                Log.e(TAG, "Root node is null");
//                this.FollowRequests = "-";
//                Callback.execute();
                helperFunctions.cleanupAndExit("Please make sure Accessibility service is enabled","error");
                return;
            }

            // Find the "Follow requests" node
            followRequestsNode = helperFunctions.findNodeByClassAndText(rootNode, "android.widget.TextView", "Follow requests");
            if (followRequestsNode == null) {
                Log.e(TAG, "No Follow Requests");
                this.FollowRequests = "-";
                Callback.execute();
                return;
            }

            // Get the parent node of the "Follow requests" node
            parentNode = followRequestsNode.getParent();
            if (parentNode == null) {
                Log.e(TAG, "Parent node is null");
                this.FollowRequests = "-";
                Callback.execute();
                return;
            }

            // Get the last child of the parent node (requests count)
            int childCount = parentNode.getChildCount();
            if (childCount > 0) {
                requestsCountNode = parentNode.getChild(childCount - 1);
                if (requestsCountNode != null && requestsCountNode.getText() != null) {
                    String requestsCountText = requestsCountNode.getText().toString();
                    if (!"Approve or ignore requests".equals(requestsCountText)) {
                        this.FollowRequests = requestsCountText;
                        Log.i(TAG, "Requests: " + this.FollowRequests);
                    } else {
                        this.FollowRequests = "-";
                    }
                } else {
                    this.FollowRequests = "-";
                }
            } else {
                this.FollowRequests = "-";
            }

            // Execute the callback
            Callback.execute();

        } catch (Exception e) {
            // Log any unexpected exceptions
            Log.e(TAG, "Exception occurred in getfollowRequestsCount: " + e.getMessage(), e);
            this.FollowRequests = "-";
            Callback.execute();
        } finally {
            // Recycle all AccessibilityNodeInfo objects to prevent memory leaks
            if (requestsCountNode != null) {
                requestsCountNode.recycle();
            }
            if (parentNode != null) {
                parentNode.recycle();
            }
            if (followRequestsNode != null) {
                followRequestsNode.recycle();
            }
            if (rootNode != null) {
                rootNode.recycle();
            }
        }
    }

    public void ChangeAccount(Action Callback) {
        try {
            Log.i(TAG, "Entered ChangeAccount");
            if (shouldContinueAutomation()) return;
            this.tracker = 0;
            // Check accountManager validity
            if (accountManager == null) {
                Log.e(TAG, "accountManager is null");
                if (helperFunctions != null) {
                    helperFunctions.cleanupAndExit("AccountManager instance is null", "final");
                } else {
                    Log.e(TAG, "helperFunctions is also null, cannot perform cleanup");
                    return;
                }
                return;
            }

            // Check if any accounts are non-blocked
            if (!accountManager.checkIsAnyAccountNonBlocked()) {
                Log.i(TAG, "All Accounts are Done");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                this.endTime = dateFormat.format(new Date());

                returnMessageBuilder.append("End Time:  ").append(this.endTime).append("\n");
                if (helperFunctions != null) {
                    helperFunctions.cleanupAndExit(this.returnMessageBuilder.toString(), "final");
                } else {
                    Log.e(TAG, "helperFunctions is null, cannot perform cleanup");
                }
                return;
            }

            // Handle task update
            if (!accountManager.getIsTaskUpdated()) {
                handleTaskUpdate();
            }

            accountManager.setIsTaskUpdated(false);

            // Check if next account timer is done
            if (!accountManager.checkIsNextAccountTimerDone()) {
                Log.e(TAG, "Next Account Timer is Not Done");
                accountManager.setIsTaskUpdated(true);
                sleepOrPerformWarmUpFunction(accountManager.getTimeRemaining() + 1000);
                return;
            }

            // Get next available account
            int oldCurrentIndex = accountManager.getCurrentIndex();
            String nextUsername = accountManager.getNextAvailableUsername();

            if (nextUsername == null) {
                Log.e(TAG, "No available account found for switching");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                this.endTime = dateFormat.format(new Date());

                returnMessageBuilder.append("End Time:  ").append(this.endTime).append("\n");
                if (helperFunctions != null) {
                    helperFunctions.cleanupAndExit(this.returnMessageBuilder.toString(), "final");
                } else {
                    Log.e(TAG, "helperFunctions is null, cannot perform cleanup");
                }
                return;
            }

            Log.i(TAG, "Next available account: " + nextUsername);
            int newCurrentIndex = accountManager.getCurrentIndex();

            // Handle single account case
            if (newCurrentIndex == oldCurrentIndex) {
                handleSingleAccountCase(Callback);
                return;
            }

            setNodeIds();

            // Find and interact with profile tab
            AccessibilityNodeInfo profileTab = null;
            if (helperFunctions != null) {
                profileTab = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/profile_tab", 10);
            } else {
                Log.e(TAG, "helperFunctions is null, cannot find profile tab");
                return;
            }

            if (profileTab == null) {
                accountManager.setIsTaskUpdated(true);
                handleProfileTabNotFound(Callback);
                return;
            }

            this.MAX_profileTab_found_try = 0;

            // Long click on profile tab and handle account switching
            profileTab.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
            handler.postDelayed(() -> {
                try {
                    handleAccountSwitching(nextUsername, Callback);
                } catch (Exception e) {
                    Log.e(TAG, "Exception in handleAccountSwitching: " + e.getMessage());
                    if (helperFunctions != null) {
                        helperFunctions.cleanupAndExit("Exception during account switching: " + e.getMessage(), "final");
                    }
                }
            }, 1500 + random.nextInt(500));
        } catch (Exception e) {
            Log.e(TAG, "Exception in ChangeAccount: " + e.getMessage());
            if (helperFunctions != null) {
                helperFunctions.cleanupAndExit("Exception in ChangeAccount: " + e.getMessage(), "final");
            }
        }
    }

    private void handleTaskUpdate() {
        if (shouldContinueAutomation()) {
            return;
        }
        try {
            accountManager.setIsTaskUpdated(true);
            SimpleDateFormat tempDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            this.thisRunEndTime = tempDateFormat.format(new Date());

            this.updateMessageBuilder.append("Start Time: ").append(this.thisRunStartTime)
                    .append("\nEnd Time: ").append(this.thisRunEndTime)
                    .append("\nAutomation Type:  ").append(this.getAutmationMethod())
                    .append("\nAccount Username:  ").append(accountManager.getCurrentUsername())
                    .append("\nAccount Actions Blocked: ").append(accountManager.getAccountLimitHit());

            if ("Method 4".equals(this.getAutmationMethod())) {
                this.updateMessageBuilder.append("\nThis Run Un-Follows made: ").append(accountManager.getThisRunFollows())
                        .append("\nTotal Un-Follows Made Till Now: ").append(accountManager.getFollowsDone());
            } else if ("Method 5".equals(this.getAutmationMethod())) {
                this.updateMessageBuilder.append("\nAccount Privacy Status: ").append(accountManager.getAccountStatus());
            } else {
                this.updateMessageBuilder.append("\nThis Run Follows made: ").append(accountManager.getThisRunFollows())
                        .append("\nThis Run Follow Requests made: ").append(accountManager.getThisRunFollowRequest())
                        .append("\nTotal Follows Made Till Now: ").append(accountManager.getFollowsDone())
                        .append("\nTotal Follow Requests Made Till Now: ").append(accountManager.getRequestsMade());
            }

            this.updateMessageBuilder
                    .append("\nNo. of Follow Requests:  ").append(this.FollowRequests)
                    .append("\nChats Notifications:  ").append(this.ChatData);

            if (helperFunctions != null) {
                helperFunctions.sendUpdateMessage(updateMessageBuilder.toString(), "update");
            } else {
                Log.e(TAG, "helperFunctions is null, cannot send update message");
            }
            this.updateMessageBuilder.setLength(0);
        } catch (Exception e) {
            Log.e(TAG, "Exception in handleTaskUpdate: " + e.getMessage());
        }
    }

    private String getAutmationMethod(){
        switch (accountManager.getCurrentAccountAutomationType()){
            case "NotificationSuggestion":
                return "Method 1";
            case "ProfileSuggestion":
                return "Method 2";
            case "ProfileLikersFollow":
                return "Method 3";
            case "unFollow":
                return "Method 4";
            case "FollowAllRequests":
                return "Method 5";
            case "FollowProfilePostsLikers":
                return "Method 6";
            case "PublicToPrivate":
                return "Method 7";
            case "PrivateToPublic":
                return "Method 8";
            default:
                return  "Unknown Method";
        }
    }





    private void handleSingleAccountCase(Action Callback) {
        if (shouldContinueAutomation()) {
            return;
        }
        try {
            Log.i(TAG, "Only One Account Left");
//            if ("ProfileLikersFollow".equals(type) || "ProfileSuggestion".equals(type)) {
            if ("ProfileSuggestion".equals(accountManager.getCurrentAccountAutomationType())) {
                if (helperFunctions == null) {
                    Log.e(TAG, "helperFunctions is null");
                    return;
                }

                AccessibilityNodeInfo FeedButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/feed_tab", 2);
                if (FeedButton != null) {
                    FeedButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                } else {
                    Log.e(TAG, "Search feed button not found");
                    helperFunctions.cleanupAndExit("Feed button not found, Please Make sure Accessibility service is enabled.", "error");
                    return;
                }
            }
            handler.postDelayed(() -> findFeedTabAndStartAutomation(Callback, 0), 300 + random.nextInt(200));
        } catch (Exception e) {
            Log.e(TAG, "Exception in handleSingleAccountCase: " + e.getMessage());
            if (helperFunctions != null) {
                helperFunctions.cleanupAndExit("Exception in single account handling: " + e.getMessage(), "final");
            }
        }
    }

    private void handleProfileTabNotFound(Action Callback) {
        if (shouldContinueAutomation()) {
            return;
        }
        try {
            Log.e(TAG, "profileTab not found, inside ChangeAccount");
            if (MAX_profileTab_found_try < MAX_profileTab_found_rejections) {
                if (helperFunctions != null) {
                    helperFunctions.navigateBack();
                } else {
                    Log.e(TAG, "helperFunctions is null, cannot navigate back");
                    return;
                }
                this.MAX_profileTab_found_try++;
//                handler.postDelayed(() -> ChangeAccount(Callback), 500 + random.nextInt(500));
                handler.postDelayed(() -> ChangeAccount(Callback), 500 + random.nextInt(500));
                return;
            }

            this.MAX_profileTab_found_try = 0;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            this.endTime = dateFormat.format(new Date());

            returnMessageBuilder.append("End Time:  ").append(this.endTime).append("\n");
            if (helperFunctions != null) {
                helperFunctions.cleanupAndExit(this.returnMessageBuilder.toString(), "final");
            } else {
                Log.e(TAG, "helperFunctions is null, cannot perform cleanup");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in handleProfileTabNotFound: " + e.getMessage());
            if (helperFunctions != null) {
                helperFunctions.cleanupAndExit("Exception when handling profile tab not found: " + e.getMessage(), "final");
            }
        }
    }

    private void handleAccountSwitching(String nextUsername, Action Callback) {
        try {
            if (shouldContinueAutomation()) {
                return;
            }
            Log.e(TAG,"Next Username: "+nextUsername);
            Log.i(TAG, "Opened Accounts tab");
            if (helperFunctions == null) {
                Log.e(TAG, "helperFunctions is null");
                return;
            }

            List<AccessibilityNodeInfo> allProfileNodes = helperFunctions.FindNodesByClassAndIndexUntilText("Add Instagram account", 1);
            if (allProfileNodes == null || allProfileNodes.isEmpty()) {
                Log.e(TAG, "No available accounts to switch to");
                helperFunctions.cleanupAndExit("No available accounts to switch to", "final");
                return;
            }

            boolean isClicked = false;
            for (AccessibilityNodeInfo node : allProfileNodes) {
                if (node != null && node.getText() != null) {
                    Log.i(TAG, "Node Text: " + node.getText().toString());

                    if (node.getText().toString().equals(nextUsername) && accountManager.isAccountToAutomate(node.getText().toString())){
                        AccessibilityNodeInfo parentNode = node.getParent();
                        if (parentNode != null && parentNode.isClickable()) {
                            isClicked = parentNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            handler.postDelayed(() -> findFeedTabAndStartAutomation(Callback, 3), 2000 + random.nextInt(1000));
                            break;
                        } else {
                            Log.e(TAG, parentNode == null ? "Parent node is null" : "Parent node is not clickable");
                            helperFunctions.cleanupAndExit("Parent node is null or not clickable", "final");
                            return;
                        }
                    }
                }
            }

            if (!isClicked) {
                helperFunctions.cleanupAndExit("No available accounts to switch to", "final");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in handleAccountSwitching: " + e.getMessage());
            if (helperFunctions != null) {
                helperFunctions.cleanupAndExit("Exception during account switching: " + e.getMessage(), "final");
            }
        }
    }

    public void sleepOrPerformWarmUpFunction(long sleepTime) {
        Log.i(TAG, "Entered sleepOrPerformWarmUpFunction");
        if (shouldContinueAutomation()) {
            return;
        }

        if(this.getAutmationMethod().equals("Method 5")){
            closeAndLaunchInstagram(sleepTime);
            return;
        }

        Log.e(TAG, "Total sleep time: " + (sleepTime / 1000) / 60 + " minutes");

        int scenario = random.nextInt(3);
        if (scenario == 1 || scenario == 2) {
            if ("ProfileSuggestion".equals(accountManager.getCurrentAccountAutomationType())) {
                helperFunctions.navigateBack();
            }
            AccessibilityNodeInfo homeFeed = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/feed_tab", 1);
            homeFeed.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            try {
                Thread.sleep(800 + random.nextInt(200));
            } catch (InterruptedException e) {
                Log.e(TAG, "Sleep interrupted", e);
            }
            homeFeed.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        switch (scenario) {
            case 0:
                Log.i(TAG, "Scenario 0: Entire sleep time - Close and launch");
//                handler.postDelayed(() -> this.ChangeAccount(this::callbackAccordingToType), sleepTime);
                helperFunctions.sendUpdateMessage(
                        "Going to Sleep for " +
                                (sleepTime / (1000.0 * 60)) +
                                " minutes.",
                        "update"
                );
                closeAndLaunchInstagram(sleepTime);
                break;

            case 1:
                Log.i(TAG, "Scenario 1: Split sleep time - Warmup and close");
                long warmupTime = (long) (sleepTime * (0.3 + random.nextDouble() * 0.4));
                long remainingSleepTime = sleepTime - warmupTime;
                helperFunctions.sendUpdateMessage(
                        "Going to perform warmup for " +
                                (warmupTime / (1000.0 * 60)) +
                                " minutes.",
                        "update"
                );

                handler.postDelayed(() -> performTimedScrollUp(warmupTime, () -> {
                    handler.postDelayed(() -> {
                        helperFunctions.sendUpdateMessage(
                                "Going to Sleep for approximately " +
                                        (remainingSleepTime / (1000.0 * 60)) +
                                        " minutes.",
                                "update"
                        );
                        closeMyApp();
                        handler.postDelayed(()->{
                            launchApp(()->ChangeAccount(this::callbackAccordingToType));
                        },remainingSleepTime+25000+ random.nextInt(20000));
                    }, 10000+random.nextInt(10000));
                }), 300 + random.nextInt(100));
                break;

            case 2: // Entire time warmup
                Log.i(TAG, "Scenario 2: Entire time warmup");
                helperFunctions.sendUpdateMessage(
                        "Going to perform warmup for " +
                                (sleepTime / (1000.0 * 60)) +
                                " minutes.",
                        "update"
                );
                handler.postDelayed(() -> performTimedScrollUp(sleepTime, () -> ChangeAccount(this::callbackAccordingToType)), 300 + random.nextInt(100));
                break;
        }
    }

    private void closeAndLaunchInstagram(long sleepTime) {
        if (sleepTime < 60000) {
            sleepTime = 40000 + random.nextInt(30000);
        }
        closeMyApp();
        handler.postDelayed(() -> launchApp(() -> this.ChangeAccount(this::callbackAccordingToType)), sleepTime);
    }

    public void closeMyApp() {
        if (shouldContinueAutomation()) {
            return;
        }

        AccessibilityService service = (MyAccessibilityService) this.context;
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
        handler.postDelayed(() -> {
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
            handler.postDelayed(this::closeAppAndClickCenter, 3000);
        }, 1500 + random.nextInt(1500));
    }

    private void closeAppAndClickCenter() {
        Log.d(TAG, "closeAppAndClickCenter: entered");
        if (shouldContinueAutomation()) {
            return;
        }

        Path swipePath = new Path();
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        swipePath.moveTo(screenWidth / 2f, screenHeight * 0.6f);  // Start lower, at 80% of the screen height
        swipePath.lineTo(screenWidth / 2f, screenHeight * 0.05f); // End near the top, at 5% of the screen height

        // Create the gesture description
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 200, 300)); // Adjust duration to 700ms

        MyAccessibilityService service = (MyAccessibilityService) context;
        service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                // After the swipe is completed, click in the center (if needed)
                handler.postDelayed(helperFunctions::clickInCenter, 2000);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.e(TAG, "Swipe gesture was cancelled.");
            }
        }, null);
    }

    public void findFeedTabAndStartAutomation(Action CallBack, int attempts) {
        Log.i(TAG, "Entered findFeedTabAndStartAutomation");
        if (shouldContinueAutomation()) {
            return;
        }
        AccessibilityNodeInfo profileTab = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/feed_tab", 2);
        if (profileTab != null) {
            if (profileTab.isClickable() && profileTab.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                handler.postDelayed(CallBack::execute, 1500 + random.nextInt(3500));
            } else {
                getBoundsAndClick(profileTab, CallBack, "Center", 1500, 3500);
            }
            return;
        }
        if (attempts > 10) {
            helperFunctions.navigateBack();
            findFeedTabAndStartAutomation(CallBack, ++attempts);
            return;
        } else {
            helperFunctions.cleanupAndExit("Automation Interupted, Could not find Home Feed Button", "error");
        }
    }

    private void performWarmUp(Action afterWarmUpCallback) {
        if (shouldContinueAutomation()) {
            return;
        }
        int warmUpType = random.nextInt(100); // Generate a random number for warm-up decision

        if (warmUpType < 50) {
            Log.i(TAG, "Performing timed scroll warm-up");
            performTimedScrollUp(60000, afterWarmUpCallback::execute);
        } else {
            Log.i(TAG, "Performing enter DM warm-up");
            this.instagramWarmUpFunctions.enterDM(afterWarmUpCallback);
        }
    }




    private void enterNotificationSection(Action callback) {
        Log.i(TAG, "Entered startNotificationSuggestion");
        if (shouldContinueAutomation()) {
            return;
        }

        if (popUpHandler.handleOtherPopups(()->this.enterNotificationSection(callback), null)) return;

        try {
            // Initialize start time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            this.startTime = dateFormat.format(new Date());
            this.isStart = true;

            // Get the root node
            AccessibilityNodeInfo rootNode = helperFunctions.getRootInActiveWindow();
            if (rootNode == null) {
                Log.i(TAG, "Could not find rootNode inside startNotificationSuggestionAutomation");
                recursiveCheckWithScroll(() -> enterNotificationSection(callback), "com.instagram.android:id/feed_tab");
                return;
            }

            try {
                // Find and handle the notification button  helperFunctions.FindAndReturnNodeById(searchBarId, 10);
                AccessibilityNodeInfo notificationButton = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/news_tab");
                if(notificationButton == null){
                    notificationButton = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/notification");
                }
                if (notificationButton != null) {
                    Log.d(TAG, "Going to press notificationButton");
                    boolean isClicked = notificationButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    notificationButton.recycle();

                    if (isClicked) {
                        Log.d(TAG, "Notification button clicked successfully");
                        handler.postDelayed(callback::execute, 3000 + random.nextInt(1000));
                    } else {
                        Log.e(TAG, "Failed to click notification button through Accessibility, attempting gesture click");
                        Rect bounds = new Rect();
                        notificationButton.getBoundsInScreen(bounds);
                        clickOnBounds(bounds, callback, "Center", 3000, 2000);
                    }
                    return;
                }

                // Handle the home button if notification button is not found
                AccessibilityNodeInfo homeButton = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/feed_tab");
                if (homeButton == null) {
                    Log.e(TAG, "Home button not found, exiting automation");
                    handler.postDelayed(() -> helperFunctions.cleanupAndExit(
                            "Could not complete Automation, Home Button Not Found. Please ensure your Accessibility Service is Enabled",
                            "error"
                    ), 3000 + random.nextInt(1000));
                    return;
                }

                if (homeButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    Log.d(TAG, "Home button clicked successfully");
                    handler.postDelayed(() -> enterNotificationSection(callback), 3000 + random.nextInt(1000));
                } else {
                    Log.e(TAG, "Failed to click home button through Accessibility, attempting gesture click");
                    Rect bounds = new Rect();
                    homeButton.getBoundsInScreen(bounds);
                    clickOnBounds(bounds, () -> enterNotificationSection(callback), "Click", 3000, 3000);
                }
            } finally {
                // Ensure the root node is recycled
                if (rootNode != null) {
                    rootNode.recycle();
                }
            }
        } catch (Exception e) {
            // Log any unexpected exceptions
            Log.e(TAG, "An unexpected error occurred in startNotificationSuggestionAutomation: " + e.getMessage(), e);
            handler.postDelayed(() -> helperFunctions.cleanupAndExit(
                    "An unexpected error occurred. Please check logs for details.",
                    "error"
            ), 3000 + random.nextInt(1000));
        }
    }


    // method 2 related functions to open profile and start their respective automation
    public void OpenSearchFeed(Action Callback) {
        Log.i(TAG, "Entered OpenSearchFeed");
        try {
            if (shouldContinueAutomation()) {
                return;
            }

            if (popUpHandler.handleOtherPopups(()->this.OpenSearchFeed(Callback), null)) return;

            // Validate URL and extract username
            try {
                this.Username = helperFunctions.CheckInstagramUrlAndReturnUsername(accountManager.getCurrentAccountUrl());
                if (this.Username == null || this.Username.isEmpty()) {
                    Log.e(TAG, "Invalid Url = " + accountManager.getCurrentAccountUrl());
                    helperFunctions.cleanupAndExit("Please Provide Correct Profile URL, Provided Url is Incorrect: " + accountManager.getCurrentAccountUrl(), "error");
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error extracting username from URL: " + e.getMessage(), e);
                helperFunctions.cleanupAndExit("Failed to process the profile URL. Please try again with a valid URL.", "error");
                return;
            }

            // Find and click on search tab
            AccessibilityNodeInfo newsTabButton = null;
            AccessibilityNodeInfo rootNode = null;
            try {
                newsTabButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/search_tab", 20);
                if (newsTabButton == null) {
                    try {
                        rootNode = helperFunctions.getRootInActiveWindow();
                        if (rootNode == null) {
                            Log.e(TAG, "Root node is null in OpenSearchFeed");
                            helperFunctions.cleanupAndExit("Failed to access Instagram UI. Please restart the app.", "error");
                            return;
                        }

                        newsTabButton = helperFunctions.findNodeByClassAndText(rootNode, "android.widget.FrameLayout", "Search and explore");

                        if (newsTabButton == null) {
                            Log.e(TAG, "News Tab Button Not Found, inside OpenProfile");
                            ChangeAccount(this::callbackAccordingToType);
                            return;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error finding search tab by class and text: " + e.getMessage(), e);
                        helperFunctions.cleanupAndExit("Failed to locate search tab. Please restart the app.", "error");
                        return;
                    } finally {
                        helperFunctions.safelyRecycleNode(rootNode);
                    }
                }

                if (newsTabButton != null) {
                    if(newsTabButton.isClickable() && newsTabButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)){
                        handler.postDelayed(Callback::execute, 1500 + random.nextInt(1500));
                        return;
                    }else{
                        try {
                            Rect bounds = new Rect();
                            newsTabButton.getBoundsInScreen(bounds);
                            helperFunctions.clickOnBounds(bounds, () -> {
                                handler.postDelayed(Callback::execute, 1500 + random.nextInt(1500));
                            }, "Center", 1000, 1000, helperFunctions);
                            return;
                        } catch (Exception e) {
                            Log.e(TAG, "Error performing fallback click on search tab: " + e.getMessage(), e);
                            helperFunctions.cleanupAndExit("Failed to navigate to search tab. Please try again.", "error");
                            return;
                        }
                    }
                } else {
                    Log.e(TAG, "Search tab button is not clickable");
                    helperFunctions.cleanupAndExit("Failed to interact with search tab. Please restart the app.", "error");
                    return;
                }


            } catch (Exception e) {
                Log.e(TAG, "Unexpected error in OpenSearchFeed: " + e.getMessage(), e);
                helperFunctions.cleanupAndExit("An unexpected error occurred while navigating to search. Please try again.", "error");
            } finally {
                if (newsTabButton != null) {
                    try {
                        newsTabButton.recycle();
                    } catch (Exception e) {
                        Log.e(TAG, "Error recycling newsTabButton: " + e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Critical error in OpenSearchFeed: " + e.getMessage(), e);
            helperFunctions.cleanupAndExit("A critical error occurred. Please restart the application.", "error");
        }
    }

    public void ClickAndOpenSearchBar(Action callback) {
        Log.i(TAG, "Entered ClickAndOpenSearchBar");
        try {
            if (shouldContinueAutomation()) {
                return;
            }

            if (popUpHandler.handleOtherPopups(()->this.ClickAndOpenSearchBar(callback), null)) return;

            AccessibilityNodeInfo searchBarButton = null;
            AccessibilityNodeInfo newsTabButton = null;

            try {
                // Try to find search bar
                searchBarButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/action_bar_search_edit_text", 5);

                // If not found, try clicking search tab again
                if (searchBarButton == null) {
                    try {
                        newsTabButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/search_tab", 3);
                        if (newsTabButton == null) {
                            Log.e(TAG, "Could not find search tab to retry");
                            helperFunctions.cleanupAndExit("Failed to locate search interface. Please restart the app.", "error");
                            return;
                        }

                        boolean success = newsTabButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        if (!success) {
                            Log.e(TAG, "Failed to click on search tab to retry");
                            helperFunctions.cleanupAndExit("Failed to navigate to search. Please try again manually.", "error");
                            return;
                        }

                        // Wait for search bar to appear after clicking tab
                        searchBarButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/action_bar_search_edit_text", 30);

                        if (searchBarButton == null) {
                            Log.e(TAG, "Could Not Found Search Bar");
                            accountManager.BlockCurrentAccount();
                            getProfileData(() -> ChangeAccount(this::callbackAccordingToType));
                            return;
                        }
                    } finally {
                        if (newsTabButton != null) {
                            try {
                                newsTabButton.recycle();
                            } catch (Exception e) {
                                Log.e(TAG, "Error recycling newsTabButton: " + e.getMessage(), e);
                            }
                        }
                    }
                }

                // Click on search bar and then type text
                if (searchBarButton != null) {
                    try {
                        Rect bounds = new Rect();
                        searchBarButton.getBoundsInScreen(bounds);

                        // Use clickOnBounds to ensure reliable clicking
                        clickOnBounds(bounds, () -> {
                            try {
                                typeTextWithDelay("com.instagram.android:id/action_bar_search_edit_text",
                                        callback,
                                        () -> {
                                            helperFunctions.cleanupAndExit("Profile provided " + accountManager.getCurrentAccountUrl() + " does not exist on Instagram. Please provide correct Profile.", "error");
                                        },
                                        this.Username,
                                        "com.instagram.android:id/row_search_user_container",
                                        "com.instagram.android:id/row_search_user_username",
                                        true);
                            } catch (Exception e) {
                                Log.e(TAG, "Error in typeTextWithDelay: " + e.getMessage(), e);
                                helperFunctions.cleanupAndExit("Failed to search for profile. Please try again.", "error");
                            }
                        }, "Center", 1000, 2000);
                    } catch (Exception e) {
                        Log.e(TAG, "Error clicking on search bar: " + e.getMessage(), e);
                        helperFunctions.cleanupAndExit("Failed to interact with search bar. Please try again.", "error");
                    }
                } else {
                    Log.e(TAG, "Search bar is null after all attempts");
                    helperFunctions.cleanupAndExit("Failed to locate search bar. Please restart the app.", "error");
                }
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error in ClickAndOpenSearchBar: " + e.getMessage(), e);
                helperFunctions.cleanupAndExit("An unexpected error occurred. Please try again.", "error");
            } finally {
                if (searchBarButton != null) {
                    try {
                        searchBarButton.recycle();
                    } catch (Exception e) {
                        Log.e(TAG, "Error recycling searchBarButton: " + e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Critical error in ClickAndOpenSearchBar: " + e.getMessage(), e);
            helperFunctions.cleanupAndExit("A critical error occurred. Please restart the application.", "error");
        }
    }

    private void typeTextWithDelay(String searchBarId, Action Callback, Action failCallback, String textToType, String ContainerId, String UsernameId, Boolean haveToClick) {
        Log.i(TAG, "Entered typeTextWithDelay for username: " + textToType);
        try {
            if (shouldContinueAutomation()) {
                return;
            }

            if (textToType == null || textToType.isEmpty()) {
                Log.e(TAG, "Text to type is null or empty");
                failCallback.execute();
                return;
            }

            CharSequence currentText = "";
            boolean profileFound = false;
            AccessibilityNodeInfo searchBar = null;

            try {
                // Find search bar with retry
                int retryCount = 0;
                while (searchBar == null && retryCount < 3) {
                    searchBar = helperFunctions.FindAndReturnNodeById(searchBarId, 10);
                    if (searchBar == null) {
                        Log.e(TAG, "Search bar not found, retry: " + retryCount);
                        retryCount++;
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            Log.e(TAG, "Sleep interrupted", ie);
                        }
                    }
                }

                if (searchBar == null) {
                    Log.e(TAG, "Failed to find search bar after retries");
                    failCallback.execute();
                    return;
                }

                // Click on search bar if required
                if (!haveToClick) {
                    boolean clickSuccess = searchBar.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    if (!clickSuccess) {
                        Log.e(TAG, "Failed to click on search bar");
                        Rect bounds = new Rect();
                        searchBar.getBoundsInScreen(bounds);
                        helperFunctions.clickOnBounds(bounds, null, "Center", 500, 500, helperFunctions);
                    }
                }

                // Type text character by character
                for (int i = 0; i < textToType.length() && !profileFound; i++) {
                    char character = textToType.charAt(i);
                    String updatedText = currentText + String.valueOf(character);

                    try {
                        Bundle arguments = new Bundle();
                        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, updatedText);
                        boolean setTextSuccess = searchBar.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);

                        if (!setTextSuccess) {
                            Log.e(TAG, "Failed to set text in search bar");
                            // Try selecting all text first and then setting
                            searchBar.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                            searchBar.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                        }

                        currentText = updatedText;
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting text: " + e.getMessage(), e);
                    }

                    try {
                        Thread.sleep(800 + random.nextInt(200));
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Typing interrupted", e);
                        break;
                    }

                    // Search for profile in results
                    AccessibilityNodeInfo rootNode = null;
                    List<AccessibilityNodeInfo> userContainerNodes = null;

                    try {
                        rootNode = helperFunctions.getRootInActiveWindow();
                        if (rootNode == null) {
                            Log.e(TAG, "Could not find rootNode inside loop of typeTextWithDelay");
                            continue;
                        }

                        userContainerNodes = rootNode.findAccessibilityNodeInfosByViewId(ContainerId);
                        Log.i(TAG, "User Containers found: " + (userContainerNodes != null ? userContainerNodes.size() : 0));

                        if (userContainerNodes != null && !userContainerNodes.isEmpty()) {
                            for (AccessibilityNodeInfo containerNode : userContainerNodes) {
                                if (containerNode == null) continue;

                                List<AccessibilityNodeInfo> usernameNodes = null;
                                AccessibilityNodeInfo usernameNode = null;

                                try {
                                    usernameNodes = containerNode.findAccessibilityNodeInfosByViewId(UsernameId);

                                    if (usernameNodes == null || usernameNodes.isEmpty()) {
                                        continue;
                                    }

                                    usernameNode = usernameNodes.get(0);

                                    if (usernameNode != null && usernameNode.getText() != null) {
                                        String foundUsername = usernameNode.getText().toString();
                                        Log.i(TAG, "Found username: " + foundUsername);

                                        if (foundUsername.equals(textToType)) {
                                            Log.i(TAG, "Profile found: " + foundUsername);
                                            profileFound = true;

                                            if (haveToClick) {
                                                if (containerNode.isClickable()) {
                                                    boolean clickSuccess = containerNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                                    if (clickSuccess) {
                                                        Log.i(TAG, "Found profile and clicked directly");
                                                        handler.postDelayed(Callback::execute, 2500 + random.nextInt(1500));
                                                    } else {
                                                        Log.e(TAG, "Direct click failed, using bounds click");
                                                        getBoundsAndClick(containerNode, Callback, "Center", 2500, 4000);
                                                    }
                                                } else {
                                                    Log.i(TAG, "Container not clickable, using bounds click");
                                                    getBoundsAndClick(containerNode, Callback, "Center", 2500, 4000);
                                                }
                                            } else {
                                                Log.i(TAG, "Found profile, no click needed");
                                                handler.postDelayed(Callback::execute, 2500 + random.nextInt(1500));
                                            }
                                            break;
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error processing username node: " + e.getMessage(), e);
                                } finally {
                                    if (usernameNode != null) {
                                        try {
                                            usernameNode.recycle();
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error recycling username node: " + e.getMessage(), e);
                                        }
                                    }

                                    if (usernameNodes != null) {
                                        for (AccessibilityNodeInfo node : usernameNodes) {
                                            if (node != null) {
                                                try {
                                                    node.recycle();
                                                } catch (Exception e) {
                                                    Log.e(TAG, "Error recycling username node in list: " + e.getMessage(), e);
                                                }
                                            }
                                        }
                                    }

                                    if (containerNode != null) {
                                        try {
                                            containerNode.recycle();
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error recycling container node: " + e.getMessage(), e);
                                        }
                                    }
                                }

                                if (profileFound) break;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error searching for profile: " + e.getMessage(), e);
                    } finally {
                        if (rootNode != null) {
                            try {
                                rootNode.recycle();
                            } catch (Exception e) {
                                Log.e(TAG, "Error recycling root node in search loop: " + e.getMessage(), e);
                            }
                        }

                        if (userContainerNodes != null) {
                            for (AccessibilityNodeInfo node : userContainerNodes) {
                                if (node != null) {
                                    try {
                                        node.recycle();
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error recycling container node in list: " + e.getMessage(), e);
                                    }
                                }
                            }
                        }
                    }

                    if (profileFound) break;
                }

                if (!profileFound) {
                    Log.e(TAG, "Profile not found after typing complete username: " + textToType);
                    failCallback.execute();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in typeTextWithDelay: " + e.getMessage(), e);
                failCallback.execute();
            } finally {
                if (searchBar != null) {
                    try {
                        searchBar.recycle();
                    } catch (Exception e) {
                        Log.e(TAG, "Error recycling search bar: " + e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Critical error in typeTextWithDelay: " + e.getMessage(), e);
            failCallback.execute();
        }
    }






    // method 4
    private void startUnFollowingAutomation() {
        try {
            Log.i(TAG, "Entered startUnFollowingAutomation");

            if (shouldContinueAutomation()) return;
            if (popUpHandler.handleOtherPopups(()->this.startUnFollowingAutomation(), null)) return;

            AccessibilityNodeInfo rootNode = helperFunctions.getRootInActiveWindow();
            if (rootNode == null) {
                Log.e(TAG, "Failed to get the rootNode Inside startUnFollowingAutomation");
                ChangeAccount(this::callbackAccordingToType);
                return;
            }

            AccessibilityNodeInfo followingButton = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/row_profile_header_following_container");
            if (followingButton == null) {
                followingButton = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/profile_header_following_stacked_familiar");
                if (followingButton == null) {
                    Log.e(TAG, "Failed to get the Following button Inside startUnFollowingAutomation");
                    ChangeAccount(this::callbackAccordingToType);
                    return;
                }
            }

            Log.i(TAG, "Found followingButton inside of startUnFollowingAutomation");

            if (followingButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                Log.i(TAG, "Clicked followingButton through Accessibility service inside of startUnFollowingAutomation");
                handler.postDelayed(() -> checkSortingType(this::startUnFollowing), 800 + random.nextInt(800));
            } else {
                Log.i(TAG, "Could not Click followingButton through Accessibility service inside of startUnFollowingAutomation, going to click through bounds");
                getBoundsAndClick(followingButton, () -> checkSortingType(this::startUnFollowing), "Center", 800, 2400);
            }
        } catch (Exception e) {
            // Log the exception for debugging purposes
            Log.e(TAG, "Exception occurred in startUnFollowingAutomation: " + e.getMessage(), e);

            // Call cleanup method with an appropriate message and type
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
        }
    }

    private void checkSortingType(Action callback) {
        try {
            Log.w(TAG, "Entered checkSortingType");

            if (shouldContinueAutomation()) {
                return;
            }

            if (popUpHandler.handleOtherPopups(()->this.checkSortingType(callback), null)) return;

            AccessibilityNodeInfo SortedTextNode = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/sorting_entry_row_option", 40);
            if (SortedTextNode == null) {
                Log.e(TAG, "Failed to get the SortedTextNode Inside checkSortingType");
                ChangeAccount(this::callbackAccordingToType);
                return;
            }

            String SortingText = SortedTextNode.getText().toString();
            if (SortingText.contains(accountManager.getCurrentAccountTypeOfSortForUnfollowing())) {
                Log.i(TAG, "List is already in required sorting");
                // Execute the callback
                callback.execute();
            } else {
                Log.i(TAG, "List is not in required sorting");

                AccessibilityNodeInfo sortingButton = SortedTextNode.getParent();
                if (sortingButton != null) {
                    if (sortingButton.isClickable() && sortingButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        handler.postDelayed(() -> changeSortingType(callback), 800 + random.nextInt(800));
                    } else {
                        getBoundsAndClick(sortingButton, () -> changeSortingType(callback), "Center", 800, 1600);
                    }
                } else {
                    ChangeAccount(this::callbackAccordingToType);
                }
            }
        } catch (Exception e) {
            // Log the exception for debugging purposes
            Log.e(TAG, "Exception occurred in checkSortingType: " + e.getMessage(), e);

            // Call cleanup method with an appropriate message and type
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. "+ e.getMessage(), "error");
        }
    }

    private void changeSortingType(Action callback) {
        try {
            Log.w(TAG, "Entered changeSortingType");

            if (shouldContinueAutomation()) {
                return;
            }

            // Find the sorting options container
            AccessibilityNodeInfo sortingOptionContainer = helperFunctions.FindAndReturnNodeById(
                    "com.instagram.android:id/follow_list_sorting_options_recycler_view", 10);

            // Validate if the container exists and has enough children
            if (sortingOptionContainer == null || sortingOptionContainer.getChildCount() < 3) {
                Log.e(TAG, "Failed to get the SortingOptionContainer inside changeSortingType");
                ChangeAccount(this::callbackAccordingToType);
                return;
            }

            // Determine the correct child node based on the sorting type
            AccessibilityNodeInfo option = null;
            switch (accountManager.getCurrentAccountTypeOfSortForUnfollowing()) {
                case "Default":
                    option = sortingOptionContainer.getChild(0);
                    break;
                case "Earliest":
                    option = sortingOptionContainer.getChild(2);
                    break;
                case "Latest":
                    option = sortingOptionContainer.getChild(1);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid sorting type: " + accountManager.getCurrentAccountTypeOfSortForUnfollowing());
            }

            // Validate the selected option
            if (option == null || !option.isClickable()) {
                Log.e(TAG, "Failed to get the option or it's not clickable inside changeSortingType");
                // Perform back action if needed
                helperFunctions.navigateBack();
                ChangeAccount(this::callbackAccordingToType);
                return;
            }

            // Attempt to click the option using performAction
            if (option.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                handler.postDelayed(callback::execute, 200 + random.nextInt(300));
            } else {
                // Fallback to bounds-based click if performAction fails
                getBoundsAndClick(option, callback, "Center", 400, 700);
            }
        } catch (Exception e) {
            // Log the exception for debugging purposes
            Log.e(TAG, "Exception occurred in changeSortingType: " + e.getMessage(), e);

            // Perform cleanup and exit with an appropriate message
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
        }
    }

    public void saveLastUsername(String accountName, String sortingType, String lastUsername){
        // Construct the composite key
        String key = accountName + "_" + sortingType;

        // Save the last username in SharedPreferences
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putString(key, lastUsername);
        editor.apply();  // Asynchronous save
    }

    public String getLastUsername(String accountName, String sortingType) {
        // Construct the composite key
        String key = accountName + "_" + sortingType;

        // Retrieve the last username from SharedPreferences
        return this.sharedPreferences.getString(key, "");  // Default value is an empty string
    }

    private void startUnFollowing() {
        try {
            Log.i(TAG, "Entered startUnFollowing");

            if (shouldContinueAutomation()) return;

            // Handle action blocker popups
            boolean outerdialogcheck = popUpHandler.checkForActionBlocker(() -> {
                accountManager.BlockCurrentAccount();
                accountManager.setAccountLimitHit(true);
                getProfileData(() -> {
                    ChangeAccount(this::callbackAccordingToType);
                });
            });

            if (outerdialogcheck) {
                Log.e(TAG, "outerdialogcheck in startUnFollowing is true");
                return;
            }

            if (popUpHandler.handleOtherPopups(()->this.startUnFollowing(), null)) return;

            if (this.tracker != 0) {
                Log.e(TAG, "Not on List Page Inside of startUnFollowing, Automation for this Account Corrupted");
                accountManager.BlockCurrentAccount();
                handler.postDelayed(() -> {
                    getProfileData(() -> {
                        ChangeAccount(this::callbackAccordingToType);
                    });
                }, 1200 + random.nextInt(800));
                return;
            }

            // Reset flags
            isCheckedThroughFollowersList = false;
            isCheckedThroughProfile = false;
            istriedUnFollowFromProfile = false;
            istriedUnFollowFromList = false;

            // Get the first node from the following list
            AccessibilityNodeInfo child = null;
            if(accountManager.getCurrentAccountTypeOfSortForUnfollowing().equals("Default")){
                child = getFirstNodeFromFollowingListForDefault();
            }else{
                child = getFirstNodeFromFollowingListForEarliest();
            }
            if (child == null) {
                Log.e(TAG, "Could Not Found Users In following");
                performStaticScrollUp(() -> {
                    CheckNewNodes(this::startUnFollowing);
                });
                return;
            }

            // Check daily and hourly limits
            if (accountManager.checkIsDailyFollowsDone()) {
                Log.e(TAG, "Account Daily Limit Reached");
                accountManager.BlockCurrentAccount();
                handler.postDelayed(() ->
                                getProfileData(() -> {
                                    ChangeAccount(this::callbackAccordingToType);
                                }),
                        400 + random.nextInt(200));
                return;
            }

            if (accountManager.checkIsHourlyFollowsDone()) {
                Log.e(TAG, "Account per hour Limit Reached");
                if (accountManager.isAccountBlocked()) {
                    Log.e(TAG, "Account daily Limit Also Reached");
                    handler.postDelayed(() ->
                                    getProfileData(() -> {
                                        ChangeAccount(this::callbackAccordingToType);
                                    }),
                            400 + random.nextInt(200));
                } else {
                    Log.i(TAG, "Setting Timer for Current Account");
                    accountManager.setSleepTime();
                    ChangeAccount(this::callbackAccordingToType);
                }
                return;
            }

            // Reset attempts counter
            unfollowingListFindAttempts = 0;

            // Random chance to check follow status from chat
            int chances = random.nextInt(100);
            if (chances < 50 && isContainerWithMessageButton) {
                Log.i(TAG, "Going to Check follow or not from Chat");
                isContainerWithMessageButton = false;

                AccessibilityNodeInfo button = HelperFunctions.findNodeByResourceId(child, Follow_Button_Id);
                if (button == null) {
                    HandleProfileDirectCheck(child);
                } else {
                    if (button.isClickable() && button.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        AccessibilityNodeInfo checkNode = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/direct_thread_header",10);
                        if(checkNode != null){
                            this.tracker++;
                            Log.i(TAG, "Tracker: " + this.tracker);
                            handler.postDelayed(this::CheckFollowingFromChat, 2000 + random.nextInt(1000));
                        }else{
                            HandleProfileDirectCheck(child);
                        }
                    } else {
                        getBoundsAndClick(child, () -> {
                            this.tracker++;
                            Log.i(TAG, "Tracker: " + this.tracker);
                            CheckFollowingFromChat();
                        }, "Center", 2000, 3000);
                    }
                }
            } else {
                HandleProfileDirectCheck(child);
            }

        } catch (Exception e) {
            // Log the exception for debugging purposes
            Log.e(TAG, "Exception occurred in startUnFollowing: " + e.getMessage(), e);

            // Perform cleanup and exit with an appropriate message
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
        }
    }

    private AccessibilityNodeInfo getFirstNodeFromFollowingListForDefault() {
        Log.i(TAG, "Entered getFirstNodeFromFollowingListList");

        List<AccessibilityNodeInfo> UsersList = helperFunctions.FindAndReturnNodesById(Container_id, 10);

    //        rootNode.recycle();
        if (UsersList == null || UsersList.isEmpty()) {
            Log.e(TAG, UsersList == null ? "UserList list node is null." : "UserList list does not have enough children.");
            return null;
        }

        updateLastChildName(UsersList);
        Log.e(TAG, "Child count = " + UsersList.size());
        AccessibilityNodeInfo child = null;
        for (int i = 0; i < UsersList.size(); i++) {
            Log.e(TAG, "Child number = " + i);
            child = UsersList.get(i);
            if (child == null) continue;

            AccessibilityNodeInfo followButton = HelperFunctions.findNodeByResourceId(child, Follow_Button_Id);
            if (followButton == null) {
                Log.e(TAG, "could not found Button inside of the container");
                child.recycle();
                child = null;
                continue;
            }
            Log.i(TAG, "found Button inside of the following user container, in getFirstNodeFromFollowingListForDefault");

            CharSequence followButtonText = followButton.getText();
            followButton.recycle();
            isContainerWithMessageButton = false;
            if (followButtonText != null && "Message".equals(followButtonText.toString().trim())) {
                Log.e(TAG, "found button with message, going to change Flag");
                isContainerWithMessageButton = true;
            }

            AccessibilityNodeInfo usernameNode = HelperFunctions.findNodeByResourceId(child, Username_Id);
            if (usernameNode == null) {
                Log.e(TAG, "Username null");
                child.recycle();
                child = null;
                continue;
            }

            CharSequence Username = usernameNode.getText();
            usernameNode.recycle();
            if (Username != null && !accountManager.checkIsUserDone(Username.toString()) && !accountManager.getCurrentAccountUsersToExcludeList().contains(Username.toString())) {
                Log.e(TAG, "Username = " + Username);
                accountManager.addUserDone(Username.toString());
                break;
            }

            child.recycle();
            child = null;
        }
        return child;
    }
    private AccessibilityNodeInfo getFirstNodeFromFollowingListForEarliest() {
        try {
            Log.i(TAG, "Entered getFirstNodeFromFollowingListList");

            List<AccessibilityNodeInfo> UsersList = new ArrayList<>();
            String lastUsername = getLastUsername(accountManager.getCurrentUsername(), accountManager.getCurrentAccountTypeOfSortForUnfollowing());
            boolean foundLastUsername = lastUsername.isEmpty(); // If empty, we don't need to find it
            Log.i(TAG, "Usernames to Exclude: " + accountManager.getCurrentAccountUsersToExcludeList());

            Log.e(TAG, "Last Username done: " + lastUsername);
            Log.v(TAG,"container Id: "+this.Container_id);
            UsersList = helperFunctions.FindAndReturnNodesById(Container_id, 10);

            Log.e(TAG, "Userlist length : " + (UsersList == null ? 0 : UsersList.size()));
            if (UsersList == null || UsersList.isEmpty()) {
                Log.e(TAG, UsersList == null ? "UserList list node is null." : "UserList list does not have enough children.");
                return null;
            }

            Log.e(TAG, "Child count = " + UsersList.size());
            AccessibilityNodeInfo child = null;
            for (int i = 0; i < UsersList.size(); i++) {
                Log.e(TAG, "Child number = " + i);
                child = UsersList.get(i);
                if (child == null) {
                    Log.e(TAG, "Child node is null at index " + i);
                    continue;
                }

                AccessibilityNodeInfo followButton = HelperFunctions.findNodeByResourceId(child, Follow_Button_Id);
                if (followButton == null) {
                    Log.e(TAG, "Could not find Button inside of the container");
                    child.recycle();
                    child = null;
                    continue;
                }

                CharSequence followButtonText = followButton.getText();
                followButton.recycle();
                isContainerWithMessageButton = false;
                if (followButtonText != null && "Message".equals(followButtonText.toString().trim())) {
                    Log.e(TAG, "Found button with message, going to change Flag");
                    isContainerWithMessageButton = true;
                }

                AccessibilityNodeInfo usernameNode = HelperFunctions.findNodeByResourceId(child, Username_Id);
                if (usernameNode == null) {
                    Log.e(TAG, "Username node is null");
                    child.recycle();
                    child = null;
                    continue;
                }

                CharSequence Username = usernameNode.getText();
                usernameNode.recycle();

                // Check if we need to find the last processed username first
                if (!foundLastUsername) {
                    if (!lastUsername.isEmpty() && Username != null && lastUsername.equals(Username.toString())) {
                        // Found the last username, now we'll move to the next one
                        foundLastUsername = true;

                        Log.i(TAG, "Found the last username done: " + lastUsername);
                    }


                    // Update last child name regardless
                    updateLastChildName(UsersList);

                    // Recycle and continue - we don't want to process this node
                    child.recycle();
                    child = null;
                    continue;
//                    accountManager.addUserDone(Username.toString());
//                    return child;
                }

                // We've found the last username or we didn't need to look for one
                // Now process the current username
                if (Username != null && !accountManager.checkIsUserDone(Username.toString()) && !accountManager.getCurrentAccountUsersToExcludeList().contains(Username.toString())) {
                    Log.e(TAG, "Username = " + Username);
                    accountManager.addUserDone(Username.toString());
                    saveLastUsername(accountManager.getCurrentUsername(), accountManager.getCurrentAccountTypeOfSortForUnfollowing(), Username.toString());
                    break;
                }

                saveLastUsername(accountManager.getCurrentUsername(), accountManager.getCurrentAccountTypeOfSortForUnfollowing(), Username.toString());
                updateLastChildName(UsersList);
                child.recycle();
                child = null;
            }
            return child;

        } catch (Exception e) {
            // Log the exception for debugging purposes
            Log.e(TAG, "Exception occurred in getFirstNodeFromFollowingListList: " + e.getMessage(), e);

            // Perform cleanup and exit with an appropriate message
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");

            // Return null to indicate failure
            return null;
        }
    }

    private void updateLastChildName(List<AccessibilityNodeInfo> UsersList) {
        try {
            if (shouldContinueAutomation()) return;

            // Validate UsersList
            if (UsersList == null || UsersList.isEmpty()) {
                Log.e(TAG, "UsersList is null or empty in updateLastChildName");
                return;
            }

            if (UsersList.size() == 1) {
                AccessibilityNodeInfo singleNode = null;
                AccessibilityNodeInfo userNameNode = null;
                try {
                    singleNode = UsersList.get(0);
                    if (singleNode == null) {
                        Log.e(TAG, "SingleNode is null in updateLastChildName");
                        return;
                    }

                    userNameNode = HelperFunctions.findNodeByResourceId(singleNode, Username_Id);
                    if (userNameNode != null && userNameNode.getText() != null) {
                        lastChildname = userNameNode.getText().toString();
                    } else {
                        Log.e(TAG, "Username node or its text is null in updateLastChildName");
                    }
                } finally {
                    // Recycle nodes to avoid memory leaks
                    if (userNameNode != null) userNameNode.recycle();
                    if (singleNode != null) singleNode.recycle();
                }
            } else if (UsersList.size() > 1) {
                AccessibilityNodeInfo lastNode = null;
                AccessibilityNodeInfo userNameNode = null;
                try {
                    lastNode = UsersList.get(UsersList.size() - 2);
                    if (lastNode == null) {
                        Log.e(TAG, "LastNode is null in updateLastChildName");
                        return;
                    }

                    userNameNode = HelperFunctions.findNodeByResourceId(lastNode, Username_Id);
                    if (userNameNode != null && userNameNode.getText() != null) {
                        lastChildname = userNameNode.getText().toString();
                    } else {
                        Log.e(TAG, "Username node or its text is null in updateLastChildName");
                    }
                } finally {
                    // Recycle nodes to avoid memory leaks
                    if (userNameNode != null) userNameNode.recycle();
                    if (lastNode != null) lastNode.recycle();
                }
            }
        } catch (Exception e) {
            // Log the exception for debugging purposes
            Log.e(TAG, "Exception occurred in updateLastChildName: " + e.getMessage(), e);

            // Perform cleanup and exit with an appropriate message
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
        }
    }

    private void HandleProfileDirectCheck(AccessibilityNodeInfo child) {
        try {
            Log.i(TAG, "Entered HandleProfileDirectCheck");

            if (shouldContinueAutomation()) {
                return;
            }

            // Validate child node
            if (child == null) {
                this.tracker++;
                Log.i(TAG, "Tracker: " + this.tracker);
                handler.postDelayed(() -> performWarmUpFunctionOnProfile((this::CheckFollowingOrNoFromProfile)), 2000 + random.nextInt(1000));
            }

            // Attempt to click the child node using performAction
            if (child.isClickable() && child.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                this.tracker++;
                Log.i(TAG, "Tracker: " + this.tracker);
                handler.postDelayed(() -> performWarmUpFunctionOnProfile(this::CheckFollowingOrNoFromProfile), 2000 + random.nextInt(1000));
            } else {
                // Fallback to bounds-based click if performAction fails
                getBoundsAndClick(child, () -> {
                    this.tracker++;
                    Log.i(TAG, "Tracker: " + this.tracker);
                    performWarmUpFunctionOnProfile(this::CheckFollowingOrNoFromProfile);
                }, "Center", 2000, 3000);
            }
        } catch (Exception e) {
            // Log the exception for debugging purposes
            Log.e(TAG, "Exception occurred in HandleProfileDirectCheck: " + e.getMessage(), e);

            // Perform cleanup and exit with an appropriate message
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
        }
    }

    private void CheckFollowingFromChat() {
        try {
            Log.i(TAG, "Entered CheckFollowingFromChat");

            if (shouldContinueAutomation()) {
                return;
            }

            // Get the root node of the active window
            AccessibilityNodeInfo rootNode = helperFunctions.getRootInActiveWindow();
            if (rootNode == null) {
                Log.e(TAG, "Could not found rootNode inside of CheckFollowingFromChat");
                helperFunctions.navigateBack();
                this.tracker--;
                Log.i(TAG, "Tracker: " + this.tracker);
                startUnFollowing();
                return;
            }

            // Handle Default sorting type
            if (accountManager.getCurrentAccountTypeOfSortForUnfollowing().equals("Default")) {
                if (random.nextInt(100) < 50) {
                    this.istriedUnFollowFromList = true;
                    handleNavigationBackWhenNodeNotFound(() -> {
                        handler.postDelayed(this::UnfollowFromList, 300 + random.nextInt(300));
                    });
                } else {
                    enterProfileFromChatAndUnfollow(rootNode, () -> {
                        checkThreadDetails(this::UnfollowFromProfileHome);
                    }, () -> {
                        handleNavigationBackWhenNodeNotFound(this::UnfollowFromList);
                    });
                }
                return;
            }

            // Check for bottom disabled container
            AccessibilityNodeInfo bottomDisabledContainer = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/thread_disabled_bottom_description");
            if (bottomDisabledContainer != null) {
                String Text = bottomDisabledContainer.getText().toString();
                if (Text.contains("unless they follow you.")) {
                    if (random.nextInt(100) < 50) {
                        this.istriedUnFollowFromList = true;
                        handleNavigationBackWhenNodeNotFound(() -> {
                            handler.postDelayed(this::UnfollowFromList, 300 + random.nextInt(300));
                        });
                    } else {
                        enterProfileFromChatAndUnfollow(rootNode, () -> {
                            checkThreadDetails(this::UnfollowFromProfileHome);
                        }, () -> {
                            handleNavigationBackWhenNodeNotFound(this::UnfollowFromList);
                        });
                    }
                    return;
                }
            }

            // Check for header text container
            AccessibilityNodeInfo HeaderTextContainer = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/thread_context_item_1");
            if (HeaderTextContainer != null) {
                String Text = HeaderTextContainer.getText().toString();
                if (Text.contains("You've followed this Instagram account since ")) {
                    if (random.nextInt(100) < 50) {
                        this.istriedUnFollowFromProfile = true;
                        handleNavigationBackWhenNodeNotFound(() -> {
                            handler.postDelayed(this::UnfollowFromList, 300 + random.nextInt(300));
                        });
                    } else {
                        enterProfileFromChatAndUnfollow(rootNode, () -> {
                            checkThreadDetails(this::UnfollowFromProfileHome);
                        }, () -> {
                            handleNavigationBackWhenNodeNotFound(this::UnfollowFromList);
                        });
                    }
                    return;
                } else if (Text.contains("You follow each other on Instagram")) {
                    handler.postDelayed(() -> handleNavigationBackWhenNodeNotFound(this::startUnFollowing), 500 + random.nextInt(100));
                    return;
                }
            }

            // Fallback to checking unfollowing from profile following list
            Log.i(TAG, "Could not find any signs of not following from chat, Going to check Unfollowing from profile following list");
            enterProfileFromChatAndUnfollow(rootNode, () -> {
                checkThreadDetails(this::CheckFollowingOrNoFromProfile);
            }, () -> {
                handleNavigationBackWhenNodeNotFound(this::startUnFollowing);
            });

        } catch (Exception e) {
            // Log the exception for debugging purposes
            Log.e(TAG, "Exception occurred in CheckFollowingFromChat: " + e.getMessage(), e);

            // Perform cleanup and exit with an appropriate message
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
        }
    }

    private void enterProfileFromChatAndUnfollow(AccessibilityNodeInfo rootNode, Action Callback, Action FailCallback) {
        try {
            Log.i(TAG, "Entered enterProfileFromChatAndUnfollow");

            if (shouldContinueAutomation()) {
                return;
            }

            // Validate rootNode
            if (rootNode == null) {
                throw new RuntimeException("Root node is null inside enterProfileFromChatAndUnfollow");
            }

            // Refresh the root node to ensure it's up-to-date
            rootNode.refresh();

            // Attempt to find and interact with the viewProfileButton
            AccessibilityNodeInfo viewProfileButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/view_profile_button", 3);
            if (viewProfileButton != null) {
                if (viewProfileButton.isClickable() && viewProfileButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    Log.i(TAG, "Clicked Following viewProfileButton Through Accessibility");
                    followButtonChecker(()->{
                        this.tracker++;
                        Log.i(TAG, "Tracker: " + this.tracker);
                        handler.postDelayed(Callback::execute, 300 + random.nextInt(100));
                    },FailCallback);
                } else {
                    Log.i(TAG, "Could not Click viewProfileButton using Accessibility Service going to click using Bounds");
                    getBoundsAndClick(viewProfileButton, () -> {
                        followButtonChecker(()->{
                            this.tracker++;
                            Log.i(TAG, "Tracker: " + this.tracker);
                            Callback.execute();
                        },FailCallback);
                    }, "Center", 500, 1000);
                }
                return;
            }

            // Attempt to find and interact with the EnterProfileButton
            AccessibilityNodeInfo EnterProfileButton = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/header_title");
            if (EnterProfileButton != null) {
                Log.i(TAG, "Found EnterProfileButton inside CheckFollowingFromChat");
                if (EnterProfileButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    followButtonChecker(()->{
                        this.tracker++;
                        Log.i(TAG, "Tracker: " + this.tracker);
                        handler.postDelayed(Callback::execute, 1500 + random.nextInt(1500));
                    }, FailCallback);
                } else {
                    getBoundsAndClick(EnterProfileButton, () -> {
                        followButtonChecker(()->{
                            this.tracker++;
                            Log.i(TAG, "Tracker: " + this.tracker);
                            Callback.execute();
                        }, FailCallback);
                    }, "Center", 1500, 3000);
                }
            } else {
                Log.i(TAG, "Could not found EnterProfileButton in chat going to Unfollow from the UserProfile list");
                handler.postDelayed(FailCallback::execute, 500 + random.nextInt(500));
            }

        } catch (Exception e) {
            // Log the exception for debugging purposes
            Log.e(TAG, "Exception occurred in enterProfileFromChatAndUnfollow: " + e.getMessage(), e);

            // Perform cleanup and exit with an appropriate message
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
        }
    }

    private void followButtonChecker(Action Callback, Action faildCallback) {
        AccessibilityNodeInfo followButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/profile_header_follow_button",15);
        if(followButton != null){
            Callback.execute();
        }else{
            faildCallback.execute();
        }
    }

    private void checkThreadDetails(Action Callback) {
        try {
            Log.i(TAG, "Entered checkThreadDetails");

            // Attempt to find the thread details header
            AccessibilityNodeInfo threadBox = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/thread_details_header", 2);
            if (threadBox == null) {
                Log.i(TAG, "Thread details header not found, executing callback");
                Callback.execute();
                return;
            }

            // Find the Profile TextView inside the threadBox
            AccessibilityNodeInfo profileTextView = helperFunctions.findNodeByClassAndText(threadBox, "android.widget.TextView", "Profile");
            if (profileTextView == null) {
                Log.e(TAG, "Could not find Profile TextView inside threadBox");
                handleNavigationBackWhenNodeNotFound(this::startUnFollowing);
                return;
            }

            // Get the parent node of the Profile TextView (Profile Button)
            AccessibilityNodeInfo profileButton = profileTextView.getParent();
            if (profileButton != null) {
                if (profileButton.isClickable() && profileButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    this.tracker++;
                    Log.i(TAG, "Tracker: " + this.tracker);
                    handler.postDelayed(Callback::execute, 1500 + random.nextInt(1500));
                } else {
                    Log.i(TAG, "Could not click Profile Button using Accessibility Service, attempting bounds-based click");
                    getBoundsAndClick(profileButton, () -> {
                        this.tracker++;
                        Log.i(TAG, "Tracker: " + this.tracker);
                        Callback.execute();
                    }, "Center", 1500, 3000);
                }
                return;
            }

            // If profileButton is null, handle navigation back
            Log.e(TAG, "Profile Button not found, navigating back");
            handleNavigationBackWhenNodeNotFound(this::startUnFollowing);

        } catch (Exception e) {
            // Log the exception for debugging purposes
            Log.e(TAG, "Exception occurred in checkThreadDetails: " + e.getMessage(), e);

            // Perform cleanup and exit with an appropriate message
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
        }
    }

    private void performWarmUpFunctionOnProfile(Action Callback) {
        Log.i(TAG, "Entered performWarmUpFunctionOnMethod4");
        if (shouldContinueAutomation()) {
            return;
        }

        if (popUpHandler.handleOtherPopups(()->this.performWarmUpFunctionOnProfile(Callback), null)) return;

        AccessibilityNodeInfo rootNode = helperFunctions.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "Entered performWarmUpFunctionOnMethod4");
            Callback.execute();
            return;
        }
        int warmUpFunctionChances = random.nextInt(100);
        if (warmUpFunctionChances < 10) {
            Log.d(TAG, "Going to view Profile");
            this.instagramWarmUpFunctions.viewProfile(rootNode, Callback);
            return;
        } else if (warmUpFunctionChances < 20) {
            Log.d(TAG, "Going to view Posts");
            this.instagramWarmUpFunctions.viewPosts(rootNode, Callback);
            return;
        } else if (warmUpFunctionChances < 30) {
            Log.d(TAG, "Going to view Followers");
            this.instagramWarmUpFunctions.viewFollowingandFollowers(rootNode, "com.instagram.android:id/row_profile_header_textview_followers_count", "com.instagram.android:id/row_profile_header_followers_container", "com.instagram.android:id/profile_header_familiar_followers_value", "com.instagram.android:id/profile_header_followers_stacked_familiar", Callback);
            return;
        }

        Log.i(TAG, "NoChances Of Performing WarmUpFunctions");
        Callback.execute();
    }

    private void CheckFollowingOrNoFromProfile() {
        try {
            Log.i(TAG, "Entered CheckFollowingOrNoFromProfile");

            if (shouldContinueAutomation()) {
                return;
            }

            // Handle action blocker popups
            boolean outerdialogcheck = popUpHandler.checkForActionBlocker(() -> {
                accountManager.BlockCurrentAccount();
                accountManager.setAccountLimitHit(true);
                getProfileData(() -> {
                    ChangeAccount(this::callbackAccordingToType);
                });
            });

            if (outerdialogcheck) {
                Log.e(TAG, "outerdialogcheck in CheckFollowingOrNoFromProfile is true");
                return;
            }

            if (popUpHandler.handleOtherPopups(()->this.CheckFollowingOrNoFromProfile(), null)) return;

            // Handle Default sorting type
            if (accountManager.getCurrentAccountTypeOfSortForUnfollowing().equals("Default")) {
                handler.postDelayed(this::UnfollowFromProfileHome, 500 + random.nextInt(300));
                return;
            }

            isCheckedThroughProfile = true;

            // Find the Following Count Button
            AccessibilityNodeInfo FollowingCountButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/profile_header_following_stacked_familiar", 5);
            if (FollowingCountButton == null) {
                FollowingCountButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/row_profile_header_following_container", 5);
                if (FollowingCountButton == null) {
                    Log.i(TAG, "Could not find FollowingCountButton");
                    handleNavigationBackWhenNodeNotFound(this::startUnFollowing);
                    return;
                }
            }

            Log.i(TAG, "Found Following Count Button Going To Click");

            // Attempt to click the Following Count Button using performAction
            if (FollowingCountButton.isClickable() && FollowingCountButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                Log.i(TAG, "Entered Following List of a profile Inside CheckFollowingOrNoFromProfile through Accessibility");
                this.tracker++;
                Log.i(TAG, "Tracker: " + this.tracker);
                handler.postDelayed(this::CheckFollowingFromFollowingList, 2000 + random.nextInt(1000));
            } else {
                Log.i(TAG, "Could not Enter Following List of a profile Inside CheckFollowingOrNoFromProfile through Accessibility, going to enter through click gesture");
                this.tracker++;
                Log.i(TAG, "Tracker: " + this.tracker);
                getBoundsAndClick(FollowingCountButton, this::CheckFollowingFromFollowingList, "Center", 2000, 3000);
            }

        } catch (Exception e) {
            // Log the exception for debugging purposes
            Log.e(TAG, "Exception occurred in CheckFollowingOrNoFromProfile: " + e.getMessage(), e);

            // Perform cleanup and exit with an appropriate message
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
        }
    }

    private void CheckFollowingFromFollowingList() {
        try {
            Log.i(TAG, "Entered CheckFollowingFromFollowingList");

            if (shouldContinueAutomation()) {
                return;
            }

            // Handle action blocker popups
            boolean outerdialogcheck = popUpHandler.checkForActionBlocker(() -> {
                accountManager.BlockCurrentAccount();
                accountManager.setAccountLimitHit(true);
                getProfileData(() -> {
                    ChangeAccount(this::callbackAccordingToType);
                });
            });

            if (outerdialogcheck) {
                Log.e(TAG, "outerdialogcheck in CheckFollowingFromFollowingList is true");
                return;
            }

            if (popUpHandler.handleOtherPopups(()->this.CheckFollowingFromFollowingList(), null)) return;

            // Find the list of users
            List<AccessibilityNodeInfo> UsersList = helperFunctions.FindAndReturnNodesById("com.instagram.android:id/follow_list_username", 15);
            if (UsersList == null || UsersList.isEmpty()) {
                Log.e(TAG, UsersList == null ? "UserList list node is null inside CheckFollowingFromFollowingList." : "UserList list does not have enough children Inside CheckFollowingFromFollowingList.");
                handler.postDelayed(() -> handleNavigationBackWhenNodeNotFound(this::startUnFollowing), 300 + random.nextInt(200));
                return;
            }

            Log.i(TAG, "Got Node List From Following of profile, Going To check Username Exists or no");

            for (int i = 0; i < UsersList.size(); i++) {
                AccessibilityNodeInfo Node = UsersList.get(i);
                if (Node == null || Node.getText() == null) {
                    Log.e(TAG, "Username Text is Null at Index " + i);
                    continue;
                }

                String Username = Node.getText().toString().trim();
                Log.i(TAG, "Username at index " + i + " " + Username);

                if (this.usernameToUnfollowFrom.equals(Username)) {
                    Log.i(TAG, "Found profile inside List, Moving To next Profile");
                    handler.postDelayed(() -> handleNavigationBackWhenNodeNotFound(this::startUnFollowing), 300 + random.nextInt(200));
                    return;
                }
            }

            Log.e(TAG, "Profile Not found in Following List");
            helperFunctions.navigateBack();
            this.tracker--;
            Log.i(TAG, "Tracker: " + this.tracker);
            handler.postDelayed(this::UnfollowFromProfileHome, 500 + random.nextInt(300));

        } catch (Exception e) {
            // Log the exception for debugging purposes
            Log.e(TAG, "Exception occurred in CheckFollowingFromFollowingList: " + e.getMessage(), e);

            // Perform cleanup and exit with an appropriate message
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
        }
    }

    private void UnfollowFromProfileHome() {
        try {
            Log.i(TAG, "Entered UnfollowFromProfile");

            if (shouldContinueAutomation()) {
                return;
            }

            istriedUnFollowFromProfile = true;

            // Handle action blocker popups
            boolean outerdialogcheck = popUpHandler.checkForActionBlocker(() -> {
                accountManager.BlockCurrentAccount();
                accountManager.setAccountLimitHit(true);
                getProfileData(() -> {
                    ChangeAccount(this::callbackAccordingToType);
                });
            });

            if (outerdialogcheck) {
                Log.e(TAG, "outerdialogcheck in UnfollowFromProfileHome is true");
                return;
            }

            // Get the root node
            AccessibilityNodeInfo rootNode = helperFunctions.getRootInActiveWindow();
            if (rootNode == null) {
                Log.i(TAG, "Could not found RootNode inside of UnfollowFromProfile");
                if (isCheckedThroughFollowersList || istriedUnFollowFromList) {
                    handler.postDelayed(() -> handleNavigationBackWhenNodeNotFound(this::startUnFollowing), 300 + random.nextInt(200));
                } else {
                    handler.postDelayed(() -> handleNavigationBackWhenNodeNotFound(this::UnfollowFromList), 300 + random.nextInt(200));
                }
                return;
            }

            // Find the Follow button
            AccessibilityNodeInfo FollowButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/profile_header_follow_button", 10);
            if (FollowButton == null) {
                Log.i(TAG, "Could not found Follow Button inside of UnfollowFromProfile");
                if (isCheckedThroughFollowersList || istriedUnFollowFromList) {
                    handler.postDelayed(() -> handleNavigationBackWhenNodeNotFound(this::startUnFollowing), 300 + random.nextInt(200));
                } else {
                    handler.postDelayed(() -> handleNavigationBackWhenNodeNotFound(this::UnfollowFromList), 300 + random.nextInt(200));
                }
                return;
            }

            String ButtonText = FollowButton.getText().toString();
            if (!"Following".equals(ButtonText)) {
                Log.i(TAG, "Could not found Following Button inside of UnfollowFromProfile");
                if (isCheckedThroughFollowersList || istriedUnFollowFromList) {
                    handler.postDelayed(() -> handleNavigationBackWhenNodeNotFound(this::startUnFollowing), 300 + random.nextInt(200));
                } else {
                    handler.postDelayed(() -> handleNavigationBackWhenNodeNotFound(this::UnfollowFromList), 300 + random.nextInt(200));
                }
                return;
            }

            // Attempt to click the Follow button
            if (FollowButton.isClickable() && FollowButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                Log.i(TAG, "Clicked Following Button Through Accessibility");
                handler.postDelayed(this::HandleUnfollowSlider, 300 + random.nextInt(100));
            } else {
                Log.i(TAG, "Could not Click Following Button using Accessibility Service going to click using Bounds");
                getBoundsAndClick(FollowButton, this::HandleUnfollowSlider, "Center", 500, 1000);
            }

        } catch (Exception e) {
            // Log the exception for debugging purposes
            Log.e(TAG, "Exception occurred in UnfollowFromProfileHome: " + e.getMessage(), e);

            // Perform cleanup and exit with an appropriate message
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
        }
    }

    private void HandleUnfollowSlider() {
        try {
            Log.i(TAG, "Entered HandleUnfollowPopUp");

            if (shouldContinueAutomation()) {
                return;
            }

            // Handle action blocker popups
            boolean outerdialogcheck = popUpHandler.checkForActionBlocker(() -> {
                accountManager.BlockCurrentAccount();
                accountManager.setAccountLimitHit(true);
                helperFunctions.navigateBack();
                getProfileData(() -> {
                    ChangeAccount(this::callbackAccordingToType);
                });
            });

            if (outerdialogcheck) {
                Log.e(TAG, "outerdialogcheck in HandleUnfollowSlider is true");
                return;
            }

            // Find the Unfollow button
            AccessibilityNodeInfo UnfollowButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/follow_sheet_unfollow_row", 10);
            if (UnfollowButton == null) {
                Log.i(TAG, "Could not found Unfollow Button inside of HandleUnfollowSlider");
                this.tracker++;
                Log.i(TAG, "Tracker: " + this.tracker);
                if (isCheckedThroughFollowersList || istriedUnFollowFromList) {
                    handler.postDelayed(() -> handleNavigationBackWhenNodeNotFound(this::startUnFollowing), 300 + random.nextInt(200));
                } else {
                    handler.postDelayed(() -> handleNavigationBackWhenNodeNotFound(this::UnfollowFromList), 300 + random.nextInt(200));
                }
                return;
            }

            // Attempt to click the Unfollow button
            if (UnfollowButton.isClickable() && UnfollowButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                Log.i(TAG, "Clicked Unfollow Button Through Accessibility");
                handler.postDelayed(() -> {
                    checkPopUps(() -> {
                        handler.postDelayed(() -> {
                            accountManager.IncrementFollowsDone();
                            accountManager.increaseThisRunFollows();
                            Log.e(TAG, "this.noOfUnFollowedAccounts: " + accountManager.getFollowsDone());
                            handleNavigationBackWhenNodeNotFound(this::startUnFollowing);
                        }, 300 + random.nextInt(200));
                    });
                }, 600 + random.nextInt(400));
            } else {
                Log.i(TAG, "Could not Click Unfollow Button using Accessibility Service going to click using Bounds");
                getBoundsAndClick(UnfollowButton, () -> {
                    checkPopUps(() -> {
                        accountManager.IncrementFollowsDone();
                        accountManager.increaseThisRunFollows();
                        Log.e(TAG, "this.noOfUnFollowedAccounts: " + accountManager.getFollowsDone());
                        handler.postDelayed(() -> handleNavigationBackWhenNodeNotFound(this::startUnFollowing), 300 + random.nextInt(200));
                    });
                }, "Center", 600, 1000);
            }

        } catch (Exception e) {
            // Log the exception for debugging purposes
            Log.e(TAG, "Exception occurred in HandleUnfollowSlider: " + e.getMessage(), e);

            // Perform cleanup and exit with an appropriate message
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
        }
    }

    private void checkPopUps(Action Callback) {
        try {
            Log.i(TAG, "Entered checkPopUps, to check PopUps");

            if (shouldContinueAutomation()) {
                return;
            }

            // Handle action blocker popups
            boolean outerdialogcheck = popUpHandler.checkForActionBlocker(() -> {
                accountManager.BlockCurrentAccount();
                accountManager.setAccountLimitHit(true);
                getProfileData(() -> {
                    ChangeAccount(this::callbackAccordingToType);
                });
            });

            if (outerdialogcheck) {
                Log.e(TAG, "outerdialogcheck in checkPopUps is true");
                return;
            }

            if (popUpHandler.handleOtherPopups(Callback, new String[]{"com.instagram.android:id/igds_alert_dialog_primary_button"})) return;

            Log.i(TAG, "Handled all PopUps");
            Callback.execute();

        } catch (Exception e) {
            // Log the exception for debugging purposes
            Log.e(TAG, "Exception occurred in checkPopUps: " + e.getMessage(), e);

            // Perform cleanup and exit with an appropriate message
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
        }
    }

    private void UnfollowFromList() {
        try {
            Log.i(TAG, "Entered UnfollowFromList");

            if (shouldContinueAutomation()) {
                return;
            }

            // Handle action blocker popups
            boolean outerdialogcheck = popUpHandler.checkForActionBlocker(() -> {
                accountManager.BlockCurrentAccount();
                accountManager.setAccountLimitHit(true);
                getProfileData(() -> {
                    ChangeAccount(this::callbackAccordingToType);
                });
            });

            if (outerdialogcheck) {
                Log.e(TAG, "outerdialogcheck in UnfollowFromList is true");
                return;
            }

            // Get the user node to unfollow
            AccessibilityNodeInfo userNode = getNodeFromListToUnFollow();
            if (userNode == null) {
                Log.e(TAG, "Node to unfollow is null in UnfollowFromList");
                startUnFollowing();
                return;
            }

            Log.e(TAG, "Back to UnfollowFromList got userNode");

            // Find the Option button
            AccessibilityNodeInfo OptionButton = HelperFunctions.findNodeByResourceId(userNode, "com.instagram.android:id/media_option_button");

            if (OptionButton != null) {
                if (OptionButton.isClickable() && OptionButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    Log.i(TAG, "Clicked on Options Button through Accessibility");
                    handler.postDelayed(this::handleUnfollowOptionsDialog, 300 + random.nextInt(300));
                } else {
                    Log.i(TAG, "Could not click the Options Button through Accessibility, going to click through Bounds");
                    getBoundsAndClick(OptionButton, this::handleUnfollowOptionsDialog, "Center", 300, 600);
                }
            } else {
                Log.i(TAG, "Option button not found, going to enter and Unfollow from Profile Home");
                if (isCheckedThroughProfile) {
                    handler.postDelayed(this::startUnFollowing, 500 + random.nextInt(500));
                    return;
                }
                if (userNode.isClickable() && userNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    Log.i(TAG, "Clicked UserNode through Accessibility, going to Unfollow From Profile Home");
                    handler.postDelayed(this::UnfollowFromProfileHome, 2000 + random.nextInt(1500));
                } else {
                    Log.i(TAG, "Could Not Click UserNode through Accessibility going to Check New Node");
                    startUnFollowing();
                }
            }

        } catch (Exception e) {
            // Log the exception for debugging purposes
            Log.e(TAG, "Exception occurred in UnfollowFromList: " + e.getMessage(), e);

            // Perform cleanup and exit with an appropriate message
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
        }
    }

    private void handleUnfollowOptionsDialog() {
        try {
            Log.i(TAG, "Entered handleUnfollowOptionsDialog");

            if (shouldContinueAutomation()) {
                return;
            }

            // Find the options container
            AccessibilityNodeInfo OptionsContainer = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/context_menu_options_list", 10);
            if (OptionsContainer == null) {
                // Try to find alternative layout
                AccessibilityNodeInfo layoutContainerBottomSheet = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/layout_container_bottom_sheet", 20);
                if (layoutContainerBottomSheet != null) {
                    AccessibilityNodeInfo UnfollowButton = helperFunctions.findNodeByClassAndText(layoutContainerBottomSheet, "android.widget.Button", "Unfollow");
                    if (UnfollowButton != null && UnfollowButton.isClickable() && UnfollowButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        Log.i(TAG, "Clicked on Unfollow Button through Accessibility");
                        handler.postDelayed(()->checkPopUps(() -> {
                            accountManager.IncrementFollowsDone();
                            accountManager.increaseThisRunFollows();
                            Log.e(TAG, "this.noOfUnFollowedAccounts: " + accountManager.getFollowsDone());
                            handler.postDelayed(this::startUnFollowing, 1000 + random.nextInt(1000));
                        }),1500);
                    } else {
                        Log.i(TAG, "Could not click the Unfollow Button through Accessibility, going to click through Bounds");
                        getBoundsAndClick(UnfollowButton, () -> {
                            accountManager.IncrementFollowsDone();
                            accountManager.increaseThisRunFollows();
                            Log.e(TAG, "this.noOfUnFollowedAccounts: " + accountManager.getFollowsDone());
                            startUnFollowing();
                        }, "Center", 500, 1000);
                    }
                    return;
                }

                Log.e(TAG, "Could not find OptionsContainer Container inside of handleUnfollowOptionsDialog");
                if (istriedUnFollowFromProfile) {
                    startUnFollowing();
                } else {
                    EnterProfileAfterOptionsButtonRejection();
                }
                return;
            }

            // Find the unfollow button
            AccessibilityNodeInfo unfollowButton = helperFunctions.findButtonByContentDesc(OptionsContainer, "Unfollow");

            if (unfollowButton == null) {
                Log.e(TAG, "Could not find Unfollow Button");
                helperFunctions.navigateBack();
                if (istriedUnFollowFromProfile) {
                    handler.postDelayed(this::startUnFollowing, 200 + random.nextInt(100));
                } else {
                    handler.postDelayed(this::EnterProfileAfterOptionsButtonRejection, 200 + random.nextInt(100));
                }
                return;
            }

            Log.e(TAG, "Found Unfollow Button inside of Options Container");

            // Attempt to click the unfollow button
            if (unfollowButton.isClickable() && unfollowButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                Log.i(TAG, "Found Unfollow Button inside the options and Clicked through Accessibility");
                handler.postDelayed(()->checkPopUps(() -> {
                    accountManager.IncrementFollowsDone();
                    accountManager.increaseThisRunFollows();
                    Log.e(TAG, "this.noOfUnFollowedAccounts: " + accountManager.getFollowsDone());
                    handler.postDelayed(this::startUnFollowing, 1000 + random.nextInt(1000));
                }),1500);
            } else {
                Log.i(TAG, "Could not Found Unfollow Button inside the options, going to click through Click Gesture");
                getBoundsAndClick(unfollowButton, () -> {
                    accountManager.IncrementFollowsDone();
                    accountManager.increaseThisRunFollows();
                    Log.e(TAG, "this.noOfUnFollowedAccounts: " + accountManager.getFollowsDone());
                    startUnFollowing();
                }, "Center", 500, 1000);
            }

        } catch (Exception e) {
            // Log the exception for debugging purposes
            Log.e(TAG, "Exception occurred in handleUnfollowOptionsDialog: " + e.getMessage(), e);

            // Perform cleanup and exit with an appropriate message
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
        }
    }

    private void EnterProfileAfterOptionsButtonRejection() {
        try {
            Log.i(TAG, "Entered EnterProfileAfterOptionsButtonRejection");

            if (shouldContinueAutomation()) {
                return;
            }

            // Get the user node
            AccessibilityNodeInfo userNode = getNodeFromListToUnFollow();
            if (userNode == null) {
                Log.i(TAG, "Could not find userNode inside of EnterProfileAfterOptionsButtonRejection");
                startUnFollowing();
                return;
            }

            Log.i(TAG, "Found userNode going to directly check");

            // Attempt to click the user node
            if (userNode.isClickable() && userNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                Log.i(TAG, "Clicked userNode through Accessibility");
                this.tracker++;
                Log.i(TAG, "Tracker: " + this.tracker);
                handler.postDelayed(this::UnfollowFromProfileHome, 2000 + random.nextInt(1000));
            } else {
                Log.i(TAG, "Could not Found userNode inside the options, going to click through Click Gesture");
                getBoundsAndClick(userNode, () -> {
                    this.tracker++;
                    Log.i(TAG, "Tracker: " + this.tracker);
                    UnfollowFromProfileHome();
                }, "Center", 2000, 3000);
            }

        } catch (Exception e) {
            // Log the exception for debugging purposes
            Log.e(TAG, "Exception occurred in EnterProfileAfterOptionsButtonRejection: " + e.getMessage(), e);

            // Perform cleanup and exit with an appropriate message
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
        }
    }

    private AccessibilityNodeInfo getNodeFromListToUnFollow() {
        try {
            Log.i(TAG, "Entered getNodeFromListToUnFollow");

            // Get the root node of the active window
            AccessibilityNodeInfo rootNode = helperFunctions.getRootInActiveWindow();
            if (rootNode == null) {
                Log.e(TAG, "No rootNode found inside of getNodeFromList");
                return null;
            }

            // Find the list of users
            List<AccessibilityNodeInfo> UsersList = rootNode.findAccessibilityNodeInfosByViewId(Container_id);

            // Recycle the root node to avoid memory leaks
            rootNode.recycle();

            if (UsersList == null || UsersList.isEmpty()) {
                Log.e(TAG, UsersList == null ? "UserList list node is null." : "UserList list does not have enough children.");
                return null;
            }

            // Iterate through the user nodes
            for (int i = 0; i < UsersList.size(); i++) {
                AccessibilityNodeInfo Node = UsersList.get(i);
                if (Node == null) {
                    continue;
                }

                // Find the username node
                AccessibilityNodeInfo usernameNode = HelperFunctions.findNodeByResourceId(Node, "com.instagram.android:id/follow_list_username");
                if (usernameNode == null || usernameNode.getText() == null) {
                    continue;
                }

                String Username = usernameNode.getText().toString().trim();
                if (accountManager.getLastUserDone().equals(Username)) {
                    Log.i(TAG, "Username = " + Username);
                    return Node;
                }
            }

            return null;

        } catch (Exception e) {
            // Log the exception for debugging purposes
            Log.e(TAG, "Exception occurred in getNodeFromListToUnFollow: " + e.getMessage(), e);

            // Perform cleanup and exit with an appropriate message
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");

            // Return null to indicate failure
            return null;
        }
    }

    private void handleNavigationBackWhenNodeNotFound(Action Callback) {
        try {
            Log.i(TAG, "Entered handleNavigationBackWhenNodeNotFound");

            Runnable navigationTask = new Runnable() {
                @Override
                public void run() {
                    try {
                        if (shouldContinueAutomation()) {
                            return;
                        }

                        // Navigate back
                        helperFunctions.navigateBack();
                        tracker--;
                        Log.i(TAG, "Tracker: " + tracker);

                        if (tracker == 0) {
                            handler.postDelayed(Callback::execute, 1000 + random.nextInt(500));
                        } else {
                            int delay = 500 + random.nextInt(500);
                            handler.postDelayed(this, delay);
                        }

                    } catch (Exception e) {
                        // Log the exception for debugging purposes
                        Log.e(TAG, "Exception occurred in handleNavigationBackWhenNodeNotFound: " + e.getMessage(), e);

                        // Perform cleanup and exit with an appropriate message
                        helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
                    }
                }
            };

            // Start the navigation task
            navigationTask.run();

        } catch (Exception e) {
            // Log the exception for debugging purposes
            Log.e(TAG, "Exception occurred in handleNavigationBackWhenNodeNotFound: " + e.getMessage(), e);

            // Perform cleanup and exit with an appropriate message
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
        }
    }






    //  helper functions
    public void clickOnBounds(android.graphics.Rect bounds, Action callback, String Type, int bastime, int maxTime) {
        if (shouldContinueAutomation()) {
            return;
        }
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
                    handler.post(() -> helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error"));
                }
            }, null);
        } catch (Exception e) {
            Log.e(TAG, "Error while clicking in the center of bounds", e);
            handler.post(() -> helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error"));
        }
    }

    public void performStaticScrollUp(Action callback) {
        if (shouldContinueAutomation()) {
            return;
        }
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
                        helperFunctions.cleanupAndExit("Automation Could not be Completed Please make sure The Device has Accessibility enabled.", "error");
                    }, 1000 + random.nextInt(2000));
                }
            }, null);
        } catch (Exception e) {
            Log.e(TAG, "Error during scroll up", e);
            // Fixed delay in case of exception
            handler.postDelayed(() -> {
                helperFunctions.cleanupAndExit("Automation Could not be Completed Please make sure The Device has Accessibility enabled.", "error");
            }, 1000 + random.nextInt(2000));
        }
    }

    public void performScrollUp(Action callback) {
        if (shouldContinueAutomation()) {
            return;
        }
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
                        helperFunctions.cleanupAndExit("Automation Could not be Completed Please make sure The Device has Accessibility enabled.", "error");
                    }, 1000 + random.nextInt(2000));
                }
            }, null);
        } catch (Exception e) {
            Log.e(TAG, "Error during scroll up", e);
            handler.postDelayed(() -> {
                helperFunctions.cleanupAndExit("Automation Could not be Completed Please make sure The Device has Accessibility enabled.", "error");
            }, 1000 + random.nextInt(2000));
        }
    }

    public void performTimedScrollUp(long durationInMillis, Runnable onTimeUpCallback) {
        if (shouldContinueAutomation()) {
            return;
        }
        if (popUpHandler.handleOtherPopups(()->this.performTimedScrollUp(durationInMillis,onTimeUpCallback), null)) return;

        final long startTime = System.currentTimeMillis();
        final AtomicBoolean isScrolling = new AtomicBoolean(true);

        final Runnable[] scrollRunnable = new Runnable[1];
        scrollRunnable[0] = new Runnable() {
            @Override
            public void run() {
                if (shouldContinueAutomation()) {
                    return;
                }
                if (!isScrolling.get()) {
                    return;
                }

                long currentTime = System.currentTimeMillis();
                if (currentTime - startTime >= durationInMillis) {
                    Log.d(TAG, "Scroll time completed, executing callback");
                    isScrolling.set(false);
                    onTimeUpCallback.run();
                    return;
                }

                performScrollUp(new Action() {
                    @Override
                    public void execute() {
                        if (isScrolling.get()) {
                            // Schedule next scroll with random delay between 2-4 seconds
                            int nextDelay = 2000 + random.nextInt(2000);
                            Log.d(TAG, "Scheduling next scroll in " + nextDelay + "ms");
                            handler.postDelayed(scrollRunnable[0], nextDelay);
                        }
                    }
                });
            }
        };

        handler.post(scrollRunnable[0]);

        // Safety timeout handler
        handler.postDelayed(() -> {
            if (isScrolling.get()) {
                Log.d(TAG, "Safety timeout triggered, ensuring callback execution");
                isScrolling.set(false);
                onTimeUpCallback.run();
            }
        }, durationInMillis + 5000); // Add 5 seconds buffer for safety
    }

    private void recycleNodes(AccessibilityNodeInfo rootNode, List<AccessibilityNodeInfo> usersList, AccessibilityNodeInfo lastNode, AccessibilityNodeInfo usernameNode) {
        if (usernameNode != null) usernameNode.recycle();
        if (lastNode != null) lastNode.recycle();
        if (usersList != null) {
            for (AccessibilityNodeInfo node : usersList) {
                if (node != null) node.recycle();
            }
        }
        if (rootNode != null) rootNode.recycle();
    }

    public void performScrollDown(Action callback) {
        if (shouldContinueAutomation()) {
            return;
        }
        Path swipePath = new Path();
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        // Scroll from top to bottom
        float startY = screenHeight * (0.3f + random.nextFloat() * 0.1f); // Start at the top
        float endY = screenHeight * (0.7f + random.nextFloat() * 0.1f);   // End at the bottom
        float xVariation = screenWidth * (0.1f * random.nextFloat() - 0.05f); // Add some random variation in the X-axis

        swipePath.moveTo(screenWidth / 2f + xVariation, startY); // Start point
        swipePath.lineTo(screenWidth / 2f + xVariation, endY);   // End point

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        int gestureDuration = 150 + random.nextInt(150); // Random gesture duration
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
                        helperFunctions.cleanupAndExit("Automation Could not be Completed Please make sure The Device has Accessibility enabled.", "error");
                    }, 1000 + random.nextInt(2000));
                }
            }, null);
        } catch (Exception e) {
            Log.e(TAG, "Error during scroll down", e);
            handler.postDelayed(() -> {
                helperFunctions.cleanupAndExit("Automation Could not be Completed Please make sure The Device has Accessibility enabled.", "error");
            }, 1000 + random.nextInt(2000));
        }
    }

    private AccessibilityNodeInfo getFirstNodeFromList() {
        Log.e(TAG, "Entered getFirstNodeFromList");

        AccessibilityNodeInfo rootNode = helperFunctions.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "Root node is null");
            return null;
        }
        rootNode.refresh();
        List<AccessibilityNodeInfo> UsersList = new ArrayList<>();

        if (Container_id.equals("com.instagram.android:id/recommended_user_card_one")) {
            List<AccessibilityNodeInfo> UsersList1 = helperFunctions.FindAndReturnNodesById("com.instagram.android:id/recommended_user_card_one", 10);
            List<AccessibilityNodeInfo> UsersList2 = helperFunctions.FindAndReturnNodesById("com.instagram.android:id/recommended_user_card_two", 10);
            if (UsersList1 == null || UsersList2 == null) {
                return null;
            } else {
                Log.e(TAG, "Userlist1 length : " + UsersList1.size());
                Log.e(TAG, "Userlist2 length : " + UsersList2.size());
                int nodeLength = Math.max(UsersList1.size(), UsersList2.size());
                for (int i = 0; i < nodeLength; i++) {
                    if (i < UsersList1.size() && UsersList1.get(i) != null) {
                        UsersList.add(UsersList1.get(i));
                    }
                    if (i < UsersList2.size() && UsersList2.get(i) != null) {
                        UsersList.add(UsersList2.get(i));
                    }
                }
            }
        } else {
            UsersList = helperFunctions.FindAndReturnNodesById(Container_id, 10);
        }
//        List<AccessibilityNodeInfo> UsersList = helperFunctions.FindAndReturnAllNodesById(Container_id, 15);
        Log.e(TAG, "Userlist length : " + UsersList.size());
        rootNode.recycle();
        if (UsersList == null || UsersList.isEmpty()) {
            Log.e(TAG, UsersList == null ? "UserList list node is null." : "UserList list does not have enough children.");
//            UserListFound = true;
            return null;
        }

        UserListFound = false;
        userListEmptyScrollUpCount = 0;
        Log.e(TAG, "Child count = " + UsersList.size());
        AccessibilityNodeInfo child = null;
        for (int i = 0; i < UsersList.size(); i++) {
            Log.e(TAG, "Child number = " + i);
            child = UsersList.get(i);
            if (child == null) continue;

            AccessibilityNodeInfo followButton = HelperFunctions.findNodeByResourceId(child, Follow_Button_Id);
            if (followButton == null) {
                Log.e(TAG, "could not found follow button");
                child.recycle();
                child = null;
                continue;
            }

            CharSequence followButtonText = followButton.getText();
            followButton.recycle();
            Log.e(TAG, "going to check follow button");
            if (followButtonText == null || !"Follow".equals(followButtonText.toString())) {
                Log.e(TAG, "found follow button but text not matched");
//                Log.e(TAG, "Found profile with text: "+followButtonText.toString());
                child.recycle();
                child = null;
                continue;
            }
            Log.e(TAG, "follow button checked");

            AccessibilityNodeInfo usernameNode = HelperFunctions.findNodeByResourceId(child, Username_Id);
            if (usernameNode == null) {
                Log.e(TAG, "Username null");
                child.recycle();
                child = null;
                continue;
            }

            CharSequence Username = usernameNode.getText();
            usernameNode.recycle();

            if (Username != null && !accountManager.checkIsUserDone(Username.toString())) {
                Log.e(TAG, "Username = " + Username);
                accountManager.addUserDone(Username.toString());
                AccessibilityNodeInfo mutualFriendsText = HelperFunctions.findNodeByResourceId(child, "com.instagram.android:id/row_recommended_social_context");
                if (mutualFriendsText != null && mutualFriendsText.getText() != null) {
                    this.mutualFriendsString = mutualFriendsText.getText().toString();
                } else {
                    this.mutualFriendsString = null;
                }
                break;
            }

            child.recycle();
            child = null;
        }

        if (UsersList.size() == 1) {
            AccessibilityNodeInfo singleNode = UsersList.get(0);
            AccessibilityNodeInfo usernameNode = HelperFunctions.findNodeByResourceId(singleNode, Username_Id);
            singleNode.recycle();
            if (usernameNode != null) {
                lastChildname = usernameNode.getText() != null ? usernameNode.getText().toString() : null;
                usernameNode.recycle();
            }
        } else if (UsersList.size() > 1) {
            AccessibilityNodeInfo lastNode = UsersList.get(UsersList.size() - 2);
            AccessibilityNodeInfo usernameNode = HelperFunctions.findNodeByResourceId(lastNode, Username_Id);
            lastNode.recycle();
            if (usernameNode != null) {
                lastChildname = usernameNode.getText() != null ? usernameNode.getText().toString() : null;
                usernameNode.recycle();
            }
        }

        return child;
    }

    private void CheckNewNodes(Action callback) {
        if (shouldContinueAutomation()) {
            return;
        }
        AccessibilityNodeInfo rootNode = null;
        List<AccessibilityNodeInfo> usersList = null;
        AccessibilityNodeInfo lastNode = null;
        AccessibilityNodeInfo usernameNode = null;

        try {
            rootNode = helperFunctions.getRootInActiveWindow();
            if (rootNode == null) {
                Log.e(TAG, "Root node is null");
                accountManager.setListStatus();
                handleNavigationByType();
                return;
            }

            usersList = rootNode.findAccessibilityNodeInfosByViewId(Container_id);
            if (usersList == null || usersList.isEmpty()) {
                Log.e(TAG, "Suggestion list issue");
                accountManager.setListStatus();
                handleNavigationByType();
                return;
            }

            lastNode = usersList.size() == 1 ? usersList.get(0) : usersList.get(usersList.size() - 2);
            usernameNode = HelperFunctions.findNodeByResourceId(lastNode, Username_Id);

            if (usernameNode == null) {
                Log.e(TAG, "Username NotFound");
                accountManager.setListStatus();
                handleNavigationByType();
                return;
            }

            String checkLastUsername = usernameNode.getText().toString();
            Log.e(TAG,"Last Child name: "+ lastChildname);
            if (lastChildname.equals(checkLastUsername)) {
                Log.d(TAG, "Could not find new profiles");
                if("FollowProfilePostsLikers".equals(accountManager.getCurrentAccountAutomationType())){
                    method6.navigateBackAndGetNextPost();
                    return;
                }
                accountManager.BlockCurrentAccount();
                accountManager.setListStatus();
                handleNavigationByType();
            } else {
                Log.d(TAG, "Found new profiles in CheckNewNodes");
                callback.execute();
            }

        } finally {

            recycleNodes(rootNode, usersList, lastNode, usernameNode);
        }
    }

    public void handleNavigationByType() {
        Log.e(TAG,"Entered handleNavigationByType");
        if (shouldContinueAutomation()) {
            return;
        }
        switch (accountManager.getCurrentAccountAutomationType()) {
            case "NotificationSuggestion":
                List_Id = "com.instagram.android:id/recycler_view";
                Username_Id = "com.instagram.android:id/row_recommended_user_username";
                Follow_Button_Id = "com.instagram.android:id/row_recommended_user_follow_button";
                Container_id = "com.instagram.android:id/recommended_user_row_content_identifier";
                helperFunctions.navigateBack();
                accountManager.BlockCurrentAccount();
                handler.postDelayed(() -> {
                    this.getProfileData(() -> {
                        ChangeAccount(() -> {
                            this.callbackAccordingToType();
//                            this.enterNotificationSection(() -> {
//                                getfollowRequestsCount(() -> {
//                                    this.method1.recursivefindButtonandClick("activity_feed_see_all_row", 0, 20, this::startFollowing);
//                                });
//                            });
                        });
                    });
                }, 600 + random.nextInt(300));
                break;
            case "ProfileSuggestion":
                accountManager.BlockCurrentAccount();
                this.getProfileData(() -> {
                    ChangeAccount(() -> {
                        this.callbackAccordingToType();
//                        this.enterNotificationSection(() -> {
//                            getfollowRequestsCount(() -> OpenSearchFeed(this::ClickAndOpenSearchBar));
//                        });
                    });
                });
                break;
            case "ProfileLikersFollow":
                closeMyApp();
                handler.postDelayed(() -> {
                    launchApp(()->{
                        this.getProfileData(() -> {
                            this.ChangeAccount(this::callbackAccordingToType);
                        });
                    });
                }, 40000 + random.nextInt(20000));
                break;
            case "unFollow":
                helperFunctions.navigateBack();
                accountManager.BlockCurrentAccount();
                handler.postDelayed(() -> {
                    this.getProfileData(() -> {
                        ChangeAccount(() -> {
                            this.callbackAccordingToType();
//                            this.enterNotificationSection(() -> {
//                                enterProfile(this::startUnFollowingAutomation);
//                            });
                        });
                    });
                }, 800 + random.nextInt(200));
                break;
            case "FollowProfilePostsLikers":
                handler.postDelayed(() -> {
                    this.getProfileData(() -> {
                        ChangeAccount(() -> {
                            this.callbackAccordingToType();
                        });
                    });
                }, 800 + random.nextInt(200));
                break;
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

    private void scheduleNextAction(boolean isFirst, int baseDelay) {
        if (shouldContinueAutomation()) {
            return;
        }
        int delay = baseDelay + random.nextInt(RANDOM_DELAY);
        handler.postDelayed(() -> startProfileFollowing(isFirst), delay);
    }

    private boolean checkBioAndMutualFriends(AccessibilityNodeInfo rootNode) {
        Log.i(TAG, "Entered checkBioAndMutualFriends");
        rootNode.refresh();
        AccessibilityNodeInfo followersButtonCheck = helperFunctions.FindAndReturnNodeById(FOLLOW_BUTTON_ID, 15);
        if(followersButtonCheck == null){
            followersButtonCheck = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/profile_header_user_action_follow_button",5);
            if (followersButtonCheck == null) return false;
        }
        AccessibilityNodeInfo bio = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/profile_header_bio_text", 10);
        if (bio != null) {
            if (bio.isClickable()) {
                bio.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

            CharSequence bioText = bio.getText();
            bio.recycle();

            if (bioText != null && !bioText.toString().isEmpty()) {
                boolean bioNegativeCheckResult = helperFunctions.evaluateNegativeKeywords(bioText.toString(), accountManager.getCurrentAccountNegativeKeywords());
                if (bioNegativeCheckResult) {
                    Log.i(TAG, "Matched Negative Keywords");
                    return false;
                } else {
                    boolean bioPositiveCheckResult = helperFunctions.evaluatePositiveKeywords(bioText.toString(), accountManager.getCurrentAccountPositiveKeywords());
                    if (bioPositiveCheckResult) {
                        Log.i(TAG, "Matched Positive Keywords");
                        return true;
                    } else {
                        Log.d(TAG, "Bio text did not match positive keywords, checking mutual friends.");
                        return checkMutualFriends(rootNode);
                    }
                }
            } else {
                Log.e(TAG, "Bio text is null.");
                return checkMutualFriends(rootNode);
            }
        } else {
            Log.e(TAG, "Bio node not found.");
            return checkMutualFriends(rootNode);
        }
    }

    public boolean checkMutualFriends(AccessibilityNodeInfo rootnode) {
        AccessibilityNodeInfo mutualFriendsContainer = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/profile_header_follow_context_text", 1);

        String text = null;

        if (mutualFriendsContainer != null) {
            CharSequence friendsText = mutualFriendsContainer.getText();
            mutualFriendsContainer.recycle();

            if (friendsText != null) {
                text = friendsText.toString(); // Assign text from mutualFriendsContainer
            } else {
                Log.d(TAG, "Inside checkMutualFriends: mutual friends container text is null");
            }
        } else {
            Log.d(TAG, "Inside checkMutualFriends: mutual friends container not found");
        }

        if (text == null && this.mutualFriendsString != null) {
            text = this.mutualFriendsString;
        }

        if (text == null) {
            Log.d(TAG, "Inside checkMutualFriends: No valid mutual friends text found");
            return false;
        }

        return helperFunctions.getFollowerCount(text, accountManager.getCurrentAccountMutualCount());
    }

    private void recursiveCheckWithScroll(Action callback, String nodeId) {
        Log.i(TAG, "Entered recursiveCheckWithScroll");
        final int maxAttempts = 1 + random.nextInt(10) + random.nextInt(10);
        checkNodeAndScroll(callback, 0, maxAttempts, nodeId);
    }

    private void checkNodeAndScroll(Action callback, int currentAttempt, int maxAttempts, String nodeId) {
        Log.i(TAG, "Entered checkNodeAndScroll with attempt: " + currentAttempt);

        try {
            // Get the root node
            AccessibilityNodeInfo rootNode = this.helperFunctions.getRootInActiveWindow();
            if (rootNode == null && currentAttempt < maxAttempts) {
                Log.e(TAG, "Root node not found in checkNodeAndScroll");
                performScrollDown(() -> checkNodeAndScroll(callback, currentAttempt + 1, maxAttempts, nodeId));
                return;
            }

            try {
                // Find the target node by resource ID
                AccessibilityNodeInfo targetButton = HelperFunctions.findNodeByResourceId(rootNode, nodeId);
                if (targetButton != null) {
                    Log.i(TAG, "Found target button in checkNodeAndScroll");

                    // Attempt to click the target button
                    boolean isClicked = targetButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    targetButton.recycle();

                    if (isClicked) {
                        Log.i(TAG, "Clicked target button through Accessibility in checkNodeAndScroll");
                        handler.postDelayed(callback::execute, 1500 + random.nextInt(1000));
                    } else {
                        Log.i(TAG, "Clicked target button through gesture in checkNodeAndScroll");
                        Rect bounds = new Rect();
                        targetButton.getBoundsInScreen(bounds);
                        clickOnBounds(bounds, callback, "Center", 1500, 2500);
                    }
                    return;
                }
            } finally {
                // Ensure the root node is recycled
                if (rootNode != null) {
                    rootNode.recycle();
                }
            }

            // If the target node is not found, scroll down and retry
            if (currentAttempt < maxAttempts) {
                Log.i(TAG, "Target node not found, scrolling down and retrying (attempt: " + (currentAttempt + 1) + ")");
                performScrollDown(() -> checkNodeAndScroll(callback, currentAttempt + 1, maxAttempts, nodeId));
            } else {
                Log.e(TAG, "Max attempts reached, exiting automation");
                handler.postDelayed(() -> helperFunctions.cleanupAndExit(
                        "Automation could not be completed. Please ensure the device has Accessibility enabled.",
                        "error"
                ), 800 + random.nextInt(1000));
            }
        } catch (Exception e) {
            // Log any unexpected exceptions
            Log.e(TAG, "An unexpected error occurred in checkNodeAndScroll: " + e.getMessage(), e);
            handler.postDelayed(() -> helperFunctions.cleanupAndExit(
                    "An unexpected error occurred. Could not found Feed Button, Please Make sure the Accessibility is Enabled.",
                    "error"
            ), 800 + random.nextInt(1000));
        }
    }

    public void getProfileData(Action Callback) {
        Log.e(TAG,"Entered getProfileData");
        if (shouldContinueAutomation()) {
            return;
        }
//        if ("ProfileLikersFollow".equals(type) && this.tracker != 0) {
//            helperFunctions.navigateBack();
//            this.movebackUntilReachedProfileTab(() -> getProfileData(Callback));
//            return;
//        }
        AccessibilityNodeInfo profileTab = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/profile_tab", 10);
        if (profileTab == null) {
            Log.e(TAG, "profileTab not found, inside profileTab");
            if (MAX_profileTab_found_try < MAX_profileTab_found_rejections && !"ProfileLikersFollow".equals(accountManager.getCurrentAccountAutomationType())) {
                helperFunctions.navigateBack();
                this.MAX_profileTab_found_try++;
                handler.postDelayed(() -> {
                    getProfileData(Callback);
                }, 500 + random.nextInt(500));
                return;
            }
            this.MAX_profileTab_found_try = 0;
            helperFunctions.cleanupAndExit("Automation Could not be Completed Please make sure The Device has Accessibility enabled.", "error");
            return;
        }
        this.MAX_profileTab_found_try = 0;
        profileTab.performAction(AccessibilityNodeInfo.ACTION_CLICK);

        handler.postDelayed(() -> performScrollDown(() -> {
            AccessibilityNodeInfo rootNode = helperFunctions.getRootInActiveWindow();
            AccessibilityNodeInfo FollowersCount = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/row_profile_header_textview_followers_count");
            if (FollowersCount != null && FollowersCount.getText() != null) {
                this.noOfFollowers = FollowersCount.getText().toString();
            } else {
                FollowersCount = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/profile_header_familiar_followers_value");
                if (FollowersCount != null && FollowersCount.getText() != null) {
                    this.noOfFollowers = FollowersCount.getText().toString();
                }
            }
            AccessibilityNodeInfo FollowingCount = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/row_profile_header_textview_following_count");
            if (FollowingCount != null && FollowingCount.getText() != null) {
                this.noOfFollowings = FollowingCount.getText().toString();
            } else {
                FollowingCount = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/profile_header_familiar_following_value");
                if (FollowingCount != null && FollowingCount.getText() != null) {
                    this.noOfFollowings = FollowingCount.getText().toString();
                }
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            this.endTime = dateFormat.format(new Date());
            if ("NotificationSuggestion".equals(accountManager.getCurrentAccountAutomationType()) || "ProfileSuggestion".equals(accountManager.getCurrentAccountAutomationType()) || "ProfileLikersFollow".equals(accountManager.getCurrentAccountAutomationType()) || "FollowProfilePostsLikers".equals(accountManager.getCurrentAccountAutomationType())) {
                returnMessageBuilder.append("\n----------------------------\n")
                        .append("Automation Type:  ").append(this.getAutmationMethod())
                        .append("\nAccount Username:  ").append(accountManager.getCurrentUsername())
                        .append("\nAccount Actions Blocked: ").append(accountManager.getAccountLimitHit())
                        .append("\nno. of Follow Made:  ").append(accountManager.getFollowsDone())
                        .append("\nno. of Follow Requests Made:  ").append(accountManager.getRequestsMade())
                        .append("\nno. of Followers:  ").append(this.noOfFollowers)
                        .append("\nno. of Followings:  ").append(this.noOfFollowings)
                        .append("\nno. of Follow Requests:  ").append(this.FollowRequests)
                        .append("\nChats Notifications:  ").append(this.ChatData)
                        .append("\n----------------------------\n")
                        .append("\n\n");
            } else if ("unFollow".equals(accountManager.getCurrentAccountAutomationType())) {
                returnMessageBuilder.append("\n----------------------------\n")
                        .append("Automation Type:  ").append(this.getAutmationMethod())
                        .append("\nAccount Username:  ").append(accountManager.getCurrentUsername())
                        .append("\nAccount Actions Blocked: ").append(accountManager.getAccountLimitHit())
                        .append("\nno. of UnFollowed Accounts:  ").append(accountManager.getFollowsDone())
                        .append("\nno. of Followers:  ").append(this.noOfFollowers)
                        .append("\nno. of Followings:  ").append(this.noOfFollowings)
                        .append("\nno. of Follow Requests:  ").append(this.FollowRequests)
                        .append("\nChats Notifications:  ").append(this.ChatData)
                        .append("\nLast username Done:  ").append(getLastUsername(accountManager.getCurrentUsername(),accountManager.getCurrentAccountTypeOfSortForUnfollowing()))
                        .append("\n----------------------------\n")
                        .append("\n\n");
            } else if ("FollowAllRequests".equals(accountManager.getCurrentAccountAutomationType()) || "PublicToPrivate".equals(accountManager.getCurrentAccountAutomationType()) || "PrivateToPublic".equals(accountManager.getCurrentAccountAutomationType())) {
                returnMessageBuilder.append("\n----------------------------\n")
                        .append("Automation Type:  ").append(this.getAutmationMethod())
                        .append("\nAccount Username:  ").append(accountManager.getCurrentUsername())
                        .append("\nAccount Actions Blocked: ").append(accountManager.getAccountLimitHit())
                        .append("\nAccount Privacy Status:  ").append(accountManager.getAccountStatus())
                        .append("\nno. of Followers:  ").append(this.noOfFollowers)
                        .append("\nno. of Followings:  ").append(this.noOfFollowings)
                        .append("\nno. of Follow Requests:  ").append(this.FollowRequests)
                        .append("\nChats Notifications:  ").append(this.ChatData)
                        .append("\n----------------------------\n")
                        .append("\n\n");
            }
            Callback.execute();
        }), 1000 + random.nextInt(300));
    }

    public void getBoundsAndClick(AccessibilityNodeInfo node, Action Callback, String Type, int basetime, int randomTime) {
        node.refresh();
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        node.recycle();

        if (bounds.isEmpty()) {
            Log.e(TAG, "Node bounds are empty, skipping click.");
            ChangeAccount(this::callbackAccordingToType);
            return;
        }

        clickOnBounds(bounds, Callback, Type, basetime, Math.max(basetime + 1, randomTime));
    }

    private void enterProfile(Action Callback) {
        if (shouldContinueAutomation()) {
            return;
        }

        if (popUpHandler.handleOtherPopups(()->this.enterProfile(Callback), null)) return;

        AccessibilityNodeInfo rootNode = helperFunctions.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "RootNode not found in enterProfile, inside enterProfile");
            helperFunctions.cleanupAndExit("Automation Could not complete, Please make sure your Accessibility Service is Enabled", "error");
            return;
        }

        AccessibilityNodeInfo profileTab = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/profile_tab");

        if (profileTab == null) {
            Log.e(TAG, "Profile Tab not found inside enterProfile");
            ChangeAccount(this::callbackAccordingToType);
        }

        if (profileTab != null && profileTab.isClickable() && profileTab.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            handler.postDelayed(() -> {
                profileTab.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                AccessibilityNodeInfo usernameNode = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/action_bar_large_title_auto_size", 10);
                if (usernameNode == null) {
                    Log.e(TAG, "Could not find Username Node");
                    ChangeAccount(this::callbackAccordingToType);
                    return;
                }
                this.usernameToUnfollowFrom = usernameNode.getText().toString().trim();
                Log.i(TAG, "UserName to Unfollow from: " + this.usernameToUnfollowFrom);
                int chances = random.nextInt(100);
                if (chances < 0) {
                    int chances2 = random.nextInt(100);
                    rootNode.refresh();
                    if (chances2 < 50) {
                        this.instagramWarmUpFunctions.viewFollowingandFollowers(rootNode, "com.instagram.android:id/row_profile_header_textview_followers_count", "com.instagram.android:id/row_profile_header_followers_container", "com.instagram.android:id/profile_header_familiar_followers_value", "com.instagram.android:id/profile_header_followers_stacked_familiar", Callback);
                    } else {
                        this.instagramWarmUpFunctions.viewFollowingandFollowers(rootNode, "com.instagram.android:id/row_profile_header_textview_following_count", "com.instagram.android:id/row_profile_header_following_container", "com.instagram.android:id/profile_header_familiar_following_value", "com.instagram.android:id/profile_header_following_stacked_familiar", Callback);
                    }
                } else {
                    Callback.execute();
                }
            }, 1500 + random.nextInt(1000));
        } else {
            getBoundsAndClick(profileTab, this::startUnFollowingAutomation, "Center", 800, 1600);
        }
    }

    public void callbackAccordingToType() {
        Log.e(TAG, "Entered callbackAccordingToType");
        if (shouldContinueAutomation()) {
            return;
        }
        Log.e(TAG,"Current Username: "+accountManager.getCurrentUsername());
        Log.e(TAG,"Type: "+accountManager.getCurrentAccountAutomationType());
        if ("NotificationSuggestion".equals(accountManager.getCurrentAccountAutomationType())) {
            List_Id = "com.instagram.android:id/recycler_view";
            Username_Id = "com.instagram.android:id/row_recommended_user_username";
            Follow_Button_Id = "com.instagram.android:id/row_recommended_user_follow_button";
            Container_id = "com.instagram.android:id/recommended_user_row_content_identifier";
            this.enterNotificationSection(() -> {
                getfollowRequestsCount(() -> {
                    this.method1.recursivefindButtonandClick("activity_feed_see_all_row", 0, 20, this::startFollowing);
                });
            });
        } else if (("ProfileSuggestion".equals(accountManager.getCurrentAccountAutomationType()))) {
            this.enterNotificationSection(() -> {
                getfollowRequestsCount(() -> OpenSearchFeed(()->{this.ClickAndOpenSearchBar(this.method2::startFollowersAutomation);}));
            });
        } else if ("ProfileLikersFollow".equals(accountManager.getCurrentAccountAutomationType())) {
            this.enterNotificationSection(() -> {
                getfollowRequestsCount(() -> {
                    closeMyApp();
                    handler.postDelayed(() -> {
                        launchInstagramPost(
                                this.method3::StartLikesFollowing,
                                accountManager.getCurrentAccountUrl()
                        );
                    }, 40000 + random.nextInt(20000));
                });
            });
        } else if ("unFollow".equals(accountManager.getCurrentAccountAutomationType())) {
            this.enterNotificationSection(() -> getfollowRequestsCount(() -> {
                enterProfile(this::startUnFollowingAutomation);
            }));
        } else if ("FollowAllRequests".equals(accountManager.getCurrentAccountAutomationType())) {
            this.enterNotificationSection(() -> {
                getfollowRequestsCount(() -> {
                    enterProfile(this.method5::AccesptAllRequests);
                });
            });
        }  else if ("FollowProfilePostsLikers".equals(accountManager.getCurrentAccountAutomationType())) {
            this.enterNotificationSection(() -> {
                getfollowRequestsCount(() -> OpenSearchFeed(()->{this.ClickAndOpenSearchBar(this.method6::startLikersAutomation);}));
            });
        } else if ("PublicToPrivate".equals(accountManager.getCurrentAccountAutomationType())) {
            this.enterNotificationSection(() -> {
                getfollowRequestsCount(() -> {
                    enterProfile(this.method7::switchPublicToPrivate);
                });
            });
        } else if ("PrivateToPublic".equals(accountManager.getCurrentAccountAutomationType())) {
            this.enterNotificationSection(() -> {
                getfollowRequestsCount(() -> {
                    enterProfile(this.method8::switchPrivateToPublic);
                });
            });
        }

    }



    // common and main functions for methods 1, 2
    public void startFollowing() {
        Log.e(TAG, "Entered startFollowing");

        try {
            // Check if automation should continue
            if (shouldContinueAutomation()) {
                return;
            }

            // Check for action blockers
            boolean outerDialogCheck = popUpHandler.checkForActionBlocker(()->{
                accountManager.BlockCurrentAccount();
                accountManager.setAccountLimitHit(true);
                if("ProfileLikersFollow".equals(accountManager.getCurrentAccountAutomationType())){
                    this.handleNavigationByType();
                    return;
                }
                if("FollowProfilePostsLikers".equals(accountManager.getCurrentAccountAutomationType())){
                    helperFunctions.navigateBack();
                    handler.postDelayed(()->{
                        helperFunctions.navigateBack();
                        handler.postDelayed(() -> {
                            helperFunctions.navigateBack();
                            handler.postDelayed(this::handleNavigationByType, 800 + random.nextInt(800));
                        }, 800 + random.nextInt(800));

                    },400+ random.nextInt(600));
                    return;
                }
                getProfileData(() -> {
                    ChangeAccount(this::callbackAccordingToType);
                });
            });
            if (outerDialogCheck) {
                Log.e(TAG, "Outer dialog check in startFollowing is true");
                return;
            }

            if (popUpHandler.handleOtherPopups(this::startFollowing, null)) {
                Log.e(TAG, "Automation stopped due to shouldContinue flag.");
                return;
            }

            // Reset bio rejection counter
            this.bioRejectionCounter = 0;

            // Get the first node from the list
            AccessibilityNodeInfo child = getFirstNodeFromList();
            if (child == null) {
                Log.e(TAG, "No child node found, attempting static scroll up.");
                performStaticScrollUp(() -> {
                    try {
                        CheckNewNodes(this::startFollowing);
                    } catch (Exception e) {
                        Log.e(TAG, "Error during static scroll up: " + e.getMessage());
                        helperFunctions.cleanupAndExit("Failed to scroll up and find new nodes.", "error");
                    }
                });
                return;
            }

            Log.d(TAG,"found child from getFirstNodeFromList");

            // Find the user node based on the container ID
            AccessibilityNodeInfo userNode = null;
            try {
                if (Container_id.equals("com.instagram.android:id/recommended_user_card_one")) {
                    userNode = child;
                } else {
                    userNode = HelperFunctions.findNodeByResourceId(child, Container_id);
                }

                if (userNode != null) {
                    Log.i(TAG, "Found UserNode, inside startFollowing");

                    // Attempt to click the user node
                    Log.d(TAG, "Found name node, attempting click");
                    boolean isClicked = userNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                    // Recycle the user node after use
                    safelyRecycleNode(userNode);

                    if (isClicked) {
                        Log.i(TAG, "Clicked userNode through Accessibility");
                        this.tracker++;
                        Log.w(TAG, "Tracker: " + this.tracker);

                        handler.postDelayed(() -> {
                            try {
                                this.startProfileFollowing(true);
                            } catch (Exception e) {
                                Log.e(TAG, "Error during startProfileFollowing: " + e.getMessage());
                                helperFunctions.cleanupAndExit("Failed to follow profile.", "error");
                            }
                        }, 1000 + random.nextInt(1000));
                    } else {
                        Log.e(TAG, "Could not click profile through Accessibility, going to click through gestures");

                        // Click using bounds
                        android.graphics.Rect bounds = new android.graphics.Rect();
                        userNode.getBoundsInScreen(bounds);

                        clickOnBounds(bounds, () -> {
                            try {
                                this.tracker++;
                                Log.w(TAG, "Tracker: " + this.tracker);
                                Log.i(TAG, "Clicked userNode successfully through gestures");
                                this.startProfileFollowing(true);
                            } catch (Exception e) {
                                Log.e(TAG, "Error during gesture-based click: " + e.getMessage());
                                helperFunctions.cleanupAndExit("Failed to click userNode via gestures.", "error");
                            }
                        }, "Center", 1000, 2000);
                    }
                } else {
                    Log.i(TAG, "UserNode not found, inside startFollowing");
                    startFollowing(); // Retry recursively
                }
            } finally {
                // Ensure child node is recycled
                safelyRecycleNode(child);
            }
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in startFollowing: " + e.getMessage());
            helperFunctions.cleanupAndExit("An unexpected error occurred during automation.", "error");
        }
    }

    public void startProfileFollowing(boolean isFirst) {
        Log.i(TAG, "Entered startProfileFollowing");

        try {
            // Check if automation should continue
            if (shouldContinueAutomation()) {
                return;
            }

            // Handle tracker == 0 case
            if (this.tracker == 0) {
                Log.e(TAG, "startProfileFollowing called with tracker=0, returning");
                this.bioRejectionCounter = 0;
                startFollowing();
                return;
            }


            // Check for action blockers
            boolean outerDialogCheck = popUpHandler.checkForActionBlocker(()->{
                accountManager.BlockCurrentAccount();
                accountManager.setAccountLimitHit(true);

                if("ProfileLikersFollow".equals(accountManager.getCurrentAccountAutomationType())){
                    this.handleNavigationByType();
                    return;
                }
                if("FollowProfilePostsLikers".equals(accountManager.getCurrentAccountAutomationType())){
                    helperFunctions.navigateBack();
                    handler.postDelayed(()->{
                        helperFunctions.navigateBack();
                        handler.postDelayed(() -> {
                            helperFunctions.navigateBack();
                            handler.postDelayed(this::handleNavigationByType, 800 + random.nextInt(800));
                        }, 800 + random.nextInt(800));

                    },400+ random.nextInt(600));
                    return;
                }

                getProfileData(() -> {
                    ChangeAccount(this::callbackAccordingToType);
                });
            });
            if (outerDialogCheck) {
                Log.e(TAG, "Outer dialog check in startProfileFollowing is true");
                return;
            }

            if (popUpHandler.handleOtherPopups(this::startFollowing, null)) {
                Log.e(TAG, "Automation stopped due to shouldContinue flag.");
                return;
            }

            // Check daily follow limit
            if (accountManager.checkIsDailyFollowsDone()) {
                Log.e(TAG, "Account Daily Limit Reached");
                handler.postDelayed(() -> {
                    try {
                        if("ProfileLikersFollow".equals(accountManager.getCurrentAccountAutomationType())){
                            this.handleNavigationByType();
                            return;
                        }

                        if("FollowProfilePostsLikers".equals(accountManager.getCurrentAccountAutomationType())){
                            helperFunctions.navigateBack();
                            handler.postDelayed(()->{
                                helperFunctions.navigateBack();
                                handler.postDelayed(() -> {
                                    helperFunctions.navigateBack();
                                    handler.postDelayed(this::handleNavigationByType, 800 + random.nextInt(800));
                                }, 800 + random.nextInt(800));

                            },400+ random.nextInt(600));
                            return;
                        }

                        getProfileData(() -> {
                            ChangeAccount(this::callbackAccordingToType);
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error while handling daily limit: " + e.getMessage());
                        helperFunctions.cleanupAndExit("Failed to handle daily follow limit.", "error");
                    }
                }, 400 + random.nextInt(200));
                return;
            }

            // Check hourly follow limit
            if (accountManager.checkIsHourlyFollowsDone()) {
                Log.e(TAG, "Account per hour Limit Reached");
                if (accountManager.isAccountBlocked()) {
                    Log.e(TAG, "Account daily Limit Also Reached");
                    handler.postDelayed(() -> {
                        try {
                            if("ProfileLikersFollow".equals(accountManager.getCurrentAccountAutomationType())){
                                this.handleNavigationByType();
                                return;
                            }

                            if("FollowProfilePostsLikers".equals(accountManager.getCurrentAccountAutomationType())){
                                helperFunctions.navigateBack();
                                handler.postDelayed(()->{
                                    helperFunctions.navigateBack();
                                    handler.postDelayed(() -> {
                                        helperFunctions.navigateBack();
                                        handler.postDelayed(this::handleNavigationByType, 800 + random.nextInt(800));
                                    }, 800 + random.nextInt(800));

                                },400+ random.nextInt(600));
                                return;
                            }

                            getProfileData(() -> {
                                ChangeAccount(this::callbackAccordingToType);
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Error while handling blocked account: " + e.getMessage());
                            helperFunctions.cleanupAndExit("Failed to handle blocked account.", "error");
                        }
                    }, 400 + random.nextInt(200));
                } else {
                    if (!accountManager.isAccountBlocked()) {
                        Log.i(TAG, "Setting Timer for Current Account");
//                        accountManager.setTimer();
//                        int sleepTime = this.minSleepTime + random.nextInt(this.maxSleepTime - this.minSleepTime + 30000);
//                        Log.i(TAG, "Sleep Time = " + sleepTime);
                        accountManager.setSleepTime();
                    }
                    handler.postDelayed(() -> {
                        try {
                            if("ProfileLikersFollow".equals(accountManager.getCurrentAccountAutomationType())){
                                closeMyApp();
                                handler.postDelayed(()->launchApp(()->this.ChangeAccount(this::callbackAccordingToType)), 40000 + random.nextInt(20000));
                                return;
                            }

                            if("FollowProfilePostsLikers".equals(accountManager.getCurrentAccountAutomationType())){
                                helperFunctions.navigateBack();
                                handler.postDelayed(()->{
                                    helperFunctions.navigateBack();
                                    handler.postDelayed(() -> {
                                        helperFunctions.navigateBack();
                                        handler.postDelayed(()->{
                                            helperFunctions.navigateBack();
                                            handler.postDelayed(()->{
                                                helperFunctions.navigateBack();
                                                handler.postDelayed(()->{
                                                    ChangeAccount(this::callbackAccordingToType);
                                                }, 400+ random.nextInt(600));
                                            }, 400+ random.nextInt(600));
                                        }, 400+ random.nextInt(600));
                                    }, 800 + random.nextInt(800));

                                },400+ random.nextInt(600));
                                return;
                            }
                            ChangeAccount(this::callbackAccordingToType);
                        } catch (Exception e) {
                            Log.e(TAG, "Error while changing account: " + e.getMessage());
                            helperFunctions.cleanupAndExit("Failed to change account.", "error");
                        }
                    }, 400 + random.nextInt(200));
                }
                return;
            }

            // Get root node
            AccessibilityNodeInfo rootNode = helperFunctions.getRootInActiveWindow();
            if (rootNode == null) {
                Log.e(TAG, "Root node is null");
                startFollowing();
                return;
            }

            try {
                if (isFirst) {
                    Log.i(TAG, "Going to check for new Profile");
                    handleFirstProfileVisit(rootNode);
                } else {
                    Log.i(TAG, "Going to check Suggestion list of a profile");
                    if("ProfileLikersFollow".equals(accountManager.getCurrentAccountAutomationType()) || "FollowProfilePostsLikers".equals(accountManager.getCurrentAccountAutomationType())){
                        this.handleBioRejection();
                        return;
                    }
                    handleSuggestedProfiles(rootNode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in startProfileFollowing: " + e.getMessage());
                this.bioRejectionCounter++;
                this.handleBioRejection();
            } finally {
                safelyRecycleNode(rootNode);
            }

        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in startProfileFollowing: " + e.getMessage());
            helperFunctions.cleanupAndExit("An unexpected error occurred during automation.", "error");
        }
    }

    private void handleFirstProfileVisit(AccessibilityNodeInfo rootNode) {
        Log.d(TAG, "Entered a new profile, inside handleFirstProfileVisit");
        if (shouldContinueAutomation()) {
            return;
        }

        AccessibilityNodeInfo isProfileLoaded = null;
        AccessibilityNodeInfo followButton = null;

        try {
            // Step 1: Check if the profile is loaded correctly
            isProfileLoaded = helperFunctions.FindAndReturnNodeById(FOLLOW_BUTTON_ID, 30);
            if (isProfileLoaded == null) {
                Log.e(TAG, "Profile did not load correctly");
                this.handleBioRejection();
                return;
            }

            // Step 2: Perform warm-up actions based on random chance
            int warmUpFunctionChances = random.nextInt(100);
            if (warmUpFunctionChances < 5) {
                Log.d(TAG, "Going to view Profile");
                this.instagramWarmUpFunctions.viewProfile(rootNode, this::handleFirstProfileVisit);
                return;
            } else if (warmUpFunctionChances < 10) {
                Log.d(TAG, "Going to view Posts");
                this.instagramWarmUpFunctions.viewPosts(rootNode, this::handleFirstProfileVisit);
                return;
            } else if (warmUpFunctionChances < 15) {
                Log.d(TAG, "Going to view Followers");
                this.instagramWarmUpFunctions.viewFollowingandFollowers(
                        rootNode,
                        "com.instagram.android:id/row_profile_header_textview_followers_count",
                        "com.instagram.android:id/row_profile_header_followers_container",
                        "com.instagram.android:id/profile_header_familiar_followers_value",
                        "com.instagram.android:id/profile_header_followers_stacked_familiar",
                        this::handleFirstProfileVisit
                );
                return;
            } else if (warmUpFunctionChances < 20) {
                Log.d(TAG, "Going to view Following");
                this.instagramWarmUpFunctions.viewFollowingandFollowers(
                        rootNode,
                        "com.instagram.android:id/row_profile_header_textview_following_count",
                        "com.instagram.android:id/row_profile_header_following_container",
                        "com.instagram.android:id/profile_header_familiar_following_value",
                        "com.instagram.android:id/profile_header_following_stacked_familiar",
                        this::handleFirstProfileVisit
                );
                return;
            }

            // Step 3: Check bio and mutual friends requirements
            boolean shouldFollow = checkBioAndMutualFriends(rootNode);

            if (shouldFollow) {
                Log.i(TAG, "Profile passed follow requirements");
                this.bioRejectionCounter = 0;

                // Find the follow button
                followButton = HelperFunctions.findNodeByResourceId(rootNode, FOLLOW_BUTTON_ID);
                if (followButton == null) {
                    Log.e(TAG, "Follow button not found");
                    this.bioRejectionCounter++;
                    this.handleBioRejection();
                    return;
                }

                // Handle private profile checks
                if (helperFunctions.InstagramPrivateProfileChecker(rootNode)) {
                    accountManager.IncrementRequestMade();
                    accountManager.increaseThisRunFollowRequest();
                    Log.i(TAG, "Follow request for this account made: " + accountManager.getRequestsMade());
                }

                // Attempt to click the follow button
                try {
                    if (followButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        Log.i(TAG, "Clicked on follow button directly through Accessibility");
                        accountManager.IncrementFollowsDone();
                        accountManager.increaseThisRunFollows();
                        scheduleNextAction(false, 2000);
                    } else {
                        Log.e(TAG, "Could not click on follow button directly through Accessibility, going to click through gesture");
                        Rect bounds = new Rect();
                        followButton.getBoundsInScreen(bounds);

                        clickOnBounds(bounds, () -> {
                            Log.i(TAG, "Clicked follow button through gesture");
                            accountManager.IncrementFollowsDone();
                            accountManager.increaseThisRunFollows();
                            scheduleNextAction(false, 1800);
                        }, "Center", 100, 200);
                    }
                } finally {
                    safelyRecycleNode(followButton);
                }
            } else {
                Log.i(TAG, "Profile failed follow requirements");
                this.bioRejectionCounter++;
                this.handleBioRejection();
            }

        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in handleFirstProfileVisit: " + e.getMessage());
            helperFunctions.cleanupAndExit("An unexpected error occurred while handling the first profile visit.", "error");
//            shouldContinue = false;

        } finally {
            // Safely recycle nodes
            safelyRecycleNode(isProfileLoaded);
            safelyRecycleNode(followButton);
        }
    }

    private void handleFirstProfileVisit() {
        Log.e(TAG, "Entered a new profile Again after checking dialog, inside handleFirstProfileVisit");
        if (shouldContinueAutomation()) {
            return;
        }

        AccessibilityNodeInfo rootNode = null;
        AccessibilityNodeInfo followButton = null;

        try {
            // Step 1: Get the root node
            rootNode = helperFunctions.getRootInActiveWindow();
            if (rootNode == null) {
                Log.e(TAG, "Root node is null inside handleFirstProfileVisit");
                this.handleBioRejection();
                return;
            }

            // Step 2: Check bio and mutual friends requirements
            boolean shouldFollow = checkBioAndMutualFriends(rootNode);

            if (shouldFollow) {
                Log.i(TAG, "Profile passed follow requirements");
                this.bioRejectionCounter = 0;

                // Find the follow button
                followButton = HelperFunctions.findNodeByResourceId(rootNode, FOLLOW_BUTTON_ID);
                if (followButton == null) {
                    Log.e(TAG, "Follow button not found");
                    this.bioRejectionCounter++;
                    this.handleBioRejection();
                    return;
                }

                // Handle private profile checks
                if (helperFunctions.InstagramPrivateProfileChecker(rootNode)) {
                    accountManager.IncrementRequestMade();
                    accountManager.increaseThisRunFollowRequest();
                    Log.i(TAG, "Follow request for this account made: " + accountManager.getRequestsMade());
                }

                // Attempt to click the follow button
                try {
                    if (followButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        Log.i(TAG, "Clicked on follow button directly through Accessibility");
                        accountManager.IncrementFollowsDone();
                        accountManager.increaseThisRunFollows();
                        scheduleNextAction(false, 2000);
                    } else {
                        Log.e(TAG, "Could not click on follow button directly through Accessibility, going to click through gesture");
                        Rect bounds = new Rect();
                        followButton.getBoundsInScreen(bounds);

                        clickOnBounds(bounds, () -> {
                            Log.i(TAG, "Clicked follow button through gesture");
                            accountManager.IncrementFollowsDone();
                            accountManager.increaseThisRunFollows();
                            scheduleNextAction(false, 2000);
                        }, "Center", 400, 800);
                    }
                } finally {
                    safelyRecycleNode(followButton);
                }
            } else {
                Log.i(TAG, "Profile failed follow requirements");
                this.bioRejectionCounter++;

                // Remove any pending callbacks and post a delayed rejection handler
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> {
                    try {
                        this.handleBioRejection();
                    } catch (Exception e) {
                        Log.e(TAG, "Error during bio rejection handling: " + e.getMessage());
                        helperFunctions.cleanupAndExit("Failed to handle bio rejection.", "error");
                    }
                }, 1000 + random.nextInt(1000));
            }

        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in handleFirstProfileVisit: " + e.getMessage());
            helperFunctions.cleanupAndExit("An unexpected error occurred while handling the first profile visit.", "error");
//            shouldContinue = false;

        } finally {
            // Safely recycle nodes
            safelyRecycleNode(rootNode);
        }
    }

    private void handleBioRejection() {
        Log.e(TAG, "Entered handleBioRejection");
        if (shouldContinueAutomation()) {
            return;
        }
        Log.d(TAG, "Current tracker: " + this.tracker + ", Bio rejections: " + bioRejectionCounter);

        if (this.tracker == 1) {
            Log.e(TAG, "Moving back to main list of users from new profile after Rejection");
            this.tracker--;
            Log.i(TAG, "Tracker: " + this.tracker);
            helperFunctions.navigateBack();
            handler.postDelayed(() -> {
                if (this.tracker == 0) {
                    startFollowing();
                }
            }, 500 + random.nextInt(500));

        } else if (this.tracker > 1) {
            if (this.bioRejectionCounter >= MAX_BIO_REJECTIONS) {
                Log.e(TAG, "Moving two steps back after bioRejectionCounter got to 3");
                this.bioRejectionCounter = 0;
                decrementTrackerAndNavigate();
            } else {
                Log.e(TAG, "Moving back after and continuing to suggestion carousel");
                this.tracker--;
                helperFunctions.navigateBack();
                scheduleNextAction(false, 400);
            }
        }
    }

    private void decrementTrackerAndNavigate() {
        Log.i(TAG, "Entered decrementTrackerAndNavigate");
        if (shouldContinueAutomation()) {
            return;
        }

        this.tracker--;
        Log.w(TAG, "Tracker: " + this.tracker);
        helperFunctions.navigateBack();
        lastProfileNodes.pop();

        handler.postDelayed(() -> {
            this.tracker--;
            Log.w(TAG, "Tracker: " + this.tracker);
            helperFunctions.navigateBack();

            if (tracker > 0) {
                scheduleNextAction(false, BASE_DELAY);
            } else {
                scheduleFollowing();
            }
        }, 600 + random.nextInt(400));
    }

    private void handleSuggestedProfiles(AccessibilityNodeInfo rootNode) {
        Log.i(TAG, "Entered handleSuggestedProfiles");
        if (shouldContinueAutomation()) {
            return;
        }

        AccessibilityNodeInfo suggestionList = null;
        AccessibilityNodeInfo suggestionChainingButton = null;
        AccessibilityNodeInfo firstNode = null;
        AccessibilityNodeInfo usernameNode = null;

        try {
            // Step 1: Find the suggestion list or chaining button
            suggestionList = helperFunctions.FindAndReturnNodeById(CAROUSEL_VIEW_ID, 8);
            if (suggestionList == null) {
                Log.e(TAG, "Suggestion list not found, attempting to find suggestion chaining button");
                suggestionChainingButton = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/row_profile_header_button_chaining");
                if (suggestionChainingButton == null) {
                    Log.e(TAG, "Suggestion chaining button not found");
                    this.handleBioRejection();
                    return;
                }

                // Attempt to click the suggestion chaining button
                boolean isClicked = suggestionChainingButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                if (!isClicked) {
                    Log.e(TAG, "Failed to click suggestion chaining button");
                    this.handleBioRejection();
                    return;
                }

                // Retry finding the suggestion list after clicking the chaining button
                suggestionList = helperFunctions.FindAndReturnNodeById(CAROUSEL_VIEW_ID, 5);
                if (suggestionList == null) {
                    Log.e(TAG, "Suggestion list still not found after clicking chaining button");
                    this.handleBioRejection();
                    return;
                }
            }

            // Step 2: Check if the suggestion list has children
            if (suggestionList != null && suggestionList.getChildCount() > 0) {
                try {
                    firstNode = suggestionList.getChild(0);
                    if (firstNode == null) {
                        Log.e(TAG, "First node is null inside suggestion list");
                        this.handleBioRejection();
                        return;
                    }

                    Log.d(TAG, "Found first node in handleSuggestedProfiles");

                    // Step 3: Find the username node
                    usernameNode = HelperFunctions.findNodeByResourceId(firstNode, CARD_CONTAINER_username);
                    if (usernameNode != null) {
                        CharSequence usernameText = usernameNode.getText();
                        if (usernameText == null) {
                            Log.e(TAG, "Username text is null");
                            dismissProfile(firstNode);
                            return;
                        }

                        String username = usernameText.toString();
                        Log.d(TAG, "Found first node username: " + username);

                        // Step 4: Check if the username has already been processed
                        if (accountManager.checkIsUserDone(username)) {
                            Log.d(TAG, "Username " + username + " already processed, dismissing profile");
                            dismissProfile(firstNode);
                            return;
                        }

                        accountManager.addUserDone(username);

                        // Step 5: Handle last profile nodes
                        if (!lastProfileNodes.isEmpty()) {
                            Log.d(TAG, "lastProfileNodes is not empty in handleSuggestedProfiles");
                            String lastName = lastProfileNodes.pop();
                            Log.d(TAG, "Last username in lastProfileNodes: " + lastName);

                            if (username.equals(lastName)) {
                                Log.d(TAG, "Profile matched in lastProfileNodes, dismissing it");
                                dismissProfile(firstNode);
                            } else {
                                Log.d(TAG, "Profile not matched, pushing back lastName and username");
                                lastProfileNodes.push(lastName);
                                lastProfileNodes.push(username);
                                processSuggestedProfile(firstNode);
                            }
                        } else {
                            Log.d(TAG, "lastProfileNodes is empty in handleSuggestedProfiles");
                            lastProfileNodes.push(username);
                            processSuggestedProfile(firstNode);
                        }
                    } else {
                        Log.e(TAG, "Username node is null, going to dismiss profile from suggestion list");
                        dismissProfile(firstNode);
                    }
                } finally {
                    safelyRecycleNode(firstNode);
                    safelyRecycleNode(usernameNode);
                }
            } else {
                Log.e(TAG, suggestionList == null ? "Suggestion list is null" : "Suggestion list has no children");
                this.handleBioRejection();
            }

        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in handleSuggestedProfiles: " + e.getMessage());
            helperFunctions.cleanupAndExit("An unexpected error occurred while handling suggested profiles.", "error");
//            shouldContinue = false;

        } finally {
            // Safely recycle nodes
            safelyRecycleNode(suggestionList);
            safelyRecycleNode(suggestionChainingButton);
        }
    }

    private void processSuggestedProfile(AccessibilityNodeInfo profileNode) {
        Log.i(TAG, "Entered processSuggestedProfile");
        if (shouldContinueAutomation()) {
            return;
        }

        AccessibilityNodeInfo cardContainer = null;
        AccessibilityNodeInfo usernameNode = null;

        try {
            // Step 1: Refresh the profile node to ensure it's up-to-date
            if (profileNode == null || !profileNode.refresh()) {
                Log.e(TAG, "Failed to refresh profileNode");
                lastProfileNodes.pop();
                dismissProfile(profileNode);
                return;
            }

            // Step 2: Find the card container
            cardContainer = HelperFunctions.findNodeByResourceId(profileNode, CARD_CONTAINER_ID);
            if (cardContainer == null) {
                Log.e(TAG, "Card container not found inside processSuggestedProfile");
                lastProfileNodes.pop();
                dismissProfile(profileNode);
                return;
            }

            // Step 3: Attempt to click the card container
            try {
                if (cardContainer.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    Log.i(TAG, "Clicked on profile node successfully through accessibility inside processSuggestedProfile");
                    this.tracker++;
                    Log.w(TAG, "Tracker: " + this.tracker);

                    handler.postDelayed(() -> {
                        try {
                            startProfileFollowing(true);
                        } catch (Exception e) {
                            Log.e(TAG, "Error during startProfileFollowing: " + e.getMessage());
                            helperFunctions.cleanupAndExit("Failed to follow profile.", "error");
                        }
                    }, 1000 + random.nextInt(500));
                } else {
                    Log.e(TAG, "Could not click on profile node successfully through accessibility inside processSuggestedProfile, going to click through gesture");

                    // Step 4: Fallback to clicking via gestures using the username node
                    usernameNode = HelperFunctions.findNodeByResourceId(profileNode, CARD_CONTAINER_username);
                    if (usernameNode == null) {
                        Log.e(TAG, "Username node not found, dismissing profile");
                        lastProfileNodes.pop();
                        dismissProfile(profileNode);
                        return;
                    }

                    this.tracker++;
                    Log.w(TAG, "Tracker: " + this.tracker);

                    Rect bounds = new Rect();
                    usernameNode.getBoundsInScreen(bounds);

                    clickOnBounds(bounds, () -> {
                        try {
                            Log.i(TAG, "Clicked on profile node successfully through gesture");
                            startProfileFollowing(true);
                        } catch (Exception e) {
                            Log.e(TAG, "Error during gesture-based click: " + e.getMessage());
                            helperFunctions.cleanupAndExit("Failed to follow profile via gesture.", "error");
                        }
                    }, "Center", 1000, 1500);
                }
            } finally {
                safelyRecycleNode(cardContainer);
                safelyRecycleNode(usernameNode);
            }

        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in processSuggestedProfile: " + e.getMessage());
            helperFunctions.cleanupAndExit("An unexpected error occurred while processing suggested profile.", "error");
//            shouldContinue = false;

        } finally {
            // Safely recycle the profile node
            safelyRecycleNode(profileNode);
        }
    }

    private void dismissProfile(AccessibilityNodeInfo profileNode) {
        Log.i(TAG, "Entered dismissProfile");
        if (shouldContinueAutomation()) {
            return;
        }

        AccessibilityNodeInfo dismissButton = null;

        try {
            // Step 1: Refresh the profile node to ensure it's up-to-date
            if (profileNode == null || !profileNode.refresh()) {
                Log.e(TAG, "Failed to refresh profileNode");
                this.handleBioRejection();
                return;
            }

            // Step 2: Find the dismiss button
            dismissButton = HelperFunctions.findNodeByResourceId(profileNode, DISMISS_BUTTON_ID);
            if (dismissButton == null) {
                Log.e(TAG, "Dismiss button not found inside dismissProfile");
                this.handleBioRejection();
                return;
            }

            // Step 3: Attempt to click the dismiss button
            try {
                if (dismissButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    Log.i(TAG, "Clicked dismiss button successfully through Accessibility");
                    scheduleNextAction(false, 500);
                } else {
                    Log.e(TAG, "Could not click dismiss button through Accessibility, going to click through bounds");

                    Rect bounds = new Rect();
                    dismissButton.getBoundsInScreen(bounds);

                    clickOnBounds(bounds, () -> {
                        try {
                            Log.i(TAG, "Successfully clicked on dismiss button through gesture");
                            scheduleNextAction(false, 500);
                        } catch (Exception e) {
                            Log.e(TAG, "Error during gesture-based click: " + e.getMessage());
                            helperFunctions.cleanupAndExit("Failed to dismiss profile via gesture.", "error");
                        }
                    }, "Center", 150, 300);
                }
            } finally {
                safelyRecycleNode(dismissButton);
            }

        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in dismissProfile: " + e.getMessage());
            helperFunctions.cleanupAndExit("An unexpected error occurred while dismissing the profile.", "error");
//            shouldContinue = false;

        } finally {
            // Safely recycle the profile node
            safelyRecycleNode(profileNode);
        }
    }

    private void scheduleFollowing() {
        if (shouldContinueAutomation()) {
            return;
        }
        int delay = BASE_DELAY + random.nextInt(RANDOM_DELAY);
        handler.postDelayed(this::startFollowing, delay);
    }


    public boolean shouldContinueAutomation() {
        Log.e(TAG, "Entered shouldContinueAutomation");
        if (this.shouldStop) {
            Log.e(TAG, "Automation Stoped By Command");
            this.shouldStop = false;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            this.endTime = dateFormat.format(new Date());
            returnMessageBuilder.append("End Time:  ").append(this.endTime).append("\n");
            helperFunctions.cleanupAndExit("Automation Stoped", "error");
            return true;
        }
        return false;
    }
}