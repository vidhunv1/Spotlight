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
public class GetMessagesUseCase {
    private MessageStore messageStore;
    private MessageApi messageApi;

    @Inject
    public GetMessagesUseCase(MessageApi messageApi, MessageStore messageStore) {
        this.messageApi = messageApi;
        this.messageStore = messageStore;
    }

    public Observable<MessageResult> execute(String chatId) {

        Observable<MessageResult> getMessages = Observable.create(subscriber -> {
            messageStore.getMessages(chatId).subscribe(new Subscriber<MessageResult>() {
                @Override
                public void onCompleted() {
                    subscriber.onCompleted();
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(MessageResult messageResults) {
                    subscriber.onNext(messageResults);
                }
            });
        });

        return getMessages;
    }
}
