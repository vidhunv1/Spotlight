package com.stairway.spotlight.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 08/02/17.
 */

public class _Button {
    @SerializedName("type")
    private Type type;
    @SerializedName("url")
    private String url;
    @SerializedName("title")
    private String title;
    @SerializedName("payload")
    private String payload;

    public enum Type {
        @SerializedName("web_url")
        web_url,
        @SerializedName("postback")
        postback
    };

    public Type getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "_Button{" +
                "type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}
