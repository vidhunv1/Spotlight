package com.stairway.spotlight.screens.home.contactlist;

import com.stairway.data.source.contacts.ContactsContent;
import com.stairway.data.source.contacts.ContactsResult;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 01/09/16.
 */
public class GetContactsUseCase {
    private ContactsContent contactsContent;

    @Inject
    public GetContactsUseCase(ContactsContent contactsContent) {
        this.contactsContent = contactsContent;
    }

    public Observable<ContactsResult> execute() {
        Observable<ContactsResult> getContacts = Observable.create(subscriber -> {
            contactsContent.getContacts().subscribe(new Subscriber<ContactsResult>() {
                @Override
                public void onCompleted() {
                    subscriber.onCompleted();
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(ContactsResult contactsResult) {
                    subscriber.onNext(contactsResult);
                }
            });
        });

        return getContacts;
    }
}
