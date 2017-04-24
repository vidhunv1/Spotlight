package com.chat.ichat.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 08/02/17.
 */

public class QuickReply {
    @SerializedName("title")
    private String title;
    @SerializedName("payload")
    private String payload;
    @SerializedName("image_url")
    private String imageUrl;
    @SerializedName("content_type")
    @Expose
    private ContentType contentType;

    public static enum ContentType {
        @SerializedName("text")
        text,
        @SerializedName("location")
        location
    }

    public String getTitle() {
        return title;
    }

    public String getPayload() {
        return payload;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public String toString() {
        return "QuickReply{" +
                "title='" + title + '\'' +
                ", payload='" + payload + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", contentType=" + contentType +
                '}';
    }
}
