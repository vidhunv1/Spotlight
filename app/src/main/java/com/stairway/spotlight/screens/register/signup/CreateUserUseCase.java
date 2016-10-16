package com.stairway.spotlight.screens.register.signup;

import com.stairway.data.source.auth.UserAuthApi;
import com.stairway.data.source.auth.models.CreateResponse;

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

    public Observable<CreateResponse> execute(String countryCode, String mobile){
        Observable<CreateResponse> create = Observable.create(subscriber -> {
            userAuthApi.createUser(countryCode, mobile).subscribe(new Subscriber<CreateResponse>() {
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
                public void onNext(CreateResponse createResponse) {
                    if(!subscriber.isUnsubscribed())
                        subscriber.onNext(createResponse);
                }
            });
        });

        return create;
    }
}
