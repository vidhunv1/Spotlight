package com.stairway.data.source.auth;

import com.stairway.data.error.DataException;
import com.stairway.data.manager.ApiManager;
import com.stairway.data.manager.Logger;
import com.stairway.data.source.auth.models.CreateRequest;
import com.stairway.data.source.auth.models.CreateResponse;
import com.stairway.data.source.auth.models.User;

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

    public Observable<CreateResponse> createUser(String countryCode, String mobile){
        UserEndpoint userEndpoint = ApiManager.getInstance().create(UserEndpoint.class);
        Observable<CreateResponse> createUser= userEndpoint.createUser(new CreateRequest(new User(countryCode, mobile)));

        return createUser;
    }
}
