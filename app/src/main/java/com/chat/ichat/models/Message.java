package com.chat.ichat.models;

import android.support.v4.content.ContextCompat;

import com.chat.ichat.R;
import com.chat.ichat.application.SpotlightApplication;
import com.google.gson.annotations.SerializedName;
import com.chat.ichat.core.Logger;
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
    private List<GenericTemplate> genericTemplate;
    @SerializedName("quick_replies")
    private List<QuickReply> quickReplies;
    @SerializedName("location")
    private LocationMessage locationMessage;
    @SerializedName("image")
    private ImageMessage imageMessage;
    @SerializedName("audio")
    private AudioMessage audioMessage;

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

    public MessageType getMessageType() {
        if(this.text!=null)
            return MessageType.text;
        else if (this.buttonTemplate!=null)
            return MessageType.button_template;
        else if(this.genericTemplate!=null) {
            Logger.d(this, "Type =  GenericTemplate");
            return MessageType.generic_template;
        } else if(this.locationMessage !=null) {
            return MessageType.location;
        } else if(this.imageMessage != null) {
            return MessageType.image;
        } else if(this.audioMessage != null) {
            return MessageType.audio;
        }
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

    public LocationMessage getLocationMessage() {
        return locationMessage;
    }

    public void setLocationMessage(LocationMessage locationMessage) {
        this.locationMessage = locationMessage;
    }

    public ImageMessage getImageMessage() {
        return imageMessage;
    }

    public AudioMessage getAudioMessage() {
        return audioMessage;
    }

    public void setAudioMessage(AudioMessage audioMessage) {
        this.audioMessage = audioMessage;
    }

    public void setImageMessage(ImageMessage imageMessage) {
        this.imageMessage = imageMessage;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getDisplayText() {
        String highlightColor = "#"+Integer.toHexString(ContextCompat.getColor(SpotlightApplication.getContext(), R.color.colorPrimary) & 0x00ffffff );
        if(getMessageType() == MessageType.text) {
            return getText();
        } else if(getMessageType() == MessageType.button_template) {
            if(buttonTemplate.getText()!=null) {
                return buttonTemplate.getText();
            } else {
                return "<font color=\""+highlightColor+"\">"+ "Template" +"</font>";
            }
        } else if(getMessageType() == MessageType.generic_template) {
            if(genericTemplate.get(0).getTitle()!=null) {
                return genericTemplate.get(0).getTitle();
            } else {
                return "<font color=\""+highlightColor+"\">"+ "Template" +"</font>";
            }
        } else if(getMessageType() == MessageType.location) {
            if(locationMessage.getAddress()!=null && !locationMessage.getAddress().isEmpty()) {
                return "<font color=\""+highlightColor+"\">"+ "Location: " + locationMessage.getAddress() +"</font>";
            } else if(locationMessage.getPlaceName()!=null && !locationMessage.getPlaceName().isEmpty()) {
                return "<font color=\""+highlightColor+"\">"+ "Location: " + locationMessage.getPlaceName() +"</font>";
            } else {
                return "<font color=\""+highlightColor+"\">"+ "Location"+"</font>";
            }
        } else if(getMessageType() == MessageType.image) {
            if(getImageMessage().getDataType() == ImageMessage.ImageType.gif) {
                return "<font color=\""+highlightColor+"\">"+ "GIF" +"</font>";
            } else {
                return "<font color=\""+highlightColor+"\">"+ "Image" +"</font>";
            }
        } else if(getMessageType() == MessageType.audio) {
            return "<font color=\""+highlightColor+"\">"+ "Voice Message" +"</font>";
        }
        else
            return "";
    }

    public ButtonTemplate getButtonTemplate() {
        return buttonTemplate;
    }

    public List<GenericTemplate> getGenericTemplate() {
        return genericTemplate;
    }

    public List<QuickReply> getQuickReplies() {
        return quickReplies;
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
}