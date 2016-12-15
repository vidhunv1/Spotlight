package com.stairway.data.source.contacts.gson_models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 09/12/16.
 */

public class ContactRequest {
    @SerializedName("phone_contact")
    @Expose
    private Contact contact;

    public ContactRequest(String phone, String countryCode, String name) {
        this.contact = new Contact(phone, countryCode, name);
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }
}
