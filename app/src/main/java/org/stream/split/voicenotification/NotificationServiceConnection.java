package org.stream.split.voicenotification;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by split on 2015-11-13.
 */
public class NotificationServiceConnection implements ServiceConnection {

    private NotificationCatcherService mNotificationService;
    private boolean mServiceBound;
    private List<ReceiverIntent> mReceivers = new ArrayList<>();
    private Context mContext;
    private static NotificationServiceConnection mNotificationServiceConnection;


    public boolean isServiceBound()
    {
        return mServiceBound;
    }

    public boolean isServiceConntected()
    {
        boolean isConnected = false;
        if(mContext != null)
            isConnected = true;
        return isConnected;
    }

    private NotificationServiceConnection(Context context)
    { mContext = context; }

    private NotificationServiceConnection()
    {}

    /**
     * Function should be called only once from Service where Broadcasts will be send from.
     * @param context service to register BroadcastReceivers
     * @return
     */
    public synchronized NotificationServiceConnection init(@NonNull Context context)
    {

        if(mContext == null)
        {
            mContext = context;
            mNotificationServiceConnection.registerReceivers();
        }

        return mNotificationServiceConnection;
    }

    public static NotificationServiceConnection getInstance()
    {
        if(mNotificationServiceConnection == null)
            mNotificationServiceConnection = new NotificationServiceConnection();

        return mNotificationServiceConnection;
    }
    public void addReceiver(@NonNull BroadcastReceiver receiver, IntentFilter filter)
    {
        if(filter == null)
            filter = new IntentFilter();

        mReceivers.add(new ReceiverIntent(receiver,filter));
    }
    public int registerReceiver(@NonNull BroadcastReceiver receiver, IntentFilter filter)
    {
        if(!isRegisteredReceiver(receiver)) {
            mReceivers.add(new ReceiverIntent(receiver,filter));
        }
        if(mContext != null)
        {
            mContext.registerReceiver(receiver,filter);
            return 1;
        }
        else
            return -1;
    }

    private void registerReceivers()
    {
        if(mContext != null)
            for(ReceiverIntent receiver:mReceivers)
            {
                mContext.registerReceiver(receiver.mReciver,receiver.mIntentFilter);
            }
    }

    public boolean isRegisteredReceiver(BroadcastReceiver receiver)
    {
        boolean isRegistered = false;
        for(ReceiverIntent receiverIntent:mReceivers)
        {
            if(receiverIntent.mReciver == receiver)
                isRegistered = true;

        }
        return isRegistered;
    }

    public void unregisterRecivers()
    {
        if(mContext != null)
            for(ReceiverIntent receiver:mReceivers)
            {
                mContext.unregisterReceiver(receiver.mReciver);
            }
    }

    public void unregisterReceiver(BroadcastReceiver receiver)
    {
        if(mContext !=null)
            mContext.unregisterReceiver(receiver);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        NotificationCatcherService.NotificationCatcherBinder binder = (NotificationCatcherService.NotificationCatcherBinder) service;
        mNotificationService = binder.getService();
        mServiceBound = true;
        //RegisterReceivers();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mServiceBound = false;
    }
}

class ReceiverIntent{
    public BroadcastReceiver mReciver;
    public IntentFilter mIntentFilter;
    public ReceiverIntent(BroadcastReceiver receiver, IntentFilter intentFilter)
    {
        mReciver = receiver;
        mIntentFilter = intentFilter;
    }
}
