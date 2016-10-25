package com.stairway.spotlight.screens.home.contacts;

import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vidhun on 31/08/16.
 */
public class ContactListContract {
    interface View extends BaseView {
        void displayContactList(ArrayList<ContactListItemModel> contactListItemModels);
        void addContact(ContactListItemModel contactListItemModel);
        void addContacts(List<ContactListItemModel> contactListItemModel);
    }

    interface Presenter extends BasePresenter<ContactListContract.View> {
        void initContactList();
    }
}
