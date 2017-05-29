package com.chat.ichat.screens.message.gif;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by vidhun on 22/05/17.
 */
public class GiphyGifResponse {
    @SerializedName("data")
    private List<Data> data;

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public class Data {
        @SerializedName("type")
        private String type;
        @SerializedName("images")
        private Image image;
        @SerializedName("id")
        private String id;

        public class Image {
            @SerializedName("fixed_width_small")
            private GifData lowGifData;
            @SerializedName("fixed_width")
            private GifData highGifData;

            public class GifData {
                @SerializedName("url")
                private String gifUrl;
                @SerializedName("width")
                private int width;
                @SerializedName("height")
                private int height;
            }
        }

        public String getLowGifUrl() {
            return image.lowGifData.gifUrl;
        }

        public String getHighGifUrl() {
            return image.highGifData.gifUrl;
        }

        public int getGifWidth() {
            return image.lowGifData.width;
        }

        public int getGifHeight() {
            return image.lowGifData.height;
        }

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }
    }
}
