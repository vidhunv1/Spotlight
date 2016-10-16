package com.stairway.data.source.contacts;

/**
 * Created by vidhun on 01/09/16.
 */
public class ContactsResult {
    private String contactId;
    private String phoneNumber;
    private String countryCode;
    private String contactName;

    public ContactsResult(String contactId, String countryCode, String phoneNumber, String displayName) {
        this.contactId = contactId;
        this.phoneNumber = phoneNumber;
        this.contactName = displayName;
    }

    public ContactsResult() {
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
        if(o instanceof ContactsResult) {
            ContactsResult temp = (ContactsResult) o;
            if(temp.getPhoneNumber().equals(this.getPhoneNumber())) {
                return true;
            }
        }

        return false;
    }
}
