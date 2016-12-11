package com.stairway.data.source.contacts.gson_models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 09/12/16.
 */

public class Contact {
    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("country_code")
    @Expose
    private String countryCode;
    @SerializedName("name")
    @Expose
    private String name;

    public Contact(String phone, String countryCode, String name) {
        this.phone = phone;
        this.countryCode = countryCode;
        this.name = name;
    }
}
