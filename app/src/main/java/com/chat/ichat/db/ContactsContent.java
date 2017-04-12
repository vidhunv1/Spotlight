package com.chat.ichat.db;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

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

                        subscriber.onNext(res);

                        cursor.close();
                        subscriber.onCompleted();

                    } catch (Exception e) {
                        Logger.e(this, "sqlite error"+e.getMessage());
                        subscriber.onError(e);
                        subscriber.onCompleted();
                    }
                });
        return getContacts;
    }
}
