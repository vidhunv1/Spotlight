package com.chat.ichat.api.location;

import com.chat.ichat.api.StatusResponse;
import com.chat.ichat.api.user.UserRequest;
import com.chat.ichat.api.user.UserResponse;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by vidhun on 21/05/17.
 */

public interface LocationApi {
    @POST("location/nearby")
    Observable<NearbyPeopleResponse> getNearbyPeople(@Body SendLocationRequest sendLocationRequest);

    @POST("location/update")
    Observable<StatusResponse> updateLocation(@Body SendLocationRequest sendLocationRequest);

    @GET("location/delete")
    Observable<StatusResponse> delete();
}