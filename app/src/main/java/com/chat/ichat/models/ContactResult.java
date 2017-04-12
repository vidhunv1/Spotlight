package com.chat.ichat.models;

import com.chat.ichat.api.user._User;

import java.io.Serializable;

/**
 * Created by vidhun on 01/09/16.
 */
public class ContactResult implements Serializable {
    private String phoneNumber;
    private String countryCode;
    private String contactName;
    private String username;
    private boolean isAdded;
    private boolean isBlocked;
    private String userId;
    private String profileDP;
    private _User.UserType userType;

    public ContactResult(String countryCode, String phoneNumber, String displayName) {
        this.phoneNumber = phoneNumber;
        this.contactName = displayName;
        this.countryCode = countryCode;
    }

    public ContactResult() {}

    public String getProfileDP() {
        return profileDP;
    }

    public void setProfileDP(String profileDP) {
        this.profileDP = profileDP;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isAdded() {
        return isAdded;
    }

    public void setAdded(boolean added) {
        isAdded = added;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public _User.UserType getUserType() {
        return userType;
    }

    public void setUserType(_User.UserType userType) {
        this.userType = userType;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setDisplayName(String displayName) {
        this.contactName = displayName;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof ContactResult) {
            ContactResult temp = (ContactResult) o;
            if(temp.getPhoneNumber().equals(this.getPhoneNumber())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "ContactResult{" +
                "phoneNumber='" + phoneNumber + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", contactName='" + contactName + '\'' +
                ", username='" + username + '\'' +
                ", isAdded=" + isAdded +
                ", isBlocked=" + isBlocked +
                ", userId='" + userId + '\'' +
                ", userType=" + userType +
                '}';
    }
}

