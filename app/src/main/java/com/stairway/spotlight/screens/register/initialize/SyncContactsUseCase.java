package com.stairway.spotlight.screens.register.initialize;

import com.stairway.data.source.contacts.ContactApi;
import com.stairway.data.source.contacts.ContactContent;
import com.stairway.data.source.contacts.ContactResult;
import com.stairway.data.source.contacts.ContactStore;
import com.stairway.data.source.contacts.gson_models.ContactResponse;

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
                            for(ContactResult contactsResult: contactResults) {
                                contactApi.createContact(contactsResult, authToken)
                                        .subscribe(new Subscriber<ContactResponse>() {
                                            @Override
                                            public void onCompleted() {}
                                            @Override
                                            public void onError(Throwable e) {
                                                subscriber.onError(e);
                                            }

                                            @Override
                                            public void onNext(ContactResponse contactResponse) {
                                                subscriber.onNext(true);
                                                subscriber.onCompleted();
                                            }
                                        });
                            }
                        }
                    });
        });

        return sync;
    }
}
