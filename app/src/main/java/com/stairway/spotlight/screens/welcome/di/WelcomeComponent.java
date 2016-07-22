package com.stairway.spotlight.screens.welcome.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.welcome.WelcomeActivity;

import dagger.Subcomponent;

/**
 * Created by vidhun on 22/07/16.
 */
@ViewScope
@Subcomponent(modules = WelcomeModule.class)
public interface WelcomeComponent {
    void inject(WelcomeActivity welcomeActivity);
}
