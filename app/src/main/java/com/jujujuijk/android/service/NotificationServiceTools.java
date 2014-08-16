package com.jujujuijk.android.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by jujujuijk on 7/8/13.
 */
public class NotificationServiceTools {

    static public boolean isMyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (NotificationService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    static public void startService(Context context) {
        Intent notificationService = new Intent(context, NotificationService.class);
        notificationService.setAction(NotificationService.ACTION_START);
        context.startService(notificationService);
    }

    /**
     *
     * @param context
     * @return true if not started. false otherwise
     */
    static public synchronized boolean tryStartService(Context context) {
        if (isMyServiceRunning(context))
            return true;
        startService(context);
        return false;
    }
}
