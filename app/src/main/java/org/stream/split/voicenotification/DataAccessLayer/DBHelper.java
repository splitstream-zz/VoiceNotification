package org.stream.split.voicenotification.DataAccessLayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.stream.split.voicenotification.Enities.AppBundleKeyEntity;
import org.stream.split.voicenotification.Enities.AppInfoEntity;
import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Enities.HistoryBundleKeyEntity;
import org.stream.split.voicenotification.Enities.HistoryNotificationEntity;
import org.stream.split.voicenotification.Enities.NotificationBundleKeyEntity;
import org.stream.split.voicenotification.Enities.NotificationEntity;
import org.stream.split.voicenotification.Enums.NotificationPolicy;
import org.stream.split.voicenotification.Logging.BaseLogger;

import java.util.ArrayList;
import java.util.Collections;
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
        db.execSQL(DBContract.NotificationBundleKeysFeed.SQL_CREATE_TABLE);
        db.execSQL(DBContract.HistoryNotificationFeed.SQL_CREATE_TABLE);
        db.execSQL(DBContract.HistoryNotificationFeed.SQL_INSERTION_TRIGGER);
        db.execSQL(DBContract.AppBundleKeysFeed.SQL_CREATE_TABLE);
        db.execSQL(DBContract.HistoryBundlesKeysFeed.SQL_CREATE_TABLE);
        db.execSQL(DBContract.SettingFeed.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(DBContract.NotificationBundleKeysFeed.SQL_DELETE_TABLE);
        db.execSQL(DBContract.HistoryNotificationFeed.SQL_DELETE_TABLE);
        db.execSQL(DBContract.AppBundleKeysFeed.SQL_DELETE_TABLE);
        db.execSQL(DBContract.FollowedNotificationsFeed.SQL_DELETE_TABLE);
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

    public List<AppInfoEntity> getAllFollowedApps(boolean getNotifications, boolean getBundleKeys) {
        List<AppInfoEntity> apps = new ArrayList<>();

        Cursor cursor = this.getReadableDatabase().query(DBContract.FollowedAppFeed.TABLE_NAME,null,null,null,null,null,null);

        if (cursor.moveToFirst()) {
            do {
                AppInfoEntity entity = getFollowedApp(cursor,getNotifications, getBundleKeys);
                apps.add(entity);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return apps;
    }

    public AppInfoEntity getFollowedApp(String packageName, boolean getNotifications, boolean getBundleKeys) {

        String sql_where = DBContract.FollowedAppFeed.COLUMN_NAME_PACKAGE_NAME + " = ?";
        Cursor cursor = getReadableDatabase().query(DBContract.FollowedAppFeed.TABLE_NAME,
                null,
                sql_where,
                new String[]{packageName},
                null, null, null);

        AppInfoEntity entity = null;
        if (cursor.moveToFirst())
            entity = getFollowedApp(cursor,getNotifications, getBundleKeys);
        cursor.close();
        return entity;
    }

    private AppInfoEntity getFollowedApp(Cursor cursor,boolean getNotifications, boolean getBundleKeys) {
        String packageName = cursor.getString(cursor.getColumnIndex(DBContract.FollowedAppFeed.COLUMN_NAME_PACKAGE_NAME));
        String label = cursor.getString(cursor.getColumnIndex(DBContract.FollowedAppFeed.COLUMN_NAME_APPLICATION_LABEL));
        AppInfoEntity app = new AppInfoEntity(packageName, label);
        app.setIsFollowed(true);

        if(getNotifications)
            app.setNotifications(getFollowedNotification(packageName, getBundleKeys));
        if (getBundleKeys)
            app.getBundleKeyList().set(getBundleKeys(app));

        return app;
    }


    public void updateOrInsert(List<AppInfoEntity> apps, boolean addNotification, boolean addBundleKeys) {
        for (AppInfoEntity app : apps) {
            updateOrInsert(app, addNotification, addBundleKeys);
        }
    }

    /**
     * inserts AppInfoEntity object into database table FollowedAppFeed
     *
     * @param app
     * @return row id of newely inserted AppInfoEntity. -1 if there was error.
     */
    public void updateOrInsert(AppInfoEntity app, boolean addNotification, boolean addBundleKeys) {
        ContentValues values = new ContentValues();
        values.put(DBContract.FollowedAppFeed.COLUMN_NAME_PACKAGE_NAME, app.getPackageName());
        values.put(DBContract.FollowedAppFeed.COLUMN_NAME_APPLICATION_LABEL, app.getApplicationLabel());

        String sql_where = DBContract.FollowedAppFeed.COLUMN_NAME_PACKAGE_NAME + " = ?";

        int rowsAffected = getWritableDatabase().update(DBContract.FollowedAppFeed.TABLE_NAME,
                values,
                sql_where,
                new String[]{app.getPackageName()});
        if(rowsAffected == 0)
            getWritableDatabase().insert(DBContract.FollowedAppFeed.TABLE_NAME, null, values);

        app.setIsModified(false);

        if(addNotification)
        {
            updateOrInsert(app.getNotifications(),addBundleKeys);
        }
        if(addBundleKeys)
        {
            List<AppBundleKeyEntity> bundleKeyEntities = app.getBundleKeyList().get();
            for(AppBundleKeyEntity bundleKeyEntity:bundleKeyEntities)
            {
                updateOrInsert(bundleKeyEntity);
            }
        }
    }

    public boolean isFollowed(String PackageName) {

        String sql_where = DBContract.FollowedAppFeed.COLUMN_NAME_PACKAGE_NAME + " = ?";
        boolean result = false;

        Cursor cursor = this.getReadableDatabase().query(DBContract.FollowedAppFeed.TABLE_NAME,
                null,
                sql_where,
                new String[]{PackageName},
                null, null, null);

        if (cursor.moveToFirst()) {
            logger.d(TAG, "cursor getcount() = " + new StringBuilder().append(cursor.getCount()).toString());
            result = true;
            cursor.close();
        }
        return result;
    }
    //todo deletebundlekeys might be not needed
    public void delete(List<AppInfoEntity> apps) {
        for (AppInfoEntity entity : apps) {
            delete(entity);
        }
    }

    public void delete(AppInfoEntity app) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(DBContract.FollowedAppFeed.TABLE_NAME,
                DBContract.FollowedAppFeed.COLUMN_NAME_PACKAGE_NAME + " = ?",
                new String[]{app.getPackageName()});

        app.setIsModified(false);
    }

    public boolean isFollowed(NotificationEntity entity)
    {
        return getFollowedNotification(entity.getPackageName(),entity.getSbnId(),false) != null;
    }

    public List<NotificationEntity> getAllFollowedNotifications(boolean getBundleKeys) {
        List<NotificationEntity> result = new ArrayList<>();
        String sql_query = "Select * from " + DBContract.FollowedNotificationsFeed.TABLE_NAME;
        Cursor cursor = getReadableDatabase().rawQuery(sql_query, null);
        if (cursor.moveToFirst())
            do {
                NotificationEntity followedNotificationEntity = getFollowedNotification(cursor,getBundleKeys);
                result.add(followedNotificationEntity);
            } while (cursor.moveToNext());
        return result;
    }
//leave it be - its used to get notification in notificationAdapter (generic invoking method - refactoring do not work)
    public NotificationEntity getNotification(NotificationEntity notification, boolean getBundleKeys)
    {
        notification =getFollowedNotification(notification.getPackageName(), notification.getSbnId(), getBundleKeys);
        return notification;
    }
    //leave it be - its used to get notification in notificationAdapter (generic invoking method - refactoring do not work)
    public HistoryNotificationEntity getNotification(HistoryNotificationEntity notification, boolean getBundleKeys)
    {
        notification = getHistoryNotification(notification.getID(),getBundleKeys);
        return notification;
    }

    public List<NotificationEntity> getFollowedNotification(String packageName, boolean getBundleKeys)
    {
        List<NotificationEntity> entities = new ArrayList<>();

        String sql_query = DBContract.FollowedNotificationsFeed.COLUMN_NAME_PACKAGE_NAME + " = ?";

        Cursor cursor = getReadableDatabase().query(DBContract.FollowedNotificationsFeed.TABLE_NAME,
                null,
                sql_query,
                new String[]{packageName},
                null, null, null);

        if(cursor.moveToFirst())
            do {
                NotificationEntity entity = getFollowedNotification(cursor, getBundleKeys);
                entities.add(entity);
            }while(cursor.moveToNext());

        return entities;
    }

    public NotificationEntity getFollowedNotification(String packageName, int sbnId, boolean getBundleKeys)
    {
        NotificationEntity entity = null;
        String sql_query = DBContract.FollowedNotificationsFeed.COLUMN_NAME_PACKAGE_NAME + " = ? AND " +
                DBContract.FollowedNotificationsFeed.COLUMN_NAME_SBN_ID + " = ?";
        Cursor cursor = getReadableDatabase().query(DBContract.FollowedNotificationsFeed.TABLE_NAME,
                null,
                sql_query,
                new String[]{packageName, String.valueOf(sbnId)},
                null, null, null);
        if(cursor.moveToFirst())
            entity = getFollowedNotification(cursor,getBundleKeys);
        return entity;
    }
    private NotificationEntity getFollowedNotification(Cursor cursor, boolean getBundleKeys)
    {
        String packageName = cursor.getString(cursor.getColumnIndex(DBContract.FollowedNotificationsFeed.COLUMN_NAME_PACKAGE_NAME));
        int sbnId = cursor.getInt(cursor.getColumnIndex(DBContract.FollowedNotificationsFeed.COLUMN_NAME_SBN_ID));
        String policyName = cursor.getString(cursor.getColumnIndex(DBContract.FollowedNotificationsFeed.COLUMN_NAME_POLICY));
        NotificationPolicy policy = NotificationPolicy.valueOf(policyName);
        NotificationEntity entity = new NotificationEntity(sbnId,packageName, policy);
        entity.setIsFollowed(true);
        if(getBundleKeys)
            entity.getBundleKeyList().set(getBundleKeys(entity));
        return entity;
    }

    public void updateOrInsert(List<NotificationEntity> entities, boolean addBundleKeys)
    {
        for(NotificationEntity entity:entities)
        {
            updateOrInsert(entity,addBundleKeys);
        }
    }
    public void updateOrInsert(NotificationEntity entity, boolean addBundleKeys) {
        ContentValues values = new ContentValues();
        values.put(DBContract.FollowedNotificationsFeed.COLUMN_NAME_PACKAGE_NAME, entity.getPackageName());
        values.put(DBContract.FollowedNotificationsFeed.COLUMN_NAME_SBN_ID, entity.getSbnId());
        values.put(DBContract.FollowedNotificationsFeed.COLUMN_NAME_POLICY, entity.getPolicy().name());

        String sql_where = DBContract.FollowedNotificationsFeed.COLUMN_NAME_PACKAGE_NAME + " = ? AND "
                + DBContract.FollowedNotificationsFeed.COLUMN_NAME_SBN_ID + " = ?";

        int rowsAffected = getWritableDatabase().update(DBContract.FollowedNotificationsFeed.TABLE_NAME,
                values,
                sql_where,
                new String[]{entity.getPackageName(),String.valueOf(entity.getSbnId())});
        if(rowsAffected == 0)
            getWritableDatabase().insert(DBContract.FollowedNotificationsFeed.TABLE_NAME, null, values);

        entity.setIsModified(false);

        if(addBundleKeys)
        {
            List<NotificationBundleKeyEntity> bundleKeyEntities = entity.getBundleKeyList().get();
            for(NotificationBundleKeyEntity bundleKeyEntity:bundleKeyEntities)
            {
                        updateOrInsert(bundleKeyEntity);
            }
        }

    }

    public int delete(NotificationEntity entity) {

        String sql_where = DBContract.FollowedNotificationsFeed.COLUMN_NAME_PACKAGE_NAME + " = ? AND "
                + DBContract.FollowedNotificationsFeed.COLUMN_NAME_SBN_ID + " = ?";

        entity.setIsModified(false);

        return getWritableDatabase().delete(DBContract.FollowedNotificationsFeed.TABLE_NAME,
                sql_where,
                new String[]{entity.getPackageName(), String.valueOf(entity.getSbnId())});
    }

    /**
     * retirve sorted by priority bundle keys belongs to appinfoentity
     * @param appInfoEntity
     * @return
     */
    public List<AppBundleKeyEntity> getBundleKeys(AppInfoEntity appInfoEntity)
    {
        List<AppBundleKeyEntity> result = null;
        if(appInfoEntity != null)
        {
            result = getBundleKeys(appInfoEntity.getPackageName());
        }
        appInfoEntity.getBundleKeyList().set(result);
        return result;
    }

    public List<HistoryBundleKeyEntity> getBundleKeys(HistoryNotificationEntity entity) {

        String sql_where = DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_NOTIFICATION_ID + " = ?";

        Cursor cursor = getReadableDatabase().query(DBContract.HistoryBundlesKeysFeed.TABLE_NAME,
                null,
                sql_where,
                new String[]{String.valueOf(entity.getID())},
                null,null,null);

        List<HistoryBundleKeyEntity> bundlekeys = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                HistoryBundleKeyEntity bundleKey = getHistoryBundleKey(cursor);
                bundlekeys.add(bundleKey);
            } while (cursor.moveToNext());
        }
        entity.getBundleKeyList().set(bundlekeys);
        return bundlekeys;
    }
    /**
     * retirve sorted by priority bundle keys belongs to notificationEntity
     * @param followedNotificationEntity
     * @return
     */
    public List<NotificationBundleKeyEntity> getBundleKeys(NotificationEntity followedNotificationEntity) {
        List<NotificationBundleKeyEntity> bundleKeys = new ArrayList<>();

        String sql_where = DBContract.NotificationBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME +" = ? AND "+
                DBContract.NotificationBundleKeysFeed.COLUMN_NAME_SBN_ID + " = ?";
        String sql_orderBy = DBContract.NotificationBundleKeysFeed.COLUMN_NAME_PRIORITY + " DESC";

        Cursor cursor = getReadableDatabase().query(DBContract.NotificationBundleKeysFeed.TABLE_NAME,
                null,
                sql_where,
                new String[]{followedNotificationEntity.getPackageName(),String.valueOf(followedNotificationEntity.getSbnId())},
                null,null,
                sql_orderBy);

        if (cursor.moveToFirst()) {
            do {
                NotificationBundleKeyEntity entity = getNotificationBundleKey(cursor);
                bundleKeys.add(entity);
            } while (cursor.moveToNext());
        }
        followedNotificationEntity.getBundleKeyList().set(bundleKeys);
        cursor.close();
        return bundleKeys;
    }

    private List<AppBundleKeyEntity> getBundleKeys(String packageName) {
        List<AppBundleKeyEntity> bundleKeys = new ArrayList<>();


        String sql_where = DBContract.AppBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME +" = ?";
        String sql_orderBy = DBContract.AppBundleKeysFeed.COLUMN_NAME_PRIORITY + " DESC";

        Cursor cursor = getReadableDatabase().query(DBContract.AppBundleKeysFeed.TABLE_NAME,
                null,
                sql_where,
                new String[]{packageName},
                null, null,
                sql_orderBy);

        if (cursor.moveToFirst()) {
            do {
                AppBundleKeyEntity entity = getAppBundleKey(cursor);
                bundleKeys.add(entity);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bundleKeys;
    }

    private NotificationBundleKeyEntity getNotificationBundleKey(Cursor cursor) {
        String bundleKey = cursor.getString(cursor.getColumnIndex(DBContract.NotificationBundleKeysFeed.COLUMN_NAME_BUNDLE_KEY));
        int priority = cursor.getInt(cursor.getColumnIndex(DBContract.NotificationBundleKeysFeed.COLUMN_NAME_PRIORITY));
        String packageName = cursor.getString(cursor.getColumnIndex(DBContract.NotificationBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME));
        int showAlwaysINT = cursor.getInt(cursor.getColumnIndex(DBContract.NotificationBundleKeysFeed.COLUMN_NAME_SHOW_ALWAYS));
        int sbnId = cursor.getInt(cursor.getColumnIndex(DBContract.NotificationBundleKeysFeed.COLUMN_NAME_SBN_ID));
        NotificationBundleKeyEntity entity = new NotificationBundleKeyEntity(packageName,sbnId, bundleKey, priority, intToBoolean(showAlwaysINT));
        logger.d(TAG, "bundle Key: " + entity.getKey() + "\tPriority: " + entity.getPriority() + "\t showAways: " + entity.isShowAlways());
        return entity;
    }


    private AppBundleKeyEntity getAppBundleKey(Cursor cursor) {
        String bundleKey = cursor.getString(cursor.getColumnIndex(DBContract.AppBundleKeysFeed.COLUMN_NAME_BUNDLE_KEY));
        int priority = cursor.getInt(cursor.getColumnIndex(DBContract.AppBundleKeysFeed.COLUMN_NAME_PRIORITY));
        String packageName = cursor.getString(cursor.getColumnIndex(DBContract.AppBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME));
        int showAlwaysINT = cursor.getInt(cursor.getColumnIndex(DBContract.AppBundleKeysFeed.COLUMN_NAME_SHOW_ALWAYS));
        AppBundleKeyEntity entity = new AppBundleKeyEntity(packageName, bundleKey, priority, intToBoolean(showAlwaysINT));
        logger.d(TAG, "bundle Key: " + entity.getKey() + "\tPriority: " + entity.getPriority() + "\t showAways: " + entity.isShowAlways());
        return entity;
    }

    public void updateOrInsert(AppBundleKeyEntity appBundleKeyEntity) {

        if (appBundleKeyEntity.isFollowed()) {
            ContentValues values = new ContentValues();
            values.put(DBContract.AppBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME, appBundleKeyEntity.getPackageName());
            values.put(DBContract.AppBundleKeysFeed.COLUMN_NAME_BUNDLE_KEY, appBundleKeyEntity.getKey());
            values.put(DBContract.AppBundleKeysFeed.COLUMN_NAME_PRIORITY, appBundleKeyEntity.getPriority());
            values.put(DBContract.AppBundleKeysFeed.COLUMN_NAME_SHOW_ALWAYS, appBundleKeyEntity.isShowAlways());

            String sql_where_clausle = DBContract.AppBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = ? AND " +
                    DBContract.AppBundleKeysFeed.COLUMN_NAME_BUNDLE_KEY + " = ?";

            SQLiteDatabase db = getWritableDatabase();
            int rowsAffected = db.update(DBContract.AppBundleKeysFeed.TABLE_NAME,
                    values,
                    sql_where_clausle,
                    new String[]{appBundleKeyEntity.getPackageName(), appBundleKeyEntity.getKey()});
            if (rowsAffected == 0) {
                db.insert(DBContract.AppBundleKeysFeed.TABLE_NAME, null, values);
            }
        } else
            delete(appBundleKeyEntity);

        appBundleKeyEntity.setIsModified(false);
    }

    public void updateOrInsert(NotificationBundleKeyEntity notificationBundleKeyEntity) {

        if(notificationBundleKeyEntity.isFollowed()) {
            ContentValues values = new ContentValues();
            values.put(DBContract.NotificationBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME, notificationBundleKeyEntity.getPackageName());
            values.put(DBContract.NotificationBundleKeysFeed.COLUMN_NAME_BUNDLE_KEY, notificationBundleKeyEntity.getKey());
            values.put(DBContract.NotificationBundleKeysFeed.COLUMN_NAME_SBN_ID, notificationBundleKeyEntity.getSbnId());
            values.put(DBContract.NotificationBundleKeysFeed.COLUMN_NAME_PRIORITY, notificationBundleKeyEntity.getPriority());
            values.put(DBContract.NotificationBundleKeysFeed.COLUMN_NAME_SHOW_ALWAYS, notificationBundleKeyEntity.isShowAlways());
            String sql_where_clausle = DBContract.NotificationBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = ? AND " +
                    DBContract.NotificationBundleKeysFeed.COLUMN_NAME_BUNDLE_KEY + " = ?";
            SQLiteDatabase db = getWritableDatabase();
            int rowsAffected = db.update(DBContract.NotificationBundleKeysFeed.TABLE_NAME,
                    values,
                    sql_where_clausle,
                    new String[]{notificationBundleKeyEntity.getPackageName(), notificationBundleKeyEntity.getKey()});
            if (rowsAffected == 0) {
                db.insert(DBContract.NotificationBundleKeysFeed.TABLE_NAME, null, values);
            }
        }
        else
            delete(notificationBundleKeyEntity);

        notificationBundleKeyEntity.setIsModified(false);
    }


    public boolean isShowAlways(AppBundleKeyEntity entity) {
        String sql_query = "SELECT * FROM " + DBContract.AppBundleKeysFeed.TABLE_NAME +
                " WHERE " + DBContract.AppBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = '" + entity.getPackageName() + "'" +
                " AND " + DBContract.AppBundleKeysFeed.COLUMN_NAME_BUNDLE_KEY + " = '" + entity.getKey() + "' " +
                " AND " + DBContract.AppBundleKeysFeed.COLUMN_NAME_SHOW_ALWAYS + " = " + booleanToInt(true) + ";";
        Cursor cursor = getReadableDatabase().rawQuery(sql_query, null);
        boolean isShownAlways = cursor.moveToFirst();
        cursor.close();
        return isShownAlways;
    }

    public boolean isShowAlways(NotificationBundleKeyEntity entity) {
        String sql_query = "SELECT * FROM " + DBContract.NotificationBundleKeysFeed.TABLE_NAME +
                " WHERE " + DBContract.NotificationBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = '" + entity.getPackageName() + "'" +
                " AND " + DBContract.NotificationBundleKeysFeed.COLUMN_NAME_SBN_ID + " = " + entity.getSbnId() +
                " AND " + DBContract.NotificationBundleKeysFeed.COLUMN_NAME_BUNDLE_KEY + " = '" + entity.getKey() + "' " +
                " AND " + DBContract.NotificationBundleKeysFeed.COLUMN_NAME_SHOW_ALWAYS + " = " + booleanToInt(true) + ";";
        Cursor cursor = getReadableDatabase().rawQuery(sql_query, null);
        boolean isShownAlways = cursor.moveToFirst();
        cursor.close();
        return isShownAlways;
    }

    public boolean isFollowed(AppBundleKeyEntity entity) {
        String sql_query = "SELECT * FROM " + DBContract.AppBundleKeysFeed.TABLE_NAME +
                " WHERE " + DBContract.AppBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = '" + entity.getPackageName() + "'" +
                " AND " + DBContract.AppBundleKeysFeed.COLUMN_NAME_BUNDLE_KEY + " = '" + entity.getKey() + "';";
        Cursor cursor = getReadableDatabase().rawQuery(sql_query, null);
        boolean isFollowed = cursor.moveToFirst();
        cursor.close();
        return isFollowed;
    }
    public boolean isFollowed(NotificationBundleKeyEntity entity) {
        String sql_query = "SELECT * FROM " + DBContract.NotificationBundleKeysFeed.TABLE_NAME +
                " WHERE " + DBContract.NotificationBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = '" + entity.getPackageName() + "'" +
                " AND " + DBContract.NotificationBundleKeysFeed.COLUMN_NAME_SBN_ID + " = " + entity.getSbnId() +
                " AND " + DBContract.NotificationBundleKeysFeed.COLUMN_NAME_BUNDLE_KEY + " = '" + entity.getKey() + "';";
        Cursor cursor = getReadableDatabase().rawQuery(sql_query, null);
        boolean isFollowed = cursor.moveToFirst();
        cursor.close();
        return isFollowed;
    }

    public int getBundleKeyPriority(AppBundleKeyEntity entity) {
        int result = 0;
        String sql_query = "SELECT " + DBContract.AppBundleKeysFeed.COLUMN_NAME_PRIORITY + " FROM " + DBContract.AppBundleKeysFeed.TABLE_NAME +
                " WHERE " + DBContract.AppBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = '" + entity.getPackageName() + "'" +
                " AND " + DBContract.AppBundleKeysFeed.COLUMN_NAME_BUNDLE_KEY + " = '" + entity.getKey() + "';";
        Cursor cursor = getReadableDatabase().rawQuery(sql_query, null);
        if (cursor.moveToFirst())
            result = cursor.getInt(cursor.getColumnIndex(DBContract.AppBundleKeysFeed.COLUMN_NAME_PRIORITY));
        cursor.close();
        return result;
    }

    public int getBundleKeyPriority(NotificationBundleKeyEntity entity) {
        int result = 0;

        String sql_where = DBContract.NotificationBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = ? AND " +
                DBContract.NotificationBundleKeysFeed.COLUMN_NAME_SBN_ID + " = ? AND " +
                DBContract.NotificationBundleKeysFeed.COLUMN_NAME_BUNDLE_KEY + " = ?";

        Cursor cursor = getReadableDatabase().query(DBContract.NotificationBundleKeysFeed.TABLE_NAME,
                null,
                sql_where,
                new String []{entity.getPackageName(), String.valueOf(entity.getSbnId()), entity.getKey()},
                null,null,null);

        if (cursor.moveToFirst())
            result = cursor.getInt(cursor.getColumnIndex(DBContract.AppBundleKeysFeed.COLUMN_NAME_PRIORITY));

        cursor.close();
        return result;
    }

//    public void deleteFollowedBundleKeys(String packageName) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete(DBContract.AppBundleKeysFeed.TABLE_NAME,
//                DBContract.AppBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = ?",
//                new String[]{packageName});
//    }

    public void delete(AppBundleKeyEntity entity) {
        String sql_where =  DBContract.AppBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = ? AND " +
                DBContract.AppBundleKeysFeed.COLUMN_NAME_BUNDLE_KEY + " = ?";
        getWritableDatabase().delete(DBContract.AppBundleKeysFeed.TABLE_NAME, sql_where,
                new String[]{entity.getPackageName(), entity.getKey()});
    }
    public void delete(NotificationBundleKeyEntity entity) {
        String sql_where =  DBContract.NotificationBundleKeysFeed.COLUMN_NAME_PACKAGE_NAME + " = ? AND " +
                DBContract.NotificationBundleKeysFeed.COLUMN_NAME_BUNDLE_KEY + " = ?";
        getWritableDatabase().delete(DBContract.NotificationBundleKeysFeed.TABLE_NAME, sql_where,
                new String[]{entity.getPackageName(), entity.getKey()});
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
        return getWritableDatabase().insert(DBContract.HistoryNotificationFeed.TABLE_NAME, null, values);
    }

    private List<Long> addNotificationBundleHistoryEntries(HistoryNotificationEntity historyNotificationEntity, long notificationId) {
        List<Long> rowIds = new ArrayList<>();

        for (HistoryBundleKeyEntity entry : historyNotificationEntity.getBundleKeyList().get()) {
            ContentValues values = new ContentValues();
            values.put(DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_NOTIFICATION_ID, notificationId);
            values.put(DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_BUNDLE_KEY, entry.getKey());
            values.put(DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_BUNDLE_VALUE, entry.getValue());
            values.put(DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_PACKAGE_NAME, entry.getPackageName());
            values.put(DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_SBN_ID, entry.getSbnId());
            rowIds.add(getWritableDatabase().insert(DBContract.HistoryBundlesKeysFeed.TABLE_NAME, null, values));
        }
        return rowIds;
    }

    public List<HistoryNotificationEntity> getAllHistoryNotification(boolean getBundleKeys) {
        List<HistoryNotificationEntity> notifications = new ArrayList<>();
        String sql_orderBy = DBContract.HistoryNotificationFeed.COLUMN_NAME_INSERTION_TIMESTAMP + " DESC";

        Cursor cursor = getReadableDatabase().query(DBContract.HistoryNotificationFeed.TABLE_NAME,
                null,null,null,null,null,
                sql_orderBy);

        if (cursor.moveToFirst()) {
            do {
                notifications.add(getHistoryNotification(cursor, getBundleKeys));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return notifications;
    }

    public HistoryNotificationEntity getHistoryNotification(int id, boolean getBundleKeys) {
        HistoryNotificationEntity notification = null;
        String sql_where = DBContract.HistoryNotificationFeed.COLUMN_NAME_ID + " = ?";

        Cursor cursor = getReadableDatabase().query(DBContract.HistoryNotificationFeed.TABLE_NAME,
                null,
                sql_where,
                new String[]{String.valueOf(id)},
                null,null,null
                );

        if (cursor.moveToFirst()) {
            notification = getHistoryNotification(cursor, getBundleKeys);
        }
        cursor.close();
        return notification;
    }

    public HistoryNotificationEntity getHistoryNotification(long rowId, boolean getBundleKeys) {

        HistoryNotificationEntity notification = null;

        String sql_where = DBContract.HistoryNotificationFeed.COLUMN_NAME_ID + " = ?";

        Cursor cursor = getReadableDatabase().query(DBContract.HistoryNotificationFeed.TABLE_NAME,
                null,
                sql_where,
                new String[]{String.valueOf(rowId)},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            notification = getHistoryNotification(cursor, getBundleKeys);
        }
        cursor.close();
        return notification;
    }

    private HistoryNotificationEntity getHistoryNotification(Cursor cursor, boolean getBundleKeys) {
        long notificationId = cursor.getLong(cursor.getColumnIndex(DBContract.HistoryNotificationFeed.COLUMN_NAME_ID));
        String packageName = cursor.getString(cursor.getColumnIndex(DBContract.HistoryNotificationFeed.COLUMN_NAME_PACKAGE_NAME));
        int sbnId = cursor.getInt(cursor.getColumnIndex(DBContract.HistoryNotificationFeed.COLUMN_NAME_SBN_ID));
        long occurrenceTime = cursor.getLong(cursor.getColumnIndex(DBContract.HistoryNotificationFeed.COLUMN_NAME_INSERTION_TIMESTAMP));

        HistoryNotificationEntity historyNotificationEntity = new HistoryNotificationEntity(packageName,sbnId, occurrenceTime);
        historyNotificationEntity.setID(notificationId);
        historyNotificationEntity.setTinkerText(cursor.getString(cursor.getColumnIndex(DBContract.HistoryNotificationFeed.COLUMN_NAME_TINKER_TEXT)));
        historyNotificationEntity.setIsFollowed(isFollowed(historyNotificationEntity));

        if (getBundleKeys) {
            List<HistoryBundleKeyEntity> bundleKeys = getBundleKeys(historyNotificationEntity);
            Collections.sort(bundleKeys, BundleKeyEntity.Comparators.DEFAULT);
            historyNotificationEntity.getBundleKeyList().set(bundleKeys);
        }
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
                " AND t1." + DBContract.HistoryNotificationFeed.COLUMN_NAME_SBN_ID + " = t2." + DBContract.HistoryNotificationFeed.COLUMN_NAME_SBN_ID +
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


    private HistoryBundleKeyEntity getHistoryBundleKey(Cursor cursor) {
        String key = cursor.getString(cursor.getColumnIndex(DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_BUNDLE_KEY));
        String value = cursor.getString(cursor.getColumnIndex(DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_BUNDLE_VALUE));
        String packageName = cursor.getString(cursor.getColumnIndex(DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_PACKAGE_NAME));
        int sbnId = cursor.getInt(cursor.getColumnIndex(DBContract.HistoryBundlesKeysFeed.COLUMN_NAME_SBN_ID));

        HistoryBundleKeyEntity entity = new HistoryBundleKeyEntity(packageName,sbnId, value, key);
        boolean isFollowed = isFollowed(entity);
        if (isFollowed) {
            boolean isShowAlways = isShowAlways(entity);
            entity.setIsShowAlways(isShowAlways);
            entity.setPriority(getBundleKeyPriority(entity));
        }
        entity.setIsFollowed(isFollowed);
        return entity;
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
