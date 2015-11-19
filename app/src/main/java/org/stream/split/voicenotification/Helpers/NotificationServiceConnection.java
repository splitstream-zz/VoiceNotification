package org.stream.split.voicenotification.Helpers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RelativeLayout;

import org.stream.split.voicenotification.NotificationService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by split on 2015-11-13.
 */
public class NotificationServiceConnection implements ServiceConnection {

    private final String TAG = "NotificationServiceConnection";
    private NotificationService mNotificationService;
    private boolean mServiceBound;
    private List<ReceiverIntent> mIntentReceivers = new ArrayList<>();
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

        mIntentReceivers.add(new ReceiverIntent(receiver, filter));
    }
    public int registerReceiver(@NonNull BroadcastReceiver receiver, IntentFilter filter)
    {
        Log.d(TAG, "registeringReceiver");

        if(!isRegisteredReceiver(receiver)) {
            mIntentReceivers.add(new ReceiverIntent(receiver, filter));
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
            for(ReceiverIntent receiver: mIntentReceivers)
            {
                this.registerReceiver(receiver.mReciver, receiver.mIntentFilter);
            }
    }

    public boolean isRegisteredReceiver(BroadcastReceiver receiver)
    {
        boolean isRegistered = false;
        for(ReceiverIntent receiverIntent: mIntentReceivers)
        {
            if(receiverIntent.mReciver == receiver)
                isRegistered = true;

        }
        return isRegistered;
    }

    public void unregisterAllRecivers()
    {
        Log.d(TAG, "unregisteringAllReceivers");
        if(isServiceBound()) {
            for (ReceiverIntent receiver : mIntentReceivers) {
                this.unregisterReceiver(receiver.mReciver);
            }
        }
    }

    public void unregisterReceiver(BroadcastReceiver receiver)
    {
        Log.d(TAG, "unregisteringReceivers");
        Log.d(TAG, "mIntentReceivers.size(): " + mIntentReceivers.size());
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

        Log.d(TAG, "mIntentReceivers.size(): " + mIntentReceivers.size() + "\tisDeleted: " + isDeleted);
    }
    public void finish()
    {
        mNotificationService.disposeVoiceReceiver();
    }

    private boolean removeReceiver(BroadcastReceiver receiver)
    {
        boolean isDeleted = false;
        List<ReceiverIntent> list = new ArrayList<>();
        for(ReceiverIntent item:mIntentReceivers)
        {
            if(item.mReciver != receiver)
            {
                list.add(item);
            }
            else
            {
                isDeleted = true;
                Log.d(TAG, "receiver: " + receiver + " was removed");
            }
        }
        mIntentReceivers = list;
        return isDeleted;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if(!isServiceBound()) {
            NotificationService.NotificationCatcherBinder binder = (NotificationService.NotificationCatcherBinder) service;
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

class ReceiverIntent {
    public BroadcastReceiver mReciver;
    public IntentFilter mIntentFilter;
    public ReceiverIntent(BroadcastReceiver receiver, IntentFilter intentFilter)
    {
        mReciver = receiver;
        mIntentFilter = intentFilter;
    }

}
