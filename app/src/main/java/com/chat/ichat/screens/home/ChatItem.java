package com.chat.ichat.screens.home;

import com.chat.ichat.models.MessageResult;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Created by vidhun on 14/07/16.
 */
public class ChatItem implements Serializable{
    private String chatId;
    private String chatName;
    private String lastMessage;
    private DateTime time;
    private int notificationCount;
    private String receiptId;
    private MessageResult.MessageStatus messageStatus;
    private boolean isMe;
    private String profileDP;
    private String messageId;

    public ChatItem(String chatId, String chatName, String lastMessage, DateTime time, MessageResult.MessageStatus messageStatus, String receiptId, String messageId, int notificationCount) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.lastMessage = lastMessage;
        this.time = time;
        this.notificationCount = notificationCount;
        this.messageStatus = messageStatus;
        this.receiptId = receiptId;
        this.messageId = messageId;
    }

    public ChatItem() {
    }

    public String getProfileDP() {
        return profileDP;
    }

    public void setProfileDP(String profileDP) {
        this.profileDP = profileDP;
    }

    public boolean isMe() {
        return isMe;
    }

    public void setMe(boolean me) {
        isMe = me;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public MessageResult.MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(MessageResult.MessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getChatId() {
        return chatId;
    }

    public String getChatName() {
        return chatName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public DateTime getTime() {
        return time;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public int getNotificationCount() {
        return notificationCount;
    }

    public void setNotificationCount(int notificationCount) {
        this.notificationCount = notificationCount;
    }

    @Override
    public String toString() {
        return "ChatItem{" +
                "chatId='" + chatId + '\'' +
                ", chatName='" + chatName + '\'' +
                ", lastMessage='" + lastMessage + '\'' +
                ", time=" + time +
                ", notificationCount=" + notificationCount +
                ", receiptId='" + receiptId + '\'' +
                ", messageStatus=" + messageStatus +
                ", isMe=" + isMe +
                ", profileDP='" + profileDP + '\'' +
                ", messageId='" + messageId + '\'' +
                '}';
    }
}
