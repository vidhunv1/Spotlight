package com.stairway.spotlight.screens.home;

import com.stairway.data.config.Logger;
import com.stairway.data.source.contacts.ContactResult;
import com.stairway.data.source.contacts.ContactStore;
import com.stairway.data.source.message.MessageResult;
import com.stairway.data.source.message.MessageStore;
import com.stairway.spotlight.screens.home.ChatListItemModel;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 11/10/16.
 */
public class GetChatsUseCase {
    private MessageStore messageStore;
    private ContactStore contactStore;

    @Inject
    public GetChatsUseCase(MessageStore messageStore, ContactStore contactStore) {
        this.messageStore = messageStore;
        this.contactStore = contactStore;
    }

    public Observable<List<ChatListItemModel>> execute() {
        Observable<List<ChatListItemModel>> getContacts = Observable.create(subscriber -> {
            messageStore.getChatList()
                    .subscribe(new Subscriber<List<MessageResult>>() {
                        @Override
                        public void onCompleted() {
                            subscriber.onCompleted();
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(List<MessageResult> messageResults) {
                            List<ChatListItemModel> chatListItemModels = new ArrayList<>(messageResults.size());
                            for (MessageResult messageResult : messageResults) {
                                Logger.d(this, "MessageRslt: - "+messageResult.toString());
                                contactStore.getContactByUserName(messageResult.getChatId()).subscribe(new Subscriber<ContactResult>() {
                                    @Override
                                    public void onCompleted() {}
                                    @Override
                                    public void onError(Throwable e) {}

                                    @Override
                                    public void onNext(ContactResult contactResult) {
                                        String name;
                                        if(contactResult!=null)
                                            name = contactResult.getDisplayName();
                                        else {
                                            //TODO: get user details from server
                                            name = messageResult.getChatId();
                                        }

                                        chatListItemModels.add(new ChatListItemModel(
                                                messageResult.getChatId(),
                                                name,
                                                messageResult.getMessage(),
                                                messageResult.getTime(),
                                                messageResult.getUnSeenCount()));
                                    }
                                });
                            }



                            subscriber.onNext(chatListItemModels);
                            subscriber.onCompleted();
                        }
                    });
        });
        return getContacts;
    }
}
