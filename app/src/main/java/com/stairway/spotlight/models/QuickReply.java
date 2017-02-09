package com.stairway.spotlight.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 08/02/17.
 */

public class QuickReply {
    @SerializedName("title")
    private String title;
    @SerializedName("payload")
    private String payload;

    public String getTitle() {
        return title;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "_QuickReplies{" +
                "title='" + title + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}
