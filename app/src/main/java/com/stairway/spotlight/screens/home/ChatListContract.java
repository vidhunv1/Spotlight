package com.stairway.spotlight.screens.home;

import com.stairway.data.source.message.MessageResult;
import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;

import org.jivesoftware.smackx.chatstates.ChatState;

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
        void addNewMessage(MessageResult messageResult);
        void showChatState(String from, ChatState chatState);

    }

    interface Presenter extends BasePresenter<ChatListContract.View> {
        void initChatList();
    }
}
