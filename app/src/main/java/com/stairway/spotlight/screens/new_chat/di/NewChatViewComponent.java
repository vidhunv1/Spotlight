package com.stairway.spotlight.screens.new_chat.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.new_chat.NewChatActivity;

import dagger.Subcomponent;

/**
 * Created by vidhun on 01/09/16.
 */
@ViewScope
@Subcomponent(modules = NewChatViewModule.class)
public interface NewChatViewComponent {
    void inject(NewChatActivity newChatActivity);
}
