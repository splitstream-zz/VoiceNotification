package org.stream.split.voicenotification.Logging;

/**
 * Created by split on 2016-01-10.
 */
public interface ILog {
    void v(String TAG, String message);
    void v(String TAG, String message, Throwable throwable);
    void d(String TAG, String message);
    void d(String TAG, String message, Throwable throwable);
    void i(String TAG, String message);
    void i(String TAG, String message, Throwable throwable);
    void w(String TAG, String message);
    void w(String TAG, String message, Throwable throwable);
    void e(String TAG, String message);
    void e(String TAG, String message, Throwable throwable);
}
