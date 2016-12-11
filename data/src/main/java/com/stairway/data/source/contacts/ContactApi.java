package com.stairway.data.source.contacts;

import com.stairway.data.config.ApiManager;
import com.stairway.data.source.contacts.gson_models.ContactRequest;
import com.stairway.data.source.contacts.gson_models.ContactResponse;

import rx.Observable;

/**
 * Created by vidhun on 08/12/16.
 */

public class ContactApi {
    public Observable<ContactResponse> createContact(ContactResult contactResult, String authToken) {
        ContactEndpoint contactEndpoint = ApiManager.getInstance(authToken).create(ContactEndpoint.class);

        ContactRequest contactRequest = new ContactRequest(contactResult.getPhoneNumber(), contactResult.getCountryCode(), contactResult.getDisplayName());
        Observable<ContactResponse> createContact = contactEndpoint.createContact(contactRequest);

        return createContact;
    }
}
