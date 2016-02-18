package org.stream.split.voicenotification.Helpers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import org.stream.split.voicenotification.Enities.BaseEntity;
import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Enities.BundleKeysOwner;
import org.stream.split.voicenotification.Enities.HistoryBundleKeyEntity;
import org.stream.split.voicenotification.Enities.HistoryNotificationEntity;
import org.stream.split.voicenotification.Logging.BaseLogger;
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
import java.util.Locale;
import java.util.Set;

/**
 * Created by split on 2015-11-17.
 */
public class Helper {
    public static final String TAG = "HELPERS";
    public static final BaseLogger logger = BaseLogger.getInstance();

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
    public static List<HistoryBundleKeyEntity>  IterateBundleExtras(Bundle bundle, HistoryNotificationEntity historyNotificationEntity)
    {
        List<HistoryBundleKeyEntity> bundlekeys = new ArrayList<>();
        String [] keys = getClassStaticFieldNames(Notification.class, String.class, "EXTRA_");

        for(String key:keys)
        {
            Object value = bundle.get(key);
            HistoryBundleKeyEntity historyBundleKeyEntity = null;

            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("key: ");
            logBuilder.append(key);
            logBuilder.append("\t\t\t\t");
            if(value != null )  {

                if(value instanceof Object[])
                {
                    logBuilder.append("\n====== Charsequence[]======\n");

                    StringBuilder builder = new StringBuilder();


                    for(Object seq:(Object[])value) {
                        logBuilder.append("value(charseq): ");
                        logBuilder.append(seq);
                        logBuilder.append("\n");

                        builder.append(seq);
                        builder.append("\n");
                    }

                    historyBundleKeyEntity = new HistoryBundleKeyEntity(historyNotificationEntity.getPackageName(),
                            historyNotificationEntity.getSbnId(), builder.toString(), key);

                    logBuilder.append("====== \"Charsequence[]======\n");
                }
                else {
                    logBuilder.append("value: ");
                    logBuilder.append(value);
                    logBuilder.append("\t");
                    historyBundleKeyEntity = new HistoryBundleKeyEntity(historyNotificationEntity.getPackageName(),
                            historyNotificationEntity.getSbnId(), new StringBuilder().append(value).toString(), key);
                }

                logBuilder.append("value class: ");
                logBuilder.append(value.getClass().getName());
            }
            else
            {
                logBuilder.append("value: null");
                historyBundleKeyEntity = new HistoryBundleKeyEntity(historyNotificationEntity.getPackageName(),
                        historyNotificationEntity.getSbnId(), "", key);
            }

            if(historyBundleKeyEntity != null)
                bundlekeys.add(historyBundleKeyEntity);

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

    public static StringBuilder LogNotificationEntity(HistoryNotificationEntity entity)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(entity.getPackageName());
        builder.append("\n");
        List<HistoryBundleKeyEntity> list = entity.getBundleKeys();
        builder.append("bundlekeys: \n");
        if(list != null || list.isEmpty())
        {

            for(HistoryBundleKeyEntity e:list)
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
                .setSmallIcon(R.drawable.ic_persistent_notification)
        .setTicker("voice notification");

        return builder.build();
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

    public static <T extends BundleKeysOwner> T getAllNotificationBundleKeys(T bundleKeyOwner)
    {
        String [] keys = getClassStaticFieldNames(Notification.class, String.class, "EXTRA_");
        List<BundleKeyEntity> bundleKeys = bundleKeyOwner.getBundleKeys();
        for(String key:keys)
        {
            boolean absent = true;
            for(BundleKeyEntity entity:bundleKeys) {
                if(entity.getKey().equals(key)) {
                    absent= false;
                    break;
                }
            }

            if(absent)
            {
                bundleKeyOwner.addBundleKey(new BundleKeyEntity(key));
            }
        }
        return bundleKeyOwner;
    }

    public static String[] getClassStaticFieldNames(Class c, Type fieldType, String nameContains)
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
    public static boolean isAnyItemModified(List<? extends BaseEntity> entities)
    {
        boolean isModified= false;
        for(BaseEntity entity:entities)
        {
            if(entity.isModified()) {
                isModified = true;
                break;
            }
        }
        return isModified;
    }
    public static boolean isAnyItemSelected(List<? extends BaseEntity> entities)
    {
        boolean isSelected= false;
        for(BaseEntity entity:entities)
        {
            if(entity.isSelected()) {
                isSelected = true;
                break;
            }
        }
        return isSelected;
    }
    public static String convertTime(long time){
        Date date = new Date(time);
        Format format = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault());
        return format.format(date);
    }
}
