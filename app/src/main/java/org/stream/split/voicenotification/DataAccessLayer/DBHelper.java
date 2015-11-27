package org.stream.split.voicenotification.DataAccessLayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.stream.split.voicenotification.Enities.AppInfoEntity;
import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Enities.NotificationEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by split on 2015-10-27.
 */
public class DBHelper extends SQLiteOpenHelper {
    static final String TAG = "DBHelper";

    public DBHelper(Context context)
    {
        super(context, DBContract.DB_Name, null, DBContract.DB_Version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(DBContract.AppFeed.SQL_CREATE_TABLE);
        db.execSQL(DBContract.NotificationBundleKeysFeed.SQL_CREATE_TABLE);
        db.execSQL(DBContract.NotificationHistoryFeed.SQL_CREATE_TABLE);
        db.execSQL(DBContract.NotificationBundlesHistoryFeed.SQL_CREATE_TABLE);
        db.execSQL(DBContract.NotificationHistoryFeed.SQL_INSERTION_TRRIGER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DBContract.NotificationBundlesHistoryFeed.SQL_DELETE_TABLE);
        db.execSQL(DBContract.NotificationHistoryFeed.SQL_DELETE_TABLE);
        db.execSQL(DBContract.NotificationBundleKeysFeed.SQL_DELETE_TABLE);
        db.execSQL(DBContract.AppFeed.SQL_DELETE_TABLE);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    public List<AppInfoEntity> getAllApps()
    {
        List<AppInfoEntity> apps = new ArrayList<>();
        String sqlQuery = "Select * from " + DBContract.AppFeed.TABLE_NAME;
        Log.d(TAG, sqlQuery);

        Cursor cursor = this.getReadableDatabase().rawQuery(sqlQuery,null);

        if(cursor.moveToFirst())
        {
            do {
                AppInfoEntity app = new AppInfoEntity();
                app.setPackageName(cursor.getString(cursor.getColumnIndex(DBContract.AppFeed.COLUMN_NAME_PACKAGE_NAME)));
                apps.add(app);
            }while(cursor.moveToNext());
        }
        return apps;
    }

    /**
     * inserts AppInfoEntity object into database table AppFeed
     * @param app
     * @return row id of newely inserted AppInfoEntity. -1 if there was error.
     */
    public long addApp(AppInfoEntity app)
    {
        ContentValues values = new ContentValues();
        values.put(DBContract.AppFeed.COLUMN_NAME_PACKAGE_NAME,app.getPackageName());
        return getWritableDatabase().insert(DBContract.AppFeed.TABLE_NAME, null, values);
    }

    public List<Long> addApps(List<AppInfoEntity> apps)
    {
        List<Long> rows = new ArrayList<>();
        for(AppInfoEntity app:apps)
        {
            rows.add(addApp(app));
        }
        return rows;
    }

    public boolean isAppFollowed(String PackageName)
    {
        String sqlQuery = "Select * from "  + DBContract.AppFeed.TABLE_NAME +
                " where " + DBContract.AppFeed.COLUMN_NAME_PACKAGE_NAME + " = '" + PackageName+ "'";
        Log.d(TAG, sqlQuery);
        Cursor cursor = this.getReadableDatabase().rawQuery(sqlQuery, null);
        if(cursor != null && cursor.getCount()>0 ) {
            Log.d(TAG, "cursor getcount() = " + new StringBuilder().append(cursor.getCount()).toString());
            return true;
        }
        return false;
    }
    public void deleteApp (AppInfoEntity app)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DBContract.NotificationBundleKeysFeed.TABLE_NAME,
                DBContract.NotificationBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = ?",
                new String[]{app.getPackageName()});
        db.delete(DBContract.AppFeed.TABLE_NAME,
                DBContract.AppFeed.COLUMN_NAME_PACKAGE_NAME + " = ?",
                new String[]{app.getPackageName()});
    }
    public NotificationEntity addNotification(NotificationEntity notificationEntity)
    {
        long notificationId = addNotificationHistoryEntry(notificationEntity);
        addNotificationBundleHistoryEntries(notificationEntity, notificationId);
        notificationEntity.setID(notificationId);
        return notificationEntity;

    }
    private long addNotificationHistoryEntry( NotificationEntity notificationEntity)
    {
        ContentValues values = new ContentValues();
        values.put(DBContract.NotificationHistoryFeed.COLUMN_NAME_PACKAGE_NAME, notificationEntity.getPackageName());
        values.put(DBContract.NotificationHistoryFeed.COLUMN_NAME_INSERTION_TIMESTAMP, notificationEntity.getOccurrenceTime());
        values.put(DBContract.NotificationHistoryFeed.COLUMN_NAME_UTTERANCE_ID, notificationEntity.getUtteranceId());
        values.put(DBContract.NotificationHistoryFeed.COLUMN_NAME_APPLICATION_LABEL, notificationEntity.getApplicationLabel());
        return getWritableDatabase().insert(DBContract.NotificationHistoryFeed.TABLE_NAME,null, values);
    }
    private List<Long> addNotificationBundleHistoryEntries(NotificationEntity notificationEntity, long notificationId)
    {
        List<Long> rowIds = new ArrayList<>();
        ContentValues values = new ContentValues();
        values.put(DBContract.NotificationBundlesHistoryFeed.COLUMN_NAME_NOTIFICATION_ID, notificationId);
        for(Map.Entry<String,String> entry:notificationEntity.getMessages().entrySet())
        {
            values.put(DBContract.NotificationBundlesHistoryFeed.COLUMN_NAME_BUNDLE_KEY, entry.getKey());
            values.put(DBContract.NotificationBundlesHistoryFeed.COLUMN_NAME_BUNDLE_VALUE, entry.getValue());
            rowIds.add(getWritableDatabase().insert(DBContract.NotificationBundlesHistoryFeed.TABLE_NAME, null, values));
        }
        return rowIds;
    }

