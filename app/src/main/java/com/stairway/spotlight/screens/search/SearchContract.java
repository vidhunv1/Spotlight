package com.stairway.spotlight.screens.search;

import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;

import java.util.List;

/**
 * Created by vidhun on 17/12/16.
 */

public interface SearchContract {
    interface View extends BaseView {
        void displayContacts(String searchQuery, List<ContactsModel> contactsModels);
        void displayMessages(String searchQuery, List<MessagesModel> messagesModels);
    }
    interface Presenter extends BasePresenter<SearchContract.View> {
        void searchContacts(String name);
        void searchMessages(String message);
    }
}
