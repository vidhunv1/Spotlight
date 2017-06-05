package com.chat.ichat.db;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;

import com.chat.ichat.api.user._User;
import com.chat.ichat.db.core.DatabaseManager;
import com.chat.ichat.db.core.SQLiteContract;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.chat.ichat.core.Logger;
import com.chat.ichat.models.ContactResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vidhun on 01/09/16.
 */
public class ContactsContent {
    private static final String CONTACT_NAME = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY;
    private static final String CONTACT_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    private static final String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
    private static final String CONTACT_ID = ContactsContract.Contacts._ID;

    private Context context;

    public ContactsContent(Context context) {
        this.context = context;
    }

    /*
    Get list of all contacts from phone. Formatted contactNumber, displayName, contactId.
    Contacts are sorted in ASC.
     */
    public Observable<List<ContactResult>> getContacts() {
        Observable<List<ContactResult>> getContacts = Observable.create(
                subscriber -> {
                    getCache()
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<List<ContactResult>>() {
                                @Override
                                public void onCompleted() {}
                                @Override
                                public void onError(Throwable e) {
                                    e.printStackTrace();
                                    subscriber.onError(e);
                                }

                                @Override
                                public void onNext(List<ContactResult> contactResults) {
                                    if(contactResults==null || contactResults.size()==0) {
                                        getPhoneBookContacts()
                                                .subscribeOn(Schedulers.newThread())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Subscriber<List<ContactResult>>() {
                                                    @Override
                                                    public void onCompleted() {
                                                        subscriber.onCompleted();
                                                    }

                                                    @Override
                                                    public void onError(Throwable e) {
                                                        e.printStackTrace();
                                                        subscriber.onError(e);
                                                    }

                                                    @Override
                                                    public void onNext(List<ContactResult> contactResults) {
                                                        subscriber.onNext(contactResults);
                                                    }
                                                });
                                    } else {
                                        subscriber.onNext(contactResults);
                                        subscriber.onCompleted();
                                    }
                                }
                            });
                });
        return getContacts;
    }

    public Observable<List<ContactResult>> getPhoneBookContacts() {
        Observable<List<ContactResult>> getContacts = Observable.create(
                subscriber -> {
                    PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
                    String defaultCountryIso = context.getResources().getConfiguration().locale.getCountry();
                    String contactName, contactNumber, contactNumberFormatted, contactId;
                    Phonenumber.PhoneNumber contactNumberFormat;

                    ContentResolver cr = context.getContentResolver();
                    Set<ContactResult> contactResults = new HashSet<>();
                    try{

                        String sortOrder = CONTACT_NAME  + " COLLATE LOCALIZED ASC";
                        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, sortOrder);
                        Cursor pCur;

                        Logger.d(this, "CURSOR: "+cursor.getCount());

                        cursor.moveToFirst();
                        while(!cursor.isAfterLast()) {
                            contactId = cursor.getString(cursor.getColumnIndex(CONTACT_ID));

                            if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER))) > 0)
                            {
                                pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",new String[]{ contactId }, null);
                                while (pCur.moveToNext())
                                {
                                    contactName = pCur.getString(pCur.getColumnIndex(CONTACT_NAME));
                                    contactNumber = pCur.getString(pCur.getColumnIndex(CONTACT_NUMBER));

                                    if(contactNumber.length()<10)
                                        continue;

                                    contactNumberFormat = phoneNumberUtil.parse(contactNumber, defaultCountryIso);
                                    if(phoneNumberUtil.isValidNumber(contactNumberFormat)) {
                                        String countryCode = Integer.toString(contactNumberFormat.getCountryCode());
                                        String mobileNumber = Long.toString(contactNumberFormat.getNationalNumber());
                                        contactNumberFormatted = contactNumberFormat.getCountryCode() + "-" + String.valueOf(contactNumberFormat.getNationalNumber());
                                        ContactResult contactResult = new ContactResult(countryCode, mobileNumber, contactName);
                                        contactResults.add(contactResult);
                                    }
                                    break;
                                }
                                pCur.close();
                            }
                            cursor.moveToNext();
                        }

                        List<ContactResult> res= new ArrayList<ContactResult>();
                        res.addAll(contactResults);
                        Collections.sort(res, new Comparator<ContactResult>() {
                            @Override
                            public int compare(ContactResult contactsResult, ContactResult t1) {
                                return contactsResult.getContactName().compareTo(t1.getContactName());
                            }
                        });
                        cursor.close();
                        storeCache(res)
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<Boolean>() {
                                    @Override
                                    public void onCompleted() {}

                                    @Override
                                    public void onError(Throwable e) {
                                        subscriber.onNext(res);
                                        subscriber.onCompleted();
                                    }

                                    @Override
                                    public void onNext(Boolean aBoolean) {
                                        subscriber.onNext(res);
                                        subscriber.onCompleted();
                                    }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Logger.e(this, "sqlite error"+e.getMessage());
                        subscriber.onError(e);
                        subscriber.onCompleted();
                    }
                });
        return getContacts;
    }

    public Observable<Boolean> storeCache(List<ContactResult> contactResults) {
        return Observable.create(subscriber -> {
            SQLiteDatabase db = DatabaseManager.getInstance().openConnection();
            ContentValues values = new ContentValues();

            for (ContactResult contactResult : contactResults) {
                values.put(SQLiteContract.PhoneContactsCache.COLUMN_PHONE_NUMBER, contactResult.getPhoneNumber());
                values.put(SQLiteContract.PhoneContactsCache.COLUMN_CONTACT_NAME, contactResult.getContactName());
                values.put(SQLiteContract.PhoneContactsCache.COLUMN_COUNTRY_CODE, contactResult.getCountryCode());
                values.put(SQLiteContract.PhoneContactsCache.COLUMN_IS_REGISTERED, contactResult.isRegistered());

                long rowId = db.insert(SQLiteContract.PhoneContactsCache.TABLE_NAME, null, values);
            }
            subscriber.onNext(true);
            subscriber.onCompleted();
            DatabaseManager.getInstance().closeConnection();
        });
    }

    public Observable<List<ContactResult>> getCache() {
        return Observable.create(subscriber -> {
            SQLiteDatabase db = DatabaseManager.getInstance().openConnection();
            List<ContactResult> result = new ArrayList<>();

            String selection = "";
            String[] selectionArgs = {};
            String[] columns = {
                    SQLiteContract.PhoneContactsCache.COLUMN_CONTACT_NAME,
                    SQLiteContract.PhoneContactsCache.COLUMN_COUNTRY_CODE,
                    SQLiteContract.PhoneContactsCache.COLUMN_PHONE_NUMBER,
                    SQLiteContract.PhoneContactsCache.COLUMN_IS_REGISTERED,
            };

            try {
                Cursor cursor = db.query(SQLiteContract.PhoneContactsCache.TABLE_NAME, columns, selection, selectionArgs, null, null, SQLiteContract.PhoneContactsCache.COLUMN_CONTACT_NAME+" COLLATE NOCASE ASC");
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    String contactName = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_CONTACT_NAME));
                    String countryCode = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_COUNTRY_CODE));
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(SQLiteContract.ContactsContract.COLUMN_PHONE_NUMBER));
                    boolean isRegistered = cursor.getInt(cursor.getColumnIndex(SQLiteContract.PhoneContactsCache.COLUMN_IS_REGISTERED)) == 1;

                    ContactResult contactResult = new ContactResult(countryCode, phoneNumber, contactName);
                    contactResult.setRegistered(isRegistered);
                    result.add(contactResult);

                    cursor.moveToNext();
                }
                cursor.close();
                subscriber.onNext(result);
                subscriber.onCompleted();

                DatabaseManager.getInstance().closeConnection();
            } catch (Exception e) {
                Logger.e(this, "ContactStore sqlite error"+e.getMessage());
                DatabaseManager.getInstance().closeConnection();
                subscriber.onError(e);
            }
        });
    }
}
