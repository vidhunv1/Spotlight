package com.stairway.spotlight.screens.message.view_models;

/**
 * Created by vidhun on 26/12/16.
 */

import static com.stairway.spotlight.core.lib.MessageXML.*;
public class TextMessage{
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

    public String toXML() {
        if(this.text!=null || !this.text.isEmpty()) {
            return openTag(TAG_HEAD_SHORT) + openTag(TAG_TEXT) + this.text + closeTag(TAG_TEXT) + closeTag(TAG_HEAD_SHORT);
        }
        throw new IllegalStateException("text message is null/empty");
    }
}
