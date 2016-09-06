package com.stairway.spotlight.screens.home.contactlist;

/**
 * Created by vidhun on 31/08/16.
 */
public class ContactListItemModel {
    private String contactName;
    private String inviteFlag;
    private String chatId;
    private String mobileNumber;

    public ContactListItemModel() {
    }

    public ContactListItemModel(String contactName, String inviteFlag, String chatId, String mobileNumber) {
        this.contactName = contactName;
        this.inviteFlag = inviteFlag;
        this.chatId = chatId;
        this.mobileNumber = mobileNumber;
    }

    public String getContactName() {
        return contactName;
    }

    public String getInviteFlag() {
        return inviteFlag;
    }

    public String getChatId() {
        return chatId;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setInviteFlag(String inviteFlag) {
        this.inviteFlag = inviteFlag;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
}
