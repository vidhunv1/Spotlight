package com.stairway.spotlight.screens.message.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.message.GetMessagesUseCase;
import com.stairway.spotlight.screens.message.MessagePresenter;
import com.stairway.spotlight.screens.message.SendMessageUseCase;
import com.stairway.spotlight.screens.message.StoreMessageUseCase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 06/08/16.
 */
@Module
public class MessageModule {
    @Provides
    @ViewScope
    public MessagePresenter providesPresenter(GetMessagesUseCase getMessagesUseCase, StoreMessageUseCase storeMessageUseCase, SendMessageUseCase sendMessageUseCase) {
        if(getMessagesUseCase==null || storeMessageUseCase ==null || sendMessageUseCase == null)
            throw new IllegalStateException("UseCase is null");
        return new MessagePresenter(getMessagesUseCase, storeMessageUseCase, sendMessageUseCase);
    }
}
