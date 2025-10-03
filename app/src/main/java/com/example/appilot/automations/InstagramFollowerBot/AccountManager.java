package com.example.appilot.automations.InstagramFollowerBot;

import android.view.accessibility.AccessibilityNodeInfo;

import com.example.appilot.automations.InstagramFollowerBot.InstagramAccount;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

class AccountManager {
    private String TAG = "Account Manager";
    private List<String> accountUsernames;
    private int currentAccountIndex;
    private Map<String, InstagramAccount> accountStats;
    private Boolean isAnyAccount;


    private final Random random = new Random();
    private List<Object> AccountsData;

    public AccountManager(List<Object> AccountInputs) {
        this.AccountsData = AccountInputs;
        this.accountUsernames = new ArrayList<>();
        this.accountStats = new HashMap<>();
        this.currentAccountIndex = 0;
        isAnyAccount = true;
    }

    public void initializeAccounts(List<AccessibilityNodeInfo> profileNodes) {
        Log.i(TAG, "Entered initializeAccounts");

        // Clear previous data
        accountUsernames.clear();
        accountStats.clear();
        Log.v(TAG, "Account lists cleared.");

        // Extract usernames from profileNodes
        List<String> usernamesListfromDevice = new ArrayList<>();
        for (AccessibilityNodeInfo node : profileNodes) {
            String username = node.getText().toString().trim();
            Log.v(TAG, "Processing node: " + username);
            usernamesListfromDevice.add(username); // Add username to the list
        }

        // Create a temporary map to hold accounts
        Map<String, InstagramAccount> tempAccountStats = new HashMap<>();

        // Check the last two objects for global switches
        String globalAutomationType = null;
        boolean hasGlobalSwitch = false;

        try {
                // Check the second-to-last object for "Switch Account Public To Private"
                JSONObject secondLastObject = (JSONObject) this.AccountsData.get(this.AccountsData.size() - 2);
                if (secondLastObject.has("Switch Account Public To Private")) {
                    JSONArray switchArray = secondLastObject.getJSONArray("Switch Account Public To Private");
                    if (switchArray.length() > 0) {
                        JSONObject switchObject = switchArray.getJSONObject(0);
                        if (switchObject.has("Switch Account Public To Private") &&
                                switchObject.getBoolean("Switch Account Public To Private")) {
                            globalAutomationType = "PublicToPrivate";
                            hasGlobalSwitch = true;
                            Log.d(TAG, "Global switch detected: PublicToPrivate");
                        }
                    }
                }

                // Check the last object for "Switch Account Private To Public" if no global switch found yet
                if (!hasGlobalSwitch) {
                    JSONObject lastObject = (JSONObject) this.AccountsData.get(this.AccountsData.size() - 1);
                    if (lastObject.has("Switch Account Private To Public")) {
                        JSONArray switchArray = lastObject.getJSONArray("Switch Account Private To Public");
                        if (switchArray.length() > 0) {
                            JSONObject switchObject = switchArray.getJSONObject(0);
                            if (switchObject.has("Switch Account Private To Public") &&
                                    switchObject.getBoolean("Switch Account Private To Public")) {
                                globalAutomationType = "PrivateToPublic";
                                hasGlobalSwitch = true;
                                Log.d(TAG, "Global switch detected: PrivateToPublic");
                            }
                        }
                    }
                }
        } catch (JSONException e) {
                Log.e(TAG, "Error checking global switches: " + e.getMessage());
        }

        // If global switch is found, initialize all accounts with the same automation type
        if (hasGlobalSwitch) {
            Log.i(TAG, "Initializing all accounts with global automation type: " + globalAutomationType);

            for (String username : usernamesListfromDevice) {
                tempAccountStats.put(username, new InstagramAccount(
                        username,
                        0, // maxFollowsPerHour
                        0, // minFollowsPerHour
                        0, // maxFollowsDaily
                        0, // minFollowsDaily
                        0, // minSleepTime
                        0, // maxSleepTime
                        0, // mutualFriendsCount
                        globalAutomationType,
                        "https://www.instagram.com", // postUrl
                        Collections.emptyList(), // positiveKeywords
                        Collections.emptyList(), // negativeKeywords
                        Collections.emptyList(), // usersToExcludeList
                        "Default" // typeOfSortForUnfollowing
                ));

                Log.i(TAG, "Account initialized for username: " + username + " with type: " + globalAutomationType);
            }
        } else {
            // No global switch, process individual accounts but skip the last two objects
            Log.i(TAG, "No global switch found, processing individual accounts");

            int endIndex = this.AccountsData.size() - 2; // Skip last two objects
            if (endIndex < 0) endIndex = 0;

            for (int j = 0; j < endIndex; j++) {
                try {
                    // Get the JSONObject and convert it to a Map
                    JSONObject jsonObject = (JSONObject) this.AccountsData.get(j);

                    // Get username directly from JSONObject
                    String username = jsonObject.getString("username");

                    // Skip the account if username is not in usernamesListfromDevice
                    if (!usernamesListfromDevice.contains(username)) continue;

                    // Handle the "inputs" JSONArray
                    JSONArray inputsJsonArray = jsonObject.getJSONArray("inputs");
                    List<Map<String, Object>> inputs = new ArrayList<>();

                    // Convert each JSONObject in inputsJsonArray to a Map
                    for (int i = 0; i < inputsJsonArray.length(); i++) {
                        JSONObject inputJsonObject = inputsJsonArray.getJSONObject(i);
                        Map<String, Object> inputMap = new HashMap<>();

                        Iterator<String> inputKeys = inputJsonObject.keys();
                        while (inputKeys.hasNext()) {
                            String key = inputKeys.next();
                            inputMap.put(key, inputJsonObject.get(key));
                        }

                        inputs.add(inputMap);
                    }

                    // Initialize variables for InstagramAccount constructor
                    int maxFollowsPerHour = 0;
                    int minFollowsPerHour = 0;
                    int maxFollowsDaily = 0;
                    int minFollowsDaily = 0;
                    int minSleepTime = 0;
                    int maxSleepTime = 0;
                    int mutualFriendsCount = 0;
                    String automationType = "";
                    String postUrl = "";
                    List<String> positiveKeywords = new ArrayList<>();
                    List<String> negativeKeywords = new ArrayList<>();
                    List<String> usersToExcludeList = new ArrayList<>();
                    String typeOfSortForUnfollowing = "Default";

                    // Process inputs and extract automation type data
                    for (Map<String, Object> automationTypeData : inputs) {
                        if (automationTypeData.containsKey("Follow from Notification Suggestions") &&
                                (Boolean) automationTypeData.get("Follow from Notification Suggestions")) {
                            automationType = "NotificationSuggestion";
                            postUrl = "https://www.instagram.com";

                            // Handle positiveKeywords and negativeKeywords safely
                            Object positiveObj = automationTypeData.get("positiveKeywords");
                            if (positiveObj instanceof JSONArray) {
                                JSONArray positiveArray = (JSONArray) positiveObj;
                                positiveKeywords = new ArrayList<>();
                                for (int i = 0; i < positiveArray.length(); i++) {
                                    try {
                                        positiveKeywords.add(positiveArray.getString(i));
                                    } catch (JSONException e) {
                                        Log.e(TAG, "Error parsing positiveKeywords: " + e.getMessage());
                                    }
                                }
                            } else if (positiveObj instanceof List) {
                                positiveKeywords = (List<String>) positiveObj;
                            }

                            Object negativeObj = automationTypeData.get("negativeKeywords");
                            if (negativeObj instanceof JSONArray) {
                                JSONArray negativeArray = (JSONArray) negativeObj;
                                negativeKeywords = new ArrayList<>();
                                for (int i = 0; i < negativeArray.length(); i++) {
                                    try {
                                        negativeKeywords.add(negativeArray.getString(i));
                                    } catch (JSONException e) {
                                        Log.e(TAG, "Error parsing negativeKeywords: " + e.getMessage());
                                    }
                                }
                            } else if (negativeObj instanceof List) {
                                negativeKeywords = (List<String>) negativeObj;
                            }

                            // Get integer values safely
                            mutualFriendsCount = getIntValue(automationTypeData, "mutualFriendsCount", 0);
                            minFollowsPerHour = getIntValue(automationTypeData, "minFollowsPerHour", 0);
                            maxFollowsPerHour = getIntValue(automationTypeData, "maxFollowsPerHour", 0);
                            minFollowsDaily = getIntValue(automationTypeData, "minFollowsDaily", 0);
                            maxFollowsDaily = getIntValue(automationTypeData, "maxFollowsDaily", 0);
                            maxSleepTime = getIntValue(automationTypeData, "maxSleepTime", 0);
                            minSleepTime = getIntValue(automationTypeData, "minSleepTime", 0);
                            Log.d(TAG, "Matched 'Follow from Notification Suggestions'. Breaking loop.");
                            break;
                        }
                        else if (automationTypeData.containsKey("Follow from Profile Followers List") &&
                                (Boolean) automationTypeData.get("Follow from Profile Followers List")) {
                            automationType = "ProfileSuggestion";
                            postUrl = getStringValue(automationTypeData, "url", "");

                            // Handle positiveKeywords and negativeKeywords safely
                            Object positiveObj = automationTypeData.get("positiveKeywords");
                            if (positiveObj instanceof JSONArray) {
                                JSONArray positiveArray = (JSONArray) positiveObj;
                                positiveKeywords = new ArrayList<>();
                                for (int i = 0; i < positiveArray.length(); i++) {
                                    try {
                                        positiveKeywords.add(positiveArray.getString(i));
                                    } catch (JSONException e) {
                                        Log.e(TAG, "Error parsing positiveKeywords: " + e.getMessage());
                                    }
                                }
                            } else if (positiveObj instanceof List) {
                                positiveKeywords = (List<String>) positiveObj;
                            }

                            Object negativeObj = automationTypeData.get("negativeKeywords");
                            if (negativeObj instanceof JSONArray) {
                                JSONArray negativeArray = (JSONArray) negativeObj;
                                negativeKeywords = new ArrayList<>();
                                for (int i = 0; i < negativeArray.length(); i++) {
                                    try {
                                        negativeKeywords.add(negativeArray.getString(i));
                                    } catch (JSONException e) {
                                        Log.e(TAG, "Error parsing negativeKeywords: " + e.getMessage());
                                    }
                                }
                            } else if (negativeObj instanceof List) {
                                negativeKeywords = (List<String>) negativeObj;
                            }

                            // Get integer values safely
                            mutualFriendsCount = getIntValue(automationTypeData, "mutualFriendsCount", 0);
                            minFollowsPerHour = getIntValue(automationTypeData, "minFollowsPerHour", 0);
                            maxFollowsPerHour = getIntValue(automationTypeData, "maxFollowsPerHour", 0);
                            minFollowsDaily = getIntValue(automationTypeData, "minFollowsDaily", 0);
                            maxFollowsDaily = getIntValue(automationTypeData, "maxFollowsDaily", 0);
                            maxSleepTime = getIntValue(automationTypeData, "maxSleepTime", 0);
                            minSleepTime = getIntValue(automationTypeData, "minSleepTime", 0);
                            Log.d(TAG, "Matched 'Follow from Profile Followers List'. Breaking loop.");
                            break;
                        } else if ((automationTypeData.containsKey("Follow from Profile Posts") &&
                                (Boolean) automationTypeData.get("Follow from Profile Posts")) || (automationTypeData.containsKey("Follow from Post") &&
                                (Boolean) automationTypeData.get("Follow from Post"))) {
                            automationType = automationTypeData.containsKey("Follow from Post") ? "ProfileLikersFollow":"FollowProfilePostsLikers";
                            postUrl = getStringValue(automationTypeData, "url", "");

                            // Handle positiveKeywords and negativeKeywords safely
                            Object positiveObj = automationTypeData.get("positiveKeywords");
                            if (positiveObj instanceof JSONArray) {
                                JSONArray positiveArray = (JSONArray) positiveObj;
                                positiveKeywords = new ArrayList<>();
                                for (int i = 0; i < positiveArray.length(); i++) {
                                    try {
                                        positiveKeywords.add(positiveArray.getString(i));
                                    } catch (JSONException e) {
                                        Log.e(TAG, "Error parsing positiveKeywords: " + e.getMessage());
                                    }
                                }
                            } else if (positiveObj instanceof List) {
                                positiveKeywords = (List<String>) positiveObj;
                            }

                            Object negativeObj = automationTypeData.get("negativeKeywords");
                            if (negativeObj instanceof JSONArray) {
                                JSONArray negativeArray = (JSONArray) negativeObj;
                                negativeKeywords = new ArrayList<>();
                                for (int i = 0; i < negativeArray.length(); i++) {
                                    try {
                                        negativeKeywords.add(negativeArray.getString(i));
                                    } catch (JSONException e) {
                                        Log.e(TAG, "Error parsing negativeKeywords: " + e.getMessage());
                                    }
                                }
                            } else if (negativeObj instanceof List) {
                                negativeKeywords = (List<String>) negativeObj;
                            }

                            // Get integer values safely
                            mutualFriendsCount = getIntValue(automationTypeData, "mutualFriendsCount", 0);
                            minFollowsPerHour = getIntValue(automationTypeData, "minFollowsPerHour", 0);
                            maxFollowsPerHour = getIntValue(automationTypeData, "maxFollowsPerHour", 0);
                            minFollowsDaily = getIntValue(automationTypeData, "minFollowsDaily", 0);
                            maxFollowsDaily = getIntValue(automationTypeData, "maxFollowsDaily", 0);
                            maxSleepTime = getIntValue(automationTypeData, "maxSleepTime", 0);
                            minSleepTime = getIntValue(automationTypeData, "minSleepTime", 0);
                            Log.d(TAG, "Matched 'Follow from Profile Posts'. Breaking loop.");
                            break;
                        } else if (automationTypeData.containsKey("Unfollow Non-Followers") &&
                                (Boolean) automationTypeData.get("Unfollow Non-Followers")) {
                            automationType = "unFollow";
                            postUrl = "https://www.instagram.com";
                            mutualFriendsCount = 0;
                            positiveKeywords = Collections.emptyList();
                            negativeKeywords = Collections.emptyList();

                            // Handle usersToExcludeList safely
                            Object excludeObj = automationTypeData.get("usersToExcludeList");
                            if (excludeObj instanceof JSONArray) {
                                JSONArray excludeArray = (JSONArray) excludeObj;
                                usersToExcludeList = new ArrayList<>();
                                for (int i = 0; i < excludeArray.length(); i++) {
                                    try {
                                        usersToExcludeList.add(excludeArray.getString(i));
                                    } catch (JSONException e) {
                                        Log.e(TAG, "Error parsing usersToExcludeList: " + e.getMessage());
                                    }
                                }
                            } else if (excludeObj instanceof List) {
                                usersToExcludeList = (List<String>) excludeObj;
                            }

                            typeOfSortForUnfollowing = getStringValue(automationTypeData, "typeOfUnfollowing", "Default");

                            // Get integer values safely
                            minFollowsPerHour = getIntValue(automationTypeData, "minFollowsPerHour", 0);
                            maxFollowsPerHour = getIntValue(automationTypeData, "maxFollowsPerHour", 0);
                            minFollowsDaily = getIntValue(automationTypeData, "minFollowsDaily", 0);
                            maxFollowsDaily = getIntValue(automationTypeData, "maxFollowsDaily", 0);
                            maxSleepTime = getIntValue(automationTypeData, "maxSleepTime", 0);
                            minSleepTime = getIntValue(automationTypeData, "minSleepTime", 0);
                            Log.d(TAG, "Matched 'Unfollow Non-Followers'. Breaking loop.");
                            break;
                        } else if (automationTypeData.containsKey("Accept All Follow Requests") &&
                                (Boolean) automationTypeData.get("Accept All Follow Requests")) {
                            automationType = "FollowAllRequests";
                            postUrl = "https://www.instagram.com";
                            mutualFriendsCount = 0;
                            positiveKeywords = Collections.emptyList();
                            negativeKeywords = Collections.emptyList();

                            // Get integer values safely
                            maxSleepTime = getIntValue(automationTypeData, "maxSleepTime", 0);
                            minSleepTime = getIntValue(automationTypeData, "minSleepTime", 0);
                            Log.d(TAG, "Matched 'Accept All Follow Requests'. Breaking loop.");
                            break;
                        } else if (automationTypeData.containsKey("Switch Account Public To Private") &&
                                (Boolean) automationTypeData.get("Switch Account Public To Private") || automationTypeData.containsKey("Switch Account Private To Public") &&
                                (Boolean) automationTypeData.get("Switch Account Private To Public")) {
                            automationType = automationTypeData.containsKey("Switch Account Public To Private")? "PublicToPrivate":"PrivateToPublic";
                            postUrl = "https://www.instagram.com";
                            mutualFriendsCount = 0;
                            positiveKeywords = Collections.emptyList();
                            negativeKeywords = Collections.emptyList();
                            if (automationTypeData.containsKey("Switch Account Public To Private")) {
                                Log.d(TAG, "Matched 'Switch Account Public To Private'. Breaking loop.");
                            }else{
                                Log.d(TAG, "Matched 'Switch Account Private To Public'. Breaking loop.");
                            }
                            break;
                        }
                    }

                    Log.i(TAG, "Account properties for username: " + username);
                    Log.i(TAG, "- automationType: " + automationType);
                    Log.i(TAG, "- postUrl: " + postUrl);
                    Log.i(TAG, "- maxFollowsPerHour: " + maxFollowsPerHour);
                    Log.i(TAG, "- minFollowsPerHour: " + minFollowsPerHour);
                    Log.i(TAG, "- maxFollowsDaily: " + maxFollowsDaily);
                    Log.i(TAG, "- minFollowsDaily: " + minFollowsDaily);
                    Log.i(TAG, "- minSleepTime: " + minSleepTime);
                    Log.i(TAG, "- maxSleepTime: " + maxSleepTime);
                    Log.i(TAG, "- mutualFriendsCount: " + mutualFriendsCount);
                    Log.i(TAG, "- positiveKeywords: " + positiveKeywords);
                    Log.i(TAG, "- negativeKeywords: " + negativeKeywords);
                    Log.i(TAG, "- usersToExcludeList: " + usersToExcludeList);
                    Log.i(TAG, "- typeOfSortForUnfollowing: " + typeOfSortForUnfollowing);

                    // Store account in temporary map
                    tempAccountStats.put(username, new InstagramAccount(
                            username,
                            maxFollowsPerHour,
                            minFollowsPerHour,
                            maxFollowsDaily,
                            minFollowsDaily,
                            minSleepTime,
                            maxSleepTime,
                            mutualFriendsCount,
                            automationType,
                            postUrl,
                            positiveKeywords,
                            negativeKeywords,
                            usersToExcludeList,
                            typeOfSortForUnfollowing
                    ));

                } catch (JSONException e) {
                    Log.e(TAG, "Error processing JSONObject: " + e.getMessage());
                }
            }
        }

        // Now add accounts to accountUsernames and accountStats in the order they appear in profileNodes
        for (AccessibilityNodeInfo node : profileNodes) {
            String username = node.getText().toString().trim();
            if (tempAccountStats.containsKey(username)) {
                accountUsernames.add(username);
                accountStats.put(username, tempAccountStats.get(username));
            }
        }

        // Check if no accounts were initialized
        if (accountUsernames.isEmpty()) {
            this.isAnyAccount = false;
            return;
        }

        // Log the final list of account usernames
        Log.v(TAG, "Final list of account usernames: " + accountUsernames);
        for (String username : accountUsernames) {
            Log.v(TAG, "Account for " + username + ": " + accountStats.get(username));
        }
    }

