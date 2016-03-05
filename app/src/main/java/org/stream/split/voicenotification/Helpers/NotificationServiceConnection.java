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
    private boolean mIsServiceBound;

    public static NotificationServiceConnection getInstance()
    {
        if(mNotificationServiceConnection == null)
            mNotificationServiceConnection = new NotificationServiceConnection();
        return mNotificationServiceConnection;
    }
    private NotificationServiceConnection()
    {

    }

    public void setActiveSpeechService(boolean isVoiceActive) {
        if(mIsServiceBound)
            mNotificationService.setVoiceActive(isVoiceActive);
        logger.d(TAG, "setActiveSpeechService("+isVoiceActive+")");
        this.mIsVoiceActive = isVoiceActive;
    }

    public void registerReceiver(@NonNull BroadcastReceiver receiver)
    {
        logger.d(TAG, "registering receiver" + receiver.getClass().getSimpleName());
        if(mIsServiceBound)
            mNotificationService.registerReceiver(receiver);
        else
            mBroadcastReceivers.add(receiver);
    }
    public void unregisterReceiver(@NonNull BroadcastReceiver receiver) {
        if (mIsServiceBound)
            mNotificationService.unregisterReceiver(receiver);
        else
            mBroadcastReceivers.remove(receiver);

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
            mIsServiceBound = true;
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mIsServiceBound = false;
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

    public void sendTestNotification(StatusBarNotification sbn) {
        if(mIsServiceBound)
            mNotificationService.sendTestNotification(sbn);
    }
}


