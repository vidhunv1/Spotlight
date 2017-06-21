package com.chat.ichat.db;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.chat.ichat.api.user._User;
import com.chat.ichat.core.Logger;
import com.chat.ichat.db.core.DatabaseManager;
import com.chat.ichat.db.core.SQLiteContract;
import com.chat.ichat.models.ContactResult;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by vidhun on 07/12/16.
 */

public class ContactStore {
    private DatabaseManager databaseManager;

    public ContactStore() {
        databaseManager = DatabaseManager.getInstance();
    }

    private static ContactStore instance;

    public static ContactStore getInstance() {
        if (instance == null) {
            instance = new ContactStore();
        }
        return instance;
    }

    public Observable<Boolean> storeContact(ContactResult contactResult){
        List<ContactResult> contact = new ArrayList<>();
        contact.add(contactResult);
        return storeContacts(contact);
    }

    public Observable<Boolean> storeContacts(List<ContactResult> contactResults){
        Logger.d(this);
        return Observable.create(subscriber -> {
            ContentValues values = new ContentValues();
            List<Observable<Boolean>> observables = new ArrayList<>();
            for (ContactResult contactResult : contactResults) {
                observables.add(
                        Observable.create(subsc -> {
                            getContactByUserName(contactResult.getUsername())
                                    .subscribeOn(Schedulers.newThread())
                                    .subscribe(new Subscriber<ContactResult>() {
                                        @Override
                                        public void onCompleted() {}
                                        @Override
                                        public void onError(Throwable e) {
                                            e.printStackTrace();
                                        }

                                        @Override
                                        public void onNext(ContactResult cc) {
                                            SQLiteDatabase db = databaseManager.openConnection();
                                            if(cc==null) {
                                                subsc.onNext(true);
                                                int isAdded = contactResult.isAdded()?1:0;
                                                values.put(SQLiteContract.ContactsContract.COLUMN_PHONE_NUMBER, contactResult.getPhoneNumber());
                                                values.put(SQLiteContract.ContactsContract.COLUMN_CONTACT_NAME, contactResult.getContactName());
                                                values.put(SQLiteContract.ContactsContract.COLUMN_COUNTRY_CODE, contactResult.getCountryCode());
                                                values.put(SQLiteContract.ContactsContract.COLUMN_USERNAME, contactResult.getUsername());
                                                values.put(SQLiteContract.ContactsContract.COLUMN_USER_ID, contactResult.getUserId());
                                                values.put(SQLiteContract.ContactsContract.COLUMN_IS_ADDED, isAdded);

                                                String profileDp = "";
                                                if(contactResult.getProfileDP()!=null)
                                                    profileDp = contactResult.getProfileDP().replace("https://", "http://");
                                                values.put(SQLiteContract.ContactsContract.COLUMN_PROFILE_DP, profileDp);
                                                if(contactResult.getUserType()==null) {
                                                    values.put(SQLiteContract.ContactsContract.COLUMN_USER_TYPE, _User.UserType.regular.name());
                                                } else {
                                                    values.put(SQLiteContract.ContactsContract.COLUMN_USER_TYPE, contactResult.getUserType().name());
                                                }
                                                long rowId = db.insert(SQLiteContract.ContactsContract.TABLE_NAME, null, values);
                                            } else {
                                                if(contactResult.isAdded() && !cc.isAdded()) {
                                                    values.put(SQLiteContract.ContactsContract.COLUMN_IS_ADDED, 1);
                                                    db.update(SQLiteContract.ContactsContract.TABLE_NAME, values, SQLiteContract.ContactsContract.COLUMN_USERNAME + "='" + contactResult.getUsername() + "'", null);
                                                }
                                                subsc.onNext(false);
                                            }
                                            databaseManager.closeConnection();
                                        }
                                    });
                        }));
            }

            Observable.zip(observables, (i) -> "Done")
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onCompleted() {}

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            subscriber.onNext(true);
                            subscriber.onCompleted();
                        }

                        @Override
                        public void onNext(String s) {
                            subscriber.onNext(true);
                            subscriber.onCompleted();
                        }
                    });
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
                    SQLiteContract.ContactsContract.COLUMN_USERNAME,
                    SQLiteContract.ContactsContract.COLUMN_USER_ID,
                    SQLiteContract.ContactsContract.COLUMN_USER_TYPE,
                    SQLiteContract.ContactsContract.COLUMN_IS_ADDED,
                    SQLiteContract.ContactsContract.COLUMN_IS_BLOCKED,
                    SQLiteContract.ContactsContract.COLUMN_PROFILE_DP
            };

            try {
                Cursor cursor = db.query(SQLiteContract.ContactsContract.TABLE_NAME, columns, selection, selectionArgs, null, null, SQLiteContract.ContactsContract.COLUMN_CONTACT_NAME+" COLLATE NOCASE ASC");
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    String contactName = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_CONTACT_NAME));
                    String countryCode = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_COUNTRY_CODE));
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_PHONE_NUMBER));
                    String username = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_USERNAME));
                    String userId = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_USER_ID));
                    String userType = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_USER_TYPE));
                    boolean isAdded = cursor.getInt(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_IS_ADDED)) == 1;
                    boolean isBlocked = cursor.getInt(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_IS_BLOCKED)) == 1;
                    String profileDP = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_PROFILE_DP));

                    ContactResult contactResult = new ContactResult(countryCode, phoneNumber, contactName);
                    contactResult.setUsername(username);
                    contactResult.setUserId(userId);
                    contactResult.setUserType(_User.UserType.valueOf(userType));
                    contactResult.setAdded(isAdded);
                    contactResult.setBlocked(isBlocked);
                    contactResult.setProfileDP(profileDP);
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
                    SQLiteContract.ContactsContract.COLUMN_USERNAME,
                    SQLiteContract.ContactsContract.COLUMN_USER_ID,
                    SQLiteContract.ContactsContract.COLUMN_USER_TYPE,
                    SQLiteContract.ContactsContract.COLUMN_IS_ADDED,
                    SQLiteContract.ContactsContract.COLUMN_IS_BLOCKED,
                    SQLiteContract.ContactsContract.COLUMN_PROFILE_DP
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
                    String userType = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_USER_TYPE));
                    boolean isAdded = cursor.getInt(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_IS_ADDED)) == 1;
                    boolean isBlocked = cursor.getInt(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_IS_BLOCKED)) == 1;
                    String profileDP = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_PROFILE_DP));

                    ContactResult contactResult = new ContactResult(countryCode, phoneNumber, contactName);
                    contactResult.setUsername(username);
                    contactResult.setUserId(userId);
                    contactResult.setAdded(isAdded);
                    contactResult.setBlocked(isBlocked);
                    contactResult.setProfileDP(profileDP);
                    contactResult.setUserType(_User.UserType.valueOf(userType));
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

    private Observable<ContactResult> getContact(String id, boolean isId) {
        return Observable.create(subscriber -> {
            if(id == null || id.length()==0) {
                subscriber.onNext(new ContactResult());
                subscriber.onCompleted();
            }
            SQLiteDatabase db = databaseManager.openConnection();

            String selection;

            if(isId)
                selection= SQLiteContract.ContactsContract.COLUMN_USER_ID + " =? ";
            else
                selection= SQLiteContract.ContactsContract.COLUMN_USERNAME + " =? ";
            String[] selectionArgs = {id};
            String[] columns = {
                    SQLiteContract.ContactsContract.COLUMN_CONTACT_NAME,
                    SQLiteContract.ContactsContract.COLUMN_COUNTRY_CODE,
                    SQLiteContract.ContactsContract.COLUMN_PHONE_NUMBER,
                    SQLiteContract.ContactsContract.COLUMN_USERNAME,
                    SQLiteContract.ContactsContract.COLUMN_USER_ID,
                    SQLiteContract.ContactsContract.COLUMN_USER_TYPE,
                    SQLiteContract.ContactsContract.COLUMN_IS_ADDED,
                    SQLiteContract.ContactsContract.COLUMN_IS_BLOCKED,
                    SQLiteContract.ContactsContract.COLUMN_PROFILE_DP
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
                    String userId = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_USER_ID));
                    String userType = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_USER_TYPE));
                    boolean isAdded = cursor.getInt(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_IS_ADDED)) == 1;
                    boolean isBlocked = cursor.getInt(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_IS_BLOCKED)) == 1;
                    String profileDP = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_PROFILE_DP));

                    ContactResult contactResult = new ContactResult(countryCode, phoneNumber, contactName);
                    contactResult.setUsername(username);
                    contactResult.setUserId(userId);
                    contactResult.setAdded(isAdded);
                    contactResult.setBlocked(isBlocked);
                    contactResult.setUserType(_User.UserType.valueOf(userType));
                    contactResult.setProfileDP(profileDP);

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

    public Observable<ContactResult> update(ContactResult contactResult){
        List<ContactResult> contact = new ArrayList<>();
        contact.add(contactResult);
        return update(contact);
    }

    public Observable<ContactResult> update(List<ContactResult> contactResults) {
        return Observable.create(subscriber -> {
            SQLiteDatabase db = databaseManager.openConnection();
            ContentValues values = new ContentValues();

            for (ContactResult contactResult : contactResults) {
                int isAdded = contactResult.isAdded()?1:0;
                int isBlocked = contactResult.isBlocked()?1:0;
                values.put(SQLiteContract.ContactsContract.COLUMN_IS_BLOCKED, isBlocked);
                values.put(SQLiteContract.ContactsContract.COLUMN_IS_ADDED, isAdded);
                if(contactResult.getContactName()!=null && !contactResult.getContactName().isEmpty()) {
                    values.put(SQLiteContract.ContactsContract.COLUMN_CONTACT_NAME, contactResult.getContactName());
                }
                if(contactResult.getProfileDP()!=null && !contactResult.getProfileDP().isEmpty()) {
                    values.put(SQLiteContract.ContactsContract.COLUMN_PROFILE_DP, contactResult.getProfileDP());
                }
                if(contactResult.getUsername()!=null && !contactResult.getUsername().isEmpty()) {
                    db.update(SQLiteContract.ContactsContract.TABLE_NAME, values, SQLiteContract.ContactsContract.COLUMN_USERNAME + "='" + contactResult.getUsername() + "'", null);
                    subscriber.onNext(contactResult);
                    continue;
                }
                if(contactResult.getUserId()!=null && !contactResult.getUserId().isEmpty()) {
                    db.update(SQLiteContract.ContactsContract.TABLE_NAME, values, SQLiteContract.ContactsContract.COLUMN_USER_ID + "='" + contactResult.getUserId() + "'", null);
                    subscriber.onNext(contactResult);
                    continue;
                }
                if(contactResult.getProfileDP()!=null && !contactResult.getProfileDP().isEmpty()) {
                    db.update(SQLiteContract.ContactsContract.TABLE_NAME, values, SQLiteContract.ContactsContract.COLUMN_PROFILE_DP + "='" + contactResult.getProfileDP() + "'", null);
                    subscriber.onNext(contactResult);
                }
            }
            subscriber.onCompleted();
            databaseManager.closeConnection();
        });
    }

    public boolean deleteContactUsername(String username) {
        SQLiteDatabase db = databaseManager.openConnection();
        String table = SQLiteContract.ContactsContract.TABLE_NAME;
        String whereClause = SQLiteContract.ContactsContract.COLUMN_USERNAME+"=?";
        String[] whereArgs = new String[] { String.valueOf(username) };
        boolean b = db.delete(table, whereClause, whereArgs) > 0;
        databaseManager.closeConnection();
        return b;
    }

    public boolean deleteContactUserId(String userId) {
        SQLiteDatabase db = databaseManager.openConnection();
        String table = SQLiteContract.ContactsContract.TABLE_NAME;
        String whereClause = SQLiteContract.ContactsContract.COLUMN_USER_ID+"=?";
        String[] whereArgs = new String[] { String.valueOf(userId) };
        boolean b = db.delete(table, whereClause, whereArgs) > 0;
        databaseManager.closeConnection();
        return b;
    }
}
