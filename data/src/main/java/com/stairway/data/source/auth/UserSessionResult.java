package com.stairway.data.source.auth;

/**
 * Created by vidhun on 16/07/16.
 */
public class UserSessionResult {
    private String userId;
    private String accessToken;
    private String refreshToken;
    private String countryCode;
    private String phone;
    private String expiry;
    private String chatId;

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public UserSessionResult(String userId) {
        this.userId = userId;
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

    public String getUserId() {
        return userId;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "UserSessionResult{" +
                "userId='" + userId + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", phone='" + phone + '\'' +
                ", expiry='" + expiry + '\'' +
                ", chatId='" + chatId + '\'' +
                '}';
    }
}
