package com.stairway.spotlight.screens.home.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.home.HomeActivity;

import dagger.Subcomponent;

/**
 * Created by vidhun on 14/07/16.
 */
@ViewScope
@Subcomponent(modules = HomeViewModule.class)
public interface HomeViewComponent {
    void inject(HomeActivity homeActivity);
}
