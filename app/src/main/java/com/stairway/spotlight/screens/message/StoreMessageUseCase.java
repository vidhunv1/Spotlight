package com.stairway.spotlight.screens.message;

import com.stairway.spotlight.MessageController;
import com.stairway.spotlight.db.MessageStore;
import com.stairway.spotlight.models.MessageResult;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 06/08/16.
 */
public class StoreMessageUseCase {
    private MessageStore messageStore;
    private MessageController messageApi;

    public StoreMessageUseCase(MessageController messageApi, MessageStore messageStore) {
        this.messageApi = messageApi;
        this.messageStore = messageStore;
    }

    public Observable<MessageResult> execute(MessageResult message) {
        Observable<MessageResult> storeMessage = Observable.create(subscriber -> {
            message.setMessageStatus(MessageResult.MessageStatus.NOT_SENT);
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
