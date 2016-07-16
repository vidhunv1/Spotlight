package com.stairway.spotlight.screens.home.chatlist;

import com.stairway.data.model.ChatListItem;

import java.util.ArrayList;

/**
 * Created by vidhun on 13/07/16.
 */
public interface ChatListContract {
    interface View extends com.stairway.spotlight.core.View {
        /*
            Delivery status:
                 - waiting to send
                 - sent to server
                 - delivered to recipient
                 - read by recipient
         */
        void setDeliveryStatus(int status, int chatId);

        void displayChatList(ArrayList<ChatListItem> chatList);
    }

    interface Presenter extends com.stairway.spotlight.core.Presenter<View> {
        void initChatList();
    }
}
