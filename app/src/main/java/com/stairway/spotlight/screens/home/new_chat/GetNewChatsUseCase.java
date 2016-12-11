package com.stairway.spotlight.screens.home.new_chat;

import com.stairway.data.source.contacts.ContactContent;
import com.stairway.data.source.contacts.ContactResult;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 01/09/16.
 */
public class GetNewChatsUseCase {
    private ContactContent contactContent;

    @Inject
    public GetNewChatsUseCase(ContactContent contactContent) {
        this.contactContent = contactContent;
    }

    public Observable<List<NewChatItemModel>> execute() {
        Observable<List<NewChatItemModel>> getContacts = Observable.create(subscriber -> {
            contactContent.getContacts()
                    .subscribe(new Subscriber<List<ContactResult>>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onNext(List<ContactResult> contactsResults) {
                            List<NewChatItemModel> newChatItemModels = new ArrayList<NewChatItemModel>(contactsResults.size());
                            newChatItemModels.add(new NewChatItemModel("Vidhun Vinod", false, "91-9489339336", "91-9489339336"));
                            newChatItemModels.add(new NewChatItemModel("Ankit Nair", false, "91-9843578487", "91-9843578487"));
                            for(ContactResult contactsResult: contactsResults) {
                                newChatItemModels.add(new NewChatItemModel(
                                        contactsResult.getDisplayName(),
                                        false,
                                        contactsResult.getContactId(),
                                        contactsResult.getPhoneNumber()
                                ));
                            }

                            subscriber.onNext(newChatItemModels);
                        }
                    });
        });

        return getContacts;
    }
}
