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
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Enities.NotificationEntity;
import org.stream.split.voicenotification.R;
import org.stream.split.voicenotification.VoiceNotificationActivity;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by split on 2015-11-17.
 */
public class Helper {
    public static final String TAG = "HELPERS";

    public static void LogBundleExtras(Bundle bundle)
    {
        for(String key:bundle.keySet())
        {
            Object value = bundle.get(key);

            StringBuilder builder = new StringBuilder();
            builder.append("key: ");
            builder.append(key);
            builder.append("\t\t\t\t");

            if(value != null) {

                builder.append("key: ");
                builder.append(key);
                builder.append("\t\t\t\t");

                if(value instanceof CharSequence[])
                {
                    builder.append("\n====== Charsequense[]======\n");

                    for(CharSequence seq:bundle.getCharSequenceArray(key)) {
                        builder.append("value(charseq): ");
                        builder.append(seq);
                        builder.append("\n");
                    }
                    builder.append("====== \"Charsequense[]======\n");
                }
                else {
                    builder.append("value: ");
                    builder.append(value);
                    builder.append("\t");
                }

                builder.append("value class: ");
                builder.append(value.getClass().getName());
            }
            else

                builder.append("value: null");

            Log.d(TAG, builder.toString());
        }
    }
    public static List<BundleKeyEntity>  IterateBundleExtras(Bundle bundle, String packageName)
    {
        List<BundleKeyEntity> bundlekeys = new ArrayList<>();

        for(String key:bundle.keySet())
        {
            Object value = bundle.get(key);

            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("key: ");
            logBuilder.append(key);
            logBuilder.append("\t\t\t\t");
            //TODO może spróbować czy nie lepiej sprawdzać czy to jest typ któy nas interesuje (string,charsequence itd)
            if(value != null )  {

                if(value instanceof CharSequence[])
                {
                    logBuilder.append("\n====== Charsequense[]======\n");

                    for(CharSequence seq:bundle.getCharSequenceArray(key)) {
                        logBuilder.append("value(charseq): ");
                        logBuilder.append(seq);
                        logBuilder.append("\n");
                        bundlekeys.add(new BundleKeyEntity(packageName, key, new StringBuilder(seq).toString()));
                    }
                    logBuilder.append("====== \"Charsequense[]======\n");
                }
                else {
                    logBuilder.append("value: ");
                    logBuilder.append(value);
                    logBuilder.append("\t");
                    bundlekeys.add(new BundleKeyEntity(packageName, key, new StringBuilder().append(value).toString()));
                }

                logBuilder.append("value class: ");
                logBuilder.append(value.getClass().getName());
            }
            else
                logBuilder.append("value: null");

            Log.d(TAG, logBuilder.toString());
        }

        return bundlekeys;
    }

    public static NotificationEntity createNotificationEntity(StatusBarNotification sbn, String label) {

        String utteranceId = getUtteranceId(sbn.getPackageName(), sbn.getId());

        NotificationEntity notificationEntity = new NotificationEntity(sbn.getId(),
                sbn.getPackageName(),
                label,
                sbn.getPostTime(),
                utteranceId);

        notificationEntity.setMessages(IterateBundleExtras(sbn.getNotification().extras, ));
        if (sbn.getNotification().tickerText != null) {
            notificationEntity.setTinkerText(sbn.getNotification().tickerText.toString());
            notificationEntity.addMessage("custom.tickerText", sbn.getNotification().tickerText.toString());
        }

        return notificationEntity;
    }
    public static String getUtteranceId(String packageName, long sbnId)
    {
        StringBuilder utteranceId = new StringBuilder();
        utteranceId.append(sbnId)
                .append("_")
                .append(packageName);
        return utteranceId.toString();
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

    public static Notification createPersistentAppNotification(Context context, PendingIntent pendingIntent)
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

    public static boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public static boolean tryParseBoolean(String value) {
        try {
            Boolean.parseBoolean(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String[] classStaticFields(Class<Object> c)
    {
        Field[] fields = c.getDeclaredFields();
        List<String> list = new ArrayList<>();
        for(Field field:fields)
        {try {
            if (field.getType().equals(String.class) && field.getName().contains("EXTRAS_") && Modifier.isStatic(field.getModifiers()))
                list.add(String.valueOf(field.get(null)));
        }
        catch(IllegalAccessException iae)
        {
            Log.d(TAG,"!!!!!!!!!!!! class Static field, illegal access exception message: " + iae.getMessage());
        }
        }
        return list.toArray(new String[list.size()]);
    }
}
