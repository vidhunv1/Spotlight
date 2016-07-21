package com.stairway.spotlight.screens.home.chatlist;

import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;

import java.util.ArrayList;

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

        void displayChatList(ArrayList<ChatListItemModel> chatList);
    }

    interface Presenter extends BasePresenter<ChatListContract.View> {
        void initChatList();
    }
}
