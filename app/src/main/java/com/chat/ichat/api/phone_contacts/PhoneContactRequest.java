package com.chat.ichat.api.phone_contacts;

import com.chat.ichat.core.Logger;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.chat.ichat.models.ContactResult;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vidhun on 31/01/17.
 */

public class PhoneContactRequest {
    @SerializedName("phone_contacts")
    @Expose
    private List<_PhoneContact> contacts;

    public PhoneContactRequest(List<ContactResult> contactResultList) {
        List<_PhoneContact> contacts = new ArrayList<>();
        for (ContactResult contactResult : contactResultList) {
            contacts.add(new _PhoneContact(contactResult.getPhoneNumber(), contactResult.getCountryCode(), contactResult.getContactName()));
        }
        this.contacts = contacts;
    }
}
