package com.stairway.spotlight.api.app;

import com.stairway.spotlight.api.StatusResponse;

import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by vidhun on 26/03/17.
 */

public interface AppApi {
    @GET("app/init")
    Observable<StatusResponse> appInit();
}
