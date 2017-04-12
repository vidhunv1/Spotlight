package com.chat.ichat.api.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 16/10/16.
 */

public class UserRequest {
    @SerializedName("user")
    @Expose
    private _User user;

    public UserRequest(){
        this.user = new _User();
    }

    public _User getUser() {
        return user;
    }

    public void setUser(_User user) {
        this.user = user;
    }
}

