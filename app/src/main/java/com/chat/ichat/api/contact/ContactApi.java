package com.chat.ichat.api.contact;

import com.chat.ichat.api.user.UserResponse;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;
/**
 * Created by vidhun on 20/05/17.
 */
public interface ContactApi {
    @GET("contacts/add/{user_id}")
    Observable<UserResponse> addContact(@Path("user_id") String userId);

    @GET("contacts/block/{user_id}")
    Observable<UserResponse> blockContact(@Path("user_id") String userId);

    @GET("contacts/unblock/{user_id}")
    Observable<UserResponse> unblockContact(@Path("user_id") String userId);

    @GET("contacts/get")
    Observable<ContactResponse> getContacts();

    @GET("contacts/suggestions")
    Observable<ContactResponse> getUserSuggestions();
}
