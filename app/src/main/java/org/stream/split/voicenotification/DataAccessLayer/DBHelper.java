package org.stream.split.voicenotification.DataAccessLayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.stream.split.voicenotification.Enities.AppInfoEntity;

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
        db.execSQL(DBContract.AppFeed.SQL_CREATE_APPFEED);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DBContract.AppFeed.SQL_DELETE_APPFEED);
        onCreate(db);

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
                app.setPackageName(cursor.getString(cursor.getColumnIndex(DBContract.AppFeed.COLUMN_NAME_PACKAGENAME)));
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
        values.put(DBContract.AppFeed.COLUMN_NAME_PACKAGENAME,app.getPackageName());
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
                " where " + DBContract.AppFeed.COLUMN_NAME_PACKAGENAME + " = '" + PackageName+ "'";
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
        this.getWritableDatabase().delete(DBContract.AppFeed.TABLE_NAME,
                DBContract.AppFeed.COLUMN_NAME_PACKAGENAME + " = ?",
                new String[]{String.valueOf(app.getPackageName())});
    }

}
