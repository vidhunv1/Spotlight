package com.stairway.spotlight.screens.search;

import com.google.gson.JsonSyntaxException;
import com.stairway.spotlight.core.GsonProvider;
import com.stairway.spotlight.core.Logger;
import com.stairway.spotlight.db.ContactStore;
import com.stairway.spotlight.db.MessageStore;
import com.stairway.spotlight.models.ContactResult;
import com.stairway.spotlight.models.Message;
import com.stairway.spotlight.models.MessageResult;

import java.util.ArrayList;
import java.util.List;


import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 17/12/16.
 */

public class SearchUseCase {
    private ContactStore contactStore;
    private MessageStore messageStore;

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
                                    for (MessageResult messageResult : messageResults) {
                                        Message parsedMessage;
                                        try {
                                            parsedMessage = GsonProvider.getGson().fromJson(messageResult.getMessage(), Message.class);

                                            if(parsedMessage.getMessageType() == Message.MessageType.text) {
                                                if (messagesModelList.size() > 0 && messageResult.getChatId().equals(messagesModelList.get(messagesModelList.size() - 1).getContactName())) {
                                                    messagesModelList.add(new MessagesModel(messageResult.getChatId(), messagesModelList.get(messagesModelList.size() - 1).getContactName(), parsedMessage.getDisplayText(), messageResult.getTime()));
                                                } else {
                                                    contactStore.getContactByUserName(messageResult.getChatId()).subscribe(new Subscriber<ContactResult>() {
                                                        @Override
                                                        public void onCompleted() {
                                                        }

                                                        @Override
                                                        public void onError(Throwable e) {
                                                        }

                                                        @Override
                                                        public void onNext(ContactResult contactResult) {
                                                            Logger.d(this, contactResult.getContactName());
                                                            messagesModelList.add(new MessagesModel(messageResult.getChatId(), contactResult.getContactName(), parsedMessage.getDisplayText(), messageResult.getTime()));
                                                        }
                                                    });
                                                }
                                            }
                                        } catch (JsonSyntaxException e) {
                                            //TODO: Should fallback to text?
                                        }
                                    }

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
