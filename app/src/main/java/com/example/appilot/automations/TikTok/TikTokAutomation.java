package com.example.appilot.automations.TikTok;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;
import com.example.appilot.services.MyAccessibilityService;
import com.example.appilot.utils.HelperFunctions;
import java.util.List;
import java.util.Random;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TikTokAutomation {
    private static final String TAG = "TikTokAutomation";
    private static final String TIKTOK_PACKAGE = "com.zhiliaoapp.musically";
    private final Context context;
    private final Handler handler;
    private final Random random;
    private final PopUpHandler popUpHandler;
    private final MyAccessibilityService service;
    private HelperFunctions helperFunctions;
    private String Task_id = null;
    private String job_id = null;
    private List<Object> AccountInputs;
    private int duration;
    private Scrolling scrolling;
    private LikeTikTokReels likeReels;

    public TikTokAutomation(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs, int duration) {
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
        Log.d(TAG, "Launching app: " + TIKTOK_PACKAGE);
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(TIKTOK_PACKAGE);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
            handler.postDelayed(() -> {
                Log.d(TAG, "Delay before the scroll down");
                startAutomation();
//                handler.postDelayed(()->{
//                    helperFunctions.cleanupAndExit("Launch the app now exiting", "final");
//                },2000);
            }, 5000);
        } else {
            Log.e(TAG, "Could not launch app: " + TIKTOK_PACKAGE);
            launchInstagramExplicitly();
        }
    }

    private void launchInstagramExplicitly() {
        Log.d(TAG, "Entered launchTikTokExplicitly.");
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse("https://www.tiktok.com/"))
                .setPackage(TIKTOK_PACKAGE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            handler.postDelayed(() -> {
                Log.d(TAG, "Adding delay before the scroll");
                startAutomation();
//                handler.postDelayed(()->{
//                    helperFunctions.cleanupAndExit("Launch the app now exiting", "final");
//                },2000);
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
        Log.d(TAG, "=== Starting TikTok Automation ===");
        Log.d(TAG, "Extracting toggle states from converted JSONArray...");

        try {
            // Initialize variables for TikTok bot features
            boolean scrollingDown = false;
            boolean likeTheReels = false;

            // Extract toggle states from the input data (already converted from JSONArray)
            if (AccountInputs != null && !AccountInputs.isEmpty()) {
                Log.d(TAG, "Processing " + AccountInputs.size() + " input objects from converted JSONArray...");

                for (Object inputObj : AccountInputs) {
                    if (inputObj instanceof JSONObject) {
                        JSONObject inputSection = (JSONObject) inputObj;
                        Log.d(TAG, "Processing input section: " + inputSection.toString());

                        // Check if this JSONObject has "Connections" key directly
                        if (inputSection.has("TikTok")) {
                            Log.d(TAG, "Found 'TikTok' key, extracting toggle states...");

                            // Extract the "Connections" array
                            JSONArray connectionsArray = inputSection.optJSONArray("TikTok");
                            if (connectionsArray != null) {
                                Log.d(TAG, "Found " + connectionsArray.length() + " toggles in TikTok array");

                                // Iterate through the connections array to get individual toggles
                                for (int i = 0; i < connectionsArray.length(); i++) {
                                    JSONObject connectionObject = connectionsArray.getJSONObject(i);
                                    Log.d(TAG, "Processing TikTok object [" + i + "]: " + connectionObject.toString());

                                    // Extract TikTok specific toggles
                                    if (connectionObject.has("Scrolling")) {
                                        scrollingDown = connectionObject.optBoolean("Scrolling", false);
                                        Log.d(TAG, "✓ Scrolling: " + scrollingDown);
                                    }

                                    if (connectionObject.has("Like the reels")) {
                                        likeTheReels = connectionObject.optBoolean("Like the reels", false);
                                        Log.d(TAG, "✓ Like the reels: " + likeTheReels);
                                    }
                                }
                            } else {
                                Log.e(TAG, "TikTok key found but value is not an array");
                            }
                            break; // Once Connections are found, no need to continue further
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
            Log.d(TAG, "=== Final TikTok Toggle States ===");
            Log.d(TAG, "Scrolling: " + scrollingDown);
            Log.d(TAG, "Like the reels: " + likeTheReels);
            Log.d(TAG, "Duration: " + duration);
            Log.d(TAG, "=====================================");

            // Create the TikTok automation objects and pass the extracted values
            this.scrolling = new Scrolling(service, Task_id, job_id, AccountInputs, duration);
            this.likeReels = new LikeTikTokReels(service, Task_id, job_id, AccountInputs, duration);
            // Route to the appropriate function based on toggle states
            if (scrollingDown && !likeTheReels) {
                Log.d(TAG, "🎯 Routing to: Scrolling - Scrolls Only");
                scrolling.onScroll();

            } else if (likeTheReels && !scrollingDown) {
                Log.d(TAG, "🎯 Routing to: Like Reels - Like Posts Only");
                likeReels.findLikeButton();

            } else {
                Log.d(TAG, "🎯 No automation selected - Using default: ReelLiker");
                helperFunctions.cleanupAndExit("Doesn't find any automation", "error");
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
}