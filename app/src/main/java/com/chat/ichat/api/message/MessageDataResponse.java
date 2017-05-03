package com.chat.ichat.api.message;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 01/05/17.
 */

public class MessageDataResponse {
    @SerializedName("data_url")
    @Expose
    private String dataUrl;

    public String getDataUrl() {
        return dataUrl;
    }

    @Override
    public String toString() {
        return "MessageDataResponse{" +
                "dataUrl='" + dataUrl + '\'' +
                '}';
    }
}
