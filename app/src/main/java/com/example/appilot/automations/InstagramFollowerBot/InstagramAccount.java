package com.example.appilot.automations.InstagramFollowerBot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import android.util.Log;

class InstagramAccount {
    private String TAG = "Instagram Account";
    private String username;
    private int hourlyFollows;
    private int dailyFollows;
    private int thisHourMaxFollows;
    private int maxDailyFollows;
    private boolean isBlocked;
    private List<String> DoneUsers = new ArrayList<>();

    private long TimerStartTime;
    private long sleepTime;

    private int FollowRequestMade;

    private String AccountPrivacyStatus = "-";

    private int totalPosts;
    private int postsDone;
    private int currentRow;
    private int currentColumn;
    private Boolean isAccountActionBlocked;

    private int thisRunFollowsMade;
    private int thisRunFollowRequestMade;

    private boolean isTaskUpdated;

    private boolean isListCompleted;

    private final int MAX_HOURLY_FOLLOWS;
    private final int MAX_DAILY_FOLLOWS;
    private final int MIN_HOURLY_FOLLOWS;
    private final int MIN_DAILY_FOLLOWS;
    public final int minSleepTime;
    public final int maxSleepTime;
    private int mutualFriends = 0;
    private final String type;
    public final String url;
    private final List<String> positiveKeywords;
    private final List<String> negativeKeywords;
    private final List<String> usersToExcludeList;
    private String  typeOfSortForUnfollowing = null;

    private final Random random = new Random();


    // Add getters/setters

    public InstagramAccount(String username, int maxFollowsPerHour, int minFollowsPerHour, int maxFollowsDaily, int minFollowsDaily, int minSleepTime, int maxSleepTime, int mutualFriendsCount, String automationType, String postUrl, List<String> positiveKeywords, List<String> negativeKeywords, List<String> usersToExcludeList, String typeOfSortForUnfollowing) {
        this.username = username;
        this.MAX_HOURLY_FOLLOWS = maxFollowsPerHour;
        this.MAX_DAILY_FOLLOWS = maxFollowsDaily;
        this.MIN_HOURLY_FOLLOWS = minFollowsPerHour;
        this.MIN_DAILY_FOLLOWS = minFollowsDaily;
        this.hourlyFollows = 0;
        this.dailyFollows = 0;
        this.maxDailyFollows = this.MIN_DAILY_FOLLOWS + random.nextInt(this.MAX_DAILY_FOLLOWS - this.MIN_DAILY_FOLLOWS+1);
        this.thisHourMaxFollows = this.MIN_HOURLY_FOLLOWS + random.nextInt(this.MAX_HOURLY_FOLLOWS - this.MIN_HOURLY_FOLLOWS+1);
        this.isBlocked = false;
        this.TimerStartTime = System.currentTimeMillis();
        this.sleepTime = 0;
        this.FollowRequestMade = 0;
        this.totalPosts = 0;
        this.postsDone = 0;
        this.currentRow = 1;
        this.currentColumn = 1;
        this.isAccountActionBlocked = false;
        this.thisRunFollowsMade = 0;
        this.thisRunFollowRequestMade = 0;
        this.isTaskUpdated = false;
        this.isListCompleted = false;

        this.minSleepTime = minSleepTime * 60 * 1000;
        this.maxSleepTime = maxSleepTime * 60 * 1000;
        this.mutualFriends = Math.max(mutualFriendsCount, 1);
        this.type = automationType != null ? automationType : "FollowAllRequests";
        this.url = postUrl != null ? postUrl : "https://www.instagram.com";
        this.positiveKeywords = positiveKeywords != null ? positiveKeywords : Collections.emptyList();
        this.negativeKeywords = negativeKeywords != null ? negativeKeywords : Collections.emptyList();
        this.usersToExcludeList = usersToExcludeList != null ? usersToExcludeList : Collections.emptyList();
        this.typeOfSortForUnfollowing = typeOfSortForUnfollowing;

    }
    public void addUsername(String userName) {
        DoneUsers.add(userName);
    }

    public void popLastUserDone(){
        DoneUsers.remove(DoneUsers.size()-1);
    }

    public Boolean checkUserNameExists(String userName) {
        if (DoneUsers.contains(userName)) return true;
        return false;
    }
    public String getLastUserDone() {
        return DoneUsers.get(DoneUsers.size()-1);
    }

    public String getUsername() {
        return username;
    }

    public int getHourlyFollows() {
        return hourlyFollows;
    }

