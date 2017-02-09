package com.stairway.spotlight.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by vidhun on 08/02/17.
 */

public class ButtonTemplate {
    @SerializedName("text")
    private String text;
    @SerializedName("buttons")
    List<_Button> buttons;

    public String getText() {
        return text;
    }

    public List<_Button> getButtons() {
        return buttons;
    }

    @Override
    public String toString() {
        String buttonString = "";
        if(buttons!=null && !buttons.isEmpty()) {
            for (_Button button : buttons) {
                buttonString = buttonString + "\n" + button;
            }
        }
        return "_ButtonTemplate{" +
                "text='" + text + '\'' +
                ", buttons=" + buttonString +
                '}';
    }
}
