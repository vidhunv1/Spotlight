package com.stairway.spotlight.screens.message;

import com.stairway.data.source.message.MessageResult;
import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;

import org.jivesoftware.smackx.chatstates.ChatState;

import java.util.List;

/**
 * Created by vidhun on 06/08/16.
 */
public interface MessageContract {
    interface View extends BaseView {
        void displayMessages(List<MessageResult> result);
        void addMessageToList(MessageResult message);
        void updateDeliveryStatus(MessageResult messageResult);
        void updateDeliveryStatus(String deliveryReceiptId, MessageResult.MessageStatus messageStatus);
        void updatePresence(String presence);
    }

    interface Presenter extends BasePresenter<MessageContract.View> {
        void loadMessages(String chatId);
        void sendMessage(MessageResult result);
        void sendChatState(String chatId, ChatState chatState);
        void updateMessageSeen(MessageResult result);
        void getPresence(String chatId);
    }
}
