package com.example.appilot.automations.LinkedIn;

import android.content.Intent;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;
import android.content.Context;
import android.os.Handler;
import java.util.List;
import java.util.Random;
import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;
import com.example.appilot.services.MyAccessibilityService;
import com.example.appilot.utils.HelperFunctions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LinkedInAutomation {
    private static final String TAG = "LinkedInAutomation";
    private static final String LINKEDIN_PACKAGE = "com.linkedin.android";
    private final Context context;
    private final Handler handler;
    private final Random random;
    private final PopUpHandler popUpHandler;
    private final MyAccessibilityService service;
    private HelperFunctions helperFunctions;
    private LikeComment likeComment;
    private Connection connection;
    private AcceptConnection acceptConnection;
    private Notification notification;
    private Profile profile;
    private String Task_id = null;
    private String job_id = null;
    private List<Object> AccountInputs;
    private boolean sendConnectionRequests = false;
    private boolean likePosts = false;
    private boolean acceptRequest = false;
    private boolean notifications = false;
    private boolean Profile = false;
    private boolean isConnectionProcessComplete = false;
    private int numberOfConnections;

    public LinkedInAutomation(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs) {
        this.context = service;
        this.service = service;
        this.Task_id = taskid;
        this.job_id = jobid;
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();
        this.popUpHandler = new PopUpHandler(this.service, this.handler, this.random, this.helperFunctions);
        this.helperFunctions = new HelperFunctions(context, Task_id, job_id);
        this.AccountInputs = AccountInputs;
    }

    public void launchApp() {
        Log.d(TAG, "Launching app: " + LINKEDIN_PACKAGE);
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(LINKEDIN_PACKAGE);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
            handler.postDelayed(() -> {
                Log.d(TAG, "Delay before the scroll down");
                helperFunctions.performScroll(0.3f, 0.7f);
                handler.postDelayed(this::startAutomation, 2000);
            }, 5000);
        } else {
            Log.e(TAG, "Could not launch app: " + LINKEDIN_PACKAGE);
            launchInstagramExplicitly();
        }
    }
    private void launchInstagramExplicitly() {
        Log.d(TAG, "Entered launchInstagramExplicitly.");
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse("https://www.linkedin.com/"))
                .setPackage(LINKEDIN_PACKAGE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            handler.postDelayed(() -> {
                Log.d(TAG, "Adding delay before the scroll");
                helperFunctions.performScroll(0.3f,0.7f);
                handler.postDelayed(this::startAutomation, 2000);
            }, 5000);

        } catch (Exception e) {
            Log.e(TAG, "Failed to launch LinkedIn", e);
            handler.postDelayed(() -> {
                Log.d(TAG, "Could not launch the LinkedIn");
                helperFunctions.cleanupAndExit("Couldn't Launch LinkedIn", "error");
            }, 2000);
        }
    }
    private void startAutomation() {
        Log.d(TAG, "=== Starting Automation ===");
        Log.d(TAG, "Extracting toggle states from converted JSONArray...");
        try {
            // Extract toggle states from the input data (already converted from JSONArray)
            if (AccountInputs != null && !AccountInputs.isEmpty()) {
                Log.d(TAG, "Processing " + AccountInputs.size() + " input objects from converted JSONArray...");

                for (Object inputObj : AccountInputs) {
                    if (inputObj instanceof JSONObject) {
                        JSONObject inputSection = (JSONObject) inputObj;
                        Log.d(TAG, "Processing input section: " + inputSection.toString());

                        // Check if this JSONObject has "Connections" key directly
                        if (inputSection.has("Connections")) {
                            Log.d(TAG, "Found 'Connections' key, extracting toggle states...");

                            // Extract the "Connections" array
                            JSONArray connectionsArray = inputSection.optJSONArray("Connections");
                            if (connectionsArray != null) {
                                Log.d(TAG, "Found " + connectionsArray.length() + " toggles in Connections array");

                                // Iterate through the connections array to get individual toggles
                                for (int i = 0; i < connectionsArray.length(); i++) {
                                    JSONObject connectionObject = connectionsArray.getJSONObject(i);
                                    Log.d(TAG, "Processing connection object [" + i + "]: " + connectionObject.toString());

                                    // Check if the current object has the numberOfConnections field
                                    if (connectionObject.has("Send Connection Requests")) {
                                        sendConnectionRequests = connectionObject.optBoolean("Send Connection Requests", false);
                                        Log.d(TAG, "✓ Send Connection Requests: " + sendConnectionRequests);

                                        if (sendConnectionRequests && connectionObject.has("numberOfConnections")) {
                                            // Extract the number of connections
                                            numberOfConnections = connectionObject.optInt("numberOfConnections", 0);
                                            Log.d(TAG, "✓ Number of Connections: " + numberOfConnections);
                                        }
                                    }

                                    if (connectionObject.has("Like the posts")) {
                                        likePosts = connectionObject.optBoolean("Like the posts", false);
                                        Log.d(TAG, "✓ Like the posts: " + likePosts);
                                    }

                                    if (connectionObject.has("Accept Requests")) {
                                        acceptRequest = connectionObject.optBoolean("Accept Requests", false);
                                        Log.d(TAG, "✓ Accept Requests: " + acceptRequest);

                                        if (acceptRequest && connectionObject.has("numberOfConnections")) {
                                            // Extract the number of connections
                                            numberOfConnections = connectionObject.optInt("numberOfConnections", 0);
                                            Log.d(TAG, "✓ Number of Connections: " + numberOfConnections);
                                        }
                                    }

                                    if (connectionObject.has("Notifications")) {
                                        notifications = connectionObject.optBoolean("Notifications", false);
                                        Log.d(TAG, "✓ Notifications: " + notifications);
                                    }

                                    if (connectionObject.has("Open Profile")) {
                                        Profile = connectionObject.optBoolean("Open Profile", false);
                                        Log.d(TAG, "✓ Open Profile: " + Profile);
                                    }
                                }
                            } else {
                                Log.e(TAG, "Connections key found but value is not an array");
                            }
                            break; // Once Connections are found, no need to continue further
                        } else {
                            Log.d(TAG, "No 'Connections' key found in this section.");
                        }
                    }
                }
            } else {
                Log.e(TAG, "No AccountInputs provided or list is empty");
                // Default to connection requests
                sendConnectionRequests = true;
                Log.d(TAG, "Using default configuration: Send Connection Requests = true");
            }
            // Log the final extracted states
            Log.d(TAG, "=== Final Toggle States ===");
            Log.d(TAG, "Send Connection Requests: " + sendConnectionRequests);
            Log.d(TAG, "Like the posts: " + likePosts);
            Log.d(TAG, "Number of Connections: " + numberOfConnections);
            Log.d(TAG, "Accept Connection: " + acceptRequest);
            Log.d(TAG, "Notifications: " + notifications);
            Log.d(TAG, "Profile: " + Profile);
            Log.d(TAG, "===========================");

            // Create the Connection object and pass the extracted values
            this.connection = new Connection(service, Task_id, job_id, AccountInputs, sendConnectionRequests, likePosts, numberOfConnections);
            this.likeComment = new LikeComment(service, Task_id, job_id, AccountInputs);
            this.acceptConnection = new AcceptConnection(service, Task_id, job_id, AccountInputs, numberOfConnections);
            this.notification = new Notification(service, Task_id, job_id, AccountInputs);
            this.profile = new Profile(service, Task_id, job_id, AccountInputs);

            // Route to the appropriate function based on toggle states
            if (sendConnectionRequests && !likePosts) {
                Log.d(TAG, "🎯 Routing to: sendConnection() - Connection Requests Only");
                connection.sendConnection();

            } else if (likePosts && !sendConnectionRequests) {
                Log.d(TAG, "🎯 Routing to: likeComment.findResourceId() - Like Posts Only");
                likeComment.startLiking();

            } else if (sendConnectionRequests && likePosts) {
                Log.d(TAG, "🎯 Both toggles ON - Starting with Connection Requests first");
                Log.d(TAG, "Like Posts will be triggered after Connection Requests complete");
                connection.sendConnection(); // Start with connections first

            } else if (acceptRequest) {
                Log.d(TAG, "🎯 Routing to:  - Accept connection requests Only");
                acceptConnection.acceptRequest();

            } else if (notifications) {
                Log.d(TAG, "🎯 Routing to:  - Accept notification only");
                notification.seeNotification();

            } else if (Profile) {
                Log.d(TAG, "🎯 Routing to:  - Accept Profile only");
                profile.findProfileOnHome();

            } else {
                Log.d(TAG, "🎯 No automation selected - Using default: sendConnection()");
                helperFunctions.cleanupAndExit("No input found", "error");
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException in startAutomation: " + e.getMessage());
            e.printStackTrace();
            // Default to connection requests on JSON error
            Log.d(TAG, "JSON error occurred, defaulting to sendConnection()");
            helperFunctions.cleanupAndExit("No input found as a JSON", "error");
        } catch (Exception e) {
            Log.e(TAG, "General Exception in startAutomation: " + e.getMessage());
            e.printStackTrace();
            // Default to connection requests on any error
            Log.d(TAG, "General error occurred, defaulting to sendConnection()");
            helperFunctions.cleanupAndExit("No input found in general exception", "error");
        }
    }
}
