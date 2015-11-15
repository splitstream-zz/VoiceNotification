package org.stream.split.voicenotification;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import org.stream.split.voicenotification.DataAccessLayer.DBHelper;

import java.util.Locale;

/**
 * Created by split on 2015-10-19.
 */
//TODO trzeba się zastanowić czy nie przenieść zawartości tej klasy do VoiceNotificationActivity
public class NotificationBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = "NotBrodRec";
    TextToSpeech tsp;
    private static int mUtteranceId = -1;
    DBHelper db;
    boolean mTspConnected = false;

    public NotificationBroadcastReceiver(Context context)
    {
        db = new DBHelper(context);
    }

    @Override
    public synchronized void onReceive(Context context, final Intent intent) {

        Log.d(TAG, "voicegenerator");
        Log.d(TAG, intent.getExtras().getString(Notification.EXTRA_TITLE));
        Log.d(TAG, intent.getExtras().getString(Notification.EXTRA_TEXT));
        String PackageName = intent.getExtras().getString("notification_pakageName");

        if(db.isAppFollowed(PackageName)) {
            int reapeat = 3;

            do {
                tsp = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status != TextToSpeech.ERROR) {
                            tsp.setLanguage(Locale.getDefault());
                            speak(intent);
                        } else {
                            tsp.shutdown();

                        }

                    }
                });

                reapeat--;


            }while(reapeat >0 && mTspConnected);

        }

    }

    private void speak(Intent intent)
    {
        mTspConnected = true;
        mUtteranceId++;
        tsp.speak(intent.getExtras().getString("notification_event"), TextToSpeech.QUEUE_ADD, null);
        tsp.speak(intent.getExtras().getString(Notification.EXTRA_TITLE), TextToSpeech.QUEUE_ADD, null);
        tsp.speak(intent.getExtras().getString(Notification.EXTRA_TEXT), TextToSpeech.QUEUE_ADD, null);
        tsp.shutdown();

    }

    public void Shutdown()
    {
        tsp.shutdown();
    }
}
