package com.stairway.spotlight.screens.search;

import com.stairway.data.source.contacts.ContactResult;
import com.stairway.data.source.contacts.ContactStore;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 17/12/16.
 */

public class SearchContactsUseCase {
    private ContactStore contactStore;

    @Inject
    public SearchContactsUseCase(ContactStore contactStore) {
        this.contactStore = contactStore;
    }

    public Observable<List<ContactsModel>> execute(String name) {
        return Observable.create(subscriber -> {
            contactStore.getContacts(name)
                    .subscribe(new Subscriber<List<ContactResult>>() {
                        @Override
                        public void onCompleted() {
                            subscriber.onCompleted();
                        }

                        @Override
                        public void onError(Throwable e) {}

                        @Override
                        public void onNext(List<ContactResult> contactResults) {
                            List<ContactsModel> contactsModelList = new ArrayList<>(contactResults.size());
                            for (ContactResult contactResult : contactResults) {
                                if(contactResult.isAdded())
                                    contactsModelList.add(new ContactsModel(contactResult.getContactName(), contactResult.getUserId(), contactResult.getUsername()));
                            }
                            subscriber.onNext(contactsModelList);
                            subscriber.onCompleted();
                        }
                    });
        });
    }
}
