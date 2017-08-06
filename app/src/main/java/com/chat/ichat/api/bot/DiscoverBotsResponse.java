package com.chat.ichat.api.bot;

import android.os.Parcelable;

import com.chat.ichat.api.user._User;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by vidhun on 31/05/17.
 */
public class DiscoverBotsResponse implements Serializable{
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;
    @SerializedName("bots")
    private List<Bots> botsList;
    @SerializedName("categories")
    private List<Category> categories;

    public class Bots implements Serializable{
        @SerializedName("category")
        private String category;
        @SerializedName("cover_picture")
        private String coverPicure;
        @SerializedName("description")
        private String description;
        @SerializedName("bot")
        private _User botUser;
        @SerializedName("stars")
        private int stars;

        public String getCategory() {
            return category;
        }

        public _User getBot() {
            return botUser;
        }

        public int getStars() {
            return stars;
        }

        public String getCoverPicure() {
            return coverPicure;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return "Bots{" +
                    "category='" + category + '\'' +
                    ", coverPicure='" + coverPicure + '\'' +
                    ", botUser=" + botUser +
                    '}';
        }
    }

    public class Category {
        @SerializedName("name")
        private String name;
        @SerializedName("description")
        private String description;

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<Bots> getBotsList() {
        return botsList;
    }

    @Override
    public String toString() {
        String b = "";
        for (Bots bots : botsList) {
            b  = b + bots.toString()  +"\n";
        }
        return "DiscoverBotsResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", botsList=" + b +
                '}';
    }
}
