package com.stairway.spotlight.screens.message;

import com.stairway.data.source.message.MessageApi;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smackx.chatstates.ChatState;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 16/11/16.
 */

public class SendChatStateUseCase {
    private MessageApi messageApi;
    public static ChatState CHAT_TYPING = ChatState.composing;
    public static ChatState CHAT_PAUSED = ChatState.paused;

    @Inject
    public SendChatStateUseCase(MessageApi messageApi) {
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
