package com.chat.ichat.api;

import com.chat.ichat.core.Logger;

import java.io.IOException;

import retrofit2.adapter.rxjava.HttpException;

/**
 * Created by vidhun on 13/03/17.
 */

public class ApiError {
    private String title;
    private String message;
    private int code;

    public ApiError(Throwable e) {
        if(e instanceof IOException) {
            IOException exception = (IOException) e;
            Logger.d(this, "Network error: "+exception.getMessage());
            if(exception.getMessage().contains("Failed to connect")) {
                this.title = "Server error";
                this.message = "Could not connect to server";
            } else {
                this.title = "Network error";
                this.message = "Please check your connection.";
            }
        } else if(e instanceof HttpException) {
            this.title = "Server error";
            this.message = "Internal server error";
        } else {
            this.title = "Unexpected error";
            this.message = "An unexpected error occured.";
        }
        e.printStackTrace();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
