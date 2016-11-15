package com.stairway.data.source.message;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.stairway.data.local.core.DatabaseManager;
import com.stairway.data.local.core.SQLiteContract.MessagesContract;
import com.stairway.data.manager.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import rx.Observable;
import rx.Subscriber;

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

        return Observable.create(subscriber -> {
            SQLiteDatabase db = databaseManager.openConnection();
            List<MessageResult> result = new ArrayList<>();

            String selection = MessagesContract.COLUMN_CHAT_ID + " = ?";
            String[] selectionArgs = {chatId};
            String[] columns = {
                            MessagesContract.COLUMN_CHAT_ID,
                            MessagesContract.COLUMN_FROM_ID,
                            MessagesContract.COLUMN_MESSAGE,
                            MessagesContract.COLUMN_MESSAGE_STATUS,
                            MessagesContract.COLUMN_ROW_ID,
                            MessagesContract.COLUMN_CREATED_AT};

            try{
                Cursor cursor = db.query(MessagesContract.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
                Logger.d("[MESSAGE STORE]: QUERY"+MessagesContract.SQL_SELECT_MESSAGES+", chatid="+chatId);
                cursor.moveToFirst();

                while(!cursor.isAfterLast()) {
//                    String chat_id = cursor.getString(cursor.getColumnIndex(MessagesContract.COLUMN_CHAT_ID));
                    String fromId = cursor.getString(cursor.getColumnIndex(MessagesContract.COLUMN_FROM_ID));
                    String message = cursor.getString(cursor.getColumnIndex(MessagesContract.COLUMN_MESSAGE));
                    String messageStatus = cursor.getString(cursor.getColumnIndex(MessagesContract.COLUMN_MESSAGE_STATUS));
                    String messageId = cursor.getString(cursor.getColumnIndex(MessagesContract.COLUMN_ROW_ID));
                    String time = cursor.getString(cursor.getColumnIndex(MessagesContract.COLUMN_CREATED_AT));

                    MessageResult msg = new MessageResult(chatId, fromId, message, MessageResult.MessageStatus.valueOf(messageStatus),getFormattedTime(time, "hh:mm"));
                    msg.setMessageId(messageId);
                    Logger.d("[MessageStore] Message:"+msg.toString());
                    result.add(msg);

                    // Update message to seen
                    if(messageStatus.equals(MessageResult.MessageStatus.UNSEEN.name())) {
                        msg.setMessageStatus(MessageResult.MessageStatus.SEEN);
                        updateMessage(msg).subscribe(new Subscriber<MessageResult>() {
                            @Override
                            public void onCompleted() {}
                            @Override
                            public void onError(Throwable e) {Logger.e(e.getMessage());}

                            @Override
                            public void onNext(MessageResult messageResult) {
                                Logger.d("Changed message to seen");
                            }
                        });
                    }

                    cursor.moveToNext();
                }
                cursor.close();
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
    }

    public Observable<MessageResult> storeMessage(MessageResult messageResult){
        return Observable.create(subscriber -> {
            SQLiteDatabase db = databaseManager.openConnection();
            String currentTime = getDateTime();

            ContentValues values = new ContentValues();
            values.put(MessagesContract.COLUMN_CHAT_ID, messageResult.getChatId());
            values.put(MessagesContract.COLUMN_FROM_ID, messageResult.getFromId());
            values.put(MessagesContract.COLUMN_MESSAGE, messageResult.getMessage());
            values.put(MessagesContract.COLUMN_MESSAGE_STATUS, messageResult.getMessageStatus().name());
            values.put(MessagesContract.COLUMN_CREATED_AT, currentTime);

            long rowId = db.insert(MessagesContract.TABLE_NAME, null, values);
            messageResult.setTime(getFormattedTime(currentTime, "hh:mm"));
            messageResult.setMessageId(String.valueOf(rowId));

            subscriber.onNext(messageResult);
            subscriber.onCompleted();
            databaseManager.closeConnection();
        });
    }

    public Observable<MessageResult> updateMessage(MessageResult messageResult){
        return Observable.create(subscriber -> {
            SQLiteDatabase db = databaseManager.openConnection();
            ContentValues values = new ContentValues();
            values.put(MessagesContract.COLUMN_CHAT_ID, messageResult.getChatId());
            values.put(MessagesContract.COLUMN_FROM_ID, messageResult.getFromId());
            values.put(MessagesContract.COLUMN_MESSAGE, messageResult.getMessage());
            values.put(MessagesContract.COLUMN_MESSAGE_STATUS, messageResult.getMessageStatus().name());

            db.update(MessagesContract.TABLE_NAME, values, MessagesContract.COLUMN_ROW_ID+"="+messageResult.getMessageId(), null);

            subscriber.onNext(messageResult);
            subscriber.onCompleted();
            databaseManager.closeConnection();
        });
    }

    public Observable<MessageResult> getUnsentMessages(String chatId) {
        return Observable.create(subscriber -> {
            SQLiteDatabase db = databaseManager.openConnection();

            String selection = MessagesContract.COLUMN_CHAT_ID + "=? AND "
                    +MessagesContract.COLUMN_MESSAGE_STATUS + "=?";

            String[] selectionArgs = {chatId, MessageResult.MessageStatus.NOT_SENT.name()};
            String[] columns = {
                    MessagesContract.COLUMN_CHAT_ID,
                    MessagesContract.COLUMN_FROM_ID,
                    MessagesContract.COLUMN_MESSAGE,
                    MessagesContract.COLUMN_MESSAGE_STATUS,
                    MessagesContract.COLUMN_ROW_ID,
                    MessagesContract.COLUMN_CREATED_AT};

            try{
                Cursor cursor = db.query(MessagesContract.TABLE_NAME, columns, selection, selectionArgs, null, null, "rowid ASC");
                cursor.moveToFirst();

                while(!cursor.isAfterLast()) {
//                    String chat_id = cursor.getString(cursor.getColumnIndex(MessagesContract.COLUMN_CHAT_ID));
                    String fromId = cursor.getString(cursor.getColumnIndex(MessagesContract.COLUMN_FROM_ID));
                    String message = cursor.getString(cursor.getColumnIndex(MessagesContract.COLUMN_MESSAGE));
                    String delivery = cursor.getString(cursor.getColumnIndex(MessagesContract.COLUMN_MESSAGE_STATUS));
                    String messageId = cursor.getString(cursor.getColumnIndex(MessagesContract.COLUMN_ROW_ID));
                    String time = cursor.getString(cursor.getColumnIndex(MessagesContract.COLUMN_CREATED_AT));

                    MessageResult.MessageStatus deliveryStatus = MessageResult.MessageStatus.valueOf(delivery);

                    MessageResult msg = new MessageResult(chatId, fromId, message, deliveryStatus, getFormattedTime(time, "hh:mm"));
                    msg.setMessageId(messageId);

                    subscriber.onNext(msg);
                    cursor.moveToNext();
                }
                cursor.close();
                subscriber.onCompleted();
                databaseManager.closeConnection();
            } catch (Exception e) {
                Logger.e("MessageStore sqlite error: "+e.getMessage());
                databaseManager.closeConnection();
                subscriber.onError(e);
                subscriber.onCompleted();
            }
        });
    }

    public Observable<List<MessageResult>> getChatList() {
        return Observable.create(subscriber -> {

            List<MessageResult> result = new ArrayList<>();
            SQLiteDatabase db = databaseManager.openConnection();
            try {
                final String COLUMN_UNSEEN_COUNT = "COUNT_UNSEEN";
                Cursor cursor = db.rawQuery("SELECT a."+MessagesContract.COLUMN_CHAT_ID+", b."+
                        MessagesContract.COLUMN_FROM_ID+", MAX(a."+
                        MessagesContract.COLUMN_ROW_ID+") AS rowid, b."+
                        MessagesContract.COLUMN_MESSAGE+", b."+
                        MessagesContract.COLUMN_MESSAGE_STATUS+", b."+
                        MessagesContract.COLUMN_CREATED_AT +" FROM "+
                        MessagesContract.TABLE_NAME+" a INNER JOIN "+
                        MessagesContract.TABLE_NAME+" b on a."+
                        MessagesContract.COLUMN_ROW_ID+"=b."+
                        MessagesContract.COLUMN_ROW_ID+" GROUP BY a."+
                        MessagesContract.COLUMN_CHAT_ID+" ORDER BY a."+
                        MessagesContract.COLUMN_ROW_ID+" DESC;", null);
                cursor.moveToFirst();

                while(!cursor.isAfterLast()) {
                    String chatId = cursor.getString(cursor.getColumnIndex(MessagesContract.COLUMN_CHAT_ID));
                    String fromId = cursor.getString(cursor.getColumnIndex(MessagesContract.COLUMN_FROM_ID));
                    String message = cursor.getString(cursor.getColumnIndex(MessagesContract.COLUMN_MESSAGE));
                    String delivery = cursor.getString(cursor.getColumnIndex(MessagesContract.COLUMN_MESSAGE_STATUS));
                    String messageId = cursor.getString(cursor.getColumnIndex(MessagesContract.COLUMN_ROW_ID));
                    String time = cursor.getString(cursor.getColumnIndex(MessagesContract.COLUMN_CREATED_AT));

                    String selection = MessagesContract.COLUMN_CHAT_ID + "=? AND "
                            +MessagesContract.COLUMN_MESSAGE_STATUS + "=?";
                    String[] selectionArgs = {chatId, MessageResult.MessageStatus.UNSEEN.name()};
                    String[] columns = {"COUNT(*) AS "+COLUMN_UNSEEN_COUNT};
                    Cursor unSeenCursor = db.query(MessagesContract.TABLE_NAME, columns, selection, selectionArgs, MessagesContract.COLUMN_MESSAGE_STATUS, null, null);
                    int unSeenCount = 0;
                    if(unSeenCursor.getCount() > 0) {
                        unSeenCursor.moveToFirst();
                        unSeenCount = unSeenCursor.getInt(unSeenCursor.getColumnIndex(COLUMN_UNSEEN_COUNT));
                    }
                    Logger.d("Notification count"+unSeenCount);

                    MessageResult.MessageStatus messageStatus = MessageResult.MessageStatus.valueOf(delivery);

                    MessageResult msg = new MessageResult(chatId, fromId, message, messageStatus, getFormattedTime(time, "hh:mm"));
                    msg.setUnSeenCount(unSeenCount);
                    msg.setMessageId(messageId);

                    result.add(msg);
                    unSeenCursor.close();
                    cursor.moveToNext();
                }
                cursor.close();
                subscriber.onNext(result);
                databaseManager.closeConnection();
            } catch (Exception e) {
                Logger.e("MessageStore sqlite error: "+e.getMessage());
                databaseManager.closeConnection();
                subscriber.onError(e);
                subscriber.onCompleted();
            }
        });
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