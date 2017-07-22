package com.chat.ichat.api.bot;

import com.chat.ichat.api.user._User;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by vidhun on 31/05/17.
 */
public class DiscoverBotsResponse {
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;
    @SerializedName("bots")
    private List<Bots> botsList;

    public class Bots {
        @SerializedName("category")
        private String category;
        @SerializedName("cover_picture")
        private String coverPicure;
        @SerializedName("description")
        private String description;
        @SerializedName("bot")
        private _User botUser;

        public String getCategory() {
            return category;
        }

        public _User getBot() {
            return botUser;
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
