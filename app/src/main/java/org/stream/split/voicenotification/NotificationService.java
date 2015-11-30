package org.stream.split.voicenotification;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.google.gson.Gson;

import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.NotificationEntity;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.Helpers.NotificationServiceConnection;


/**
 * Created by split on 2015-10-18.
 */
public class NotificationService extends NotificationListenerService {

    public static final String TAG = "NotificationService";
    public static final String NOTIFICATION_OBJECT = "notification_object";
    public static final String ACTION_NOTIFICATION_POSTED = TAG + ".notificationPosted";
    public static final String ACTION_NOTIFICATION_REMOVED = TAG + ".notificationRemoved";

    public static final String CUSTOM_BINDING = "org.stream.split.voicenotification.CustomIntent_NotificationCatcher";
    private final IBinder mBinder = new NotificationCatcherBinder();
    private NotificationBroadcastReceiver mVoiceGenerator;

    private boolean mIsVoiceActive = false;

    public boolean isVoiceActive() {
        return mIsVoiceActive;
    }

    public void setVoiceActive(boolean isVoiceActive) {

            if (isVoiceActive && !mIsVoiceActive) {
                this.registerVoiceReceivers();
            }
            else
                this.unregisterVoiceReceiver();
        mIsVoiceActive = isVoiceActive;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Notification Listener created!");
        //TODO registerVoiceReceivers should be invoked only by setVoiceActive?
        //registerVoiceReceivers();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterVoiceReceiver();
        Log.d(TAG, "notification Listener onDestroy()");
    }

    private void unregisterVoiceReceiver()
    {
        Log.d(TAG, "unregisterVoiceReciver Before");
        if(mVoiceGenerator != null) {
            Log.d(TAG, "unregisterVoiceReciver Inside");
            this.mVoiceGenerator.Shutdown();

            try {
                unregisterReceiver(mVoiceGenerator);
            } catch (IllegalArgumentException arg) {
                Log.e(TAG, "mVoiceGenerator is not registered!!!!!");
            }
            mVoiceGenerator = null;
        }
    }

    private void registerVoiceReceivers()
    {
        Log.d(TAG, "registerVoiceReciver Before");
        if(mVoiceGenerator == null) {
            Log.d(TAG, "registerVoiceReciver Inside");
            mVoiceGenerator = new NotificationBroadcastReceiver(this);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_NOTIFICATION_POSTED);
            intentFilter.addAction(ACTION_NOTIFICATION_REMOVED);
            registerReceiver(mVoiceGenerator, intentFilter);
        }

    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Log.d(TAG, "**********  onNotificationPosted");
        Log.d(TAG, "ID : " + sbn.getId() + ",\tTAG: " + sbn.getTag() + ",\tNumber: " + sbn.getNotification().number + "\t" + sbn.getPackageName());
        Log.d(TAG, "TickerText: " + sbn.getNotification().tickerText);

        String label = Helper.getApplicationLabel(sbn.getPackageName(), this);
        NotificationEntity notificationEntity = Helper.createNotificationEntity(sbn, label);
        DBHelper db = new DBHelper(this);
        notificationEntity = db.addNotification(notificationEntity);
        db.close();
        Log.d(TAG, "Newly inserted notification Id: " + notificationEntity.getID());

        if (isVoiceActive()) {
            Intent intent = new Intent();
            intent.setAction(ACTION_NOTIFICATION_POSTED);
            intent.putExtra(NOTIFICATION_OBJECT, new Gson().toJson(notificationEntity));
            sendBroadcast(intent);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "********** onNOtificationRemoved");
        Log.d(TAG, "ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
        Intent intent = new Intent(ACTION_NOTIFICATION_REMOVED);
        sendBroadcast(intent);
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