    public List<Long> updateNotification (NotificationEntity notificationEntity, boolean updateNotificationMessages)
    {
        List<Long> affectedRows = new ArrayList<>();
        affectedRows.add( updateNotificationHistoryEntry(notificationEntity));
        if(updateNotificationMessages)
        {
            affectedRows.add(updateNotificationMessagesValues(notificationEntity));
        }
        return affectedRows;
    }

    /**
     *
     * @param notificationEntity
     * @return numbers of rows affected
     */
    private long updateNotificationHistoryEntry(NotificationEntity notificationEntity)
    {
        ContentValues values = new ContentValues();
        values.put(DBContract.NotificationHistoryFeed.COLUMN_NAME_PACKAGE_NAME, notificationEntity.getPackageName());
        values.put(DBContract.NotificationHistoryFeed.COLUMN_NAME_INSERTION_TIMESTAMP, notificationEntity.getOccurrenceTime());
        values.put(DBContract.NotificationHistoryFeed.COLUMN_NAME_UTTERANCE_ID, notificationEntity.getUtteranceId());
        values.put(DBContract.NotificationHistoryFeed.COLUMN_NAME_APPLICATION_LABEL, notificationEntity.getApplicationLabel());
        long rowsAffected = getWritableDatabase().update(DBContract.NotificationHistoryFeed.TABLE_NAME,
                values,
                DBContract.NotificationHistoryFeed.COLUMN_NAME_NOTIFICATION_ID + " = " + notificationEntity.getID(), null);
        return rowsAffected;
    }

    //TODO trzeba koniecznie przetestować przed użyciem
    private long updateNotificationMessagesValues(NotificationEntity notificationEntity)
    {
        long affectedRows = 0;
        ContentValues values = new ContentValues();
        for(Map.Entry<String,String> entry:notificationEntity.getMessages().entrySet())
        {
            values.put(DBContract.NotificationBundlesHistoryFeed.COLUMN_NAME_BUNDLE_VALUE, entry.getValue());
            affectedRows += getWritableDatabase().update(DBContract.NotificationBundlesHistoryFeed.TABLE_NAME,
                    values,
                    DBContract.NotificationBundlesHistoryFeed.COLUMN_NAME_NOTIFICATION_ID +" = "+ notificationEntity.getID() +" AND " +
                            DBContract.NotificationBundlesHistoryFeed.COLUMN_NAME_BUNDLE_KEY + " = '"+entry.getKey() +"'",
                    null);
        }
        return affectedRows;
    }

    public List<NotificationEntity> getAllNotification()
    {
        List <NotificationEntity> notifications = new ArrayList<>();
        String sql_select_all = "SELECT * FROM " + DBContract.NotificationHistoryFeed.TABLE_NAME;
        Cursor cursor = getReadableDatabase().rawQuery(sql_select_all,null);
        if(cursor.moveToFirst())
        {
            do {
                notifications.add(getNotification(cursor));
            }while(cursor.moveToNext());
        }
        return notifications;
    }

