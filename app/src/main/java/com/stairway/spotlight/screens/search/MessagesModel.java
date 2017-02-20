package com.stairway.spotlight.screens.search;

import org.joda.time.DateTime;

/**
 * Created by vidhun on 16/12/16.
 */

public class MessagesModel {
    private String contactName;
    private String message;
    private DateTime time;
    private String userId;

    public MessagesModel(String userId, String contactName, String message, DateTime time) {
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

    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
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
