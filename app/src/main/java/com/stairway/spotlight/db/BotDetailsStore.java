package com.stairway.spotlight.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.stairway.spotlight.api.bot.PersistentMenu;
import com.stairway.spotlight.core.GsonProvider;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.db.core.DatabaseManager;
import com.stairway.spotlight.db.core.SQLiteContract;

import java.util.Arrays;
import java.util.List;

import rx.Observable;

/**
 * Created by vidhun on 10/02/17.
 */

public class BotDetailsStore {
    private DatabaseManager databaseManager;

    public BotDetailsStore() {
        databaseManager = DatabaseManager.getInstance();
    }

    private static BotDetailsStore instance;

    public static BotDetailsStore getInstance() {
        if (instance == null) {
            instance = new BotDetailsStore();
        }
        return instance;
    }

    public Observable<Boolean> putMenu(String userName, List<PersistentMenu> menus){
        return Observable.create(subscriber -> {
            SQLiteDatabase db = databaseManager.openConnection();
            ContentValues values = new ContentValues();
            values.put(SQLiteContract.BotDetailsContract.COLUMN_USER_NAME, userName);
            values.put(SQLiteContract.BotDetailsContract.COLUMN_PERSISTENT_MENU, GsonProvider.getGson().toJson(menus));
            long rowId = db.insert(SQLiteContract.BotDetailsContract.TABLE_NAME, null, values);
            subscriber.onNext(true);
            subscriber.onCompleted();
            databaseManager.closeConnection();
        });
    }

    public Observable<List<PersistentMenu>> getMenu(String username) {
        return Observable.create(subscriber -> {
            SQLiteDatabase db = databaseManager.openConnection();

            String selection = SQLiteContract.BotDetailsContract.COLUMN_USER_NAME + " = ?";
            String[] selectionArgs = {username};
            String[] columns = {
                    SQLiteContract.BotDetailsContract.COLUMN_USER_NAME,
                    SQLiteContract.BotDetailsContract.COLUMN_PERSISTENT_MENU
            };

            try {
                Cursor cursor = db.query(SQLiteContract.BotDetailsContract.TABLE_NAME, columns, selection, selectionArgs, null, null, null);

                if(cursor.getCount()<=0) {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } else {
                    cursor.moveToFirst();
                    String menu = cursor.getString(cursor.getColumnIndex(SQLiteContract.BotDetailsContract.COLUMN_PERSISTENT_MENU));
                    cursor.close();
                    PersistentMenu[] persistentMenus = GsonProvider.getGson().fromJson(menu, PersistentMenu[].class);
                    List<PersistentMenu> out = Arrays.asList(persistentMenus);
                    subscriber.onNext(out);
                    subscriber.onCompleted();
                }

                databaseManager.closeConnection();
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e(this, "ContactStore sqlite error"+e.getMessage());
                databaseManager.closeConnection();
                subscriber.onError(e);
            }
        });
    }
}
