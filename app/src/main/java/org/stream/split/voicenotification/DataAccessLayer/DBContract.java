package org.stream.split.voicenotification.DataAccessLayer;

import android.provider.BaseColumns;

/**
 * Created by split on 2015-10-27.
 */
public final class DBContract {

    public final static String DB_Name = "VoiceNotification.db";
    public final static int DB_Version = 51;
    public final static int HistoryQuantityLimit = 50;

    DBContract(){}

    public static abstract class FollowedAppFeed implements BaseColumns {
        public static final String TABLE_NAME = "FollowedApp";
        public static final String COLUMN_NAME_PACKAGE_NAME = "PackageName";
        public static final String COLUMN_NAME_APPLICATION_LABEL = "Label";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE "+ TABLE_NAME +" ("+
                COLUMN_NAME_PACKAGE_NAME + " TEXT PRIMARY KEY," +
                COLUMN_NAME_APPLICATION_LABEL + " TEXT)";

        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class FollowedNotificationsFeed implements BaseColumns{
        public static final String TABLE_NAME = "FollowedNotifications";
        public static final String COLUMN_NAME_SBN_ID = "SbnId";
        public static final String COLUMN_NAME_PACKAGE_NAME = "PackageName";
        public static final String COLUMN_NAME_POLICY = "Policy";


        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "( "+
                COLUMN_NAME_PACKAGE_NAME + " TEXT NOT NULL, " +
                COLUMN_NAME_SBN_ID + " INTEGER NOT NULL, " +
                COLUMN_NAME_POLICY + " TEXT NOT NULL, " +
                "PRIMARY KEY(" + COLUMN_NAME_PACKAGE_NAME + ", " + COLUMN_NAME_SBN_ID+"), "+
        "FOREIGN KEY("+COLUMN_NAME_PACKAGE_NAME + ") REFERENCES " +
                FollowedAppFeed.TABLE_NAME + "("+FollowedAppFeed.COLUMN_NAME_PACKAGE_NAME+"));";

        public static String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    //TODO PackageName and sbn_id might not be needed - only one way relationship is required? form followedApp/followedNotification
    /**
     * Table should be used for setting purposes. It stores Keys for Notification.extras
     * bundle values that should be uttered in speech module.
     */
    public static abstract class FollowedBundleKeysFeed implements BaseColumns {
        public static final String TABLE_NAME = "FollowedBundleKeys";
        public static final String COLUMN_NAME_PACKAGE_NAME = "PackageName";
        public static final String COLUMN_NAME_SBN_ID = "SbnId";
        public static final String COLUMN_NAME_BUNDLE_KEY = "BundleKey";
        public static final String COLUMN_NAME_PRIORITY = "BundleKeyPriority";
        public static final String COLUMN_NAME_SHOW_ALWAYS = "ShowAlways";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "( " +
                        COLUMN_NAME_PACKAGE_NAME + " TEXT NOT NULL, " +
                        COLUMN_NAME_SBN_ID + " INTEGER DEFAULT -1, " +
                        COLUMN_NAME_BUNDLE_KEY + " TEXT NOT NULL, " +
                        COLUMN_NAME_PRIORITY + " INTEGER NOT NULL, " +
                        COLUMN_NAME_SHOW_ALWAYS + " INTEGER DEFAULT 0," +
                        "FOREIGN KEY(" + COLUMN_NAME_PACKAGE_NAME + "," + COLUMN_NAME_SBN_ID + ") REFERENCES + " +
                        FollowedNotificationsFeed.TABLE_NAME + "(" + FollowedNotificationsFeed.COLUMN_NAME_PACKAGE_NAME + "," + FollowedNotificationsFeed.COLUMN_NAME_SBN_ID + ") " +
                        "ON DELETE CASCADE, " +
                        "FOREIGN KEY(" + COLUMN_NAME_PACKAGE_NAME + ") REFERENCES + " +
                        FollowedAppFeed.TABLE_NAME + "(" + FollowedAppFeed.COLUMN_NAME_PACKAGE_NAME + ") " +
                        "ON DELETE CASCADE);";

        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class HistoryNotificationFeed implements BaseColumns {
        public static final String TABLE_NAME = "NotificationHistory";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_SBN_ID = "SbnId";
        public static final String COLUMN_NAME_TINKER_TEXT = "TinkerText";
        public static final String COLUMN_NAME_PACKAGE_NAME = "PackageName";
        public static final String COLUMN_NAME_INSERTION_TIMESTAMP = "InsertionDate";
        public static final String COLUMN_NAME_APPLICATION_LABEL = "ApplicationLabel";
        //TODO tinkertext is needed?
        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " ( " +
                        COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_NAME_SBN_ID + " INTEGER NOT NULL, " +
                        COLUMN_NAME_PACKAGE_NAME + " TEXT NOT NULL, " +
                        COLUMN_NAME_TINKER_TEXT + " TEXT, " +
                        COLUMN_NAME_INSERTION_TIMESTAMP + " INTEGER NOT NULL, " +
                        COLUMN_NAME_APPLICATION_LABEL + " TEXT);";

        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final String SQL_INSERTION_TRIGGER =
                "CREATE TRIGGER limiter_trigger AFTER INSERT ON " + TABLE_NAME +
                        " BEGIN " +
                        "DELETE FROM " + TABLE_NAME + " where " + COLUMN_NAME_ID +
                        " NOT IN (SELECT " + COLUMN_NAME_ID + " from " + TABLE_NAME + " ORDER BY " + COLUMN_NAME_INSERTION_TIMESTAMP + " DESC LIMIT " + HistoryQuantityLimit +"); "+
                        " END;";
    }

    public static abstract class HistoryBundlesKeysFeed implements BaseColumns
    {
        public static final String TABLE_NAME = "BundlesHistory";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_NOTIFICATION_ID = "NotificationId";
        public static final String COLUMN_NAME_UTTERANCE_ID = "UtteranceId";
        public static final String COLUMN_NAME_BUNDLE_KEY = "BundleKey";
        public static final String COLUMN_NAME_PACKAGE_NAME = "PackageName";
        public static final String COLUMN_NAME_BUNDLE_VALUE = "BundleValue";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "( "+
                        COLUMN_NAME_ID + " INTEGER PRIMARY KEY, "+
                        COLUMN_NAME_NOTIFICATION_ID + " INTEGER NOT NULL, " +
                        COLUMN_NAME_PACKAGE_NAME + " TEXT NOT NULL, " +
                        COLUMN_NAME_UTTERANCE_ID + " TEXT, " +
                        COLUMN_NAME_BUNDLE_KEY + " TEXT NOT NULL, " +
                        COLUMN_NAME_BUNDLE_VALUE + " TEXT NOT NULL, " +

                        "FOREIGN KEY(" + COLUMN_NAME_NOTIFICATION_ID + ") REFERENCES " +
                        HistoryNotificationFeed.TABLE_NAME + "(" + HistoryNotificationFeed.COLUMN_NAME_ID +
                        ") ON DELETE CASCADE);";

        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    }

    public static abstract class SettingFeed implements BaseColumns
    {
        public static String TABLE_NAME = "settingsFeed";
        public static String COLUMN_NAME_ID = "id";
        public static String COLUMN_NAME_LABEL = "label";
        public static String COLUMN_NAME_VALUE = "value";

        public static final String SQL_CREATE_TABLE =
        "CREATE TABLE " + TABLE_NAME + "( "+
                COLUMN_NAME_ID + " INTEGER PRIMARY KEY, "+
                COLUMN_NAME_LABEL +  " TEXT NOT NULL, " +
                COLUMN_NAME_VALUE + " TEXT NOT NULL);";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

}
