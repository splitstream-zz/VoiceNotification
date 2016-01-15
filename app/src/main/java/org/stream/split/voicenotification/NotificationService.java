package org.stream.split.voicenotification;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Enities.NotificationEntity;
import org.stream.split.voicenotification.Exceptions.ExceptionHandler;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.Logging.BaseLogger;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by split on 2015-10-18.
 */
public class NotificationService extends NotificationListenerService {

    public static final String TAG = "NotificationService";
    public static final String CUSTOM_BINDING = "org.stream.split.voicenotification.CustomIntent_NotificationCatcher";
    public static final String CUSTOM_BINDING_SEND_NOTIFICATION = "org.stream.split.voicenotification.CustomIntent_NotificationCatcher.send_notification";
    public static final String NOTIFICATION_OBJECT = "new_notification_object";
    public static final String ACTION_NOTIFICATION_POSTED = TAG + ".notificationPosted";
    public static final String ACTION_NOTIFICATION_REMOVED = TAG + ".notificationRemoved";
    private static boolean mIsSystemNotificationServiceConnected = false;
    public static boolean isNotificationRelayActive()
    {
        return mIsSystemNotificationServiceConnected;
    }
    private static BaseLogger LOGGER = BaseLogger.getInstance();

    private List<BroadcastReceiver> mReceivers = new ArrayList<>();
    private NotificationBroadcastReceiver mVoiceGenerator;
    private static boolean mIsVoiceActive = false;

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

        //Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler(this.getBaseContext()));

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

    public void initializeService(boolean isSpeechModuleActive, List<BroadcastReceiver> receivers)
    {
        setVoiceActive(isSpeechModuleActive);
        for(BroadcastReceiver receiver:receivers)
            this.registerReceiver(receiver);
    }


