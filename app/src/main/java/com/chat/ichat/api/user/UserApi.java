package com.chat.ichat.api.user;

import com.chat.ichat.api.StatusResponse;

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

public interface UserApi {
    @POST("users")
    Observable<UserResponse> createUser(@Body UserRequest userRequest);

    @POST("users/verify")
    Observable<UserResponse> verifyUser(@Body VerifyRequest userRequest);

    @PUT("users")
    Observable<UserResponse> updateUser(@Body UserRequest userRequest);

    @Multipart
    @PUT("users")
    Observable<UserResponse> uploadProfileDP(@Part MultipartBody.Part profileDP);

    @GET("users/username/{username}")
    Observable<UserResponse> findUserByUserName(@Path("username") String username);

    @GET("users/id/{user_id}")
    Observable<UserResponse> findUserByUserId(@Path("user_id") String userId);

    @GET("users/logout")
    Observable<StatusResponse> logout();

    @POST("users/login")
    Observable<UserResponse> loginUser(@Body UserRequest userRequest);
}
