package org.stream.split.voicenotification.DataAccessLayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.stream.split.voicenotification.Enities.AppInfoEntity;
import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Enities.HistoryNotificationEntity;
import org.stream.split.voicenotification.Enities.NotificationEntity;
import org.stream.split.voicenotification.Logging.BaseLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by split on 2015-10-27.
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final String TAG = "DBHelper";
    private BaseLogger logger = BaseLogger.getInstance();
    Context mContext;

    public DBHelper(Context context) {
        super(context, DBContract.DB_Name, null, DBContract.DB_Version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(DBContract.FollowedAppFeed.SQL_CREATE_TABLE);
        db.execSQL(DBContract.FollowedNotificationsFeed.SQL_CREATE_TABLE);
        db.execSQL(DBContract.HistoryNotificationFeed.SQL_CREATE_TABLE);
        db.execSQL(DBContract.HistoryNotificationFeed.SQL_INSERTION_TRIGGER);
        db.execSQL(DBContract.FollowedBundleKeysFeed.SQL_CREATE_TABLE);
        db.execSQL(DBContract.HistoryBundlesKeysFeed.SQL_CREATE_TABLE);
        db.execSQL(DBContract.SettingFeed.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(DBContract.HistoryBundlesKeysFeed.SQL_DELETE_TABLE);
        db.execSQL(DBContract.HistoryNotificationFeed.SQL_DELETE_TABLE);
        db.execSQL(DBContract.FollowedBundleKeysFeed.SQL_DELETE_TABLE);
        db.execSQL(DBContract.FollowedNotificationsFeed.SQL_CREATE_TABLE);
        db.execSQL(DBContract.FollowedAppFeed.SQL_DELETE_TABLE);
        db.execSQL(DBContract.HistoryBundlesKeysFeed.SQL_DELETE_TABLE);
        db.execSQL(DBContract.SettingFeed.SQL_DELETE_TABLE);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    public List<AppInfoEntity> getAllFollowedApps(boolean getBundleKeys) {
        List<AppInfoEntity> apps = new ArrayList<>();
        String sqlQuery = "Select * from " + DBContract.FollowedAppFeed.TABLE_NAME;
        logger.d(TAG, sqlQuery);

        Cursor cursor = this.getReadableDatabase().rawQuery(sqlQuery, null);

        if (cursor.moveToFirst()) {
            do {
                AppInfoEntity entity = getFollowedApp(cursor, getBundleKeys);
                apps.add(entity);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return apps;
    }

    public AppInfoEntity getFollowedApp(String packageName, boolean getBundleKeys) {
        String sqlQuery = "Select * from " + DBContract.FollowedAppFeed.TABLE_NAME +
                " WHERE " + DBContract.FollowedAppFeed.COLUMN_NAME_PACKAGE_NAME + " = " + packageName + ";";
        logger.d(TAG, sqlQuery);

        Cursor cursor = this.getReadableDatabase().rawQuery(sqlQuery, null);
        AppInfoEntity entity = null;
        if (cursor.moveToFirst())
            entity = getFollowedApp(cursor, getBundleKeys);
        cursor.close();
        return entity;
    }

    private AppInfoEntity getFollowedApp(Cursor cursor, boolean getBundleKeys) {
        String packageName = cursor.getString(cursor.getColumnIndex(DBContract.FollowedAppFeed.COLUMN_NAME_PACKAGE_NAME));
        String label = cursor.getString(cursor.getColumnIndex(DBContract.FollowedAppFeed.COLUMN_NAME_APPLICATION_LABEL));
        AppInfoEntity app = new AppInfoEntity(packageName, label);
        app.setIsFollowed(true);
        if (getBundleKeys)
            app.setBundleKeys(getSortedFollowedBundleKeys(app));
        return app;
    }

    /**
     * inserts AppInfoEntity object into database table FollowedAppFeed
     *
     * @param app
     * @return row id of newely inserted AppInfoEntity. -1 if there was error.
     */
    public long addFollowedApp(AppInfoEntity app) {
        ContentValues values = new ContentValues();
        values.put(DBContract.FollowedAppFeed.COLUMN_NAME_PACKAGE_NAME, app.getPackageName());
        values.put(DBContract.FollowedAppFeed.COLUMN_NAME_APPLICATION_LABEL, app.getApplicationLabel());
        return getWritableDatabase().insert(DBContract.FollowedAppFeed.TABLE_NAME, null, values);
    }

    public List<Long> addFollowedApps(List<AppInfoEntity> apps) {
        List<Long> rows = new ArrayList<>();
        for (AppInfoEntity app : apps) {
            rows.add(addFollowedApp(app));
        }
        return rows;
    }

    public boolean isAppFollowed(String PackageName) {
        String sqlQuery = "Select * from " + DBContract.FollowedAppFeed.TABLE_NAME +
                " where " + DBContract.FollowedAppFeed.COLUMN_NAME_PACKAGE_NAME + " = '" + PackageName + "'";
        boolean result = false;
        logger.d(TAG, sqlQuery);
        Cursor cursor = this.getReadableDatabase().rawQuery(sqlQuery, null);
        if (cursor.moveToFirst()) {
            logger.d(TAG, "cursor getcount() = " + new StringBuilder().append(cursor.getCount()).toString());
            result = true;
            cursor.close();
        }
        return result;
    }

    public void deleteFollowedApps(List<AppInfoEntity> apps, boolean deleteBundleKeys) {
        for (AppInfoEntity entity : apps) {
            deleteFollowedApp(entity, deleteBundleKeys);
        }
    }

    public void deleteFollowedApp(AppInfoEntity app, boolean deleteBundleKeys) {
        SQLiteDatabase db = this.getWritableDatabase();
        //TODO CHECK IF BUNDLE KEYS ARE DELETED AUTOMATICALLY
//        if (deleteBundleKeys) {
//            deleteFollowedBundleKeys(app.getPackageName());
//        }
        db.delete(DBContract.FollowedAppFeed.TABLE_NAME,
                DBContract.FollowedAppFeed.COLUMN_NAME_PACKAGE_NAME + " = ?",
                new String[]{app.getPackageName()});
    }

    public List<NotificationEntity> getAllFollowedNotifications(boolean getBundleKeys) {
        List<NotificationEntity> result = new ArrayList<>();
        String sql_query = "Select * from " + DBContract.FollowedNotificationsFeed.TABLE_NAME;
        Cursor cursor = getReadableDatabase().rawQuery(sql_query, null);
        if (cursor.moveToFirst())
            do {
                NotificationEntity notificationEntity = getFollowedNotification(cursor,getBundleKeys);
                result.add(notificationEntity);
            } while (cursor.moveToNext());
        return result;
    }
    private NotificationEntity getFollowedNotification(Cursor cursor, boolean getBundleKeys)
    {
        String label =
    }

    public long addFollowedNotification(NotificationEntity entity) {
        ContentValues values = new ContentValues();
        values.put(DBContract.FollowedNotificationsFeed.COLUMN_NAME_PACKAGE_NAME, entity.getPackageName());
        values.put(DBContract.FollowedNotificationsFeed.COLUMN_NAME_SBN_ID, entity.getSbnId());
        return getWritableDatabase().insert(DBContract.FollowedNotificationsFeed.TABLE_NAME, null, values);
    }

    public int deleteFollowedNotificaiton(NotificationEntity entity) {
        return getWritableDatabase().delete(DBContract.FollowedNotificationsFeed.TABLE_NAME,
                DBContract.FollowedNotificationsFeed.COLUMN_NAME_PACKAGE_NAME + " = ? AND "
                        + DBContract.FollowedNotificationsFeed.COLUMN_NAME_SBN_ID + " = ?",
                new String[]{entity.getPackageName(), String.valueOf(entity.getSbnId())});
    }

    public long addHistoryNotification(HistoryNotificationEntity historyNotificationEntity) {
        long rowId = addNotificationHistoryEntry(historyNotificationEntity);
        historyNotificationEntity.setID(rowId);
        logger.d(TAG, "addHistoryNotification() rowId: " + rowId);
        if (rowId != -1)
            addNotificationBundleHistoryEntries(historyNotificationEntity, rowId);
        return rowId;

    }

    private long addNotificationHistoryEntry(HistoryNotificationEntity historyNotificationEntity) {
        ContentValues values = new ContentValues();
        values.put(DBContract.HistoryNotificationFeed.COLUMN_NAME_SBN_ID, historyNotificationEntity.getSbnId());
        values.put(DBContract.HistoryNotificationFeed.COLUMN_NAME_PACKAGE_NAME, historyNotificationEntity.getPackageName());
        values.put(DBContract.HistoryNotificationFeed.COLUMN_NAME_INSERTION_TIMESTAMP, historyNotificationEntity.getOccurrenceTime());
        values.put(DBContract.HistoryNotificationFeed.COLUMN_NAME_APPLICATION_LABEL, historyNotificationEntity.getApplicationLabel());
        return getWritableDatabase().insert(DBContract.HistoryNotificationFeed.TABLE_NAME, null, values);
    }

    private List<Long> addNotificationBundleHistoryEntries(HistoryNotificationEntity historyNotificationEntity, long notificationId) {
        List<Long> rowIds = new ArrayList<>();

        for (BundleKeyEntity entry : historyNotificationEntity.getBundleKeys()) {
            ContentValues values = new ContentValues();
            values.put(DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_NOTIFICATION_ID, notificationId);
            values.put(DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_BUNDLE_KEY, entry.getKey());
            values.put(DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_BUNDLE_VALUE, entry.getValue());
            values.put(DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_PACKAGE_NAME, entry.getPackageName());
            rowIds.add(getWritableDatabase().insert(DBContract.HistoryBundlesKeysFeed.TABLE_NAME, null, values));
        }
        return rowIds;
    }

    public List<Long> updateNotification(HistoryNotificationEntity historyNotificationEntity, boolean updateNotificationMessages) {
        List<Long> affectedRows = new ArrayList<>();
        affectedRows.add(updateNotificationHistoryEntry(historyNotificationEntity));
        if (updateNotificationMessages) {
            affectedRows.add(updateNotificationMessagesValues(historyNotificationEntity));
        }
        return affectedRows;
    }

    /**
     * @param historyNotificationEntity
     * @return numbers of rows affected
     */
    private long updateNotificationHistoryEntry(HistoryNotificationEntity historyNotificationEntity) {
        ContentValues values = new ContentValues();
        values.put(DBContract.HistoryNotificationFeed.COLUMN_NAME_PACKAGE_NAME, historyNotificationEntity.getPackageName());
        values.put(DBContract.HistoryNotificationFeed.COLUMN_NAME_INSERTION_TIMESTAMP, historyNotificationEntity.getOccurrenceTime());
        values.put(DBContract.HistoryNotificationFeed.COLUMN_NAME_APPLICATION_LABEL, historyNotificationEntity.getApplicationLabel());
        return getWritableDatabase().update(DBContract.HistoryNotificationFeed.TABLE_NAME,
                values,
                DBContract.HistoryNotificationFeed.COLUMN_NAME_ID + " = " + historyNotificationEntity.getID(), null);
    }

    //TODO trzeba koniecznie przetestować przed użyciem
    private long updateNotificationMessagesValues(HistoryNotificationEntity historyNotificationEntity) {
        long affectedRows = 0;
        ContentValues values = new ContentValues();
        for (BundleKeyEntity entry : historyNotificationEntity.getBundleKeys()) {
            values.put(DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_BUNDLE_VALUE, entry.getValue());
            affectedRows += getWritableDatabase().update(DBContract.HistoryBundlesKeysFeed.TABLE_NAME,
                    values,
                    DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_NOTIFICATION_ID + " = " + historyNotificationEntity.getID() + " AND " +
                            DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_BUNDLE_KEY + " = '" + entry.getKey() + "'",
                    null);
        }
        return affectedRows;
    }

    public List<HistoryNotificationEntity> getAllHistoryNotification(boolean getBundleKeys) {
        List<HistoryNotificationEntity> notifications = new ArrayList<>();
        String sql_select_all = "SELECT * FROM " + DBContract.HistoryNotificationFeed.TABLE_NAME +
                " ORDER BY " + DBContract.HistoryNotificationFeed.COLUMN_NAME_INSERTION_TIMESTAMP + " DESC ;";
        Cursor cursor = getReadableDatabase().rawQuery(sql_select_all, null);
        if (cursor.moveToFirst()) {
            do {
                notifications.add(getHistoryNotification(cursor, getBundleKeys));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return notifications;
    }

    public HistoryNotificationEntity getHistoryNotification(long sbnId, String packageName, boolean getBundleKeys) {
        HistoryNotificationEntity notification = null;
        String sql_select = "SELECT * FROM " + DBContract.HistoryNotificationFeed.TABLE_NAME + " WHERE " +
                DBContract.HistoryNotificationFeed.COLUMN_NAME_PACKAGE_NAME + " = '" + packageName + "' AND " +
                DBContract.HistoryNotificationFeed.COLUMN_NAME_SBN_ID + " = " + sbnId + ";";
        Cursor cursor = getReadableDatabase().rawQuery(sql_select, null);
        if (cursor.moveToFirst()) {
            notification = getHistoryNotification(cursor, getBundleKeys);
        }
        cursor.close();
        return notification;
    }

    public HistoryNotificationEntity getHistoryNotification(long rowId, boolean getBundleKeys) {
        HistoryNotificationEntity notification = null;
        String sql_select = "SELECT * FROM " + DBContract.HistoryNotificationFeed.TABLE_NAME + " WHERE " +
                DBContract.HistoryNotificationFeed.COLUMN_NAME_ID + " = " + rowId + ";";
        Cursor cursor = getReadableDatabase().rawQuery(sql_select, null);
        if (cursor.moveToFirst()) {
            notification = getHistoryNotification(cursor, getBundleKeys);
        } else
            logger.d(TAG, "notification = null");
        cursor.close();
        return notification;
    }

    private HistoryNotificationEntity getHistoryNotification(Cursor cursor, boolean getBundleKeys) {
        long notificationId = cursor.getLong(cursor.getColumnIndex(DBContract.HistoryNotificationFeed.COLUMN_NAME_ID));
        String packageName = cursor.getString(cursor.getColumnIndex(DBContract.HistoryNotificationFeed.COLUMN_NAME_PACKAGE_NAME));
        String applicationLabel = cursor.getString(cursor.getColumnIndex(DBContract.HistoryNotificationFeed.COLUMN_NAME_APPLICATION_LABEL));
        int sbnId = cursor.getInt(cursor.getColumnIndex(DBContract.HistoryNotificationFeed.COLUMN_NAME_SBN_ID));
        long occurrenceTime = cursor.getLong(cursor.getColumnIndex(DBContract.HistoryNotificationFeed.COLUMN_NAME_INSERTION_TIMESTAMP));

        HistoryNotificationEntity historyNotificationEntity = new HistoryNotificationEntity(sbnId, packageName, applicationLabel, occurrenceTime);
        historyNotificationEntity.setID(notificationId);
        historyNotificationEntity.setTinkerText(cursor.getString(cursor.getColumnIndex(DBContract.HistoryNotificationFeed.COLUMN_NAME_TINKER_TEXT)));
        historyNotificationEntity.setIsFollowed(isAppFollowed(historyNotificationEntity.getPackageName()));

        if (getBundleKeys)
            historyNotificationEntity.setBundleKeys(getHistoryBundleKeys(notificationId));
        return historyNotificationEntity;
    }

    public HistoryNotificationEntity getLastHistoryNotification(long rowId, boolean getBundleKeys) {
        String lastRowIdColumnName = "lastRowId";
        logger.d(TAG, "getLastHistoryNotification, newNotification.getId() = " + rowId);
        String sql_query = "SELECT " + lastRowIdColumnName + ", MIN(diff) AS minTimedifference, * " +
                "FROM (" +
                "SELECT t2.rowId AS " + lastRowIdColumnName + ", " +
                "t1." + DBContract.HistoryNotificationFeed.COLUMN_NAME_INSERTION_TIMESTAMP + " - t2." + DBContract.HistoryNotificationFeed.COLUMN_NAME_INSERTION_TIMESTAMP + " AS diff, * " +
                "FROM " +
                DBContract.HistoryNotificationFeed.TABLE_NAME + " t1 INNER JOIN " +
                DBContract.HistoryNotificationFeed.TABLE_NAME + " t2 " +
                " ON t2." + DBContract.HistoryNotificationFeed.COLUMN_NAME_INSERTION_TIMESTAMP + " < t1." + DBContract.HistoryNotificationFeed.COLUMN_NAME_INSERTION_TIMESTAMP +
                " AND t1." + DBContract.HistoryNotificationFeed.COLUMN_NAME_PACKAGE_NAME + " = t2." + DBContract.HistoryNotificationFeed.COLUMN_NAME_PACKAGE_NAME +
                " WHERE t1.rowId = " + rowId + ");";
        logger.d(TAG, sql_query);
        Cursor cursor = getReadableDatabase().rawQuery(sql_query, null);
        HistoryNotificationEntity entity = null;

        if (cursor.moveToFirst()) {
            do {
                long lastRowId = cursor.getLong(cursor.getColumnIndex(lastRowIdColumnName));
                logger.d(TAG, "lastRowId = " + lastRowId + "cursor.position() = " + cursor.getPosition());
            } while (cursor.moveToNext());
            if (cursor.moveToFirst()) {
                long lastRowId1 = cursor.getLong(cursor.getColumnIndex(lastRowIdColumnName));
                logger.d(TAG, "lastRowId = " + lastRowId1 + "cursor.position() = " + cursor.getPosition());
                entity = getHistoryNotification(lastRowId1, getBundleKeys);
            }
        } else
            logger.d(TAG, "lastNotification = null");
        cursor.close();
        return entity;
    }

    public List<BundleKeyEntity> getHistoryBundleKeys(long notificationId) {
        String sql_select_id = "SELECT * FROM " + DBContract.HistoryBundlesKeysFeed.TABLE_NAME +
                " INNER JOIN " + DBContract.HistoryNotificationFeed.TABLE_NAME +
                " ON " + DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_NOTIFICATION_ID + " = " + DBContract.HistoryNotificationFeed.COLUMN_NAME_ID +
                " WHERE " + DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_NOTIFICATION_ID + " = " + notificationId + ";";
        Cursor cursor = getReadableDatabase().rawQuery(sql_select_id, null);

        List<BundleKeyEntity> bundlekeys = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                BundleKeyEntity entity = getHistoryBundleKey(cursor);
                bundlekeys.add(entity);
            } while (cursor.moveToNext());
        }
        return bundlekeys;
    }

    private BundleKeyEntity getHistoryBundleKey(Cursor cursor) {
        String key = cursor.getString(cursor.getColumnIndex(DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_BUNDLE_KEY));
        String value = cursor.getString(cursor.getColumnIndex(DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_BUNDLE_VALUE));
        String packageName = cursor.getString(cursor.getColumnIndex(DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_PACKAGE_NAME));
        int sbnId = cursor.getInt(cursor.getColumnIndex(DBContract.HistoryNotificationFeed.COLUMN_NAME_SBN_ID));

        BundleKeyEntity entity = new BundleKeyEntity(packageName,sbnId, key, value);
        boolean isFollowed = isFollowed(entity);
        if (isFollowed) {
            boolean isShowAlways = isShowAlways(entity);
            entity.setIsShowAlways(isShowAlways);
            entity.setPriority(getFollowedBundleKeyPriority(entity));
        }
        entity.setIsFollowed(isFollowed);
        return entity;
    }

    public List<BundleKeyEntity> getSortedFollowedBundleKeys(NotificationEntity notificationEntity) {
        List<BundleKeyEntity> result = null;
        if(notificationEntity != null)
        {
            result = getSortedFollowedBundleKeys(notificationEntity.getPackageName(),notificationEntity.getSbnId());
        }
        return result;
    }
    public List<BundleKeyEntity> getSortedFollowedBundleKeys(AppInfoEntity appInfoEntity)
    {
        List<BundleKeyEntity> result = null;
        if(appInfoEntity != null)
        {
            result = getSortedFollowedBundleKeys(appInfoEntity.getPackageName(),-1);
        }
        return result;
    }

    public List<BundleKeyEntity> getSortedFollowedBundleKeys(String packageName, long sbnId) {
        List<BundleKeyEntity> bundleKeys = new ArrayList<>();

        String sql_select_bundlekeys = "SELECT * FROM " + DBContract.FollowedBundleKeysFeed.TABLE_NAME +
                " WHERE " + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = '" + packageName + "'" +
                " AND " + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_SBN_ID + " = " + sbnId +
                " ORDER BY " + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_PRIORITY + " ASC;";
        logger.d(TAG, sql_select_bundlekeys);

        Cursor cursor = getReadableDatabase().rawQuery(sql_select_bundlekeys, null);
        if (cursor.moveToFirst()) {
            do {
                BundleKeyEntity entity = getFollowedBundleKey(cursor);
                bundleKeys.add(entity);


            } while (cursor.moveToNext());
        }
        cursor.close();
        return bundleKeys;
    }

    private BundleKeyEntity getFollowedBundleKey(Cursor cursor) {
        String bundleKey = cursor.getString(cursor.getColumnIndex(DBContract.FollowedBundleKeysFeed.COLUMN_NAME_BUNDLE_KEY));
        int priority = cursor.getInt(cursor.getColumnIndex(DBContract.FollowedBundleKeysFeed.COLUMN_NAME_PRIORITY));
        String packageName = cursor.getString(cursor.getColumnIndex(DBContract.FollowedBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME));
        int showAlwaysINT = cursor.getInt(cursor.getColumnIndex(DBContract.FollowedBundleKeysFeed.COLUMN_NAME_SHOW_ALWAYS));
        int sbnId = cursor.getInt(cursor.getColumnIndex(DBContract.FollowedBundleKeysFeed.COLUMN_NAME_SBN_ID));
        BundleKeyEntity entity = new BundleKeyEntity(packageName,sbnId, bundleKey, priority, intToBoolean(showAlwaysINT));
        logger.d(TAG, "bundle Key: " + entity.getKey() + "\tPriority: " + entity.getPriority() + "\t showAways: " + entity.isShowAlways());
        return entity;
    }

    public void addUpdateFollowedBundleKey(BundleKeyEntity bundleKeyEntity) {

        String sql_insert_or_update = "INSERT OR REPLACE INTO " + DBContract.FollowedBundleKeysFeed.TABLE_NAME + " ("
                + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME +
                ", " + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_SBN_ID +
                ", " + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_BUNDLE_KEY +
                ", " + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_PRIORITY +
                ", " + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_SHOW_ALWAYS + ")" +
                "  VALUES ('" + bundleKeyEntity.getPackageName() + "'" +
                ", " + bundleKeyEntity.getSbnId() +
                ", '" + bundleKeyEntity.getKey() + "'" +
                ", " + bundleKeyEntity.getPriority() +
                ", " + booleanToInt(bundleKeyEntity.isShowAlways()) + ");";
        logger.d(TAG, sql_insert_or_update);
        getWritableDatabase().execSQL(sql_insert_or_update);
    }

    public boolean isShowAlways(BundleKeyEntity entity) {
        String sql_query = "SELECT * FROM " + DBContract.FollowedBundleKeysFeed.TABLE_NAME +
                " WHERE " + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = '" + entity.getPackageName() + "'" +
                " AND " + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_SBN_ID + " = " + entity.getSbnId() +
                " AND " + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_BUNDLE_KEY + " = '" + entity.getKey() + "' " +
                " AND " + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_SHOW_ALWAYS + " = " + booleanToInt(true) + ";";
        Cursor cursor = getReadableDatabase().rawQuery(sql_query, null);
        boolean isShownAlways = cursor.moveToFirst();
        cursor.close();
        return isShownAlways;
    }

    public boolean isFollowed(BundleKeyEntity entity) {
        String sql_query = "SELECT * FROM " + DBContract.FollowedBundleKeysFeed.TABLE_NAME +
                " WHERE " + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = '" + entity.getPackageName() + "'" +
                " AND " + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_SBN_ID + " = " + entity.getSbnId() +
                " AND " + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_BUNDLE_KEY + " = '" + entity.getKey() + "';";
        Cursor cursor = getReadableDatabase().rawQuery(sql_query, null);
        boolean isFollowed = cursor.moveToFirst();
        cursor.close();
        return isFollowed;
    }

    public int getFollowedBundleKeyPriority(BundleKeyEntity entity) {
        int result = 0;
        String sql_query = "SELECT " + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_PRIORITY + " FROM " + DBContract.FollowedBundleKeysFeed.TABLE_NAME +
                " WHERE " + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = '" + entity.getPackageName() + "'" +
                " WHERE " + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_SBN_ID + " = " + entity.getSbnId() +
                " AND " + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_BUNDLE_KEY + " = '" + entity.getKey() + "';";
        Cursor cursor = getReadableDatabase().rawQuery(sql_query, null);
        if (cursor.moveToFirst())
            result = cursor.getInt(cursor.getColumnIndex(DBContract.FollowedBundleKeysFeed.COLUMN_NAME_PRIORITY));
        cursor.close();
        return result;
    }

    public void deleteFollowedBundleKeys(String packageName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DBContract.FollowedBundleKeysFeed.TABLE_NAME,
                DBContract.FollowedBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = ?" ,
                new String[]{packageName});
    }

    public void deleteFollowedBundleKey(BundleKeyEntity entity) {
        String sql_query = "DELETE FROM " + DBContract.FollowedBundleKeysFeed.TABLE_NAME +
                " WHERE " + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = '" + entity.getPackageName() + "'" +
                " AND " + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_SBN_ID + " = " + entity.getSbnId() +
                " AND " + DBContract.FollowedBundleKeysFeed.COLUMN_NAME_BUNDLE_KEY + " = '" + entity.getKey() + "';";
        getWritableDatabase().execSQL(sql_query);
    }

    public int booleanToInt(boolean bool) {
        if (bool)
            return 1;
        else
            return 0;
    }

    public boolean intToBoolean(int i) {
        return i == 1;
    }


}