    public int registerReceiver(@NonNull BroadcastReceiver receiver)
    {
        Log.d(TAG, "registeringReceiver " + receiver.getClass().getSimpleName());
        LOGGER.d(TAG, "mreceivers.size() = " + String.valueOf(mReceivers.size()));
        int result = -1;
        if(!isRegisteredReceiver(receiver)) {

            mReceivers.add(receiver);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_NOTIFICATION_POSTED);
            super.registerReceiver(receiver, intentFilter);
            LOGGER.d(TAG, receiver.getClass().getSimpleName() + " was successfully registered");
            result = 0;
        }
        else {
            LOGGER.d(TAG, receiver.getClass().getSimpleName() + " was already registered");
            result = 1;
        }
        return result;
    }

    public boolean isRegisteredReceiver(BroadcastReceiver receiver)
    {
        boolean isRegistered = false;
        for(BroadcastReceiver receiverEntity: mReceivers)
        {
            if(receiverEntity == receiver)
                isRegistered = true;

        }
        return isRegistered;
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
        Log.d(TAG, "unregisteringReceiver");
        Log.d(TAG, "mBroadcastReceivers.size(): " + mReceivers.size());
        boolean isDeleted = false;

        try {
            isDeleted = mReceivers.remove(receiver);

            if(isDeleted) {
                super.unregisterReceiver(receiver);
                Log.d(TAG, receiver.toString() + " was unregistered");
                Log.d(TAG, "mBroadcastReceivers.size(): " + mReceivers.size() + "\tisDeleted: " + isDeleted);
            }
        } catch (IllegalArgumentException arg) {
            Log.d(TAG, receiver.toString() + " is not registered");
        }

    }

    private void unregisterAllReceivers() {
        for(BroadcastReceiver receiver:mReceivers)
            unregisterReceiver(receiver);
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

    private void unregisterVoiceReceiver()
    {
        Log.d(TAG, "unregisterVoiceReciver Before");
        if(mVoiceGenerator != null) {
            this.mVoiceGenerator.Shutdown();
            try {
                unregisterReceiver(mVoiceGenerator);
                LOGGER.d(TAG, "unregisterVoiceReciver Inside");
            } catch (IllegalArgumentException arg) {
                Log.e(TAG, "mVoiceGenerator is not registered!!!!!");
            }
            mVoiceGenerator = null;
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(TAG, "**********  onNotificationPosted");
        Log.d(TAG, "Id : " + sbn.getId() + ",\tTAG: " + sbn.getTag() + ",\tpackagename:" + sbn.getPackageName());
        if(sbn.getNotification().tickerText != null)
            Log.d(TAG, "TickerText: " + sbn.getNotification().tickerText);

        NotificationEntity newNotificationEntity = createNotification(sbn);

        StringBuilder builder = Helper.LogNotificationEntity(newNotificationEntity);
        Log.d(TAG, builder.toString());
        Log.d(TAG, "Newly inserted notification Id: " + newNotificationEntity.getID());

        Intent intent = new Intent();
        intent.setAction(ACTION_NOTIFICATION_POSTED);
        intent.putExtra(NOTIFICATION_OBJECT, new Gson().toJson(newNotificationEntity));
        sendBroadcast(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "********** onNOtificationRemoved");
        Log.d(TAG, "COLUMN_NAME_ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
        Intent intent = new Intent(ACTION_NOTIFICATION_REMOVED);
        sendBroadcast(intent);
    }
    private NotificationEntity createNotification(StatusBarNotification sbn)
    {

        String label = Helper.getApplicationLabel(sbn.getPackageName(),this);

        NotificationEntity newNotificationEntity = new NotificationEntity(sbn.getId(),
                sbn.getPackageName(),
                label,
                sbn.getPostTime());

        List<BundleKeyEntity> bundles = Helper.IterateBundleExtras(sbn.getNotification().extras, sbn.getPackageName());

        newNotificationEntity.setBundleKeys(bundles);
        if (sbn.getNotification().tickerText != null) {
            newNotificationEntity.setTinkerText(sbn.getNotification().tickerText.toString());
            newNotificationEntity.addBundleKey("custom.tickerText", sbn.getNotification().tickerText.toString());
        }

        DBHelper db = new DBHelper(this);
        long rowId = db.addNotification(newNotificationEntity);
        newNotificationEntity.setID(rowId);
        boolean isFollowed = db.isAppFollowed(sbn.getPackageName());
        newNotificationEntity.setIsFollowed(isFollowed);

        if(newNotificationEntity.isFollowed()) {
            for(BundleKeyEntity entity:newNotificationEntity.getBundleKeys())
                entity.setIsFollowed(db.isFollowed(entity));
        }
        db.close();
        return newNotificationEntity;
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
        Log.d(TAG, "onBind() intent.getAction(): " + intent.getAction());
        if(intent.getAction().equals(CUSTOM_BINDING))
            return new NotificationCatcherBinder();
//        else if(intent.getAction().equals(CUSTOM_BINDING_SEND_NOTIFICATION))
//        {
//
//        }
        else {
            Log.d(TAG, "onBind else intent.getAction(): " + intent.getAction());
            mIsSystemNotificationServiceConnected = true;
            return super.onBind(intent);
        }

    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind() intent.getAction(): " + intent.getAction());
        if(!intent.getAction().equals(CUSTOM_BINDING)) {
            Log.d(TAG, "!!!!!!!NotificationListenerService unbinded - trying to onBind(intent)");
            mIsSystemNotificationServiceConnected = false;
            //todo not sure if this onBind is going to help
            onBind(intent);
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
        public void registerReceiver(BroadcastReceiver receiver)
        {
            NotificationService.this.registerReceiver(receiver);
        }
        public void unregisterReceiver(BroadcastReceiver receiver)
        {
            NotificationService.this.unregisterReceiver(receiver);
        }
        public void setVoiceActive(boolean isActive)
        {
            NotificationService.this.setVoiceActive(isActive);
        }
        public void unregisterAllReceivers()
        {
            NotificationService.this.unregisterAllReceivers();
        }
        public void sendTestNotification(StatusBarNotification sbn)
        {
            NotificationService.this.onNotificationPosted(sbn);
        }
    }

}
