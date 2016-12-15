package com.stairway.spotlight.screens.contacts;

/**
 * Created by vidhun on 13/12/16.
 */

public class ContactItemModel {
    private String contactName;
    private boolean isAdded;
    private boolean isRegistered;
    private String userId;
    private String userName;

    public ContactItemModel(String contactName, String userName, String userId) {
        this.contactName = contactName;
        this.userName = userName;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }

    public boolean isAdded() {
        return isAdded;
    }

    public void setAdded(boolean added) {
        isAdded = added;
    }

    public String getContactName() {
        return contactName;
    }

    @Override
    public String toString() {
        return "ContactItemModel{" +
                "contactName='" + contactName + '\'' +
                ", isAdded=" + isAdded +
                ", isRegistered=" + isRegistered +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
