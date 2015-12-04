package org.stream.split.voicenotification;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Enities.NotificationEntity;
import org.stream.split.voicenotification.Enities.UtteranceEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

/**
 * Created by split on 2015-11-26.
 */
public class SpeechModule extends android.speech.tts.UtteranceProgressListener implements TextToSpeech.OnInitListener
{
    public final static String TAG = "TTS";
    private TextToSpeech mTts;
    private Queue<UtteranceEntity> mUtterances;
    private Context mContext;
    private List<BundleKeyEntity> mDefualtKeys;
    private boolean mIsSpeaking = false;

    public SpeechModule(Context context)
    {
        super();
        mContext = context;
        mUtterances = new LinkedList<>();
        mDefualtKeys = new ArrayList<>();
        mDefualtKeys.add(new BundleKeyEntity(null, Notification.EXTRA_TITLE,1));
        mDefualtKeys.add(new BundleKeyEntity(null, Notification.EXTRA_TEXT, 2));

    }

    public void addUtterance(NotificationEntity notificationEntity,List<BundleKeyEntity> bundleKeysEntities,boolean autoStart)
    {
        //TODO usunąć po fazie testów?
        if(bundleKeysEntities.isEmpty())
        {
            bundleKeysEntities = mDefualtKeys;
        }
        Log.d(TAG,"mDefualtKeys.size(): "+String.valueOf(mDefualtKeys.size()));

        StringBuilder builder = new StringBuilder();
        for(BundleKeyEntity entity:bundleKeysEntities)
        {
            String value = notificationEntity.getMessage(entity.getKey());
            if(value != null && !value.isEmpty()) {
                Log.d(TAG, value);
                builder.append(value);
                builder.append(". ");
            }
        }
        String utteranceMessage = builder.toString();
        UtteranceEntity utteranceEntity = new UtteranceEntity(notificationEntity.getUtteranceId(), utteranceMessage);

        Log.d(TAG, "Utterance: " + utteranceEntity.getMessage() + "\t utteranceId: " + utteranceEntity.getUtteranceId());

        boolean utteranceUpdated = false;
        for(UtteranceEntity entity:mUtterances)
        {
            if(entity.getUtteranceId() == notificationEntity.getUtteranceId())
            {
                entity = utteranceEntity;
                utteranceUpdated = true;
            }
        }

        if(!utteranceUpdated)
            mUtterances.add(utteranceEntity);

        Log.d(TAG, "autostart = " + autoStart);
        if(autoStart)
        {
            startNext();
        }
    }
    public void removeUtterance(String utteraanceId)
    {
        Iterator<UtteranceEntity> i = mUtterances.iterator();
        while(i.hasNext())
        {
            UtteranceEntity entity = i.next();
            Log.d(TAG, "utteranceId = " + entity.getUtteranceId());
            if(entity.getUtteranceId() == utteraanceId)
                i.remove();
        }
    }
    public void startNext() {
        Log.d(TAG, "startNext()");

        if(!mUtterances.isEmpty()) {

            if (mTts == null) {
                Log.d(TAG, "mTts = null");
                mTts = new TextToSpeech(mContext, this);
                return;
            }
            else {
                Log.d(TAG, "else, isSpeaking() = " + mTts.isSpeaking());
                if (!mIsSpeaking) {
                    UtteranceEntity utteranceEntity = mUtterances.remove();
                    speak(utteranceEntity);
                }
            }
        }
        else {
            mTts.shutdown();
            mTts = null;
        }

    }
    private void speak(UtteranceEntity utteranceEntity) {
        Log.d(TAG, "speak()");
        HashMap<String, String> params = new HashMap<>();
        String id = utteranceEntity.getUtteranceId();
        String message = utteranceEntity.getMessage();
        Log.d(TAG, "startNext()\tutteranceId:" + id +"\tMessage: " + message);
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
        mTts.speak(message, TextToSpeech.QUEUE_FLUSH, params);

    }
    public void stopUtterance()
    {
        if(mTts != null)
            mTts.stop();
    }
    public void clearUtterances()
    {
        if(mUtterances != null)
        {
            mUtterances.clear();
        }
    }
    public void shutdown()
    {
        if(mTts != null) {
            mTts.stop();
            clearUtterances();
            mTts.shutdown();
            mTts = null;
        }
    }



    @Override
    public void onStart(String utteranceId) {
        Log.d(TAG, "onStart(utteranceId)");
        mIsSpeaking = true;
    }

    @Override
    public void onDone(String utteranceId) {
        Log.d(TAG, "onDone()");
        mIsSpeaking = false;
        if(!mUtterances.isEmpty()) {
            startNext();
        }
        else {
            mTts.shutdown();
            mTts = null;
        }
    }

    @Override
    public void onError(String utteranceId) {
        Log.d(TAG, "onError(String utteranceId)");
        mIsSpeaking = false;
        if(mTts != null)
        {
            mTts.shutdown();
            mTts = null;
        }
        startNext();
    }

    @Override
    public void onError(String utteranceId, int errorCode) {
        super.onError(utteranceId, errorCode);
        Log.d(TAG, "onError(String utteranceId, errorcode)");
        mIsSpeaking = false;
    }

    @Override
    public void onStop(String utteranceId, boolean interrupted) {
        super.onStop(utteranceId, interrupted);
        Log.d(TAG, "onStop(String utteranceId, boolean interrupted)");
        mIsSpeaking = false;
    }

    @Override
    public void onInit(int status) {
        Log.d(TAG, "onInit status = " + status);
        Locale locale = new Locale("pl","PL");

        if(status == TextToSpeech.SUCCESS)
        {
            mTts.setOnUtteranceProgressListener(this);
            int available  = mTts.isLanguageAvailable(locale);
            Log.d(TAG, "is lang available " + available);

            if(mTts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                int languageSetResult = mTts.setLanguage(locale);
                Log.d(TAG, "setLanguage: " + languageSetResult);
                startNext();
            }
            else
            {
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.mContext.startActivity(installIntent);

            }
        }
    }
//    private int setLanguage(Locale locale)
//    {
//
//    }

}