package com.chat.ichat.api.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 16/10/16.
 */
public class _User {
    public _User(String countryCode, String phone) {
        this.phone = phone;
        this.countryCode = countryCode;
    }

    public UserType getUserType() {
        return userType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserTypeRegular() {
        this.userType = UserType.regular;
    }

    public _User() {
    }

    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("country_code")
    @Expose
    private String countryCode;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("password")
    @Expose
    private String password;
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
    @SerializedName("user_type")
    @Expose
    private UserType userType;
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("profile_dp")
    @Expose
    private String profileDP;
    @SerializedName("notification_token")
    @Expose
    private String notificationToken;
    @SerializedName("imei")
    @Expose
    private String imei;
    @SerializedName("mobile_carrier")
    @Expose
    private String mobileCarrier;

    public static enum UserType {
        @SerializedName("official")
        official,
        @SerializedName("regular")
        regular
    }

    public void setIMEI(String imei) {
        this.imei = imei;
    }

    public String getNotificationToken() {
        return notificationToken;
    }

    public void setNotificationToken(String notificationToken) {
        this.notificationToken = notificationToken;
    }

    public void setMobileCarrier(String mobileCarrier) {
        this.mobileCarrier = mobileCarrier;
    }

    public String getProfileDP() {
        return profileDP;
    }

    public void setProfileDP(String profileDP) {
        this.profileDP = profileDP;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    @Override
    public String toString() {
        return "_User{" +
                "phone='" + phone + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", verificationCode='" + verificationCode + '\'' +
                ", name='" + name + '\'' +
                ", isRegistered='" + isRegistered + '\'' +
                ", username='" + username + '\'' +
                ", phoneFormatted='" + phoneFormatted + '\'' +
                ", userType=" + userType +
                ", userId='" + userId + '\'' +
                ", profileDP='" + profileDP + '\'' +
                ", notificationToken='" + notificationToken + '\'' +
                ", imei='" + imei + '\'' +
                ", mobileCarrier='" + mobileCarrier + '\'' +
                '}';
    }
}

