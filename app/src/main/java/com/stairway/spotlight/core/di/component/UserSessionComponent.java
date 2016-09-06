package com.stairway.spotlight.core.di.component;

import com.stairway.data.source.auth.UserSessionResult;
import com.stairway.spotlight.core.di.module.DataModule;
import com.stairway.spotlight.core.di.module.UserSessionModule;
import com.stairway.spotlight.core.di.scope.UserSessionScope;
import com.stairway.data.manager.XMPPManager;
import com.stairway.spotlight.screens.home.chatlist.di.ChatListViewComponent;
import com.stairway.spotlight.screens.home.chatlist.di.ChatListViewModule;
import com.stairway.spotlight.screens.home.contactlist.di.ContactListViewComponent;
import com.stairway.spotlight.screens.home.contactlist.di.ContactListViewModule;
import com.stairway.spotlight.screens.message.di.MessageComponent;
import com.stairway.spotlight.screens.message.di.MessageModule;

import dagger.Subcomponent;

/**
 * Created by vidhun on 19/07/16.
 */
@UserSessionScope
@Subcomponent(modules = {UserSessionModule.class, DataModule.class})
public interface UserSessionComponent {
    UserSessionResult getUserSession();
    XMPPManager getXMPPConnection();

    // Subcomponents
    ChatListViewComponent plus(ChatListViewModule chatListViewModule);
    MessageComponent plus(MessageModule messageModule);
    ContactListViewComponent plus(ContactListViewModule contactListViewModule);

}