    private NotificationEntity getNotification(Cursor cursor)
    {
        long notificationId = cursor.getLong(cursor.getColumnIndex(DBContract.NotificationHistoryFeed.COLUMN_NAME_NOTIFICATION_ID));
        NotificationEntity notificationEntity = new NotificationEntity(notificationId);
        notificationEntity.setPackageName(cursor.getString(cursor.getColumnIndex(DBContract.NotificationHistoryFeed.COLUMN_NAME_PACKAGE_NAME)));
        notificationEntity.setOccurenceTime(cursor.getLong(cursor.getColumnIndex(DBContract.NotificationHistoryFeed.COLUMN_NAME_INSERTION_TIMESTAMP)));
        notificationEntity.setTinkerText(cursor.getString(cursor.getColumnIndex(DBContract.NotificationHistoryFeed.COLUMN_NAME_TINKER_TEXT)));
        notificationEntity.setApplicationLabel(cursor.getString(cursor.getColumnIndex(DBContract.NotificationHistoryFeed.COLUMN_NAME_APPLICATION_LABEL)));
        notificationEntity.setUtteranceId(cursor.getString(cursor.getColumnIndex(DBContract.NotificationHistoryFeed.COLUMN_NAME_UTTERANCE_ID)));
        notificationEntity.setMessages(getMessages(notificationId));
        return notificationEntity;
    }
    public Map<String,String> getMessages(long notificationId)
    {
        String sql_select_id = "SELECT * FROM "+ DBContract.NotificationBundlesHistoryFeed.TABLE_NAME +
                " WHERE " + DBContract.NotificationBundlesHistoryFeed.COLUMN_NAME_NOTIFICATION_ID + " = " + notificationId + ";";
        Cursor cursor = getReadableDatabase().rawQuery(sql_select_id, null);
        return getMessages(cursor);
    }
    private Map<String,String> getMessages(Cursor cursor)
    {
        Map<String,String> map = new HashMap<>();
        if(cursor.moveToFirst())
        {
            do {
                String key = cursor.getString(cursor.getColumnIndex(DBContract.NotificationBundlesHistoryFeed.COLUMN_NAME_BUNDLE_KEY));
                String value = cursor.getString(cursor.getColumnIndex(DBContract.NotificationBundlesHistoryFeed.COLUMN_NAME_BUNDLE_VALUE));
                map.put(key, value);
            }while(cursor.moveToNext());
        }
        return map;
    }

    public List<BundleKeyEntity> getBundleKeys(String packageName)
    {
        List<BundleKeyEntity> bundleKeys = new ArrayList<>();

        String sql_select_bundlekeys = "SELECT * FROM " + DBContract.NotificationBundleKeysFeed.TABLE_NAME +
                " WHERE " + DBContract.NotificationBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = '" + packageName+"';";
        Log.d(TAG, sql_select_bundlekeys);

        Cursor cursor = getReadableDatabase().rawQuery(sql_select_bundlekeys, null);
        if(cursor.moveToFirst())
        {
            do{
                String bundleKey = cursor.getString(cursor.getColumnIndex(DBContract.NotificationBundleKeysFeed.COLUMN_NAME_KEY));
                int priority = cursor.getInt(cursor.getColumnIndex(DBContract.NotificationBundleKeysFeed.COLUMN_NAME_PRIORITY));
                BundleKeyEntity entity = new BundleKeyEntity(packageName,bundleKey,priority);
                bundleKeys.add(entity);
                Log.d(TAG, "bundle Key: " + bundleKey + "\tPriority: " + priority);
            }while (cursor.moveToNext());
        }
        return bundleKeys;
    }
    public List<BundleKeyEntity> getSortedBundleKeys(String packageName)
    {
        List<BundleKeyEntity> bundleKeys = new ArrayList<>();

        String sql_select_bundlekeys = "SELECT * FROM " + DBContract.NotificationBundleKeysFeed.TABLE_NAME +
                " WHERE " + DBContract.NotificationBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = '" + packageName+"'"+
                " ORDER BY " + DBContract.NotificationBundleKeysFeed.COLUMN_NAME_PRIORITY + " DESC;";
        Log.d(TAG, sql_select_bundlekeys);

        Cursor cursor = getReadableDatabase().rawQuery(sql_select_bundlekeys, null);
        if(cursor.moveToFirst())
        {
            do{
                String bundleKey = cursor.getString(cursor.getColumnIndex(DBContract.NotificationBundleKeysFeed.COLUMN_NAME_KEY));
                int priority = cursor.getInt(cursor.getColumnIndex(DBContract.NotificationBundleKeysFeed.COLUMN_NAME_PRIORITY));
                BundleKeyEntity entity = new BundleKeyEntity(packageName,bundleKey,priority);
                bundleKeys.add(entity);
                Log.d(TAG, "bundle Key: " + bundleKey + "\tPriority: " + priority);
            }while (cursor.moveToNext());
        }
        return bundleKeys;
    }
    public long addBundleKey(BundleKeyEntity bundleKeyEntity)
    {
        ContentValues values = new ContentValues();
        values.put(DBContract.NotificationBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME, bundleKeyEntity.getPackageName());
        values.put(DBContract.NotificationBundleKeysFeed.COLUMN_NAME_KEY, bundleKeyEntity.getKey());
        values.put(DBContract.NotificationBundleKeysFeed.COLUMN_NAME_PRIORITY, bundleKeyEntity.getPriority());
        return getWritableDatabase().insert(DBContract.NotificationBundlesHistoryFeed.TABLE_NAME,null,values);
    }
}
