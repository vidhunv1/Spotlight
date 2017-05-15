package com.chat.ichat.screens.search;


import com.chat.ichat.models.ContactResult;

import java.util.List;

/**
 * Created by vidhun on 16/12/16.
 */

public class SearchModel {
    private String searchTerm;
    private List<ContactResult> contactsModelList;
    private List<ContactResult> suggestedModelList;
    private ContactResult searchUser;

    public SearchModel(String searchTerm, List<ContactResult> contactsModelList, List<ContactResult> suggestedModelList) {
        this.searchTerm = searchTerm;
        this.contactsModelList = contactsModelList;
        this.suggestedModelList = suggestedModelList;
    }

    public SearchModel(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public List<ContactResult> getContactsModelList() {
        return contactsModelList;
    }

    public void setContactsModelList(List<ContactResult> contactsModelList) {
        this.contactsModelList = contactsModelList;
    }

    public List<ContactResult> getSuggestedModelList() {
        return suggestedModelList;
    }

    public void setSuggestedModelList(List<ContactResult> suggestedModelList) {
        this.suggestedModelList = suggestedModelList;
    }

    public ContactResult getSearchUser() {
        return searchUser;
    }

    public void setSearchUser(ContactResult searchUser) {
        this.searchUser = searchUser;
    }

    @Override
    public String toString() {
        String out = "Search: "+searchTerm+"\n";
        out = out + "SearchUser: "+searchUser+"\n";
        out = out + "Contacts: \n";
        if(contactsModelList!=null) {
            for (ContactResult contactsModel : contactsModelList)
                out = out + contactsModel.toString();
        }
        out = out + "\n Suggested: \n";
        if(suggestedModelList!=null) {
            for (ContactResult contact : suggestedModelList)
                out = out + contact.toString();
        }
        return out;
    }
}
