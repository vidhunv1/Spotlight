package com.chat.ichat.screens.message.gif;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by vidhun on 22/05/17.
 */

public interface GifApi {
    @GET("http://api.giphy.com/v1/gifs/trending?api_key=dc6zaTOxFJmzC")
    Observable<GiphyGifResponse> getTrendingGifs();

    @GET("http://api.tenor.com/v1/tags?key=LIVDSRZULELA")
    Observable<TenorTagsResponse> getTenorTags();

    @GET("http://api.tenor.com/v1/search?key=LIVDSRZULELA")
    Observable<TenorGifResponse> getTenorGifs(@Query("tag") CharSequence searchTerm);
}
