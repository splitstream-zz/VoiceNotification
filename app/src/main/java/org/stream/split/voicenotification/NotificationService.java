package org.stream.split.voicenotification;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.Helpers.NotificationServiceConnection;


/**
 * Created by split on 2015-10-18.
 */
public class NotificationService extends NotificationListenerService {

    public static final String TAG = "NotificationService";
    public static final String NOTIFICATION_OBJECT = "notification_object";
    public static final String NOTIFICATION_POST_TIME = "notification_Post_Time";
    public static final String NOTIFICATION_PACKAGE_NAME = "notification_pakageName";
    public static final String NOTIFICATION_APPLICATION_LABEL = "notification_application_name";
    public static final String CUSTOM_BINDING = "org.stream.split.voicenotification.CustomIntent_NotificationCatcher";

    private final IBinder mBinder = new NotificationCatcherBinder();

    private NotificationServiceConnection mConnection;
    private NotificationBroadcastReceiver mVoiceGenerator;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Notification Listener created!");
        registerVoiceReceivers();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposeVoiceReceiver();
        Log.d(TAG, "notification Listener onDestroy()");
    }

    public void disposeVoiceReceiver()
    {
        mVoiceGenerator.Shutdown();
        try {
            unregisterReceiver(mVoiceGenerator);
        }
        catch (IllegalArgumentException arg)
        {
            Log.e(TAG, "mVoiceGenerator is not registered!!!!!");
        }

    }

    private void registerVoiceReceivers()
    {
        mVoiceGenerator = new NotificationBroadcastReceiver(this);
        IntentFilter intentFilter = new IntentFilter(TAG);
        registerReceiver(mVoiceGenerator, intentFilter);
    }

    public void dummyFunction()
    {
        Log.d(TAG, "DummyFunction");
    }

    // TODO sprawdzać czy nie jest null
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Log.d(TAG, "**********  onNotificationPosted");
        Log.d(TAG, "ID : " + sbn.getId() + ",\t" + sbn.getNotification().tickerText + ",\t" + sbn.getPackageName());
        Helper.IterateBundleExtras(sbn.getNotification().extras);
        //Intent intent = createNotificationIntent(sbn);
        //sendBroadcast(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "********** onNOtificationRemoved");
        Log.d(TAG, "ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d(TAG, "onListenerConnected()");

    }

    /**
     * Function make possible to return custom binder to get access to instance of service
     * and be able to intercept notifications also.
     * @param intent
     * @return if its called with intent action "R.string.CustomIntent_NotificationCatcher"
     * returns custom binder
     */
    @Override
    public IBinder onBind(Intent intent)
    {
        if(intent.getAction().equals(CUSTOM_BINDING))
            return mBinder;
        else
            return super.onBind(intent);

    }

    @Override
    public boolean onUnbind(Intent intent) {
        if(intent.getAction().equals(CUSTOM_BINDING)) {
            NotificationServiceConnection.getInstance().onServiceDisconnected(new ComponentName(this, this.getClass()));
        }
        return super.onUnbind(intent);

    }

    //TODO when finish with testing change return type to Intent
    //TODO make Bundle ready with addition of package name, label,
    private Intent createNotificationIntent(StatusBarNotification sbn)
    {
        Intent intent = new Intent();

        Log.d(TAG, "creating Notification, ");
        Notification notification = sbn.getNotification();
        String packageName = sbn.getPackageName();
        Bundle bundle = notification.extras;
        bundle.putString(NOTIFICATION_PACKAGE_NAME,packageName);
        bundle.putString(NOTIFICATION_APPLICATION_LABEL, Helper.getApplicationLabel(packageName, this));
        intent.putExtras(bundle);
        return intent;
    }




    /**
     * custom binder class to get access to instance of service and its classes from VoiceNotificationActivity in particular.
     */
    public class NotificationCatcherBinder extends Binder
    {
        public NotificationService getService()
        {
            return NotificationService.this;
        }
    }
}
