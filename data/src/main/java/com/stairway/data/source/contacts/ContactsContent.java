package com.stairway.data.source.contacts;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.stairway.data.local.core.SQLiteContract;
import com.stairway.data.manager.Logger;
import com.stairway.data.source.message.MessageResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.Observable;

/**
 * Created by vidhun on 01/09/16.
 */
public class ContactsContent {
    private static final String CONTACT_NAME = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY;

    private Context context;

    public ContactsContent(Context context) {
        this.context = context;
    }

    public Observable<List<ContactsResult>> getContacts() {
        Logger.d("ContactsContent: ");

        Observable<List<ContactsResult>> getContacts = Observable.create(
                subscriber -> {
                    ContentResolver cr = context.getContentResolver();
                    List<ContactsResult> contactsResults = new ArrayList<>();
                    try{
                        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, CONTACT_NAME + " ASC");

                        cursor.moveToFirst();
                        while(!cursor.isAfterLast()) {
                            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                            if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                            {
                                Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",new String[]{ id }, null);
                                while (pCur.moveToNext())
                                {
                                    String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    String contactName = pCur.getString(pCur.getColumnIndex(CONTACT_NAME));
                                    contactsResults.add(new ContactsResult(Integer.valueOf(id), contactNumber, contactName));
                                    break;
                                }
                                pCur.close();
                            }
                            cursor.moveToNext();
                        }

                        subscriber.onNext(contactsResults);
                        cursor.close();
                        subscriber.onCompleted();

                    } catch (Exception e) {
                        Logger.e("MessageStore sqlite error");
                        subscriber.onError(e);
                        subscriber.onCompleted();
                    }
                });
        return getContacts;
    }
}