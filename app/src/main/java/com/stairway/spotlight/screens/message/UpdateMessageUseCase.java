package com.stairway.spotlight.screens.message;

import com.stairway.data.source.message.MessageResult;
import com.stairway.data.source.message.MessageStore;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 16/11/16.
 */

public class UpdateMessageUseCase {
    private MessageStore messageStore;

    @Inject
    public UpdateMessageUseCase(MessageStore messageStore) {
        this.messageStore = messageStore;
    }

    public Observable<MessageResult> execute(MessageResult messageResult){
        Observable<MessageResult> updateMessage = Observable.create(subscriber -> {
            messageStore.updateMessage(messageResult).subscribe(new Subscriber<MessageResult>() {
                @Override
                public void onCompleted() {
                    subscriber.onCompleted();
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(MessageResult messageResult) {
                    subscriber.onNext(messageResult);
                }
            });
        });

        return updateMessage;
    }
}
