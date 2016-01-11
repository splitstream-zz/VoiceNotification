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

import java.util.List;

/**
 * Created by split on 2015-10-19.
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = "NotBrodRec";
    private boolean mAutostart = true;
    private SpeechModule mSpeechModule;
    private Context mContext;

    public NotificationBroadcastReceiver(Context context)
    {
        mSpeechModule = new SpeechModule(context);
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
        String PackageName = newNotificationEntity.getPackageName();
        DBHelper db = new DBHelper(mContext);
        List<BundleKeyEntity> bundleKeyEntities = db.getSortedBundleKeys(PackageName);
        db.close();
        UtteranceEntity utteranceEntity = getUtteranceEntity1()

    }
    private NotificationEntity getNotificationEntity(Bundle bundle, String key)
    {
        String gsonToJson = bundle.getString(key);
        return new Gson().fromJson(gsonToJson, NotificationEntity.class);
    }


    private String getUtteranceEntity(NotificationEntity newNotificationEntity, NotificationEntity lastNotificationEntity, List<BundleKeyEntity> followedBundleKeysEntities)
    {
        StringBuilder messagebuilder = new StringBuilder();

        for(BundleKeyEntity followedBundleKey:followedBundleKeysEntities)
        {
            BundleKeyEntity newFollowedBundleKeys = newNotificationEntity.getBundleKey(followedBundleKey.getKey());
            String[] textLines = newFollowedBundleKeys.getValue().split("\\n");
            for(String line:textLines)
            {
                if(lastNotificationEntity != null) {
                    BundleKeyEntity lastFollowedBundleKey = lastNotificationEntity.getBundleKey(followedBundleKey.getKey());
                    if (lastFollowedBundleKey != null && !lastFollowedBundleKey.getValue().contains(line)) {
                        messagebuilder.append(line);
                        messagebuilder.append(".\n");
                    }
                }
                else {
                    messagebuilder.append(line);
                    messagebuilder.append(".\n");
                }
            }

        }
        return messagebuilder.toString();
    }
    private String getUtteranceEntity1(NotificationEntity newNotificationEntity, NotificationEntity lastNotificationEntity, List<BundleKeyEntity> followedBundleKeysEntities)
    {
        UtteranceEntity utteranceEntity = new UtteranceEntity();
        for(BundleKeyEntity followedBundleKey:followedBundleKeysEntities)
        {
            if(followedBundleKey.getKey().contains("title"))
                utteranceEntitynewNotificationEntity.getBundleKey(followedBundleKey.getKey());
            String newFollowedBundleValue = newNotificationEntity.getBundleKey(followedBundleKey.getKey()).getValue();
            String lastFollowedBundleValue = lastNotificationEntity.getBundleKey(followedBundleKey.getKey()).getValue();
            messagebuilder.append(newFollowedBundleValue.replace(lastFollowedBundleValue,""));
        }
        return messagebuilder.toString();
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
