package com.stairway.data.source.user.gson_models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 16/10/16.
 */

public class UserRequest {
    @SerializedName("user")
    @Expose
    private User user;

    public UserRequest(String countryCode, String mobile) {
        this.user = new User(countryCode, mobile);
    }

    public UserRequest(){
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
