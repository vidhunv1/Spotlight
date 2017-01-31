package com.stairway.spotlight.screens.contacts;

import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.local.ContactStore;
import com.stairway.spotlight.models.ContactResult;

import java.util.ArrayList;
import java.util.List;


import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 13/12/16.
 */

public class GetContactsUseCase {
    private ContactStore contactStore;

    public GetContactsUseCase(ContactStore contactStore) {
        this.contactStore = contactStore;
    }

    public Observable<List<ContactItemModel>> execute() {
        Logger.d(this, "GetContactsUseCase");
        return Observable.create(subscriber -> {
            contactStore.getContacts()
                    .subscribe(new Subscriber<List<ContactResult>>() {
                        @Override
                        public void onCompleted() {
                            subscriber.onCompleted();
                        }

                        @Override
                        public void onError(Throwable e) {}

                        @Override
                        public void onNext(List<ContactResult> contactResults) {
                            List<ContactItemModel> newContactItemModels = new ArrayList<>(contactResults.size());
                            for(ContactResult contactsResult: contactResults) {
                                ContactItemModel contactItemModel = new ContactItemModel(
                                        contactsResult.getDisplayName(),
                                        contactsResult.getUsername(),
                                        contactsResult.getUserId());
                                contactItemModel.setAdded(contactsResult.isAdded());
                                contactItemModel.setRegistered(contactsResult.isRegistered());

                                if(contactsResult.isRegistered())
                                    newContactItemModels.add(contactItemModel);
                            }
                            subscriber.onNext(newContactItemModels);
                        }
                    });
        });
    }
}
