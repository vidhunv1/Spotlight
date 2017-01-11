package com.stairway.spotlight.screens.add_user;

import com.stairway.data.source.contacts.ContactResult;
import com.stairway.data.source.contacts.ContactStore;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 07/01/17.
 */

public class AddUserUseCase {
    private ContactStore contactStore;

    @Inject
    public AddUserUseCase(ContactStore contactStore) {
        this.contactStore = contactStore;
    }

    public Observable<ContactResult> execute(ContactResult contact) {
        return Observable.create(subscriber -> {
            contact.setAdded(true);
            contact.setRegistered(true);

            contactStore.storeContact(contact)
                    .subscribe(new Subscriber<ContactResult>() {
                        @Override
                        public void onCompleted() {}
                        @Override
                        public void onError(Throwable e) {}

                        @Override
                        public void onNext(ContactResult contactResult) {
                            subscriber.onNext(contactResult);
                            subscriber.onCompleted();
                        }
                    });
        });
    }
}
