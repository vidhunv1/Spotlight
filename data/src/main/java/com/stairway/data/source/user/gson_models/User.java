package com.stairway.data.source.user.gson_models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 16/10/16.
 */

public class User {
    public User(String countryCode, String phone) {
        this.phone = phone;
        this.countryCode = countryCode;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserTypeRegular() {
        this.userType = "regular";
    }

    public User() {
    }

    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("country_code")
    @Expose
    private String countryCode;
    @SerializedName("verification_code")
    @Expose
    private String verificationCode;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("is_registered")
    @Expose
    private String isRegistered;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("phone_formatted")
    @Expose
    private String phoneFormatted;
    @SerializedName("notification_token")
    @Expose
    private String notificationToken;
    @SerializedName("user_type")
    @Expose
    private String userType;
    @SerializedName("user_id")
    @Expose
    private String userId;

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNotificationToken() {
        return notificationToken;
    }

    public void setNotificationToken(String notificationToken) {
        this.notificationToken = notificationToken;
    }

    public String getPhoneFormatted() {
        return phoneFormatted;
    }

    public void setPhoneFormatted(String phone_formatted) {
        this.phoneFormatted = phone_formatted;
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

    public String getVerificationCode() {
        return verificationCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIsRegistered() {
        return isRegistered;
    }

    public void setIsRegistered(String is_registered) {
        this.isRegistered = is_registered;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
}
