package org.stream.split.voicenotification;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;

import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Enities.NotificationEntity;
import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.UtteranceEntity;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.Logging.BaseLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by split on 2015-10-19.
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = "NotBrodRec";
    private SpeechModule mSpeechModule;
    private static BaseLogger LOGGER = BaseLogger.getInstance();
    private Context mContext;

    public NotificationBroadcastReceiver(Context context)
    {
        mSpeechModule = new SpeechModule(context);
        mContext = context;
    }

    @Override
    public synchronized void onReceive(Context context, final Intent intent) {

        Log.d(TAG, "OnReceive()");
        Bundle bundle = intent.getExtras();

        if(bundle != null) {

            NotificationEntity newNotificationEntity = getNotificationEntity(bundle, NotificationService.NOTIFICATION_OBJECT);
            Log.d(TAG, newNotificationEntity.getPackageName() + ".isFollowed() = " + String.valueOf(newNotificationEntity.isFollowed()));

            if (newNotificationEntity.isFollowed()) {

                switch(intent.getAction())
                {
                    case NotificationService.ACTION_NOTIFICATION_POSTED:
                        LOGGER.d(TAG, "ACTION_NOTIFICATION_POSTED");
                        addUtterance(newNotificationEntity);
                        break;
                    case NotificationService.ACTION_NOTIFICATION_REMOVED:
                        //mSpeechModule.removeUtterance(newNotificationEntity.getUtteranceId());
                        break;
                }
            }
        }
        else
            Log.d(TAG, "!!!!!!!!intent.Extras == null (gson to json NOT successful)");

    }
    private void addUtterance(NotificationEntity newNotificationEntity)
    {
        LOGGER.d(TAG, "addUtterance()");
        UtteranceEntity utteranceEntity = getUtteranceEntity(newNotificationEntity);
        LOGGER.d(TAG, "addUtterance(), getUtterance completed");
        mSpeechModule.addUtterance(utteranceEntity);
    }
    private NotificationEntity getNotificationEntity(Bundle bundle, String key)
    {
        String gsonToJson = bundle.getString(key);
        return new Gson().fromJson(gsonToJson, NotificationEntity.class);
    }

    private UtteranceEntity getUtteranceEntity(NotificationEntity newNotificationEntity) {

        LOGGER.d(TAG, "getUtteranceEntity()");
        String PackageName = newNotificationEntity.getPackageName();

        DBHelper db = new DBHelper(mContext);
        List<BundleKeyEntity> followedBundleKeys = db.getSortedFollowedBundleKeys(PackageName);
        NotificationEntity lastNotificationEntity = db.getLastNotification(PackageName, true);
        db.close();

        UtteranceEntity lastUtteranceEntity = new UtteranceEntity();
        if(lastNotificationEntity != null)
            lastUtteranceEntity.addMessages(lastNotificationEntity.getBundleKeys(true));
        String lastUtteranceFlatMessage = lastUtteranceEntity.getFlatMessage();
        LOGGER.d(TAG, "getUtteranceEntity()");
        LOGGER.d(TAG, "lastUtterance.getFlatMessage(): " + lastUtteranceFlatMessage);

        UtteranceEntity utteranceEntity = new UtteranceEntity();
        utteranceEntity.setUtteranceId(Helper.getUtteranceId(PackageName,newNotificationEntity.getID()));

        for(BundleKeyEntity followedEntity:followedBundleKeys) {
            List<BundleKeyEntity> newBundleKeys = newNotificationEntity.getBundleKeys(followedEntity.getKey());
            List<BundleKeyEntity> lastBundleKeys = new ArrayList<>();
            if(lastNotificationEntity != null)
                lastBundleKeys = lastNotificationEntity.getBundleKeys(followedEntity.getKey());

            if (followedEntity.isShowAlways())
                utteranceEntity.addMessages(newBundleKeys);
            else {
                for (BundleKeyEntity newEntity : newBundleKeys) {
                    boolean isNew = true;
                    for (BundleKeyEntity lastEntity : lastBundleKeys) {
                        if (newEntity.getValue().equals(lastEntity.getValue())) {
                            isNew = false;
                            LOGGER.d(TAG, "Repeated: " + newEntity.getValue());
                            break;
                        }
                    }
                    if (isNew)
                        utteranceEntity.addMessage(newEntity);
                }
            }
        }
        return utteranceEntity;
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
