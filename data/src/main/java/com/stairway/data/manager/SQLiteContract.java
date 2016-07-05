package com.stairway.data.manager;

/**
 * Created by vidhun on 05/07/16.
 */
public abstract class SQLiteContract {
    public static final class GenericCache {
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
}
