package com.chat.ichat.screens.invite_friends;

import com.chat.ichat.core.BasePresenter;
import com.chat.ichat.core.BaseView;
import com.chat.ichat.models.ContactResult;

import java.util.List;

/**
 * Created by vidhun on 03/06/17.
 */

public class InviteFriendsContract {
    interface View extends BaseView {
        void displayInviteList(List<ContactResult> contactResultList);
    }

    interface Presenter extends BasePresenter<View> {
        void getInviteList();
    }
}
