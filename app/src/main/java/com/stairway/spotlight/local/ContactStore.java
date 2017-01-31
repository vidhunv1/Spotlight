package com.stairway.spotlight.local;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.local.core.DatabaseManager;
import com.stairway.spotlight.local.core.SQLiteContract;
import com.stairway.spotlight.models.ContactResult;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by vidhun on 07/12/16.
 */

public class ContactStore {
    private DatabaseManager databaseManager;

    public ContactStore() {
        databaseManager = DatabaseManager.getInstance();
    }

    public Observable<Boolean> storeContact(ContactResult contactResult){
        List<ContactResult> contact = new ArrayList<>();
        contact.add(contactResult);
        return storeContacts(contact);
    }

    public Observable<Boolean> storeContacts(List<ContactResult> contactResults){
        Logger.d(this);
        return Observable.create(subscriber -> {
            SQLiteDatabase db = databaseManager.openConnection();
            ContentValues values = new ContentValues();

            for (ContactResult contactResult : contactResults) {
                values.put(SQLiteContract.ContactsContract.COLUMN_CONTACT_NAME, contactResult.getDisplayName());
                values.put(SQLiteContract.ContactsContract.COLUMN_PHONE_NUMBER, contactResult.getPhoneNumber());
                values.put(SQLiteContract.ContactsContract.COLUMN_COUNTRY_CODE, contactResult.getCountryCode());
                values.put(SQLiteContract.ContactsContract.COLUMN_USERNAME, contactResult.getUsername());
                values.put(SQLiteContract.ContactsContract.COLUMN_USER_ID, contactResult.getUserId());
                values.put(SQLiteContract.ContactsContract.COLUMN_IS_REGISTERED, contactResult.isRegistered());
                values.put(SQLiteContract.ContactsContract.COLUMN_IS_ADDED, contactResult.isAdded());
                long rowId = db.insert(SQLiteContract.ContactsContract.TABLE_NAME, null, values);
            }
            subscriber.onNext(true);
            subscriber.onCompleted();
            databaseManager.closeConnection();
        });
    }

