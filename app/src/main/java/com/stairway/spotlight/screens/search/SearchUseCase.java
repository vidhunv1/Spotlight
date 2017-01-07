package com.stairway.spotlight.screens.search;

import com.stairway.data.config.Logger;
import com.stairway.data.source.contacts.ContactResult;
import com.stairway.data.source.contacts.ContactStore;
import com.stairway.data.source.message.MessageResult;
import com.stairway.data.source.message.MessageStore;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 17/12/16.
 */

public class SearchUseCase {
    private ContactStore contactStore;
    private MessageStore messageStore;

    @Inject
    public SearchUseCase(ContactStore contactStore, MessageStore messageStore) {
        this.contactStore = contactStore;
        this.messageStore = messageStore;
    }

    public Observable<SearchModel> execute(String searchTerm) {
        return Observable.create(subscriber -> {
            contactStore.getContacts(searchTerm)
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
                            List<MessagesModel> messagesModelList = new ArrayList<>();
                            for (ContactResult contactResult : contactResults) {
                                if(contactResult.isAdded())
                                    contactsModelList.add(new ContactsModel(contactResult.getContactName(), contactResult.getUserId(), contactResult.getUsername()));
                            }
                            messageStore.searchMessages(searchTerm).subscribe(new Subscriber<List<MessageResult>>() {
                                @Override
                                public void onCompleted() {}
                                @Override
                                public void onError(Throwable e) {}
                                @Override
                                public void onNext(List<MessageResult> messageResults) {
                                    for (MessageResult messageResult : messageResults)
                                        messagesModelList.add(new MessagesModel(messageResult.getChatId(), messageResult.getName(), messageResult.getMessage(), messageResult.getTime()));

                                    SearchModel searchModel = new SearchModel(searchTerm, contactsModelList, messagesModelList);
                                    Logger.d(this, searchModel.toString());
                                    subscriber.onNext(searchModel);
                                    subscriber.onCompleted();
                                }
                            });
                        }
                    });
        });
    }
}
