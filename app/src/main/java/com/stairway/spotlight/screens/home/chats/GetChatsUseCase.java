package com.stairway.spotlight.screens.home.chats;

import com.stairway.data.source.message.MessageResult;
import com.stairway.data.source.message.MessageStore;
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
    @Inject
    public GetChatsUseCase(MessageStore messageStore) {
        this.messageStore = messageStore;
    }

    public Observable<List<ChatListItemModel>> execute() {
        Observable<List<ChatListItemModel>> getContacts = Observable.create(subscriber -> {
            messageStore.getChatList()
                    .subscribe(new Subscriber<List<MessageResult>>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(List<MessageResult> messageResults) {
                            List<ChatListItemModel> chatListItemModels = new ArrayList<>(messageResults.size());
                            for (MessageResult messageResult : messageResults) {
                                chatListItemModels.add(new ChatListItemModel(
                                        messageResult.getChatId(),
                                        messageResult.getChatId(),
                                        messageResult.getMessage(),
                                        messageResult.getTime(),
                                        1));
                            }
                            subscriber.onNext(chatListItemModels);
                        }
                    });
        });
        return getContacts;
    }
}
