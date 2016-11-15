package com.stairway.spotlight.screens.home.chats;

/**
 * Created by vidhun on 14/07/16.
 */
public class ChatListItemModel {
    private String chatId;
    private String chatName;
    private String lastMessage;
    private String time;
    private int notificationCount;

    public ChatListItemModel(String chatId, String chatName, String lastMessage, String time, int notificationCount) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.lastMessage = lastMessage;
        this.time = time;
        this.notificationCount = notificationCount;
    }

    public ChatListItemModel() {
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

    public String getTime() {
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

    public void setTime(String time) {
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
        return "ChatListItemModel{" +
                "chatId='" + chatId + '\'' +
                ", chatName='" + chatName + '\'' +
                ", lastMessage='" + lastMessage + '\'' +
                ", time='" + time + '\'' +
                ", notificationCount=" + notificationCount +
                '}';
    }
}
