package com.stairway.spotlight.screens.home.chatlist.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.home.chatlist.ChatListPresenter;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 14/07/16.
 */
@Module
public class ChatListViewModule {
    @Provides
    @ViewScope
    public ChatListPresenter providesChatListPresenter() {
        return new ChatListPresenter();
    }
}
