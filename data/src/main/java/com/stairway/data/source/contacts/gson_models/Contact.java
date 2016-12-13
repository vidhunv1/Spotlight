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
    @SerializedName("is_registered")
    boolean isRegistered;
    @SerializedName("username")
    String username;
    @SerializedName("id")
    String id;

    public Contact(String phone, String countryCode, String name) {
        this.phone = phone;
        this.countryCode = countryCode;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "phone='" + phone + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", name='" + name + '\'' +
                ", isRegistered=" + isRegistered +
                ", username='" + username + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
