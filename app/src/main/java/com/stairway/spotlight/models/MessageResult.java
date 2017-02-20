package com.stairway.spotlight.models;


import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Created by vidhun on 06/08/16.
 */
public class MessageResult implements Serializable {
    private String message;
    private String chatId; //The chat id for screen, contains id of sender
    private String fromId; //
    private MessageResult.MessageStatus messageStatus;
    private String messageId;
    private String receiptId;
    private boolean isReceiptSent;
    private DateTime time;
    private String name;
    private int unSeenCount;

    public enum MessageStatus {
        NOT_SENT,
        SENT,
        DELIVERED,
        READ,
        // Self
        SEEN,
        UNSEEN
    }

    public MessageResult(String chatId, String fromId, String message) {
        this.message = message;
        this.chatId = chatId;
        this.fromId = fromId;
    }

    public static String getDeliveryStatusText(MessageStatus messageStatus) {
        if(messageStatus == MessageStatus.NOT_SENT) {
            return "Sending";
        } else if(messageStatus == MessageStatus.SENT) {
            return "Sent";
        } else if(messageStatus == MessageStatus.DELIVERED) {
            return "Delivered";
        } else if(messageStatus == MessageStatus.READ) {
            return "Seen";
        } else return "";
    }

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public int getUnSeenCount() {
        return unSeenCount;
    }

    public void setUnSeenCount(int unSeenCount) {
        this.unSeenCount = unSeenCount;
    }

    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
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

    public MessageResult.MessageStatus getMessageStatus() {
        return messageStatus;
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

    public void setMessageStatus(MessageResult.MessageStatus deliveryStatus) {
        this.messageStatus = deliveryStatus;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMe() {
        if(this.chatId!=null && !this.chatId.isEmpty())
            if(this.fromId!=null && !this.fromId.isEmpty())
                if(!this.chatId.equals(this.fromId))
                    return true;
        return false;
    }

    @Override
    public String toString() {
        return "Message: "+message+", DS: "+messageStatus+", from: "+fromId+", chatId: "+chatId+", messageId: "+messageId+", time"+getTime();
    }
}