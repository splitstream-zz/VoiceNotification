package org.stream.split.voicenotification.Logging;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteClosable;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.Closeable;

/**
 * Created by split on 2016-01-25.
 */
public abstract class ILogDb extends SQLiteOpenHelper {

    public ILogDb(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public abstract void Log(int priority, String tag, String message, long timeStamp);
    public abstract void Log(int priority, String tag, String message, Throwable throwable, long timeStamp);
}
