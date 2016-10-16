package com.stairway.data.source.auth.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 16/10/16.
 */
public class VerifyRequest {
    public VerifyRequest(String countryCode, String mobile, String verificationCode) {
        this.user = new User(countryCode, mobile);
        user.setVerificationCode(verificationCode);
    }

    @SerializedName("user")
    @Expose
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}