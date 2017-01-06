package com.stairway.spotlight.screens.message;

import com.stairway.data.source.message.MessageApi;
import com.stairway.data.source.message.MessageResult;
import com.stairway.data.source.message.MessageStore;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 08/08/16.
 */
public class SendMessageUseCase {
    private MessageApi messageApi;
    private MessageStore messageStore;

    @Inject
    public SendMessageUseCase(MessageApi messageApi, MessageStore messageStore) {
        this.messageApi = messageApi;
        this.messageStore = messageStore;
    }

    public Observable<MessageResult> execute(MessageResult message) {
        Observable<MessageResult> sendMessage = Observable.create(subscriber -> {

            messageStore.getUnsentMessages(message.getChatId())
                    .doOnNext(unsentMessageResult -> {
                        messageApi.sendMessage(unsentMessageResult)
                                .doOnNext(messageResult1 -> {
                                    messageStore.updateMessage(unsentMessageResult)
                                            .subscribe(new Subscriber<MessageResult>() {
                                                @Override
                                                public void onCompleted() {
                                                    subscriber.onCompleted();
                                                }
                                                @Override
                                                public void onError(Throwable e) {}

                                                @Override
                                                public void onNext(MessageResult sentMessageResult) {
                                                    subscriber.onNext(sentMessageResult);
                                                }
                                            });
                                }).subscribe();
                    }).subscribe();
        });

        return sendMessage;
    }
}
