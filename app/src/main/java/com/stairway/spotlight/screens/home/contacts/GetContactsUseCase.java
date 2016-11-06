package com.stairway.spotlight.screens.home.contacts;

import com.stairway.data.source.contacts.ContactsContent;
import com.stairway.data.source.contacts.ContactsResult;

import java.util.ArrayList;
import java.util.List;

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

    public Observable<List<ContactListItemModel>> execute() {
        Observable<List<ContactListItemModel>> getContacts = Observable.create(subscriber -> {
            contactsContent.getContacts()
                    .subscribe(new Subscriber<List<ContactsResult>>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onNext(List<ContactsResult> contactsResults) {
                            List<ContactListItemModel> contactListItemModels = new ArrayList<ContactListItemModel>(contactsResults.size());
                            contactListItemModels.add(new ContactListItemModel("Police", true, "100", "100"));
                            contactListItemModels.add(new ContactListItemModel("Emergency", true, "911", "911"));
                            contactListItemModels.add(new ContactListItemModel("Hospital", true, "101", "101"));
                            contactListItemModels.add(new ContactListItemModel("Vidhun Vinod", false, "91-9489339336", "91-9489339336"));
                            for(ContactsResult contactsResult: contactsResults) {
                                contactListItemModels.add(new ContactListItemModel(
                                        contactsResult.getDisplayName(),
                                        false,
                                        contactsResult.getContactId(),
                                        contactsResult.getPhoneNumber()
                                ));
                            }

                            subscriber.onNext(contactListItemModels);
                        }
                    });
        });

        return getContacts;
    }
}
