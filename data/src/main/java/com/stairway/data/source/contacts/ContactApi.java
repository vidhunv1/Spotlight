package com.stairway.data.source.contacts;

import com.stairway.data.config.ApiManager;
import com.stairway.data.config.Logger;
import com.stairway.data.source.contacts.gson_models.ContactRequest;
import com.stairway.data.source.contacts.gson_models.ContactResponse;

import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 08/12/16.
 */

public class ContactApi {
    public Observable<ContactResponse> createContact(List<ContactResult> contactResults, String authToken) {
        ContactEndpoint contactEndpoint = ApiManager.getInstance(authToken).create(ContactEndpoint.class);
        return Observable.create(subscriber -> {
            get(contactResults, 0, subscriber, contactEndpoint);
        });
    }

    public void get(List<ContactResult> contactResults, int pos, Subscriber<? super ContactResponse> subscriber, ContactEndpoint contactEndpoint){
        Logger.d(this, pos+"/"+contactResults.size());
        ContactResult contactResult = contactResults.get(pos);
        contactEndpoint.createContact(new ContactRequest(contactResult.getPhoneNumber(), contactResult.getCountryCode(), contactResult.getDisplayName()))
            .subscribe(new Subscriber<ContactResponse>() {
                @Override
                public void onCompleted() {}
                @Override
                public void onError(Throwable e) {}

                @Override
                public void onNext(ContactResponse contactResponse) {
                    subscriber.onNext(contactResponse);
                    if((pos+1)<contactResults.size()) {
                        get(contactResults, pos + 1, subscriber, contactEndpoint);
                        return;
                    }
                    subscriber.onCompleted();
                }
            });
    }
}
