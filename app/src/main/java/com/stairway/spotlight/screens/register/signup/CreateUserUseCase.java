package com.stairway.spotlight.screens.register.signup;

import com.stairway.data.source.user.UserAuthApi;
import com.stairway.data.source.user.models.StatusResponse;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 16/10/16.
 */

public class CreateUserUseCase {
    private UserAuthApi userAuthApi;

    @Inject
    public CreateUserUseCase(UserAuthApi userAuthApi) {
        this.userAuthApi = userAuthApi;
    }

    public Observable<StatusResponse> execute(String countryCode, String mobile){
        Observable<StatusResponse> create = Observable.create(subscriber -> {
            userAuthApi.createUser(countryCode, mobile).subscribe(new Subscriber<StatusResponse>() {
                @Override
                public void onCompleted() {
                    if(!subscriber.isUnsubscribed())
                        subscriber.onCompleted();
                }

                @Override
                public void onError(Throwable e) {
                    subscriber.onError(e);
                }

                @Override
                public void onNext(StatusResponse createResponse) {
                    if(!subscriber.isUnsubscribed())
                        subscriber.onNext(createResponse);
                }
            });
        });

        return create;
    }
}
