package com.stairway.spotlight.api.contacts;

import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by vidhun on 31/01/17.
 */

public interface ContactsApi {
    @POST("contacts")
    Observable<ContactResponse> createContacts(@Body ContactRequest contactRequest);
}
