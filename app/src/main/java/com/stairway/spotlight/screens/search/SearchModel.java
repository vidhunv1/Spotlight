package com.stairway.spotlight.screens.search;

import java.util.List;

/**
 * Created by vidhun on 16/12/16.
 */

public class SearchModel {
    private String searchTerm;
    private List<ContactsModel> contactsModelList;
    private List<MessagesModel> messagesModelList;

    public SearchModel(String searchTerm, List<ContactsModel> contactsModelList, List<MessagesModel> messagesModelList) {
        this.searchTerm = searchTerm;
        this.contactsModelList = contactsModelList;
        this.messagesModelList = messagesModelList;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public List<ContactsModel> getContactsModelList() {
        return contactsModelList;
    }

    public void setContactsModelList(List<ContactsModel> contactsModelList) {
        this.contactsModelList = contactsModelList;
    }

    public List<MessagesModel> getMessagesModelList() {
        return messagesModelList;
    }

    public void setMessagesModelList(List<MessagesModel> messagesModelList) {
        this.messagesModelList = messagesModelList;
    }
}
