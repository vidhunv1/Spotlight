package com.stairway.data.source.contacts.gson_models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 09/12/16.
 */

public class ContactResponse {
    @SerializedName("data")
    @Expose
    private Contact contact;

    public Contact getContact() {
        return contact;
    }

    @Override
    public String toString() {
        return "ContactResponse{" +
                "contact=" + contact +
                '}';
    }
}
