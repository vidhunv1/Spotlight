package com.stairway.data.source.contacts;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.stairway.data.config.Logger;
import com.stairway.data.db.core.DatabaseManager;
import com.stairway.data.db.core.SQLiteContract;
import com.stairway.data.db.core.SQLiteContract.ContactsContract;

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

    public Observable<ContactResult> storeContact(ContactResult contactResult){
        return Observable.create(subscriber -> {
            SQLiteDatabase db = databaseManager.openConnection();
            ContentValues values = new ContentValues();
            values.put(ContactsContract.COLUMN_CONTACT_ID, contactResult.getContactId());
            values.put(ContactsContract.COLUMN_CONTACT_NAME, contactResult.getDisplayName());
            values.put(ContactsContract.COLUMN_PHONE_NUMBER, contactResult.getPhoneNumber());
            values.put(ContactsContract.COLUMN_COUNTRY_CODE, contactResult.getCountryCode());
            values.put(ContactsContract.COLUMN_USERNAME, contactResult.getUsername());
            values.put(ContactsContract.COLUMN_USER_ID, contactResult.getUserId());
            values.put(ContactsContract.COLUMN_IS_REGISTERED, contactResult.isRegistered());

            long rowId = db.insert(ContactsContract.TABLE_NAME, null, values);
            subscriber.onNext(contactResult);
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
                    ContactsContract.COLUMN_CONTACT_ID,
                    ContactsContract.COLUMN_CONTACT_NAME,
                    ContactsContract.COLUMN_COUNTRY_CODE,
                    ContactsContract.COLUMN_PHONE_NUMBER,
                    ContactsContract.COLUMN_IS_ADDED,
                    ContactsContract.COLUMN_IS_REGISTERED,
                    ContactsContract.COLUMN_USERNAME,
                    ContactsContract.COLUMN_USER_ID
            };

            try {
                Cursor cursor = db.query(SQLiteContract.ContactsContract.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.COLUMN_CONTACT_ID));
                    String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.COLUMN_CONTACT_NAME));
                    String countryCode = cursor.getString(cursor.getColumnIndex(ContactsContract.COLUMN_COUNTRY_CODE));
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.COLUMN_PHONE_NUMBER));
                    String username = cursor.getString(cursor.getColumnIndex(ContactsContract.COLUMN_USERNAME));
                    String userId = cursor.getString(cursor.getColumnIndex(ContactsContract.COLUMN_USER_ID));
                    boolean isRegistered = (cursor.getInt(cursor.getColumnIndex(ContactsContract.COLUMN_IS_REGISTERED)) == 1);
                    boolean isAdded = (cursor.getInt(cursor.getColumnIndex(ContactsContract.COLUMN_IS_ADDED)) == 1);

                    ContactResult contactResult = new ContactResult(contactId, countryCode, phoneNumber, contactName);
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
                Logger.e("ContactStore sqlite error"+e.getMessage());
                databaseManager.closeConnection();
                subscriber.onError(e);
            }
        });
    }

    public Observable<List<ContactResult>> getContacts(String name) {
        Logger.d(name+" contactStore");
        return Observable.create(subscriber -> {
            SQLiteDatabase db = databaseManager.openConnection();
            List<ContactResult> result = new ArrayList<>();

            String selection = ContactsContract.COLUMN_CONTACT_NAME + " LIKE ? OR "+ContactsContract.COLUMN_CONTACT_NAME+" LIKE ? ";
            String[] selectionArgs = new String[2];
            if(name.length()==0) {
                subscriber.onNext(result);
                subscriber.onCompleted();
            }
            else {
                selectionArgs[0] = name + "%";
                selectionArgs[1] = "% "+name+"%";
            }
            String[] columns = {
                    ContactsContract.COLUMN_CONTACT_ID,
                    ContactsContract.COLUMN_CONTACT_NAME,
                    ContactsContract.COLUMN_COUNTRY_CODE,
                    ContactsContract.COLUMN_PHONE_NUMBER,
                    ContactsContract.COLUMN_IS_ADDED,
                    ContactsContract.COLUMN_IS_REGISTERED,
                    ContactsContract.COLUMN_USERNAME,
                    ContactsContract.COLUMN_USER_ID
            };

            try {
                Cursor cursor = db.query(SQLiteContract.ContactsContract.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
                cursor.moveToFirst();
                Logger.d("count ");

                while (!cursor.isAfterLast()) {
                    String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.COLUMN_CONTACT_ID));
                    String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.COLUMN_CONTACT_NAME));
                    String countryCode = cursor.getString(cursor.getColumnIndex(ContactsContract.COLUMN_COUNTRY_CODE));
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.COLUMN_PHONE_NUMBER));
                    String username = cursor.getString(cursor.getColumnIndex(ContactsContract.COLUMN_USERNAME));
                    String userId = cursor.getString(cursor.getColumnIndex(ContactsContract.COLUMN_USER_ID));
                    boolean isRegistered = (cursor.getInt(cursor.getColumnIndex(ContactsContract.COLUMN_IS_REGISTERED)) == 1);
                    boolean isAdded = (cursor.getInt(cursor.getColumnIndex(ContactsContract.COLUMN_IS_ADDED)) == 1);

                    ContactResult contactResult = new ContactResult(contactId, countryCode, phoneNumber, contactName);
                    contactResult.setAdded(isAdded);
                    contactResult.setRegistered(isRegistered);
                    contactResult.setUsername(username);
                    contactResult.setUserId(userId);
                    result.add(contactResult);

                    Logger.d("Contacts like: "+contactResult.toString());

                    cursor.moveToNext();
                }
                cursor.close();
                subscriber.onNext(result);
                subscriber.onCompleted();

                databaseManager.closeConnection();
            } catch (Exception e) {
                Logger.e("ContactStore sqlite error"+e.getMessage());
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
                values.put(ContactsContract.COLUMN_IS_ADDED, 1);
            db.update(ContactsContract.TABLE_NAME, values, ContactsContract.COLUMN_USERNAME+"='"+contactResult.getUsername()+"'", null);
            subscriber.onNext(contactResult);
            subscriber.onCompleted();
            databaseManager.closeConnection();
        });
    }
}