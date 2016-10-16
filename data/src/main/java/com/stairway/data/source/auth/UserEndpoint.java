package com.stairway.data.source.auth;

import com.stairway.data.source.auth.models.CreateRequest;
import com.stairway.data.source.auth.models.CreateResponse;

import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by vidhun on 16/10/16.
 */

public interface UserEndpoint {
    @POST("users")
    Observable<CreateResponse> createUser(@Body CreateRequest createRequest);
}