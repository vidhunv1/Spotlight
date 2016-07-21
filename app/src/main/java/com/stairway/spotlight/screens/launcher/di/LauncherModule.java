package com.stairway.spotlight.screens.launcher.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.launcher.LauncherPresenter;
import com.stairway.spotlight.screens.launcher.UserSessionUseCase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 21/07/16.
 */
@Module
public class LauncherModule {

    // TODO: How is UserSessionUseCase injected?
    @Provides
    @ViewScope
    public LauncherPresenter providesLauncherPresenter(UserSessionUseCase userSessionUseCase) {
        if(userSessionUseCase == null)
            throw new IllegalStateException("UserSessionUseCase is null");
        return new LauncherPresenter(userSessionUseCase);
    }
}