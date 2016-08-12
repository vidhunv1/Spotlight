package com.stairway.spotlight.screens.message;

import com.stairway.data.source.message.MessageApi;
import com.stairway.data.source.message.MessageResult;
import com.stairway.data.source.message.MessageStore;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 12/08/16.
 */
public class ReceiveMessagesUseCase {
    private MessageStore messageStore;
    private MessageApi messageApi;

    @Inject
    public ReceiveMessagesUseCase(MessageApi messageApi, MessageStore messageStore) {
        this.messageApi = messageApi;
        this.messageStore = messageStore;
    }

    public Observable<MessageResult> execute() {
        Observable<MessageResult> receiveMessages = Observable.create(subscriber -> {

            messageApi.receiveMessages()
                    .doOnNext(messageResult -> {
                        messageStore.storeMessage(messageResult)
                                .subscribe(new Subscriber<MessageResult>() {
                                    @Override
                                    public void onCompleted() {

                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onNext(MessageResult messageResult) {
                                        subscriber.onNext(messageResult);
                                    }
                                });
                    })
                    .subscribe();
        });

        return receiveMessages;
    }
}