    public int getDailyFollows() {
        return this.dailyFollows - this.FollowRequestMade;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void blockAccount() {
        this.isBlocked = true;
    }

    public void resetHourlyFollows() {
        this.hourlyFollows = 0;
    }

    public void incrementFollows() {
        this.hourlyFollows++;
        this.dailyFollows++;
    }

    public void setSleepTime(){
        this.TimerStartTime = System.currentTimeMillis();
        this.sleepTime = this.minSleepTime + random.nextInt(this.maxSleepTime - this.minSleepTime + 30000);
        Log.i(TAG, "Sleep Time = " + this.sleepTime);
    }

    public Boolean getIsTimerDone(){
        long elapsedTime = System.currentTimeMillis() - this.TimerStartTime;
        return elapsedTime >= this.sleepTime;
    }

    public long getHowMuchTimeRemaining(){
        long elapsedTime = System.currentTimeMillis() - this.TimerStartTime;
        return this.sleepTime - elapsedTime;
    }

    public boolean checkHourlyFollows(){
        Log.i(TAG, "Entered to checkHourlyFollows");
        Log.e(TAG,"hourlyFollows = "+this.hourlyFollows);
        Log.e(TAG,"thisHourMaxFollows = "+this.thisHourMaxFollows);
        if(this.hourlyFollows >= this.thisHourMaxFollows){
            this.hourlyFollows = 0;
            Log.e(TAG,"dailyFollows = "+this.dailyFollows);
            Log.e(TAG,"maxdailyFollows = "+this.maxDailyFollows);
            if(this.dailyFollows >= this.maxDailyFollows){
                Log.e(TAG, "this.dailyFollows >= this.maxDailyFollows: true");
                this.isBlocked = true;
            }
            return true;
        }
        return false;
    }

    public boolean checkDailyFollows(){
        Log.i(TAG, "Entered to checkDailyFollows");
        Log.e(TAG,"dailyFollows = "+this.dailyFollows);
        Log.e(TAG,"maxdailyFollows = "+this.maxDailyFollows);
        if(this.dailyFollows >= this.maxDailyFollows){
            Log.e(TAG, "this.dailyFollows >= this.maxDailyFollows: true");
            this.isBlocked = true;
            return true;
        }
        return false;
    }

    public void UpdateHourlyFollows(){
        Log.i(TAG,"Entered UpdateHourlyFollows");
        this.thisHourMaxFollows = this.MIN_HOURLY_FOLLOWS + random.nextInt(this.MAX_HOURLY_FOLLOWS - this.MIN_HOURLY_FOLLOWS+1);
    }

    public void setAccountPrivacyStatus(String status){
        this.AccountPrivacyStatus = status;
    }

    public String getAccountPrivacyStatus(){
        return this.AccountPrivacyStatus;
    }

    public void IncrementRequestsMade(){
        this.FollowRequestMade++;
    }

    public int getFollowRequestsMade(){
        return this.FollowRequestMade;
    }

    public void setTotalPosts(int totalposts){
        this.totalPosts = totalposts;
    }

    public int getTotalPosts(){
        return this.totalPosts;
    }

    public void incrementPostsDone(){
        this.postsDone++;
    }

    public int getPostsDone(){
        return this.postsDone;
    }

    public int getCurrentColumn(){
        return this.currentColumn;
    }

    public void incrementCurrentColumn(){
        this.currentColumn++;
    }

    public void setCurrentColumn(int val){
        this.currentColumn = val;
    }

    public int getCurrentRow(){
        return this.currentRow;
    }

    public void incrementCurrentRow(){
        this.currentRow++;
    }

    public void setAccountActionBlocked(boolean isAccountBlocked){
        this.isAccountActionBlocked = isAccountBlocked;
    }
    public boolean getAccountActionBlocked(){
        return this.isAccountActionBlocked;
    }

    public void increaseThisRunFollowsMade(){
        this.thisRunFollowsMade++;
    }
    public void increaseThisRunFollowRequestsMade(){
        this.thisRunFollowRequestMade++;
    }

    public int increateThisRunFollowsFade(){
        int temp = this.thisRunFollowsMade - this.thisRunFollowRequestMade;
        if(temp < 0) temp = 0;
        this.thisRunFollowsMade = 0;
        return temp;
    }
    public int increateThisRunFollowRequestsFade(){
        int followRequests = this.thisRunFollowRequestMade;
        this.thisRunFollowRequestMade = 0;
        return followRequests;
    }

    public void setIsTaskUpdated(boolean val){
        this.isTaskUpdated = val;
    }

    public boolean getIsTaskUpdated(){
        return this.isTaskUpdated;
    }

    public  boolean getListStatus(){
        return this.isListCompleted;
    }

    public void setListStatus(){
        this.isListCompleted = true;
    }

    public int getMutualCount(){
        return this.mutualFriends;
    }

    public String getAutomationType(){
        return this.type;
    }

    public String getUrl(){
        return this.url;
    }

    public List<String> getPositiveKeywords(){
        return this.positiveKeywords;
    }

    public List<String> getNegativeKeywords(){
        return this.negativeKeywords;
    }

    public  List<String> getUsersToExcludeList(){
        return this.usersToExcludeList;
    }

    public String getTypeOfSortForUnfollowing(){
        return this.typeOfSortForUnfollowing;
    }

}