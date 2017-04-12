package com.chat.ichat.screens.new_chat;

import com.chat.ichat.core.BasePresenter;
import com.chat.ichat.core.BaseView;

import java.util.List;

/**
 * Created by vidhun on 31/08/16.
 */
public class NewChatContract {
    interface View extends BaseView {
        void displayContacts(List<NewChatItemModel> newChatItemModel);
        void showContactAddedSuccess(String contactName, String username, boolean isExistingContact);
        void showInvalidIDError();
    }

    interface Presenter extends BasePresenter<NewChatContract.View> {
        void initContactList();
        void addContact(String userId);
    }
}
