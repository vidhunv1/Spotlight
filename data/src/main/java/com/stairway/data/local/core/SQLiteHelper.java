package com.stairway.data.local.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.stairway.data.manager.Logger;

/**
 * Created by vidhun on 05/07/16.
 */
public class SQLiteHelper extends SQLiteOpenHelper{
    private static final String TAG = "SQLiteHelper";

    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "spotlight.db";

    public SQLiteHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(SQLiteContract.GenericCacheContract.SQL_CREATE_TABLE);
        db.execSQL(SQLiteContract.MessagesContract.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(SQLiteContract.GenericCacheContract.SQL_DELETE_TABLE);
        db.execSQL(SQLiteContract.MessagesContract.SQL_DELETE_TABLE);
        onCreate(db);
        Logger.d("Sqlite Cache database upgraded");
    }
}


