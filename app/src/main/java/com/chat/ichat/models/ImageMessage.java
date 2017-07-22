package com.chat.ichat.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 01/05/17.
 */

public class ImageMessage {
    @SerializedName("image_url")
    private String imageUrl;
    @SerializedName("file_uri")
    private String fileUri;
    @SerializedName("width")
    private String width;
    @SerializedName("height")
    private String height;
    @SerializedName("data_type")
    private ImageType dataType;

    public enum ImageType {
        @SerializedName("gif")
        gif,
        @SerializedName("image")
        image
    }

    public ImageMessage() {
        this.dataType = ImageType.image;
    }

    public void setDataType(ImageType dataType) {
        this.dataType = dataType;
    }

    public ImageType getDataType() {
        return dataType;
    }

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

    public int getWidth() {
        if(width == null || width.equals("")){
            return 0;
        }
        return Integer.valueOf(width);
    }

    public int getHeight() {
        if(height == null || height.equals("")){
            return 0;
        }
        return Integer.valueOf(height);
    }

    public void setWidth(int width) {
        this.width = width+"";
    }

    public void setHeight(int height) {
        this.height = height+"";
    }

    @Override
    public String toString() {
        return "ImageMessage{" +
                "imageUrl='" + imageUrl + '\'' +
                ", fileUri='" + fileUri + '\'' +
                '}';
    }
}
