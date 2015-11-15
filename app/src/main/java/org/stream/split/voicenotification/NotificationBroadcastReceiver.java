package org.stream.split.voicenotification;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import org.stream.split.voicenotification.BussinessLayer.NotificationEntity;
import org.stream.split.voicenotification.DataAccessLayer.DBHelper;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
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
    List<NotificationEntity> mUterrances;

    public NotificationBroadcastReceiver(Context context)
    {
        mUterrances = new ArrayList<>();
        db = new DBHelper(context);
        tsp = new TextToSpeech(context, new OnInitTsp());
    }

    @Override
    public synchronized void onReceive(Context context, final Intent intent) {

        Log.d(TAG, "OnReceive()");
        String PackageName = intent.getExtras().getString(NotificationCatcherService.NOTIFICAITON_PACKAGE_NAME);
        Boolean isFollowed = db.isAppFollowed(PackageName);
        Log.d(TAG,PackageName + " isFollowed: " + String.valueOf(isFollowed));

        if(isFollowed) {
            NotificationEntity notificationEntity = new NotificationEntity();
            notificationEntity.setApplicationName(intent.getExtras().getString(NotificationCatcherService.NOTIFICATION_APPLICATION_LABEL));

        }

    }

    private NotificationEntity createNotificationEntity(Notification notification, String packageName, Timestamp date, String label)
    {
        if(notification != null) {
            NotificationEntity notificationEntity = new NotificationEntity(notification);
            notificationEntity.setApplicationName(label);
            notificationEntity.setPackageName(packageName);
            notificationEntity.

        }
    }
    private void speak(Intent intent)
    {
        Log.d(TAG, "speak");
        mTspConnected = true;
        mUtteranceId++;
        Notification notification = intent.getParcelableExtra(NotificationCatcherService.NOTIFICATION_OBJECT);

        tsp.speak(intent.getExtras().getString("notification_event"), TextToSpeech.QUEUE_ADD, null);
        tsp.speak(intent.getExtras().getString(Notification.EXTRA_TITLE), TextToSpeech.QUEUE_ADD, null);
        tsp.speak(intent.getExtras().getString(Notification.EXTRA_TEXT), TextToSpeech.QUEUE_ADD, null);
        tsp.shutdown();
        tsp = null;

    }

    public void Shutdown()
    {
        if(tsp != null)
            tsp.shutdown();
    }
    private class OnInitTsp implements TextToSpeech.OnInitListener
    {

        @Override
        public void onInit(int status) {
            if(status == TextToSpeech.SUCCESS)
            {
                speak()
            }

        }
    }
}
