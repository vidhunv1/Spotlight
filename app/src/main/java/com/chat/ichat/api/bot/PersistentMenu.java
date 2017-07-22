package com.chat.ichat.api.bot;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 10/02/17.
 */

public class PersistentMenu {
    @SerializedName("type")
    private Type type;
    @SerializedName("title")
    private String title;
    @SerializedName("payload")
    private String payload;
    @SerializedName("url")
    private String url;

    public enum Type {
        @SerializedName("web_url")
        web_url,
        @SerializedName("postback")
        postback
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "PersistentMenu{" +
                "type=" + type +
                ", title='" + title + '\'' +
                ", payload='" + payload + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
