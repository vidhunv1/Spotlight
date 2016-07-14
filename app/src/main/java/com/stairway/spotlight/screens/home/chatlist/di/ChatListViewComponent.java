package com.stairway.spotlight.screens.home.chatlist.di;

import com.stairway.spotlight.internal.di.scope.ViewScope;
import com.stairway.spotlight.screens.home.chatlist.ChatListFragment;

import dagger.Subcomponent;

/**
 * Created by vidhun on 14/07/16.
 */
@ViewScope
@Subcomponent(modules = ChatListViewModule.class)
public interface ChatListViewComponent {
    void inject(ChatListFragment chatListFragment);
}
