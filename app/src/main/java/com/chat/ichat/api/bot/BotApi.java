package com.chat.ichat.api.bot;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by vidhun on 10/02/17.
 */

public interface BotApi {
    @GET("bot/{user_name}")
    public Observable<BotResponse> getBotDetails(@Path("user_name") String userName);
}
