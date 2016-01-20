package org.stream.split.voicenotification.Exceptions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import org.stream.split.voicenotification.Logging.BaseLogger;
import org.stream.split.voicenotification.VoiceNotificationActivity;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by split on 2016-01-11.
 */
public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Context mContext;
    private final String LINE_SEPARATOR = "\n";
    private BaseLogger Logger = BaseLogger.getInstance();
    private String TAG = this.getClass().getSimpleName();

    public ExceptionHandler(Context context)
    {
        mContext = context;
    }
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        StringWriter stackTrace = new StringWriter();
        ex.printStackTrace(new PrintWriter(stackTrace));
        StringBuilder errorReport = new StringBuilder();
        errorReport.append("************ CAUSE OF ERROR ************\n\n");
        errorReport.append(stackTrace.toString());
        errorReport.append("\n************ DEVICE INFORMATION ***********\n");
        errorReport.append("Brand: ");
        errorReport.append(Build.BRAND);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Device: ");
        errorReport.append(Build.DEVICE);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Model: ");
        errorReport.append(Build.MODEL);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Id: ");
        errorReport.append(Build.ID);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Product: ");
        errorReport.append(Build.PRODUCT);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("\n************ FIRMWARE ************\n");
        errorReport.append("SDK: ");
        errorReport.append(Build.VERSION.SDK);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Release: ");
        errorReport.append(Build.VERSION.RELEASE);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Incremental: ");
        errorReport.append(Build.VERSION.INCREMENTAL);
        errorReport.append(LINE_SEPARATOR);

        Logger.d(TAG, errorReport.toString());
        Intent intent = new Intent(mContext, mContext.getClass());
        intent.putExtra("error", errorReport.toString());
        mContext.startActivity(intent);

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}
