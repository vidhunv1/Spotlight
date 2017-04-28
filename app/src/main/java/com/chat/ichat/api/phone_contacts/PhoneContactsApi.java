package com.chat.ichat.api.phone_contacts;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by vidhun on 31/01/17.
 */

public interface PhoneContactsApi {
    @POST("contacts/phone")
    Observable<PhoneContactResponse> createContacts(@Body PhoneContactRequest phoneContactRequest);

    @GET("contacts/phone")
    Observable<PhoneContactResponse> getContacts();
}
