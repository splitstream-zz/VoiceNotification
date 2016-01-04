package org.stream.split.voicenotification.Helpers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.stream.split.voicenotification.Enities.AppInfoEntity;
import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Enities.NotificationEntity;
import org.stream.split.voicenotification.R;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.sql.Date;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
        String [] keys = classStaticFieldNames(Notification.class,String.class, "EXTRA_");

        for(String key:keys)
        {
            Object value = bundle.get(key);

            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("key: ");
            logBuilder.append(key);
            logBuilder.append("\t\t\t\t");
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
            {
                logBuilder.append("value: null");
                bundlekeys.add(new BundleKeyEntity(packageName, key, ""));
            }

            Log.d(TAG, logBuilder.toString());
        }

        return bundlekeys;
    }

    private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

    public static boolean isWrapperType(Class<?> clazz)
    {
        return WRAPPER_TYPES.contains(clazz);
    }

    private static Set<Class<?>> getWrapperTypes()
    {
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        ret.add(String.class);
        return ret;
    }

    public static StringBuilder LogNotificationEntity(Object notificationEntity, StringBuilder builder)
    {
        Field[] fields = notificationEntity.getClass().getDeclaredFields();
        builder.append(notificationEntity.getClass().getSimpleName());
        for(Field field:fields)
        {

            builder.append(field.getName());
            builder.append(field.getType());
            try {
                Object value = field.get(notificationEntity);
                if(value != null) {
                    if (!isWrapperType(field.getType())) {
                        builder.append("lets go deeper is not primitive!!!!!");
                        LogNotificationEntity(value, builder);
                        continue;
                    } else if (value instanceof Collection<?>) {
                        for (Object o : (Collection) value) {
                            builder.append("Collection Object");
                            builder.append(LogNotificationEntity(o, builder));
                        }
                    } else {
                        builder.append(value);
                    }
                }
                else
                    builder.append("value is null");

            }
            catch (IllegalAccessException iae)
            {
                //builder.append(iae.getMessage());
            }
        }
        return builder;
    }

    public static StringBuilder LogNotificationEntity(NotificationEntity entity)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(entity.getApplicationLabel());
        builder.append("\n");
        List<BundleKeyEntity> list = entity.getBundleKeys();
        builder.append("bundlekeys: \n");
        if(list != null || list.isEmpty())
        {

            for(BundleKeyEntity e:list)
            {
                builder.append("key: ");
                builder.append(e.getKey());
                builder.append("\n");
                builder.append("value: ");
                builder.append(e.getValue());
                builder.append("\n");
            }
        }
        return builder;
    }

    public static NotificationEntity createNotificationEntity(StatusBarNotification sbn, String label) {

        String utteranceId = getUtteranceId(sbn.getPackageName(), sbn.getId());

        NotificationEntity notificationEntity = new NotificationEntity(sbn.getId(),
                sbn.getPackageName(),
                label,
                sbn.getPostTime(),
                utteranceId);

        notificationEntity.setBundleKeys(IterateBundleExtras(sbn.getNotification().extras, sbn.getPackageName()));
        if (sbn.getNotification().tickerText != null) {
            notificationEntity.setTinkerText(sbn.getNotification().tickerText.toString());
            notificationEntity.addBundleKey("custom.tickerText", sbn.getNotification().tickerText.toString());
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

    public static List<BundleKeyEntity> getAllNotificationBundleKeys(String packageName)
    {
        List<BundleKeyEntity> bundleKeyEntities = new ArrayList<>();
        String [] keys = classStaticFieldNames(Notification.class,String.class, "EXTRA_");
        for(String key:keys)
        {
            BundleKeyEntity entity = new BundleKeyEntity(packageName,key);
            bundleKeyEntities.add(entity);
        }
        return bundleKeyEntities;
    }

    public static String[] classStaticFieldNames(Class c,Type fieldType, String nameContains)
    {
        Field[] fields = c.getDeclaredFields();
        List<String> list = new ArrayList<>();
        for(Field field:fields)
        {try {
            boolean isString = field.getType().equals(fieldType);
            boolean containsExtra = field.getName().contains(nameContains);
            boolean isStatic = Modifier.isStatic(field.getModifiers());
            if (field.getType().equals(String.class) && field.getName().contains("EXTRA_") && Modifier.isStatic(field.getModifiers()))
                list.add(String.valueOf(field.get(null)));
        }
        catch(IllegalAccessException iae)
        {
            Log.d(TAG,"!!!!!!!!!!!! class Static field, illegal access exception message: " + iae.getMessage());
        }
        }
        return list.toArray(new String[list.size()]);
    }
    public static boolean isAnyItemSelected(List<AppInfoEntity> entities)
    {
        boolean isSelected= false;
        for(AppInfoEntity entity:entities)
        {
            if(entity.isModified()) {
                isSelected = true;
                break;
            }
        }
        return isSelected;
    }
    public static String convertTime(long time){
        Date date = new Date(time);
        Format format = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        return format.format(date);
    }
}
