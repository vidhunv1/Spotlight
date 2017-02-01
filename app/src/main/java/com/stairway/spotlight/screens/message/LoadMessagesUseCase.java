package com.stairway.spotlight.screens.message;

import com.stairway.spotlight.MessageController;
import com.stairway.spotlight.db.MessageStore;
import com.stairway.spotlight.models.MessageResult;

import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 06/08/16.
 */
public class LoadMessagesUseCase {
    private MessageStore messageStore;
    private MessageController messageApi;

    public LoadMessagesUseCase(MessageController messageApi, MessageStore messageStore) {
        this.messageApi = messageApi;
        this.messageStore = messageStore;
    }

    public Observable<List<MessageResult>> execute(String chatId) {

        Observable<List<MessageResult>> getMessages = Observable.create(subscriber -> {
            messageStore.getMessages(chatId).subscribe(new Subscriber<List<MessageResult>>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(List<MessageResult> messageResults) {
                    subscriber.onNext(messageResults);
                }
            });
        });

        return getMessages;
    }
}