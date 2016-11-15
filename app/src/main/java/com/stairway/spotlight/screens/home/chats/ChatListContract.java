package com.stairway.spotlight.screens.home.chats;

import com.stairway.data.source.message.MessageResult;
import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;

import java.util.List;

/**
 * Created by vidhun on 13/07/16.
 */
public interface ChatListContract {
    interface View extends BaseView {
        /*
            Delivery status:
                 - waiting to send
                 - sent to server
                 - delivered to recipient
                 - read by recipient
         */
        void setDeliveryStatus(int status, int chatId);

        void displayChatList(List<ChatListItemModel> chatList);

        void onMessageReceived(MessageResult messageResult);
    }

    interface Presenter extends BasePresenter<ChatListContract.View> {
        void initChatList();
    }
}
