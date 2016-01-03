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
import java.util.List;

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
        db.execSQL(DBContract.NotificationHistoryFeed.SQL_CREATE_TABLE);
        db.execSQL(DBContract.NotificationHistoryFeed.SQL_INSERTION_TRIGGER);
        db.execSQL(DBContract.BundleKeysFeed.SQL_CREATE_TABLE);
        db.execSQL(DBContract.BundlesHistoryFeed.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(DBContract.BundlesHistoryFeed.SQL_DELETE_TABLE);
        db.execSQL(DBContract.NotificationHistoryFeed.SQL_DELETE_TABLE);
        db.execSQL(DBContract.BundleKeysFeed.SQL_DELETE_TABLE);
        db.execSQL(DBContract.AppFeed.SQL_DELETE_TABLE);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    public List<AppInfoEntity> getAllApps(boolean getBundleKeys)
    {
        List<AppInfoEntity> apps = new ArrayList<>();
        String sqlQuery = "Select * from " + DBContract.AppFeed.TABLE_NAME;
        Log.d(TAG, sqlQuery);

        Cursor cursor = this.getReadableDatabase().rawQuery(sqlQuery,null);

        if(cursor.moveToFirst())
        {
            do {
                AppInfoEntity entity = getApp(cursor,getBundleKeys);
                apps.add(entity);
            }while(cursor.moveToNext());
        }
        return apps;
    }
    public AppInfoEntity getApp(String packageName, boolean getBundleKeys)
    {
        String sqlQuery = "Select * from " + DBContract.AppFeed.TABLE_NAME +
                " WHERE " + DBContract.AppFeed.COLUMN_NAME_PACKAGE_NAME + " = " + packageName+";";
        Log.d(TAG, sqlQuery);

        Cursor cursor = this.getReadableDatabase().rawQuery(sqlQuery, null);
        AppInfoEntity entity = null;
        if(cursor.moveToFirst())
            entity = getApp(cursor,getBundleKeys);
        return entity;
    }
    private AppInfoEntity getApp(Cursor cursor, boolean getBundleKeys)
    {
        String packageName = cursor.getString(cursor.getColumnIndex(DBContract.AppFeed.COLUMN_NAME_PACKAGE_NAME));
        String label = cursor.getString(cursor.getColumnIndex(DBContract.AppFeed.COLUMN_NAME_APPLICATION_LABEL));
        AppInfoEntity app = new AppInfoEntity(packageName, label);
        app.setIsFollowed(true);
        if(getBundleKeys)
            app.setBundleKeys(getSortedBundleKeys(packageName));
        return app;
    }
    /**
     * inserts AppInfoEntity object into database table AppFeed
     * @param app
     * @return row id of newely inserted AppInfoEntity. -1 if there was error.
     */
    public long addApp(AppInfoEntity app)
    {
        ContentValues values = new ContentValues();
        values.put(DBContract.AppFeed.COLUMN_NAME_PACKAGE_NAME, app.getPackageName());
        values.put(DBContract.AppFeed.COLUMN_NAME_APPLICATION_LABEL, app.getApplicationLabel());
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
    public void deleteApps(List<AppInfoEntity> apps, boolean deleteBundleKeys)
    {
        for(AppInfoEntity entity:apps)
        {
            deleteApp(entity, deleteBundleKeys);
        }
    }
    public void deleteApp (AppInfoEntity app, boolean deleteBundleKeys)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        if(deleteBundleKeys) {
            deleteBundleKeys(app.getPackageName());
        }
        db.delete(DBContract.AppFeed.TABLE_NAME,
                DBContract.AppFeed.COLUMN_NAME_PACKAGE_NAME + " = ?",
                new String[]{app.getPackageName()});
    }
    public long addNotification(NotificationEntity notificationEntity)
    {
        long rowId = addNotificationHistoryEntry(notificationEntity);
        notificationEntity.setID(rowId);
        Log.d(TAG, "addNotification() rowId: " + rowId);
        if(rowId != -1)
            addNotificationBundleHistoryEntries(notificationEntity, rowId);
        return rowId;

    }
    private long addNotificationHistoryEntry( NotificationEntity notificationEntity)
    {
        ContentValues values = new ContentValues();
        values.put(DBContract.NotificationHistoryFeed.COLUMN_NAME_SBN_ID, notificationEntity.getSbnId());
        values.put(DBContract.NotificationHistoryFeed.COLUMN_NAME_PACKAGE_NAME, notificationEntity.getPackageName());
        values.put(DBContract.NotificationHistoryFeed.COLUMN_NAME_INSERTION_TIMESTAMP, notificationEntity.getOccurrenceTime());
        values.put(DBContract.NotificationHistoryFeed.COLUMN_NAME_UTTERANCE_ID, notificationEntity.getUtteranceId());
        values.put(DBContract.NotificationHistoryFeed.COLUMN_NAME_APPLICATION_LABEL, notificationEntity.getApplicationLabel());
        return getWritableDatabase().insert(DBContract.NotificationHistoryFeed.TABLE_NAME,null, values);
    }
    private List<Long> addNotificationBundleHistoryEntries(NotificationEntity notificationEntity, long notificationId)
    {
        List<Long> rowIds = new ArrayList<>();

        for(BundleKeyEntity entry:notificationEntity.getBundleKeys())
        {
            ContentValues values = new ContentValues();
            values.put(DBContract.BundlesHistoryFeed.COLUMN_NAME_NOTIFICATION_ID, notificationId);
            values.put(DBContract.BundlesHistoryFeed.COLUMN_NAME_BUNDLE_KEY, entry.getKey());
            values.put(DBContract.BundlesHistoryFeed.COLUMN_NAME_BUNDLE_VALUE, entry.getValue());
            values.put(DBContract.BundlesHistoryFeed.COLUMN_NAME_PACKAGE_NAME, entry.getPackageName());
            rowIds.add(getWritableDatabase().insert(DBContract.BundlesHistoryFeed.TABLE_NAME, null, values));
        }
        return rowIds;
    }

    public List<Long> updateNotification (NotificationEntity notificationEntity, boolean updateNotificationMessages)
    {
        List<Long> affectedRows = new ArrayList<>();
        affectedRows.add(updateNotificationHistoryEntry(notificationEntity));
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
                DBContract.NotificationHistoryFeed.COLUMN_NAME_ID + " = " + notificationEntity.getID(), null);
        return rowsAffected;
    }

    //TODO trzeba koniecznie przetestować przed użyciem
    private long updateNotificationMessagesValues(NotificationEntity notificationEntity)
    {
        long affectedRows = 0;
        ContentValues values = new ContentValues();
        for(BundleKeyEntity entry:notificationEntity.getBundleKeys())
        {
            values.put(DBContract.BundlesHistoryFeed.COLUMN_NAME_BUNDLE_VALUE, entry.getValue());
            affectedRows += getWritableDatabase().update(DBContract.BundlesHistoryFeed.TABLE_NAME,
                    values,
                    DBContract.BundlesHistoryFeed.COLUMN_NAME_NOTIFICATION_ID +" = "+ notificationEntity.getID() +" AND " +
                            DBContract.BundlesHistoryFeed.COLUMN_NAME_BUNDLE_KEY + " = '"+entry.getKey() +"'",
                    null);
        }
        return affectedRows;
    }
    //TODO add support for isfollowed - where AppFeed.packageName != null (columnName AS allias)
    public List<NotificationEntity> getAllNotification(boolean getBundleKeys)
    {
        List <NotificationEntity> notifications = new ArrayList<>();
        String sql_select_all = "SELECT * FROM " + DBContract.NotificationHistoryFeed.TABLE_NAME +
                " ORDER BY "+ DBContract.NotificationHistoryFeed.COLUMN_NAME_INSERTION_TIMESTAMP + "ASC ;";
        Cursor cursor = getReadableDatabase().rawQuery(sql_select_all, null);
        if(cursor.moveToFirst())
        {
            do {
                notifications.add(getNotification(cursor, getBundleKeys));
            }while(cursor.moveToNext());
        }
        return notifications;
    }
    public NotificationEntity getNotificaiton(long sbnId, String packageName, boolean getBundleKeys)
    {
        NotificationEntity notification = null;
        String sql_select = "SELECT * FROM " + DBContract.NotificationHistoryFeed.TABLE_NAME +" WHERE " +
                DBContract.NotificationHistoryFeed.COLUMN_NAME_PACKAGE_NAME + " = '" + packageName + "' AND " +
                DBContract.NotificationHistoryFeed.COLUMN_NAME_SBN_ID + " = " + sbnId+ ";";
        Cursor cursor = getReadableDatabase().rawQuery(sql_select,null);
        if(cursor.moveToFirst())
        {
            notification = getNotification(cursor, getBundleKeys);
        }
        return notification;
    }

    private NotificationEntity getNotification(Cursor cursor, boolean getBundleKeys)
    {
        long notificationId = cursor.getLong(cursor.getColumnIndex(DBContract.NotificationHistoryFeed.COLUMN_NAME_ID));
        NotificationEntity notificationEntity = new NotificationEntity(notificationId);
        notificationEntity.setPackageName(cursor.getString(cursor.getColumnIndex(DBContract.NotificationHistoryFeed.COLUMN_NAME_PACKAGE_NAME)));
        notificationEntity.setOccurrenceTime(cursor.getLong(cursor.getColumnIndex(DBContract.NotificationHistoryFeed.COLUMN_NAME_INSERTION_TIMESTAMP)));
        notificationEntity.setTinkerText(cursor.getString(cursor.getColumnIndex(DBContract.NotificationHistoryFeed.COLUMN_NAME_TINKER_TEXT)));
        notificationEntity.setApplicationLabel(cursor.getString(cursor.getColumnIndex(DBContract.NotificationHistoryFeed.COLUMN_NAME_APPLICATION_LABEL)));
        notificationEntity.setUtteranceId(cursor.getString(cursor.getColumnIndex(DBContract.NotificationHistoryFeed.COLUMN_NAME_UTTERANCE_ID)));
        notificationEntity.setIsFollowed(isAppFollowed(notificationEntity.getPackageName()));
        if(getBundleKeys)
            notificationEntity.setBundleKeys(getMessages(notificationId));
        return notificationEntity;
    }
    public List<BundleKeyEntity> getMessages(long notificationId)
    {
        String sql_select_id = "SELECT * FROM "+ DBContract.BundlesHistoryFeed.TABLE_NAME +
                " WHERE " + DBContract.BundlesHistoryFeed.COLUMN_NAME_NOTIFICATION_ID + " = " + notificationId + ";";
        Cursor cursor = getReadableDatabase().rawQuery(sql_select_id, null);
        return getMessages(cursor);
    }
    private List<BundleKeyEntity> getMessages(Cursor cursor)
    {
        List<BundleKeyEntity> bundlekeys = new ArrayList<>();
        if(cursor.moveToFirst())
        {
            do {
                BundleKeyEntity entity = getMesssage(cursor);
                bundlekeys.add(entity);
            }while(cursor.moveToNext());
        }
        return bundlekeys;
    }
    private BundleKeyEntity getMesssage(Cursor cursor)
    {
        String key = cursor.getString(cursor.getColumnIndex(DBContract.BundlesHistoryFeed.COLUMN_NAME_BUNDLE_KEY));
        String value = cursor.getString(cursor.getColumnIndex(DBContract.BundlesHistoryFeed.COLUMN_NAME_BUNDLE_VALUE));
        String packageName = cursor.getString(cursor.getColumnIndex(DBContract.BundlesHistoryFeed.COLUMN_NAME_PACKAGE_NAME));
        BundleKeyEntity entity = new BundleKeyEntity(packageName,key,value);
        boolean isFollowed =isBundleKeyFollowed(entity);
        if(isFollowed) {
            entity.setPriority(getBundleKeyPriority(entity));
        }
        entity.setIsFollowed(isFollowed);
        return entity;
    }

    public List<BundleKeyEntity> getBundleKeys(String packageName)
    {
        List<BundleKeyEntity> bundleKeys = new ArrayList<>();

        String sql_select_bundlekeys = "SELECT * FROM " + DBContract.BundleKeysFeed.TABLE_NAME +
                " WHERE " + DBContract.BundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = '" + packageName+"';";
        Log.d(TAG, sql_select_bundlekeys);

        Cursor cursor = getReadableDatabase().rawQuery(sql_select_bundlekeys, null);
        if(cursor.moveToFirst())
        {
            do{
                String bundleKey = cursor.getString(cursor.getColumnIndex(DBContract.BundleKeysFeed.COLUMN_NAME_BUNDLE_KEY));
                int priority = cursor.getInt(cursor.getColumnIndex(DBContract.BundleKeysFeed.COLUMN_NAME_PRIORITY));
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

        String sql_select_bundlekeys = "SELECT * FROM " + DBContract.BundleKeysFeed.TABLE_NAME +
                " WHERE " + DBContract.BundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = '" + packageName+"'"+
                " ORDER BY " + DBContract.BundleKeysFeed.COLUMN_NAME_PRIORITY + " DESC;";
        Log.d(TAG, sql_select_bundlekeys);

        Cursor cursor = getReadableDatabase().rawQuery(sql_select_bundlekeys, null);
        if(cursor.moveToFirst())
        {
            do{
                BundleKeyEntity entity = getBundleKey(cursor);
                bundleKeys.add(entity);
                Log.d(TAG, "bundle Key: " + entity.getKey() + "\tPriority: " + entity.getPriority());
            }while (cursor.moveToNext());
        }
        return bundleKeys;
    }
    private BundleKeyEntity getBundleKey(Cursor cursor)
    {
        String bundleKey = cursor.getString(cursor.getColumnIndex(DBContract.BundleKeysFeed.COLUMN_NAME_BUNDLE_KEY));
        int priority = cursor.getInt(cursor.getColumnIndex(DBContract.BundleKeysFeed.COLUMN_NAME_PRIORITY));
        String packageName = cursor.getString(cursor.getColumnIndex(DBContract.BundleKeysFeed.COLUMN_NAME_PACKAGE_NAME));
        BundleKeyEntity entity = new BundleKeyEntity(packageName,bundleKey,priority);
        return entity;
    }
    public void addUpdateBundleKey(BundleKeyEntity bundleKeyEntity) {
        String sql_insert_or_update = "INSERT OR REPLACE INTO " + DBContract.BundleKeysFeed.TABLE_NAME + " ("
                + DBContract.BundleKeysFeed.COLUMN_NAME_PACKAGE_NAME +
                ", " + DBContract.BundleKeysFeed.COLUMN_NAME_BUNDLE_KEY +
                ", " + DBContract.BundleKeysFeed.COLUMN_NAME_PRIORITY + ")" +
                "  VALUES ('" + bundleKeyEntity.getPackageName() +"'"+
                ", '" + bundleKeyEntity.getKey() +"'"+
                ", " + bundleKeyEntity.getPriority() + ");";

        getWritableDatabase().execSQL(sql_insert_or_update);
    }
    public boolean isBundleKeyFollowed(BundleKeyEntity entity)
    {
        String sql_query = "SELECT * FROM "+ DBContract.BundleKeysFeed.TABLE_NAME +
                " WHERE " + DBContract.BundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = '" + entity.getPackageName() + "'"+
                " AND " + DBContract.BundleKeysFeed.COLUMN_NAME_BUNDLE_KEY + " = '" + entity.getKey() +"';";
        Cursor cursor = getReadableDatabase().rawQuery(sql_query,null);
        return cursor.moveToFirst();
    }
    public int getBundleKeyPriority(BundleKeyEntity entity)
    {
        int result = 0;
        String sql_query = "SELECT "+ DBContract.BundleKeysFeed.COLUMN_NAME_PRIORITY +" FROM "+ DBContract.BundleKeysFeed.TABLE_NAME +
                " WHERE " + DBContract.BundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = '" + entity.getPackageName() + "'"+
                " AND " + DBContract.BundleKeysFeed.COLUMN_NAME_BUNDLE_KEY + " = '" + entity.getKey() +"';";
        Cursor cursor = getReadableDatabase().rawQuery(sql_query,null);
        if(cursor.moveToFirst())
            result =  cursor.getInt(cursor.getColumnIndex(DBContract.BundleKeysFeed.COLUMN_NAME_PRIORITY));
        return result;
    }
    public void deleteBundleKeys(String packageName)
    {
        SQLiteDatabase db = this.getWritableDatabase();
            db.delete(DBContract.BundleKeysFeed.TABLE_NAME,
                    DBContract.BundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = ?",
                    new String[]{packageName});
    }
    public void deleteBundleKey(BundleKeyEntity entity)
    {
        String sql_query = "DELETE FROM "+ DBContract.BundleKeysFeed.TABLE_NAME +
                " WHERE " + DBContract.BundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = '" + entity.getPackageName() + "'"+
                " AND " + DBContract.BundleKeysFeed.COLUMN_NAME_BUNDLE_KEY + " = '" + entity.getKey() +"';";
        getWritableDatabase().execSQL(sql_query);
    }
}
