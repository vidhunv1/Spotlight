package com.stairway.spotlight.screens.message.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.message.GetPresenceUseCase;
import com.stairway.spotlight.screens.message.LoadMessagesUseCase;
import com.stairway.spotlight.screens.message.MessagePresenter;
import com.stairway.spotlight.screens.message.ReceiveMessagesUseCase;
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
    public MessagePresenter providesPresenter(LoadMessagesUseCase loadMessagesUseCase,
                                              StoreMessageUseCase storeMessageUseCase,
                                              SendMessageUseCase sendMessageUseCase,
                                              ReceiveMessagesUseCase receiveMessagesUseCase,
                                              GetPresenceUseCase getPresenceUseCase) {
        if(loadMessagesUseCase ==null || storeMessageUseCase ==null || sendMessageUseCase == null || getPresenceUseCase==null)
            throw new IllegalStateException("UseCase is null");
        return new MessagePresenter(loadMessagesUseCase, storeMessageUseCase, sendMessageUseCase, receiveMessagesUseCase, getPresenceUseCase);
    }
}
