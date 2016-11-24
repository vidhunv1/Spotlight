package com.stairway.data.source.message;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by vidhun on 06/08/16.
 */
public class MessageResult implements Serializable {
    private String message;
    private String chatId; //The chat id for screen, contains id of sender
    private String fromId; //
    private MessageStatus messageStatus;
    private String messageId;
    private String receiptId;

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    private String time;
    private String name;
    private int unSeenCount;

    public int getUnSeenCount() {
        return unSeenCount;
    }

    public void setUnSeenCount(int unSeenCount) {
        this.unSeenCount = unSeenCount;
    }

    public static enum MessageStatus {
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public MessageResult(String chatId, String fromId, String message, MessageStatus messageStatus, String time) {
        this.chatId = chatId;
        this.messageStatus = messageStatus;
        this.fromId = fromId;
        this.message = message;
        this.time = time;
    }

    public MessageResult(String chatId, String fromId, String message, MessageStatus messageStatus) {
        this.chatId = chatId;
        this.messageStatus = messageStatus;
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

    public MessageStatus getMessageStatus() {
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

    public void setMessageStatus(MessageStatus deliveryStatus) {
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

    @Override
    public String toString() {
        return "Message: "+message+", DS: "+messageStatus+", from: "+fromId+", chatId: "+chatId+", messageId: "+messageId+", time"+getTime();
    }
}