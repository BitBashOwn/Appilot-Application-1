package com.example.appilot.automations.TikTok;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;
import com.example.appilot.services.MyAccessibilityService;
import com.example.appilot.utils.HelperFunctions;

import java.util.List;
import java.util.Random;

public class Scrolling {
    private static final String TAG = "LinkedInAutomation";
    private final Context context;
    private final Handler handler;
    private final Random random;
    private final PopUpHandler popUpHandler;
    private final MyAccessibilityService service;
    private HelperFunctions helperFunctions;
    private String Task_id = null;
    private String job_id = null;
    private int duration;
    private long startTime;

    public Scrolling(MyAccessibilityService service, String taskid, String jobid, List<Object> AccountInputs, int duration) {
        this.context = service;
        this.service = service;
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();
        this.Task_id = taskid;
        this.job_id = jobid;
        this.popUpHandler = new PopUpHandler(this.service, this.handler, this.random, this.helperFunctions);
        this.helperFunctions = new HelperFunctions(context, Task_id, job_id);
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }
    public void onScroll() {
        Log.d(TAG,"Duration : " + duration + " minutes");
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000 / 60;

        Log.d(TAG, "Elapsed Time: " + elapsedTime + " minutes");

        if (elapsedTime < duration) {
            handler.postDelayed(() -> {
                Log.d(TAG, "Scrolling... (" + elapsedTime + "/" + duration + " minutes)");
                helperFunctions.performScroll(0.8f, 0.3f);
                int randomDelay = 2000 + random.nextInt(8000);

                handler.postDelayed(this::onScroll, randomDelay);
            }, 3000);
        } else {
            Log.d(TAG, "Reached duration of " + duration + " minutes. Exiting app...");
            handler.postDelayed(() -> {
                helperFunctions.cleanupAndExit("Scroll Completed..", "final");
            }, 2000);
        }
    }
}
