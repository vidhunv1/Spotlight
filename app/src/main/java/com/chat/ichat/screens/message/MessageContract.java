package com.chat.ichat.screens.message;

import com.chat.ichat.api.bot.PersistentMenu;
import com.chat.ichat.core.BasePresenter;
import com.chat.ichat.core.BaseView;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.models.MessageResult;

import org.jivesoftware.smackx.chatstates.ChatState;

import java.util.List;

/**
 * Created by vidhun on 06/08/16.
 */
public interface MessageContract {
    interface View extends BaseView {
        void showContactAddedSuccess();
        void showContactBlockedSuccess(boolean isBlocked);
        void setContactDetails(ContactResult contactName);
        void showAddBlock(boolean shouldShow);
        void displayMessages(List<MessageResult> result);
        void addMessageToList(MessageResult message);
        void updateDeliveryStatus(MessageResult messageResult);
        void updateDeliveryStatus(String messageId, String deliveryReceiptId, MessageResult.MessageStatus messageStatus);
        void updateLastActivity(String time);
        void setKeyboardType(boolean isBotKeyboard);
        void initBotMenu(List<PersistentMenu> persistentMenus);
    }

    interface Presenter extends BasePresenter<MessageContract.View> {
        void loadContactDetails(String chatUserName);
        void loadMessages(String chatUserName);
        void sendTextMessage(String toId, String fromId, String message);
        void sendChatState(String chatId, ChatState chatState);
        void updateMessageRead(MessageResult result);
        void getLastActivity(String chatId);
        void sendReadReceipt(String chatId);
        void loadKeyboard(String chatId);
        void addContact(String username);
        void blockContact(String username, boolean shouldBlock);
    }
}
