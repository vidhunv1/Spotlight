package com.stairway.spotlight.screens.home.new_chat.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.home.new_chat.NewChatFragment;

import dagger.Subcomponent;

/**
 * Created by vidhun on 01/09/16.
 */
@ViewScope
@Subcomponent(modules = NewChatViewModule.class)
public interface NewChatViewComponent {
    void inject(NewChatFragment newChatFragment);
}
