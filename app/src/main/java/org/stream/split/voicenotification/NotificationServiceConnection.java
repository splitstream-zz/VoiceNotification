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
    private NotificationServiceConnection mNotificationServiceConnection;


    public boolean isServiceBound()
    {
        return mServiceBound;
    }

    private NotificationServiceConnection(Context context)
    { mContext = context; }

    /**
     * Function should be called only once from Service where Broadcasts will be send from.
     * @param context service to register BroadcastReceivers
     * @return
     */
    public static synchronized NotificationServiceConnection init(Context context)
    {

        if(mNotificationServiceConnection == null)
        {
            mNotificationServiceConnection = new NotificationServiceConnection(context);
        }

        return mNotificationServiceConnection;
    }

    public void addReceiver(@NonNull BroadcastReceiver receiver, IntentFilter filter)
    {
        if(filter == null)
            filter = new IntentFilter();

        mReceivers.add(new ReceiverIntent(receiver,filter));
    }
    private int RegisterReceivers()
    {
        if(!mReceivers.isEmpty()) {
            for(ReceiverIntent receiverIntent:mReceivers)
            {
               mContext.registerReceiver(receiverIntent.mReciver, receiverIntent.mIntentFilter);
                ((NotificationCatcherService)mContext).dummyFunction();
            }
            return 0;
        }
        else
            return -1;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        NotificationCatcherService.NotificationCatcherBinder binder = (NotificationCatcherService.NotificationCatcherBinder) service;
        mNotificationService = binder.getService();

        mServiceBound = true;
        RegisterReceivers();
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
