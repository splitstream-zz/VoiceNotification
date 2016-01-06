package org.stream.split.voicenotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;

import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Enities.NotificationEntity;
import org.stream.split.voicenotification.DataAccessLayer.DBHelper;

import java.util.List;

/**
 * Created by split on 2015-10-19.
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = "NotBrodRec";
    private boolean mAutostart = true;
    private SpeechModule mSpeechModule;

    public NotificationBroadcastReceiver(Context context)
    {
        mSpeechModule = new SpeechModule(context);
    }

    @Override
    public synchronized void onReceive(Context context, final Intent intent) {

        Log.d(TAG, "OnReceive()");
        Bundle extras = intent.getExtras();
        String gsonToJson;

        if(extras != null) {
            gsonToJson = extras.getString(NotificationService.NEW_NOTIFICATION_OBJECT);
            NotificationEntity newNotificationEntity = new Gson().fromJson(gsonToJson, NotificationEntity.class);
            gsonToJson = extras.getString(NotificationService.LAST_NOTIFICATION_OBJECT);
            NotificationEntity lastNotificationEntity = new Gson().fromJson(gsonToJson, NotificationEntity.class);

            String PackageName = newNotificationEntity.getPackageName();

            DBHelper db = new DBHelper(context);
            List<BundleKeyEntity> bundleKeyEntities = db.getSortedBundleKeys(PackageName);
            db.close();

            Log.d(TAG, PackageName + " isFollowed: " + String.valueOf(newNotificationEntity.isFollowed()));

            if (newNotificationEntity.isFollowed()) {
                switch(intent.getAction())
                {
                    case NotificationService.ACTION_NOTIFICATION_POSTED:
                        mSpeechModule.addUtterance(newNotificationEntity,lastNotificationEntity,bundleKeyEntities, mAutostart);
                        break;
                    case NotificationService.ACTION_NOTIFICATION_REMOVED:
                        //mSpeechModule.removeUtterance(newNotificationEntity.getUtteranceId());
                        break;
                }
            }
        }
        else
            Log.d(TAG, "!!!!!!!!intent.Extras == null gsontojson not successful");

    }

    public void Shutdown()
    {
        if(mSpeechModule != null)
        {
            mSpeechModule.stopUtterance();
            mSpeechModule.clearUtterances();
            mSpeechModule.shutdown();
        }

    }

}
