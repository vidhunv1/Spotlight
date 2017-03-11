package com.stairway.spotlight.models;

import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Set;

/**
 * Created by vidhun on 29/01/17.
 */
public class UserSession {
    private Date expires;
    private String accessToken;
    private String userName;
    private String name;
    private String email;
    private String password;
    private String countryCode;
    private String mobile;
    private String userId;

    public UserSession(String accessToken, String userName, Date expires, String name, String email, String password) {
        this.expires = expires;
        this.accessToken = accessToken;
        this.userName = userName;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public UserSession() {
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Date getExpires() {
        return expires;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isExpired() {
        return new Date().after(this.expires);
    }
}
