package org.stream.split.voicenotification;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * Created by split on 2015-10-18.
 */
public class NotificationCatcherService extends NotificationListenerService {

    public static final String TAG = "NotificationCatcherServicen";
    public static final String NOTIFICATION_OBJECT = "notification_object";
    public static final String NOTIFICATION_POST_TIME = "notification_Post_Time";
    public static final String NOTIFICAITON_PACKAGE_NAME = "notification_pakageName";
    public static final String NOTIFICATION_APPLICATION_LABEL = "notification_application_name";
    public static final String CUSTOM_BINDING = "org.stream.split.voicenotification.CustomIntent_NotificationCatcher";

    private final IBinder mBinder = new NotificationCatcherBinder();

    private NotificationServiceConnection mConnection;
    private NotificationBroadcastReceiver mVoiceGenerator;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Notification Listener created!");
        initializeVoiceReceivers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mVoiceGenerator);
        Log.d(TAG, "notification Listener onDestroy()");
    }

    public void initializeVoiceReceivers()
    {
        mVoiceGenerator = new NotificationBroadcastReceiver(this);
        IntentFilter intentFilter = new IntentFilter(TAG);
        NotificationServiceConnection.getInstance().registerReceiver(mVoiceGenerator, intentFilter);

    }


    public void addBroadcastFilter(IntentFilter intentFilter)
    {
        addBroadcastFilter(intentFilter);
    }

    public void dummyFunction()
    {
        Log.d(TAG, "DummyFunction");
    }

    // TODO sprawdzać czy nie jest null
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {


        Log.d(TAG, "**********  onNotificationPosted");
        Log.d(TAG, "ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
        Intent intent = createNotificationIntent(sbn);
        sendBroadcast(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "********** onNOtificationRemoved");
        Log.d(TAG, "ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();

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

    private Intent createNotificationIntent(StatusBarNotification sbn)
    {
        Intent intent = new Intent(TAG);
        intent.putExtra(NOTIFICAITON_PACKAGE_NAME,sbn.getPackageName());
        intent.putExtra(NOTIFICATION_OBJECT,sbn.getNotification());
        intent.putExtra(NOTIFICATION_POST_TIME,sbn.getPostTime());
        PackageManager packageManager = getPackageManager();
        String label;
        try {
            label = packageManager.getApplicationLabel(packageManager.getApplicationInfo(sbn.getPackageName(), 0)).toString();
        }catch (PackageManager.NameNotFoundException arg){
            Log.d(TAG,"!!!!!!!!!!!! getting label not possible - NameNotFound");
            label = "";
        }
        intent.putExtra(NOTIFICATION_APPLICATION_LABEL, label);
        return intent;
    }
    /**
     * custom binder class to get access to instance of service and its classes from VoiceNotificationActivity in particular.
     */
    public class NotificationCatcherBinder extends Binder
    {
        public NotificationCatcherService getService()
        {
            return NotificationCatcherService.this;
        }
    }
}
