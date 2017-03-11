package com.stairway.spotlight.db.core;

/**
 * Created by vidhun on 05/07/16.
 */
public abstract class SQLiteContract {
    public static final class GenericCacheContract {
        public static final String TABLE_NAME = "generic_cache";

        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_VALUE = "value";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_KEY + " TEXT PRIMARY KEY, " +
                COLUMN_VALUE + " TEXT)";

        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME + " VALUES (?,?)";
        public static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME + " where key = ?";
    }

    public static final class BotDetailsContract {
        public static final String TABLE_NAME = "bot_details";

        public static final String COLUMN_USER_NAME = "user_name";
        public static final String COLUMN_PERSISTENT_MENU = "persistent_menu";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_USER_NAME + " TEXT PRIMARY KEY, " +
                COLUMN_PERSISTENT_MENU + " TEXT, " +
                "UNIQUE("+COLUMN_USER_NAME+") ON CONFLICT REPLACE); ";

        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static final class MessagesContract {
        public static final String TABLE_NAME = "messages";

        public static final String COLUMN_ROW_ID = "rowid";
        public static final String COLUMN_CHAT_ID = "chat_id";
        public static final String COLUMN_FROM_ID = "from_id";
        public static final String COLUMN_MESSAGE = "message";
        public static final String COLUMN_MESSAGE_STATUS = "delivery_status";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_RECEIPT_ID = "receipt_id";
        public static final String COLUMN_IS_RECEIPT_SENT = "is_receipt_sent";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_CHAT_ID + " INTEGER, " +
                COLUMN_FROM_ID + " INTEGER, " +
                COLUMN_MESSAGE + " TEXT, " +
                COLUMN_MESSAGE_STATUS + " INTEGER, " +
                COLUMN_RECEIPT_ID + " TEXT, " +
                COLUMN_IS_RECEIPT_SENT + " INTEGER DEFAULT 0, " +
                COLUMN_CREATED_AT+ " TEXT);";

        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    }

    public static final class ContactsContract {
        public static final String TABLE_NAME = "contacts";

        public static final String COLUMN_PHONE_NUMBER = "phone_number";
        public static final String COLUMN_COUNTRY_CODE = "country_code";
        public static final String COLUMN_CONTACT_NAME = "contact_name";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_USER_TYPE = "user_type";
        public static final String COLUMN_IS_ADDED = "is_added";
        public static final String COLUMN_IS_BLOCKED = "is_blocked";
        public static final String COLUMN_CREATED_AT = "created_at";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
//                COLUMN_CONTACT_ID + " INTEGER, " +
                COLUMN_PHONE_NUMBER + " TEXT, " +
                COLUMN_COUNTRY_CODE + " TEXT, " +
                COLUMN_USERNAME + " TEXT, "+
                COLUMN_USER_ID + " TEXT, "+
                COLUMN_USER_TYPE + " TEXT, " +
                COLUMN_CONTACT_NAME + " TEXT, " +
                COLUMN_IS_ADDED + " INTEGER DEFAULT 0, " +
                COLUMN_IS_BLOCKED + " INTEGER DEFAULT 0, " +
                COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE("+COLUMN_USERNAME+") ON CONFLICT REPLACE); ";

        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
