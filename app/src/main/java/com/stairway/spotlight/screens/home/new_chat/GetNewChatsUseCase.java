package com.stairway.spotlight.screens.home.new_chat;

import com.stairway.data.source.contacts.ContactContent;
import com.stairway.data.source.contacts.ContactResult;
import com.stairway.data.source.contacts.ContactStore;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 01/09/16.
 */
public class GetNewChatsUseCase {
    private ContactStore contactStore;

    @Inject
    public GetNewChatsUseCase(ContactStore contactStore) {
        this.contactStore = contactStore;
    }

    public Observable<List<NewChatItemModel>> execute() {
        return Observable.create(subscriber -> {
                contactStore.getContacts()
                        .subscribe(new Subscriber<List<ContactResult>>() {
                            @Override
                            public void onCompleted() {}
                            @Override
                            public void onError(Throwable e) {}

                            @Override
                            public void onNext(List<ContactResult> contactsResults) {
                                List<NewChatItemModel> newChatItemModels = new ArrayList<NewChatItemModel>(contactsResults.size());
                                for(ContactResult contactsResult: contactsResults) {
                                    NewChatItemModel newChatItemModel = new NewChatItemModel(
                                            contactsResult.getDisplayName(),
                                            contactsResult.getUsername(),
                                            contactsResult.getUserId());

                                    if(contactsResult.isRegistered() && contactsResult.isAdded())
                                        newChatItemModels.add(newChatItemModel);
                                }

                                subscriber.onNext(newChatItemModels);
                            }
                        });
        });
    }
}
