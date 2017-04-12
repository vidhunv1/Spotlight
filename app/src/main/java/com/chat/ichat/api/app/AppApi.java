package com.chat.ichat.api.app;

import com.chat.ichat.api.StatusResponse;

import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by vidhun on 26/03/17.
 */

public interface AppApi {
    @GET("app/init")
    Observable<StatusResponse> appInit();

    @GET("app/version/android")
    Observable<VersionResponse> appVersion();
}
