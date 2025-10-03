package com.example.appilot.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.appilot.services.MyAccessibilityService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Start the service after the boot
            Intent serviceIntent = new Intent(context, MyAccessibilityService.class);
            context.startService(serviceIntent);
        }
    }
}
