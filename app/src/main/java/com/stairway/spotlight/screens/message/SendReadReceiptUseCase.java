package com.stairway.spotlight.screens.message;

import com.stairway.data.source.message.MessageApi;
import com.stairway.data.source.message.MessageResult;
import com.stairway.data.source.message.MessageStore;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 29/11/16.
 */

public class SendReadReceiptUseCase {
    private MessageApi messageApi;
    private MessageStore messageStore;

    @Inject
    public SendReadReceiptUseCase(MessageApi messageApi, MessageStore messageStore) {
        this.messageApi = messageApi;
        this.messageStore = messageStore;
    }

    public Observable<Boolean> execute(MessageResult messageResult){
        return Observable.create(subscriber -> {
            sendReadReceipt(messageResult, subscriber);
        });
    }

    public Observable<Boolean> execute(String chatId){
        return Observable.create(subscriber -> {
            messageStore.getLastUnsentReceipt(chatId).subscribe(new Subscriber<MessageResult>() {
                @Override
                public void onCompleted() {}
                @Override
                public void onError(Throwable e) {}

                @Override
                public void onNext(MessageResult messageResult) {
                    sendReadReceipt(messageResult, subscriber);
                }
            });
        });
    }

    private void sendReadReceipt(MessageResult messageResult, Subscriber <? super Boolean> subscriber) {
        messageApi.sendReadReceipt(messageResult).subscribe(new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {}
            @Override
            public void onError(Throwable e) {}

            @Override
            public void onNext(Boolean aBoolean) {
                messageStore.updateReadReceiptSent(messageResult)
                        .subscribe(new Subscriber<MessageResult>() {
                            @Override
                            public void onCompleted() {}
                            @Override
                            public void onError(Throwable e) {}
                            @Override
                            public void onNext(MessageResult messageResult) {
                                subscriber.onNext(true);
                            }
                        });
            }
        });
    }
}
