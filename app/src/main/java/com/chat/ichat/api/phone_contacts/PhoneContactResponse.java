package com.chat.ichat.api.phone_contacts;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by vidhun on 09/12/16.
 */
public class PhoneContactResponse {
    @SerializedName("data")
    @Expose
    private List<_PhoneContact> contacts;

    public List<_PhoneContact> getContacts() {
        return contacts;
    }

    @Override
    public String toString() {
        return "ContactResponse{" +
                "contact=" + contacts +
                '}';
    }
}

