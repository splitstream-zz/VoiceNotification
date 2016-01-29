package org.stream.split.voicenotification.Logging;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by split on 2016-01-25.
 */
public class DbToLog extends SQLiteOpenHelper implements ILogDb  {

    public DbToLog(Context context) {
        super(context,LogDBContract.DB_NAME,null,LogDBContract.DB_VERSION);
    }

    public DbToLog(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LogDBContract.LogFeed.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(LogDBContract.LogFeed.SQL_DELETE_TABLE);
        onCreate(db);
    }

    public void Log(int priority, String tag, String message, long timeStamp) {
        Log(null, priority, tag, message, timeStamp);
    }

    public void Log(int priority, String tag, String message, Throwable throwable, long timeStamp) {
        ContentValues values = new ContentValues();
        values.put(LogDBContract.LogFeed.COLUMN_NAME_EXCEPTION,throwable.getLocalizedMessage());

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        values.put(LogDBContract.LogFeed.COLUMN_NAME_STACK_TRACE,sw.toString());

        Log(values,priority,tag,message,timeStamp);
    }

    private void Log(ContentValues values,int priority, String tag, String message, long timeStamp)
    {
        if(values == null)
            values = new ContentValues();

        values.put(LogDBContract.LogFeed.COLUMN_NAME_TAG,tag);
        values.put(LogDBContract.LogFeed.COLUMN_NAME_MESSAGE,message);
        values.put(LogDBContract.LogFeed.COLUMN_NAME_MESSAGE_PRIORITY,priority);
        values.put(LogDBContract.LogFeed.COLUMN_NAME_CREATION_DATE, timeStamp);

        getWritableDatabase().insert(LogDBContract.LogFeed.TABLE_NAME,null,values);
    }

}
