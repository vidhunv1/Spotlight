package com.stairway.data.source.user;

import com.stairway.data.config.ApiManager;
import com.stairway.data.config.Logger;
import com.stairway.data.source.user.models.User;
import com.stairway.data.source.user.models.UserRequest;
import com.stairway.data.source.user.models.StatusResponse;
import com.stairway.data.source.user.models.UserResponse;

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

    public Observable<StatusResponse> createUser(String countryCode, String mobile){
        UserEndpoint userEndpoint = ApiManager.getInstance().create(UserEndpoint.class);
        Observable<StatusResponse> createUser= userEndpoint.createUser(new UserRequest(countryCode, mobile));

        return createUser;
    }

    public Observable<UserResponse> verifyUser(String countryCode, String mobile, String verificationCode){
        UserEndpoint userEndpoint = ApiManager.getInstance().create(UserEndpoint.class);
        User user = new User(countryCode, mobile);
        user.setUserTypeRegular();
        user.setVerificationCode(verificationCode);

        UserRequest verifyRequest = new UserRequest();
        verifyRequest.setUser(user);

        Observable<UserResponse> verifyUser = userEndpoint.verifyUser(verifyRequest);

        return verifyUser;
    }

    public Observable<UserResponse> updateUser(User user, String authToken){
        UserEndpoint userEndpoint = ApiManager.getInstance(authToken).create(UserEndpoint.class);
        UserRequest userRequest = new UserRequest();
        userRequest.setUser(user);
        Observable<UserResponse> updatedUser = userEndpoint.updateUser(userRequest);
        return updatedUser;
    }
}
