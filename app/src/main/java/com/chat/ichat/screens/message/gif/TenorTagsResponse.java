package com.chat.ichat.screens.message.gif;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by vidhun on 28/05/17.
 */
public class TenorTagsResponse {
    @SerializedName("tags")
    private List<Tags> tags;

    public class Tags {
        @SerializedName("searchterm")
        private String searchTerm;
        @SerializedName("path")
        private String path;
        @SerializedName("image")
        private String image;
        @SerializedName("name")
        private String name;

        public String getSearchTerm() {
            return searchTerm;
        }

        public String getPath() {
            return path;
        }

        public String getImage() {
            return image;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Tags{" +
                    "searchTerm='" + searchTerm + '\'' +
                    ", path='" + path + '\'' +
                    ", image='" + image + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    public List<Tags> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return "TenorTagsResponse{" +
                "tags=" + tags +
                '}';
    }
}
