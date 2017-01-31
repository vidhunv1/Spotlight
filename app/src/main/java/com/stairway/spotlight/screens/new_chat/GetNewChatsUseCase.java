package com.stairway.spotlight.screens.new_chat;

import com.stairway.spotlight.local.ContactStore;
import com.stairway.spotlight.models.ContactResult;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 01/09/16.
 */
public class GetNewChatsUseCase {
    private ContactStore contactStore;

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
