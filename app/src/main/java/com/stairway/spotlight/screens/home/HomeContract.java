package com.stairway.spotlight.screens.home;

import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;
import com.stairway.spotlight.models.MessageResult;

import org.jivesoftware.smackx.chatstates.ChatState;

import java.util.List;

/**
 * Created by vidhun on 13/07/16.
 */
public interface HomeContract {
    interface View extends BaseView {
        /*
            Delivery status:
                 - waiting to send
                 - sent to server
                 - delivered to recipient
                 - read by recipient
         */
        void setDeliveryStatus(int status, int chatId);

        void displayChatList(List<ChatItem> chatList);
        void addNewMessage(MessageResult messageResult);
        void showChatState(String from, ChatState chatState);
    }

    interface Presenter extends BasePresenter<HomeContract.View> {
        void loadChatList();
    }
}
