package com.stairway.data.source.contacts;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.stairway.data.manager.Logger;

import java.util.ArrayList;
import java.util.HashMap;

import rx.Observable;

/**
 * Created by vidhun on 01/09/16.
 */
public class ContactsContent {
    private static final String CONTACT_ID = ContactsContract.Contacts._ID;
    private static final String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

    private static final String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    private static final String PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
    private static final String CONTACT_NAME = ContactsContract.PRIMARY_ACCOUNT_NAME;

    private Context context;

    public ContactsContent(Context context) {
        this.context = context;
    }

    public Observable<ContactsResult> getContacts() {

        Observable<ContactsResult> getContacts = Observable.create(subscriber -> {
            ContentResolver cr = context.getContentResolver();

            Cursor cursor = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{PHONE_NUMBER, PHONE_CONTACT_ID, CONTACT_ID, CONTACT_NAME, PHONE_NUMBER, HAS_PHONE_NUMBER},
                    HAS_PHONE_NUMBER + " > 0",
                    null,
                    null
            );

            if(cursor != null){
                if(cursor.getCount() > 0) {
                    ArrayList<ContactsResult> result = new ArrayList<>();

                    while (cursor.moveToNext()) {
                        ContactsResult contact = new ContactsResult();
                        contact.setContactId(cursor.getInt(cursor.getColumnIndex(PHONE_CONTACT_ID)));
                        contact.setPhoneNumber(cursor.getString(cursor.getColumnIndex(PHONE_NUMBER)));
                        contact.setDisplayName(cursor.getString(cursor.getColumnIndex(CONTACT_NAME)));

                        Logger.d("Contacts: "+contact.toString());

                        subscriber.onNext(contact);
                    }
                }
                cursor.close();
            }
        });

        return getContacts;
    }
}
