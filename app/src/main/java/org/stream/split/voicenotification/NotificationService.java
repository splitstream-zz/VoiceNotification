package org.stream.split.voicenotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Enities.HistoryBundleKeyEntity;
import org.stream.split.voicenotification.Enities.HistoryNotificationEntity;
import org.stream.split.voicenotification.Exceptions.ExceptionHandler;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.Logging.BaseLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by split on 2015-10-18.
 */
public class NotificationService extends NotificationListenerService implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = "NotificationService";
    public static final String CUSTOM_BINDING = "org.stream.split.voicenotification.CustomIntent_NotificationCatcher";
    public static final String NEW_NOTIFICATION_OBJECT = "new_notification_object";
    public static final String LAST_NOTIFICATION_OBJECT = "last_notification_object";
    public static final String ACTION_NOTIFICATION_POSTED = TAG + ".notificationPosted";
    public static final String ACTION_NOTIFICATION_REMOVED = TAG + ".notificationRemoved";
    private static boolean mIsSystemNotificationServiceConnected = false;
    private static BaseLogger Logger = BaseLogger.getInstance();

    private SharedPreferences mSharedPreferences;
    private int mPersistentNotificationID = 69;
    private List<BroadcastReceiver> mReceivers = new ArrayList<>();
    private static NotificationBroadcastReceiver mVoiceGenerator;
    private boolean mIsVoiceActive = false;
    private boolean mIsPersistentNotification = true;


    public static boolean isNotificationRelayActive()
    {
        return mIsSystemNotificationServiceConnected;
    }
    private void setIsSystemNotificationServiceConnected(boolean isSystemNotificationServiceConnected) {
        NotificationService.mIsSystemNotificationServiceConnected = isSystemNotificationServiceConnected;
        updatePersistentAppNotification();
    }

    private void setIsPersistentNotification(boolean isPersistentNotification) {
        this.mIsPersistentNotification = isPersistentNotification;
        updatePersistentAppNotification();
    }

    private void setIsVoiceActive(boolean isVoiceActive) {

//        if (isVoiceActive && !mIsVoiceActive)
//            this.registerVoiceReceivers();
//        if (!isVoiceActive)
//            this.unregisterVoiceReceiver();
        mIsVoiceActive = isVoiceActive;
        mVoiceGenerator.setIsVoiceActive(isVoiceActive);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Notification Listener created!");
        Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler(this));
        registerVoiceReceivers();
        Resources res = this.getResources();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        initializeService(mSharedPreferences, res);

        updatePersistentAppNotification();
    }

    private void initializeService(SharedPreferences pref, Resources res)
    {
        Logger.d(TAG, "initializing service");

        Boolean isVoiceActive = pref.getBoolean(res.getString(R.string.pref_is_voice_active_key),false);
        Logger.d(TAG, "mIsVoiceActive = " + isVoiceActive);
        setIsVoiceActive(isVoiceActive);

        boolean isPersistentNotification = pref.getBoolean(res.getString(R.string.pref_is_persistent_notification_key),true);
        Logger.d(TAG, "mIsvoiceActive = " + mIsVoiceActive);
        setIsPersistentNotification(isPersistentNotification);
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
        unregisterAllReceivers();

        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);

        Log.d(TAG, "notification Listener onDestroy()");
    }




    public int registerReceiver(@NonNull BroadcastReceiver receiver)
    {
        Log.d(TAG, "registeringReceiver " + receiver.getClass().getSimpleName());
        Logger.d(TAG, "mreceivers.size() = " + String.valueOf(mReceivers.size()));
        int result = -1;
        if(!isRegisteredReceiver(receiver)) {

            mReceivers.add(receiver);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_NOTIFICATION_POSTED);
            super.registerReceiver(receiver, intentFilter);
            Logger.d(TAG, receiver.getClass().getSimpleName() + " was successfully registered");
            result = 0;
        }
        else {
            Logger.d(TAG, receiver.getClass().getSimpleName() + " was already registered");
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
        Iterator<BroadcastReceiver> i = mReceivers.iterator();
        while(i.hasNext())
        {
            BroadcastReceiver receiver = i.next();
            unregisterReceiver(receiver);
        }
    }

    private void updatePersistentAppNotification()
    {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Intent intent = new Intent(getApplicationContext(), VoiceNotificationActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Resources res = this.getResources();
            String title = res.getString(R.string.Persistent_notification_title);
            String tickerText = res.getString(R.string.Persistent_notification_ticker_text);
            String text;
            if (mIsVoiceActive) {
                if (mIsSystemNotificationServiceConnected)
                    text = res.getString(R.string.Persistent_notification_text_enabled);
                else
                    text = res.getString(R.string.Persistent_notification_text_no_notification_access);
            } else
                text = res.getString(R.string.Persistent_notification_text_disabled);

            Notification.Builder builder = new Notification.Builder(this.getBaseContext());
            builder.setContentTitle(title)
                    .setContentText(text)
                    .setOngoing(mIsPersistentNotification)
                    .setContentIntent(pendingIntent)
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setSmallIcon(R.drawable.ic_persistent_notification)
                    .setTicker(tickerText);


            notificationManager.notify(mPersistentNotificationID, builder.build());
        if(!mIsPersistentNotification)
            notificationManager.cancel(mPersistentNotificationID);

    }

    private synchronized void registerVoiceReceivers()
    {
        Log.d(TAG, "registerVoiceReciver");
        if(mVoiceGenerator == null) {
            Log.d(TAG, "mVoiceGenerator is being registered");
            mVoiceGenerator = new NotificationBroadcastReceiver(this);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_NOTIFICATION_POSTED);
            intentFilter.addAction(ACTION_NOTIFICATION_REMOVED);
            super.registerReceiver(mVoiceGenerator, intentFilter);
        }
        else
            Logger.d(TAG, "mVoiceGenerator != null");

    }

    private synchronized void unregisterVoiceReceiver()
    {
        Log.d(TAG, "unregisterVoiceReciver");
        if(mVoiceGenerator != null) {
            mVoiceGenerator.stop();
            try {
                super.unregisterReceiver(mVoiceGenerator);
                Logger.d(TAG, "mVoiceGenerator is being unregistered");
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
        if(!mReceivers.isEmpty() || mIsVoiceActive) {
            if (sbn.getNotification().tickerText != null)
                Log.d(TAG, "TickerText: " + sbn.getNotification().tickerText);

            DBHelper db = new DBHelper(this);

            HistoryNotificationEntity temp = createNotification(sbn);

            //first added to db and then load to get all needed data from db
            long rowId = db.addHistoryNotification(temp);
            HistoryNotificationEntity newHistoryNotificationEntity = db.getHistoryNotification(rowId, true);

            db.close();

            StringBuilder builder = Helper.LogNotificationEntity(newHistoryNotificationEntity);
            Log.d(TAG, builder.toString());
            Log.d(TAG, "Newly inserted notification Id: " + newHistoryNotificationEntity.getID());

            Intent intent = new Intent();
            intent.setAction(ACTION_NOTIFICATION_POSTED);
            intent.putExtra(NEW_NOTIFICATION_OBJECT, new Gson().toJson(newHistoryNotificationEntity));
            sendBroadcast(intent);
        }
        else
            Logger.d(TAG, "mReceivers().size = " + mReceivers.size() + " mIsVoiceActive = " + mIsVoiceActive);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "********** onNOtificationRemoved");
        Log.d(TAG, "COLUMN_NAME_ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
        if(!mReceivers.isEmpty() || mIsVoiceActive) {
            Intent intent = new Intent(ACTION_NOTIFICATION_REMOVED);
            sendBroadcast(intent);
        }
        else
            Logger.d(TAG, "mReceivers().size = " + mReceivers.size() + " mIsVoiceActive = " + mIsVoiceActive);

    }
    private HistoryNotificationEntity createNotification(StatusBarNotification sbn)
    {
        HistoryNotificationEntity newHistoryNotificationEntity = new HistoryNotificationEntity(sbn.getId(),
                sbn.getPackageName(),
                sbn.getPostTime());

        List<HistoryBundleKeyEntity> bundles = Helper.IterateBundleExtras(sbn.getNotification().extras, newHistoryNotificationEntity);

        newHistoryNotificationEntity.setBundleKeys(bundles);
        if (sbn.getNotification().tickerText != null) {
            newHistoryNotificationEntity.setTinkerText(sbn.getNotification().tickerText.toString());
            HistoryBundleKeyEntity entity = new HistoryBundleKeyEntity(newHistoryNotificationEntity,"custom.tickerText", sbn.getNotification().tickerText.toString());
            newHistoryNotificationEntity.addBundleKey(entity);
        }

        return newHistoryNotificationEntity;
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
        else {
            Log.d(TAG, "onBind else intent.getAction(): " + intent.getAction());
            setIsSystemNotificationServiceConnected(true);
            return super.onBind(intent);
        }

    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind() intent.getAction(): " + intent.getAction());
        if(!intent.getAction().equals(CUSTOM_BINDING)) {
            Log.d(TAG, "!!!!!!!NotificationListenerService unbinded - trying to onBind(intent)");
            setIsSystemNotificationServiceConnected(false);
            //todo not sure if this onBind is going to help
            onBind(intent);
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Resources res = this.getResources();
        Logger.d(TAG, "onSharedPreferencesChanged, key = " + key);
        if(key.equals(res.getString(R.string.pref_is_persistent_notification_key)))
            setIsPersistentNotification(sharedPreferences.getBoolean(key, true));
        if(key.equals(res.getString(R.string.pref_is_voice_active_key)))
            setIsVoiceActive(sharedPreferences.getBoolean(key, false));
        updatePersistentAppNotification();
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
            NotificationService.this.setIsVoiceActive(isActive);
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
