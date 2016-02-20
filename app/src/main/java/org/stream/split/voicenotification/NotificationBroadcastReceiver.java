package org.stream.split.voicenotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;

import org.stream.split.voicenotification.Enities.HistoryBundleKeyEntity;
import org.stream.split.voicenotification.Enities.HistoryNotificationEntity;
import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.UtteranceEntity;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.Logging.BaseLogger;

import java.util.List;

//todo updateOrInsert condition not to utter when device is in mute or only vibrate mode
// todo if !textlines.isEmpty() do not show text
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

            HistoryNotificationEntity newHistoryNotificationEntity = getNotificationEntity(bundle, NotificationService.NEW_NOTIFICATION_OBJECT);
            Log.d(TAG, newHistoryNotificationEntity.getPackageName() + ".isFollowed() = " + String.valueOf(newHistoryNotificationEntity.isFollowed()));

            if (newHistoryNotificationEntity.isFollowed()) {

                switch(intent.getAction())
                {
                    case NotificationService.ACTION_NOTIFICATION_POSTED:
                        logger.d(TAG, "ACTION_NOTIFICATION_POSTED");
                        if(isVoiceActive())
                            addUtterance(newHistoryNotificationEntity);
                        break;
                    case NotificationService.ACTION_NOTIFICATION_REMOVED:
                        //mSpeechModule.removeUtterance(newHistoryNotificationEntity.getUtteranceId());
                        break;
                }
            }
        }
        else
            Log.d(TAG, "!!!!!!!!intent.Extras == null (gson to json NOT successful)");

    }
    private void addUtterance(HistoryNotificationEntity newHistoryNotificationEntity)
    {
        DBHelper db = new DBHelper(mContext);
        HistoryNotificationEntity lastHistoryNotificationEntity = db.getLastHistoryNotification(newHistoryNotificationEntity.getID(), true);
        db.close();

        UtteranceEntity utteranceEntity = createUtteranceEntity(newHistoryNotificationEntity, lastHistoryNotificationEntity);

        mSpeechModule.addUtterance(utteranceEntity);
    }
    private HistoryNotificationEntity getNotificationEntity(Bundle bundle, String key)
    {
        String gsonToJson = bundle.getString(key);
        return new Gson().fromJson(gsonToJson, HistoryNotificationEntity.class);
    }

    private UtteranceEntity createUtteranceEntity(HistoryNotificationEntity newHistoryNotificationEntity,
                                                  HistoryNotificationEntity lastHistoryNotificationEntity) {

//        UtteranceEntity lastUtteranceEntity = new UtteranceEntity();
//        if(lastHistoryNotificationEntity != null)
//            lastUtteranceEntity.addMessages(lastHistoryNotificationEntity.getBundleKeys(true));
//        String lastUtteranceFlatMessage = lastUtteranceEntity.getFlatMessage();
        logger.d(TAG, "createUtteranceEntity()");

        UtteranceEntity utteranceEntity = new UtteranceEntity();
        utteranceEntity.setUtteranceId(Helper.getUtteranceId(newHistoryNotificationEntity.getPackageName()
                ,newHistoryNotificationEntity.getID()));

        List<HistoryBundleKeyEntity> followedbundleKeys = newHistoryNotificationEntity.getBundleKeys(true);

        logger.d(TAG,"========for1========");
        for(HistoryBundleKeyEntity followedEntity:followedbundleKeys) {
            logger.d(TAG, "key: " + followedEntity.getKey());
            List<HistoryBundleKeyEntity> newBundleKeys = newHistoryNotificationEntity.getBundleKeys(followedEntity.getKey());

            UtteranceEntity temp = new UtteranceEntity();
            temp.addMessages(newBundleKeys);

            String lastUtteranceFlatMessage  = "";
            if(lastHistoryNotificationEntity != null) {
                List<HistoryBundleKeyEntity> lastBundleKeys = lastHistoryNotificationEntity.getBundleKeys(followedEntity.getKey());
                UtteranceEntity lastUtteranceEntity = new UtteranceEntity();
                lastUtteranceEntity.addMessages(lastBundleKeys);
                lastUtteranceFlatMessage = lastUtteranceEntity.getFlatMessage();
            }

            logger.d(TAG, "lastUtterance.getFlatMessage(): " + lastUtteranceFlatMessage);

            if (followedEntity.isShowAlways()) {
                logger.d(TAG, "isShownAlways");
                utteranceEntity.addMessages(newBundleKeys);
            }
            else {
                logger.d(TAG, "========for2========");
                for (HistoryBundleKeyEntity newEntity : newBundleKeys) {
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
