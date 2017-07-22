package com.chat.ichat.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.chat.ichat.db.core.DatabaseManager;
import com.chat.ichat.db.core.SQLiteContract;
import com.chat.ichat.models.SavedCardModel;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by vidhun on 14/07/17.
 */
public class SavedCardsStore {
    private DatabaseManager databaseManager;
    public SavedCardsStore() {
        databaseManager = DatabaseManager.getInstance();
    }

    private static SavedCardsStore instance;

    public static SavedCardsStore getInstance() {
        if (instance == null) {
            instance = new SavedCardsStore();
        }
        return instance;
    }

    public Observable<Boolean> putCard(String cardNum, String cardType, int serverId) {
        return Observable.create(subscriber -> {
            SQLiteDatabase db = databaseManager.openConnection();
            ContentValues values = new ContentValues();
            values.put(SQLiteContract.SavedCards.COLUMN_CARD_NUM_MASKED, cardNum);
            values.put(SQLiteContract.SavedCards.COLUMN_CARD_TYPE, cardType);
            values.put(SQLiteContract.SavedCards.COLUMN_SERVER_ID, serverId+"");
            long rowId = db.insert(SQLiteContract.SavedCards.TABLE_NAME, null, values);
            subscriber.onNext(true);
            subscriber.onCompleted();
            databaseManager.closeConnection();
        });
    }

    public Observable<List<SavedCardModel>> getCards() {
        return Observable.create(subscriber -> {
            SQLiteDatabase db = databaseManager.openConnection();

            String[] columns = {
                    SQLiteContract.SavedCards.COLUMN_CARD_NUM_MASKED,
                    SQLiteContract.SavedCards.COLUMN_CARD_TYPE,
                    SQLiteContract.SavedCards.COLUMN_SERVER_ID
            };

            try {
                Cursor cursor = db.query(SQLiteContract.SavedCards.TABLE_NAME, columns, null, null, null, null, null);

                if(cursor.getCount()<=0) {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } else {
                    cursor.moveToFirst();

                    List<SavedCardModel> savedCardModels = new ArrayList<>();
                    while (!cursor.isAfterLast()) {
                        String cardNumber = cursor.getString(cursor.getColumnIndex(SQLiteContract.SavedCards.COLUMN_CARD_NUM_MASKED));
                        String cardType = cursor.getString(cursor.getColumnIndex(SQLiteContract.SavedCards.COLUMN_CARD_TYPE));
                        String serverId = cursor.getString(cursor.getColumnIndex(SQLiteContract.SavedCards.COLUMN_SERVER_ID));
                        savedCardModels.add(new SavedCardModel(cardNumber, cardType, serverId));
                        cursor.moveToNext();
                    }
                    cursor.close();
                    subscriber.onNext(savedCardModels);
                    subscriber.onCompleted();
                }
                databaseManager.closeConnection();
            } catch (Exception e) {
                e.printStackTrace();
                databaseManager.closeConnection();
                subscriber.onError(e);
            }
        });
    }

    public boolean deleteCard(String serverId) {
        SQLiteDatabase db = databaseManager.openConnection();
        String table = SQLiteContract.SavedCards.TABLE_NAME;
        String whereClause = SQLiteContract.SavedCards.COLUMN_SERVER_ID+"=?";
        String[] whereArgs = new String[] { String.valueOf(serverId) };
        boolean b = db.delete(table, whereClause, whereArgs) > 0;
        databaseManager.closeConnection();
        return b;
    }
}
