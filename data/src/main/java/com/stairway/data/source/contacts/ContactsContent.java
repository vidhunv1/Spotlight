package com.stairway.data.source.contacts;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.stairway.data.local.core.SQLiteContract;
import com.stairway.data.manager.Logger;
import com.stairway.data.source.message.MessageResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;

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
    public Observable<List<ContactsResult>> getContacts() {
        Logger.d("ContactsContent: ");

        Observable<List<ContactsResult>> getContacts = Observable.create(
                subscriber -> {
                    PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
                    String defaultCountryIso = context.getResources().getConfiguration().locale.getCountry();
                    String contactName, contactNumber, contactNumberFormatted, contactId;
                    Phonenumber.PhoneNumber contactNumberFormat;

                    ContentResolver cr = context.getContentResolver();
                    Set<ContactsResult> contactsResults = new HashSet<>();
                    try{

                        String sortOrder = CONTACT_NAME  + " COLLATE LOCALIZED ASC";
                        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, sortOrder);
                        Cursor pCur;

                        Logger.d("CURSOR: "+cursor.getCount());

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
                                    contactNumberFormat = phoneNumberUtil.parse(contactNumber, defaultCountryIso);

                                    if(phoneNumberUtil.isValidNumber(contactNumberFormat)) {
                                        contactNumberFormatted = "+" + contactNumberFormat.getCountryCode() + " " + String.valueOf(contactNumberFormat.getNationalNumber());

                                        contactsResults.add(new ContactsResult(Integer.valueOf(contactId), contactNumberFormatted, contactName));
                                    }
                                    break;
                                }
                                pCur.close();
                            }
                            cursor.moveToNext();
                        }

                        List<ContactsResult> res= new ArrayList<ContactsResult>();
                        res.addAll(contactsResults);
                        Collections.sort(res, new Comparator<ContactsResult>() {
                            @Override
                            public int compare(ContactsResult contactsResult, ContactsResult t1) {
                                return contactsResult.getDisplayName().compareTo(t1.getDisplayName());
                            }
                        });

                        subscriber.onNext(res);

                        cursor.close();
                        subscriber.onCompleted();

                    } catch (Exception e) {
                        Logger.e("ContactsContent sqlite error"+e.getMessage());
                        subscriber.onError(e);
                        subscriber.onCompleted();
                    }
                });
        return getContacts;
    }
}