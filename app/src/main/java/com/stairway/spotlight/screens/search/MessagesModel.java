package com.stairway.spotlight.screens.search;

/**
 * Created by vidhun on 16/12/16.
 */

public class MessagesModel {
    private String contactName;
    private String message;
    private String time;
    private String userId;

    public MessagesModel(String userId, String contactName, String message, String time) {
        this.contactName = contactName;
        this.message = message;
        this.time = time;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "MessagesModel{" +
                "contactName='" + contactName + '\'' +
                ", message='" + message + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
