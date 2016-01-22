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
import org.stream.split.voicenotification.Enities.UtteranceEntity;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.Logging.BaseLogger;

import java.util.List;

/**
 * Created by split on 2015-10-19.
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = "NotBrodRec";
    private SpeechModule mSpeechModule;
    private static BaseLogger logger = BaseLogger.getInstance();
    private Context mContext;
    private boolean mIsVoiceActive;

    public boolean isVoiceActive() {
        return mIsVoiceActive;
    }

    public void setIsVoiceActive(boolean isVoiceActive) {
        this.mIsVoiceActive = isVoiceActive;
        if(!isVoiceActive)
            mSpeechModule.stop();
    }


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

            NotificationEntity newNotificationEntity = getNotificationEntity(bundle, NotificationService.NEW_NOTIFICATION_OBJECT);
            Log.d(TAG, newNotificationEntity.getPackageName() + ".isFollowed() = " + String.valueOf(newNotificationEntity.isFollowed()));

            if (newNotificationEntity.isFollowed()) {

                switch(intent.getAction())
                {
                    case NotificationService.ACTION_NOTIFICATION_POSTED:
                        logger.d(TAG, "ACTION_NOTIFICATION_POSTED");
                        if(isVoiceActive())
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
        DBHelper db = new DBHelper(mContext);
        List<BundleKeyEntity> followedBundleKeys = db.getSortedFollowedBundleKeys(newNotificationEntity.getPackageName());
        NotificationEntity lastNotificationEntity = db.getLastNotification(newNotificationEntity.getID(), true);
        logger.d(TAG, "lasnotification.getID: " + lastNotificationEntity.getID());
        db.close();
        logger.d(TAG, "addUtterance()");
        UtteranceEntity utteranceEntity = getUtteranceEntity(newNotificationEntity,lastNotificationEntity,followedBundleKeys);
        logger.d(TAG, "addUtterance(), getUtterance completed");
        mSpeechModule.addUtterance(utteranceEntity);
    }
    private NotificationEntity getNotificationEntity(Bundle bundle, String key)
    {
        String gsonToJson = bundle.getString(key);
        return new Gson().fromJson(gsonToJson, NotificationEntity.class);
    }

    private UtteranceEntity getUtteranceEntity(NotificationEntity newNotificationEntity,
                                               NotificationEntity lastNotificationEntity,
                                               List<BundleKeyEntity> followedBundleKeys) {

//        UtteranceEntity lastUtteranceEntity = new UtteranceEntity();
//        if(lastNotificationEntity != null)
//            lastUtteranceEntity.addMessages(lastNotificationEntity.getBundleKeys(true));
//        String lastUtteranceFlatMessage = lastUtteranceEntity.getFlatMessage();
        logger.d(TAG, "getUtteranceEntity()");


        UtteranceEntity utteranceEntity = new UtteranceEntity();
        utteranceEntity.setUtteranceId(Helper.getUtteranceId(newNotificationEntity.getPackageName()
                ,newNotificationEntity.getID()));

        logger.d(TAG,"========for1========");
        for(BundleKeyEntity followedEntity:followedBundleKeys) {
            logger.d(TAG, "key: " + followedEntity.getKey());
            List<BundleKeyEntity> newBundleKeys = newNotificationEntity.getBundleKeys(followedEntity.getKey());
            UtteranceEntity temp = new UtteranceEntity();
            temp.addMessages(newBundleKeys);

            List<BundleKeyEntity> lastBundleKeys = lastNotificationEntity.getBundleKeys(followedEntity.getKey());
            UtteranceEntity lastUtteranceEntity = new UtteranceEntity();
            lastUtteranceEntity.addMessages(lastBundleKeys);
            String lastUtteranceFlatMessage = lastUtteranceEntity.getFlatMessage();

            logger.d(TAG, "lastUtterance.getFlatMessage(): " + lastUtteranceFlatMessage);

            if (followedEntity.isShowAlways()) {
                logger.d(TAG, "isShownAlways");
                utteranceEntity.addMessages(newBundleKeys);
            }
            else {
                logger.d(TAG, "========for2========");
                for (BundleKeyEntity newEntity : newBundleKeys) {
                    String newUtteranceMessage = temp.getFlatMessage().replace(lastUtteranceFlatMessage, "");
                    newEntity.setValue(newUtteranceMessage);
                    utteranceEntity.addMessage(newEntity);
                    logger.d(TAG, "===message====:\n" + newUtteranceMessage + "\n====end message====");
                    logger.d(TAG, "===value====:\n" + newEntity.getValue() + "\n====end value====");
                }
                logger.d(TAG,"========/for2========");
            }

        }
        logger.d(TAG,"========/for1========");
        logger.d(TAG, "New utterance: " + utteranceEntity.getFlatMessage());
        return utteranceEntity;
    }

    public void stop()
    {
        mSpeechModule.stop();
    }

}
