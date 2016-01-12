package org.stream.split.voicenotification.Helpers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.util.Log;

import org.stream.split.voicenotification.NotificationService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by split on 2015-11-13.
 */
public class NotificationServiceConnection implements ServiceConnection {

    private final String TAG = "NotificServConn";
    private NotificationService mNotificationService;
    private boolean mServiceBound;
    private List<BroadcastReceiver> mBroadcastReceivers = new ArrayList<>();
    private boolean mIsVoiceActive = false;
    private static NotificationServiceConnection mNotificationServiceConnection;
    private Context mContext;

    public static NotificationServiceConnection getInstance()
    {
        if(mNotificationServiceConnection == null)
            mNotificationServiceConnection = new NotificationServiceConnection();
        return mNotificationServiceConnection;
    }
    private NotificationServiceConnection()
    {

    }
    public void initializeServiceContection(Context context)
    {
        mContext = context;
    }

    public boolean isServiceBound()
    {
        return mServiceBound;
    }

    public void setActiveSpeechService(boolean isVoiceActive) {
        this.mIsVoiceActive = isVoiceActive;
        if(mServiceBound)
            mNotificationService.setVoiceActive(isVoiceActive);
    }
    public void sentTestNotification(StatusBarNotification sbn)
    {
        if(mServiceBound)
           mNotificationService.onNotificationPosted(sbn);
    }
    public void registerReceiver(@NonNull BroadcastReceiver receiver)
    {
        mBroadcastReceivers.add(receiver);
    }
    public void unregisterReceiver(@NonNull BroadcastReceiver receiver)
    {
        mBroadcastReceivers.remove(receiver);
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if(service instanceof NotificationService.NotificationCatcherBinder) {
            NotificationService.NotificationCatcherBinder binder = (NotificationService.NotificationCatcherBinder) service;
            mNotificationService = binder.getService();
            mServiceBound = true;
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mServiceBound = false;
        mNotificationService = null;
        //mContext.start
    }
}


