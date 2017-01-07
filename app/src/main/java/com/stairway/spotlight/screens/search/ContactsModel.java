package com.stairway.spotlight.screens.search;

/**
 * Created by vidhun on 16/12/16.
 */

public class ContactsModel {
    private String contactName;
    private String userId;
    private String userName;

    public ContactsModel(String contactName, String userId, String userName) {
        this.contactName = contactName;
        this.userId = userId;
        this.userName = userName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
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

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "ContactsModel{" +
                "contactName='" + contactName + '\'' +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
