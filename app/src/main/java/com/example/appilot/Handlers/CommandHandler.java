package com.example.appilot.Handlers;

import android.util.Log;

import com.example.appilot.automations.InstagramFollowerBot.InstagramFollowerBotAutomation;
import com.example.appilot.automations.TikTok.TikTokAutomation;
import com.example.appilot.automations.linkedInConnectionBot.linkedInConnectionBotAutomation;
import com.example.appilot.automations.reddit.RedditAutomation;
import com.example.appilot.automations.Twitter.TwitterAutomation;
import com.example.appilot.services.MyAccessibilityService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;
import java.util.Locale;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
public class CommandHandler {
    private static final String TAG = "CommandHandler";
    private final MyAccessibilityService service;
    private TwitterAutomation twitterAutomation;
    private RedditAutomation redditAutomation;
    private InstagramFollowerBotAutomation instagramFollowerBotAutomation;
    private linkedInConnectionBotAutomation linkedInAutomation;
    private TikTokAutomation tikTokAutomation;
    private volatile boolean shouldAutomationStop = false;
    private Random random = new Random();
    private String currentlyRunningAutomation = null;

    public CommandHandler(MyAccessibilityService service) {
        this.service = service;
    }

    private void stopAllAutomations() {
        Log.d(TAG, "Stopping all automations");
        shouldAutomationStop = true;

        // Stop Reddit automation if running
        if (redditAutomation != null) {
            // Assuming RedditAutomation has a method to stop it
//            redditAutomation.shouldStop;
            redditAutomation = null;
        }

        // Stop Instagram automation if running
        if (instagramFollowerBotAutomation != null) {
            // Assuming InstagramFollowerBotAutomation has a method to stop it
            instagramFollowerBotAutomation.shouldStop = true;
            instagramFollowerBotAutomation = null;
        }

        // Reset the currently running automation tracker
        currentlyRunningAutomation = null;
    }

