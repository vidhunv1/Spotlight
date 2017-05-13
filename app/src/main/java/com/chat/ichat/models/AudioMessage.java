package com.chat.ichat.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 08/05/17.
 */

public class AudioMessage {
    @SerializedName("audio_url")
    private String audioUrl;
    @SerializedName("file_uri")
    private String fileUri;

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
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
                "audioUrl='" + audioUrl + '\'' +
                ", fileUri='" + fileUri + '\'' +
                '}';
    }
}
