package com.stairway.spotlight.core.di.component;

import com.stairway.data.source.user.UserSessionResult;
import com.stairway.spotlight.core.di.module.UserSessionModule;
import com.stairway.spotlight.core.di.scope.UserSessionScope;
import com.stairway.data.config.XMPPManager;
import com.stairway.spotlight.screens.contacts.di.ContactsViewComponent;
import com.stairway.spotlight.screens.contacts.di.ContactsViewModule;
import com.stairway.spotlight.screens.add_contact.di.AddContactComponent;
import com.stairway.spotlight.screens.add_contact.di.AddContactModule;
import com.stairway.spotlight.screens.home.chats.di.ChatListViewComponent;
import com.stairway.spotlight.screens.home.chats.di.ChatListViewModule;
import com.stairway.spotlight.screens.home.new_chat.di.NewChatViewComponent;
import com.stairway.spotlight.screens.home.new_chat.di.NewChatViewModule;
import com.stairway.spotlight.screens.home.profile.di.ProfileViewComponent;
import com.stairway.spotlight.screens.home.profile.di.ProfileViewModule;
import com.stairway.spotlight.screens.message.di.MessageComponent;
import com.stairway.spotlight.screens.message.di.MessageModule;
import com.stairway.spotlight.screens.register.initialize.di.InitializeViewComponent;
import com.stairway.spotlight.screens.register.initialize.di.InitializeViewModule;
import com.stairway.spotlight.screens.search.di.SearchViewComponent;
import com.stairway.spotlight.screens.search.di.SearchViewModule;

import dagger.Subcomponent;

/**
 * Created by vidhun on 19/07/16.
 */
@UserSessionScope
@Subcomponent(modules = {UserSessionModule.class})
public interface UserSessionComponent {
    UserSessionResult getUserSession();
    XMPPManager getXMPPConnection();

    // Subcomponents
    ChatListViewComponent plus(ChatListViewModule chatListViewModule);
    MessageComponent plus(MessageModule messageModule);
    NewChatViewComponent plus(NewChatViewModule newChatViewModule);
    ContactsViewComponent plus(ContactsViewModule contactsViewModule);
    InitializeViewComponent plus(InitializeViewModule initializeViewModule);
    SearchViewComponent plus(SearchViewModule searchViewModule);
    ProfileViewComponent plus(ProfileViewModule profileViewModule);
    AddContactComponent plus(AddContactModule addContactModule);
}