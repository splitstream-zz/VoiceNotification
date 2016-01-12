package org.stream.split.voicenotification.Logging;


import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by split on 2016-01-09.
 */
public class BaseLogger {
    private static BaseLogger mInstance;
    private List<ILog> mLoggers;
    private boolean mLogToOutput = true;

    private BaseLogger()
    {
        mLoggers = new ArrayList<>();
    }
    public void initialize(boolean logToOutput)
    {
        mLogToOutput = logToOutput;
    }

    public static BaseLogger getInstance()
    {
        if(mInstance == null)
            mInstance = new BaseLogger();
        return mInstance;
    }

    public void addLogger(ILog logger)
    {
        mLoggers.add(logger);
    }

    public void v(String TAG,String message)
    {
        if(mLogToOutput)
        Log.v(TAG, message);
        for(ILog logger:mLoggers)
            logger.v(TAG, message);
    }
    public void v(String TAG,String message, Throwable throwable)
    {
        if(mLogToOutput)
        Log.v(TAG, message, throwable);
        for(ILog logger:mLoggers)
            logger.v(TAG,message,throwable);
    }
    public void d(String TAG,String message)
    {
        if(mLogToOutput)
        Log.d(TAG, message);
        for(ILog logger:mLoggers)
            logger.d(TAG, message);
    }
    public void d(String TAG,String message, Throwable throwable)
    {
        if(mLogToOutput)
        Log.d(TAG, message, throwable);
        for(ILog logger:mLoggers)
            logger.d(TAG, message, throwable);
    }
    public void i(String TAG,String message)
    {
        if(mLogToOutput)
        Log.i(TAG, message);
        for(ILog logger:mLoggers)
            logger.i(TAG, message);
    }
    public void i(String TAG,String message, Throwable throwable)
    {
        if(mLogToOutput)
        Log.i(TAG, message, throwable);
        for(ILog logger:mLoggers)
            logger.i(TAG, message, throwable);
    }
    public void w(String TAG,String message)
    {
        if(mLogToOutput)
        Log.w(TAG, message);
        for(ILog logger:mLoggers)
            logger.w(TAG, message);
    }
    public void w(String TAG,String message, Throwable throwable)
    {
        if(mLogToOutput)
        Log.w(TAG, message, throwable);
        for(ILog logger:mLoggers)
            logger.w(TAG, message, throwable);
    }
    public void e(String TAG,String message)
    {
        if(mLogToOutput)
        Log.e(TAG, message);
        for(ILog logger:mLoggers)
            logger.e(TAG, message);
    }
    public void e(String TAG,String message, Throwable throwable)
    {
        if(mLogToOutput)
        Log.e(TAG, message, throwable);
        for(ILog logger:mLoggers)
            logger.e(TAG, message, throwable);
    }


}
