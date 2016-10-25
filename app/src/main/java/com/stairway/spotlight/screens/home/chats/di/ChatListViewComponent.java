package com.stairway.spotlight.screens.home.chats.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.home.chats.ChatListFragment;

import dagger.Subcomponent;

/**
 * Created by vidhun on 14/07/16.
 */
@ViewScope
@Subcomponent(modules = ChatListViewModule.class)
public interface ChatListViewComponent {
    void inject(ChatListFragment chatListFragment);
}
