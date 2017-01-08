package com.stairway.spotlight.screens.new_chat;

/**
 * Created by vidhun on 31/08/16.
 */
public class NewChatItemModel {
    private String contactName;
    private String userName;
    private String userId;


    public NewChatItemModel() {
    }

    public NewChatItemModel(String contactName, String username, String userId) {
        this.contactName = contactName;
        this.userId = userId;
        this.userName = username;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "NewChatItemModel{" +
                "contactName='" + contactName + '\'' +
                ", userName='" + userName + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
