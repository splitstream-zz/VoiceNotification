package org.stream.split.voicenotification.Helpers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;

import org.stream.split.voicenotification.Logging.BaseLogger;
import org.stream.split.voicenotification.NotificationService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by split on 2015-11-13.
 */
public class NotificationServiceConnection implements ServiceConnection {

    private static NotificationServiceConnection mNotificationServiceConnection;
    private final String TAG = "NotificServConn";
    private NotificationService.NotificationCatcherBinder mNotificationService;
    private List<BroadcastReceiver> mBroadcastReceivers = new ArrayList<>();
    private boolean mIsVoiceActive = false;
    private Context mContext;
    private BaseLogger logger = BaseLogger.getInstance();

    public static NotificationServiceConnection getInstance()
    {
        if(mNotificationServiceConnection == null)
            mNotificationServiceConnection = new NotificationServiceConnection();
        return mNotificationServiceConnection;
    }
    private NotificationServiceConnection()
    {

    }
    public void initializeServiceState(Context context)
    {
        mContext = context;
    }


    public void setActiveSpeechService(boolean isVoiceActive) {
        logger.d(TAG, "setActiveSpeechService("+isVoiceActive+")");
        this.mIsVoiceActive = isVoiceActive;
        bindService();
    }

    public void registerReceiver(@NonNull BroadcastReceiver receiver)
    {
        logger.d(TAG,"registering receiver" + receiver.getClass().getSimpleName());
        mBroadcastReceivers.add(receiver);
        bindService();
    }
    public void unregisterReceiver(@NonNull BroadcastReceiver receiver)
    {
        mBroadcastReceivers.remove(receiver);
        bindService();
    }

    private void bindService()
    {
        logger.d(TAG, "binding");
        Intent intent = new Intent(mContext,NotificationService.class);
        intent.setAction(NotificationService.CUSTOM_BINDING);
        mContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        logger.d(TAG, "onServiceConnected");
        logger.d(TAG, service.getClass().getCanonicalName());
        if(service instanceof NotificationService.NotificationCatcherBinder) {
            mNotificationService = (NotificationService.NotificationCatcherBinder) service;
            for(BroadcastReceiver receiver:mBroadcastReceivers)
                mNotificationService.registerReceiver(receiver);
            mBroadcastReceivers.clear();
            mNotificationService.setVoiceActive(mIsVoiceActive);
            mContext.unbindService(this);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        logger.d(TAG,"!!!!!!!!!!!!Service was disconexted");
        mNotificationService = null;
        Intent intent = new Intent(mContext,name.getClass());
        mContext.startService(intent);
    }

    public void unregisterAllReceivers() {

    }

    public boolean isSpeechServiceActive() {
        return mIsVoiceActive;
    }
}


