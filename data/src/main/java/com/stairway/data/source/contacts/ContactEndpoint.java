package com.stairway.data.source.contacts;

import com.stairway.data.source.contacts.gson_models.ContactRequest;
import com.stairway.data.source.contacts.gson_models.ContactResponse;

import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by vidhun on 09/12/16.
 */

public interface ContactEndpoint {
    @POST("contacts")
    Observable<ContactResponse> createContact(@Body ContactRequest contactRequest);
}
