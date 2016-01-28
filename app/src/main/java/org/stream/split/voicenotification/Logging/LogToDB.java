package org.stream.split.voicenotification.Logging;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import org.stream.split.voicenotification.DataAccessLayer.DBContract;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by split on 2016-01-25.
 */
public class LogToDB extends ILogDb  {

    public LogToDB(Context context) {
        super(context,LogDBContract.DB_NAME,null,LogDBContract.DB_VERSION);
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

    private abstract class LogDBContract
    {
        public static final String DB_NAME = "LOG.db";
        public static final int DB_VERSION = 1;

        public abstract class LogFeed implements BaseColumns {
            public static final String TABLE_NAME = "Logs";
            public static final String COLUMN_NAME_TAG = "Tag";
            public static final String COLUMN_NAME_ID = "id";
            public static final String COLUMN_NAME_MESSAGE_PRIORITY = "MessagePriority";
            public static final String COLUMN_NAME_MESSAGE = "Message";
            public static final String COLUMN_NAME_CREATION_DATE = "CreationDate";
            public static final String COLUMN_NAME_EXCEPTION = "Exception";
            public static final String COLUMN_NAME_STACK_TRACE = "StackTrace";

            public static final String SQL_CREATE_TABLE = "CREATE TABLE "+ TABLE_NAME + "( "+
                    COLUMN_NAME_ID + " INTEGER PRIMARY KEY, "+
                    COLUMN_NAME_TAG + " TEXT NOT NULL, " +
                    COLUMN_NAME_MESSAGE_PRIORITY + " INTEGER NOT NULL, "+
                    COLUMN_NAME_MESSAGE + " TEXT NOT NULL, "+
                    COLUMN_NAME_CREATION_DATE + " INTEGER NOT NULL, "+
                    COLUMN_NAME_EXCEPTION + " TEXT, "+
                    COLUMN_NAME_STACK_TRACE + " TEXT);";

            public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        }
    }
}
