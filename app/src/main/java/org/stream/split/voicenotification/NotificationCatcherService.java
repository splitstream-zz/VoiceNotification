package org.stream.split.voicenotification;

import android.app.Notification;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.stream.split.voicenotification.BussinessLayer.AppInfoEntity;
import org.stream.split.voicenotification.DataAccessLayer.DBHelper;

/**
 * Created by split on 2015-10-18.
 */
public class NotificationCatcherService extends NotificationListenerService {

    public static final String TAG = "NotificationCatcherServicen";

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
        mConnection.unregisterRecivers();
        mVoiceGenerator.Shutdown();
    }

    public void initializeVoiceReceivers()
    {
        mVoiceGenerator = new NotificationBroadcastReceiver(this);
        IntentFilter intentFilter = new IntentFilter(TAG);
        mConnection = NotificationServiceConnection.getInstance();
        mConnection.init(this);
        mConnection.registerReceiver(mVoiceGenerator, intentFilter);

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
        Intent intent = new Intent(TAG);
        intent.putExtra("notification_pakageName",sbn.getPackageName());
        intent.putExtra("notification_id",sbn.getNotification().when);
        intent.putExtra("notification_event",sbn.getNotification().tickerText);
        intent.putExtra(Notification.EXTRA_TITLE, sbn.getNotification().extras.getString(Notification.EXTRA_TITLE));
        intent.putExtra(Notification.EXTRA_TEXT, sbn.getNotification().extras.getString(Notification.EXTRA_TEXT));
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
