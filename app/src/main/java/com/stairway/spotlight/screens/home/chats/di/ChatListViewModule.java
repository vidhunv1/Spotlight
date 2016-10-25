package com.stairway.spotlight.screens.home.chats.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.home.chats.ChatListPresenter;
import com.stairway.spotlight.screens.home.chats.GetChatsUseCase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 14/07/16.
 */
@Module
public class ChatListViewModule {
    @Provides
    @ViewScope
    public ChatListPresenter providesChatListPresenter(GetChatsUseCase getChatsUseCase) {
        if(getChatsUseCase==null)
            throw new IllegalStateException("UseCase is null");
        return new ChatListPresenter(getChatsUseCase);
    }
}
