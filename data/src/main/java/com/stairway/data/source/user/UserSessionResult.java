package com.stairway.data.source.user;

/**
 * Created by vidhun on 16/07/16.
 */
public class UserSessionResult {
    private String userName;
    private String accessToken;
    private String refreshToken;
    private String countryCode;
    private String phone;
    private String expiry;
    private String userId;

    public UserSessionResult() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public UserSessionResult(String userName) {
        this.userName = userName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "UserSessionResult{" +
                "userId='" + userName + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", phone='" + phone + '\'' +
                ", expiry='" + expiry + '\'' +
                '}';
    }
}
