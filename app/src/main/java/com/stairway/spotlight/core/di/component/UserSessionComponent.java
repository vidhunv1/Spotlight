package com.stairway.spotlight.core.di.component;

import com.stairway.data.source.auth.UserSessionResult;
import com.stairway.spotlight.core.di.module.UserSessionModule;
import com.stairway.spotlight.core.di.scope.UserSessionScope;
import com.stairway.spotlight.screens.home.chatlist.di.ChatListViewComponent;
import com.stairway.spotlight.screens.home.chatlist.di.ChatListViewModule;

import dagger.Subcomponent;

/**
 * Created by vidhun on 19/07/16.
 */
@UserSessionScope
@Subcomponent(modules = UserSessionModule.class)
public interface UserSessionComponent {
    UserSessionResult getUserSession();

    ChatListViewComponent plus(ChatListViewModule chatListViewModule);

}