    public Observable<List<ContactResult>> getContacts() {
        return Observable.create(subscriber -> {
            SQLiteDatabase db = databaseManager.openConnection();
            List<ContactResult> result = new ArrayList<>();

            String selection = "";
            String[] selectionArgs = {};
            String[] columns = {
                    SQLiteContract.ContactsContract.COLUMN_CONTACT_NAME,
                    SQLiteContract.ContactsContract.COLUMN_COUNTRY_CODE,
                    SQLiteContract.ContactsContract.COLUMN_PHONE_NUMBER,
                    SQLiteContract.ContactsContract.COLUMN_IS_ADDED,
                    SQLiteContract.ContactsContract.COLUMN_IS_REGISTERED,
                    SQLiteContract.ContactsContract.COLUMN_USERNAME,
                    SQLiteContract.ContactsContract.COLUMN_USER_ID
            };

            try {
                Cursor cursor = db.query(SQLiteContract.ContactsContract.TABLE_NAME, columns, selection, selectionArgs, null, null, SQLiteContract.ContactsContract.COLUMN_CONTACT_NAME+" ASC");
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    String contactName = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_CONTACT_NAME));
                    String countryCode = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_COUNTRY_CODE));
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_PHONE_NUMBER));
                    String username = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_USERNAME));
                    String userId = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_USER_ID));
                    boolean isRegistered = (cursor.getInt(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_IS_REGISTERED)) == 1);
                    boolean isAdded = (cursor.getInt(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_IS_ADDED)) == 1);

                    ContactResult contactResult = new ContactResult(countryCode, phoneNumber, contactName);
                    contactResult.setAdded(isAdded);
                    contactResult.setRegistered(isRegistered);
                    contactResult.setUsername(username);
                    contactResult.setUserId(userId);
                    result.add(contactResult);

                    cursor.moveToNext();
                }
                cursor.close();
                subscriber.onNext(result);
                subscriber.onCompleted();

                databaseManager.closeConnection();
            } catch (Exception e) {
                Logger.e(this, "ContactStore sqlite error"+e.getMessage());
                databaseManager.closeConnection();
                subscriber.onError(e);
            }
        });
    }

    public Observable<List<ContactResult>> getContacts(String name) {
        Logger.d(this, name+" contactStore");
        return Observable.create(subscriber -> {
            List<ContactResult> result = new ArrayList<>();
            if(name.length()==0) {
                subscriber.onNext(result);
                subscriber.onCompleted();
            }
            SQLiteDatabase db = databaseManager.openConnection();

            String selection = SQLiteContract.ContactsContract.COLUMN_CONTACT_NAME + " LIKE ? OR "+ SQLiteContract.ContactsContract.COLUMN_CONTACT_NAME+" LIKE ? ";
            String[] selectionArgs = {name+"%", "% "+name+"%"};
            String[] columns = {
                    SQLiteContract.ContactsContract.COLUMN_CONTACT_NAME,
                    SQLiteContract.ContactsContract.COLUMN_COUNTRY_CODE,
                    SQLiteContract.ContactsContract.COLUMN_PHONE_NUMBER,
                    SQLiteContract.ContactsContract.COLUMN_IS_ADDED,
                    SQLiteContract.ContactsContract.COLUMN_IS_REGISTERED,
                    SQLiteContract.ContactsContract.COLUMN_USERNAME,
                    SQLiteContract.ContactsContract.COLUMN_USER_ID
            };

            try {
                Cursor cursor = db.query(SQLiteContract.ContactsContract.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
                cursor.moveToFirst();
                Logger.d(this, "count ");

                while (!cursor.isAfterLast()) {
                    String contactName = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_CONTACT_NAME));
                    String countryCode = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_COUNTRY_CODE));
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_PHONE_NUMBER));
                    String username = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_USERNAME));
                    String userId = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_USER_ID));
                    boolean isRegistered = (cursor.getInt(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_IS_REGISTERED)) == 1);
                    boolean isAdded = (cursor.getInt(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_IS_ADDED)) == 1);

                    ContactResult contactResult = new ContactResult(countryCode, phoneNumber, contactName);
                    contactResult.setAdded(isAdded);
                    contactResult.setRegistered(isRegistered);
                    contactResult.setUsername(username);
                    contactResult.setUserId(userId);
                    result.add(contactResult);

                    Logger.d(this, "Contacts like: "+contactResult.toString());

                    cursor.moveToNext();
                }
                cursor.close();
                subscriber.onNext(result);
                subscriber.onCompleted();

                databaseManager.closeConnection();
            } catch (Exception e) {
                Logger.e(this, "ContactStore sqlite error"+e.getMessage());
                databaseManager.closeConnection();
                subscriber.onError(e);
            }
        });
    }

    public Observable<ContactResult> getContactByUserId(String userId) {
        return getContact(userId, true);
    }

    public Observable<ContactResult> getContactByUserName(String userName) {
        return getContact(userName, false);
    }

    private Observable<ContactResult> getContact(String userId, boolean isId) {
        return Observable.create(subscriber -> {
            if(userId.length()==0) {
                subscriber.onNext(new ContactResult());
                subscriber.onCompleted();
            }
            SQLiteDatabase db = databaseManager.openConnection();

            String selection;

            if(isId)
                selection= SQLiteContract.ContactsContract.COLUMN_USER_ID + " = ?";
            else
                selection= SQLiteContract.ContactsContract.COLUMN_USERNAME + " = ?";
            String[] selectionArgs = {userId};
            String[] columns = {
                    SQLiteContract.ContactsContract.COLUMN_CONTACT_NAME,
                    SQLiteContract.ContactsContract.COLUMN_COUNTRY_CODE,
                    SQLiteContract.ContactsContract.COLUMN_PHONE_NUMBER,
                    SQLiteContract.ContactsContract.COLUMN_IS_ADDED,
                    SQLiteContract.ContactsContract.COLUMN_IS_REGISTERED,
                    SQLiteContract.ContactsContract.COLUMN_USERNAME,
                    SQLiteContract.ContactsContract.COLUMN_USER_ID
            };

            try {
                Cursor cursor = db.query(SQLiteContract.ContactsContract.TABLE_NAME, columns, selection, selectionArgs, null, null, null);

                if(cursor.getCount()<=0) {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } else {
                    cursor.moveToFirst();
                    String contactName = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_CONTACT_NAME));
                    String countryCode = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_COUNTRY_CODE));
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_PHONE_NUMBER));
                    String username = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_USERNAME));
                    boolean isRegistered = (cursor.getInt(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_IS_REGISTERED)) == 1);
                    boolean isAdded = (cursor.getInt(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_IS_ADDED)) == 1);
                    //String userId = cursor.getString(cursor.getColumnIndex(ContactsContract.COLUMN_USER_ID));

                    ContactResult contactResult = new ContactResult(countryCode, phoneNumber, contactName);
                    contactResult.setAdded(isAdded);
                    contactResult.setRegistered(isRegistered);
                    contactResult.setUsername(username);
                    contactResult.setUserId(userId);

                    cursor.close();
                    subscriber.onNext(contactResult);
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

    public Observable<ContactResult> update(ContactResult contactResult) {
        return Observable.create(subscriber -> {
            SQLiteDatabase db = databaseManager.openConnection();
            ContentValues values = new ContentValues();
            if(contactResult.isAdded())
                values.put(SQLiteContract.ContactsContract.COLUMN_IS_ADDED, 1);
            db.update(SQLiteContract.ContactsContract.TABLE_NAME, values, SQLiteContract.ContactsContract.COLUMN_USERNAME+"='"+contactResult.getUsername()+"'", null);
            subscriber.onNext(contactResult);
            subscriber.onCompleted();
            databaseManager.closeConnection();
        });
    }
}
