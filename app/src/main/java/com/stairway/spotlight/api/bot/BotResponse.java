package com.stairway.spotlight.api.bot;

import com.google.gson.annotations.SerializedName;
import com.stairway.spotlight.api.ErrorResponse;

import java.util.List;

/**
 * Created by vidhun on 10/02/17.
 */

public class BotResponse {
    @SerializedName("data")
    Data data;
    @SerializedName("error")
    ErrorResponse error;

    public Data getData() {
        return data;
    }

    public ErrorResponse getError() {
        return error;
    }

    public boolean isSuccess() {
        return error==null;
    }

    public class Data {
        @SerializedName("username")
        private String username;
        @SerializedName("user_id")
        private String userId;
        @SerializedName("persistent_menu")
        List<PersistentMenu> persistentMenus;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public List<PersistentMenu> getPersistentMenus() {
            return persistentMenus;
        }

        public void setPersistentMenus(List<PersistentMenu> persistentMenus) {
            this.persistentMenus = persistentMenus;
        }

        @Override
        public String toString() {
            String menusString = "";
            if(persistentMenus!=null && !persistentMenus.isEmpty()) {
                for (PersistentMenu persistentMenu : persistentMenus) {
                    menusString = menusString + "\n" + persistentMenu;
                }
            }
            return "BotResponse{" +
                    "username='" + username + '\'' +
                    ", userId='" + userId + '\'' +
                    ", persistentMenus=" + menusString +
                    '}';
        }
    }
}
