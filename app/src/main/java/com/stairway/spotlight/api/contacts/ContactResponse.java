package com.stairway.spotlight.api.contacts;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by vidhun on 09/12/16.
 */

public class ContactResponse {
    @SerializedName("data")
    @Expose
    private List<_Contact> contacts;

    public List<_Contact> getContacts() {
        return contacts;
    }

    @Override
    public String toString() {
        return "ContactResponse{" +
                "contact=" + contacts +
                '}';
    }
}

