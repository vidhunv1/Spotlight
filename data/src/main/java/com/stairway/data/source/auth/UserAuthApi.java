package com.stairway.data.source.auth;

import com.stairway.data.manager.Logger;

import rx.Observable;

/**
 * Created by vidhun on 19/07/16.
 */
public class UserAuthApi {
    public Observable<Boolean> authenticate(UserSessionResult userSessionResult) {
        // TODO: Implement strategy for token based authentication
        return Observable.just(true);

    }

    public Observable<UserSessionResult> refreshToken(UserSessionResult userSessionResult) {
        // TODO: Refresh token and return.

        Logger.v("Refresh User Session Token");
        String newAccessToken = "spotlight";
        userSessionResult.setAccessToken(newAccessToken);
        return Observable.just(userSessionResult);
    }

    public Observable<UserSessionResult> registerUser(String mobile, int otp) {
        // TODO: register user api.
        Logger.v("Register user: ["+mobile+","+otp+"]");
        return Observable.just(new UserSessionResult("spotlight", "12345", mobile));

    }
}
