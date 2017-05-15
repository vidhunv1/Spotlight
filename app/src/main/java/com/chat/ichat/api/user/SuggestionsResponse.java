package com.chat.ichat.api.user;

import com.chat.ichat.api.phone_contacts._PhoneContact;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by vidhun on 13/05/17.
 */

public class SuggestionsResponse {
    @SerializedName("user")
    @Expose
    private List<_User> users;

    public List<_User> getUsers() {
        return users;
    }

    public void setUsers(List<_User> users) {
        this.users = users;
    }
}
