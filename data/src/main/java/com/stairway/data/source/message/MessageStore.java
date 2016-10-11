package com.stairway.data.source.message;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.stairway.data.local.core.DatabaseManager;
import com.stairway.data.local.core.SQLiteContract;
import com.stairway.data.manager.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
                    String time = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_CREATED_AT));

                    MessageResult msg = new MessageResult(chatId, fromId, message, MessageResult.DeliveryStatus.valueOf(deliveryStatus),getFormattedTime(time, "hh:mm"));
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
            String currentTime = getDateTime();

            ContentValues values = new ContentValues();
            values.put(SQLiteContract.MessagesContract.COLUMN_CHAT_ID, messageResult.getChatId());
            values.put(SQLiteContract.MessagesContract.COLUMN_FROM_ID, messageResult.getFromId());
            values.put(SQLiteContract.MessagesContract.COLUMN_MESSAGE, messageResult.getMessage());
            values.put(SQLiteContract.MessagesContract.COLUMN_DELIVERY_STATUS, messageResult.getDeliveryStatus().name());
            values.put(SQLiteContract.MessagesContract.COLUMN_CREATED_AT, currentTime);

            long rowId = db.insert(SQLiteContract.MessagesContract.TABLE_NAME, null, values);
            messageResult.setTime(getFormattedTime(currentTime, "hh:mm"));
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
                    SQLiteContract.MessagesContract.COLUMN_ROW_ID,
                    SQLiteContract.MessagesContract.COLUMN_CREATED_AT};

            try{
                Cursor cursor = db.query(SQLiteContract.MessagesContract.TABLE_NAME, columns, selection, selectionArgs, null, null, "rowid ASC");
                cursor.moveToFirst();

                Logger.d("[MESSAGE STORE]: QUERY"+SQLiteContract.MessagesContract.SQL_SELECT_MESSAGES+", chatid="+chatId);
                while(!cursor.isAfterLast()) {
                    String chat_id = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_CHAT_ID));
                    String fromId = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_FROM_ID));
                    String message = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_MESSAGE));
                    String delivery = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_DELIVERY_STATUS));
                    String messageId = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_ROW_ID));
                    String time = cursor.getString(cursor.getColumnIndex(SQLiteContract.MessagesContract.COLUMN_CREATED_AT));

                    MessageResult.DeliveryStatus deliveryStatus = MessageResult.DeliveryStatus.valueOf(delivery);

                    MessageResult msg = new MessageResult(chatId, fromId, message, deliveryStatus, getFormattedTime(time, "hh:mm"));
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

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    private String getFormattedTime(String time, String format) {
        SimpleDateFormat formatterFrom = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat formatterTo = new SimpleDateFormat(format);
        formatterTo.setTimeZone(TimeZone.getDefault());
        try {
            Date fullDate = formatterFrom.parse(time);
            return formatterTo.format(fullDate);
        } catch (ParseException e) {
            Logger.e("Error parsing DateTime");
            return "";
        }
    }
}