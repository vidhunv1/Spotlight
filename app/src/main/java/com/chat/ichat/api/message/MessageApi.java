package com.chat.ichat.api.message;

import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import rx.Observable;

/**
 * Created by vidhun on 01/05/17.
 */
public interface MessageApi {
    @Multipart
    @PUT("message/image")
    Observable<MessageDataResponse> uploadImageData(@Part MultipartBody.Part image);
}