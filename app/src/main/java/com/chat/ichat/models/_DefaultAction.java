package com.chat.ichat.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 08/02/17.
 */

public class _DefaultAction {
    @SerializedName("url")
    private String url;
    @SerializedName("fallback_url")
    private String fallbackUrl;
    @SerializedName("payload")
    private String payload;
    @SerializedName("type")
    private Type type;

    public enum Type {
        @SerializedName("web_url")
        web_url,
        @SerializedName("postback")
        postback
    }

    public String getUrl() {
        return url;
    }

    public String getFallbackUrl() {
        return fallbackUrl;
    }

    public String getPayload() {
        return payload;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "_DefaultAction{" +
                "type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", fallbackUrl='" + fallbackUrl + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}
