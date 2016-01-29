package org.stream.split.voicenotification.Logging;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.Constructor;

/**
 * Created by split on 2016-01-28.
 */
public class DbLogger<T extends SQLiteOpenHelper & ILogDb> implements ILog{
    public static int PRIORITY_V = 0;
    public static int PRIORITY_D = 1;
    public static int PRIORITY_I = 2;
    public static int PRIORITY_W = 3;
    public static int PRIORITY_E = 4;
    public static String TAG = "DbLogger";

    BaseLogger Logger = BaseLogger.getInstance();
    int mPriorityThreshold;
    Context mContext;
    Class<T> mDbType;

    public DbLogger(int priorityThreshold, Context context, Class<T> clazz) {
        mPriorityThreshold = priorityThreshold;
        mContext = context;
        mDbType = clazz;

    }

    @Override
    public void v(String TAG, String message) {
        if (mPriorityThreshold <= PRIORITY_V)
            Log(PRIORITY_V, TAG, message, null, System.currentTimeMillis());
    }

    @Override
    public void v(String TAG, String message, Throwable throwable) {
        if (mPriorityThreshold <= PRIORITY_V) {
            Log(PRIORITY_V, TAG, message, throwable, System.currentTimeMillis());
        }
    }

    @Override
    public void d(String TAG, String message) {
        if (mPriorityThreshold <= PRIORITY_D) {
            Log(PRIORITY_D, TAG, message, null, System.currentTimeMillis());
        }
    }

    @Override
    public void d(String TAG, String message, Throwable throwable) {
        if (mPriorityThreshold <= PRIORITY_D) {
            Log(PRIORITY_D, TAG, message, throwable, System.currentTimeMillis());
        }
    }

    @Override
    public void i(String TAG, String message) {
        if (mPriorityThreshold <= PRIORITY_I) {
            Log(PRIORITY_I, TAG, message, null, System.currentTimeMillis());
        }
    }

    @Override
    public void i(String TAG, String message, Throwable throwable) {
        if (mPriorityThreshold <= PRIORITY_I) {
            Log(PRIORITY_I, TAG, message, throwable, System.currentTimeMillis());
        }
    }

    @Override
    public void w(String TAG, String message) {
        if (mPriorityThreshold <= PRIORITY_W) {
            Log(PRIORITY_W, TAG, message, null, System.currentTimeMillis());
        }
    }

    @Override
    public void w(String TAG, String message, Throwable throwable) {
        if (mPriorityThreshold <= PRIORITY_W) {
            Log(PRIORITY_W, TAG, message, throwable, System.currentTimeMillis());
        }
    }

    @Override
    public void e(String TAG, String message) {
        if (mPriorityThreshold <= PRIORITY_E) {
            Log(PRIORITY_E, TAG, message, null, System.currentTimeMillis());
        }
    }

    @Override
    public void e(String TAG, String message, Throwable throwable) {
        if (mPriorityThreshold <= PRIORITY_E) {
            Log(PRIORITY_E, TAG, message, throwable, System.currentTimeMillis());
        }
    }
    private void Log(int priority, String tag, String message,long timestamp)
    {
        Class[] args = new Class[1];
        args[0] = Context.class;
        try {
            Constructor<T> ct = mDbType.getDeclaredConstructor(args);
            T db = ct.newInstance(mContext);
            db.Log(priority,tag,message,timestamp);
            db.close();
        }
        catch(Exception ex)
        {
            Logger.e(TAG, ex.getMessage(), ex);
        }
    }
    private void Log(int priority, String tag, String message,Throwable throwable, long timestamp)
    {
        Class[] args = new Class[4];
        args[0] = Context.class;
        args[1] = String.class;
        args[2] = SQLiteDatabase.CursorFactory.class;
        args[3] = int.class;

        try {
            Constructor<T> ct = mDbType.getDeclaredConstructor(args);
            T db = ct.newInstance(mContext,LogDBContract.DB_NAME, null, LogDBContract.DB_VERSION);
            db.Log(priority,tag,message,throwable,timestamp);
            db.close();
        }
        catch(Exception ex)
        {
            Logger.e(TAG, ex.getMessage(), ex);
        }
    }
}
