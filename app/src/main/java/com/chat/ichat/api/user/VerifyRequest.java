package com.chat.ichat.api.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 28/04/17.
 */

public class VerifyRequest {
    @SerializedName("country_code")
    @Expose
    private String countryCode;
    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("verification_code")
    @Expose
    private String verificationCode;
    @SerializedName("verification_uuid")
    @Expose
    private String verificationUUID;
    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("notification_token")
    @Expose
    private String notificationToken;

    public VerifyRequest(String countryCode, String phone, String verificationCode, String verificationUUID, String password, String notificationToken) {
        this.countryCode = countryCode;
        this.phone = phone;
        this.verificationCode = verificationCode;
        this.verificationUUID = verificationUUID;
        this.password = password;
        this.notificationToken = notificationToken;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public void setVerificationUUID(String verificationUUID) {
        this.verificationUUID = verificationUUID;
    }
}
