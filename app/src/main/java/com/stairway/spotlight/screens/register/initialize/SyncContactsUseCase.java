package com.stairway.spotlight.screens.register.initialize;

import com.stairway.data.config.Logger;
import com.stairway.data.source.contacts.ContactApi;
import com.stairway.data.source.contacts.ContactContent;
import com.stairway.data.source.contacts.ContactResult;
import com.stairway.data.source.contacts.ContactStore;
import com.stairway.data.source.contacts.gson_models.Contact;
import com.stairway.data.source.contacts.gson_models.ContactResponse;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 09/12/16.
 */

public class SyncContactsUseCase {
    private ContactApi contactApi;
    private ContactContent contactContent;
    private ContactStore contactStore;

    @Inject
    public SyncContactsUseCase(ContactApi contactApi, ContactContent contactContent, ContactStore contactStore) {
        this.contactApi = contactApi;
        this.contactContent = contactContent;
        this.contactStore = contactStore;
    }

    public Observable<Boolean> execute(String authToken) {
        Observable<Boolean> sync = Observable.create(subscriber -> {
            contactContent.getContacts()
                    .subscribe(new Subscriber<List<ContactResult>>() {
                        @Override
                        public void onCompleted() {}
                        @Override
                        public void onError(Throwable e) {}

                        @Override
                        public void onNext(List<ContactResult> contactResults) {
                            contactApi.createContact(contactResults, authToken)
                                    .subscribe(new Subscriber<ContactResponse>() {
                                        @Override
                                        public void onCompleted() {
                                            subscriber.onNext(true);
                                            subscriber.onCompleted();
                                        }
                                        @Override
                                        public void onError(Throwable e) {
                                            subscriber.onError(e);
                                        }

                                        @Override
                                        public void onNext(ContactResponse contactResponse) {
                                            Contact contact = contactResponse.getContact();
                                            ContactResult contactResult = new ContactResult(contact.getId(), contact.getCountryCode(), contact.getPhone(), contact.getName());
                                            contactResult.setUsername(contact.getUsername());
                                            contactResult.setRegistered(contact.isRegistered());

                                            contactStore.storeContact(contactResult)
                                                    .subscribe(new Subscriber<ContactResult>() {
                                                        @Override
                                                        public void onCompleted() {}
                                                        @Override
                                                        public void onError(Throwable e) {}

                                                        @Override
                                                        public void onNext(ContactResult contactResult) {}
                                                    });
                                        }
                                    });
                        }
                    });
        });
        return sync;
    }
}
