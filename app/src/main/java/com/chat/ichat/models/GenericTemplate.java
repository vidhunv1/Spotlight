package com.chat.ichat.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by vidhun on 08/02/17.
 */

public class GenericTemplate {
    @SerializedName("title")
    private String title;
    @SerializedName("image_url")
    private String imageUrl;
    @SerializedName("subtitle")
    private String subtitle;
    @SerializedName("default_action")
    private _DefaultAction defaultAction;
    @SerializedName("buttons")
    private List<_Button> buttons;

    @Override
    public String toString() {
        return "GenericTemplate{" +
                "title='" + title + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", defaultAction=" + defaultAction +
                ", buttons=" + buttons +
                '}';
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public _DefaultAction getDefaultAction() {
        return defaultAction;
    }

    public List<_Button> getButtons() {
        return buttons;
    }
}
