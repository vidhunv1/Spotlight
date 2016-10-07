package com.stairway.spotlight.screens.message;

import com.stairway.data.manager.Logger;
import com.stairway.data.source.message.MessageApi;
import com.stairway.data.source.message.MessageResult;
import com.stairway.data.source.message.MessageStore;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 06/08/16.
 */
public class LoadMessagesUseCase {
    private MessageStore messageStore;
    private MessageApi messageApi;

    @Inject
    public LoadMessagesUseCase(MessageApi messageApi, MessageStore messageStore) {
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