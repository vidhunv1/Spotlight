package com.stairway.spotlight.screens.add_user;

import com.stairway.data.source.contacts.ContactResult;
import com.stairway.spotlight.core.BasePresenter;
import com.stairway.spotlight.core.BaseView;

/**
 * Created by vidhun on 07/01/17.
 */

public interface AddUserContract {
    interface View extends BaseView {
        void navigateToMessage(String userName);
    }
    interface Presenter extends BasePresenter<AddUserContract.View> {
        void addContact(ContactResult contactResult);
    }
}
