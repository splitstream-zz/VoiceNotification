package org.stream.split.voicenotification;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;

/**
 * Created by split on 2015-10-19.
 */
//TODO trzeba się zastanowić czy nie przenieść zawartości tej klasy do VoiceNotification
public class NotificationBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = this.getClass().getSimpleName();
    TextToSpeech tsp;
    public NotificationBroadcastReceiver(Context context)
    {
        tsp = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR)
                    tsp.setLanguage(Locale.getDefault());
            }
        });

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "voicegenerator");
        Log.d(TAG, intent.getExtras().getString(Notification.EXTRA_TITLE));
        Log.d(TAG, intent.getExtras().getString(Notification.EXTRA_TEXT));

        tsp.speak(intent.getExtras().getString("notification_event"), TextToSpeech.QUEUE_ADD, null);
        tsp.speak(intent.getExtras().getString(Notification.EXTRA_TITLE),TextToSpeech.QUEUE_ADD,null);
        tsp.speak(intent.getExtras().getString(Notification.EXTRA_TEXT), TextToSpeech.QUEUE_ADD, null);

    }

    public void Shutdown()
    {
        tsp.shutdown();
    }
}