    public void executeAutomation(String command) {
        String duration = null;
        JSONArray inputsArray;

        try {
            // Parse the command as a JSON object
            JSONObject commandJson = new JSONObject(command);
            Log.d(TAG, "Start Automation Command Received after JSON conversion: " + command);
            String appName = commandJson.getString("appName");
            duration = commandJson.optString("duration", "0");
            int durationInt = Integer.parseInt(duration);
            String Task_Id = commandJson.optString("task_id", null);
            String job_id = commandJson.optString("job_id", null);

            switch (appName.toLowerCase()) {
                case "stop automation":
                    Log.e(TAG, "Stopping automation");
                    stopAllAutomations();
                    break;

                case "reddit karma bot":
                    // Set the currently running automation
                    currentlyRunningAutomation = "reddit";

                    JSONArray scrollingInputs = null;
                    JSONArray voteInputs = null;
                    JSONArray commentInputs = null;
                    JSONArray userType = null;
                    boolean isvoting = false;
                    boolean isCommenting = false;
                    int maxUpvote = 0;
                    int maxComment = 0;
                    long upvoteDuration = 0;
                    long commentDuration = 0;

                    inputsArray = commandJson.getJSONArray("inputs");
                    for (int i = 0; i < inputsArray.length(); i++) {
                        JSONObject inputObject = inputsArray.getJSONObject(i);
                        if (inputObject.has("Scrolling")) {
                            scrollingInputs = inputObject.getJSONArray("Scrolling");
                        } else if (inputObject.has("Upvote or Downvote a Random Post")) {
                            voteInputs = inputObject.getJSONArray("Upvote or Downvote a Random Post");
                            isvoting = voteInputs.getJSONObject(0).getBoolean("Quick Upvote");
                        } else if (inputObject.has("Comment on a Random Post")) {
                            commentInputs = inputObject.getJSONArray("Comment on a Random Post");
                            isCommenting = commentInputs.getJSONObject(0).getBoolean("Quick Comment") || commentInputs.getJSONObject(1).getBoolean("Detailed Response") || commentInputs.getJSONObject(2).getBoolean("Reply to Comment")? true:false;
                        }  else if (inputObject.has("User Interaction Speed")) {
                            userType = inputObject.getJSONArray("User Interaction Speed");
                        }
                    }

                    if (durationInt < 1) {
                        Log.d(TAG, "Insufficient time for automation: " + durationInt);
                        break;
                    }

                    if (scrollingInputs != null || voteInputs != null || commentInputs != null || userType != null) {
                        Log.d(TAG, "Reddit automation will run for duration: " + durationInt);
                        Log.d(TAG, "Starting Reddit automation with Scrolling: " + scrollingInputs);
                        Log.d(TAG, "Starting Reddit automation with upvoting: " + voteInputs);
                        Log.d(TAG, "Starting Reddit automation with commenting: " + commentInputs);
                        Log.d(TAG, "Starting Reddit automation with User Type: " + userType);
                        if(isvoting || isCommenting ){
                            if (userType.getJSONObject(0).getBoolean("Normal User")) {
                                if (durationInt > 0 && durationInt <= 60) {
                                    if(isvoting){
                                        upvoteDuration = 120000 + random.nextInt(360000);
                                        maxUpvote = 1;
                                    }
                                    if(isCommenting){
                                        commentDuration = 720000 + random.nextInt(1800000);
                                        maxComment = 1;
                                    }
                                } else if (durationInt > 60 && durationInt <= 1440) {
                                    if(isvoting){
                                        upvoteDuration = 3600000;
                                        maxUpvote = 10 + random.nextInt(15);
                                    }
                                    if(isCommenting){
                                        commentDuration = 3600000;
                                        maxComment = 2 + random.nextInt(3);
                                    }
                                } else if (durationInt > 1440) {
                                    if(isvoting){
                                        upvoteDuration = 3600000 + random.nextInt(7200000);
                                        maxUpvote = 20 + random.nextInt(30);
                                    }
                                    if(isCommenting){
                                        commentDuration = 3600000 + random.nextInt(7200000);
                                        maxComment = 2 + random.nextInt(8);
                                    }
                                }
                            } else if (userType.getJSONObject(1).getBoolean("Extensive User")) {
                                if (durationInt > 0 && durationInt <= 60) {
                                    if(isvoting){
                                        upvoteDuration = 60000 + random.nextInt(120000);
                                        maxUpvote = 1;
                                    }
                                    if(isCommenting){
                                        commentDuration = 360000 + random.nextInt(720000);
                                        maxComment = 1;
                                    }
                                } else if (durationInt > 60 && durationInt <= 1440) {
                                    if(isvoting){
                                        upvoteDuration = 3600000;
                                        maxUpvote = 30 + random.nextInt(20);
                                    }
                                    if(isCommenting){
                                        commentDuration = 3600000;
                                        maxComment = 5 + random.nextInt(5);
                                    }
                                } else if (durationInt > 1440) {
                                    if(isvoting){
                                        upvoteDuration = 180000 + random.nextInt(300000);
                                        maxUpvote = 100 + random.nextInt(150);
                                    }
                                    if(isCommenting){
                                        commentDuration = 180000 + random.nextInt(300000);
                                        maxComment = 15 + random.nextInt(35);
                                    }
                                }
                            }
                        }

                        redditAutomation = new RedditAutomation(this.service, durationInt, scrollingInputs, voteInputs, commentInputs, userType, maxUpvote, maxComment, upvoteDuration, commentDuration, Task_Id, job_id);
                        redditAutomation.startScrollingAndUpvoting();
                        break;
                    } else {
                        Log.d(TAG, "inputs are incorrect.");
                    }
                    break;

                case "instagram followers bot":
                    currentlyRunningAutomation = "instagram";

                    Log.d(TAG, "Starting Instagram followers bot automation with duration: " + durationInt);
                    inputsArray = commandJson.getJSONArray("inputs");

                    // Create a proper list of objects from the JSONArray
                    List<Object> inputsList = new ArrayList<>();
                    for (int i = 0; i < inputsArray.length(); i++) {
                        // This will properly maintain the JSONObject type
                        inputsList.add(inputsArray.get(i));
                    }
                    Log.e(TAG,"Comamd: "+inputsList);
                    instagramFollowerBotAutomation = new InstagramFollowerBotAutomation(
                            this.service,
                            Task_Id,
                            job_id,
                            inputsList
                    );
                    instagramFollowerBotAutomation.checkToperformWarmUpAndThenStartAutomation();
                    break;

                case "tiktok bot":
                    currentlyRunningAutomation = "tiktok";
                    Log.d(TAG, "Starting tiktok bot automation with duration: " + durationInt);
                    inputsArray = commandJson.getJSONArray("inputs");

                    List<Object> inputListTikTok = new ArrayList<>();
                    for (int i = 0; i < inputsArray.length(); i++) {
                        inputListTikTok.add(inputsArray.get(i));
                    }

                    if (durationInt < 1) {
                        Log.d(TAG, "Insufficient time for automation: " + durationInt);
                        break;
                    }

                    tikTokAutomation = new TikTokAutomation(
                            this.service,
                            Task_Id,
                            job_id,
                            inputListTikTok,
                            durationInt
                    );
                    tikTokAutomation.launchApp();
                    break;


                case "linkedin bot":
                    currentlyRunningAutomation = "linkedin";

                    Log.d(TAG, "Starting linkedin bot automation with duration: " + durationInt);
                    inputsArray = commandJson.getJSONArray("inputs");

                    // Create a proper list of objects from the JSONArray
                    List<Object> inputList = new ArrayList<>();
                    for (int i = 0; i < inputsArray.length(); i++) {
                        // This will properly maintain the JSONObject type
                        inputList.add(inputsArray.get(i));
                    }
                    linkedInAutomation = new linkedInConnectionBotAutomation(
                            this.service,
                            Task_Id,
                            job_id,
                            inputList
                    );
                    linkedInAutomation.launchApp();
                    break;

                case "x (formerly twitter) bot":
                    currentlyRunningAutomation = "twitter";
                    Log.d(TAG, "Starting Twitter bot automation with duration: " + durationInt);
                    inputsArray = commandJson.getJSONArray("inputs");

                    List<Object> inputListTwitter = new ArrayList<>();
                    for (int i = 0; i < inputsArray.length(); i++) {
                        inputListTwitter.add(inputsArray.get(i));
                    }
                    twitterAutomation = new TwitterAutomation(
                            this.service,
                            Task_Id,
                            job_id,
                            inputListTwitter,
                            durationInt
                    );
                    Log.e(TAG,"Command: "+inputListTwitter);
                    twitterAutomation.launchApp();
                    break;
                default:
                    Log.e(TAG, "Unknown command received: " + appName);
                    break;
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse command: " + command, e);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid duration format: " + duration, e);
        }
    }
}