package org.stream.split.voicenotification;

import android.app.Notification;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.List;

/**
 * Created by split on 2015-10-18.
 */
public class NotificationCatcher extends NotificationListenerService {

    private final String TAG = this.getClass().getSimpleName();
    private VoiceGenerator voiceGenerator;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Notification Listener created!");
        IntentFilter intentFilter = new IntentFilter(TAG);
        voiceGenerator = new VoiceGenerator(this.getApplicationContext());
        registerReceiver(voiceGenerator, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(voiceGenerator);
        voiceGenerator.Shutdown();
    }


    public void addBroadcastFilter(IntentFilter intentFilter)
    {
        addBroadcastFilter(intentFilter);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {


        Log.d(TAG, "**********  onNotificationPosted");
        Log.d(TAG, "ID :" + sbn.getId() + "t" + sbn.getNotification().tickerText + "t" + sbn.getPackageName());
        Intent intent = new Intent(TAG);
        intent.putExtra("notification_id",sbn.getNotification().when);
        intent.putExtra("notification_event",sbn.getNotification().tickerText);
        intent.putExtra(Notification.EXTRA_TITLE, sbn.getNotification().extras.getString(Notification.EXTRA_TITLE));
        intent.putExtra(Notification.EXTRA_TEXT, sbn.getNotification().extras.getString(Notification.EXTRA_TEXT));
        sendBroadcast(intent);

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "********** onNOtificationRemoved");
        Log.d(TAG, "ID :" + sbn.getId() + "t" + sbn.getNotification().tickerText + "t" + sbn.getPackageName());
    }

//    public List<ApplicationInfo> getInstaledApplications()
//    {
//        final PackageManager packageManager = getPackageManager();
//        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
//    }
}