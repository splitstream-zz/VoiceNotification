package org.stream.split.voicenotification;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
    public final static String TAG = "TTS";
    private TextToSpeech mTts;
    private Queue<UtteranceEntity> mUtterances;
    private Context mContext;

    public SpeechModule(Context context)
    {
        super();
        mContext = context;
        mUtterances = new LinkedList<>();
    }

    public void addUtterance(UtteranceEntity utteranceEntity,boolean autoStart)
    {
        if(utteranceEntity != null)
            mUtterances.add(utteranceEntity);

        BaseLogger.getInstance().d(TAG, "autostart = " + autoStart);
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
            BaseLogger.getInstance().d(TAG, "utteranceId = " + entity.getUtteranceId());
            if(entity.getUtteranceId() == utteraanceId)
                i.remove();
        }
    }
    public void startNext() {
        BaseLogger.getInstance().d(TAG, "startNext()");

        if(!mUtterances.isEmpty()) {

            if (mTts == null) {
                BaseLogger.getInstance().d(TAG, "mTts = null");
                mTts = new TextToSpeech(mContext, this);
                return;
            }
            else {
                BaseLogger.getInstance().d(TAG, "else, isSpeaking() = " + mTts.isSpeaking());
                UtteranceEntity utteranceEntity = mUtterances.peek();
                speak(utteranceEntity);
            }
        }
        else {
            mTts.shutdown();
            mTts = null;
        }

    }
    private void speak(UtteranceEntity utteranceEntity) {
        BaseLogger.getInstance().d(TAG, "speak()");

        HashMap<String, String> params = new HashMap<>();
        String id = utteranceEntity.getUtteranceId();
        List<BundleKeyEntity> messages = utteranceEntity.getMessages();

        //BaseLogger.getInstance().d(TAG, "startNext()\tutteranceId:" + id + "\tMessage: " + message);
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
        for(BundleKeyEntity message:messages)
            mTts.speak(message.getValue(), TextToSpeech.QUEUE_ADD, params);

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
        clearUtterances();
        if(mTts != null) {
            mTts.stop();
            mTts.shutdown();
            mTts = null;
        }
    }



    @Override
    public void onStart(String utteranceId) {
        BaseLogger.getInstance().d(TAG, "onStart(utteranceId)");
    }

    @Override
    public void onDone(String utteranceId) {
        BaseLogger.getInstance().d(TAG, "onDone()");
        if(!mUtterances.isEmpty()) {
            Queue<UtteranceEntity> utterances = new LinkedList<>();
            for(UtteranceEntity entity:mUtterances)
                if(!entity.getUtteranceId().equals(utteranceId))
                    utterances.add(entity);
            mUtterances= utterances;
            startNext();
        }
        else {
            mTts.shutdown();
            mTts = null;
        }
    }

    @Override
    public void onError(String utteranceId) {
        BaseLogger.getInstance().d(TAG, "onError(String utteranceId)");
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
        BaseLogger.getInstance().d(TAG, "onError(String utteranceId, errorcode)");
    }

    @Override
    public void onStop(String utteranceId, boolean interrupted) {
        super.onStop(utteranceId, interrupted);
        BaseLogger.getInstance().d(TAG, "onStop(String utteranceId, boolean interrupted)");
    }

    @Override
    public void onInit(int status) {
        BaseLogger.getInstance().d(TAG, "onInit status = " + status);
        Locale locale = new Locale("pl","PL");

        if(status == TextToSpeech.SUCCESS)
        {
            mTts.setOnUtteranceProgressListener(this);
            int available  = mTts.isLanguageAvailable(locale);
            BaseLogger.getInstance().d(TAG, "is lang available " + available);

            if(mTts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                int languageSetResult = mTts.setLanguage(locale);
                BaseLogger.getInstance().d(TAG, "setLanguage: " + languageSetResult);
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


}