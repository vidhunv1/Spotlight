package com.stairway.spotlight.screens.new_chat;

import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vidhun on 31/08/16.
 */
public class NewChatContract {
    interface View extends BaseView {
        void displayContact(NewChatItemModel newChatItemModel);
        void displayContacts(List<NewChatItemModel> newChatItemModel);
    }

    interface Presenter extends BasePresenter<NewChatContract.View> {
        void initContactList();
    }
}
