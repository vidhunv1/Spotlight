package com.stairway.spotlight.screens.contacts;

import com.stairway.data.source.contacts.ContactResult;
import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;

import java.util.List;

/**
 * Created by vidhun on 13/12/16.
 */

public class ContactsContract {
    interface View extends BaseView {
        void contactAdded(String userId);
        void showContacts(List<ContactItemModel> contactResultList);
    }

    interface Presenter extends BasePresenter<ContactsContract.View> {
        void addContact(String userId);
        void loadContacts();
    }
}
