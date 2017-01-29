package com.stairway.spotlight.screens.register.verifyotp;

import com.stairway.data.config.Logger;
import com.stairway.data.source.user.UserApi;
import com.stairway.data.source.user.UserSessionResult;
import com.stairway.data.source.user.UserSessionStore;
import com.stairway.data.source.user.gson_models.UserResponse;
import com.stairway.spotlight.AccessToken;
import com.stairway.spotlight.AccessTokenManager;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vidhun on 25/07/16.
 */
public class RegisterUseCase {
    private UserApi userApi;
    private AccessTokenManager accessTokenManager;

    @Inject
    public RegisterUseCase(UserApi userApi) {
        this.userApi = userApi;
        accessTokenManager = AccessTokenManager.getInstance();
    }

    public Observable<AccessToken> execute(String countryCode, String mobile, String otp) {
        Observable<AccessToken> register = Observable.create( subscriber -> {
            userApi.verifyUser(countryCode, mobile, otp).subscribe(new Subscriber<UserResponse>() {
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
                public void onNext(UserResponse verifyResponse) {
                    if(!subscriber.isUnsubscribed()){
//                        UserSessionResult userSessionResult = new UserSessionResult(verifyResponse.getUser().getUsername());
//                        userSessionResult.setAccessToken(verifyResponse.getAccessToken());
//                        userSessionResult.setPhone(verifyResponse.getUser().getPhone());
//                        userSessionResult.setCountryCode(verifyResponse.getUser().getCountryCode());
////                        userSessionResult.setExpiry(verifyResponse.getExpiry());
//                        userSessionResult.setUserId(verifyResponse.getUser().getUserId());
//                        userSessionResult.setProfileDP(verifyResponse.getUser().getProfileDP());

                        AccessToken accessToken = new AccessToken(verifyResponse.getAccessToken(), verifyResponse.getUser().getUserId(), verifyResponse.getExpiry());
                        storeToken(accessToken);

                        subscriber.onNext(accessToken);
                    }
                }
            });
        });
        return register.subscribeOn(Schedulers.newThread());
    }

    /*
    Store the Token after authentication.
     */
    private void storeToken(AccessToken accessToken) {
        final boolean result;

        Observable.create(subscriber -> {
            accessTokenManager.save(accessToken);
        });
    }
}
