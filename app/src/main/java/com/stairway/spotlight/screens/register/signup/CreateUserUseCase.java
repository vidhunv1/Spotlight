package com.stairway.spotlight.screens.register.signup;

import com.stairway.data.source.user.UserApi;
import com.stairway.data.source.user.gson_models.StatusResponse;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vidhun on 16/10/16.
 */

public class CreateUserUseCase {
    private UserApi userApi;

    @Inject
    public CreateUserUseCase(UserApi userApi) {
        this.userApi = userApi;
    }

    public Observable<StatusResponse> execute(String countryCode, String mobile){
        Observable<StatusResponse> create = Observable.create(subscriber -> {
            userApi.createUser(countryCode, mobile).subscribe(new Subscriber<StatusResponse>() {
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
