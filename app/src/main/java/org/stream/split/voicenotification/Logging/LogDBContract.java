package org.stream.split.voicenotification.Logging;

import android.provider.BaseColumns;

/**
 * Created by B on 2016-01-29.
 */
abstract class LogDBContract {
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

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "( " +
                COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NAME_TAG + " TEXT NOT NULL, " +
                COLUMN_NAME_MESSAGE_PRIORITY + " INTEGER NOT NULL, " +
                COLUMN_NAME_MESSAGE + " TEXT NOT NULL, " +
                COLUMN_NAME_CREATION_DATE + " INTEGER NOT NULL, " +
                COLUMN_NAME_EXCEPTION + " TEXT, " +
                COLUMN_NAME_STACK_TRACE + " TEXT);";

        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
