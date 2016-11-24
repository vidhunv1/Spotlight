package com.stairway.data.local.core;

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
        public static final String SQL_DELETE_ALL = "DELETE FROM " + TABLE_NAME;
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

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_CHAT_ID + " INTEGER, " +
                COLUMN_FROM_ID + " INTEGER, " +
                COLUMN_MESSAGE + " TEXT, " +
                COLUMN_MESSAGE_STATUS + " INTEGER, " +
                COLUMN_RECEIPT_ID + " TEXT, " +
                COLUMN_CREATED_AT+ " DATETIME);";

        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final String SQL_INSERT_MESSAGE = "INSERT INTO " + TABLE_NAME + " VALUES (?, ?, ?, ?)";
        public static final String SQL_DELETE_ALL = "DELETE FROM " + TABLE_NAME;

        public static final String SQL_SELECT_MESSAGES = "SELECT "+COLUMN_CHAT_ID+", "+COLUMN_FROM_ID+", "+", "+COLUMN_RECEIPT_ID+", "+
                COLUMN_MESSAGE+", "+COLUMN_MESSAGE_STATUS+" FROM "+TABLE_NAME+" WHERE "+COLUMN_CHAT_ID+"=?;";

    }
}
