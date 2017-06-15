package com.chat.ichat.screens.message;

import com.chat.ichat.MessageController;
import com.chat.ichat.db.MessageStore;
import com.chat.ichat.models.MessageResult;

import rx.Observable;
import rx.Subscriber;
/**
 * Created by vidhun on 08/08/16.
 */
public class SendMessageUseCase {
    private MessageController messageApi;
    private MessageStore messageStore;

    public SendMessageUseCase(MessageController messageApi, MessageStore messageStore) {
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