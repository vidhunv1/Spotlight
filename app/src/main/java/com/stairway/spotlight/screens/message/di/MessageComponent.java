package com.stairway.spotlight.screens.message.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.message.MessageActivity;

import dagger.Subcomponent;

@ViewScope
@Subcomponent(modules = MessageModule.class)
public interface MessageComponent {
    void inject(MessageActivity messageActivity);
}
