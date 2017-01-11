package com.stairway.spotlight.screens.message.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.message.GetNameUseCase;
import com.stairway.spotlight.screens.message.GetPresenceUseCase;
import com.stairway.spotlight.screens.message.LoadMessagesUseCase;
import com.stairway.spotlight.screens.message.MessagePresenter;
import com.stairway.spotlight.screens.message.SendChatStateUseCase;
import com.stairway.spotlight.screens.message.SendMessageUseCase;
import com.stairway.spotlight.screens.message.SendReadReceiptUseCase;
import com.stairway.spotlight.screens.message.StoreMessageUseCase;
import com.stairway.spotlight.screens.message.UpdateMessageUseCase;

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
                                              GetPresenceUseCase getPresenceUseCase,
                                              UpdateMessageUseCase updateMessageUseCase,
                                              SendChatStateUseCase sendChatStateUseCase,
                                              SendReadReceiptUseCase sendReadReceiptUseCase,
                                              GetNameUseCase getNameUseCase) {
        if(loadMessagesUseCase ==null || storeMessageUseCase ==null || sendMessageUseCase == null || getPresenceUseCase==null || updateMessageUseCase==null || sendChatStateUseCase==null || sendReadReceiptUseCase==null || getNameUseCase==null)
            throw new IllegalStateException("UseCase is null");
        return new MessagePresenter(loadMessagesUseCase, storeMessageUseCase, sendMessageUseCase, getPresenceUseCase, updateMessageUseCase, sendChatStateUseCase, sendReadReceiptUseCase, getNameUseCase);
    }
}
