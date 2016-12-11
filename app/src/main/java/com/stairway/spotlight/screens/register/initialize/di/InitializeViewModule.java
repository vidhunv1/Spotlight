package com.stairway.spotlight.screens.register.initialize.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.register.initialize.InitializePresenter;
import com.stairway.spotlight.screens.register.initialize.SyncContactsUseCase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 09/12/16.
 */
@Module
public class InitializeViewModule {
    @Provides
    @ViewScope
    public InitializePresenter providesInitializePresenter(SyncContactsUseCase syncContactsUseCase) {
        if(syncContactsUseCase == null)
            throw new IllegalStateException("syncContactsUseCase is null");
        return new InitializePresenter(syncContactsUseCase);
    }
}
