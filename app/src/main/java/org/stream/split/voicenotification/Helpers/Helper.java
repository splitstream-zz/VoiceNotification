package org.stream.split.voicenotification.Helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import org.stream.split.voicenotification.R;
import org.stream.split.voicenotification.VoiceNotificationActivity;

/**
 * Created by split on 2015-11-17.
 */
public class Helper {
    public static final String TAG = "HELPERS";

    public static void IterateBundleExtras(Bundle bundle)
    {
        for(String key:bundle.keySet())
        {

            Object value = bundle.get(key);
            if(value != null) {
                StringBuilder builder = new StringBuilder();
                builder.append("key: ");
                builder.append(key);
                builder.append("\t\t\t\t");

                if(value.getClass() == CharSequence.class)
                {
                    CharSequence seq = bundle.getCharSequence(key);

                    builder.append("value(charseq): ");
                    builder.append(seq);
                    builder.append("\t");
                }
                else {
                    builder.append("value: ");
                    builder.append(value);
                    builder.append("\t");
                }
                builder.append("value class: ");
                builder.append(value.getClass().getName());


                Log.d(TAG, builder.toString());
            }

        }
    }
    public static String getApplicationLabel(String packageName, Context context)
    {
        PackageManager packageManager = context.getPackageManager();
        String label;
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName,0);
            label = String.valueOf(packageManager.getApplicationLabel(appInfo));
        }catch (PackageManager.NameNotFoundException arg){
            Log.d(TAG,"!!!!!!!!!!!! getting label not possible - NameNotFound");
            label = "";
        }
        return label;
    }
    /**
     *
     * @param notificationManager for adding new notification
     * @param notificationId Id for retrieving and updating purposes
     */
    public static Notification createNotification(Context context,PendingIntent pendingIntent,
                                                  String title,String text, String subText, Boolean persistance)
    {

        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(title)
                .setContentText(text)
                .setSubText(subText)
                .setOngoing(persistance)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.ic_persistent_notification);

        return builder.build();
    }

    public static Notification createPersistantAppNotification(Context context, PendingIntent pendingIntent)
    {
        Resources res = context.getResources();
        Notification notification = createNotification(context,
                pendingIntent,
                res.getString(R.string.Notification_title),
                res.getString(R.string.Notification_text),
                res.getString(R.string.Notification_subtext),
                true);
        return notification;
    }
}
