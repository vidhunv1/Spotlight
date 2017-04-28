package com.chat.ichat.api.user;

import com.chat.ichat.api.ErrorResponse;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by vidhun on 24/04/17.
 */

public class ContactResponse {
    @SerializedName("error")
    ErrorResponse error;
    @SerializedName("contacts")
    @Expose
    private List<_User> user;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("message")
    @Expose
    private String message;

    public ErrorResponse getError() {
        return error;
    }

    public List<_User> getUser() {
        return user;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
