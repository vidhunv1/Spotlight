package com.stairway.spotlight.screens.contacts;

/**
 * Created by vidhun on 13/12/16.
 */

public class ContactItemModel {
    private String contactName;
    private boolean inviteFlag;
    private String mobileNumber;
    private boolean isAdded;
    private boolean isRegistered;
    private String userName;

    public ContactItemModel() {
    }

    public ContactItemModel(String contactName, boolean inviteFlag, String userName, String mobileNumber) {
        this.contactName = contactName;
        this.inviteFlag = inviteFlag;
        this.userName = userName;
        this.mobileNumber = mobileNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isRegistered() {
        return isRegistered;
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

    public boolean getInviteFlag() {
        return inviteFlag;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setInviteFlag(boolean inviteFlag) {
        this.inviteFlag = inviteFlag;
    }



    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    @Override
    public String toString() {
        return "ContactItemModel{" +
                "contactName='" + contactName + '\'' +
                ", inviteFlag=" + inviteFlag +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", isAdded=" + isAdded +
                ", isRegistered=" + isRegistered +
                ", userName='" + userName + '\'' +
                '}';
    }
}
