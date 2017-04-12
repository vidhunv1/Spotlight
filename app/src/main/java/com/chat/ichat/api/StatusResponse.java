package com.chat.ichat.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 14/03/17.
 */

public class StatusResponse {
    @SerializedName("error")
    ErrorResponse error;
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return error==null;
    }

    public ErrorResponse getError() {
        return error;
    }

    public void setError(ErrorResponse error) {
        this.error = error;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
