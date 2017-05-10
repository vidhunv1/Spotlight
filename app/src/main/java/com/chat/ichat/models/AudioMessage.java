package com.chat.ichat.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 08/05/17.
 */

public class AudioMessage {
    @SerializedName("audio_url")
    private String imageUrl;
    @SerializedName("file_uri")
    private String fileUri;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }

    @Override
    public String toString() {
        return "ImageMessage{" +
                "imageUrl='" + imageUrl + '\'' +
                ", fileUri='" + fileUri + '\'' +
                '}';
    }
}
