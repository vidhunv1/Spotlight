package com.stairway.spotlight.screens.message;

import com.stairway.data.source.message.MessageApi;
import com.stairway.data.source.message.MessageResult;
import com.stairway.data.source.message.MessageStore;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 06/08/16.
 */
public class StoreMessageUseCase {
    private MessageStore messageStore;
    private MessageApi messageApi;

    @Inject
    public StoreMessageUseCase(MessageApi messageApi, MessageStore messageStore) {
        this.messageApi = messageApi;
        this.messageStore = messageStore;
    }

    public Observable<MessageResult> execute(MessageResult message) {
        Observable<MessageResult> storeMessage = Observable.create(subscriber -> {
            messageStore.storeMessage(message).subscribe(new Subscriber<MessageResult>() {
                @Override
                public void onCompleted() {
                    subscriber.onCompleted();
                }

                @Override
                public void onError(Throwable e) {
                    subscriber.onError(e);
                }

                @Override
                public void onNext(MessageResult result) {
                    subscriber.onNext(result);
                    subscriber.onCompleted();
                }
            });
        });

        return storeMessage;
    }

}
