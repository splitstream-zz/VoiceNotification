package org.stream.split.voicenotification;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;

import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Enities.NotificationEntity;
import org.stream.split.voicenotification.Enities.UtteranceEntity;
import org.stream.split.voicenotification.Logging.BaseLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

//todo somewhere when lastnotificationentity is null it goes nullargumentexception
//TODO clean up after getMessage rearangment!
//TODO title should be read always
// TODO: text and textlines should be treat as one? or maybe when we have textlines we should compare the number of lines? (but then we shall not save it as one in HELPER.createnotification function.
// TODO: 2016-01-12 make sure utterance is not empty
/**
 * Created by split on 2015-11-26.
 */
public class SpeechModule extends android.speech.tts.UtteranceProgressListener implements TextToSpeech.OnInitListener
{
    public final static String TAG = "SpeechModule";
    private TextToSpeech mTts;
    private Queue<UtteranceEntity> mUtterances;
    private Context mContext;
    private BaseLogger logger = BaseLogger.getInstance();
    AudioManager am;


    public SpeechModule(Context context)
    {
        super();
        mContext = context;
        mUtterances = new LinkedList<>();
        am= (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

    }

    public void addUtterance(UtteranceEntity utteranceEntity)
    {
        logger.d(TAG, "addUtterance()");
        if(utteranceEntity != null)
            mUtterances.add(utteranceEntity);
        startNext();

    }

    public void removeUtterance(String utteraanceId)
    {
        Iterator<UtteranceEntity> i = mUtterances.iterator();
        while(i.hasNext())
        {
            UtteranceEntity entity = i.next();
            logger.d(TAG, "utteranceId = " + entity.getUtteranceId());
            if(entity.getUtteranceId().equals(utteraanceId))
                i.remove();
        }
    }
    public synchronized void startNext() {
        logger.d(TAG, "startNext()");

        if(!mUtterances.isEmpty()) {
            if (mTts == null) {
                logger.d(TAG, "mTts = null");
                mTts = new TextToSpeech(mContext, this);
            }
            else {
                logger.d(TAG, "else, isSpeaking() = " + mTts.isSpeaking());
                UtteranceEntity utteranceEntity = mUtterances.remove();
                speak(utteranceEntity);
            }
        }
    }
    private void speak(UtteranceEntity utteranceEntity) {
        logger.d(TAG, "speak()");

        HashMap<String, String> params = new HashMap<>();
        String id = utteranceEntity.getUtteranceId();
        String message = utteranceEntity.getFlatMessage();
        logger.d(TAG, "\tutteranceId:" + id + "\tMessage: " + message);
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
        mTts.speak(message, TextToSpeech.QUEUE_ADD, params);

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
        clearUtterances();
        if(mTts != null) {
            mTts.stop();
            mTts.shutdown();
            mTts = null;
        }
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }



    @Override
    public void onStart(String utteranceId) {
        logger.d(TAG, "onStart(utteranceId)");
        am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    }

    @Override
    public void onDone(String utteranceId) {
        logger.d(TAG, "onDone()");
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    @Override
    public void onError(String utteranceId) {
        logger.d(TAG, "onError(String utteranceId)");
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
        logger.d(TAG, "onError(String utteranceId, errorcode)");
    }

    @Override
    public void onStop(String utteranceId, boolean interrupted) {
        super.onStop(utteranceId, interrupted);
        logger.d(TAG, "onStop(String utteranceId, boolean interrupted)");
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

    }

    @Override
    public void onInit(int status) {
        logger.d(TAG, "onInit status = " + status);
        Locale locale = new Locale("pl","PL");

        if(status == TextToSpeech.SUCCESS)
        {
            mTts.setOnUtteranceProgressListener(this);
            int available  = mTts.isLanguageAvailable(locale);
            logger.d(TAG, "is lang available " + available);

            if(mTts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                int languageSetResult = mTts.setLanguage(locale);
                logger.d(TAG, "setLanguage: " + languageSetResult);
                startNext();
            }
            else
            {
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if(mContext.getPackageManager().resolveActivity(installIntent,PackageManager.GET_META_DATA) != null)
                    this.mContext.startActivity(installIntent);

            }
        }
        else
            mTts = new TextToSpeech(mContext,this);
    }


    public void stop() {
        if(mTts != null)
            mTts.stop();
    }
}