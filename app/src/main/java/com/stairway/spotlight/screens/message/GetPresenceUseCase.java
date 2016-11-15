package com.stairway.spotlight.screens.message;

import com.stairway.data.manager.Logger;
import com.stairway.data.source.message.MessageApi;

import org.jivesoftware.smack.packet.Presence;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 06/11/16.
 */

public class GetPresenceUseCase {
    private MessageApi messageApi;

    @Inject
    public GetPresenceUseCase(MessageApi messageApi) {
        this.messageApi = messageApi;
    }

    //TODO: Buggy
    public Observable<String> execute(String userId) {
        Observable<String> presence = Observable.create(subscriber -> {
            messageApi.getLastActivity(userId).subscribe(new Subscriber<Long>() {
                @Override
                public void onCompleted() {
                    messageApi.getPresence(userId).subscribe(new Subscriber<Presence.Type>() {
                        @Override
                        public void onCompleted() {}
                        @Override
                        public void onError(Throwable e) {}

                        @Override
                        public void onNext(Presence.Type presence) {
                            if (presence == Presence.Type.available) {
                                subscriber.onNext("Online");
                            } else if(presence == Presence.Type.unavailable) {
                                messageApi.getLastActivity(userId).subscribe(new Subscriber<Long>() {
                                    @Override
                                    public void onCompleted() {}
                                    @Override
                                    public void onError(Throwable e) {}

                                    @Override
                                    public void onNext(Long secAgo) {
                                        if(secAgo == 0)
                                            subscriber.onNext("");
                                        else
                                            subscriber.onNext("Active "+secAgo+"s ago..");
                                    }
                                });
                            }
                        }
                    });
                }
                @Override
                public void onError(Throwable e) {}

                @Override
                public void onNext(Long secAgo) {
                    if(secAgo==0)
                        subscriber.onNext("Online");
                    else
                        subscriber.onNext("Active "+secAgo+"s ago..");
                }
            });

        });

        return presence;
    }
}
