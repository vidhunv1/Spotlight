package com.stairway.data.source.contacts;

/**
 * Created by vidhun on 01/09/16.
 */
public class ContactResult {
    private String contactId;
    private String phoneNumber;
    private String countryCode;
    private String contactName;
    private boolean isRegistered;
    private boolean isAdded;

    public ContactResult(String contactId, String countryCode, String phoneNumber, String displayName) {
        this.contactId = contactId;
        this.phoneNumber = phoneNumber;
        this.contactName = displayName;
        this.countryCode = countryCode;
    }

    public ContactResult() {
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

    public String getContactId() {
        return contactId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getDisplayName() {
        return contactName;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setDisplayName(String displayName) {
        this.contactName = displayName;
    }

    @Override
    public String toString() {
        return "ContactsResult{" +
                "contactId=" + contactId +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", contactName='" + contactName + '\'' +
                '}';
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
}
