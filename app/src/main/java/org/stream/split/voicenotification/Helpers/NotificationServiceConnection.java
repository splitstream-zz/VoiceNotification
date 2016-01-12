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
    private static NotificationServiceConnection mNotificationServiceConnection;
    private Context mContext;

    public NotificationServiceConnection(Context context)
    {
        mContext = context;
        mContext.bindService(new Intent(NotificationService.CUSTOM_BINDING), this,Context.BIND_NOT_FOREGROUND);
    }



    public void setActiveSpeechService(boolean isVoiceActive) {
        this.mIsVoiceActive = isVoiceActive;
        if(mServiceBound)
            mNotificationService.setVoiceActive(isVoiceActive);
    }

    public boolean IsVoiceActive()
    {
        boolean isActive = false;
        Log.d(TAG, "serviceBound = " + mServiceBound);
        if(mServiceBound) {
            isActive = mNotificationService.isVoiceActive();
            Log.d(TAG, "isActive = " + isActive);
        }
        return isActive;
    }

    private boolean mIsVoiceActive = false;


    public boolean isServiceBound()
    {
        return mServiceBound;
    }

    public void sentTestNotification(StatusBarNotification sbn)
    {
        if(mServiceBound)
           mNotificationService.onNotificationPosted(sbn);
    }

    private NotificationServiceConnection()
    {}

    public static NotificationServiceConnection getInstance()
    {
        if(mNotificationServiceConnection == null)
            mNotificationServiceConnection = new NotificationServiceConnection();

        return mNotificationServiceConnection;
    }
    private void addReceiver(@NonNull BroadcastReceiver receiver)
    {

        mBroadcastReceivers.add(receiver);
    }
    public int registerReceiver(@NonNull BroadcastReceiver receiver)
    {
        Log.d(TAG, "registeringReceiver");

        if(!isRegisteredReceiver(receiver)) {
            mBroadcastReceivers.add(receiver);
        }
        if(isServiceBound())
        {
            Log.d(TAG, "registering: " + receiver.toString());
            mNotificationService.registerReceiver(receiver,new IntentFilter());
            return 1;
        }
        else {
            Log.d(TAG, "mNotificationService: " + String.valueOf(mNotificationService == null));
            return -1;
        }
    }

    private void registerAllReceivers()
    {
        Log.d(TAG, "registeringAllReceivers");
        if(isServiceBound())
            for(BroadcastReceiver receiver: mBroadcastReceivers)
            {
                this.registerReceiver(receiver);
            }
    }

    public boolean isRegisteredReceiver(BroadcastReceiver receiver)
    {
        boolean isRegistered = false;
        for(BroadcastReceiver receiverEntity: mBroadcastReceivers)
        {
            if(receiverEntity.getClass().equals(receiver.getClass()))
                isRegistered = true;

        }
        return isRegistered;
    }

    public void unregisterAllRecivers()
    {
        Log.d(TAG, "unregisteringAllReceivers");
        if(isServiceBound()) {
            for (ReceiverIntent receiver : mBroadcastReceivers) {
                this.unregisterReceiver(receiver.mReciver);
            }
        }
    }

    public void unregisterReceiver(BroadcastReceiver receiver)
    {
        Log.d(TAG, "unregisteringReceivers");
        Log.d(TAG, "mBroadcastReceivers.size(): " + mBroadcastReceivers.size());
        boolean isDeleted = false;

        if(isServiceBound()) {
            try{
                Log.d(TAG, receiver.toString() + " was unregistered");
                mNotificationService.unregisterReceiver(receiver);
            }
            catch(IllegalArgumentException arg)
            {
                Log.d(TAG, receiver.toString()+ " is not registered");
            }
        }

        isDeleted = removeReceiver(receiver);

        Log.d(TAG, "mBroadcastReceivers.size(): " + mBroadcastReceivers.size() + "\tisDeleted: " + isDeleted);
    }

    private boolean removeReceiver(BroadcastReceiver receiver)
    {
        boolean isDeleted = false;
        List<BroadcastReceiver> list = new ArrayList<>();
        for(BroadcastReceiver item: mBroadcastReceivers)
        {
            if(item != receiver)
            {
                list.add(item);
            }
            else
            {
                isDeleted = true;
                Log.d(TAG, "receiver: " + receiver + " was removed");
            }
        }
        mBroadcastReceivers = list;
        return isDeleted;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if(service instanceof NotificationService.NotificationCatcherBinder) {
            NotificationService.NotificationCatcherBinder binder = (NotificationService.NotificationCatcherBinder) service;
            mNotificationService = binder.getService();
            mServiceBound = true;
            mNotificationService.setVoiceActive(mIsVoiceActive);
            registerAllReceivers();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mServiceBound = false;
        mNotificationService = null;
        mContext.start
    }
}


