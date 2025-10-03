package com.example.appilot.automations.instagram;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import android.graphics.Path;
import java.util.Random;
import android.util.Log;

import com.example.appilot.services.MyAccessibilityService;
import com.example.appilot.utils.HelperFunctions;

public class InstagramAutomation {

    private static final String TAG = "InstagramRandomScrollUpvote";
    private static final String Instagram_Package = "com.instagram.android";

    private final Context context;
    private final Handler handler;
    private final Random random;
    private final int duration;
    private long startTime;

    private String Task_id = null;
    private String job_id = null;



    public InstagramAutomation(Context context, int duration){
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();
//        this.duration = duration * 60 * 1000;
        this.duration = 1 * 60 * 1000;
    }

    public void startScrollingAndLiking(){
        Log.d(TAG,"starting Instagram Automation");
        openInstagramApp();

    }

    public void openInstagramApp() {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(Instagram_Package);
        if(launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
            Log.d(TAG, "Instagram app launched successfully.");
            handler.postDelayed(this::initiateScrollAndLiking, 4000 + random.nextInt(2000));
        } else {
            launchInstagramExplicitly();
        }
    }


    private void launchInstagramExplicitly() {
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse("https://www.instagram.com"))
                .setPackage(Instagram_Package)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            handler.postDelayed(this::initiateScrollAndLiking, 4000 + random.nextInt(2000));
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Instagram Explicitly", e);
        }
    }


    private void initiateScrollAndLiking(){
        Log.d(TAG, "initiating Scroll and liking");
        startTime = System.currentTimeMillis();
        handler.postDelayed(this::performNextAction,2000 + random.nextInt(2000));
//        performNextAction();
    }

    private void performNextAction(){
        if(System.currentTimeMillis() - startTime >=duration){
            Log.d(TAG, "Automation Duration Completed");
            cleanupAndExit();
            return;
        }
        performScrollUp();
    }

    private void performScrollUp(){
        Path swipPath = new Path();
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        float startY = screenHeight * (0.65f + random.nextFloat() * 0.15f);
        float endY = screenHeight * (0.15f + random.nextFloat() * 0.15f);
        float xVariation = screenWidth * (0.1f * random.nextFloat() - 0.05f);

        swipPath.moveTo(screenWidth / 2f + xVariation, startY);
        swipPath.lineTo(screenWidth / 2f + xVariation, endY);

        performScrollUpGesture(swipPath, "up");
    }

    private void performScrollUpGesture(Path path, String direction){
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        int gestureDuration = 150 + random.nextInt(100);

        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 0, gestureDuration));

        try {
            MyAccessibilityService service = (MyAccessibilityService) context;
            service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback(){
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    int delay = 1500 + random.nextInt(2000);
                    handler.postDelayed(() -> performNextAction(), delay);
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    handler.postDelayed(() -> performNextAction(), 1000);
                }
            }, null);


        } catch (Exception e) {
            Log.e(TAG, "performScrollUpGesture: Error while performing Scroll up gesture");
//            performNextAction()
            handler.postDelayed(()->performNextAction(),1000);
        }
    }


    private void cleanupAndExit(){
//        HelperFunctions helperFunctions = new HelperFunctions(context);
//        handler.postDelayed(helperFunctions::closeAndOpenMyApp,2000 + random.nextInt(2000));
    }
}
