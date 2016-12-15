package com.stairway.spotlight.screens.contacts;

import com.stairway.data.source.contacts.ContactResult;
import com.stairway.data.source.contacts.ContactStore;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 13/12/16.
 */

public class AddContactUseCase {
    private ContactStore contactStore;

    @Inject
    public AddContactUseCase(ContactStore contactStore) {
        this.contactStore = contactStore;
    }

    public Observable<Boolean> execute(String userName) {
        return Observable.create(subscriber -> {
            ContactResult contactResult = new ContactResult();
            contactResult.setUsername(userName);
            contactResult.setAdded(true);
            contactStore.update(contactResult)
                    .subscribe(new Subscriber<ContactResult>() {
                        @Override
                        public void onCompleted() {
                            subscriber.onCompleted();
                        }

                        @Override
                        public void onError(Throwable e) {}
                        @Override
                        public void onNext(ContactResult contactResult) {
                            subscriber.onNext(true);
                        }
                    });
        });
    }
}
