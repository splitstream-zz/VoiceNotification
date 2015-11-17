package org.stream.split.voicenotification;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.nfc.Tag;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by split on 2015-11-13.
 */
public class NotificationServiceConnection implements ServiceConnection {

    private final String TAG = "NotificationServiceConnection";
    private NotificationCatcherService mNotificationService;
    private boolean mServiceBound;
    private List<ReceiverIntent> mReceivers = new ArrayList<>();
    private static NotificationServiceConnection mNotificationServiceConnection;


    public boolean isServiceBound()
    {
        return mServiceBound;
    }

    private NotificationServiceConnection()
    {}

    public static NotificationServiceConnection getInstance()
    {
        if(mNotificationServiceConnection == null)
            mNotificationServiceConnection = new NotificationServiceConnection();

        return mNotificationServiceConnection;
    }
    private void addReceiver(@NonNull BroadcastReceiver receiver, IntentFilter filter)
    {
        if(filter == null)
            filter = new IntentFilter();

        mReceivers.add(new ReceiverIntent(receiver,filter));
    }
    public int registerReceiver(@NonNull BroadcastReceiver receiver, IntentFilter filter)
    {
        Log.d(TAG, "registeringReceiver");

        if(!isRegisteredReceiver(receiver)) {
            mReceivers.add(new ReceiverIntent(receiver,filter));
        }
        if(isServiceBound())
        {
            Log.d(TAG, "registering: " + receiver.toString());
            mNotificationService.registerReceiver(receiver,filter);
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
            for(ReceiverIntent receiver:mReceivers)
            {
                this.registerReceiver(receiver.mReciver, receiver.mIntentFilter);
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

    public void unregisterAllRecivers()
    {
        if(isServiceBound())
            for(ReceiverIntent receiver:mReceivers)
            {
                    this.unregisterReceiver(receiver.mReciver);

            }
    }

    public void unregisterReceiver(BroadcastReceiver receiver)
    {
        if(isServiceBound()) {
            try{
                Log.d(TAG, receiver.toString()+ " was unregistered");
                mNotificationService.unregisterReceiver(receiver);
                mReceivers.remove(receiver);
            }
            catch(IllegalArgumentException arg)
            {
                Log.d(TAG, receiver.toString()+ " is not registered");
            }
        }
    }
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if(!isServiceBound()) {
            NotificationCatcherService.NotificationCatcherBinder binder = (NotificationCatcherService.NotificationCatcherBinder) service;
            mNotificationService = binder.getService();
            mServiceBound = true;
            registerAllReceivers();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mServiceBound = false;
        mNotificationService = null;
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
