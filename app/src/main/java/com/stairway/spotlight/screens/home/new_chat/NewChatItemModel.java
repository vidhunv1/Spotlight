package com.stairway.spotlight.screens.home.new_chat;

/**
 * Created by vidhun on 31/08/16.
 */
public class NewChatItemModel {
    private String contactName;
    private boolean inviteFlag;
    private String chatId;
    private String mobileNumber;

    public NewChatItemModel() {
    }

    public NewChatItemModel(String contactName, boolean inviteFlag, String chatId, String mobileNumber) {
        this.contactName = contactName;
        this.inviteFlag = inviteFlag;
        this.chatId = chatId;
        this.mobileNumber = mobileNumber;
    }

    public String getContactName() {
        return contactName;
    }

    public boolean getInviteFlag() {
        return inviteFlag;
    }

    public String getChatId() {
        return chatId;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setInviteFlag(boolean inviteFlag) {
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
