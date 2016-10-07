package com.stairway.data.source.message;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.stairway.data.local.core.DatabaseManager;
import com.stairway.data.local.core.SQLiteContract;
import com.stairway.data.manager.Logger;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by vidhun on 06/08/16.
 */
public class MessageStore {

    private DatabaseManager databaseManager;

    public MessageStore() {
        databaseManager = DatabaseManager.getInstance();
    }

    /*
    Get messages with chatId.
     */
    public Observable<List<MessageResult>> getMessages(String chatId) {

        Observable<List<MessageResult>> getMessages = Observable.create(subscriber -> {
            SQLiteDatabase db = databaseManager.openConnection();
            List<MessageResult> result = new ArrayList<>();

            String selection = SQLiteContract.MessagesContract.COLUMN_CHAT_ID + " = ?";
            String[] selectionArgs = {chatId};
            String[] columns = {
                            SQLiteContract.MessagesContract.COLUMN_CHAT_ID,
                            SQLiteContract.MessagesContract.COLUMN_FROM_ID,
                            SQLiteContract.MessagesContract.COLUMN_MESSAGE,
                            SQLiteContract.MessagesContract.COLUMN_DELIVERY_STATUS,
                            SQLiteContract.MessagesContract.COLUMN_ROW_ID,
                            SQLiteContract.MessagesContract.COLUMN_CREATED_AT};

            try{

                Cursor cursor = db.query(SQLiteContract.MessagesContract.TABLE_NAME, columns, selection, selectionArgs, null, null, null);

                Logger.d("[MESSAGE STORE]: QUERY"+SQLiteContract.MessagesContract.SQL_SELECT_MESSAGES+", chatid="+chatId);
                cursor.moveToFirst();

                while(!cursor.isAfterLast()) {
                    String chat_id = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_CHAT_ID));
                    String fromId = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_FROM_ID));
                    String message = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_MESSAGE));
                    String deliveryStatus = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_DELIVERY_STATUS));
                    String messageId = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_ROW_ID));

                    MessageResult msg = new MessageResult(chatId, fromId, message, MessageResult.DeliveryStatus.valueOf(deliveryStatus));
                    msg.setMessageId(messageId);
                    Logger.d("[MessageStore] Message:"+msg.toString());
                    result.add(msg);
                    cursor.moveToNext();
                }
                subscriber.onNext(result);
                subscriber.onCompleted();

                databaseManager.closeConnection();

            } catch (Exception e) {
                Logger.e("MessageStore sqlite error"+e.getMessage());
                databaseManager.closeConnection();
                subscriber.onError(e);
                subscriber.onCompleted();
            }
        });

        return getMessages;
    }

    public Observable<MessageResult> storeMessage(MessageResult messageResult){
        Observable<MessageResult> storeMessage = Observable.create(subscriber -> {
            SQLiteDatabase db = databaseManager.openConnection();

            ContentValues values = new ContentValues();
            values.put(SQLiteContract.MessagesContract.COLUMN_CHAT_ID, messageResult.getChatId());
            values.put(SQLiteContract.MessagesContract.COLUMN_FROM_ID, messageResult.getFromId());
            values.put(SQLiteContract.MessagesContract.COLUMN_MESSAGE, messageResult.getMessage());
            values.put(SQLiteContract.MessagesContract.COLUMN_DELIVERY_STATUS, messageResult.getDeliveryStatus().name());

            long rowId = db.insert(SQLiteContract.MessagesContract.TABLE_NAME, null, values);

            messageResult.setMessageId(String.valueOf(rowId));

            subscriber.onNext(messageResult);
            subscriber.onCompleted();

            databaseManager.closeConnection();

        });

        Logger.d("Message store: storedMessage "+messageResult.toString());

        return storeMessage;
    }

    public Observable<MessageResult> updateMessage(MessageResult messageResult){


        Observable<MessageResult> updateMessage = Observable.create(subscriber -> {
            SQLiteDatabase db = databaseManager.openConnection();

            ContentValues values = new ContentValues();
            values.put(SQLiteContract.MessagesContract.COLUMN_CHAT_ID, messageResult.getChatId());
            values.put(SQLiteContract.MessagesContract.COLUMN_FROM_ID, messageResult.getFromId());
            values.put(SQLiteContract.MessagesContract.COLUMN_MESSAGE, messageResult.getMessage());
            values.put(SQLiteContract.MessagesContract.COLUMN_DELIVERY_STATUS, messageResult.getDeliveryStatus().name());

            db.update(SQLiteContract.MessagesContract.TABLE_NAME, values, SQLiteContract.MessagesContract.COLUMN_ROW_ID+"="+messageResult.getMessageId(), null);

            subscriber.onNext(messageResult);
            subscriber.onCompleted();

            databaseManager.closeConnection();

        });


        return updateMessage;
    }

    public Observable<MessageResult> getUnsentMessages(String chatId) {
        Observable<MessageResult> getMessages = Observable.create(subscriber -> {
            SQLiteDatabase db = databaseManager.openConnection();
            List<MessageResult> result = new ArrayList<>();

            String selection = SQLiteContract.MessagesContract.COLUMN_CHAT_ID + "=? AND "
                    +SQLiteContract.MessagesContract.COLUMN_DELIVERY_STATUS + "=?";

            String[] selectionArgs = {chatId, MessageResult.DeliveryStatus.NOT_SENT.name()};
            String[] columns = {
                    SQLiteContract.MessagesContract.COLUMN_CHAT_ID,
                    SQLiteContract.MessagesContract.COLUMN_FROM_ID,
                    SQLiteContract.MessagesContract.COLUMN_MESSAGE,
                    SQLiteContract.MessagesContract.COLUMN_DELIVERY_STATUS,
                    SQLiteContract.MessagesContract.COLUMN_ROW_ID};

            try{

                Cursor cursor = db.query(SQLiteContract.MessagesContract.TABLE_NAME, columns, selection, selectionArgs, null, null, "rowid ASC");

                Logger.d("[MESSAGE STORE]: QUERY"+SQLiteContract.MessagesContract.SQL_SELECT_MESSAGES+", chatid="+chatId);
                cursor.moveToFirst();

                while(!cursor.isAfterLast()) {
                    String chat_id = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_CHAT_ID));
                    String fromId = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_FROM_ID));
                    String message = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_MESSAGE));
                    String delivery = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_DELIVERY_STATUS));
                    String messageId = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_ROW_ID));
                    MessageResult.DeliveryStatus deliveryStatus = MessageResult.DeliveryStatus.valueOf(delivery);

                    MessageResult msg = new MessageResult(chatId, fromId, message, deliveryStatus);
                    msg.setMessageId(messageId);

                    subscriber.onNext(msg);
                    cursor.moveToNext();
                }

                subscriber.onCompleted();

                databaseManager.closeConnection();
                Logger.d("[Message store] results count: "+result.size());

            } catch (Exception e) {
                Logger.e("MessageStore sqlite error: "+e.getMessage());
                databaseManager.closeConnection();
                subscriber.onError(e);
                subscriber.onCompleted();
            }
        });

        return getMessages;
    }
}