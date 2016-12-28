package com.stairway.spotlight.screens.message.view_models;

/**
 * Created by vidhun on 26/12/16.
 */

public class AudioMessage {
    private String url;

    public AudioMessage(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
