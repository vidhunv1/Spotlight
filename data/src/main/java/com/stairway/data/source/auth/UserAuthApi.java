package com.stairway.data.source.auth;

import com.stairway.data.error.DataException;
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

    public Observable<UserSessionResult> registerUser(String mobile, String otp) {
        // TODO: register user api.
        Logger.v("Register user: ["+mobile+","+otp+"]");
        if(otp.equals("123456"))
            return Observable.just(new UserSessionResult("spotlight", "12345", mobile));

        else {
            Observable<UserSessionResult> registerObservable = Observable.create( subscriber -> {
                subscriber.onError(new DataException("Invalid OTP", DataException.Kind.OTP_INVALID));
            });
            return registerObservable;
        }
    }
}
