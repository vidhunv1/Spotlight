package com.stairway.spotlight.screens.home.contactlist;

import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;

import java.util.ArrayList;

/**
 * Created by vidhun on 31/08/16.
 */
public class ContactListContract {
    interface View extends BaseView {
        void displayContactList(ArrayList<ContactListItemModel> contactListItemModels);
    }

    interface Presenter extends BasePresenter<ContactListContract.View> {
        void initContactList();
    }
}
