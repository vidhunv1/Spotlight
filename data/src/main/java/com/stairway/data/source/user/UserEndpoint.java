package com.stairway.data.source.user;

import com.stairway.data.source.user.models.UserRequest;
import com.stairway.data.source.user.models.StatusResponse;
import com.stairway.data.source.user.models.UserResponse;

import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import rx.Observable;

/**
 * Created by vidhun on 16/10/16.
 */

public interface UserEndpoint {
    @POST("users")
    Observable<StatusResponse> createUser(@Body UserRequest userRequest);

    @POST("users/verify")
    Observable<UserResponse> verifyUser(@Body UserRequest userRequest);

    @PUT("users/")
    Observable<UserResponse> updateUser(@Body UserRequest userRequest);

}