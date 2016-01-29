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
public interface ILogDb {
    void Log(int priority, String tag, String message, long timeStamp);
    void Log(int priority, String tag, String message, Throwable throwable, long timeStamp);
}
