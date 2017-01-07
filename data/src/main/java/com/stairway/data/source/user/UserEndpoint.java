package com.stairway.data.source.user;

import com.stairway.data.source.user.gson_models.UserRequest;
import com.stairway.data.source.user.gson_models.StatusResponse;
import com.stairway.data.source.user.gson_models.UserResponse;

import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by vidhun on 16/10/16.
 */

public interface UserEndpoint {
    @POST("users")
    Observable<StatusResponse> createUser(@Body UserRequest userRequest);

    @POST("users/verify")
    Observable<UserResponse> verifyUser(@Body UserRequest userRequest);

    @PUT("users")
    Observable<UserResponse> updateUser(@Body UserRequest userRequest);

    @Multipart
    @PUT("users")
    Observable<UserResponse> uploadProfileDP(@Part MultipartBody.Part profileDP);

    @GET("users/{username}")
    Observable<UserResponse> findUser(@Path("username") String userName);
}