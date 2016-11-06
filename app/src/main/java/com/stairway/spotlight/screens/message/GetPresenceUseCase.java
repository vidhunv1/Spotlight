package com.stairway.spotlight.screens.message;

import com.stairway.data.manager.Logger;
import com.stairway.data.source.message.MessageApi;

import org.jivesoftware.smack.packet.Presence;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * Created by vidhun on 06/11/16.
 */

public class GetPresenceUseCase {
    private MessageApi messageApi;

    @Inject
    public GetPresenceUseCase(MessageApi messageApi) {
        this.messageApi = messageApi;
    }

    public Observable<String> execute(String userId) {
        Observable<String> presence = Observable.create(subscriber -> {
            messageApi.getPresence(userId).subscribe(new Subscriber<Presence.Mode>() {
                @Override
                public void onCompleted() {}
                @Override
                public void onError(Throwable e) {}

                @Override
                public void onNext(Presence.Mode presence) {
                    if (presence == Presence.Mode.available) {
                        subscriber.onNext("Online");
                    } else if(presence == Presence.Mode.away) {
                        messageApi.getLastActivity(userId).subscribe(new Subscriber<Long>() {
                            @Override
                            public void onCompleted() {}
                            @Override
                            public void onError(Throwable e) {}

                            @Override
                            public void onNext(Long aLong) {
                                subscriber.onNext("Last seen " + aLong + "s");
                            }
                        });
                    }
                }
            });
        });

        return presence;
    }
}
