package com.stairway.spotlight.screens.welcome.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.welcome.WelcomePresenter;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 22/07/16.
 */
@Module
public class WelcomeModule {
    @Provides
    @ViewScope
    public WelcomePresenter providesWelcomePresenter() {
        return new WelcomePresenter();
    }
}
