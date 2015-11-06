package org.stream.split.voicenotification.DataAccessLayer;

import android.provider.BaseColumns;

/**
 * Created by split on 2015-10-27.
 */
public final class DBContract {

    public final static String DB_Name = "VoiceNotification.db";
    public final static int DB_Version = 1;


    DBContract(){}

    public static abstract class AppFeed implements BaseColumns {
        public static final String TABLE_NAME = "App";
        public static final String COLUMN_NAME_PACKAGENAME = "PackageName";

        public static final String SQL_CREATE_APPFEED = "CREATE TABLE "+ TABLE_NAME +" ("+
                COLUMN_NAME_PACKAGENAME + " TEXT PRIMARY KEY)";
        public static final String SQL_DELETE_APPFEED = "DROP TABLE IF EXIST " + TABLE_NAME;

    }

    public static abstract class NotificationHistoryFeed implements BaseColumns
    {
        public static final String TABLE_NAME = "NotificationHistory";
        public static final String COLUMN_NAME_TIME_STAMP = "TimeStamp";
        public static final String COLUMN_NAME_PACKAGENAME = "PackageName";
        public static final String COLUMN_NAME_MESSAGE = "Message";
    }

    public static abstract class LogFeed implements BaseColumns{
        public static final String TABLE_NAME = "Logs";
        public static final String COLUMN_NAME_MESSAGE = "Message";
        public static final String COLUMN_NAME_MESSAGE_TYPE = "MessageType";
        public static final String COLUMN_NAME_CREATION_DATE = "CreationDate";
        public static final String COLUMN_NAME_EXCEPTION = "Exception";
        public static final String COLUMN_NAME_STACK_TRACE = "StackTrace";
    }
}
