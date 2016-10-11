package com.stairway.data.source.message;

import java.util.Date;

/**
 * Created by vidhun on 06/08/16.
 */
public class MessageResult {
    private String message;
    private String chatId; //The chat id for screen, contains id of sender
    private String fromId; //
    private DeliveryStatus deliveryStatus;
    private String messageId;
    private String time;

    public static enum DeliveryStatus {
        NOT_SENT,
        SENT,
        DELIVERED,
        READ,
        NOT_AVAILABLE
    }

    public MessageResult(String chatId, String fromId, String message) {
        this.message = message;
        this.chatId = chatId;
        this.fromId = fromId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public MessageResult(String chatId, String fromId, String message, DeliveryStatus deliveryStatus, String time) {
        this.chatId = chatId;
        this.deliveryStatus = deliveryStatus;
        this.fromId = fromId;
        this.message = message;
        this.time = time;
    }

    public MessageResult(String chatId, String fromId, String message, DeliveryStatus deliveryStatus) {
        this.chatId = chatId;
        this.deliveryStatus = deliveryStatus;
        this.fromId = fromId;
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getChatId() {
        return chatId;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public String getFromId() {
        return fromId;
    }

    public String getMessage() {
        return message;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Message: "+message+", DS: "+deliveryStatus+", from: "+fromId+", chatId: "+chatId+", messageId: "+messageId+", time"+getTime();
    }
}