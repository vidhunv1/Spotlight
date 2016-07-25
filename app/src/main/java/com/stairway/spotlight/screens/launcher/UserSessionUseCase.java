package com.stairway.spotlight.screens.launcher;

import com.stairway.data.manager.Logger;
import com.stairway.data.source.auth.UserSessionResult;
import com.stairway.data.source.auth.UserSessionStore;
import com.stairway.data.source.auth.UserAuthApi;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vidhun on 20/07/16.
 */
public class UserSessionUseCase {
    private UserSessionStore userSessionStore;
    private UserAuthApi userAuthApi;

    @Inject
    public UserSessionUseCase(UserAuthApi userAuthApi, UserSessionStore userSessionStore) {
        this.userAuthApi = userAuthApi;
        this.userSessionStore = userSessionStore;
    }

    /*
    Get user session. Flow: Read from store(execute) -> authenticate(token) ->
        if authenticated
            return token
        else
            refreshToken
            storeNewToken(newToken)

     */
    public Observable<UserSessionResult> execute() {
        Logger.d("GETUSERSESSION UseCase");
        Observable<UserSessionResult> getUserSession = Observable.create( (subscriber) -> {
            userSessionStore.getUserSession().subscribe(new Subscriber<UserSessionResult>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    subscriber.onError(e);
                }

                @Override
                public void onNext(UserSessionResult userSession) {
                    authenticate(userSession, subscriber);
                }
            });
        });

        return getUserSession.subscribeOn(Schedulers.newThread());
    }

    /*
        Authenticate access token and check if valid. Update with new token if expired.
    */
    private void authenticate(UserSessionResult userSessionResult, final Subscriber<? super UserSessionResult> subscriber) {
        Observable<Boolean> isAuthenticated = userAuthApi.authenticate(userSessionResult)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());

        isAuthenticated.subscribe(new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                // Return existing token even if error(network error?).
                if(!subscriber.isUnsubscribed())
                    subscriber.onNext(userSessionResult);
            }

            @Override
            public void onNext(Boolean isTokenValid) {
                if(!subscriber.isUnsubscribed()) {
                    if (isTokenValid) {
                        subscriber.onNext(userSessionResult);
                        subscriber.onCompleted();
                    }
                    else
                        refreshtoken(userSessionResult, subscriber);
                }
            }
        });
    }

    /*
    Refresh access token with new token and update UserSessionStore with new access token.
     */
    private void refreshtoken(UserSessionResult userSessionResult, final Subscriber<? super UserSessionResult> subscriber) {
        Observable<UserSessionResult> refreshToken = userAuthApi.refreshToken(userSessionResult)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());

        refreshToken.subscribe(new Subscriber<UserSessionResult>() {
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
                    storeNewToken(userSessionResult);
                    subscriber.onNext(userSessionResult);
                }
            }
        });
    }

    private void storeNewToken(UserSessionResult userSessionResult) {
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
