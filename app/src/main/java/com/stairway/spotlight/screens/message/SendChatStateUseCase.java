package com.stairway.spotlight.screens.message;

import com.stairway.spotlight.MessageController;
import org.jivesoftware.smackx.chatstates.ChatState;


import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 16/11/16.
 */

public class SendChatStateUseCase {
    private MessageController messageApi;

    public SendChatStateUseCase(MessageController messageApi) {
        this.messageApi = messageApi;
    }

    public Observable<String> execute(String chatId, ChatState chatState){
        Observable<String> state = Observable.create(subscriber -> {
            messageApi.sendChatState(chatId, chatState).subscribe(new Subscriber<Boolean>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(Boolean aBoolean) {
                    if(aBoolean)
                        subscriber.onNext("Typing");
                    else
                        subscriber.onNext("Online");

                }
            });
        });

        return state;
    }
}
