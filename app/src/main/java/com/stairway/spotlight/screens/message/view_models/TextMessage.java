package com.stairway.spotlight.screens.message.view_models;

/**
 * Created by vidhun on 26/12/16.
 */

public class TextMessage {
    private String text;

    public TextMessage(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
