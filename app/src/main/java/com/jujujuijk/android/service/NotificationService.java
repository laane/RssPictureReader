package com.jujujuijk.android.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.jujujuijk.android.rssreader.MainActivity;
import com.jujujuijk.android.rssreader.R;
import com.jujujuijk.android.database.Feed;
import com.jujujuijk.android.database.MyDatabase;
import com.jujujuijk.android.asynctask.FeedParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jujujuijk on 7/8/13.
 */
public class NotificationService
        extends IntentService
        implements MyDatabase.IDatabaseWatcher, FeedParser.RssParserCallBack {

    // Notification tools
    public static final int NOTIFICATION_ID = 4242;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;

    // Public ACTIONS
    public static final String ACTION_START = "com.jujujuijk.android.service.ACTION_START";
    public static final String ACTION_DISMISS = "com.jujujuijk.android.service.ACTION_DISMISS";
    public static final String NOTIFICATION_FEED_START = "com.jujujuijk.android.service.NOTIFICATION_FEED_START";

    // Timer length
    public static final int TIMER_MIN = 10;
    public static final int TIMER_SEC = 0;

    public NotificationService() {
        super("FeedNotifService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

//        Log.e("boap", "Debut onHandleIntent");
        String action = intent == null ? ACTION_START : intent.getAction();

        if (action == null || action.equals(ACTION_START)) {
            startNotificationLoop();
        } else {
            Log.e("boap", "Action inconnue: " + action);
        }
    }

    private void startNotificationLoop() {
//        Log.e("boap", "Je lance la boucle (startnotificationloop)");

        MyDatabase.getInstance().follow(this);
        mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        int i = 0;
        while (true) {
//            Log.e("boap", "BOAP loopAction! " + i++);
            loopAction();
            synchronized (this) {
                try {
                    wait(TIMER_MIN * 60000 + TIMER_SEC * 1000);
                } catch (InterruptedException e) {}
            }
        }
    }

    private void loopAction() {
        List<Feed> feedList = new ArrayList<Feed>(MyDatabase.getInstance().getAllFeeds());

        for (Feed feed : feedList) {
            if (feed.getNotify() != 0) { // Update feeds for notifications and live-wallpaper
                new FeedParser(this, feed).execute(1);
            }
        }
    }

    @Override
    public void onDestroy() {
        MyDatabase.getInstance().unFollow(this);
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
            restartServiceIntent.setPackage(getPackageName());

            PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            alarmService.setExact(
                    AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + 1000,
                    restartServicePendingIntent);
        }
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void notifyFeedListChanged() {
        List<Feed> feedList = new ArrayList<Feed>(MyDatabase.getInstance().getAllFeeds());

        String message = "";
        long firstImageId = -1;
        for (Feed f : feedList) {
            if (f.getNotify() == Feed.Notify.NOTIF && !f.getPictureLast().equals(f.getPictureSeen())) {
                if (firstImageId == -1) {
                    firstImageId = f.getId();
                } else {
                    message += ", ";
                }
                message += f.getName();
            }
        }

        if (message.length() == 0) {
            mNotificationManager.cancel(NOTIFICATION_ID);
            return;
        }

        // Intent that is called when the notification gets wiped
        Intent dismissIntent = new Intent(this, ServiceBroadcastReceiver.class);
        dismissIntent.setAction(ACTION_DISMISS);
        PendingIntent piDismiss = PendingIntent.getBroadcast(this.getApplicationContext(), 0, dismissIntent, 0);

        // Constructs the Builder object.
        mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(getString(R.string.notification_title))
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message));

        Intent resultIntent = new Intent(this, MainActivity.class);
        // Gives to MainActivity which feed it should launch when the notification is clicked
        resultIntent.putExtra(NOTIFICATION_FEED_START, firstImageId);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setDeleteIntent(piDismiss);

        // Including the notification ID allows you to update the notification later on.
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    @Override
    public void onRssParserPostExecute(List<Bundle> images, Feed oldFeed) {
        if (images == null || images.size() != 1) {
            Log.e("rssreader", oldFeed.getName() + ": parser returned smthg else than 1 image");
            return;
        }
        // We get a new feed from db in case where it has changed during the url fetching
        Feed feed = MyDatabase.getInstance().getFeed(oldFeed.getId());
        if (feed == null)
            return;

        String lastPic = images.get(0).getString("url");

        if (feed.getPictureLast().equals(lastPic))
            return;

        feed.setPictureLast(lastPic);

        MyDatabase.getInstance().updateFeed(feed);
    }

}
