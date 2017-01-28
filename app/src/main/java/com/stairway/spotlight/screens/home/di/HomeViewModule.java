package com.stairway.spotlight.screens.home.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.home.HomePresenter;
import com.stairway.spotlight.screens.home.GetChatsUseCase;
import com.stairway.spotlight.screens.home.FindUserUseCase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 14/07/16.
 */
@Module
public class HomeViewModule {
    @Provides
    @ViewScope
    public HomePresenter providesChatListPresenter(GetChatsUseCase getChatsUseCase) {
        if(getChatsUseCase==null)
            throw new IllegalStateException("UseCase is null");
        return new HomePresenter(getChatsUseCase);
    }
}
