package com.stairway.spotlight.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by vidhun on 08/02/17.
 */

public class Message {
    @SerializedName("text")
    private String text;
    @SerializedName("payload")
    private String payload;
    @SerializedName("button_template")
    private ButtonTemplate buttonTemplate;
    @SerializedName("generic_template")
    private GenericTemplate genericTemplate;

    @SerializedName("quick_replies")
    private List<QuickReply> quickReplies;

    public enum MessageType {
        text,
        image,
        video,
        audio,
        location,
        button_template,
        generic_template,
        unknown
    }

    @Override
    public String toString() {
        String quickRepliesString = "";
        if(quickReplies!=null && !quickReplies.isEmpty()) {
            for (QuickReply q : quickReplies) {
                quickRepliesString = quickRepliesString + "\n" + q.toString();
            }
        }

        return "Message{" +
                "text='" + text + '\'' +
                ", buttonTemplate=" + buttonTemplate +
                ", genericTemplate=" + genericTemplate +
                ", quickReplies=" + quickRepliesString +
                '}';
    }

    public MessageType getMessageType() {
        if(this.text!=null)
            return MessageType.text;
        else if (this.buttonTemplate!=null)
            return MessageType.button_template;
        else if(this.genericTemplate!=null)
            return MessageType.generic_template;
        else
            return MessageType.unknown;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getDisplayText() {
        if(getMessageType() == MessageType.text)
            return getText();
        else if(getMessageType() == MessageType.button_template)
            return getButtonTemplate().getText();
        else if(getMessageType() == MessageType.generic_template)
            return getGenericTemplate().getTitle();
        else
            return "";
    }

    public ButtonTemplate getButtonTemplate() {
        return buttonTemplate;
    }

    public GenericTemplate getGenericTemplate() {
        return genericTemplate;
    }

    public List<QuickReply> getQuickReplies() {
        return quickReplies;
    }
}
