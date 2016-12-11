package com.stairway.spotlight.screens.home.new_chat.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.home.new_chat.NewChatPresenter;
import com.stairway.spotlight.screens.home.new_chat.GetNewChatsUseCase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 01/09/16.
 */
@Module
public class NewChatViewModule {
    @Provides
    @ViewScope
    public NewChatPresenter providesContactListPresenter(GetNewChatsUseCase getNewChatsUseCase) {
        if(getNewChatsUseCase ==null)
            throw new IllegalStateException("UseCase is null");
        return new NewChatPresenter(getNewChatsUseCase);
    }
}
