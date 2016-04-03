package org.stream.split.voicenotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import org.stream.split.voicenotification.Enities.HistoryBundleKeyEntity;
import org.stream.split.voicenotification.Enities.HistoryNotificationEntity;
import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.UtteranceEntity;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.Logging.BaseLogger;

import java.util.ArrayList;
import java.util.List;

//todo updateOrInsert condition not to utter when device is in mute or only vibrate mode
// todo if !textlines.isEmpty() do not show text
/**
 * Created by split on 2015-10-19.
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final String TAG = "NotBrodRec";
    private static BaseLogger logger = BaseLogger.getInstance();
    private static NotificationBroadcastReceiver SINGLETON;
    private final SharedPreferences mSharedPreferences;

    private SpeechModule mSpeechModule;
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

    public static NotificationBroadcastReceiver getInstance(Context context)
    {
        if(SINGLETON == null)
            SINGLETON = new NotificationBroadcastReceiver(context);
        return SINGLETON;
    }

    private NotificationBroadcastReceiver(Context context)
    {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        mSpeechModule = SpeechModule.getInstance(context);
        mContext = context;

        initialize(mSharedPreferences);

    }
    private void initialize(SharedPreferences sharedPreferences)
    {

    }

    @Override
    public synchronized void onReceive(Context context, final Intent intent) {

        Log.d(TAG, "OnReceive()");

        switch (intent.getAction()) {
            case NotificationService.ACTION_NOTIFICATION_POSTED:
                logger.d(TAG, NotificationService.ACTION_NOTIFICATION_POSTED);
                actionNotificationPosted(intent);
                break;
            case Intent.ACTION_SCREEN_ON:
                logger.d(TAG, Intent.ACTION_SCREEN_ON);
                actionScreenOn();
                break;
        }
    }



    private void actionScreenOn() {
        //stopSpeaking();
    }

    private void actionNotificationPosted(Intent intent) {
        Bundle bundle = intent.getExtras();

        if (bundle != null) {

            HistoryNotificationEntity postedNotificationEntity = getNotificationEntity(bundle, NotificationService.EXTRA_NOTIFICATION_OBJECT);

            if (postedNotificationEntity != null && postedNotificationEntity.isFollowed()) {

                Log.d(TAG, postedNotificationEntity.getPackageName() + ".isFollowed() = " + String.valueOf(postedNotificationEntity.isFollowed()));

                DBHelper db = new DBHelper(mContext);
                HistoryNotificationEntity lastHistoryNotificationEntity = db.getLastHistoryNotification(postedNotificationEntity.getID(), true);
                db.close();

                UtteranceEntity utteranceEntity = createUtteranceEntity(postedNotificationEntity, lastHistoryNotificationEntity);

                mSpeechModule.addUtterance(utteranceEntity);
                mSpeechModule.start();
            }
        }
        else
            Log.d(TAG, "!!!!!!!!intent.Extras == null (gson to json NOT successful)");
    }
    private HistoryNotificationEntity getNotificationEntity(Bundle bundle, String key)
    {
        String gsonToJson = bundle.getString(key);
        return new Gson().fromJson(gsonToJson, HistoryNotificationEntity.class);
    }

    private UtteranceEntity createUtteranceEntity(HistoryNotificationEntity newHistoryNotificationEntity,
                                                  HistoryNotificationEntity lastHistoryNotificationEntity) {

        logger.d(TAG, "===========createUtteranceEntity()");

        UtteranceEntity utteranceEntity = new UtteranceEntity();
        utteranceEntity.setUtteranceId(Helper.getUtteranceId(newHistoryNotificationEntity.getPackageName()
                , newHistoryNotificationEntity.getID()));


        List<HistoryBundleKeyEntity> postedBundleKeys = newHistoryNotificationEntity.getBundleKeyList().get(true);

        List<HistoryBundleKeyEntity> lastBundleKeys = new ArrayList<>();
        if(lastHistoryNotificationEntity != null) {
             lastBundleKeys.addAll(lastHistoryNotificationEntity.getBundleKeyList().get(true));
        }

        logger.d(TAG,"========for1========");
        for(HistoryBundleKeyEntity postedBundleKey:postedBundleKeys) {
            logger.d(TAG, "key: " + postedBundleKey.getKey());

            if (postedBundleKey.isShowAlways()) {
                logger.d(TAG, "isShownAlways");
                utteranceEntity.addMessage(postedBundleKey);
            }
            else {
                logger.d(TAG, "========for2========");
                for (HistoryBundleKeyEntity lastBundleKey : lastBundleKeys) {
                    String newMessage = postedBundleKey.getValue().replace(lastBundleKey.getValue(),"");
                    postedBundleKey.setValue(newMessage);
                }
                utteranceEntity.addMessage(postedBundleKey);
                logger.d(TAG,"========/for2========");
            }

        }
        logger.d(TAG,"========/for1========");
        logger.d(TAG, "New utterance: " + utteranceEntity.getFlatMessage());
        return utteranceEntity;
    }


    private void stopSpeaking()
    {
        mSpeechModule.stop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    public void dispose() {
        mSpeechModule.shutdown(true);
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}