    // Helper method to safely get Integer values
    private int getIntValue(Map<String, Object> map, String key, int defaultValue) {
        try {
            Object value = map.get(key);
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof Number) {
                return ((Number) value).intValue();
            } else if (value instanceof String) {
                return Integer.parseInt((String) value);
            } else if (value instanceof JSONObject) {
                return ((JSONObject) value).getInt(key);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting integer value for key " + key + ": " + e.getMessage());
        }
        return defaultValue;
    }

    // Helper method to safely get String values
    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        try {
            Object value = map.get(key);
            if (value instanceof String) {
                return (String) value;
            } else if (value != null) {
                return value.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting string value for key " + key + ": " + e.getMessage());
        }
        return defaultValue;
    }

    public boolean checkIsUserDone(String userName) {
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);

        return account.checkUserNameExists(userName);
    }

    public String getLastUserDone() {
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);

        return account.getLastUserDone();
    }

    public boolean isAccountBlocked() {
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        Log.i(TAG, "Checking is Account Blocked with username = " + username);
        return account.isBlocked();
    }



    public void setSleepTime() {
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        Log.i(TAG, "Setting sleep time for Account with username = " + username);
        account.setSleepTime();
    }


    public boolean checkIsNextAccountTimerDone() {
        Log.i(TAG, "Entered checkIsNextAccountTimerDone");
        if (accountUsernames.isEmpty()) {
            Log.e(TAG, "accountUsernames is Empty");
            return false;
        }

        int startIndex = (currentAccountIndex + 1) % accountUsernames.size();
        int index = startIndex;
        int iterationCount = 0;

        do {
            String username = accountUsernames.get(index);
            InstagramAccount account = accountStats.get(username);
            Log.i(TAG, "Checking is Timer Done for Account with username = " + username);

            // Skip blocked accounts
            if (!account.isBlocked()) {
                if (account.getIsTimerDone()) {
                    return true;
                }
            }

            // Move to next account, wrapping around if needed
            index = (index + 1) % accountUsernames.size();
            iterationCount++;

            // If we've checked all accounts and none are ready, return false
            if (iterationCount >= accountUsernames.size()) {
                return false;
            }
        } while (index != startIndex);

        return false;
    }

    public long getTimeRemaining() {
        Log.i(TAG, "Entered getTimeRemaining");
        if (accountUsernames.isEmpty()) {
            return 0;
        }

        int startIndex = (currentAccountIndex + 1) % accountUsernames.size();
        int index = startIndex;
        int iterationCount = 0;

        do {
            String username = accountUsernames.get(index);
            InstagramAccount account = accountStats.get(username);
            Log.i(TAG, "Checking remaining time for Account with username = " + username);

            // Skip blocked accounts
            if (!account.isBlocked()) {
                return account.getHowMuchTimeRemaining();
            }

            // Move to next account, wrapping around if needed
            index = (index + 1) % accountUsernames.size();
            iterationCount++;

            // If we've checked all accounts and none are non-blocked, return 0
            if (iterationCount >= accountUsernames.size()) {
                return 0;
            }
        } while (index != startIndex);

        return 0;
    }

    public String getNextAvailableUsername() {
        Log.i(TAG, "Entered to getNextAvailableUsername");
        if (accountUsernames.isEmpty()) {
            Log.e(TAG, "accountUsernames is Empty");
            return null;
        }

        int startIndex = (currentAccountIndex + 1) % accountUsernames.size();
        int index = startIndex;
        int iterationCount = 0;

        do {
            String username = accountUsernames.get(index);
            InstagramAccount account = accountStats.get(username);

            if (!account.isBlocked()) {
                currentAccountIndex = index;
                account.UpdateHourlyFollows();
                Log.i(TAG,"Next Account: "+accountUsernames.get(currentAccountIndex));
                return username;
            }

            // Move to next account, wrapping around if needed
            index = (index + 1) % accountUsernames.size();
            iterationCount++;

            // If we've checked all accounts and none are available, return null
            if (iterationCount >= accountUsernames.size()) {
                return null;
            }
        } while (index != startIndex);

        return null;
    }

    public boolean checkIsAnyAccountNonBlocked() {
        Log.i(TAG, "Entered checkIsAnyAccountNonBlocked");
        if (accountUsernames.isEmpty()) {
            Log.e(TAG, "accountUsernames is Empty");
            return false;
        }

        int startIndex = (currentAccountIndex + 1) % accountUsernames.size();
        int index = startIndex;
        int iterationCount = 0;

        do {
            String username = accountUsernames.get(index);
            InstagramAccount account = accountStats.get(username);

            if (!account.isBlocked()) {
                return true;
            }

            // Move to next account, wrapping around if needed
            index = (index + 1) % accountUsernames.size();
            iterationCount++;

            // If we've checked all accounts and none are non-blocked, return false
            if (iterationCount >= accountUsernames.size()) {
                return false;
            }
        } while (index != startIndex);

        return false;
    }
    public void addUserDone(String userName) {
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        account.addUsername(userName);
    }

    public void popLastUserDone() {
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        account.popLastUserDone();
    }

    public String getCurrentUsername() {
        if (accountUsernames.isEmpty()) return null;
        return accountUsernames.get(currentAccountIndex);
    }

    public int getFollowsDone() {
        if (accountUsernames.isEmpty()) return 0;
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        return account.getDailyFollows();
    }

    public String getAccountStatus(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        return account.getAccountPrivacyStatus();
    }

    public void setAccountStatus(String status){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        account.setAccountPrivacyStatus(status);
    }

    public void IncrementFollowsDone() {
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        account.incrementFollows();
    }

    public int getRequestsMade() {
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        return account.getFollowRequestsMade();
    }

    public void IncrementRequestMade() {
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        account.IncrementRequestsMade();
    }


    public boolean checkIsHourlyFollowsDone() {
        Log.i(TAG, "Entered to checkIsHourlyFollowsDone");
        String username = accountUsernames.get(currentAccountIndex);
        Log.v(TAG, "Username = "+username);
        InstagramAccount account = accountStats.get(username);
        return account.checkHourlyFollows();
    }

    public boolean checkIsDailyFollowsDone() {
        Log.i(TAG, "Entered to checkIsDailyFollowsDone");
        String username = accountUsernames.get(currentAccountIndex);
        Log.v(TAG, "Username = "+username);
        InstagramAccount account = accountStats.get(username);
        return account.checkDailyFollows();
    }

    public int getCurrentIndex(){
        return currentAccountIndex;
    }

    public void BlockCurrentAccount() {
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        account.blockAccount();
    }

    public void setCurrentAccountTotalPosts(int totalposts){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        account.setTotalPosts(totalposts);
    }

    public int getCurrentAccountTotalPosts(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        return account.getTotalPosts();
    }

    public void incrementCurrentAccountPostsDone(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        account.incrementPostsDone();
    }

    public int getCurrentAccountPostsDone(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        return account.getPostsDone();
    }
    public int getCurrentAccountCurrentColumn(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        return account.getCurrentColumn();
    }
    public void incrementCurrentAccountCurrentColumn(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        account.incrementCurrentColumn();
    }
    public void setCurrentAccountCurrentColumn(int val){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        account.setCurrentColumn(val);
    }
    public int getCurrentAccountCurrentRow(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        return account.getCurrentRow();
    }
    public void incrementCurrentAccountCurrentRow(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        account.incrementCurrentRow();
    }

    public void setAccountLimitHit(boolean isAccountBlocked){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        account.setAccountActionBlocked(isAccountBlocked);
    }

    public boolean getAccountLimitHit(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        return account.getAccountActionBlocked();
    }

    public void increaseThisRunFollows(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        account.increaseThisRunFollowsMade();
    }

    public void increaseThisRunFollowRequest(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        account.increaseThisRunFollowRequestsMade();
    }

    public int getThisRunFollows(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        return account.increateThisRunFollowsFade();
    }

    public int getThisRunFollowRequest(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        return account.increateThisRunFollowRequestsFade();
    }

    public boolean getIsTaskUpdated(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        return account.getIsTaskUpdated();
    }

    public void setIsTaskUpdated(boolean val){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        account.setIsTaskUpdated(val);
    }

    public boolean getListStatus(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        return account.getListStatus();
    }

    public void setListStatus(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        account.setListStatus();
    }

    public Boolean getIsAnyAccount(){
        return this.isAnyAccount;
    }

    public Boolean isAccountToAutomate(String Username){
        return accountUsernames.contains(Username);
    }

    public int getCurrentAccountMutualCount(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        return account.getMutualCount();
    }

    public String getCurrentAccountAutomationType(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        return account.getAutomationType();
    }

    public String getCurrentAccountUrl(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        return account.getUrl();
    }

    public List<String> getCurrentAccountPositiveKeywords(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        return account.getPositiveKeywords();
    }

    public List<String> getCurrentAccountNegativeKeywords(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        return account.getNegativeKeywords();
    }

    public  List<String> getCurrentAccountUsersToExcludeList(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        return account.getUsersToExcludeList();
    }

    public String getCurrentAccountTypeOfSortForUnfollowing(){
        String username = accountUsernames.get(currentAccountIndex);
        InstagramAccount account = accountStats.get(username);
        return account.getTypeOfSortForUnfollowing();
    }
}