package org.stream.split.voicenotification;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;

import org.stream.split.voicenotification.Enities.UtteranceEntity;
import org.stream.split.voicenotification.Logging.BaseLogger;

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
    private int mStatus = TextToSpeech.ERROR;
    private Queue<UtteranceEntity> mUtterances;
    private Context mContext;
    private BaseLogger logger = BaseLogger.getInstance();
    private AudioManager am;
    private static SpeechModule SINGLETON;

    public static SpeechModule getInstance(Context context)
    {
        if(SINGLETON == null)
        {
            SINGLETON = new SpeechModule(context);
        }
        return SINGLETON;
    }

    private SpeechModule(Context context)
    {
        super();
        mContext = context;
        mUtterances = new LinkedList<>();
        am= (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        initialize();
    }

    private void initialize() {
    }

    public void addUtterance(UtteranceEntity utteranceEntity)
    {
        logger.d(TAG, "addUtterance()");
        if(utteranceEntity != null) {
            mUtterances.add(utteranceEntity);
            logger.d(TAG,"utteranceId = " + utteranceEntity.getUtteranceId());
        }
    }
    public boolean removeUtterance(UtteranceEntity utteranceEntity)
    {
        boolean result = removeUtterance(mUtterances,utteranceEntity);
        return result;
    }

    private boolean removeUtterance(Queue<UtteranceEntity> utterances, UtteranceEntity utteranceEntity)
    {
        boolean result = false;
        if(utterances != null && !utterances.isEmpty() && utteranceEntity != null)
        {
            Iterator<UtteranceEntity> i = utterances.iterator();
            while(i.hasNext())
            {
                UtteranceEntity entity = i.next();
                if(entity.getUtteranceId().equals(utteranceEntity.getUtteranceId()))
                {
                    i.remove();
                    result = true;
                }
            }
        }
        return result;
    }
//todo cos nie tak tutaj!!!!!!!!
    public synchronized void start() {
        logger.d(TAG, "start()");
        start(mUtterances,mTts,mStatus);
    }
    private boolean start(Queue<UtteranceEntity> utterances, TextToSpeech tts, int status)
    {
        boolean result = false;
        if (utterances != null && !utterances.isEmpty()) {
            if (tts != null && status == TextToSpeech.SUCCESS) {
                speak(utterances.remove());
                result = true;
            }
            else
            {
                tts = new TextToSpeech(mContext,this);
            }
        }
        return result;
    }
    private void speak(Queue<UtteranceEntity> utterances)
    {
        while(utterances != null && !utterances.isEmpty())
        {
            UtteranceEntity entity = utterances.remove();
            speak(entity);
        }
    }
    private void speak(UtteranceEntity utteranceEntity) {
        logger.d(TAG, "speak()");

        HashMap<String, String> params = new HashMap<>();
        String id = utteranceEntity.getUtteranceId();
        String message = utteranceEntity.getFlatMessage();

        logger.d(TAG, "\tutteranceId:" + id + "\tMessage: " + message);
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
        params.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_RING));

        mTts.speak(message, TextToSpeech.QUEUE_ADD, params);
    }

    public void clearUtterances()
    {
        if(mUtterances != null)
        {
            mUtterances.clear();
        }
    }

    public void shutdown(boolean clearUtterances)
    {
        if(clearUtterances)
            clearUtterances();
        if(mTts != null) {
            mStatus = TextToSpeech.ERROR;
            mTts.stop();
            mTts.shutdown();
            mTts = null;
        }
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

    }

    @Override
    public void onStart(String utteranceId) {
        logger.d(TAG, "onStart(utteranceId)");
        am.getMode();
        am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    }

    @Override
    public void onDone(String utteranceId) {
        logger.d(TAG, "onDone()");
        logger.d(TAG, "Done speaking utteranceID = " + utteranceId);
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        start();
    }

    @Override
    public void onError(String utteranceId) {
        logger.d(TAG, "onError(String utteranceId = " + utteranceId +")");
        mStatus = TextToSpeech.ERROR;
        if(mTts != null)
        {
            shutdown(false);
        }
        start();
    }

    @Override
    public void onError(String utteranceId, int errorCode) {
        logger.d(TAG, "onError(String utteranceId, errorcode)");
        logger.d(TAG, "utteranceId = "+ utteranceId + "\terrorCode = " + errorCode );
        super.onError(utteranceId, errorCode);
    }

    @Override
    public void onStop(String utteranceId, boolean interrupted) {
        super.onStop(utteranceId, interrupted);
        logger.d(TAG, "onStop(String utteranceId, boolean interrupted)");
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        clearUtterances();
    }

    @Override
    public void onInit(int status) {
        logger.d(TAG, "onInit status = " + status);
        Locale locale = new Locale("pl","PL");
        mStatus = status;
        if(status == TextToSpeech.SUCCESS)
        {
            mTts.setOnUtteranceProgressListener(this);
            int available  = mTts.isLanguageAvailable(locale);
            logger.d(TAG, "is lang available " + available);

            if(mTts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                int languageSetResult = mTts.setLanguage(locale);
                logger.d(TAG, "setLanguage: " + languageSetResult);
                start();
            }
            else
            {
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
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