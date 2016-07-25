package com.stairway.spotlight.screens.register.verifyotp;

import com.stairway.data.manager.Logger;
import com.stairway.data.source.auth.UserAuthApi;
import com.stairway.data.source.auth.UserSessionResult;
import com.stairway.data.source.auth.UserSessionStore;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vidhun on 25/07/16.
 */
public class RegisterUseCase {
    private UserSessionStore userSessionStore;
    private UserAuthApi userAuthApi;

    @Inject
    public RegisterUseCase(UserAuthApi userAuthApi, UserSessionStore userSessionStore) {
        this.userAuthApi = userAuthApi;
        this.userSessionStore = userSessionStore;
    }

    public Observable<UserSessionResult> execute(String mobile, String otp) {
        Observable<UserSessionResult> register = Observable.create( subscriber -> {
            userAuthApi.registerUser(mobile, otp).subscribe(new Subscriber<UserSessionResult>() {
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
                public void onNext(UserSessionResult userSessionResult) {
                    if(!subscriber.isUnsubscribed()) {
                        storeToken(userSessionResult);
                        subscriber.onNext(userSessionResult);
                    }
                }
            });
        });

        return register.subscribeOn(Schedulers.newThread());
    }

    /*
    Store the Token after authentication.
     */
    private void storeToken(UserSessionResult userSessionResult) {
        final boolean result;

        Observable<Boolean> putUserSession = userSessionStore.putUserSession(userSessionResult)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());

        putUserSession.subscribe(new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Logger.e("Error storing new Token to UserSessionStore");
            }

            @Override
            public void onNext(Boolean aBoolean) {
                if(aBoolean)
                    Logger.d("Stored new Session token to UserSessionStore");
            }
        });
    }
}
