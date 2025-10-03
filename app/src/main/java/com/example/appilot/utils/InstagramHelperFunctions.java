package com.example.appilot.utils;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.appilot.automations.Interfaces.Action;
import com.example.appilot.services.MyAccessibilityService;

import java.util.List;
import java.util.Random;

public class InstagramHelperFunctions {
    private static final String TAG = "Instagram Warmup functions Handler";
    private final MyAccessibilityService service;
    private final HelperFunctions helperFunctions;
    private final Handler handler;
    private final Random random;

    public InstagramHelperFunctions(MyAccessibilityService service, Handler handler, Random random, HelperFunctions helperfunctions){
        this.service = service;
        this.handler = handler;
        this.random = random;
        this.helperFunctions = helperfunctions;
    }

}
