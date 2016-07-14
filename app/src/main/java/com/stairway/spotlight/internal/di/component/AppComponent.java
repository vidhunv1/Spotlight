package com.stairway.spotlight.internal.di.component;

import android.content.Context;

import com.stairway.spotlight.internal.di.module.AppModule;
import com.stairway.spotlight.internal.di.module.NetModule;
import com.stairway.spotlight.internal.di.module.UtilModule;
import com.stairway.spotlight.internal.di.scope.ApplicationScope;
import com.stairway.spotlight.screens.LauncherActivity;
import com.stairway.spotlight.screens.home.HomeActivity;
import com.stairway.spotlight.screens.home.chatlist.di.ChatListViewComponent;
import com.stairway.spotlight.screens.home.chatlist.di.ChatListViewModule;

import dagger.Component;

/**
 * Created by vidhun on 12/07/16.
 */
@ApplicationScope
@Component(modules = {AppModule.class, UtilModule.class, NetModule.class})

public interface AppComponent {
    Context appContext();

    ChatListViewComponent plus(ChatListViewModule chatListViewModule);

    // Test
    void inject(HomeActivity activity);
    void inject(LauncherActivity activity);
}
